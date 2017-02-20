package jmri.managers;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the ProxyReporterManager
 *
 * @author	Bob Jacobsen 2003, 2006, 2008
 * @author	Mark Underwood 2012
 * @author	Paul Bender 2016
 */
public class ProxyReporterManagerTest extends AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "IR" + i;
    }

    @Test
    public void testReporterPutGet() {
        // create
        Reporter t = l.newReporter(getSystemName(getNumToTest1()), "mine");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testTwoNames() {
        Reporter ir211 = l.provideReporter("LR211");
        Reporter lr211 = l.provideReporter("IR211");

        Assert.assertNotNull(ir211);
        Assert.assertNotNull(lr211);
        Assert.assertTrue(ir211 != lr211);
    }

    @Test
    public void testDefaultNotInternal() {
        Reporter lut = l.provideReporter("211");

        Assert.assertNotNull(lut);
        Assert.assertEquals("IR211", lut.getSystemName());
    }

    @Test
    public void testProvideUser() {
        Reporter l1 = l.provideReporter("211");
        l1.setUserName("user 1");
        Reporter l2 = l.provideReporter("user 1");
        Reporter l3 = l.getReporter("user 1");

        Assert.assertNotNull(l1);
        Assert.assertNotNull(l2);
        Assert.assertNotNull(l3);
        Assert.assertEquals(l1, l2);
        Assert.assertEquals(l3, l2);
        Assert.assertEquals(l1, l3);

        Reporter l4 = l.getReporter("JLuser 1");
        Assert.assertNull(l4);
    }

    @Test
    public void testInstanceManagerIntegration() {
        jmri.util.JUnitUtil.resetInstanceManager();
        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class));

        jmri.util.JUnitUtil.initReporterManager();

        Assert.assertTrue(InstanceManager.getDefault(ReporterManager.class) instanceof ProxyReporterManager);

        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1"));

        InternalReporterManager m = new InternalReporterManager() {

            @Override
            public String getSystemPrefix() {
                return "J";
            }
        };
        InstanceManager.setReporterManager(m);

        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("JR1"));
        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("IR2"));
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // create and register the manager object
        l = InstanceManager.getDefault(jmri.ReporterManager.class);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
