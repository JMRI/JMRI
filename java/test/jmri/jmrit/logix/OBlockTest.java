package jmri.jmrit.logix;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Bob Jacobsen Copyright 2010, 2014
 */
public class OBlockTest {

    private OBlockManager blkMgr;

    /* OBlock.DARK replaced with Block.UNDETECTED - 12/10/2016 pwc
    @SuppressWarnings("all") // otherwise, you get "Comparing identical" warning (until something breaks!)
    public void testEqualCoding() {
        // the following match is required by the Javadoc
        Assert.assertTrue("Block.UNKNOWN == OBlock.DARK", Block.UNKNOWN == OBlock.DARK);
    }*/

    @Test
    public void testCTor(){
        assertNotNull(new OBlock("OB01"),"OBlock Creation");
    }
 
    @Test
    public void testCTor2Param(){
        assertNotNull(new OBlock("OB01","test OBlock"),"OBlock Creation");
    }

    @Test
    public void testSeparateCoding() {
        assertTrue( Block.OCCUPIED != OBlock.ALLOCATED, "Block.OCCUPIED != OBlock.ALLOCATED");
        assertTrue( Block.OCCUPIED != OBlock.RUNNING, "Block.OCCUPIED != OBlock.RUNNING");
        assertTrue( Block.OCCUPIED != OBlock.OUT_OF_SERVICE, "Block.OCCUPIED != OBlock.OUT_OF_SERVICE");
        assertTrue( Block.OCCUPIED != OBlock.TRACK_ERROR, "Block.OCCUPIED != OBlock.TRACK_ERROR");
        assertTrue( Block.OCCUPIED != OBlock.UNOCCUPIED, "Block.OCCUPIED != OBlock.UNOCCUPIED");

        assertTrue( Block.UNOCCUPIED != OBlock.ALLOCATED, "Block.UNOCCUPIED != OBlock.ALLOCATED");
        assertTrue( Block.UNOCCUPIED != OBlock.RUNNING, "Block.UNOCCUPIED != OBlock.RUNNING");
        assertTrue( Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE, "Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE");
        assertTrue( Block.UNOCCUPIED != OBlock.TRACK_ERROR, "Block.UNOCCUPIED != OBlock.TRACK_ERROR");

        assertTrue( Block.UNDETECTED != OBlock.ALLOCATED, "Block.UNDETECTED != OBlock.ALLOCATED");
        assertTrue( Block.UNDETECTED != OBlock.RUNNING, "Block.UNDETECTED != OBlock.RUNNING");
        assertTrue( Block.UNDETECTED != OBlock.OUT_OF_SERVICE, "Block.UNDETECTED != OBlock.OUT_OF_SERVICE");
        assertTrue( Block.UNDETECTED != OBlock.TRACK_ERROR, "Block.UNDETECTED != OBlock.TRACK_ERROR");
        assertTrue( Block.UNDETECTED != OBlock.UNOCCUPIED, "Block.UNDETECTED != OBlock.UNOCCUPIED");

        assertTrue( Block.UNKNOWN != OBlock.ALLOCATED, "Block.UNKNOWN != OBlock.ALLOCATED");
        assertTrue( Block.UNKNOWN != OBlock.RUNNING, "Block.UNKNOWN != OBlock.RUNNING");
        assertTrue( Block.UNKNOWN != OBlock.OUT_OF_SERVICE, "Block.UNKNOWN != OBlock.OUT_OF_SERVICE");
        assertTrue( Block.UNKNOWN != OBlock.TRACK_ERROR, "Block.UNKNOWN != OBlock.TRACK_ERROR");
        assertTrue( Block.UNKNOWN != OBlock.UNOCCUPIED, "Block.UNKNOWN != OBlock.UNOCCUPIED");
    }

    @Test
    public void testSetSensor() throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB100", "a");
        assertNotNull(b);
        assertFalse( b.setSensor("foo"), "setSensor");
        JUnitAppender.assertErrorMessage("No sensor named 'foo' exists.");
        assertNull( b.getSensor(), "getSensor");

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor s1 = sensorMgr.newSensor("IS1", "sensor1");
        assertTrue( b.setSensor("sensor1"), "setSensor");
        assertEquals( s1, b.getSensor(), "getSensor");
        assertEquals( OBlock.UNKNOWN, b.getState(), "state unknown");

        assertTrue( b.setSensor("IS1"), "dup setSensor");
        assertEquals( s1, b.getSensor(), "dup getSensor s1");
        assertEquals( OBlock.UNKNOWN, b.getState(), "dup state unknown");

        assertTrue( b.setSensor("  "), "setSensor none");
        assertNull( b.getSensor(), "getSensor none");
        assertEquals( OBlock.UNDETECTED, b.getState(), "none state dark");

        assertTrue( b.isFree(), "Not Free");
        b.setState(b.getState() | OBlock.ALLOCATED|OBlock.RUNNING);
        assertFalse( b.isFree(), "Is Free");
        s1.setState(Sensor.ACTIVE);
        assertTrue( b.setSensor("sensor1"), "setSensor sensor1");
        assertEquals( OBlock.OCCUPIED|OBlock.ALLOCATED|OBlock.RUNNING, b.getState(), "state allocated&running");
    }

    @Test
    public void testSetErrorSensor() throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB101", "b");
        assertNotNull(b);
        assertFalse( b.setErrorSensor("foo"), "setErrorSensor foo");
        JUnitAppender.assertErrorMessage("No sensor named 'foo' exists.");
        assertNull( b.getErrorSensor(), "getErrorSensor foo");

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor se = sensorMgr.newSensor("ISE1", "error1");
        se.setState(Sensor.ACTIVE);
        assertTrue( b.setErrorSensor("error1"), "setErrorSensor only");
        assertEquals( se, b.getErrorSensor(), "getErrorSensor only");
        assertEquals( OBlock.TRACK_ERROR | OBlock.UNDETECTED, b.getState(), "state error only");

        Sensor s1 = sensorMgr.newSensor("IS1", "sensor1");
        s1.setState(Sensor.ACTIVE);
        assertTrue( b.setSensor("IS1"), "setSensor");
        assertEquals( se, b.getErrorSensor(), "getErrorSensor se");
        assertEquals( OBlock.TRACK_ERROR | OBlock.OCCUPIED, b.getState(), "state error");

        assertTrue( b.setErrorSensor("  "), "setErrorSensor");
        assertNull( b.getErrorSensor(), "getErrorSensor none");
        assertEquals( OBlock.OCCUPIED, b.getState(), "state dark");
    }

    @Test
    public void testAllocate() {
        Warrant w1 = new Warrant("IW1", null);
        w1.setTrainName("T1");
        Warrant w2 = new Warrant("IW2", null);
        OBlock b = blkMgr.createNewOBlock("OB102", "c");
        assertNotNull(b);
        assertEquals( OBlock.UNDETECTED, b.getState(), "state is dark");
        assertNull( b.allocate(w1), "Allocate w1");
        assertEquals(
            Bundle.getMessage("AllocatedToWarrant", w1.getDisplayName(), b.getDisplayName(), w1.getTrainName()),
            b.allocate(w2), "Allocate w2");

        assertEquals(
            Bundle.getMessage("PathNotFound", "PathName", b.getDisplayName()),
            b.setPath("PathName", w1), "path not found");
        OPath path1 = new OPath(b, "path1");
        b.addPath(path1);
        assertNull( b.setPath("path1", w1), "path set");
        assertFalse( b.isAllocatedTo(w2), "Allocated to w2");
        assertTrue( b.isAllocatedTo(w1), "Allocated to w1");

        assertTrue( b.deAllocate(w1), "DeAllocate null");
        b.setOutOfService(true);
        assertNull( b.allocate(w2), "Allocate w2");        
        b.setOutOfService(false);
        assertTrue( b.deAllocate(w2), "deAllocate w2");        

        assertEquals(
            Bundle.getMessage("PathNotSet", "path1", b.getDisplayName(), Bundle.getMessage("Warrant")),
            b.setPath("path1", w1), "PathNotSet");
        assertNull( b.allocate(w1), "Allocate w1");
        assertEquals(
            Bundle.getMessage("PathNotSet", "path1", b.getDisplayName(), w1.getDisplayName()),
            b.setPath("path1", w2),"path not set");
    }
    
    @Test
    public void testSensorChanges() throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB103", null);
        assertNotNull(b);
        Warrant w0 = new Warrant("IW0", "war0");
        b.setOutOfService(true);
        assertEquals( OBlock.UNDETECTED|OBlock.OUT_OF_SERVICE, b.getState(),"state OutOfService & dark");

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor s0 = sensorMgr.newSensor("IS0", "sensor0");
        assertTrue( b.setSensor("sensor0"), "setSensor");
        assertEquals( OBlock.UNKNOWN|OBlock.OUT_OF_SERVICE, b.getState(), "state unknown & dark");
        b.setOutOfService(false);
        assertNull( b.allocate(w0), "Allocate w0");
        assertEquals( OBlock.ALLOCATED|OBlock.UNKNOWN, b.getState(), "state allocated & unknown");
        s0.setState(Sensor.ACTIVE);
        assertEquals( OBlock.ALLOCATED|OBlock.OCCUPIED, b.getState(), "state allocated & unknown");
        assertTrue( b.deAllocate(w0), "DeAllocate w0");
        b.setOutOfService(true);
        s0.setState(Sensor.INACTIVE);
        assertEquals( OBlock.OUT_OF_SERVICE|OBlock.UNOCCUPIED, b.getState(), "state allocated & unknown");
        b.setError(false);
        s0.setState(Sensor.INCONSISTENT);
        assertEquals( OBlock.OUT_OF_SERVICE|OBlock.INCONSISTENT, b.getState(), "state  OutOfService & inconsistent");
        assertTrue( b.setSensor(null), "setSensor none");
        assertEquals( OBlock.OUT_OF_SERVICE|OBlock.UNDETECTED, b.getState(), "state  OutOfService & dark");
    }

    @Test
    public void testAddPortal() {
        OBlock b = blkMgr.createNewOBlock("OB0", "");
        assertNotNull(b);
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal p = portalMgr.providePortal("Doop");
        b.addPortal(p);
        assertEquals( 0, b.getPortals().size(), "No portals");

        p.setFromBlock(b, true);
        b.addPortal(p);
        assertEquals( 1, b.getPortals().size(), "One portal");
        p.setToBlock(b, true);
        b.addPortal(p);
        assertEquals( 1, b.getPortals().size(), "One portal only");
        p = portalMgr.providePortal("barp");
        b.addPortal(p);
        p.setToBlock(b, false);
        assertEquals( 2, b.getPortals().size(), "Two portals");

        assertEquals( p, b.getPortalByName("barp"), "Same Portal");
        p = b.getPortalByName("Doop");
        assertNotNull(p, "Get Portal");
        b.removePortal(p);
        assertEquals( 1, b.getPortals().size(), "One portal");

        JUnitAppender.assertWarnMessage("Portal \"Doop\" between OBlocks \"null\" and \"null\" not in block OB0"); 
        JUnitAppender.assertWarnMessage("Portal \"barp\" between OBlocks \"null\"and \"null\" not in block OB0");

    }

    @Test
    public void testAddPath() {
        OBlock b = blkMgr.createNewOBlock("OB1", "");
        assertNotNull(b);
        OPath path1 = new OPath(b, "path1");
        // also test the "add" method checks
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        path1.setToPortal(portalMgr.providePortal("foo"));
        assertFalse( b.addPath(path1), "add path1 to block path1 not in foo");
        portalMgr.providePortal("foo").addPath(path1);
        assertFalse( b.addPath(path1), "add path1 to block b not in foo");
        portalMgr.providePortal("foo").setFromBlock(b, false);
        assertTrue( b.addPath(path1), "add path1 to block");
        assertEquals( 1, b.getPaths().size(), "One path");
        OBlock bb = blkMgr.createNewOBlock("OB2", "");
        OPath path2 = new OPath(bb, "path2");
        assertFalse( b.addPath(path2), "path2 not in block");
        assertEquals( 1, b.getPaths().size(), "path2 not in block");
        JUnitAppender.assertWarnMessage("Path \"path2\" already in block OB2, cannot be added to block OB1");

        assertFalse( b.addPath(path1), "path1 already in block");
        assertEquals( 1, b.getPaths().size(), "path1 already in block");
        OPath path11 = new OPath(b, "path1");
        assertFalse( b.addPath(path11), "path with name \"path1\" already in block");
        assertEquals(1, b.getPaths().size(), "path with name \"path1\" already in block");

        path2 = new OPath("path2", b, portalMgr.providePortal("bar"), null, null);
        portalMgr.providePortal("bar").addPath(path2);
        portalMgr.providePortal("bar").setToBlock(b, false);
        assertTrue(b.addPath(path2), "path2 in block");
        assertEquals( path1, b.getPathByName("path1"), "get \"path1\"");

        b.removeOPath(path1);
        b.removeOPath(path2);
        assertEquals( 0, b.getPaths().size(), "no paths");

    }

    @Test
    public void testAddUserName() {
        OBlock b = blkMgr.provideOBlock("OB99");
        assertNotNull( b, "Block OB99 is null");
        b.setUserName("99user");
        b = blkMgr.getBySystemName("OB99");
        assertNotNull(b);
        assertEquals( "99user", b.getUserName(), "UserName not kept");
    }

    @Test
    public void testGetLocalStatusName() {
        assertEquals( Bundle.getMessage("unoccupied"), OBlock.getLocalStatusName("unoccupied"));
        assertEquals( Bundle.getMessage("occupied"), OBlock.getLocalStatusName("occupied"));
        assertEquals( Bundle.getMessage("allocated"), OBlock.getLocalStatusName("allocated"));
        assertEquals( Bundle.getMessage("running"), OBlock.getLocalStatusName("running"));
        assertEquals( Bundle.getMessage("outOfService"), OBlock.getLocalStatusName("outOfService"));
        assertEquals( Bundle.getMessage("dark"), OBlock.getLocalStatusName("dark"));
        assertEquals( Bundle.getMessage("powerError"), OBlock.getLocalStatusName("powerError"));
    }

    @Test
    public void testGetSystemStatusName() {
        assertEquals("unoccupied", OBlock.getSystemStatusName(Bundle.getMessage("unoccupied")));
        assertEquals("occupied", OBlock.getSystemStatusName(Bundle.getMessage("occupied")));
        assertEquals("allocated", OBlock.getSystemStatusName(Bundle.getMessage("allocated")));
        assertEquals("running", OBlock.getSystemStatusName(Bundle.getMessage("running")));
        assertEquals("outOfService", OBlock.getSystemStatusName(Bundle.getMessage("outOfService")));
        assertEquals("dark", OBlock.getSystemStatusName(Bundle.getMessage("dark")));
        assertEquals("powerError", OBlock.getSystemStatusName(Bundle.getMessage("powerError")));
    }

    @Test
    public void testStatusIs() {

        OBlock oblock = new OBlock("OB99");
        
        oblock.setState(OBlock.UNOCCUPIED);
        assertTrue(oblock.statusIs("unoccupied"));
        oblock.setState(OBlock.OCCUPIED);
        assertTrue(oblock.statusIs("occupied"));
        oblock.setState(OBlock.ALLOCATED);
        assertTrue(oblock.statusIs("allocated"));
        oblock.setState(OBlock.RUNNING);
        assertTrue(oblock.statusIs("running"));
        oblock.setState(OBlock.OUT_OF_SERVICE);
        assertTrue(oblock.statusIs("outOfService"));
        oblock.setState(OBlock.UNDETECTED);
        assertTrue(oblock.statusIs("dark"));
        oblock.setState(OBlock.TRACK_ERROR);
        assertTrue(oblock.statusIs("powerError"));
    }
    
    @Test
    public void testOBlockStatusEnum() {
        assertEquals(OBlock.UNOCCUPIED, OBlock.OBlockStatus.Unoccupied.getStatus());
        assertEquals("unoccupied", OBlock.OBlockStatus.Unoccupied.getName());
        assertEquals(Bundle.getMessage("unoccupied"), OBlock.OBlockStatus.Unoccupied.getDescr());
        
        assertEquals(OBlock.OCCUPIED, OBlock.OBlockStatus.Occupied.getStatus());
        assertEquals("occupied", OBlock.OBlockStatus.Occupied.getName());
        assertEquals(Bundle.getMessage("occupied"), OBlock.OBlockStatus.Occupied.getDescr());
        
        assertEquals(OBlock.ALLOCATED, OBlock.OBlockStatus.Allocated.getStatus());
        assertEquals("allocated", OBlock.OBlockStatus.Allocated.getName());
        assertEquals(Bundle.getMessage("allocated"), OBlock.OBlockStatus.Allocated.getDescr());
        
        assertEquals(OBlock.RUNNING, OBlock.OBlockStatus.Running.getStatus());
        assertEquals("running", OBlock.OBlockStatus.Running.getName());
        assertEquals(Bundle.getMessage("running"), OBlock.OBlockStatus.Running.getDescr());
        
        assertEquals(OBlock.OUT_OF_SERVICE, OBlock.OBlockStatus.OutOfService.getStatus());
        assertEquals("outOfService", OBlock.OBlockStatus.OutOfService.getName());
        assertEquals(Bundle.getMessage("outOfService"), OBlock.OBlockStatus.OutOfService.getDescr());
        
        assertEquals(OBlock.UNDETECTED, OBlock.OBlockStatus.Dark.getStatus());
        assertEquals("dark", OBlock.OBlockStatus.Dark.getName());
        assertEquals(Bundle.getMessage("dark"), OBlock.OBlockStatus.Dark.getDescr());
        
        assertEquals(OBlock.TRACK_ERROR, OBlock.OBlockStatus.TrackError.getStatus());
        assertEquals("powerError", OBlock.OBlockStatus.TrackError.getName());
        assertEquals(Bundle.getMessage("powerError"), OBlock.OBlockStatus.TrackError.getDescr());
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        blkMgr = new OBlockManager();
    }

    @AfterEach
    public void tearDown() {
        if (blkMgr != null ) {
            blkMgr.dispose();
            blkMgr = null;
        }
        JUnitUtil.tearDown();
    }

}
