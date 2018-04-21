package com.pkukielka;

import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClassMethodSelector {
    private final List<ClassMethodDefinition> definitionList = new ArrayList<ClassMethodDefinition>();

    ClassMethodSelector() {
        for (Config include : (new AppConfig()).includes) {
            definitionList.add(new ClassMethodDefinition(
                    include.getString("class"),
                    include.getString("method")
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
        return definition.classRegex.matcher(classNameDotted).matches() && (methodName == null || definition.methodRegex.matcher(methodName).matches());
    }

    private boolean isMatchingDefinition(final String classNameDotted, final String methodName) {
        for (final ClassMethodDefinition definition : definitionList) {
            if (doesDefinitionMatch(definition, classNameDotted, methodName)) {
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
