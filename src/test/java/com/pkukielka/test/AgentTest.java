package com.pkukielka.test;

import com.pkukielka.MethodRewriter;
import org.junit.Before;
import org.junit.Test;

class MyTest {
    int  interestingMethod() {
        return 42;
    }

    static int interestingStaticMethod() {
        return -1;
    }

    int otherMethod() {
        return 0;
    }
}

public class AgentTest {
    private boolean failed = false;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
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
        MethodRewriter.clearState();
        failed = false;
    }


    @Test
    public void runInterestingMethodInMultipleThreadsWithSingleInstance() throws InterruptedException {
        final MyTest t = new MyTest();
        startThreads(new Runnable() {
            public void run() {
                t.interestingMethod();
            }
        });
        assert (failed);
    }

    @Test
    public void runInterestingMethodInMultipleThreadsWithManyInstances() throws InterruptedException {
        startThreads(new Runnable() {
            public void run() {
                new MyTest().interestingMethod();
            }
        });
        assert (!failed);
    }

    @Test
    public void runOtherMethodInMultipleThreads() throws InterruptedException {
        final MyTest t = new MyTest();
        startThreads(new Runnable() {
            public void run() {
                t.otherMethod();
            }
        });
        assert (!failed);
    }

    @Test
    public void runStaticMethodInMultipleThreads() throws InterruptedException {
        startThreads(new Runnable() {
            public void run() {
                MyTest.interestingStaticMethod();
            }
        });
        assert (!failed);
    }

    @Test
    public void runInterestingMethodInSingleThread() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            new MyTest().interestingMethod();
        }
    }
}
