package jmri.util;

import java.lang.reflect.InvocationTargetException;
import jmri.ConditionalManager;
import jmri.ConfigureManager;
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
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.managers.AbstractSignalHeadManager;
import jmri.managers.DefaultConditionalManager;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.DefaultLogixManager;
import jmri.managers.DefaultMemoryManager;
import jmri.managers.DefaultSignalMastLogicManager;
import jmri.managers.InternalReporterManager;
import jmri.managers.InternalSensorManager;
import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for working with JUnit.
 * <p>
 * To release the current thread and allow other listeners to execute:  <code><pre>
 * JUnitUtil.releaseThread(this);
 * </pre></code> Note that this is not appropriate for Swing objects; you need
 * to use JFCUnit for that.
 * <p>
 * If you're using the InstanceManager, setUp() implementation should start
 * with:  <pre><code>
 * super.setUp();
 * JUnitUtil.resetInstanceManager();
 * JUnitUtil.initInternalTurnoutManager();
 * JUnitUtil.initInternalLightManager();
 * JUnitUtil.initInternalSensorManager();
 * JUnitUtil.initDebugThrottleManager();
 * </code></pre>
 * <p>
 * Your tearDown() should end with:  <pre><code>
 * JUnitUtil.resetInstanceManager();
 * super.tearDown();
 * </code></pre>
 *
 * Note that memory managers and some others are completely internal, and will
 * be reset when you reset the instance manager.
 *
 * @author Bob Jacobsen Copyright 2009, 2015
 * @since 2.5.3
 */
public class JUnitUtil {

    static final int DEFAULT_RELEASETHREAD_DELAY = 50;

    static int count = 0;
    /**
     * Release the current thread, allowing other threads to process.
     * 
     * This cannot be used on the Swing or AWT event threads.
     * For those, please use JFCUnit's flushAWT() and waitAtLeast(..)
     */
    public static void releaseThread(Object self) {
        releaseThread(self, DEFAULT_RELEASETHREAD_DELAY);
    }

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

    static final int WAITFOR_DELAY_STEP = 5;
    static final int WAITFOR_MAX_DELAY = 15000; // really long, but only matters when failing
    
    /** 
     * Wait for a specific condition to be true, without having to wait longer
     * <p>
     * To be used in tests, will do an assert if the total delay is longer than WAITFOR_MAX_DELAY
     * <p>
     * Typical use:
     * waitFor(()->{return replyVariable != null;},"reply not received")
     *
     * @param condition name of condition being waited for; will appear in Assert.fail if condition not true fast enough
     */
    static public void waitFor(ReleaseUntil condition, String name) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use waitFor on Swing thread", new Exception());
            return;
        }
        int delay = 0;
        try {
            while (delay < WAITFOR_MAX_DELAY) {
                if (condition.ready()) return;
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
            Assert.fail("\""+name+"\" did not occur in time");
        } catch (Exception ex) {
            Assert.fail("Exception while waiting for \""+name+"\" "+ex);
        }
    }

    static public interface ReleaseUntil {
        public boolean ready() throws Exception;
    }

    /** 
     * Set a NamedBean (Turnout, Sensor, SignalHead, ...)
     * to a specific value in a thread-safe way.
     * 
     * You can't assume that all the consequences of that setting
     * will have propagated through when this returns; those might
     * take a long time.  But the set operation itself will be complete.
     * @param NamedBean
     * @param state
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
    
    public static void resetInstanceManager() {
        // clear system connections
        jmri.jmrix.SystemConnectionMemo.reset();

        // create a new instance manager & use initializer to clear static list of state
        new InstanceManager() {
            { managerLists.clear(); }
        };
        
        // add the NamedBeanHandleManager, which is always needed
        InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    public static void resetTurnoutOperationManager() {
        new jmri.TurnoutOperationManager(){
            { resetTheInstance();}
        };
    }
    
    public static void initConfigureManager() {
        InstanceManager.setDefault(ConfigureManager.class, new JmriConfigurationManager());
    }

    public static void initDefaultUserMessagePreferences() {
        InstanceManager.store(
                new jmri.managers.TestUserPreferencesManager(),
                jmri.UserPreferencesManager.class);
    }

    public static void initInternalTurnoutManager() {
        // now done automatically by InstanceManager's autoinit
        jmri.InstanceManager.turnoutManagerInstance();
    }

    public static void initInternalLightManager() {
        // now done automatically by InstanceManager's autoinit
         jmri.InstanceManager.lightManagerInstance();
   }

    public static void initInternalSensorManager() {
        // now done automatically by InstanceManager's autoinit
        jmri.InstanceManager.sensorManagerInstance();
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
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.MEMORIES);
        }
    }

    public static void initReporterManager() {
        ReporterManager m = new InternalReporterManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.REPORTERS);
        }
    }

    public static void initOBlockManager() {
        OBlockManager b = new OBlockManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(b, jmri.Manager.OBLOCKS);
        }
    }

    public static void initWarrantManager() {
        WarrantManager w = new WarrantManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.WARRANTS);
        }
    }

    public static void initSignalMastLogicManager() {
        SignalMastLogicManager w = new DefaultSignalMastLogicManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.SIGNALMASTLOGICS);
        }
    }

    public static void initLayoutBlockManager() {
        LayoutBlockManager w = new LayoutBlockManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.LAYOUTBLOCKS);
        }
    }

    public static void initSectionManager() {
        jmri.SectionManager w = new jmri.SectionManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(w, jmri.Manager.SECTIONS);
        }
    }

    public static void initInternalSignalHeadManager() {
        SignalHeadManager m = new AbstractSignalHeadManager();
        InstanceManager.setSignalHeadManager(m);
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.SIGNALHEADS);
        }
    }

    public static void initDebugThrottleManager() {
        jmri.ThrottleManager m = new DebugThrottleManager();
        InstanceManager.setThrottleManager(m);
    }

    public static void initDebugPowerManager() {
        InstanceManager.setDefault(PowerManager.class, new PowerManagerScaffold());
    }

    public static void initIdTagManager() {
        InstanceManager.reset(jmri.IdTagManager.class);
        InstanceManager.store(new DefaultIdTagManager(), jmri.IdTagManager.class);
    }

    public static void initLogixManager() {
        LogixManager m = new DefaultLogixManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.LOGIXS);
        }
    }

    public static void initConditionalManager() {
        ConditionalManager m = new DefaultConditionalManager();
        if (InstanceManager.getOptionalDefault(ConfigureManager.class) != null) {
            InstanceManager.getDefault(ConfigureManager.class).registerConfig(m, jmri.Manager.CONDITIONALS);
        }
    }
    
    public static void initShutDownManager() {
        if (InstanceManager.getOptionalDefault(ShutDownManager.class) == null) {
            InstanceManager.setDefault(ShutDownManager.class, new MockShutDownManager());
        }
    }

    public static void initStartupActionsManager() {
        InstanceManager.store(
                new apps.StartupActionsManager(),
                apps.StartupActionsManager.class);
    }

    private final static Logger log = LoggerFactory.getLogger(JUnitUtil.class.getName());
}
