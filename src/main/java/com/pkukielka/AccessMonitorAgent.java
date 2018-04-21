package com.pkukielka;

import java.lang.instrument.Instrumentation;

public class AccessMonitorAgent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Starting AccessMonitorAgent");
        new AccessMonitorTransformer(instrumentation, classMethodSelector(), new MethodRewriter());
    }

    private static ClassMethodSelector classMethodSelector() {
        ClassMethodSelector cls = new ClassMethodSelector();
        cls.addDefinition(".*MyTest.*", ".*");
        return cls;
    }
}
