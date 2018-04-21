package com.pkukielka;

import java.lang.instrument.Instrumentation;

public class AccessMonitorAgent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Starting AccessMonitorAgent");
        new AccessMonitorTransformer(instrumentation, new ClassMethodSelector(), new MethodRewriter());
    }
}
