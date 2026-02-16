package jmri.jmrit.operations;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.netbeans.jemmy.QueueTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.ShutDownManager;
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
        JUnitUtil.initTimeProviderManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.clearShutDownManager();
    }

    private static final boolean WAIT_EVENT_QUEUE_NOT_EMPTY = false;
    private static final boolean CHECK_EVENT_QUEUE_EMPTY = false;
    private static final boolean CHECK_SHUTDOWN_TASK = false;

    @AfterEach
    public void tearDown() {
        if (WAIT_EVENT_QUEUE_NOT_EMPTY) {
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

        if (CHECK_EVENT_QUEUE_EMPTY) {
            final Semaphore sem = new Semaphore(0);
            new Thread( () -> checkJemmyQueueTool(sem),
                "Operations Check Jemmy Thread " + this.getClass().getName()).start();
            try {
                if (!sem.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
                    System.err.println("Check event queue empty failed for test " + this.getClass().getName());
                    fail("Event queue is not empty after this test");
                }
            } catch (InterruptedException e) {
                // ignore.
            }
        }

        JUnitUtil.deregisterBlockManagerShutdownTask();
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
            var list = sm.getCallables();
            while (!list.isEmpty()) {
                var task = list.get(0);
                sm.deregister(task);
                list = sm.getCallables();
                if (CHECK_SHUTDOWN_TASK) {
                    fail("Shutdown task found: " + task);
                }
            }
        }

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    private void checkJemmyQueueTool(Semaphore sem) {
        new QueueTool().waitEmpty(250);
        sem.release();
    }

    private final static Logger log = LoggerFactory.getLogger(OperationsTestCase.class);
}
