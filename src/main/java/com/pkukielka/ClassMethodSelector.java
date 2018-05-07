package com.pkukielka;

import com.typesafe.config.Config;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClassMethodSelector {
    private final List<ClassMethodDefinition> includes = new ArrayList<ClassMethodDefinition>();
    private final List<ClassMethodDefinition> excludes = new ArrayList<ClassMethodDefinition>();

    ClassMethodSelector() {
        for (Config include : (new AppConfig()).includes) {
            includes.add(new ClassMethodDefinition(
                    include.getString("class"),
                    include.getString("method"),
                    include.hasPath("ifCalledFrom") ? include.getString("ifCalledFrom") : null,
                    include.hasPath("onlyPublicMethods") && include.getBoolean("onlyPublicMethods")
            ));
        }
        for (Config exclude : (new AppConfig()).excludes) {
            excludes.add(new ClassMethodDefinition(
                    exclude.getString("class"),
                    exclude.getString("method"),
                    null,
                    false
            ));
        }
    }

    boolean shouldTransformClass(final String classNameDotted) {
        return isMatchingDefinition(classNameDotted, null);
    }

    boolean shouldTransform(final String classNameDotted, final CtMethod editableMethod) {
        return isMatchingDefinition(classNameDotted, editableMethod);
    }

    private boolean doesDefinitionMatch(final ClassMethodDefinition definition, final String classNameDotted, final CtMethod editableMethod) {
        return definition.classRegex.matcher(classNameDotted).matches() &&
                (editableMethod == null || (
                        !editableMethod.isEmpty() &&
                        (editableMethod.getModifiers() & AccessFlag.STATIC) == 0) &&
                        definition.methodRegex.matcher(editableMethod.getName()).matches() &&
                        (!definition.onlyPublicMethods || (editableMethod.getModifiers() & AccessFlag.PUBLIC) != 0));
    }

    ClassMethodDefinition findMatchingDefinition(final String classNameDotted, final CtMethod editableMethod) {
        // We are excluding only particular methods of the class, never pre-exclude whole class
        if (editableMethod != null) {
            for (final ClassMethodDefinition exclude : excludes) {
                if (doesDefinitionMatch(exclude, classNameDotted, editableMethod)) {
                    return null;
                }
            }
        }
        for (final ClassMethodDefinition include : includes) {
            if (doesDefinitionMatch(include, classNameDotted, editableMethod)) {
                return include;
            }
        }
        return null;
    }

    private boolean isMatchingDefinition(final String classNameDotted, final CtMethod editableMethod) {
        return findMatchingDefinition(classNameDotted, editableMethod) != null;
    }

    static class ClassMethodDefinition {
        final Pattern classRegex;
        final Pattern methodRegex;
        final String ifCalledFrom;
        final boolean onlyPublicMethods;


        ClassMethodDefinition(String classRegex, String methodRegex, String ifCalledFrom, boolean onlyPublicMethods) {
            this.classRegex = Pattern.compile(classRegex);
            this.methodRegex = Pattern.compile(methodRegex);
            this.ifCalledFrom = ifCalledFrom;
            this.onlyPublicMethods = onlyPublicMethods;
        }
    }
}
