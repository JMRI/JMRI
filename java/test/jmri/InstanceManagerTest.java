package jmri;

import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.managers.TurnoutManagerScaffold;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test InstanceManager
 *
 * @author	Bob Jacobsen
 */
public class InstanceManagerTest extends TestCase implements InstanceManagerAutoDefault {

    public void testDefaultPowerManager() {
        PowerManager m = new PowerManagerScaffold();

        InstanceManager.store(m, jmri.PowerManager.class);

        Assert.assertTrue("power manager present", InstanceManager.powerManagerInstance() == m);
    }

    public void testSecondDefaultPowerManager() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = new PowerManagerScaffold();

        InstanceManager.store(m1, jmri.PowerManager.class);
        InstanceManager.store(m2, jmri.PowerManager.class);

        Assert.assertTrue("power manager present", InstanceManager.powerManagerInstance() == m2);
    }

    public void testDefaultProgrammerManagers() {
        ProgrammerManager m = new jmri.progdebugger.DebugProgrammerManager();

        InstanceManager.setProgrammerManager(m);

        Assert.assertTrue("global programmer manager was set", InstanceManager.getDefault(GlobalProgrammerManager.class) == m);
        Assert.assertTrue("addressed programmer manager was set", InstanceManager.getDefault(AddressedProgrammerManager.class) == m);
    }

    public void testSecondDefaultProgrammerManager() {
        ProgrammerManager m1 = new jmri.progdebugger.DebugProgrammerManager();
        ProgrammerManager m2 = new jmri.progdebugger.DebugProgrammerManager();

        InstanceManager.setProgrammerManager(m1);
        InstanceManager.setProgrammerManager(m2);

        Assert.assertTrue("2nd global programmer manager is default", InstanceManager.getDefault(GlobalProgrammerManager.class) == m2);
        Assert.assertTrue("2nd addressed programmer manager is default", InstanceManager.getDefault(AddressedProgrammerManager.class) == m2);
    }

    // Testing new load store
    public void testGenericStoreAndGet() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = null;

        InstanceManager.store(m1, PowerManager.class);
        m2 = InstanceManager.getDefault(PowerManager.class);

        Assert.assertEquals("retrieved same object", m1, m2);
    }

    public void testGenericStoreList() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = new PowerManagerScaffold();

        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.store(m2, PowerManager.class);

        Assert.assertEquals("list length", 2,
                InstanceManager.getList(PowerManager.class).size());
        Assert.assertEquals("retrieved 1st PowerManager", m1,
                InstanceManager.getList(PowerManager.class).get(0));
        Assert.assertEquals("retrieved 2nd PowerManager", m2,
                InstanceManager.getList(PowerManager.class).get(1));
    }

    public void testGenericStoreAndGetTwoDifferentTypes() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = null;
        TurnoutManager t1 = new TurnoutManagerScaffold();
        TurnoutManager t2;

        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.store(t1, TurnoutManager.class);
        m2 = InstanceManager.getDefault(PowerManager.class);
        t2 = InstanceManager.getDefault(TurnoutManager.class);

        Assert.assertEquals("retrieved same PowerManager", m1, m2);
        Assert.assertEquals("retrieved same TurnoutManager", t1, t2);
    }

    public void testGenericStoreAndReset() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = null;

        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.reset(PowerManager.class);
        m1 = new PowerManagerScaffold();
        InstanceManager.store(m1, PowerManager.class);

        m2 = InstanceManager.getDefault(PowerManager.class);

        Assert.assertEquals("retrieved second PowerManager", m1, m2);
    }

    public static class OkAutoCreate implements InstanceManagerAutoDefault {

        public OkAutoCreate() {
            System.out.println();
        }
    }

    public void testAutoCreateOK() {

        OkAutoCreate obj1 = InstanceManager.getDefault(OkAutoCreate.class);
        Assert.assertNotNull("Created object 1", obj1);
        OkAutoCreate obj2 = InstanceManager.getDefault(OkAutoCreate.class);
        Assert.assertNotNull("Created object 2", obj2);
        Assert.assertTrue("same object", obj1 == obj2);
    }

    public class NoAutoCreate {
    }

    public void testAutoCreateNotOK() {
        NoAutoCreate obj = InstanceManager.getDefault(NoAutoCreate.class);
        Assert.assertNull(obj);
        jmri.util.JUnitAppender.assertWarnMessage("getDefault found no default object for type \"jmri.InstanceManagerTest$NoAutoCreate\""); 
    }

    /**
     * Test of types that have defaults, even with no system attached.
     */
    public void testAllDefaults() {
        Assert.assertNotNull(InstanceManager.sensorManagerInstance());
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance());
        Assert.assertNotNull(InstanceManager.lightManagerInstance());
        Assert.assertNotNull(InstanceManager.signalHeadManagerInstance());
        Assert.assertNotNull(InstanceManager.signalMastManagerInstance());
        Assert.assertNotNull(InstanceManager.signalSystemManagerInstance());
        Assert.assertNotNull(InstanceManager.signalGroupManagerInstance());
        Assert.assertNotNull(InstanceManager.blockManagerInstance());
        Assert.assertNotNull(InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(WarrantManager.class));
        Assert.assertNotNull(InstanceManager.sectionManagerInstance());
        Assert.assertNotNull(InstanceManager.transitManagerInstance());
        Assert.assertNotNull(InstanceManager.routeManagerInstance());
        Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class));
        Assert.assertNotNull(InstanceManager.conditionalManagerInstance());
        Assert.assertNotNull(InstanceManager.logixManagerInstance());
        Assert.assertNotNull(InstanceManager.timebaseInstance());
        Assert.assertNotNull(InstanceManager.clockControlInstance());
        Assert.assertNotNull(InstanceManager.signalGroupManagerInstance());
        Assert.assertNotNull(InstanceManager.reporterManagerInstance());
        Assert.assertNotNull(InstanceManager.getDefault(CatalogTreeManager.class));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance());
        Assert.assertNotNull(InstanceManager.getDefault(AudioManager.class));
        Assert.assertNotNull(InstanceManager.rosterIconFactoryInstance());
    }

    //
    // Tests of individual types, to make sure they
    // properly create defaults
    //
    public void testLayoutBlockManager() {
        LayoutBlockManager obj = InstanceManager.getDefault(LayoutBlockManager.class);
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
    }

    public void testWarrantManager() {
        WarrantManager obj = InstanceManager.getDefault(WarrantManager.class);
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
    }

    public void testOBlockManager() {
        OBlockManager obj = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj, InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(OBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
    }

    // from here down is testing infrastructure
    public InstanceManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {InstanceManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(InstanceManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
