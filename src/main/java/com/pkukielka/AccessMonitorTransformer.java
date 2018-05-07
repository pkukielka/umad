package com.pkukielka;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class AccessMonitorTransformer implements ClassFileTransformer {
    private final ClassMethodSelector classMethodSelector;
    private final MethodRewriter methodRewriter;

    AccessMonitorTransformer(final Instrumentation instrumentation, ClassMethodSelector classMethodSelector, MethodRewriter methodRewriter) {
        this.classMethodSelector = classMethodSelector;
        this.methodRewriter = methodRewriter;

        instrumentation.addTransformer(this, true);

        System.out.println("[Info] TracingTransformer active");
    }

    public byte[] transform(final ClassLoader loader, final String className, final Class classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        final String classNameDotted = className.replaceAll("/", ".");
        if (classMethodSelector.shouldTransformClass(classNameDotted)) {
            try {
                final ClassPool classpool = ClassPool.getDefault();
                final ClassPath loaderClassPath = new LoaderClassPath(loader);
                final ClassPath byteArrayClassPath = new ByteArrayClassPath(classNameDotted, byteCode);

                // We add the loaderClassPath so that the classpool can find the dependencies needed when it needs to recompile the class
                classpool.appendClassPath(loaderClassPath);
                // This class has not yet actually been loaded by any classloader, so we must add the class directly so it can be found by the classpool.
                classpool.insertClassPath(byteArrayClassPath);

                final CtClass editableClass = classpool.get(classNameDotted);
                final CtMethod declaredMethods[] = editableClass.getDeclaredMethods();
                for (final CtMethod editableMethod : declaredMethods) {
                    ClassMethodSelector.ClassMethodDefinition md = classMethodSelector.findMatchingDefinition(classNameDotted, editableMethod);
                    if (md != null) {
                        methodRewriter.editMethod(editableMethod, md.ifCalledFrom);
                    }
                }

                byteCode = editableClass.toBytecode();
                editableClass.detach();

                // These appear to only be needed during rewriting
                // If we don't remove, the list just keeps growing as we rewrite more classes
                // or transform the same class again
                classpool.removeClassPath(loaderClassPath);
                classpool.removeClassPath(byteArrayClassPath);

                System.out.println("[Info] Transformed " + classNameDotted);
            } catch (Exception ex) {
                System.err.println("[Error] Unable to transform: " + classNameDotted);
                ex.printStackTrace();
            }
        }

        return byteCode;
    }
}