package com.pkukielka.test;

import org.junit.Before;
import org.junit.Test;


public class AgentTest {
    private class MyTest {
        void interestingMethod() {
        }

        void otherMethod() {
        }
    }

    private boolean failed = false;

    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            if (ex.getClass() == IllegalThreadStateException.class) failed = true;
        }
    };

    private void startThreads(Runnable r) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            t.start();
            t.join();
        }
    }

    @Before
    public void setUp() {
        failed = false;
    }

    @Test
    public void runInterestingMethodInMultipleThreads() throws InterruptedException {
        startThreads(new Runnable() {
            public void run() {
                new MyTest().interestingMethod();
            }
        });
        assert (failed);
    }

    @Test
    public void runOtherMethodInMultipleThreads() throws InterruptedException {
        startThreads(new Runnable() {
            public void run() {
                new MyTest().otherMethod();
            }
        });
        assert (!failed);
    }
}
