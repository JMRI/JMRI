package jmri.managers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.AbstractShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2020
 */
public class DefaultShutDownManagerTest {

    private int runs;
    private DefaultShutDownManager dsdm;

    @Test
    public void testCTor() {
        // remove the default shutdown hook to prevent crashes stopping tests
        Runtime.getRuntime().removeShutdownHook(dsdm.shutdownHook);
        Assert.assertNotNull("exists", dsdm);
    }

    @Test
    public void testRegister_Task() {
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public void run() {
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getRunnables().size());
        Assert.assertEquals(1, dsdm.getCallables().size());
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getRunnables().size());
        Assert.assertEquals(1, dsdm.getCallables().size());
        assertThatCode(() -> dsdm.register((ShutDownTask) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testDeregister_Task() {
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        ShutDownTask task = new QuietShutDownTask("task") {
            @Override
            public void run() {
            }
        };
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getRunnables().size());
        Assert.assertEquals(1, dsdm.getCallables().size());
        Assert.assertTrue(dsdm.getCallables().contains(task));
        dsdm.deregister(task);
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        assertThatCode(() -> dsdm.deregister((ShutDownTask) null)).doesNotThrowAnyException();
    }

    @Test
    public void testRegister_Callable() {
        Assert.assertEquals(0, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Callable<Boolean> task = () -> true;
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        assertThatCode(() -> dsdm.register((Callable<Boolean>) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testDeregister_Callable() {
        Assert.assertEquals(0, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Callable<Boolean> task = () -> true;
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertTrue(dsdm.getCallables().contains(task));
        dsdm.deregister(task);
        Assert.assertEquals(0, dsdm.getCallables().size());
        Assert.assertEquals(0, dsdm.getRunnables().size());
        assertThatCode(() -> dsdm.deregister((Callable<Boolean>) null)).doesNotThrowAnyException();
    }

    @Test
    public void testRegister_Runnable() {
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        Runnable task = () -> {};
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        assertThatCode(() -> dsdm.register((Runnable) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testDeregister_Runnable() {
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        Runnable task = () -> {};
        dsdm.register(task);
        Assert.assertEquals(1, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        Assert.assertTrue(dsdm.getRunnables().contains(task));
        dsdm.deregister(task);
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        assertThatCode(() -> dsdm.deregister((Runnable) null)).doesNotThrowAnyException();
    }

    @Test
    public void testIsShuttingDown() {
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
    public void testShutDownisInterruptedByShutDownTask() {
        dsdm.register(new AbstractShutDownTask("test") {

            @Override
            public Boolean call() {
                return false;
            }

            @Override
            public void run() {
                runs++;
            }});
        dsdm.shutdown(0, false);
        assertThat(dsdm.isShuttingDown()).isFalse();
        assertThat(runs).isEqualTo(0);
    }

    @Test
    public void testShutDownisNotInterruptedByShutDownTask() {
        dsdm.register(new AbstractShutDownTask("test") {

            @Override
            public Boolean call() {
                return true;
            }

            @Override
            public void run() {
                runs++;
            }});
        dsdm.shutdown(0, false);
        assertThat(dsdm.isShuttingDown()).isTrue();
        assertThat(runs).isEqualTo(1);
    }

    @Test
    public void testShutDownisInterruptedByCallableFalse() {
        dsdm.register(() -> runs++);
        dsdm.register(() -> Boolean.FALSE);
        dsdm.shutdown(0, false);
        assertThat(dsdm.isShuttingDown()).isFalse();
        assertThat(runs).isEqualTo(0);
    }

    @Test
    public void testShutDownisInterruptedByCallableException() {
        dsdm.register(() -> runs++);
        dsdm.register(() -> {
            throw new Exception();
        });
        dsdm.shutdown(0, false);
        assertThat(dsdm.isShuttingDown()).isFalse();
        assertThat(runs).isEqualTo(0);
        JUnitAppender.assertErrorMessage("Unable to stop");
    }

    @Test
    public void testShutDownisNotInterruptedByCallable() {
        dsdm.register(() -> runs++);
        dsdm.register(() -> true);
        dsdm.shutdown(0, false);
        assertThat(dsdm.isShuttingDown()).isTrue();
        assertThat(runs).isEqualTo(1);
    }

    @Test
    public void testInstanceManagerCreates() {
        assertThat(InstanceManager.getNullableDefault(ShutDownManager.class)).isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        dsdm = new DefaultShutDownManager();
        runs = 0;
    }

    @AfterEach
    public void tearDown() {
        try {
            Class<?> c = jmri.managers.DefaultShutDownManager.class;
            Field f = c.getDeclaredField("shuttingDown");
            f.setAccessible(true);
            f.set(dsdm, false);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            log.error("Failed to reset DefaultShutDownManager shuttingDown field", x);
        }
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultShutDownManagerTest.class);

}
