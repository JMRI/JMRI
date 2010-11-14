package jmri.util;

import junit.framework.Assert;

import jmri.SignalHeadManager;
import jmri.MemoryManager;

import jmri.InstanceManager;
import jmri.managers.InternalLightManager;
import jmri.managers.InternalSensorManager;
import jmri.managers.InternalTurnoutManager;
import jmri.managers.AbstractSignalHeadManager;
import jmri.managers.DefaultMemoryManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;

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
 * @version $Revision: 1.10 $
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
			protected void init() {
				root = null;
				super.init();
				root = this;
			}
		};
	}

    public static void initConfigureManager() {
        InstanceManager.setConfigureManager(new jmri.configurexml.ConfigXmlManager());
    }

    public static void initInternalTurnoutManager() {
        InstanceManager.setTurnoutManager(new InternalTurnoutManager());
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(
                InstanceManager.turnoutManagerInstance());
    }

    public static void initInternalLightManager() {
        InternalLightManager m = new InternalLightManager();
        InstanceManager.setLightManager(m);
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m);
    }

    public static void initInternalSensorManager() {
        InternalSensorManager m = new InternalSensorManager();
        InstanceManager.setSensorManager(m);
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m);
    }

    public static void initMemoryManager() {
        MemoryManager m = new DefaultMemoryManager();
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m);
    }

    public static void initInternalSignalHeadManager() {
        SignalHeadManager m = new AbstractSignalHeadManager();
        InstanceManager.setSignalHeadManager(m);
        if (InstanceManager.configureManagerInstance() != null)
            InstanceManager.configureManagerInstance().registerConfig(m);
    }

    public static void initDebugThrottleManager() {
        jmri.ThrottleManager m = new DebugThrottleManager();
        InstanceManager.setThrottleManager(m);
        return;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JUnitUtil.class.getName());
}