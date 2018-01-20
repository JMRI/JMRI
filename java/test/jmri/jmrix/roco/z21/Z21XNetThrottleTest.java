package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.lenz.z21XNetThrottle class
 *
 * @author	Paul Bender
 */
public class Z21XNetThrottleTest extends jmri.jmrix.lenz.XNetThrottleTest {

    @Test(timeout=1000)
    @Override
    public void testCtor() {
        // infrastructure objects
        Z21XNetThrottle t = new Z21XNetThrottle(memo,tc);
        Assert.assertNotNull(t);
    }

    // Test the constructor with an address specified.
    @Test(timeout=1000)
    @Override
    public void testCtorWithArg() throws Exception {
        Assert.assertNotNull(instance);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        tc = new XNetInterfaceScaffold(new RocoZ21CommandStation());
        memo = new XNetSystemConnectionMemo(tc);
        memo.setThrottleManager(new Z21XNetThrottleManager(memo)); 
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class,memo.getThrottleManager());
        instance = new Z21XNetThrottle(memo, new jmri.DccLocoAddress(3, false), tc);
    }

    @After
    public void tearDown() throws Exception {
        java.lang.reflect.Method throttleDisposeMethod = null;
        try {
           throttleDisposeMethod = instance.getClass().getDeclaredMethod("throttleDispose");
           throttleDisposeMethod.setAccessible(true);
           throttleDisposeMethod.invoke(instance);
        } catch(java.lang.NoSuchMethodException | 
                java.lang.IllegalAccessException | 
                java.lang.reflect.InvocationTargetException e ) {
           // error getting method so we could stop threads.
        }
        JUnitUtil.tearDown();
    }

}
