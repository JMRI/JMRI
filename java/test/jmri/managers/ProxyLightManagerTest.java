package jmri.managers;

import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the ProxyLightManager
 *
 * @author	Bob Jacobsen 2003, 2006, 2008
 * @version	$Revision$
 */
public class ProxyLightManagerTest extends TestCase {

    public String getSystemName(int i) {
        return "JL" + i;
    }

    protected LightManager l = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    public void testLightPutGet() {
        // create
        Light t = l.newLight(getSystemName(getNumToTest1()), "mine");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    public void testDefaultSystemName() {
        // create
        Light t = l.provideLight("" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    public void testProvideFailure() {
        boolean correct = false;
        try {
            Light t = l.provideLight("");
            Assert.fail("didn't throw");
        } catch (IllegalArgumentException ex) {
            correct = true;
        }
        Assert.assertTrue("Exception thrown properly", correct);
        
    }

    public void testSingleObject() {
        // test that you always get the same representation
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));

        Light t2 = l.newLight(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    public void testMisses() {
        // try to get nonexistant lights
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    public void testUpperLower() {
        Light t = l.provideLight("" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
    }

    public void testRename() {
        // get light
        Light t1 = l.newLight(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Light t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    public void testTwoNames() {
        Light il211 = l.provideLight("IL211");
        Light jl211 = l.provideLight("JL211");

        Assert.assertNotNull(il211);
        Assert.assertNotNull(jl211);
        Assert.assertTrue(il211 != jl211);
    }

    public void testDefaultNotInternal() {
        Light lut = l.provideLight("211");

        Assert.assertNotNull(lut);
        Assert.assertEquals("JL211", lut.getSystemName());
    }

    public void testProvideUser() {
        Light l1 = l.provideLight("211");
        l1.setUserName("user 1");
        Light l2 = l.provideLight("user 1");
        Light l3 = l.getLight("user 1");

        Assert.assertNotNull(l1);
        Assert.assertNotNull(l2);
        Assert.assertNotNull(l3);
        Assert.assertEquals(l1, l2);
        Assert.assertEquals(l3, l2);
        Assert.assertEquals(l1, l3);

        Light l4 = l.getLight("JLuser 1");
        Assert.assertNull(l4);
    }

    public void testInstanceManagerIntegration() {
        jmri.util.JUnitUtil.resetInstanceManager();
        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class));

        jmri.util.JUnitUtil.initInternalLightManager();

        Assert.assertTrue(InstanceManager.getDefault(LightManager.class) instanceof ProxyLightManager);

        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("IL1"));

        InternalLightManager m = new InternalLightManager() {

            public String getSystemPrefix() {
                return "J";
            }
        };
        InstanceManager.setLightManager(m);

        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("JL1"));
        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class).provideLight("IL2"));
    }

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

    // from here down is testing infrastructure
    public ProxyLightManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ProxyLightManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProxyLightManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create and register the manager object
        l = new InternalLightManager() {
            public String getSystemPrefix() {
                return "J";
            }
        };
        jmri.InstanceManager.setLightManager(l);
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
