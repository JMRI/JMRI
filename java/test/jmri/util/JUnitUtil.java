package jmri.util;

import org.apache.log4j.Logger;
import junit.framework.Assert;

import jmri.*;
import jmri.managers.*;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrit.logix.OBlockManager;

import java.beans.PropertyChangeListener;

/**
 * Common utility methods for working with JUnit.
 *<p>
 * To release the current thread and allow other listeners to execute:
<code><pre>
    JUnitUtil.releaseThread(this);
</pre></code>
 * Note that this is not appropriate for Swing objects;
 * you need to use JFCUnit for that.
 *<p>
 * If you're using the InstanceManager, setUp() implementation should start with:
<code><pre>
    super.setUp();
    JUnitUtil.resetInstanceManager();
    JUnitUtil.initInternalTurnoutManager();
    JUnitUtil.initInternalLightManager();
    JUnitUtil.initInternalSensorManager();
    JUnitUtil.initDebugThrottleManager();
</pre></code>
 *<p>
 * Your tearDown() should end with:
<code><pre>
    JUnitUtil.resetInstanceManager();
    super.tearDown();
</pre></code>

 * Note that memory managers and some others are completely
 * internal, and will be reset when you reset the instance manager.
 *
 * @author Bob Jacobsen  Copyright 2009
 * @version $Revision$
 * @since 2.5.3
 */

public class JUnitUtil {

    static int DEFAULTDELAY = 200;
    
    /** 
     * Release the current thread, allowing other 
     * threads to process
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
            }
            catch (InterruptedException e) {
                Assert.fail("failed due to InterruptedException");
            }
        }
	}
    
	public static void releaseThread(Object self) {
	    releaseThread(self, DEFAULTDELAY);
    }

	public static void resetInstanceManager() {    
		// create a new instance manager
		new InstanceManager(){
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
        InstanceManager.setConfigureManager(new jmri.configurexml.ConfigXmlManager());
    }

    public static void initInternalTurnoutManager() {
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(
                InstanceManager.turnoutManagerInstance(), jmri.Manager.TURNOUTS);
    }

    public static void initInternalLightManager() {
        InternalLightManager m = new InternalLightManager();
        InstanceManager.setLightManager(m);
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.LIGHTS);
    }

    public static void initInternalSensorManager() {
        InternalSensorManager m = new InternalSensorManager();
        InstanceManager.setSensorManager(m);
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.SENSORS);
    }

    public static void initMemoryManager() {
        MemoryManager m = new DefaultMemoryManager();
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.MEMORIES);
    }

    public static void initOBlockManager() {
        OBlockManager b = new OBlockManager();
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(b, jmri.Manager.OBLOCKS);
    }

    public static void initInternalSignalHeadManager() {
        SignalHeadManager m = new AbstractSignalHeadManager();
        InstanceManager.setSignalHeadManager(m);
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m, jmri.Manager.SIGNALHEADS);
    }

    public static void initDebugThrottleManager() {
        jmri.ThrottleManager m = new DebugThrottleManager();
        InstanceManager.setThrottleManager(m);
        return;
    }

    public static void initDebugPowerManager() {
        jmri.PowerManager manager = new jmri.PowerManager() {
                int state = PowerManager.UNKNOWN;
                public void setPower(int v)     throws JmriException { state = v; }
                public int      getPower()      throws JmriException { return state;}
                public void dispose() throws JmriException {}
                public void addPropertyChangeListener(PropertyChangeListener p) {}
                public void removePropertyChangeListener(PropertyChangeListener p) {}
                public String getUserName() { return "test"; }
            }; // end of anonymous PowerManager class new()
        // store dummy power manager object for retrieval
        InstanceManager.setPowerManager(manager);
    }

    public static void initIdTagManager() {
        InstanceManager.reset(jmri.IdTagManager.class);
        InstanceManager.store(new DefaultIdTagManager(), jmri.IdTagManager.class);
    }

    static Logger log = Logger.getLogger(JUnitUtil.class.getName());
}
