package jmri.util;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.AbstractButton;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.netbeans.jemmy.FrameWaiter;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apps.gui.GuiLafPreferencesManager;
import jmri.*;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.internal.InternalReporterManager;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.*;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.progdebugger.DebugProgrammerManager;
import jmri.util.managers.InternalLightManagerThrowExceptionScaffold;
import jmri.util.managers.MemoryManagerThrowExceptionScaffold;
import jmri.util.managers.OBlockManagerThrowExceptionScaffold;
import jmri.util.managers.RouteManagerThrowExceptionScaffold;
import jmri.util.managers.SensorManagerThrowExceptionScaffold;
import jmri.util.managers.SignalHeadManagerThrowExceptionScaffold;
import jmri.util.managers.SignalMastManagerThrowExceptionScaffold;
import jmri.util.managers.TurnoutManagerThrowExceptionScaffold;
import jmri.util.managers.WarrantManagerThrowExceptionScaffold;
import jmri.util.prefs.InitializationException;
import jmri.util.prefs.JmriConfigurationProvider;
import jmri.util.prefs.JmriPreferencesProvider;
import jmri.util.prefs.JmriUserInterfaceConfigurationProvider;
import jmri.util.zeroconf.MockZeroConfServiceManager;
import jmri.util.zeroconf.ZeroConfServiceManager;

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
     * Standard time step (in mSec) when looping in a waitFor operation.
     * <p>
     * Public in case modification is needed from a test or script.
     */    
    static final public int WAITFOR_DELAY_STEP = 5;
    /**
     * Maximum time to wait before failing a waitFor operation.
     * The default value is really long, but that only matters when the test is failing anyway, 
     * and some of the LayoutEditor/SignalMastLogic tests are slow. But too long will cause CI jobs
     * to time out before this logs the error....
     * <p>
     * Public in case modification is needed from a test or script.
     */    
    static final public int WAITFOR_MAX_DELAY = 10000;

    /**
     * When true, prints each setUp method to help identify which tests include a failure.
     * When checkSetUpTearDownSequence is also true, this also sprints on execution of tearDown.
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
     * Check for any threads left behind after a test calls {@link #tearDown}
     * <p>
     * Set from the jmri.util.JUnitUtil.checkRemnantThreads environment variable.
     */
    static boolean checkRemnantThreads =    Boolean.getBoolean("jmri.util.JUnitUtil.checkRemnantThreads"); // false unless set true

    /**
     * Check for tests that take an excessive time
     * <p>
     * Set from the jmri.util.JUnitUtil.checkTestDuration environment variable.
     */
    static boolean checkTestDuration =      Boolean.getBoolean("jmri.util.JUnitUtil.checkTestDuration"); // false unless set true
    static long    checkTestDurationMax =   Long.getLong("jmri.util.JUnitUtil.checkTestDurationMax", 5000); // milliseconds

    static long    checkTestDurationStartTime = 0;  // working value
    
    static private int threadCount = 0;
    
    static private boolean didSetUp = false;
    static private boolean didTearDown = true;
    static private String lastSetUpClassName = "<unknown>";
    static private String lastSetUpThreadName = "<unknown>";
    static private StackTraceElement[] lastSetUpStackTrace = new StackTraceElement[0];
    static private String lastTearDownClassName = "<unknown>";
    static private String lastTearDownThreadName = "<unknown>";
    static private StackTraceElement[] lastTearDownStackTrace = new StackTraceElement[0];
    
    static private boolean isLoggingInitialized = false;
    static private String initPrefsDir = null;

    /**
     * JMRI standard setUp for tests. This should be the first line in the {@code @Before}
     * annotated method.
     */
    public static void setUp() {
        if (!isLoggingInitialized) {
            // init logging if needed
            isLoggingInitialized = true;
            String filename = System.getProperty("jmri.log4jconfigfilename", "tests.lcf");
            Log4JUtil.initLogging(filename);
        }
        
        // need to do this each time
        try {
            JUnitAppender.start();
            
            // reset warn _only_ once logic to make tests repeatable
            Log4JUtil.restartWarnOnce();
            // ensure logging of deprecated method calls;
            // individual tests can turn off as needed
            Log4JUtil.setDeprecatedLogging(true);
 
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
        System.setProperty("jmri.prefsdir",initPrefsDir);
        
        // silence the Jemmy GUI unit testing framework
        JUnitUtil.silenceGUITestOutput();

        // ideally this would be resetWindows(false, true) to force an error if an earlier
        // test left a window open, but different platforms seem to have just
        // enough differences that this is, for now, turned off
        resetWindows(false, false);

        // Do a minimal amount of de-novo setup
        resetInstanceManager();

        // Log and/or check the use of setUp and tearDown
        if (checkSetUpTearDownSequence || printSetUpTearDownNames) {
            lastSetUpClassName = getTestClassName();
        
            if (printSetUpTearDownNames) System.err.println(">> Starting test in "+lastSetUpClassName);
        
            if ( checkSetUpTearDownSequence)  {
                if (checkSequenceDumpsStack)  lastSetUpThreadName = Thread.currentThread().getName();
                
                if (didSetUp || ! didTearDown) {
                    System.err.println("   "+getTestClassName()+".setUp on thread "+lastSetUpThreadName+" unexpectedly found setUp="+didSetUp+" tearDown="+didTearDown+"; last tearDown was in "+lastTearDownClassName+" thread "+lastTearDownThreadName);
                    if (checkSequenceDumpsStack) {
                        System.err.println("---- This stack ------");
                        Thread.dumpStack();
                        System.err.println("---- Last setUp stack ------");
                        for (StackTraceElement e : lastSetUpStackTrace) System.err.println("	at "+e);
                        System.err.println("---- Last tearDown stack ------");
                        for (StackTraceElement e : lastTearDownStackTrace) System.err.println("	at "+e);
                        System.err.println("----------------------");
                    }
                }
                
                didTearDown = false;
                didSetUp = true;

                if (checkSequenceDumpsStack) lastSetUpStackTrace = Thread.currentThread().getStackTrace();
            }
        }
        
        // checking time?
        if (checkTestDuration) checkTestDurationStartTime = System.currentTimeMillis();
    }
    
    /**
     * Teardown from tests. This should be the last line in the {@code @After}
     * annotated method.
     */
    public static void tearDown() {

        // check for hanging shutdown tasks
        checkShutDownManager();
        
        // checking time?
        if (checkTestDuration) {
            long duration = System.currentTimeMillis() - checkTestDurationStartTime;
            if (duration > checkTestDurationMax) {
                // test too long, log that
                log.warn("Test in {} duration {} msec exceeded limit {}", getTestClassName(), duration, checkTestDurationMax);
            }
        }
        // Log and/or check the use of setUp and tearDown
        if (checkSetUpTearDownSequence || printSetUpTearDownNames) {
            lastTearDownClassName = getTestClassName();

            if (checkSetUpTearDownSequence) {
                if (checkSequenceDumpsStack) lastTearDownThreadName = Thread.currentThread().getName();
                
                if (! didSetUp || didTearDown) {
                    System.err.println("   "+getTestClassName()+".tearDown on thread "+lastTearDownThreadName+" unexpectedly found setUp="+didSetUp+" tearDown="+didTearDown+"; last setUp was in "+lastSetUpClassName+" thread "+lastSetUpThreadName);
                    if (checkSequenceDumpsStack) {
                        System.err.println("---- This stack ------");
                        Thread.dumpStack();
                        System.err.println("---- Last setUp stack ------");
                        for (StackTraceElement e : lastSetUpStackTrace) System.err.println("	at "+e);
                        System.err.println("---- Last tearDown stack ------");
                        for (StackTraceElement e : lastTearDownStackTrace) System.err.println("	at "+e);
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
        
        // Optionally, check that no threads were left running (controlled by jmri.util.JUnitUtil.checkRemnantThreads environment var)
        if (checkRemnantThreads) {
            checkThreads();
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
     * @param time Delay in msec
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
            return;
        } catch (Exception ex) {
            log.error("Exception in waitFor condition.", ex);
            return;
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
        jmri.jmrit.blockboss.BlockBossLogic.stopAllAndClear();
    }
    
    /**
     * Leaves ShutDownManager, if any, in place,
     * but removes its contents.  Instead of using this,
     * it's better to have your test code remove _and_ _check_
     * for specific items; this just suppresses output from the 
     * {@link #checkShutDownManager()} check down as part of the 
     * default end-of-test code.
     *
     * @see #checkShutDownManager()
     * @see #initShutDownManager()
     * @deprecated 4.17.4 because tests should directly test and remove queued items;
     *             we do not intend to remove this method soon but you should not use
     *             it in new code.
     */
    @Deprecated // 4.17.4 because tests should directly test and remove queued items;
                // we do not intend to remove this method soon but you should not use
                // it in new code.
    public static void clearShutDownManager() {
        if (!  InstanceManager.containsDefault(ShutDownManager.class)) return; // not present, stop (don't create)

        ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
        List<ShutDownTask> list = sm.tasks();
        while (list != null && list.size() > 0) {
            ShutDownTask task = list.get(0);
            sm.deregister(task);
            list = sm.tasks();  // avoid ConcurrentModificationException
        }
    }

    /**
     * Errors if the {@link jmri.ShutDownManager} was not left empty. Normally
     * run as part of the default end-of-test code. Considered an error so that
     * CI will flag these and tests will be improved.
     *
     * @see #clearShutDownManager()
     * @see #initShutDownManager()
     */
    static void checkShutDownManager() {
        if (!  InstanceManager.containsDefault(ShutDownManager.class)) return; // not present, stop (don't create)
        
        ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
        List<ShutDownTask> list = sm.tasks();
        while (list != null && !list.isEmpty()) {
            ShutDownTask task = list.get(0);
            log.error("Test {} left ShutDownTask registered: {} (of type {})}", getTestClassName(), task.getName(), task.getClass(), 
                        Log4JUtil.shortenStacktrace(new Exception("traceback")));
            sm.deregister(task);
            list = sm.tasks();  // avoid ConcurrentModificationException
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

    /**
     * Creates a new ShutDownManager.
     * Does not remove the contents (i.e. kill the future actions) of any existing ShutDownManager.
     * Normally, this is not needed for tests, as 
     * a {@link MockShutDownManager} is created and provided when a {@link ShutDownManager}
     * is requested from the {@link InstanceManager} via a {@link InstanceManager#getDefault()} call.
     * @see #clearShutDownManager()
     * @deprecated 4.17.5 should not be needed in new test code
     */
    @Deprecated // 4.17.5 should not be needed in new test code
    public static void initShutDownManager() {
        ShutDownManager manager = InstanceManager.getDefault(ShutDownManager.class);
        List<ShutDownTask> tasks = manager.tasks();
        while (!tasks.isEmpty()) {
            manager.deregister(tasks.get(0));
            tasks = manager.tasks(); // avoid ConcurrentModificationException
        }
        if (manager instanceof MockShutDownManager) {
            ((MockShutDownManager) manager).resetShuttingDown();
        }
    }

    public static void initStartupActionsManager() {
        InstanceManager.store(
                new apps.StartupActionsManager(),
                apps.StartupActionsManager.class);
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
     * Use reflection to reset the apps.AppsBase instance
     */
    public static void resetAppsBase() {
        try {
            Class<?> c = apps.AppsBase.class;
            Field f = c.getDeclaredField("preInit");
            f.setAccessible(true);
            f.set(null, false);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            log.error("Failed to reset apps.AppsBase static preInit field", x);
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
     * The new profile will have the name {@literal TestProfile }, the id
     * {@literal 00000000 }, and will be in the directory {@literal temp }
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
     * @Before
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
            ((HashMap<?, ?>) providers.get(null)).clear();
            // reset XML storage provider
            providers = JmriConfigurationProvider.class.getDeclaredField("PROVIDERS");
            providers.setAccessible(true);
            ((HashMap<?, ?>) providers.get(null)).clear();
            // reset java.util.prefs.Preferences storage provider
            Field shared = JmriPreferencesProvider.class.getDeclaredField("SHARED_PROVIDERS");
            Field privat = JmriPreferencesProvider.class.getDeclaredField("PRIVATE_PROVIDERS");
            shared.setAccessible(true);
            ((HashMap<?, ?>) shared.get(null)).clear();
            privat.setAccessible(true);
            ((HashMap<?, ?>) privat.get(null)).clear();
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
        "GC Daemon",
        "Finalizer",
        "Reference Handler",
        "Signal Dispatcher",                // POSIX signals in JRE
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
    }));
    static List<Thread> threadsSeen = new ArrayList<>();

    /**
     * Do a diagnostic check of threads, 
     * providing a traceback if any new ones are still around.
     * <p>
     * First implementation is rather simplistic.
     */
    static void checkThreads() {
        // now check for extra threads
        threadCount = 0;
        Thread.getAllStackTraces().keySet().forEach((t) -> 
            {
                if (threadsSeen.contains(t)) return;
                if (t.getState() == Thread.State.TERMINATED) return; // going away, just not cleaned up yet
                String name = t.getName();
                if (! (threadNames.contains(name)
                     || name.startsWith("Timer-")  // we separately scan for JMRI-resident timers
                     || name.startsWith("RMI TCP Accept")
                     || name.startsWith("AWT-EventQueue")
                     || name.startsWith("Aqua L&F")
                     || name.startsWith("Image Fetcher ")
                     || name.startsWith("Image Animator ")
                     || name.startsWith("JmDNS(")
                     || name.startsWith("SocketListener(")
                     || ( t.getThreadGroup() != null && t.getThreadGroup().getName().contains("FailOnTimeoutGroup")) // JUnit timeouts
                     || name.startsWith("SocketListener(")
                     || (name.startsWith("SwingWorker-pool-1-thread-") && 
                            ( t.getThreadGroup() != null && 
                                (t.getThreadGroup().getName().contains("FailOnTimeoutGroup") || t.getThreadGroup().getName().contains("main") )
                            ) 
                        )
                    )) {  
                    
                        // if still running, wait to see if being terminated
                        
                        threadCount++;
                        threadsSeen.add(t);
                        
                        // for anonymous threads, show the traceback in hopes of finding what it is
                        if (name.startsWith("Thread-")) {
                            Exception ex = new Exception("traceback of numbered thread");
                            ex.setStackTrace(Thread.getAllStackTraces().get(t));
                            log.warn("Found remnant thread \"{}\" in group \"{}\" after {}", t.getName(), (t.getThreadGroup() != null ? t.getThreadGroup().getName() : "<no group>"), getTestClassName(), ex);
                        } else {
                            log.warn("Found remnant thread \"{}\" in group \"{}\" after {}", t.getName(), (t.getThreadGroup() != null ? t.getThreadGroup().getName() : "<no group>"), getTestClassName());
                        }
                }
            });
    }

    /* Global Panel operations */
    /**
     * Close all panels associated with the {@link EditorManager} default
     * instance.
     */
    public static void closeAllPanels() {
        EditorManager manager = InstanceManager.getNullableDefault(EditorManager.class);
        if (manager != null) {
            for (Editor e : manager.getEditorsList()) {
                new EditorFrameOperator(e).closeFrameWithConfirmations();
            }
        }
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
     * @param clazz an object no longer used
     * @param frame container containing button to press
     * @param text button title
     * @return the pressed button
     * @deprecated use {@link #pressButton(Container, String)} instead
     */
    @Deprecated // for removal after 4.18
    public static AbstractButton pressButton(SwingTestCase clazz, Container frame, String text) {
        return pressButton(frame, text);
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

    private final static Logger log = LoggerFactory.getLogger(JUnitUtil.class);

}
