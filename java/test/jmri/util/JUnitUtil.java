package jmri.util;

import java.beans.PropertyChangeListener;
import jmri.ConditionalManager;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.LogixManager;
import jmri.MemoryManager;
import jmri.PowerManager;
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
 * @author Bob Jacobsen Copyright 2009
 * @version $Revision$
 * @since 2.5.3
 */
public class JUnitUtil {

    static int DEFAULTDELAY = 200;

    /**
     * Release the current thread, allowing other threads to process
     */
    public static void releaseThread(Object self, int delay) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use releaseThread on Swing thread", new Exception());
            return;
        }
        synchronized (self) {
            try {
                int priority = Thread.currentThread().getPriority();
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                Thread.yield();
                Thread.sleep(delay);
                Thread.currentThread().setPriority(priority);
                self.wait(delay);
            } catch (InterruptedException e) {
                Assert.fail("failed due to InterruptedException");
            }
        }
    }

    public static void releaseThread(Object self) {
        releaseThread(self, DEFAULTDELAY);
    }

    public static void resetInstanceManager() {
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

    public static void initConfigureManager() {
        InstanceManager.setDefault(ConfigureManager.class, new JmriConfigurationManager());
    }

    public static void initInternalTurnoutManager() {
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(
                    InstanceManager.turnoutManagerInstance(), jmri.Manager.TURNOUTS);
        }
    }

    public static void initInternalLightManager() {
        InternalLightManager m = new InternalLightManager();
        InstanceManager.setLightManager(m);
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.LIGHTS);
        }
    }

    public static void initInternalSensorManager() {
        InternalSensorManager m = new InternalSensorManager();
        InstanceManager.setSensorManager(m);
        if (InstanceManager.configureManagerInstance() != null) {
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.SENSORS);
        }
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
        jmri.PowerManager manager = new jmri.PowerManager() {
            int state = PowerManager.UNKNOWN;

            public void setPower(int v) throws JmriException {
                state = v;
            }

            public int getPower() throws JmriException {
                return state;
            }

            public void dispose() throws JmriException {
            }

            public void addPropertyChangeListener(PropertyChangeListener p) {
            }

            public void removePropertyChangeListener(PropertyChangeListener p) {
            }

            public String getUserName() {
                return "test";
            }
        }; // end of anonymous PowerManager class new()
        // store dummy power manager object for retrieval
        InstanceManager.setPowerManager(manager);
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

    static Logger log = LoggerFactory.getLogger(JUnitUtil.class.getName());
}
