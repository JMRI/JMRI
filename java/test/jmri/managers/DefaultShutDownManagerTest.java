package jmri.managers;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @SuppressWarnings("null")
    @Test
    public void testRegister() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
        List<?> tasks = this.exposeTasks(dsdm);
        Assert.assertEquals(0, tasks.size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public boolean execute() {
                return true;
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, tasks.size());
        dsdm.register(task);
        Assert.assertEquals(1, tasks.size());
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
        List<?> tasks = this.exposeTasks(dsdm);
        Assert.assertEquals(0, tasks.size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public boolean execute() {
                return true;
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, tasks.size());
        Assert.assertTrue(tasks.contains(task));
        dsdm.deregister(task);
        Assert.assertEquals(0, tasks.size());
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private ArrayList<?> exposeTasks(DefaultShutDownManager manager) {
        ArrayList<?> tasks = null;
        try {
            Field f = manager.getClass().getDeclaredField("tasks");
            f.setAccessible(true);
            tasks = (ArrayList<?>) f.get(manager);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Assert.fail("Unable to introspect tasks field");
        }
        return tasks;
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultShutDownManagerTest.class);

}
