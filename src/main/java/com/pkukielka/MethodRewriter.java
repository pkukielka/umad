package com.pkukielka;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


class LastAccess {
    long timestamp;
    long threadId;
    int hashCode;
    String stackTrace;
    String threadName;

    LastAccess(long timestamp, long threadId, int hashCode, String threadName) {
        this.timestamp = timestamp;
        this.threadId = threadId;
        this.hashCode = hashCode;
        this.stackTrace = null;
        this.threadName = threadName;
    }
}

public class MethodRewriter {
    private static final AppConfig conf = new AppConfig();
    private static final Map<String, LastAccess> methodCalls = new HashMap<String, LastAccess>();
    private static final Set<String> alreadyReported = new HashSet<String>();

    public static void clearState() {
        methodCalls.clear();
        alreadyReported.clear();
    }


    public static int realStackStartIndex = 2;

    public static void logUnsafeMethodCalls(String methodName, String ifCalledFrom, int hashCode) {
        synchronized (conf) {
            Thread thread = Thread.currentThread();
            Long currentTimestamp = System.currentTimeMillis();

            LastAccess current = new LastAccess(currentTimestamp, thread.getId(), hashCode, thread.getName());
            LastAccess last = methodCalls.put(methodName, current);

            if (last != null &&
                    last.threadId != current.threadId &&
                    last.hashCode == current.hashCode &&
                    current.timestamp - last.timestamp <= conf.intervalMs)
            {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

                String calledFrom = stackTrace[realStackStartIndex + 1].toString();

                Pattern ifCalledFromPattern = Pattern.compile(ifCalledFrom);
                methodName = (ifCalledFrom.equals("null")) ? methodName :
                        (ifCalledFromPattern.matcher(calledFrom).matches() ? calledFrom : null);

                if (methodName != null && alreadyReported.add(methodName)) {
                    String msg = String.format("Method accessed from multiple threads (%s, %s): %s",
                            last.threadName, current.threadName, methodName);

                    StringBuilder str = new StringBuilder("[WARN] " + msg + "\n");
                    for (int i = realStackStartIndex; i < stackTrace.length; i++) {
                        str.append("    ").append(stackTrace[i].toString()).append("\n");
                    }
                    String stack = str.toString();
                    current.stackTrace = stack;

                    if (conf.shouldPrintStackTrace) System.out.println(stack);
                    if (conf.shouldThrowExceptions) throw new IllegalThreadStateException(msg);
                }
            }
        }

    }

    void editMethod(final CtMethod editableMethod, String ifCalledFrom) throws CannotCompileException {
        String methodName = editableMethod.getLongName();
        editableMethod.insertBefore(
                String.format("com.pkukielka.MethodRewriter.logUnsafeMethodCalls(\"%s\", \"%s\", System.identityHashCode(this));",
                        methodName, ifCalledFrom));
    }
}
