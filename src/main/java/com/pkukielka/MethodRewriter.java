package com.pkukielka;

import javassist.CannotCompileException;
import javassist.CtMethod;

public class MethodRewriter {
    public void editMethod(final CtMethod editableMethod) throws CannotCompileException {
        String methodName = editableMethod.getDeclaringClass().getSimpleName() + "." + editableMethod.getName();
        editableMethod.insertBefore(String.format("System.out.println(\"### Called %s\");", methodName));
    }
}
