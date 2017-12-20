package jmri;

import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.managers.TurnoutManagerScaffold;
import jmri.progdebugger.DebugProgrammerManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test InstanceManager
 *
 * @author Bob Jacobsen
 */
public class InstanceManagerTest {

    @Test
    public void testDefaultPowerManager() {
        PowerManager m = new PowerManagerScaffold();

        InstanceManager.store(m, jmri.PowerManager.class);

        Assert.assertTrue("power manager present", InstanceManager.getDefault(jmri.PowerManager.class) == m);
    }

    @Test
    public void testSecondDefaultPowerManager() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = new PowerManagerScaffold();

        InstanceManager.store(m1, jmri.PowerManager.class);
        InstanceManager.store(m2, jmri.PowerManager.class);

        Assert.assertTrue("power manager present", InstanceManager.getDefault(jmri.PowerManager.class) == m2);
    }

    @Test
    public void testDefaultProgrammerManagers() {
        DebugProgrammerManager m = new DebugProgrammerManager();

        InstanceManager.setAddressedProgrammerManager(m);
        InstanceManager.store(m, GlobalProgrammerManager.class);

        Assert.assertTrue("global programmer manager was set", InstanceManager.getDefault(GlobalProgrammerManager.class) == m);
        Assert.assertTrue("addressed programmer manager was set", InstanceManager.getDefault(AddressedProgrammerManager.class) == m);
    }

    @Test
    public void testSecondDefaultProgrammerManager() {
        DebugProgrammerManager m1 = new DebugProgrammerManager();
        DebugProgrammerManager m2 = new DebugProgrammerManager();

        InstanceManager.setAddressedProgrammerManager(m1);
        InstanceManager.store(m1, GlobalProgrammerManager.class);
        InstanceManager.setAddressedProgrammerManager(m2);
        InstanceManager.store(m2, GlobalProgrammerManager.class);

        Assert.assertTrue("2nd global programmer manager is default", InstanceManager.getDefault(GlobalProgrammerManager.class) == m2);
        Assert.assertTrue("2nd addressed programmer manager is default", InstanceManager.getDefault(AddressedProgrammerManager.class) == m2);
    }

    // the following test was moved from jmri.jmrit.symbolicprog.PackageTet when
    // it was converted to JUnit4 format.  It seemed out of place there.
    // check configuring the programmer
    @Test
    public void testConfigProgrammer() {
        // initialize the system
        Programmer p = new jmri.progdebugger.ProgDebugger();
        InstanceManager.store(new jmri.managers.DefaultProgrammerManager(p), GlobalProgrammerManager.class);
        Assert.assertEquals(p, InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer());
    }

    // Testing new load store
    @Test
    public void testGenericStoreAndGet() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2;

        InstanceManager.store(m1, PowerManager.class);
        m2 = InstanceManager.getDefault(PowerManager.class);

        Assert.assertEquals("retrieved same object", m1, m2);
    }

    @Test
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

    @Test
    public void testGenericStoreAndGetTwoDifferentTypes() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2;
        TurnoutManager t1 = new TurnoutManagerScaffold();
        TurnoutManager t2;

        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.store(t1, TurnoutManager.class);
        m2 = InstanceManager.getDefault(PowerManager.class);
        t2 = InstanceManager.getDefault(TurnoutManager.class);

        Assert.assertEquals("retrieved same PowerManager", m1, m2);
        Assert.assertEquals("retrieved same TurnoutManager", t1, t2);
    }

    @Test
    public void testGenericStoreAndReset() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2;

        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.reset(PowerManager.class);
        m1 = new PowerManagerScaffold();
        InstanceManager.store(m1, PowerManager.class);

        m2 = InstanceManager.getDefault(PowerManager.class);

        Assert.assertEquals("retrieved second PowerManager", m1, m2);
    }

    public static class OkAutoCreate implements InstanceManagerAutoDefault {

        public OkAutoCreate() {
        }
    }

    @Test
    public void testAutoCreateOK() {

        OkAutoCreate obj1 = InstanceManager.getDefault(OkAutoCreate.class);
        Assert.assertNotNull("Created object 1", obj1);
        OkAutoCreate obj2 = InstanceManager.getDefault(OkAutoCreate.class);
        Assert.assertNotNull("Created object 2", obj2);
        Assert.assertTrue("same object", obj1 == obj2);
    }

    public static class NoAutoCreate {
    }

    @Test
    public void testAutoCreateNotOK() {
        try {
            InstanceManager.getDefault(NoAutoCreate.class);
            Assert.fail("Expected NullPointerException not thrown");
        } catch (NullPointerException ex) {
            // passes
        }
    }

    static boolean avoidLoopAutoCreateCycle = true;

    public static class AutoCreateCycle implements InstanceManagerAutoDefault {

        public AutoCreateCycle() {
            if (avoidLoopAutoCreateCycle) {
                avoidLoopAutoCreateCycle = false;
                InstanceManager.getDefault(AutoCreateCycle.class);
            }
        }
    }

    @Test
    public void testAutoCreateCycle() {
        avoidLoopAutoCreateCycle = true;
        InstanceManager.getDefault(AutoCreateCycle.class);
        JUnitAppender.assertErrorMessage("Proceeding to initialize class jmri.InstanceManagerTest$AutoCreateCycle while already in initialization");
        JUnitAppender.assertErrorMessage("    Prior initialization:");
    }

    public static class OkToDispose implements Disposable {

        public static final String MESSAGE = "dispose called";
        private static int times = 0;

        private static void setUp() {
            times = 0;
        }

        @Override
        public void dispose() {
            times++;
            log.warn("{} {}", MESSAGE, times);
        }
    }

    @Test
    public void testDisposable() {
        OkToDispose d1 = new OkToDispose();

        // register d1 in single list
        InstanceManager.store(d1, OkToDispose.class);
        InstanceManager.deregister(d1, OkToDispose.class);
        // dispose should have been called since registered in only one list
        JUnitAppender.assertWarnMessage(OkToDispose.MESSAGE + 1);
        // register d1 in two lists
        InstanceManager.store(d1, OkToDispose.class);
        InstanceManager.store(d1, Disposable.class);
        InstanceManager.deregister(d1, OkToDispose.class);
        // dispose should not have been called because removed from only one list
        InstanceManager.deregister(d1, Disposable.class);
        // dispose should be called again as removed from all lists
        JUnitAppender.assertWarnMessage(OkToDispose.MESSAGE + 2);
    }

    @Test
    public void testDisposeInClear() {
        OkToDispose d1 = new OkToDispose();

        // register d1 in single list
        InstanceManager.store(d1, OkToDispose.class);
        InstanceManager.getDefault().clear(OkToDispose.class);
        // dispose should have been called since registered in only one list
        JUnitAppender.assertWarnMessage(OkToDispose.MESSAGE + 1);
        // register d1 in two lists
        InstanceManager.store(d1, OkToDispose.class);
        InstanceManager.store(d1, Disposable.class);
        InstanceManager.getDefault().clear(OkToDispose.class);
        // dispose should not have been called because removed from only one list
        InstanceManager.getDefault().clear(Disposable.class);
        // dispose should be called again as removed from all lists
        JUnitAppender.assertWarnMessage(OkToDispose.MESSAGE + 2);

    }

    /**
     * Test of types that have defaults, even with no system attached.
     */
    @Test
    public void testAllDefaults() {
        Assert.assertNotNull(InstanceManager.getDefault(SensorManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(TurnoutManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(LightManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalHeadManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalSystemManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalGroupManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(WarrantManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.TransitManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.RouteManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.ConditionalManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.LogixManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(Timebase.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.ClockControl.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalGroupManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.ReporterManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(CatalogTreeManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(MemoryManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(AudioManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(RosterIconFactory.class));
    }

    //
    // Tests of individual types, to make sure they
    // properly create defaults
    //
    @Test
    public void testLayoutBlockManager() {
        LayoutBlockManager obj = InstanceManager.getDefault(LayoutBlockManager.class);
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
    }

    @Test
    public void testWarrantManager() {
        WarrantManager obj = InstanceManager.getDefault(WarrantManager.class);
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
    }

    @Test
    public void testOBlockManager() {
        OBlockManager obj = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj, InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(OBlockManager.class));
        Assert.assertEquals(obj, InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
    }

    @Test
    public void testClearAll() {
        PowerManager pm1 = new PowerManagerScaffold();
        PowerManager pm2 = new PowerManagerScaffold();
        NoAutoCreate nac1 = new NoAutoCreate();
        InstanceManager.store(pm1, PowerManager.class);
        InstanceManager.store(pm2, PowerManager.class);
        InstanceManager.store(nac1, NoAutoCreate.class);
        // should contain two lists and calls for other lists should be empty
        Assert.assertFalse(InstanceManager.getList(PowerManager.class).isEmpty());
        Assert.assertFalse(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clearAll();
        // should contain only empty lists
        Assert.assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
    }

    @Test
    public void testClear() {
        PowerManager pm1 = new PowerManagerScaffold();
        PowerManager pm2 = new PowerManagerScaffold();
        NoAutoCreate nac1 = new NoAutoCreate();
        InstanceManager.store(pm1, PowerManager.class);
        InstanceManager.store(pm2, PowerManager.class);
        InstanceManager.store(nac1, NoAutoCreate.class);
        // should contain two lists and calls for other lists should be empty
        Assert.assertFalse(InstanceManager.getList(PowerManager.class).isEmpty());
        Assert.assertFalse(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clear(PowerManager.class);
        // should contain one list and calls for other lists should be empty
        Assert.assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        Assert.assertFalse(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clear(NoAutoCreate.class);
        // should contain only empty lists
        Assert.assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clear(OkAutoCreate.class);
        // verify that no exception was thrown
        Assert.assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        Assert.assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
    }

    @Test
    public void testContainsDefault() {
        // verify not OkAutoCreate instances exist
        InstanceManager.reset(OkAutoCreate.class);
        Assert.assertFalse("Should be empty", InstanceManager.containsDefault(OkAutoCreate.class));
        // create a OkAutoCreate instance
        Assert.assertNotNull(InstanceManager.getDefault(OkAutoCreate.class));
        Assert.assertTrue("Should not be empty", InstanceManager.containsDefault(OkAutoCreate.class));
        // remote OkAutoCreate instance
        InstanceManager.reset(OkAutoCreate.class);
        Assert.assertFalse("Should be empty", InstanceManager.containsDefault(OkAutoCreate.class));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        OkToDispose.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(InstanceManagerTest.class);
}
