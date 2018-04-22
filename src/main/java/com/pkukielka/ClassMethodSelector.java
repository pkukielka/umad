package com.pkukielka;

import com.typesafe.config.Config;

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
                    include.getString("method")
            ));
        }
        for (Config exclude : (new AppConfig()).excludes) {
            excludes.add(new ClassMethodDefinition(
                    exclude.getString("class"),
                    exclude.getString("method")
            ));
        }
    }

    public boolean shouldTransformClass(final String classNameDotted) {
        return isMatchingDefinition(classNameDotted, null);
    }

    public boolean shouldTransform(final String classNameDotted, final String methodName) {
        return isMatchingDefinition(classNameDotted, methodName);
    }

    private boolean doesDefinitionMatch(final ClassMethodDefinition definition, final String classNameDotted, final String methodName) {
        return definition.classRegex.matcher(classNameDotted).matches() &&
                (methodName == null || definition.methodRegex.matcher(methodName).matches());
    }

    private boolean isMatchingDefinition(final String classNameDotted, final String methodName) {
        if (methodName != null) {
            for (final ClassMethodDefinition exclude : excludes) {
                if (doesDefinitionMatch(exclude, classNameDotted, methodName)) {
                    return false;
                }
            }
        }
        for (final ClassMethodDefinition include : includes) {
            if (doesDefinitionMatch(include, classNameDotted, methodName)) {
                return true;
            }
        }
        return false;
    }

    static class ClassMethodDefinition {
        final Pattern classRegex;
        final Pattern methodRegex;

        ClassMethodDefinition(String classRegex, String methodRegex) {
            this.classRegex = Pattern.compile(classRegex);
            this.methodRegex = Pattern.compile(methodRegex);
        }
    }
}
