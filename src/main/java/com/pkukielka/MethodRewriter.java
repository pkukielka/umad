package com.pkukielka;

import javassist.CannotCompileException;
import javassist.CtMethod;

import java.util.HashMap;
import java.util.Map;

public class MethodRewriter {
    static AppConfig conf = new AppConfig();
    static Map<String, Long> methodCalls = new HashMap<String, Long>();

    private static String stackTrace(StackTraceElement[] st) {
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < st.length; i++) {
            sb.append(st[i].toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void logUnsafeMethodCalls() {
        String stackTrace = stackTrace(Thread.currentThread().getStackTrace());
        long currentCallTime = System.currentTimeMillis();
        long lastCallTime = methodCalls.containsKey(stackTrace) ? methodCalls.get(stackTrace) : 0;

        methodCalls.put(stackTrace, currentCallTime);

        if (currentCallTime - lastCallTime <= conf.intervalMs) {
            if (conf.shouldPrintStackTrace) {
                System.out.println("[WARN] Code accessed from multiple threads:\n" + stackTrace);
            }
            if (conf.shouldThrowExceptions) {
                throw new IllegalThreadStateException(stackTrace);
            }
        }
    }

    public void editMethod(final CtMethod editableMethod) throws CannotCompileException {
        editableMethod.insertBefore("com.pkukielka.MethodRewriter.logUnsafeMethodCalls();");
    }
}
