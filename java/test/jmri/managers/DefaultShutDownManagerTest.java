package jmri.managers;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultShutDownManagerTest {

    @Test
    public void testCTor() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
        // remove the default shutdown hook to prevent crashes stopping tests
        Runtime.getRuntime().removeShutdownHook(dsdm.shutdownHook);
        Assert.assertNotNull("exists", dsdm);
    }

    @Test
    public void testRegister() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
        Assert.assertEquals(0, dsdm.tasks().size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public boolean execute() {
                return true;
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.tasks().size());
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.tasks().size());
        try {
            dsdm.register(null);
            Assert.fail("Expected NullPointerException not thrown");
        } catch (NullPointerException ex) {
            // ignore since throwing the NPE is passing
        }
    }

    @Test
    public void testDeregister() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
        Assert.assertEquals(0, dsdm.tasks().size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public boolean execute() {
                return true;
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.tasks().size());
        Assert.assertTrue(dsdm.tasks().contains(task));
        dsdm.deregister(task);
        Assert.assertEquals(0, dsdm.tasks().size());
    }

    @Test
    public void testIsShuttingDown() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
        Frame frame = null;
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new Frame("Shutdown test frame");
        }
        Assert.assertFalse(dsdm.isShuttingDown());
        dsdm.shutdown(0, false);
        Assert.assertTrue(dsdm.isShuttingDown());
        if (frame != null) {
            JUnitUtil.dispose(frame);
        }
    }

    @Test
    public void testInstanceManagerCreates() {
        jmri.ShutDownManager sdm = jmri.InstanceManager.getNullableDefault(jmri.ShutDownManager.class);
        Assert.assertNotNull(sdm);
    }
    
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultShutDownManagerTest.class);

}
