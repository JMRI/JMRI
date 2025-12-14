package jmri;

import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.managers.TurnoutManagerScaffold;
import jmri.progdebugger.DebugProgrammerManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue( InstanceManager.getDefault(jmri.PowerManager.class) == m, "power manager present");
    }

    @Test
    public void testSecondDefaultPowerManager() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = new PowerManagerScaffold();

        InstanceManager.store(m1, jmri.PowerManager.class);
        InstanceManager.store(m2, jmri.PowerManager.class);

        assertTrue( InstanceManager.getDefault(jmri.PowerManager.class) == m2, "power manager present");
    }

    @Test
    public void testDefaultProgrammerManagers() {
        DebugProgrammerManager m = new DebugProgrammerManager();

        InstanceManager.store(m, AddressedProgrammerManager.class);
        InstanceManager.store(m, GlobalProgrammerManager.class);

        assertTrue( InstanceManager.getDefault(GlobalProgrammerManager.class) == m, "global programmer manager was set");
        assertTrue( InstanceManager.getDefault(AddressedProgrammerManager.class) == m, "addressed programmer manager was set");
    }

    @Test
    public void testSecondDefaultProgrammerManager() {
        DebugProgrammerManager m1 = new DebugProgrammerManager();
        DebugProgrammerManager m2 = new DebugProgrammerManager();

        InstanceManager.store(m1, AddressedProgrammerManager.class);
        InstanceManager.store(m1, GlobalProgrammerManager.class);
        InstanceManager.store(m2, AddressedProgrammerManager.class);
        InstanceManager.store(m2, GlobalProgrammerManager.class);

        assertTrue( InstanceManager.getDefault(GlobalProgrammerManager.class) == m2, "2nd global programmer manager is default");
        assertTrue( InstanceManager.getDefault(AddressedProgrammerManager.class) == m2, "2nd addressed programmer manager is default");
    }

    @Test
    public void testIsInitialized() {
        // counts on the following test class to be loaded
        assertFalse(InstanceManager.isInitialized(jmri.InstanceManagerTest.InstanceManagerInitCheck.class));
        assertFalse(InstanceManager.isInitialized(jmri.InstanceManagerTest.InstanceManagerInitCheck.class));

        assertNotNull(InstanceManager.getDefault(jmri.InstanceManagerTest.InstanceManagerInitCheck.class));

        assertTrue(InstanceManager.isInitialized(jmri.InstanceManagerTest.InstanceManagerInitCheck.class));
    }

    static public class InstanceManagerInitCheck implements jmri.InstanceManagerAutoDefault {
        public InstanceManagerInitCheck() {}
    }

    // the following test was moved from jmri.jmrit.symbolicprog.PackageTet when
    // it was converted to JUnit4 format.  It seemed out of place there.
    // check configuring the programmer
    @Test
    public void testConfigProgrammer() {
        // initialize the system
        Programmer p = new jmri.progdebugger.ProgDebugger();
        InstanceManager.store(new jmri.managers.DefaultProgrammerManager(p), GlobalProgrammerManager.class);
        assertEquals(p.getConfigurator(),InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer().getConfigurator());
    }

    // Testing new load store
    @Test
    public void testGenericStoreAndGet() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2;

        InstanceManager.store(m1, PowerManager.class);
        m2 = InstanceManager.getDefault(PowerManager.class);

        assertEquals( m1, m2, "retrieved same object");
    }

    @Test
    public void testGenericStoreList() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = new PowerManagerScaffold();

        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.store(m2, PowerManager.class);

        assertEquals( 2, InstanceManager.getList(PowerManager.class).size(),
            "list length");
        assertEquals( m1, InstanceManager.getList(PowerManager.class).get(0),
            "retrieved 1st PowerManager");
        assertEquals( m2, InstanceManager.getList(PowerManager.class).get(1),
            "retrieved 2nd PowerManager");

        assertTrue( InstanceManager.getList(PowerManager.class).equals(
            InstanceManager.getList("jmri.PowerManager")),
            "access by string");
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

        assertEquals( m1, m2, "retrieved same PowerManager");
        assertEquals( t1, t2, "retrieved same TurnoutManager");

        assertEquals(
                InstanceManager.getDefault(PowerManager.class),
                InstanceManager.getDefault("jmri.PowerManager"),
                "access by string"
            );

    }

    @Test
    public void testListTwoDifferentTypes() {
        PowerManager m1 = new PowerManagerScaffold();
        TurnoutManager t1 = new TurnoutManagerScaffold();

        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.store(t1, TurnoutManager.class);

        var set = InstanceManager.getInstanceClasses();

        assertTrue( set.contains(PowerManager.class), "PowerManager");
        assertTrue( set.contains(TurnoutManager.class), "TurnoutManager");
    }

    @Test
    public void testGetInstance() throws ClassNotFoundException {
        // for sync usage, check a predicate - Class.forName returns same object always
        assertTrue( Class.forName("jmri.PowerManager") == Class.forName("jmri.PowerManager"),
            "access by string");
        // the rest of the checks are done via calls to getInstance
        // embedded in various other tests

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

        assertEquals( m1, m2, "retrieved second PowerManager");
    }

    public static class OkAutoCreate implements InstanceManagerAutoDefault {

        public OkAutoCreate() {
        }
    }

    @Test
    public void testAutoCreateOK() {

        OkAutoCreate obj1 = InstanceManager.getDefault(OkAutoCreate.class);
        assertNotNull( obj1, "Created object 1");
        OkAutoCreate obj2 = InstanceManager.getDefault(OkAutoCreate.class);
        assertNotNull( obj2, "Created object 2");
        assertTrue( obj1 == obj2, "same object");
    }

    public static class NoAutoCreate {
    }

    @Test
    public void testAutoCreateNotOK() {
        Exception ex = assertThrows(NullPointerException.class, () ->
            InstanceManager.getDefault(NoAutoCreate.class) );
        assertEquals("Required nonnull default for jmri.InstanceManagerTest$NoAutoCreate does not exist.",
            ex.getMessage());
    }

    static volatile boolean avoidLoopAutoCreateCycle = true;

    static synchronized void setavoidLoopAutoCreateCycle( boolean newVal) {
        avoidLoopAutoCreateCycle = newVal;
    }

    public static class AutoCreateCycle implements InstanceManagerAutoDefault {

        public AutoCreateCycle() {
            if (InstanceManagerTest.avoidLoopAutoCreateCycle) {
                InstanceManagerTest.setavoidLoopAutoCreateCycle(false);
                InstanceManager.getDefault(AutoCreateCycle.class);
            }
        }
    }

    @Test
    public void testAutoCreateCycle() {
        InstanceManagerTest.setavoidLoopAutoCreateCycle(true);
        InstanceManager.getDefault(AutoCreateCycle.class);
        JUnitAppender.assertErrorMessage("Proceeding to initialize class jmri.InstanceManagerTest$AutoCreateCycle while already in initialization");
        JUnitAppender.assertErrorMessage("    Prior initialization:");
    }

    public static class OkToDispose implements Disposable {

        public static final String MESSAGE = "dispose called";
        private static int times = 0;

        private static void startUp() {
            times = 0;
        }

        private static void increaseDisposedCount() {
            OkToDispose.times++;
        }

        @Override
        public void dispose() {
            increaseDisposedCount();
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
        assertNotNull(InstanceManager.getDefault(SensorManager.class));
        assertNotNull(InstanceManager.getDefault(TurnoutManager.class));
        assertNotNull(InstanceManager.getDefault(LightManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.SignalHeadManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.SignalSystemManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.SignalGroupManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
        assertNotNull(InstanceManager.getDefault(WarrantManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.TransitManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.RouteManager.class));
        assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.ConditionalManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.LogixManager.class));
        assertNotNull(InstanceManager.getDefault(Timebase.class));
        assertNotNull(InstanceManager.getDefault(jmri.ClockControl.class));
        assertNotNull(InstanceManager.getDefault(jmri.SignalGroupManager.class));
        assertNotNull(InstanceManager.getDefault(jmri.ReporterManager.class));
        assertNotNull(InstanceManager.getDefault(CatalogTreeManager.class));
        assertNotNull(InstanceManager.getDefault(MemoryManager.class));
        assertNotNull(InstanceManager.getDefault(AudioManager.class));
        assertNotNull(InstanceManager.getDefault(RosterIconFactory.class));
        assertNotNull(InstanceManager.getDefault(jmri.time.TimeProviderManager.class));
    }

    //
    // Tests of individual types, to make sure they
    // properly create defaults
    //
    @Test
    public void testLayoutBlockManager() {
        LayoutBlockManager obj = InstanceManager.getDefault(LayoutBlockManager.class);
        assertNotNull(obj);
        assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
        assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
        assertEquals(obj, InstanceManager.getDefault(LayoutBlockManager.class));
    }

    @Test
    public void testWarrantManager() {
        WarrantManager obj = InstanceManager.getDefault(WarrantManager.class);
        assertNotNull(obj);
        assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
        assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
        assertEquals(obj, InstanceManager.getDefault(WarrantManager.class));
    }

    @Test
    public void testOBlockManager() {
        OBlockManager obj = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        assertNotNull(obj);
        assertEquals(obj, InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
        assertEquals(obj, InstanceManager.getDefault(OBlockManager.class));
        assertEquals(obj, InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class));
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
        assertFalse(InstanceManager.getList(PowerManager.class).isEmpty());
        assertFalse(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clearAll();
        // should contain only empty lists
        assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        assertTrue(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
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
        assertFalse(InstanceManager.getList(PowerManager.class).isEmpty());
        assertFalse(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clear(PowerManager.class);
        // should contain one list and calls for other lists should be empty
        assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        assertFalse(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clear(NoAutoCreate.class);
        // should contain only empty lists
        assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        assertTrue(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
        InstanceManager.getDefault().clear(OkAutoCreate.class);
        // verify that no exception was thrown
        assertTrue(InstanceManager.getList(PowerManager.class).isEmpty());
        assertTrue(InstanceManager.getList(NoAutoCreate.class).isEmpty());
        assertTrue(InstanceManager.getList(OkAutoCreate.class).isEmpty());
    }

    @Test
    public void testContainsDefault() {
        // verify not OkAutoCreate instances exist
        InstanceManager.reset(OkAutoCreate.class);
        assertFalse( InstanceManager.containsDefault(OkAutoCreate.class), "Should be empty");
        // create a OkAutoCreate instance
        assertNotNull(InstanceManager.getDefault(OkAutoCreate.class));
        assertTrue( InstanceManager.containsDefault(OkAutoCreate.class), "Should not be empty");
        // remote OkAutoCreate instance
        InstanceManager.reset(OkAutoCreate.class);
        assertFalse( InstanceManager.containsDefault(OkAutoCreate.class), "Should be empty");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        OkToDispose.startUp();
        InstanceManager.getDefault().clearAll();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstanceManagerTest.class);
}
