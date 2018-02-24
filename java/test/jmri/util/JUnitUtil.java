package jmri.util;

import apps.gui.GuiLafPreferencesManager;
import apps.tests.Log4JFixture;
import java.awt.Frame;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.annotation.Nonnull;
import jmri.ConditionalManager;
import jmri.ConfigureManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.LogixManager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.PowerManagerScaffold;
import jmri.ReporterManager;
import jmri.RouteManager;
import jmri.ShutDownManager;
import jmri.SignalHeadManager;
import jmri.SignalMastLogicManager;
import jmri.SignalMastManager;
import jmri.TurnoutOperationManager;
import jmri.UserPreferencesManager;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.managers.AbstractSignalHeadManager;
import jmri.managers.DefaultConditionalManager;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultLogixManager;
import jmri.managers.DefaultMemoryManager;
import jmri.managers.DefaultRailComManager;
import jmri.managers.DefaultSignalMastLogicManager;
import jmri.managers.DefaultSignalMastManager;
import jmri.managers.InternalReporterManager;
import jmri.managers.InternalSensorManager;
import jmri.managers.TestUserPreferencesManager;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.progdebugger.DebugProgrammerManager;
import jmri.util.prefs.JmriConfigurationProvider;
import jmri.util.prefs.JmriPreferencesProvider;
import jmri.util.prefs.JmriUserInterfaceConfigurationProvider;
import org.junit.Assert;
import org.netbeans.jemmy.FrameWaiter;
import org.netbeans.jemmy.TestOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    static final int DEFAULT_RELEASETHREAD_DELAY = 50;
    static final int WAITFOR_DELAY_STEP = 5;
    static final int WAITFOR_MAX_DELAY = 30000; // really long, but only matters when failing, and LayoutEditor/SignalMastLogic is slow

    static int count = 0;

    /**
     * Setup for tests. This should be the first line in the {@code @Before}
     * annotated method.
     */
    public static void setUp() {
        Log4JFixture.setUp();
        // ideally this would be false, true to force an error if an earlier
        // test left a window open, but different platforms seem to have just
        // enough differences that this is, for now, only emitting a warning
        resetWindows(true, false);
        resetInstanceManager();
    }

    /**
     * Teardown from tests. This should be the last line in the {@code @After}
     * annotated method.
     */
    public static void tearDown() {
        resetWindows(true, false); // warn
        resetInstanceManager();
        Log4JFixture.tearDown();
    }

    /**
     * Release the current thread, allowing other threads to process. Waits for
     * {@value #DEFAULT_RELEASETHREAD_DELAY} milliseconds.
     * <p>
     * This cannot be used on the Swing or AWT event threads. For those, please
     * use JFCUnit's flushAWT() and waitAtLeast(..)
     *
     * @param self currently ignored
     * @deprecated 4.9.1 Use the various waitFor routines instead
     */
    @Deprecated
    public static void releaseThread(Object self) {
        releaseThread(self, DEFAULT_RELEASETHREAD_DELAY);
    }

    /**
     * Release the current thread, allowing other threads to process.
     * <p>
     * This cannot be used on the Swing or AWT event threads. For those, please
     * use JFCUnit's flushAWT() and waitAtLeast(..)
     *
     * @param self  currently ignored
     * @param delay milliseconds to wait
     * @deprecated 4.9.1 Use the various waitFor routines instead
     */
    @Deprecated
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
     * Reset the user files path in the default
     * {@link jmri.util.FileUtilSupport} object (used by
     * {@link jmri.util.FileUtil}) to the default settings/user files path for
     * tests of {@code git-working-copy/temp}.
     */
    public static void resetFileUtilSupport() {
        FileUtilSupport.getDefault().setUserFilesPath(FileUtil.getPreferencesPath());
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
        // ensure the auto-defeault UserPreferencesManager is not created
        InstanceManager.setDefault(UserPreferencesManager.class, new TestUserPreferencesManager());
    }

    public static void resetTurnoutOperationManager() {
        InstanceManager.reset(TurnoutOperationManager.class);
        TurnoutOperationManager.getDefault();
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
        MemoryManager m = new DefaultMemoryManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.MEMORIES);
        }
    }

    public static void initReporterManager() {
        ReporterManager m = new InternalReporterManager();
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
        SignalMastLogicManager w = new DefaultSignalMastLogicManager();
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
        SignalHeadManager m = new AbstractSignalHeadManager();
        InstanceManager.setSignalHeadManager(m);
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.SIGNALHEADS);
        }
    }

    public static void initDefaultSignalMastManager() {
        InstanceManager.setDefault(SignalMastManager.class, new DefaultSignalMastManager());
    }

    public static void initDebugCommandStation() {
        jmri.CommandStation cs = new jmri.CommandStation() {
            @Override
            public void sendPacket(@Nonnull byte[] packet, int repeats) {
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
        // we should use setDefault here, but setCommandStation will
        // install a consist manager if one is not already installed.
        //InstanceManager.setDefault(jmri.CommandStation.class,cs);
        InstanceManager.setCommandStation(cs);
    }

    public static void initDebugThrottleManager() {
        jmri.ThrottleManager m = new DebugThrottleManager();
        InstanceManager.setThrottleManager(m);
    }

    public static void initDebugProgrammerManager() {
        DebugProgrammerManager m = new DebugProgrammerManager();
        InstanceManager.setAddressedProgrammerManager(m);
        InstanceManager.store(m, GlobalProgrammerManager.class);
    }

    public static void initDebugPowerManager() {
        InstanceManager.setDefault(PowerManager.class, new PowerManagerScaffold());
    }

    public static void initIdTagManager() {
        InstanceManager.reset(jmri.IdTagManager.class);
        InstanceManager.store(new DefaultIdTagManager(), jmri.IdTagManager.class);
    }

    public static void initRailComManager() {
        InstanceManager.reset(jmri.RailComManager.class);
        InstanceManager.store(new DefaultRailComManager(), jmri.RailComManager.class);
    }

    public static void initLogixManager() {
        LogixManager m = new DefaultLogixManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.LOGIXS);
        }
    }

    public static void initConditionalManager() {
        ConditionalManager m = new DefaultConditionalManager();
        if (InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.CONDITIONALS);
        }
    }

    public static void initShutDownManager() {
        if (InstanceManager.getNullableDefault(ShutDownManager.class) == null) {
            InstanceManager.setDefault(ShutDownManager.class, new MockShutDownManager());
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

    /*
     * Use reflection to reset the jmri.Application instance
     */
    public static void resetApplication() {
        try {
            Class<?> c = jmri.Application.class;
            java.lang.reflect.Field f = c.getDeclaredField("name");
            f.setAccessible(true);
            f.set(new jmri.Application(), null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            log.error("Failed to reset jmri.Application static field", x);
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
     */
    static String getTestClassName() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        for (StackTraceElement e : trace) {
            if (e.getClassName().startsWith("jmri") || e.getClassName().startsWith("apps")) {
                if (!e.getClassName().endsWith("JUnitUtil")) {
                    return e.getClassName();
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
                    if (!error) {
                        log.warn(message, getTestClassName());
                    } else {
                        log.error(message, getTestClassName());
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
                    if (!error) {
                        log.warn(message, getTestClassName());
                    } else {
                        log.error(message, getTestClassName());
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
        ThreadingUtil.runOnGUI(() -> {
            window.dispose();
        });
    }

    private final static Logger log = LoggerFactory.getLogger(JUnitUtil.class);

}
