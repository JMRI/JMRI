package jmri.util;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.*;

import jmri.ShutDownTask;
import jmri.implementation.AbstractShutDownTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2019
 */
public class MockShutDownManagerTest {

    @Test
    public void testCTor() {
        MockShutDownManager dsdm = new MockShutDownManager();
        assertNotNull( dsdm, "exists");
    }

    @Test
    public void testRegister() {
        MockShutDownManager dsdm = new MockShutDownManager();
        assertEquals(0, dsdm.getCallables().size());
        assertEquals(0, dsdm.getRunnables().size());
        ShutDownTask task = new AbstractShutDownTask("task") {
            @Override
            public void run() {
            }
        };
        dsdm.register(task);
        assertEquals(1, dsdm.getCallables().size());
        assertEquals(1, dsdm.getRunnables().size());
        dsdm.register(task);
        assertEquals(1, dsdm.getCallables().size());
        assertEquals(1, dsdm.getRunnables().size());

        Exception ex = Assertions.assertThrows(NullPointerException.class, () -> {
            registerNull(dsdm);
        },"Expected NullPointerException not thrown");
        Assertions.assertNotNull(ex);

    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "testing passing null to create exception ")
    private void registerNull(MockShutDownManager dsdm){
        dsdm.register(null);
    }

    @Test
    public void testDeregister() {
        MockShutDownManager dsdm = new MockShutDownManager();
        assertEquals(0, dsdm.getCallables().size());
        assertEquals(0, dsdm.getRunnables().size());
        ShutDownTask task = new AbstractShutDownTask("task") {
            @Override
            public void run() {
            }
        };
        dsdm.register(task);
        assertEquals(1, dsdm.getCallables().size());
        assertEquals(1, dsdm.getRunnables().size());
        assertTrue(dsdm.getRunnables().contains(task));
        dsdm.deregister(task);
        assertEquals(0, dsdm.getCallables().size());
        assertEquals(0, dsdm.getRunnables().size());
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
        dsdm.setBlockingShutdown(true);
        Frame frame = null;
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new Frame("Shutdown test frame");
        }
        assertFalse(dsdm.isShuttingDown());
        dsdm.shutdown();
        assertTrue(dsdm.isShuttingDown());
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
