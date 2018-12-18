package jmri.jmrit.operations;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.netbeans.jemmy.QueueTool;

/**
 * Common setup and tear down for operation tests.
 *
 * @author Dan Boudreau Copyright (C) 2015
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class OperationsTestCase {

    @Before
    public void setUp() {
        JUnitUtil.setUp();

        // Set things up outside of operations
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.initShutDownManager();
        JUnitUtil.resetProfileManager();

        JUnitOperationsUtil.resetOperationsManager();
    }
    
    private final boolean testThreadRunning = false;

    @After
    public void tearDown() {
        if (testThreadRunning) {
            final Semaphore sem = new Semaphore(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new QueueTool().waitEmpty(250);
                    sem.release();
                }
            }).start();
            try {
                if (!sem.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
                    System.err.println("Jemmy exit wait failed for test " + this.getClass().getName());
                    Assert.fail("Jemmy is not empty after this test");
                }
            } catch (InterruptedException e) {
                // ignore.
            }
        }
        JUnitUtil.tearDown();
    }
}
