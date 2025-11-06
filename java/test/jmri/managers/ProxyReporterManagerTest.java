package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

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
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getByUserName("mine"), "user name correct ");
        assertSame( t, l.getBySystemName(getSystemName("sysName")), "system name correct ");
    }

    @Test
    public void testTwoNames() {
        Reporter ir211 = l.provideReporter("LR211");
        Reporter lr211 = l.provideReporter("IR211");

        assertNotNull(ir211);
        assertNotNull(lr211);
        assertNotSame(ir211, lr211);
    }

    @Test
    public void testDefaultNotInternal() {
        Reporter lut = l.provideReporter("211");

        assertNotNull(lut);
        assertEquals("IR211", lut.getSystemName());
    }

    @Test
    public void testProvideUser() {
        Reporter l1 = l.provideReporter("211");
        l1.setUserName("user 1");
        Reporter l2 = l.provideReporter("user 1");
        Reporter l3 = l.getReporter("user 1");

        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);
        assertEquals(l1, l2);
        assertEquals(l3, l2);
        assertEquals(l1, l3);

        Reporter l4 = l.getReporter("JLuser 1");
        assertNull(l4);
    }

    @Test
    public void testInstanceManagerIntegration() {
        JUnitUtil.resetInstanceManager();
        assertNotNull(InstanceManager.getDefault(ReporterManager.class));

        JUnitUtil.initReporterManager();

        assertInstanceOf( ProxyReporterManager.class, InstanceManager.getDefault(ReporterManager.class));

        assertNotNull(InstanceManager.getDefault(ReporterManager.class));
        assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1"));

        ReporterManager m = new jmri.jmrix.internal.InternalReporterManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setReporterManager(m);

        assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("JR1"));
        assertNotNull(InstanceManager.getDefault(ReporterManager.class).provideReporter("IR2"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        ReporterManager irman = InstanceManager.getDefault(ReporterManager.class);
        assertInstanceOf( ProxyReporterManager.class, irman,
            "ReporterManager is not a ProxyReporterManager");
        l = (ProxyReporterManager) irman;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
