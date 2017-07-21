package jmri.managers;

<<<<<<< HEAD
=======
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
>>>>>>> JMRI/master
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
<<<<<<< HEAD
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
=======
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
>>>>>>> JMRI/master
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultShutDownManagerTest {

    @Test
<<<<<<< HEAD
    @Ignore("Causes Exception and hang on appveyor")
    public void testCTor() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
=======
    public void testCTor() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
        // remove the default shutdown hook to prevent crashes stopping tests
        Runtime.getRuntime().removeShutdownHook(dsdm.shutdownHook);
>>>>>>> JMRI/master
        Assert.assertNotNull("exists", dsdm);
    }

    @Test
    public void testRegister() {
        DefaultShutDownManager dsdm = new DefaultShutDownManager();
        List tasks = this.exposeTasks(dsdm);
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
        List tasks = this.exposeTasks(dsdm);
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
<<<<<<< HEAD
        Assert.assertFalse(dsdm.isShuttingDown());
        dsdm.shutdown(0, false);
        Assert.assertTrue(dsdm.isShuttingDown());
=======
        Frame frame = null;
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new Frame("Shutdown test frame");
        }
        Assert.assertFalse(dsdm.isShuttingDown());
        dsdm.shutdown(0, false);
        Assert.assertTrue(dsdm.isShuttingDown());
        if (frame != null) {
            frame.dispose();
        }
>>>>>>> JMRI/master
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
<<<<<<< HEAD
        jmri.util.JUnitUtil.resetInstanceManager();
=======
        JUnitUtil.resetWindows(true);
        JUnitUtil.resetInstanceManager();
>>>>>>> JMRI/master
    }

    @After
    public void tearDown() {
<<<<<<< HEAD
        jmri.util.JUnitUtil.resetInstanceManager();
=======
        JUnitUtil.resetInstanceManager();
>>>>>>> JMRI/master
        apps.tests.Log4JFixture.tearDown();
    }

    private ArrayList exposeTasks(DefaultShutDownManager manager) {
        ArrayList tasks = null;
        try {
            Field f = manager.getClass().getDeclaredField("tasks");
            f.setAccessible(true);
            tasks = (ArrayList) f.get(manager);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Assert.fail("Unable to introspect tasks field");
        }
        return tasks;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultShutDownManagerTest.class.getName());

}
