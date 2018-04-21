package com.pkukielka.test;

import org.junit.Test;


public class AgentTest {

    class MyTest {
        void someMethod() {
            System.out.println("someMethod");
        }
        void otherMethod() {
            System.out.println("otherMethod");
        }
    }

    @Test
    public void runWithAgent() {
        MyTest t = new MyTest();
        t.someMethod();
        t.otherMethod();
    }
}
