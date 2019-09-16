package jmri.managers;

import java.beans.PropertyChangeListener;
import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrix.internal.InternalTurnoutManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the ProxyTurnoutManager.
 *
 * @author	Bob Jacobsen 2003, 2006, 2008, 2014, 2018
 */
public class ProxyTurnoutManagerTest {

    public String getSystemName(int i) {
        return "JT" + i;
    }

    protected TurnoutManager l = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testPutGet() {
        // create
        Turnout t = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testProvideFailure() {
        try {
            l.provideTurnout("");
            Assert.fail("didn't throw");
        } catch (IllegalArgumentException ex) {
            JUnitAppender.assertErrorMessage("Invalid system name for Turnout: System name must start with \"" + l.getSystemNamePrefix() + "\".");
            throw ex;
        }
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));

        Turnout t2 = l.newTurnout(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    @Test
    public void testMisses() {
        // try to get nonexistant objects
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getTurnout(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get
        Turnout t1 = l.newTurnout(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Turnout t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    public void testTwoNames() {
        Turnout jl212 = l.provideTurnout("JT212");
        Turnout jl211 = l.provideTurnout("JT211");

        Assert.assertNotNull(jl212);
        Assert.assertNotNull(jl211);
        Assert.assertTrue(jl212 != jl211);
    }

    @Test
    public void testDefaultNotInternal() {
        Turnout lut = l.provideTurnout("211");

        Assert.assertNotNull(lut);
        Assert.assertEquals("JT211", lut.getSystemName());
    }

    @Test
    public void testProvideUser() {
        Turnout l1 = l.provideTurnout("211");
        l1.setUserName("user 1");
        Turnout l2 = l.provideTurnout("user 1");
        Turnout l3 = l.getTurnout("user 1");

        Assert.assertNotNull(l1);
        Assert.assertNotNull(l2);
        Assert.assertNotNull(l3);
        Assert.assertEquals(l1, l2);
        Assert.assertEquals(l3, l2);
        Assert.assertEquals(l1, l3);

        Turnout l4 = l.getTurnout("JLuser 1");
        Assert.assertNull(l4);
    }

    @Test
    public void testInstanceManagerIntegration() {
        jmri.util.JUnitUtil.resetInstanceManager();
        Assert.assertNotNull(InstanceManager.getDefault(TurnoutManager.class));

        jmri.util.JUnitUtil.initInternalTurnoutManager();

        Assert.assertTrue(InstanceManager.getDefault(TurnoutManager.class) instanceof ProxyTurnoutManager);

        Assert.assertNotNull(InstanceManager.getDefault(TurnoutManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IS1"));

        InternalTurnoutManager m = new InternalTurnoutManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setTurnoutManager(m);

        Assert.assertNotNull(InstanceManager.getDefault(TurnoutManager.class).provideTurnout("JS1"));
        Assert.assertNotNull(InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IS2"));
    }

    /**
     * Number of unit to test. Made a separate method so it can be overridden in
     * subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        l = new InternalTurnoutManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
