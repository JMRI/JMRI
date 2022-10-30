package jmri.managers;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test the ProxyReporterManager
 *
 * @author Bob Jacobsen 2003, 2006, 2008
 * @author Mark Underwood 2012
 * @author Paul Bender 2016
 */
public class ProxyReporterManagerTest extends AbstractProxyManagerTestBase<ProxyReporterManager, Reporter> {

    public String getSystemName(String i) {
        return "IR" + i;
    }

    @Test
    public void testReporterPutGet() {
        // create
        Reporter t = l.newReporter(getSystemName("sysName"), "mine");
        // check
        Assert.assertNotNull("real object returned ", t );
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName("sysName")));
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
        JUnitUtil.resetInstanceManager();
        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class));

        JUnitUtil.initReporterManager();

        Assert.assertTrue(InstanceManager.getDefault(ReporterManager.class) instanceof ProxyReporterManager);

        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1"));

        ReporterManager m = new jmri.jmrix.internal.InternalReporterManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setReporterManager(m);

        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("JR1"));
        Assert.assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("IR2"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        ReporterManager irman = InstanceManager.getDefault(ReporterManager.class);
        if ( irman instanceof ProxyReporterManager ) {
            l = (ProxyReporterManager) irman;
        } else {
            Assertions.fail("ReporterManager is not a ProxyReporterManager");
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
