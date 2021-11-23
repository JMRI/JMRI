package jmri.util;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.swing.AbstractButton;

import jmri.*;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrit.blockboss.BlockBossLogicProvider;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultLogixNGManager;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGManager;
import jmri.jmrit.logixng.implementation.DefaultAnalogActionManager;
import jmri.jmrit.logixng.implementation.DefaultAnalogExpressionManager;
import jmri.jmrit.logixng.implementation.DefaultDigitalActionManager;
import jmri.jmrit.logixng.implementation.DefaultDigitalBooleanActionManager;
import jmri.jmrit.logixng.implementation.DefaultDigitalExpressionManager;
import jmri.jmrit.logixng.implementation.DefaultStringActionManager;
import jmri.jmrit.logixng.implementation.DefaultStringExpressionManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.internal.*;
import jmri.managers.*;
import jmri.profile.*;
import jmri.progdebugger.DebugProgrammerManager;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.managers.*;
import jmri.util.prefs.*;
import jmri.util.zeroconf.MockZeroConfServiceManager;
import jmri.util.zeroconf.ZeroConfServiceManager;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;

/**
 * Common utility methods for working with JUnit.
 * <p>
 * To release the current thread and allow other listeners to execute:  <code><pre>
 * JUnitUtil.releaseThread(this);
 * </pre></code> Note that this is not appropriate for Swing objects; you need
 * to use Jemmy for that.
 * <p>
 * If you're using the InstanceManager, setUp() implementation should start
 * with:
 * <pre><code>
 * JUnitUtil.setUp();
 * JUnitUtil.initInternalTurnoutManager();
 * JUnitUtil.initInternalLightManager();
 * JUnitUtil.initInternalSensorManager();
 * JUnitUtil.initDebugThrottleManager();
 * </code></pre>
 * <p>
 * Your tearDown() should end with:
 * <pre><code>
 * JUnitUtil.tearDown();
 * </code></pre>
 * <p>
 * Note that memory managers and some others are completely internal, and will
 * be reset when you reset the instance manager.
 *
 * @author Bob Jacobsen Copyright 2009, 2015
 * @since 2.5.3
 */
public class JUnitUtil {

    /**
     * Standard time (in mSec) to wait when releasing
     * a thread during a test.
     * <p>
     * Public in case modification is needed from a test or script.
     */
    static final public int DEFAULT_RELEASETHREAD_DELAY = 50;

    /**
     * Default standard time step (in mSec) when looping in a waitFor operation.
     */
    static final private int DEFAULT_WAITFOR_DELAY_STEP = 5;

    /**
     * Standard time step (in mSec) when looping in a waitFor operation.
     * <p>
     * Public in case modification is needed from a test or script.
     * This value is always reset to {@value #DEFAULT_WAITFOR_DELAY_STEP}
     * during setUp().
     */
    static public int WAITFOR_DELAY_STEP = DEFAULT_WAITFOR_DELAY_STEP;

    /**
     * Default maximum time to wait before failing a waitFor operation.
     * <p>
     * The default value is really long, but that only matters when the test
     * is failing anyway, and some of the LayoutEditor/SignalMastLogic tests
     * are slow. But too long will cause CI jobs to time out before this logs
     * the error....
     */
    static final private int DEFAULT_WAITFOR_MAX_DELAY = 10000;

    /**
     * Maximum time to wait before failing a waitFor operation.
     * <p>
     * Public in case modification is needed from a test or script.
     * This value is always reset to {@value #DEFAULT_WAITFOR_MAX_DELAY} during setUp().
     */
    static public int WAITFOR_MAX_DELAY = DEFAULT_WAITFOR_MAX_DELAY;

    /**
     * When true, prints each setUp method to help identify which tests include a failure.
     * When checkSetUpTearDownSequence is also true, this also prints on execution of tearDown.
     * <p>
     * Set from the jmri.util.JUnitUtil.printSetUpTearDownNames environment variable.
     */
    static boolean printSetUpTearDownNames = Boolean.getBoolean("jmri.util.JUnitUtil.printSetUpTearDownNames"); // false unless set true

    /**
     * When true, checks that calls to setUp and tearDown properly alterante, printing an
     * error message with context information on System.err if inconsistent calls are observed.
     * <p>
     * Set from the jmri.util.JUnitUtil.checkSetUpTearDownSequence environment variable.
     */
    static boolean checkSetUpTearDownSequence = Boolean.getBoolean("jmri.util.JUnitUtil.checkSetUpTearDownSequence"); // false unless set true

    /**
     * Adds extensive error information to the output of checkSetUpTearDownSequence.
     * Note: The context checking and storage required for this takes a lot of time.
     * <p>
     * Set from the jmri.util.JUnitUtil.checkSequenceDumpsStack environment variable.
     */
    static boolean checkSequenceDumpsStack =    Boolean.getBoolean("jmri.util.JUnitUtil.checkSequenceDumpsStack"); // false unless set true

    /**
     * Announce any threads left behind after a test calls {@link #tearDown}
     * <p>
     * Set from the jmri.util.JUnitUtil.checkRemnantThreads environment variable.
     */
    static boolean checkRemnantThreads =    Boolean.getBoolean("jmri.util.JUnitUtil.checkRemnantThreads"); // false unless set true

    /**
     * Kill any threads left behind after a test calls {@link #tearDown}
     * <p>
     * Set from the jmri.util.JUnitUtil.killRemnantThreads environment variable
     */
    static boolean killRemnantThreads =    Boolean.getBoolean("jmri.util.JUnitUtil.killRemnantThreads"); // false unless set true

    /**
     * Check for tests that take an excessive time
     * <p>
     * Set from the jmri.util.JUnitUtil.checkTestDuration environment variable.
     */
    static boolean checkTestDuration =      Boolean.getBoolean("jmri.util.JUnitUtil.checkTestDuration"); // false unless set true
    static long    checkTestDurationMax =   Long.getLong("jmri.util.JUnitUtil.checkTestDurationMax", 5000); // milliseconds

    static long    checkTestDurationStartTime = 0;  // working value

    static private boolean didSetUp = false;    // If true, last saw setUp, waiting tearDown normally
    static private boolean didTearDown = true;  // If true, last saw tearDown, waiting setUp normally

    static private String lastSetUpClassName = "<unknown>";
    static private String lastSetUpThreadName = "<unknown>";
    static private StackTraceElement[] lastSetUpStackTrace = new StackTraceElement[0];
    static private String lastTearDownClassName = "<unknown>";
    static private String lastTearDownThreadName = "<unknown>";
    static private StackTraceElement[] lastTearDownStackTrace = new StackTraceElement[0];

    static private boolean isLoggingInitialized = false;
    static private String initPrefsDir = null;

    /**
     * JMRI standard setUp for tests that mock the InstanceManager. This should be the first line in the {@code @Before}
     * annotated method if the tests mock the InstanceManager.
     * One or the other of {@link #setUp()} or {@link #setUpLoggingAndCommonProperties()} must
     * be present in the {@code @Before} routine.
     */
    public static void setUpLoggingAndCommonProperties() {
        if (!isLoggingInitialized) {
            // init logging if needed
            isLoggingInitialized = true;
            String filename = System.getProperty("jmri.log4jconfigfilename", "tests.lcf");
            TestingLoggerConfiguration.initLogging(filename);
        }

        // need to do this each time
        try {
            JUnitAppender.start();

            // reset warn _only_ once logic to make tests repeatable
            JUnitLoggingUtil.restartWarnOnce();
            // ensure logging of deprecated method calls;
            // individual tests can turn off as needed
            JUnitLoggingUtil.setDeprecatedLogging(true);

        } catch (Throwable e) {
            System.err.println("Could not start JUnitAppender, but test continues:\n" + e);
        }

        // clear the backlog and reset the UnexpectedMessageFlags so that
        // errors from a previous test do not interfere with the current test.
        JUnitAppender.clearBacklog();
        JUnitAppender.resetUnexpectedMessageFlags(Level.INFO);


        // do not set the UncaughtExceptionHandler while unit testing
        // individual tests can explicitly set it after calling this method
        Thread.setDefaultUncaughtExceptionHandler(null);

        // make sure the jmri.prefsdir property match the property passed
        // to the tests.
        if (initPrefsDir == null) {
            initPrefsDir = System.getProperty("jmri.prefsdir", "./temp");
        }
        System.setProperty("jmri.prefsdir", initPrefsDir);

        // silence the Jemmy GUI unit testing framework
        JUnitUtil.silenceGUITestOutput();

        // ideally this would be resetWindows(false, true) to force an error if an earlier
        // test left a window open, but different platforms seem to have just
        // enough differences that this is, for now, turned off
        resetWindows(false, false);

        // Log and/or check the use of setUp and tearDown
        if (checkSetUpTearDownSequence || printSetUpTearDownNames) {
            lastSetUpClassName = getTestClassName();

            if (printSetUpTearDownNames) System.err.println(">> Starting test in "+lastSetUpClassName);

            if ( checkSetUpTearDownSequence)  {
                if (checkSequenceDumpsStack)  lastSetUpThreadName = Thread.currentThread().getName();

                if (didSetUp || ! didTearDown) {
                    System.err.println("   "+getTestClassName()+".setUp on thread "+lastSetUpThreadName+" unexpectedly found setUp="+didSetUp+" tearDown="+didTearDown+"; last setUp was in "+lastSetUpClassName+" thread "+lastSetUpThreadName);
                    if (checkSequenceDumpsStack) {
                        System.err.println("---- This stack ------");
                        Thread.dumpStack();
                        System.err.println("---- Last setUp stack ------");
                        for (StackTraceElement e : lastSetUpStackTrace) System.err.println("    at " + e);
                        System.err.println("---- Last tearDown stack ------");
                        for (StackTraceElement e : lastTearDownStackTrace) System.err.println("    at " + e);
                        System.err.println("----------------------");
                    }
                }

                didTearDown = false;
                didSetUp = true;

                if (checkSequenceDumpsStack) lastSetUpStackTrace = Thread.currentThread().getStackTrace();
            }
        }

        // checking time?
        if (checkTestDuration) {
            checkTestDurationStartTime = System.currentTimeMillis();
        }
    }

    /**
     * JMRI standard setUp for tests. This should be the first line in the {@code @Before}
     * annotated method if the tests do not mock the InstanceManager.
     * One or the other of {@link #setUp()} or {@link #setUpLoggingAndCommonProperties()} must
     * be present in the {@code @Before} routine.
     */
    public static void setUp() {
        WAITFOR_DELAY_STEP = DEFAULT_WAITFOR_DELAY_STEP;
        WAITFOR_MAX_DELAY = DEFAULT_WAITFOR_MAX_DELAY;

        // all the setup for a MockInstanceManager applies
        setUpLoggingAndCommonProperties();

        // Do a minimal amount of de-novo setup
        resetInstanceManager();

    }

    /**
     * Silently remove any AbstractTurnout threads that are still running.
     * A bit expensive, so only used when needed.
     */
    public static void clearTurnoutThreads(){
        removeMatchingThreads("setCommandedStateAtInterval"); // must stay consistent with AbstractTurnout
    }

    /**
     * Silently remove any DefaultRoute threads that are still running.
     * A bit expensive, so only used when needed.
     */
    public static void clearRouteThreads(){
        removeMatchingThreads("setRoute"); // must stay consistent with DefaultRoute
    }

    /**
     * Silently remove any blockboss/Simple Signal Logic threads that are still running.
     * A bit expensive, so only used when needed.
     */
    public static void clearBlockBossLogicThreads(){
        removeMatchingThreads("BlockBossLogic");
    }

    /**
     * Utility to remove any threads with a matchiing name
     */
    public static void removeMatchingThreads(String nameContains) {
        ThreadGroup main = Thread.currentThread().getThreadGroup();
        while (main.getParent() != null ) {main = main.getParent(); }
        Thread[] list = new Thread[main.activeCount()+2];  // space on end
        int max = main.enumerate(list);

        for (int i = 0; i<max; i++) {
            Thread t = list[i];
            if (t.getState() == Thread.State.TERMINATED) { // going away, just not cleaned up yet
                continue;
            }
            String name = t.getName();
            if (name.contains(nameContains)) {
                killThread(t);
            }
        }
    }

    static void killThread(Thread t) {
        t.interrupt();
        try {
            t.join(100); // give it a bit of time to end
            if (t.getState() != Thread.State.TERMINATED) {
                t.stop(); // yes, we know it's deprecated, but it's the only option for Jemmy threads
                log.warn("   Thread {} did not terminate", t.getName());
            }
        } catch (IllegalMonitorStateException | IllegalStateException | InterruptedException e) {
            log.error("While interrupting thread {}:", t.getName(), e);
        }
    }

    /**
     * Teardown from tests. This should be the last line in the {@code @After}
     * annotated method.
     */
    public static void tearDown() {

        // check that no LogixNG threads is still running
        jmri.jmrit.logixng.util.LogixNG_Thread.assertLogixNGThreadNotRunning();

        // checking time?
        if (checkTestDuration) {
            long duration = System.currentTimeMillis() - checkTestDurationStartTime;
            if (duration > checkTestDurationMax) {
                // test too long, log that
                System.err.println("Test in "+getTestClassName()+" duration "+duration+" ms exceeded limit "+checkTestDurationMax);
            }
        }
        // Log and/or check the use of setUp and tearDown
        if (checkSetUpTearDownSequence || printSetUpTearDownNames) {
            lastTearDownClassName = getTestClassName();

            if (checkSetUpTearDownSequence) {
                if (checkSequenceDumpsStack) lastTearDownThreadName = Thread.currentThread().getName();

                if (! didSetUp || didTearDown) {
                    System.err.println("   "+getTestClassName()+".tearDown on thread "+lastTearDownThreadName+" unexpectedly found setUp="+didSetUp+" tearDown="+didTearDown+"; last tearDown was in "+lastTearDownClassName+" thread "+lastTearDownThreadName);
                    if (checkSequenceDumpsStack) {
                        System.err.println("---- This stack ------");
                        Thread.dumpStack();
                        System.err.println("---- Last setUp stack ------");
                        for (StackTraceElement e : lastSetUpStackTrace) System.err.println("    at " + e);
                        System.err.println("---- Last tearDown stack ------");
                        for (StackTraceElement e : lastTearDownStackTrace) System.err.println("    at " + e);
                        System.err.println("----------------------");
                    }
                }

                didSetUp = false;
                didTearDown = true;

                if (checkSequenceDumpsStack) lastTearDownStackTrace = Thread.currentThread().getStackTrace();
            }

            // To save time & space, only print end when doing full check
            if (printSetUpTearDownNames && checkSetUpTearDownSequence)  System.err.println("<<   Ending test in "+lastTearDownClassName);

        }

        // ideally this would be resetWindows(false, true) to force an error if an earlier
        // test left a window open, but different platforms seem to have just
        // enough differences that this is, for now, turned off
        resetWindows(false, false);

        // Check final status of logging in the test just completed
        JUnitAppender.end();
        Level severity = Level.ERROR; // level at or above which we'll complain
        boolean unexpectedMessageSeen = JUnitAppender.unexpectedMessageSeen(severity);
        String unexpectedMessageContent = JUnitAppender.unexpectedMessageContent(severity);
        JUnitAppender.verifyNoBacklog();
        JUnitAppender.resetUnexpectedMessageFlags(severity);
        Assert.assertFalse("Unexpected "+severity+" or higher messages emitted: "+unexpectedMessageContent, unexpectedMessageSeen);

        // check for hanging shutdown tasks - after test for ERROR so it can complain
        checkShutDownManager();

        // Optionally, handle any threads left running
        if (checkRemnantThreads || killRemnantThreads) {
            handleThreads();
        }

        // Optionally, print whatever is on the Swing queue to see what's keeping things alive
        //Object entry = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent();
        //if (entry != null) System.err.println("entry: "+entry);

        // Optionally, check that the Swing queue is idle
        //new org.netbeans.jemmy.QueueTool().waitEmpty(250);

    }

    /**
     * Release the current thread, allowing other threads to process. Waits for
     * {@value #DEFAULT_RELEASETHREAD_DELAY} milliseconds.
     * <p>
     * This cannot be used on the Swing or AWT event threads. For those, please
     * use Jemmy's wait routine.
     *
     * @param self currently ignored
     * @deprecated 4.9.1 Use the various waitFor routines instead
     */
    @Deprecated // 4.9.1 Use the various waitFor routines instead
    public static void releaseThread(Object self) {
        releaseThread(self, DEFAULT_RELEASETHREAD_DELAY);
    }

    /**
     * Release the current thread, allowing other threads to process.
     * <p>
     * This cannot be used on the Swing or AWT event threads. For those, please
     * use Jemmy's wait routine.
     *
     * @param self  currently ignored
     * @param delay milliseconds to wait
     * @deprecated 4.9.1 Use the various waitFor routines instead
     */
    @Deprecated // 4.9.1 Use the various waitFor routines instead
    public static void releaseThread(Object self, int delay) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use releaseThread on Swing thread", new Exception());
            return;
        }
        try {
            int priority = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            Thread.sleep(delay);
            Thread.currentThread().setPriority(priority);
        } catch (InterruptedException e) {
            Assert.fail("failed due to InterruptedException");
        }
    }

    /**
     * Wait for a specific condition to be true, without having to wait longer
     * <p>
     * To be used in tests, will do an assert if the total delay is longer than
     * WAITFOR_MAX_DELAY
     * <p>
     * Typical use:
     * <code>JUnitUtil.waitFor(()->{return replyVariable != null;},"reply not received")</code>
     *
     * @param condition condition being waited for
     * @param name      name of condition being waited for; will appear in
     *                  Assert.fail if condition not true fast enough
     */
    static public void waitFor(ReleaseUntil condition, String name) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use waitFor on Swing thread", new Exception());
            return;
        }
        int delay = 0;
        try {
            while (delay < WAITFOR_MAX_DELAY) {
                if (condition.ready()) {
                    return;
                }
                int priority = Thread.currentThread().getPriority();
                try {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.sleep(WAITFOR_DELAY_STEP);
                    delay += WAITFOR_DELAY_STEP;
                } catch (InterruptedException e) {
                    Assert.fail("failed due to InterruptedException");
                } finally {
                    Thread.currentThread().setPriority(priority);
                }
            }
            Assert.fail("\"" + name + "\" did not occur in time");
        } catch (Exception ex) {
            Assert.fail("Exception while waiting for \"" + name + "\" " + ex);
        }
    }

    /**
     * Wait for a specific condition to be true, without having to wait longer
     * <p>
     * To be used in assumptions, will return false if the total delay is longer
     * than WAITFOR_MAX_DELAY
     * <p>
     * Typical use:
     * <code>Assume.assumeTrue("reply not received", JUnitUtil.waitForTrue(()->{return replyVariable != null;}));</code>
     *
     * @param condition condition to wait for
     * @return true if condition is met before WAITFOR_MAX_DELAY, false
     *         otherwise
     */
    static public boolean waitFor(ReleaseUntil condition) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use waitFor on Swing thread", new Exception());
            return false;
        }
        int delay = 0;
        try {
            while (delay < WAITFOR_MAX_DELAY) {
                if (condition.ready()) {
                    return true;
                }
                int priority = Thread.currentThread().getPriority();
                try {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.sleep(WAITFOR_DELAY_STEP);
                    delay += WAITFOR_DELAY_STEP;
                } catch (InterruptedException e) {
                    return false;
                } finally {
                    Thread.currentThread().setPriority(priority);
                }
            }
            return false;
        } catch (Exception ex) {
            log.error("Exception in waitFor condition.", ex);
            return false;
        }
    }

    /**
     * Wait for a specific amount of time
     * <p>
     * It's better to wait for a condition, but if you can't find a condition,
     * this will have to do.
     * <p>
     *
     * @param time Delay in milliseconds
     */
    static public void waitFor(int time) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use waitFor on Swing thread", new Exception());
            return;
        }
        int delay = 0;
        try {
            while (delay < time) {
                int priority = Thread.currentThread().getPriority();
                try {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.sleep(WAITFOR_DELAY_STEP);
                    delay += WAITFOR_DELAY_STEP;
                } catch (InterruptedException e) {
                    return;
                } finally {
                    Thread.currentThread().setPriority(priority);
                }
            }
        } catch (Exception ex) {
            log.error("Exception in waitFor condition.", ex);
        }
    }

    /**
     * Wait for a specific condition to be true, without having to wait longer
     * <p>
     * To be used in tests, will do an assert if the total delay is longer than
     * 1 second
     * <p>
     * Typical use:
     * <code>JUnitUtil.fasterWaitFor(()->{return replyVariable != null;},"reply not received")</code>
     *
     * @param condition condition being waited for
     * @param name      name of condition being waited for; will appear in
     *                  Assert.fail if condition not true fast enough
     */
    static public void fasterWaitFor(ReleaseUntil condition, String name) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use waitFor on Swing thread", new Exception());
            return;
        }
        int delay = 0;
        try {
            while (delay < 1000) {
                if (condition.ready()) {
                    return;
                }
                int priority = Thread.currentThread().getPriority();
                try {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.sleep(5);
                    delay += 5;
                } catch (InterruptedException e) {
                    Assert.fail("failed due to InterruptedException");
                } finally {
                    Thread.currentThread().setPriority(priority);
                }
            }
            Assert.fail("\"" + name + "\" did not occur in time");
        } catch (Exception ex) {
            Assert.fail("Exception while waiting for \"" + name + "\" " + ex);
        }
    }

    /**
     * Wait at most 1 second for a specific condition to be true, without having to wait longer
     * <p>
     * To be used in assumptions, will return false if the total delay is longer
     * than 1000 milliseconds.
     * <p>
     * Typical use:
     * <code>Assume.assumeTrue("reply not received", JUnitUtil.fasterWaitForTrue(()->{return replyVariable != null;}));</code>
     *
     * @param condition condition to wait for
     * @return true if condition is met before 1 second, false
     *         otherwise
     */
    static public boolean fasterWaitFor(ReleaseUntil condition) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use waitFor on Swing thread", new Exception());
            return false;
        }
        int delay = 0;
        try {
            while (delay < 1000) {
                if (condition.ready()) {
                    return true;
                }
                int priority = Thread.currentThread().getPriority();
                try {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.sleep(5);
                    delay += 5;
                } catch (InterruptedException e) {
                    return false;
                } finally {
                    Thread.currentThread().setPriority(priority);
                }
            }
            return false;
        } catch (Exception ex) {
            log.error("Exception in waitFor condition.", ex);
            return false;
        }
    }

    /**
     * Reset the user files path in the default
     * {@link jmri.util.FileUtilSupport} object (used by
     * {@link jmri.util.FileUtil}) to the default settings/user files path for
     * tests of {@code git-working-copy/temp}.
     */
    public static void resetFileUtilSupport() {
        try {
            Field field = FileUtilSupport.class.getDeclaredField("defaultInstance");
            field.setAccessible(true);
            field.set(null, null);
            FileUtilSupport.getDefault().setUserFilesPath(ProfileManager.getDefault().getActiveProfile(), FileUtil.getPreferencesPath());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            log.error("Exception resetting FileUtilSupport", ex);
        }
    }

    static public interface ReleaseUntil {

        public boolean ready() throws Exception;
    }

    /**
     * Set a NamedBean (Turnout, Sensor, SignalHead, ...) to a specific value in
     * a thread-safe way.
     * <p>
     * You can't assume that all the consequences of that setting will have
     * propagated through when this returns; those might take a long time. But
     * the set operation itself will be complete.
     *
     * @param bean  the bean
     * @param state the desired state
     */
    static public void setBeanState(NamedBean bean, int state) {
        try {
            javax.swing.SwingUtilities.invokeAndWait(
                    () -> {
                        try {
                            bean.setState(state);
                        } catch (JmriException e) {
                            log.error("Threw exception while setting state: ", e);
                        }
                    }
            );
        } catch (InterruptedException e) {
            log.warn("Interrupted while setting state: ", e);
        } catch (InvocationTargetException e) {
            log.warn("Failed during invocation while setting state: ", e);
        }
    }

    /**
     * Set a NamedBean (Turnout, Sensor, SignalHead, ...) to a specific value in
     * a thread-safe way, including waiting for the state to appear.
     * <p>
     * You can't assume that all the consequences of that setting will have
     * propagated through when this returns; those might take a long time. But
     * the set operation itself will be complete.
     *
     * @param bean  the bean
     * @param state the desired state
     */
    static public void setBeanStateAndWait(NamedBean bean, int state) {
        setBeanState(bean, state);
        JUnitUtil.waitFor(() -> {
            return state == bean.getState();
        }, "setAndWait " + bean.getSystemName() + ": " + state);
    }

    public static void resetInstanceManager() {
        // clear all instances from the static InstanceManager
        InstanceManager.getDefault().clearAll();
        // ensure the auto-default UserPreferencesManager is not created by installing a test one
        InstanceManager.setDefault(UserPreferencesManager.class, new TestUserPreferencesManager());
    }

    public static void resetTurnoutOperationManager() {
        InstanceManager.reset(TurnoutOperationManager.class);
        InstanceManager.getDefault(TurnoutOperationManager.class); // force creation
    }

    public static void initConfigureManager() {
        InstanceManager.setDefault(ConfigureManager.class, new JmriConfigurationManager());
    }

    public static void initDefaultUserMessagePreferences() {
        // remove the existing user preferences manager (if present)
        InstanceManager.reset(UserPreferencesManager.class);
        // create a test user preferences manager
        InstanceManager.setDefault(UserPreferencesManager.class, new TestUserPreferencesManager());
    }

    public static void initInternalTurnoutManager() {
        // now done automatically by InstanceManager's autoinit
        InstanceManager.turnoutManagerInstance();
    }

    public static void initInternalLightManager() {
        // now done automatically by InstanceManager's autoinit
        InstanceManager.lightManagerInstance();
    }

    public static void initInternalSensorManager() {
        // now done automatically by InstanceManager's autoinit
        InstanceManager.sensorManagerInstance();
        InternalSensorManager.setDefaultStateForNewSensors(jmri.Sensor.UNKNOWN);
    }

    public static void initRouteManager() {
        // routes provide sensors, so ensure the sensor manager is initialized
        // routes need turnouts, so ensure the turnout manager is initialized
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        // now done automatically by InstanceManager's autoinit
        InstanceManager.getDefault(RouteManager.class);
    }

    public static void initMemoryManager() {
        MemoryManager m = new DefaultMemoryManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.MEMORIES);
        }
    }

    public static void initReporterManager() {
        ReporterManager m = new InternalReporterManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.REPORTERS);
        }
    }

    public static void initOBlockManager() {
        OBlockManager b = new OBlockManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(b, jmri.Manager.OBLOCKS);
        }
    }

    public static void deregisterBlockManagerShutdownTask() {
        if (! InstanceManager.isInitialized(ShutDownManager.class)) return;
        if (! InstanceManager.isInitialized(BlockManager.class)) return;

        InstanceManager
                .getDefault(ShutDownManager.class)
                .deregister(InstanceManager.getDefault(BlockManager.class).shutDownTask);
    }

    public static void deregisterEditorManagerShutdownTask() {
        if (! InstanceManager.isInitialized(ShutDownManager.class)) return;
        if (! InstanceManager.isInitialized(EditorManager.class)) return;

        InstanceManager
                .getDefault(ShutDownManager.class)
                .deregister(InstanceManager.getDefault(EditorManager.class).shutDownTask);
    }

    public static void initWarrantManager() {
        WarrantManager w = new WarrantManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.WARRANTS);
        }
    }

    public static void initSignalMastLogicManager() {
        SignalMastLogicManager w = new DefaultSignalMastLogicManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.SIGNALMASTLOGICS);
        }
    }

    public static void initLayoutBlockManager() {
        LayoutBlockManager w = new LayoutBlockManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.LAYOUTBLOCKS);
        }
    }

    public static void initSectionManager() {
        jmri.SectionManager w = new jmri.SectionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.SECTIONS);
        }
    }

    public static void initInternalSignalHeadManager() {
        SignalHeadManager m = new AbstractSignalHeadManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.setDefault(SignalHeadManager.class, m);
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.SIGNALHEADS);
        }
    }

    public static void initDefaultSignalMastManager() {
        InstanceManager.setDefault(SignalMastManager.class, new DefaultSignalMastManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
    }

    public static void initDebugCommandStation() {
        jmri.CommandStation cs = new jmri.CommandStation() {
            @Override
            public boolean sendPacket(@Nonnull byte[] packet, int repeats) {
            return true;
            }

            @Override
            public String getUserName() {
                return "testCS";
            }

            @Override
            public String getSystemPrefix() {
                return "I";
            }

        };

        InstanceManager.store(cs, jmri.CommandStation.class);
    }

    public static void initDebugThrottleManager() {
        jmri.ThrottleManager m = new DebugThrottleManager();
        InstanceManager.store(m, ThrottleManager.class);
    }

    public static void initDebugProgrammerManager() {
        DebugProgrammerManager m = new DebugProgrammerManager();
        InstanceManager.store(m, AddressedProgrammerManager.class);
        InstanceManager.store(m, GlobalProgrammerManager.class);
    }

    public static void initDebugPowerManager() {
        InstanceManager.setDefault(PowerManager.class, new PowerManagerScaffold());
    }

    /**
     * Initialize an {@link IdTagManager} that does not use persistent storage.
     * If needing an IdTagManager that does use persistent storage use
     * {@code InstanceManager.setDefault(IdTagManager.class, new DefaultIdTagManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));}
     * to initialize an IdTagManager in the {@code @Before} annotated method of
     * the test class or allow the {@link DefaultIdTagManager} to be
     * automatically initialized when needed.
     */
    public static void initIdTagManager() {
        InstanceManager.reset(IdTagManager.class);
        InstanceManager.setDefault(IdTagManager.class,
                new DefaultIdTagManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)) {
                    @Override
                    public void writeIdTagDetails() {
                        // do not actually write tags
                        this.dirty = false;
                    }

                    @Override
                    public void readIdTagDetails() {
                        // do not actually read tags
                        this.dirty = false;
                    }

                    @Override
                    protected void initShutdownTask(){
                        //don't even register the shutdownTask
                    }

                });
    }

    public static void initRailComManager() {
        InstanceManager.reset(jmri.RailComManager.class);
        InstanceManager.store(new DefaultRailComManager(), jmri.RailComManager.class);
    }

    public static void initLogixManager() {
        LogixManager m = new DefaultLogixManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.LOGIXS);
        }
    }

    public static void initConditionalManager() {
        ConditionalManager m = new DefaultConditionalManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.CONDITIONALS);
        }
    }

    public static void initInternalTurnoutManagerThrowException() {
        InstanceManager.setDefault(TurnoutManager.class, new TurnoutManagerThrowExceptionScaffold());
    }

    public static void initLogixNGManager() {
        initLogixNGManager(true);
    }

    public static void initLogixNGManager(boolean activate) {
        LogixNG_Manager m1 = new DefaultLogixNGManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m1, jmri.Manager.LOGIXNGS);
        }
        InstanceManager.setDefault(LogixNG_Manager.class, m1);

        ConditionalNG_Manager m2 = new DefaultConditionalNGManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m2, jmri.Manager.LOGIXNG_CONDITIONALNGS);
        }
        InstanceManager.setDefault(ConditionalNG_Manager.class, m2);

        AnalogActionManager m3 = new DefaultAnalogActionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m3, jmri.Manager.LOGIXNG_ANALOG_ACTIONS);
        }
        InstanceManager.setDefault(AnalogActionManager.class, m3);

        AnalogExpressionManager m4 = new DefaultAnalogExpressionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m4, jmri.Manager.LOGIXNG_ANALOG_EXPRESSIONS);
        }
        InstanceManager.setDefault(AnalogExpressionManager.class, m4);

        DigitalActionManager m5 = new DefaultDigitalActionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m5, jmri.Manager.LOGIXNG_DIGITAL_ACTIONS);
        }
        InstanceManager.setDefault(DigitalActionManager.class, m5);

        DigitalBooleanActionManager m6 = new DefaultDigitalBooleanActionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m6, jmri.Manager.LOGIXNG_DIGITAL_BOOLEAN_ACTIONS);
        }
        InstanceManager.setDefault(DigitalBooleanActionManager.class, m6);

        DigitalExpressionManager m7 = new DefaultDigitalExpressionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m7, jmri.Manager.LOGIXNG_DIGITAL_EXPRESSIONS);
        }
        InstanceManager.setDefault(DigitalExpressionManager.class, m7);

        StringActionManager m8 = new DefaultStringActionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m8, jmri.Manager.LOGIXNG_STRING_ACTIONS);
        }
        InstanceManager.setDefault(StringActionManager.class, m8);

        StringExpressionManager m9 = new DefaultStringExpressionManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m9, jmri.Manager.LOGIXNG_STRING_EXPRESSIONS);
        }
        InstanceManager.setDefault(StringExpressionManager.class, m9);

        if (activate) m1.activateAllLogixNGs(false, false);
    }

    public static void initInternalSensorManagerThrowException() {
        InstanceManager.setDefault(SensorManager.class, new SensorManagerThrowExceptionScaffold());
    }

    public static void initLightManagerThrowException() {
        InstanceManager.setDefault(LightManager.class, new InternalLightManagerThrowExceptionScaffold());
    }

    public static void initMemoryManagerThrowException() {
        InstanceManager.setDefault(MemoryManager.class, new MemoryManagerThrowExceptionScaffold());
    }

    public static void initSignalHeadManagerThrowException() {
        InstanceManager.setDefault(SignalHeadManager.class, new SignalHeadManagerThrowExceptionScaffold());
    }

    public static void initSignalMastManagerThrowException() {
        InstanceManager.setDefault(SignalMastManager.class, new SignalMastManagerThrowExceptionScaffold());
    }

    public static void initWarrantManagerThrowException() {
        InstanceManager.setDefault(WarrantManager.class, new WarrantManagerThrowExceptionScaffold());
    }

    public static void initOBlockManagerThrowException() {
        InstanceManager.setDefault(OBlockManager.class, new OBlockManagerThrowExceptionScaffold());
    }

    public static void initRouteManagerThrowException() {
        InstanceManager.setDefault(RouteManager.class, new RouteManagerThrowExceptionScaffold());
    }

    /**
     * Initialize a {@link jmri.util.zeroconf.MockZeroConfServiceManager} after
     * ensuring that any existing
     * {@link jmri.util.zeroconf.ZeroConfServiceManager} (real or mocked) has
     * stopped all services it is managing.
     */
    public static void initZeroConfServiceManager() {
        resetZeroConfServiceManager();
        InstanceManager.setDefault(ZeroConfServiceManager.class, new MockZeroConfServiceManager());
    }

    /**
     * Ensure that any existing
     * {@link jmri.util.zeroconf.ZeroConfServiceManager} (real or mocked) has
     * stopped all services it is managing.
     */
    public static void resetZeroConfServiceManager() {
        if (! InstanceManager.containsDefault(ZeroConfServiceManager.class)) return; // not present, don't create on by asking for it.

        ZeroConfServiceManager manager = InstanceManager.getDefault(ZeroConfServiceManager.class);
        manager.stopAll();

        JUnitUtil.waitFor(() -> {
            return (manager.allServices().isEmpty());
        }, "Stopping all ZeroConf Services");

        manager.dispose();
    }

    /**
     * End any running BlockBossLogic (Simple Signal Logic) objects
     */
    public static void clearBlockBossLogic() {
        if(InstanceManager.containsDefault(BlockBossLogicProvider.class)) {
            InstanceManager.getDefault(BlockBossLogicProvider.class).dispose();
        }
    }

    /**
     * Leaves ShutDownManager, if any, in place,
     * but removes its contents.
     * <p>
     * Instead of using this,
     * it's better to have your test code remove _and_ _check_
     * for specific items; this just suppresses output from the
     * {@link #checkShutDownManager()} check down as part of the
     * default end-of-test code.
     *
     * @see #checkShutDownManager()
     */
    public static void clearShutDownManager() {
        if (!  InstanceManager.containsDefault(ShutDownManager.class)) return; // not present, stop (don't create)

        ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);

        List<Callable<Boolean>> callables = sm.getCallables();
        while (!callables.isEmpty()) {
            Callable<Boolean> callable = callables.get(0);
            sm.deregister(callable);
            callables = sm.getCallables(); // avoid ConcurrentModificationException
        }

        List<Runnable> runnables = sm.getRunnables();
        while (!runnables.isEmpty()) {
            Runnable runnable = runnables.get(0);
            sm.deregister(runnable);
            runnables = sm.getRunnables(); // avoid ConcurrentModificationException
        }
    }

    /**
     * Errors if the {@link jmri.ShutDownManager} was not left empty. Normally
     * run as part of the default end-of-test code. Considered an error so that
     * CI will flag these and tests will be improved.
     *
     * @see #clearShutDownManager()
     */
    static void checkShutDownManager() {
        if (!  InstanceManager.containsDefault(ShutDownManager.class)) return; // not present, stop (don't create)

        ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);

        List<Callable<Boolean>> callables = sm.getCallables();
        while (!callables.isEmpty()) {
            Callable<Boolean> callable = callables.get(0);
            log.error("Test {} left registered shutdown callable of type {}", getTestClassName(), callable.getClass(),
                        LoggingUtil.shortenStacktrace(new Exception("traceback")));
            sm.deregister(callable);
            callables = sm.getCallables(); // avoid ConcurrentModificationException
        }
        List<Runnable> runnables = sm.getRunnables();
        while (!runnables.isEmpty()) {
            Runnable runnable = runnables.get(0);
            log.error("Test {} left registered shutdown runnable of type {}", getTestClassName(), runnable.getClass(),
                        LoggingUtil.shortenStacktrace(new Exception("traceback")));
            sm.deregister(runnable);
            runnables = sm.getRunnables(); // avoid ConcurrentModificationException
        }

        // use reflection to reset static fields in the class.
        try {
            Class<?> c = jmri.managers.DefaultShutDownManager.class;
            Field f = c.getDeclaredField("shuttingDown");
            f.setAccessible(true);
            f.set(sm, false);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            log.error("Failed to reset DefaultShutDownManager shuttingDown field", x);
        }

    }

    public static void initStartupActionsManager() {
        InstanceManager.store(
                new jmri.util.startup.StartupActionsManager(),
                jmri.util.startup.StartupActionsManager.class);
    }

    public static void initConnectionConfigManager() {
        InstanceManager.setDefault(ConnectionConfigManager.class, new ConnectionConfigManager());
    }

    public static void initRosterConfigManager() {
        RosterConfigManager manager = new RosterConfigManager();
        try {
            manager.initialize(ProfileManager.getDefault().getActiveProfile());
        } catch (InitializationException ex) {
            log.error("Failed to initialize RosterConfigManager", ex);
        }
        InstanceManager.setDefault(RosterConfigManager.class, manager);
    }

    /*
     * Use reflection to reset the jmri.Application instance
     */
    public static void resetApplication() {
        try {
            Class<?> c = jmri.Application.class;
            Field f = c.getDeclaredField("name");
            f.setAccessible(true);
            f.set(new jmri.Application(), null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            log.error("Failed to reset jmri.Application static field", x);
        }
    }

    /*
     * Use reflection to reset the jmri.util.node.NodeIdentity instance
     */
    public static void resetNodeIdentity() {
        try {
            Class<?> c = jmri.util.node.NodeIdentity.class;
            Field f = c.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(c, null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            log.error("Failed to reset jmri.util.node.NodeIdentity instance", x);
        }
    }


    public static void initGuiLafPreferencesManager() {
        GuiLafPreferencesManager m = new GuiLafPreferencesManager();
        InstanceManager.setDefault(GuiLafPreferencesManager.class, m);
    }

    /**
     * Use only if profile contents are not to be verified or modified in test.
     * If a profile will be written to and its contents verified as part of a
     * test use {@link #resetProfileManager(jmri.profile.Profile)} with a
     * provided profile.
     * <p>
     * The new profile will have the name {@literal TestProfile}, the id
     * {@literal 00000000}, and will be in the directory {@literal temp}
     * within the sources working copy.
     */
    public static void resetProfileManager() {
        try {
            Profile profile = new NullProfile("TestProfile", "00000000", FileUtil.getFile(FileUtil.SETTINGS));
            resetProfileManager(profile);
        } catch (FileNotFoundException ex) {
            log.error("Settings directory \"{}\" does not exist", FileUtil.SETTINGS);
        } catch (IOException | IllegalArgumentException ex) {
            log.error("Unable to create profile", ex);
        }
    }

    /**
     * Use if the profile needs to be written to or cleared as part of the test.
     * Suggested use in the {@link org.junit.Before} annotated method is:      <code>
     *
     * @Rule
     * public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();
     *
     * @BeforeEach
     * public void setUp() {
     *     resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
     * }
     * </code>
     *
     * @param profile the provided profile
     */
    public static void resetProfileManager(Profile profile) {
        ProfileManager.getDefault().setActiveProfile(profile);
    }

    /**
     * PreferencesProviders retain per-profile objects; reset them to force that
     * information to be dumped.
     */
    public static void resetPreferencesProviders() {
        try {
            // reset UI provider
            Field providers = JmriUserInterfaceConfigurationProvider.class.getDeclaredField("PROVIDERS");
            providers.setAccessible(true);
            ((Map<?, ?>) providers.get(null)).clear();
            // reset XML storage provider
            providers = JmriConfigurationProvider.class.getDeclaredField("PROVIDERS");
            providers.setAccessible(true);
            ((Map<?, ?>) providers.get(null)).clear();
            // reset java.util.prefs.Preferences storage provider
            Field shared = JmriPreferencesProvider.class.getDeclaredField("SHARED_PROVIDERS");
            Field privat = JmriPreferencesProvider.class.getDeclaredField("PRIVATE_PROVIDERS");
            shared.setAccessible(true);
            ((Map<?, ?>) shared.get(null)).clear();
            privat.setAccessible(true);
            ((Map<?, ?>) privat.get(null)).clear();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            log.error("Unable to reset preferences providers", ex);
        }
    }

    /**
     * Silences the outputs from the Jemmy GUI Test framework.
     */
    public static void silenceGUITestOutput() {
        JUnitUtil.setGUITestOutput(TestOut.getNullOutput());
    }

    /**
     * Sets the outputs for the Jemmy GUI Test framework to the defaults. Call
     * this after setting up logging to enable outputs for a specific test.
     */
    public static void verboseGUITestOutput() {
        JUnitUtil.setGUITestOutput(new TestOut());
    }

    /**
     * Set the outputs for the Jemmy GUI Test framework.
     *
     * @param output a container for the input, output, and error streams
     */
    public static void setGUITestOutput(TestOut output) {
        org.netbeans.jemmy.JemmyProperties.setCurrentOutput(output);
    }

    /**
     * Service method to find the test class name in the traceback. Heuristic:
     * First jmri or apps class that isn't this one.
     * @return String class name
     */
    static String getTestClassName() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        for (StackTraceElement e : trace) {
            String name = e.getClassName();
            if (name.startsWith("jmri") || name.startsWith("apps")) {
                if (!name.endsWith("JUnitUtil")) {
                    return name;
                }
            }
        }

        return "<unknown class>";
    }

    /**
     * Dispose of any disposable windows. This should only be used if there is
     * no ability to actually close windows opened by a test using
     * {@link #dispose(java.awt.Window)} or
     * {@link #disposeFrame(java.lang.String, boolean, boolean)}, since this may
     * mask other side effects that should be dealt with explicitly.
     *
     * @param warn  log a warning for each window if true
     * @param error log an error (failing the test) for each window if true
     */
    public static void resetWindows(boolean warn, boolean error) {
        // close any open remaining windows from earlier tests
        for (Frame frame : Frame.getFrames()) {
            if (frame.isDisplayable()) {
                if (frame.getClass().getName().equals("javax.swing.SwingUtilities$SharedOwnerFrame")) {
                    String message = "Cleaning up nameless invisible frame created by creating a dialog with a null parent in {}.";
                    if (error) {
                        log.error(message, getTestClassName());
                    } else if (warn) {
                        log.warn(message, getTestClassName());
                    }
                } else {
                    String message = "Cleaning up frame \"{}\" (a {}) in {}.";
                    if (error) {
                        log.error(message, frame.getTitle(), frame.getClass(), getTestClassName());
                    } else if (warn) {
                        log.warn(message, frame.getTitle(), frame.getClass(), getTestClassName());
                    }
                }
                JUnitUtil.dispose(frame);
            }
        }
        for (Window window : Window.getWindows()) {
            if (window.isDisplayable()) {
                if (window.getClass().getName().equals("javax.swing.SwingUtilities$SharedOwnerFrame")) {
                    String message = "Cleaning up nameless invisible window created by creating a dialog with a null parent in {}.";
                    if (error) {
                        log.error(message, getTestClassName());
                    } else if (warn) {
                        log.warn(message, getTestClassName());
                    }
                } else {
                    String message = "Cleaning up window \"{}\" (a {}) in {}.";
                    if (error) {
                        log.error(message, window.getName(), window.getClass(), getTestClassName());
                    } else if (warn) {
                        log.warn(message, window.getName(), window.getClass(), getTestClassName());
                    }
                }
                JUnitUtil.dispose(window);
            }
        }
    }

    /**
     * Dispose of a visible frame searched for by title. Disposes of the first
     * visible frame found with the given title. Asserts that the calling test
     * failed if the frame cannot be found.
     *
     * @param title the title of the frame to dispose of
     * @param ce    true to match title param as a substring of the frame's
     *              title; false to require an exact match
     * @param cc    true if search is case sensitive; false otherwise
     */
    public static void disposeFrame(String title, boolean ce, boolean cc) {
        Frame frame = FrameWaiter.getFrame(title, ce, cc);
        if (frame != null) {
            JUnitUtil.dispose(frame);
        } else {
            Assert.fail("Unable to find frame \"" + title + "\" to dispose.");
        }
    }

    /**
     * Dispose of a window. Disposes of the window on the GUI thread, returning
     * only after the window is disposed of.
     *
     * @param window the window to dispose of
     */
    public static void dispose(@Nonnull Window window) {
        java.util.Objects.requireNonNull(window, "Window cannot be null");

        ThreadingUtil.runOnGUI(() -> {
            window.dispose();
        });
    }

    public static Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

    public static Thread getThreadStartsWithName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().startsWith(threadName)) return t;
        }
        return null;
    }

    static SortedSet<String> threadNames = new TreeSet<>(Arrays.asList(new String[]{
        // names we know about from normal running
        "main",
        "Java2D Disposer",
        "AWT-Shutdown",
        "AWT-EventQueue",
        "AWT-XAWT",                         // seen on Jenkins Ubuntu
        "GC Daemon",
        "Finalizer",
        "Reference Handler",
        "Signal Dispatcher",                // POSIX signals in JRE, not trains signals
        "Java2D Queue Flusher",
        "Time-limited test",
        "WindowMonitor-DispatchThread",
        "RMI Reaper",
        "RMI TCP Accept",
        "TimerQueue",
        "Java Sound Event Dispatcher",
        "Aqua L&F",                         // macOS
        "AppKit Thread",
        "JMRI Common Timer",
        "BluecoveAsynchronousShutdownThread", // from LocoNet BlueTooth implementation
        "Keep-Alive-Timer",                 // from "system" group
        "process reaper",                   // observed in macOS JRE
        "SIGINT handler",                   // observed in JmDNS; clean shutdown takes time
        "Multihomed mDNS.Timer",            // observed in JmDNS; clean shutdown takes time
        "Direct Clip",                      // observed in macOS JRE, associated with javax.sound.sampled.AudioSystem
        "Basic L&F File Loading Thread",
        "dns.close in ZeroConfServiceManager#stopAll",
        "Common-Cleaner",
        "Batik CleanerThread"  // XML
    }));
    static List<Thread> threadsSeen = new ArrayList<>();

    /**
     * Do a diagnostic check of threads,
     * providing a traceback if any new ones are still around.
     * <p>
     * First implementation is rather simplistic.
     */
    static void handleThreads() {
        // now check for extra threads
        ThreadGroup main = Thread.currentThread().getThreadGroup();
        while (main.getParent() != null ) {main = main.getParent(); }
        Thread[] list = new Thread[main.activeCount()+2];  // space on end
        int max = main.enumerate(list);

        for (int i = 0; i<max; i++) {
            Thread t = list[i];
            if (t.getState() == Thread.State.TERMINATED) { // going away, just not cleaned up yet
                threadsSeen.remove(t);  // don't want to prevent gc
                continue;
            }
            if (threadsSeen.contains(t)) continue;
            String name = t.getName();
            ThreadGroup g = t.getThreadGroup();
            String group = (g != null) ?  g.getName() : "<null group>";

            if (! (
                    threadNames.contains(name)
                 || group.equals("system")
                 || name.startsWith("Timer-")  // we separately scan for JMRI-resident timers
                 || name.startsWith("RMI TCP Accept")
                 || name.startsWith("AWT-EventQueue")
                 || name.startsWith("Aqua L&F")
                 || name.startsWith("junit-jupiter-")  // JUnit
                 || name.startsWith("Image Fetcher ")
                 || name.startsWith("Image Animator ")
                 || name.startsWith("JmDNS(")
                 || name.startsWith("JmmDNS pool")
                 || name.startsWith("ForkJoinPool.commonPool-worker")
                 || name.startsWith("SocketListener(")
                 || group.contains("FailOnTimeoutGroup") // JUnit timeouts
                 || ( name.startsWith("SwingWorker-pool-1-thread-") &&
                         ( group.contains("FailOnTimeoutGroup") || group.contains("main") )
                    )
                )) {

                        if (t.getState() == Thread.State.TERMINATED) {
                            // might have transitioned during above (logging slow)
                            continue;
                        }

                        // This thread we have to deal with.
                        boolean kill = true;
                        String action = "Interrupt";
                        if (!killRemnantThreads) {
                            action = "Found";
                            kill = false;
                        }
                        if (name.toUpperCase().startsWith("OLCB") || name.toUpperCase().startsWith("OPENLCB")) { // ugly special case
                            action = "Skipping";
                            kill = false;
                        }

                        // for anonymous threads, show the traceback in hopes of finding what it is
                        if (name.startsWith("Thread-")) {
                            StackTraceElement[] traces = Thread.getAllStackTraces().get(t);
                            if (traces == null) continue;  // thread went away, maybe terminated in parallel
                            if (traces.length >7 && traces[7].getClassName().contains("org.netbeans.jemmy") ) {
                                // empirically. jemmy leaves anonymous threads
                                log.warn("Jemmy remnant thread running {}.{} [{}.{}]",
                                        traces[7].getClassName(),
                                        traces[7].getMethodName(),
                                       traces[7].getFileName(),
                                       traces[7].getLineNumber()
                                    );
                            } else {
                                // anonymous thread that should be displayed
                                Exception ex = new Exception("traceback of numbered thread");
                                ex.setStackTrace(traces);
                                log.warn("{} remnant thread \"{}\" in group \"{}\" after {}", action, name, group, getTestClassName(), ex);
                            }
                        } else {
                            log.warn("{} remnant thread \"{}\" in group \"{}\" after {}", action, name, group, getTestClassName());
                        }
                        if (kill) {
                            killThread(t);
                        } else {
                            threadsSeen.add(t);
                        }
            }
        }
    }

    /* Global Panel operations */
    /**
     * Close all panels associated with the {@link EditorManager} default
     * instance.
     */
    public static void closeAllPanels() {
        InstanceManager.getOptionalDefault(EditorManager.class)
                .ifPresent(m -> m.getAll()
                        .forEach(e -> {
                            if(e.isVisible()){
                               e.requestFocus();
                               try {
                                   EditorFrameOperator editorFrameOperator = new EditorFrameOperator(e.getTargetFrame());
                                   editorFrameOperator.closeFrameWithConfirmations();
                               } catch (TimeoutExpiredException timeoutException ) {
                                   log.error("Failed to close panel {} with exception {}",e.getTitle(),
                                           timeoutException.getMessage(),
                                           LoggingUtil.shortenStacktrace(timeoutException));
                               }
                            }
                            e.dispose();
                        }));
        EditorFrameOperator.clearEditorFrameOperatorThreads();
    }

    /* GraphicsEnvironment utility methods */

    /**
     * Get the content pane of a dialog.
     *
     * @param title the dialog title
     * @return the content pane
     */
    public static Container findContainer(String title) {
        return new JDialogOperator(title).getContentPane();
    }

    /**
     * Press a button after finding it in a container by title.
     *
     * @param frame container containing button to press
     * @param text button title
     * @return the pressed button
     */
    public static AbstractButton pressButton(Container frame, String text) {
        AbstractButton button = JButtonOperator.findAbstractButton(frame, text, true, true);
        Assert.assertNotNull(text + " Button not found", button);
        AbstractButtonOperator abo = new AbstractButtonOperator(button);
        abo.doClick();
        return button;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JUnitUtil.class);

}
