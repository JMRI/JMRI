package jmri.managers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;
import java.util.concurrent.*;
import java.util.Date;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.AbstractShutDownTask;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright 2017
 * @author Randall Wood Copyright 2020
 */
@Timeout(10)
public class DefaultShutDownManagerTest {

    private ConcurrentMap<String, String> concurrentRuns = new ConcurrentHashMap<>();
    private ConcurrentMap<String, String> concurrentEarlyRuns = new ConcurrentHashMap<>();

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
        ShutDownTask task = new AbstractShutDownTask("task") {
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
        assertThatCode(() -> registerNullShutDownTask(dsdm)).isInstanceOf(NullPointerException.class);
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION", justification = "passing null to non-null to check exception")
    private void registerNullShutDownTask(DefaultShutDownManager d) {
        d.register((ShutDownTask) null);
    }

    @Test
    public void testDeregister_Task() {
        Assert.assertEquals(0, dsdm.getRunnables().size());
        Assert.assertEquals(0, dsdm.getCallables().size());
        ShutDownTask task = new AbstractShutDownTask("task") {
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
        assertThatCode(() -> registerNullCallable(dsdm) ).isInstanceOf(NullPointerException.class);
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION", justification = "passing null to non-null to check exception")
    private void registerNullCallable(DefaultShutDownManager d) {
        d.register((Callable<Boolean>) null);
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
    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION", justification = "passing null to non-null to check exception")
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
        assertThatCode(() -> registerNullRunnable(dsdm) ).isInstanceOf(NullPointerException.class);
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION", justification = "passing null to non-null to check exception")
    private void registerNullRunnable(DefaultShutDownManager d) {
        d.register((Runnable) null);
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
        dsdm.register(new CallTrueShutDownTask("testShutDownisNotInterruptedByShutDownTask"));
        dsdm.shutdown(0, false);
        assertThat(dsdm.isShuttingDown()).isTrue();
        assertThat(runs).isEqualTo(1);
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");
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

        JUnitUtil.waitFor(dsdm.tasksTimeOutMilliSec * 4);
        JUnitAppender.assertErrorMessageStartsWith("Unable to stop");
    }

    @Test
    public void testShutDownisNotInterruptedByCallable() {
        dsdm.register(() -> runs++);
        dsdm.register(() -> true);
        dsdm.shutdown(0, false);
        assertThat(dsdm.isShuttingDown()).isTrue();
        assertThat(runs).isEqualTo(1);
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");
    }

    @Test
    public void testInstanceManagerCreates() {
        assertThat(InstanceManager.getNullableDefault(ShutDownManager.class)).isNotNull();
    }

    @Test
    public void testShutDownTaskTakesTooLong() {
        dsdm.register(new TakesOneSecondShutDownTask("testShutDownTaskTakesTooLong"));
        dsdm.shutdown(0, false);
        
        Assertions.assertTrue(dsdm.isShuttingDown());
        JUnitUtil.waitFor(() -> { return runs == 2; } , "Second runs++ call eventually triggered");
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");

        JUnitUtil.waitFor(dsdm.tasksTimeOutMilliSec * 4);
        JUnitAppender.assertErrorMessageStartsWith("JMRI ShutDown - Main Tasks Task timed out:");
    }

    @Test
    public void testEarlyShutDownTaskTakesTooLong() {
        dsdm.register(new TakesOneSecondEarlyShutDownTask("testEarlyShutDownTaskTakesTooLong"));
        dsdm.shutdown(0, false);
        
        Assertions.assertTrue(dsdm.isShuttingDown());
        JUnitUtil.waitFor(() -> { return runs == 2; } , "Second runs++ call eventually triggered");
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");

        JUnitUtil.waitFor(dsdm.tasksTimeOutMilliSec * 4);
        JUnitAppender.assertErrorMessageStartsWith("JMRI ShutDown - Early Tasks Task timed out:");
    }

    @Test
    public void testShutDownTaskThrowsException() {
        dsdm.register(new ThrowsExceptionShutDownTask("testShutDownTaskThrowsException"));
        dsdm.shutdown(0, false);

        Assertions.assertTrue(dsdm.isShuttingDown());
        Assertions.assertEquals( 1, runs, "run triggered");
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");
        JUnitUtil.waitFor(dsdm.tasksTimeOutMilliSec * 4);
        JUnitAppender.assertErrorMessageStartsWith("JMRI ShutDown - Main Tasks Exception in task:");
    }

    @Test
    public void testMoreThanEightTasks() {
        concurrentRuns = new ConcurrentHashMap<>(41);
        for ( int i=0; i<30; i++ ){
            dsdm.register(new CallTrueShutDownTask("testMoreThanEightTasks"+i));
        }
        Assertions.assertEquals(30, dsdm.getRunnables().size());
        dsdm.shutdown(0, false);
        Assertions.assertTrue(dsdm.isShuttingDown());
        // Assertions.assertEquals(30, runs); // fails often with a few missing
        // JUnitUtil.waitFor times out every now and then, so we use concurrentRuns
        Assertions.assertEquals(30, concurrentRuns.size(),"all runs triggered");
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");
    }

    @Test
    public void testManagerDoesNotUseFullTimeOutWhenComplete() {
        Date start = new Date();
        dsdm.register(new CallTrueShutDownTask("testManagerDoesNotUseFullTimeOutWhenComplete"));
        dsdm.shutdown(0, false);
        Assertions.assertTrue(dsdm.isShuttingDown());
        JUnitUtil.waitFor(() -> runs == 1 , "run triggered");
        long testTime = new Date().getTime() - start.getTime();
        Assertions.assertTrue(testTime < dsdm.tasksTimeOutMilliSec*2,
            "Completed before earlytimeout and mainTimeout");
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");
    }

    @Test
    public void testEarlyTasks() {
        concurrentEarlyRuns = new ConcurrentHashMap<>(3);
        concurrentRuns = new ConcurrentHashMap<>(3);
        dsdm.tasksTimeOutMilliSec *= 2;
        dsdm.register(new EarlyShutDownTask("testEarlyTasks"));

        Thread t1 = new Thread(() -> {
            dsdm.shutdown(0, false);
        });
        t1.setName("testEarlyTasksShutdown");
        t1.start();

        JUnitUtil.waitFor(() -> concurrentEarlyRuns.size() == 1 , "early run triggered");
        Assertions.assertEquals(0, concurrentRuns.size(),"early run triggered before main run");
        JUnitUtil.waitFor(() -> concurrentRuns.size() == 1 , "run triggered");
        JUnitUtil.waitFor(()->{return !(t1.isAlive());}, "shutdown thread completed");
        JUnitUtil.waitFor( () -> dsdm.isShutDownComplete(),"ShutDown Completed");
    }

    private class CallTrueShutDownTask extends AbstractShutDownTask {

        CallTrueShutDownTask(String id){
            super("Call True Task " + id );
        }

        @Override
        public Boolean call() {
            return true;
        }

        @Override
        public void run() {
            runs++;
            concurrentRuns.putIfAbsent(this.getName(), this.getName());
        }

    }

    private class ThrowsExceptionShutDownTask extends AbstractShutDownTask {

        ThrowsExceptionShutDownTask(String id){
            super("Throws NPE Task " + id );
        }

        @Override
        public Boolean call() {
            return true;
        }

        @Override
        public void run() {
            runs++;
            throw new NullPointerException("ThrowsExceptionShutDownTask " + getName());
        }

    }

    private class TakesOneSecondShutDownTask extends AbstractShutDownTask {

        TakesOneSecondShutDownTask(String id){
            super("Takes Too Long " + id );
        }

        @Override
        public Boolean call() {
            return true;
        }

        @Override
        public void run() {
            runs++;
            JUnitUtil.waitFor(1000);
            runs++;
        }

    }

    private class TakesOneSecondEarlyShutDownTask extends AbstractShutDownTask {

        TakesOneSecondEarlyShutDownTask(String id){
            super("Early SDT Takes Too Long " + id );
        }

        @Override
        public Boolean call() {
            return true;
        }

        @Override
        public void runEarly() {
            runs++;
            JUnitUtil.waitFor(1000);
            runs++;
        }

        @Override
        public void run() {
            // does nothing
        }

    }

    private class EarlyShutDownTask extends AbstractShutDownTask {

        EarlyShutDownTask(String id){
            super("EarlyShutDownTask " + id );
        }

        @Override
        public Boolean call() {
            return true;
        }

        @Override
        public void runEarly() {
            concurrentEarlyRuns.putIfAbsent(this.getName(), this.getName());
            JUnitUtil.waitFor(100);
        }

        @Override
        public void run() {
            concurrentRuns.putIfAbsent(this.getName(), this.getName());
        }

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.waitFor(50);
        dsdm = new DefaultShutDownManager();
        dsdm.setBlockingShutdown(true);
        dsdm.tasksTimeOutMilliSec = 200; // normal default 30000 msecs but this is a test
        runs = 0;
        InstanceManager.getDefault(jmri.configurexml.ShutdownPreferences.class).setEnableStoreCheck(false);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.waitFor(50);
        dsdm = null;
        DefaultShutDownManager.setStaticShuttingDown(false);
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultShutDownManagerTest.class);

}
