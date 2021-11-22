package jmri.util;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2019
 */
public class MockShutDownManagerTest {

    @Test
    public void testCTor() {
        MockShutDownManager dsdm = new MockShutDownManager();
        Assert.assertNotNull("exists", dsdm);
    }

    @Test
    public void testRegister() {
        MockShutDownManager dsdm = new MockShutDownManager();
        Assert.assertEquals(0, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public void run() {
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getCallables().size());
        Assert.assertEquals(1, dsdm.getRunnables().size());
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getCallables().size());
        Assert.assertEquals(1, dsdm.getRunnables().size());
        try {
            dsdm.register(null);
            Assert.fail("Expected NullPointerException not thrown");
        } catch (NullPointerException ex) {
            // ignore since throwing the NPE is passing
        }
    }

    @Test
    public void testDeregister() {
        MockShutDownManager dsdm = new MockShutDownManager();
        Assert.assertEquals(0, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public void run() {
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getCallables().size());
        Assert.assertEquals(1, dsdm.getRunnables().size());
        Assert.assertTrue(dsdm.getRunnables().contains(task));
        dsdm.deregister(task);
        Assert.assertEquals(0, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
    }

    /**
     * Test that isShuttingDown is correct. Note that if this is last test
     * before the JVM actually shuts down, it is an indication that someone
     * incorrectly modified jmri.util.MockShutDownManager.shutdown() or
     * jmri.managers.DefaultShutDownManager.shutdown(int, boolean) incorrectly
     * to forcably call System.exit()
     */
    @Test
    public void testIsShuttingDown() {
        MockShutDownManager dsdm = new MockShutDownManager();
        Frame frame = null;
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new Frame("Shutdown test frame");
        }
        Assert.assertFalse(dsdm.isShuttingDown());
        dsdm.shutdown();
        Assert.assertTrue(dsdm.isShuttingDown());
        if (frame != null) {
            JUnitUtil.dispose(frame);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MockShutDownManagerTest.class);

}
