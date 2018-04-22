package com.pkukielka;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


class LastAccess {
    long timestamp;
    long threadId;
    int hashCode;

    LastAccess(long timestamp, long threadId, int hashCode) {
        this.timestamp = timestamp;
        this.threadId = threadId;
        this.hashCode = hashCode;
    }
}

public class MethodRewriter {
    private static AppConfig conf = new AppConfig();
    private static Map<String, LastAccess> methodCalls = new HashMap<String, LastAccess>();
    private static Set<String> alreadyReported = new HashSet<String>();

    public static void clearState() {
        methodCalls.clear();
        alreadyReported.clear();
    }

    public static void logUnsafeMethodCalls(String methodName, int hashCode) {
        long currentTimestamp = System.currentTimeMillis();
        Thread thread = Thread.currentThread();

        LastAccess last = methodCalls.get(methodName);
        LastAccess current = new LastAccess(currentTimestamp, thread.getId(), hashCode);
        methodCalls.put(methodName, current);

        if (last != null && !alreadyReported.contains(methodName)) {
            if (last.threadId != current.threadId && last.hashCode == current.hashCode && current.timestamp - last.timestamp <= conf.intervalMs) {
                String msg = String.format("Method accessed from multiple threads: %s", methodName);

                alreadyReported.add(methodName);

                if (conf.shouldPrintStackTrace) {
                    System.out.println("[WARN] " + msg);
                }

                if (conf.shouldThrowExceptions) {
                    throw new IllegalThreadStateException(msg);
                }
            }
        }
    }

    public void editMethod(final CtMethod editableMethod) throws CannotCompileException {
        String methodName = editableMethod.getLongName();
        String that = ((editableMethod.getModifiers() & AccessFlag.STATIC) == 0) ? "System.identityHashCode(this)" : "0";
        editableMethod.insertBefore(String.format("com.pkukielka.MethodRewriter.logUnsafeMethodCalls(\"%s\", %s);",
                methodName, that));
    }
}
