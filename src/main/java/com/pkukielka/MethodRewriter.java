package com.pkukielka;

import javassist.CannotCompileException;
import javassist.CtMethod;

import java.util.HashMap;
import java.util.Map;


class LastAccess {
    long timestamp;
    long threadId;
    String threadName;

    LastAccess(long timestamp, long threadId, String threadName) {
        this.timestamp = timestamp;
        this.threadId = threadId;
        this.threadName = threadName;
    }
}

public class MethodRewriter {
    private static AppConfig conf = new AppConfig();
    private static Map<String, LastAccess> methodCalls = new HashMap<String, LastAccess>();

    public static void clearState() {
        methodCalls.clear();
    }

    public static void logUnsafeMethodCalls(String methodName) {
        long currentTimestamp = System.currentTimeMillis();
        Thread thread = Thread.currentThread();

        LastAccess last = methodCalls.get(methodName);
        LastAccess current = new LastAccess(currentTimestamp, thread.getId(), thread.getName());
        methodCalls.put(methodName, current);

        if (last != null) {
            if (last.threadId != current.threadId && current.timestamp - last.timestamp <= conf.intervalMs) {
                String msg = String.format(
                        "Method accessed from multiple threads (%s, %s):\n%s",
                        last.threadName, current.threadName, methodName);

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
        editableMethod.insertBefore(String.format("com.pkukielka.MethodRewriter.logUnsafeMethodCalls(\"%s\");", methodName));
    }
}
