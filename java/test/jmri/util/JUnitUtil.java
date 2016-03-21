package jmri.util;

import java.lang.reflect.InvocationTargetException;

import jmri.ConditionalManager;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.LogixManager;
import jmri.NamedBean;
import jmri.MemoryManager;
import jmri.PowerManager;
import jmri.PowerManagerScaffold;
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
import jmri.managers.InternalLightManager;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;

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
 * with:  <code><pre>
 * super.setUp();
 * JUnitUtil.resetInstanceManager();
 * JUnitUtil.initInternalTurnoutManager();
 * JUnitUtil.initInternalLightManager();
 * JUnitUtil.initInternalSensorManager();
 * JUnitUtil.initDebugThrottleManager();
 * </pre></code>
 * <p>
 * Your tearDown() should end with:  <code><pre>
 * JUnitUtil.resetInstanceManager();
 * super.tearDown();
 * </pre></code>
 *
 * Note that memory managers and some others are completely internal, and will
 * be reset when you reset the instance manager.
 *
 * @author Bob Jacobsen Copyright 2009, 2015
 * @version $Revision$
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
    static final int WAITFOR_MAX_DELAY = 5000; // really long, but only matters when failing
    
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

        // create a new instance manager
        new InstanceManager() {
            @Override
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
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
    }

    public static void initMemoryManager() {
        MemoryManager m = new DefaultMemoryManager();
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.MEMORIES);
        }
    }

    public static void initOBlockManager() {
        OBlockManager b = new OBlockManager();
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(b, jmri.Manager.OBLOCKS);
        }
    }

    public static void initWarrantManager() {
        WarrantManager w = new WarrantManager();
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(w, jmri.Manager.WARRANTS);
        }
    }

    public static void initSignalMastLogicManager() {
        SignalMastLogicManager w = new DefaultSignalMastLogicManager();
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(w, jmri.Manager.SIGNALMASTLOGICS);
        }
    }

    public static void initLayoutBlockManager() {
        LayoutBlockManager w = new LayoutBlockManager();
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(w, jmri.Manager.LAYOUTBLOCKS);
        }
    }

    public static void initSectionManager() {
        jmri.SectionManager w = new jmri.SectionManager();
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(w, jmri.Manager.SECTIONS);
        }
    }

    public static void initInternalSignalHeadManager() {
        SignalHeadManager m = new AbstractSignalHeadManager();
        InstanceManager.setSignalHeadManager(m);
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.SIGNALHEADS);
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
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.LOGIXS);
        }
    }

    public static void initConditionalManager() {
        ConditionalManager m = new DefaultConditionalManager();
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.CONDITIONALS);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JUnitUtil.class.getName());
}
