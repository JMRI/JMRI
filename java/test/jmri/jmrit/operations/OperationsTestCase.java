package jmri.jmrit.operations;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.QueueTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 * Common setup and tear down for operation tests.
 *
 * @author Dan Boudreau Copyright (C) 2015
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class OperationsTestCase {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        reset();
        JUnitOperationsUtil.setupOperationsTests();
    }

    // Set things up outside of operations
    public void reset() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.clearShutDownManager();
    }

    private final boolean waitOnEventQueueNotEmpty = false;
    private final boolean checkEventQueueEmpty = false;
    private final boolean checkShutDownTask = false;

    @AfterEach
    public void tearDown() {
        if (waitOnEventQueueNotEmpty) {
            Thread AWT_EventQueue = JUnitUtil.getThreadStartsWithName("AWT-EventQueue");
            if (AWT_EventQueue != null) {
                if (AWT_EventQueue.isAlive()) {
                    log.debug("event queue running");
                }
                try {
                    AWT_EventQueue.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (checkEventQueueEmpty) {
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
                    System.err.println("Check event queue empty failed for test " + this.getClass().getName());
                    Assert.fail("Event queue is not empty after this test");
                }
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
            List<ShutDownTask> list = sm.tasks();
            while (list.size() > 0) {
                ShutDownTask task = list.get(0);
                sm.deregister(task);
                if (checkShutDownTask) {
                    Assert.fail("Shutdown task found: " + task.getName());
                }
            }
        }

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(OperationsTestCase.class);
}
