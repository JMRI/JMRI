package jmri.jmrit.logix;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Bob Jacobsen Copyright 2010, 2014
 */
public class OBlockTest {

    OBlockManager blkMgr;

    /* OBlock.DARK replaced with Block.UNDETECTED - 12/10/2016 pwc
    @SuppressWarnings("all") // otherwise, you get "Comparing identical" warning (until something breaks!)
    public void testEqualCoding() {
        // the following match is required by the Javadoc
        Assert.assertTrue("Block.UNKNOWN == OBlock.DARK", Block.UNKNOWN == OBlock.DARK);
    }*/

    @Test
    public void testCTor(){
       assertThat(new OBlock("OB01")).withFailMessage("OBlock Creation").isNotNull();
    }
 
   @Test
    public void testCTor2Param(){
       assertThat(new OBlock("OB01","test OBlock")).withFailMessage("OBlock Creation").isNotNull();
    }

    @Test
    public void testSeparateCoding() {
        assertThat(Block.OCCUPIED != OBlock.ALLOCATED).withFailMessage("Block.OCCUPIED != OBlock.ALLOCATED").isTrue();
        assertThat(Block.OCCUPIED != OBlock.RUNNING).withFailMessage("Block.OCCUPIED != OBlock.RUNNING").isTrue();
        assertThat(Block.OCCUPIED != OBlock.OUT_OF_SERVICE).withFailMessage("Block.OCCUPIED != OBlock.OUT_OF_SERVICE").isTrue();
        assertThat(Block.OCCUPIED != OBlock.TRACK_ERROR).withFailMessage("Block.OCCUPIED != OBlock.TRACK_ERROR").isTrue();
        assertThat(Block.OCCUPIED != OBlock.UNOCCUPIED).withFailMessage("Block.OCCUPIED != OBlock.UNOCCUPIED").isTrue();

        assertThat(Block.UNOCCUPIED != OBlock.ALLOCATED).withFailMessage("Block.UNOCCUPIED != OBlock.ALLOCATED").isTrue();
        assertThat(Block.UNOCCUPIED != OBlock.RUNNING).withFailMessage("Block.UNOCCUPIED != OBlock.RUNNING").isTrue();
        assertThat(Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE).withFailMessage("Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE").isTrue();
        assertThat(Block.UNOCCUPIED != OBlock.TRACK_ERROR).withFailMessage("Block.UNOCCUPIED != OBlock.TRACK_ERROR").isTrue();

        assertThat(Block.UNDETECTED != OBlock.ALLOCATED).withFailMessage("Block.UNDETECTED != OBlock.ALLOCATED").isTrue();
        assertThat(Block.UNDETECTED != OBlock.RUNNING).withFailMessage("Block.UNDETECTED != OBlock.RUNNING").isTrue();
        assertThat(Block.UNDETECTED != OBlock.OUT_OF_SERVICE).withFailMessage("Block.UNDETECTED != OBlock.OUT_OF_SERVICE").isTrue();
        assertThat(Block.UNDETECTED != OBlock.TRACK_ERROR).withFailMessage("Block.UNDETECTED != OBlock.TRACK_ERROR").isTrue();
        assertThat(Block.UNDETECTED != OBlock.UNOCCUPIED).withFailMessage("Block.UNDETECTED != OBlock.UNOCCUPIED").isTrue();

        assertThat(Block.UNKNOWN != OBlock.ALLOCATED).withFailMessage("Block.UNKNOWN != OBlock.ALLOCATED").isTrue();
        assertThat(Block.UNKNOWN != OBlock.RUNNING).withFailMessage("Block.UNKNOWN != OBlock.RUNNING").isTrue();
        assertThat(Block.UNKNOWN != OBlock.OUT_OF_SERVICE).withFailMessage("Block.UNKNOWN != OBlock.OUT_OF_SERVICE").isTrue();
        assertThat(Block.UNKNOWN != OBlock.TRACK_ERROR).withFailMessage("Block.UNKNOWN != OBlock.TRACK_ERROR").isTrue();
        assertThat(Block.UNKNOWN != OBlock.UNOCCUPIED).withFailMessage("Block.UNKNOWN != OBlock.UNOCCUPIED").isTrue();
    }
    
    @Test
    public void testSetSensor()  throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB100", "a");
        Assert.assertFalse("setSensor", b.setSensor("foo"));
        Assert.assertNull("getSensor", b.getSensor());
        jmri.util.JUnitAppender.assertErrorMessage("No sensor named 'foo' exists.");        
        
        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor s1 = sensorMgr.newSensor("IS1", "sensor1");
        assertThat(b.setSensor("sensor1")).withFailMessage("setSensor").isTrue();
        assertThat(b.getSensor()).withFailMessage("getSensor").isEqualTo(s1);
        assertThat(b.getState()).withFailMessage("state unknown").isEqualTo(OBlock.UNKNOWN);
        
        assertThat(b.setSensor("IS1")).withFailMessage("dup setSensor").isTrue();
        assertThat(b.getSensor()).withFailMessage("dup getSensor s1").isEqualTo(s1);
        assertThat(b.getState()).withFailMessage("dup state unknown").isEqualTo(OBlock.UNKNOWN);

        assertThat(b.setSensor("  ")).withFailMessage("setSensor none").isTrue();
        Assert.assertNull("getSensor none", b.getSensor());
        assertThat(b.getState()).withFailMessage("none state dark").isEqualTo(OBlock.UNDETECTED);

        assertThat(b.isFree()).withFailMessage("Not Free").isTrue();
        b.setState(b.getState() | OBlock.ALLOCATED|OBlock.RUNNING);
        Assert.assertFalse("Is Free", b.isFree());
        s1.setState(Sensor.ACTIVE);
        assertThat(b.setSensor("sensor1")).withFailMessage("setSensor sensor1").isTrue();
        assertThat(b.getState()).withFailMessage("state allocated&running").isEqualTo(OBlock.OCCUPIED|OBlock.ALLOCATED|OBlock.RUNNING);
    }

    @Test
    public void testSetErrorSensor() throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB101", "b");
        Assert.assertFalse("setErrorSensor foo", b.setErrorSensor("foo"));
        Assert.assertNull("getErrorSensor foo", b.getErrorSensor());
        jmri.util.JUnitAppender.assertErrorMessage("No sensor named 'foo' exists.");        

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor se = sensorMgr.newSensor("ISE1", "error1");
        se.setState(Sensor.ACTIVE);
        assertThat(b.setErrorSensor("error1")).withFailMessage("setErrorSensor only").isTrue();
        assertThat(b.getErrorSensor()).withFailMessage("getErrorSensor only").isEqualTo(se);
        assertThat(b.getState()).withFailMessage("state error only").isEqualTo(OBlock.TRACK_ERROR | OBlock.UNDETECTED);
        
        Sensor s1 = sensorMgr.newSensor("IS1", "sensor1");
        s1.setState(Sensor.ACTIVE);
        assertThat(b.setSensor("IS1")).withFailMessage("setSensor").isTrue();
        assertThat(b.getErrorSensor()).withFailMessage("getErrorSensor se").isEqualTo(se);
        assertThat(b.getState()).withFailMessage("state error").isEqualTo(OBlock.TRACK_ERROR | OBlock.OCCUPIED);

        assertThat(b.setErrorSensor("  ")).withFailMessage("setErrorSensor").isTrue();
        Assert.assertNull("getErrorSensor none", b.getErrorSensor());
        assertThat(b.getState()).withFailMessage("state dark").isEqualTo(OBlock.OCCUPIED);
    }

    @Test
    public void testAllocate() {
        Warrant w1 = new Warrant("IW1", null);
        w1.setTrainName("T1");
        Warrant w2 = new Warrant("IW2", null);
        OBlock b = blkMgr.createNewOBlock("OB102", "c");
        Assert.assertNull("Allocate w1", b.allocate(w1));
        assertThat(b.getState()).withFailMessage("state allocated & dark").isEqualTo(OBlock.ALLOCATED|OBlock.UNDETECTED);
        assertThat(b.allocate(w2)).withFailMessage("Allocate w2").isEqualTo(Bundle.getMessage("AllocatedToWarrant", w1.getDisplayName(), b.getDisplayName(), w1.getTrainName()));
        
        assertThat(b.setPath("PathName", w1)).withFailMessage("path not found").isEqualTo(Bundle.getMessage("PathNotFound", "PathName", b.getDisplayName()));
        OPath path1 = new OPath(b, "path1");
        b.addPath(path1);
        Assert.assertNull("path set", b.setPath("path1", w1));
        Assert.assertFalse("Allocated to w2", b.isAllocatedTo(w2));
        assertThat(b.isAllocatedTo(w1)).withFailMessage("Allocated to w1").isTrue();
        Assert.assertFalse("DeAllocate null", b.deAllocate(null));

        Assert.assertTrue("DeAllocate null", b.deAllocate(w1));
        b.setOutOfService(true);
        assertThat(b.allocate(w2)).withFailMessage("Allocate oos").isEqualTo(Bundle.getMessage("BlockOutOfService", b.getDisplayName()));
        assertThat(b.getState()).withFailMessage("state not allocated, dark").isEqualTo(OBlock.UNDETECTED|OBlock.OUT_OF_SERVICE);
        
        b.setOutOfService(false);
        assertThat(b.setPath("path1", w1)).withFailMessage("path not set").isEqualTo(Bundle.getMessage("PathNotSet", "path1", b.getDisplayName()));
    }
    
    @Test
    public void testSensorChanges() throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB103", null);
        Warrant w0 = new Warrant("IW0", "war0");
        b.setOutOfService(true);
        assertThat(b.getState()).withFailMessage("state OutOfService & dark").isEqualTo(OBlock.UNDETECTED|OBlock.OUT_OF_SERVICE);
       
        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor s0 = sensorMgr.newSensor("IS0", "sensor0");
        assertThat(b.setSensor("sensor0")).withFailMessage("setSensor").isTrue();
        assertThat(b.getState()).withFailMessage("state unknown & dark").isEqualTo(OBlock.UNKNOWN|OBlock.OUT_OF_SERVICE);
        b.setOutOfService(false);
        Assert.assertNull("Allocate w0", b.allocate(w0));
        assertThat(b.getState()).withFailMessage("state allocated & unknown").isEqualTo(OBlock.ALLOCATED|OBlock.UNKNOWN);
        s0.setState(Sensor.ACTIVE);
        assertThat(b.getState()).withFailMessage("state allocated & unknown").isEqualTo(OBlock.ALLOCATED|OBlock.OCCUPIED);
        Assert.assertTrue("DeAllocate w0", b.deAllocate(w0));
        b.setOutOfService(true);
        s0.setState(Sensor.INACTIVE);
        assertThat(b.getState()).withFailMessage("state allocated & unknown").isEqualTo(OBlock.OUT_OF_SERVICE|OBlock.UNOCCUPIED);
        b.setError(false);
        s0.setState(Sensor.INCONSISTENT);
        assertThat(b.getState()).withFailMessage("state  OutOfService & inconsistent").isEqualTo(OBlock.OUT_OF_SERVICE|OBlock.INCONSISTENT);
        assertThat(b.setSensor(null)).withFailMessage("setSensor none").isTrue();
        assertThat(b.getState()).withFailMessage("state  OutOfService & dark").isEqualTo(OBlock.OUT_OF_SERVICE|OBlock.UNDETECTED);
    }

    @Test
    public void testAddPortal() {
        OBlock b = blkMgr.createNewOBlock("OB0", "");
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal p = portalMgr.providePortal("Doop");
        b.addPortal(p);
        assertThat(b.getPortals().size()).withFailMessage("No portals").isEqualTo(0);

        p.setFromBlock(b, true);
        b.addPortal(p);
        assertThat(b.getPortals().size()).withFailMessage("One portal").isEqualTo(1);
        p.setToBlock(b, true);
        b.addPortal(p);
        assertThat(b.getPortals().size()).withFailMessage("One portal only").isEqualTo(1);
        p = portalMgr.providePortal("barp");
        b.addPortal(p);
        p.setToBlock(b, false);
        assertThat(b.getPortals().size()).withFailMessage("Two portals").isEqualTo(2);

        assertThat(b.getPortalByName("barp")).withFailMessage("Same Portal").isEqualTo(p);
        p = b.getPortalByName("Doop");
        assertThat(p).withFailMessage("Get Portal").isNotNull();
        b.removePortal(p);
        assertThat(b.getPortals().size()).withFailMessage("One portals").isEqualTo(1);
        
        jmri.util.JUnitAppender.assertWarnMessage("Portal \"Doop\" between OBlocks \"null\" and \"null\" not in block OB0"); 
        jmri.util.JUnitAppender.assertWarnMessage("Portal \"barp\" between OBlocks \"null\"and \"null\" not in block OB0");
        portalMgr = null;
    }
        
    @Test
    public void testAddPath() {
        OBlock b = blkMgr.createNewOBlock("OB1", "");
        OPath path1 = new OPath(b, "path1");
        // also test the "add" method checks
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        path1.setToPortal(portalMgr.providePortal("foo"));
        Assert.assertFalse("add path1 to block", b.addPath(path1)); // path1 not in foo
        portalMgr.providePortal("foo").addPath(path1);
        Assert.assertFalse("add path1 to block", b.addPath(path1)); // b not in foo
        portalMgr.providePortal("foo").setFromBlock(b, false);
        assertThat(b.addPath(path1)).withFailMessage("add path1 to block").isTrue();  //finally OK
        assertThat(b.getPaths().size()).withFailMessage("One path").isEqualTo(1);
        OBlock bb = blkMgr.createNewOBlock("OB2", "");
        OPath path2 = new OPath(bb, "path2");
        Assert.assertFalse("path2 not in block", b.addPath(path2));
        assertThat(b.getPaths().size()).withFailMessage("path2 not in block").isEqualTo(1);
        jmri.util.JUnitAppender.assertWarnMessage("Path \"path2\" already in block OB2, cannot be added to block OB1"); 
        
        Assert.assertFalse("path1 already in block", b.addPath(path1));
        assertThat(b.getPaths().size()).withFailMessage("path1 already in block").isEqualTo(1);
        OPath path11 = new OPath(b, "path1");
        Assert.assertFalse("path with name \"path1\" already in block", b.addPath(path11));
        assertThat(b.getPaths().size()).withFailMessage("path with name \"path1\" already in block").isEqualTo(1);
        
        path2 = new OPath("path2", b, portalMgr.providePortal("bar"), null, null);
        portalMgr.providePortal("bar").addPath(path2);
        portalMgr.providePortal("bar").setToBlock(b, false);
        assertThat(b.addPath(path2)).withFailMessage("path2 in block").isTrue();
        assertThat(b.getPathByName("path1")).withFailMessage("get \"path1\"").isEqualTo(path1);
        
        b.removeOPath(path1);
        b.removeOPath(path2);
        assertThat(b.getPaths().size()).withFailMessage("no paths").isEqualTo(0);
        portalMgr = null;
    }

    @Test
    public void testAddUserName() {
        OBlock b = blkMgr.provideOBlock("OB99");
        assertThat(b).withFailMessage("Block OB99 is null").isNotNull();
        b.setUserName("99user");
        b = blkMgr.getBySystemName("OB99");
        assertThat(b.getUserName()).withFailMessage("UserName not kept").isEqualTo("99user");
    }
    
    @Test
    public void testGetLocalStatusName() {
        Assert.assertEquals(OBlock.getLocalStatusName("unoccupied"), Bundle.getMessage("unoccupied"));
        Assert.assertEquals(OBlock.getLocalStatusName("occupied"), Bundle.getMessage("occupied"));
        Assert.assertEquals(OBlock.getLocalStatusName("allocated"), Bundle.getMessage("allocated"));
        Assert.assertEquals(OBlock.getLocalStatusName("running"), Bundle.getMessage("running"));
        Assert.assertEquals(OBlock.getLocalStatusName("outOfService"), Bundle.getMessage("outOfService"));
        Assert.assertEquals(OBlock.getLocalStatusName("dark"), Bundle.getMessage("dark"));
        Assert.assertEquals(OBlock.getLocalStatusName("powerError"), Bundle.getMessage("powerError"));
    }
    
    @Test
    public void testGetSystemStatusName() {
        Assert.assertEquals("unoccupied", OBlock.getSystemStatusName(Bundle.getMessage("unoccupied")));
        Assert.assertEquals("occupied", OBlock.getSystemStatusName(Bundle.getMessage("occupied")));
        Assert.assertEquals("allocated", OBlock.getSystemStatusName(Bundle.getMessage("allocated")));
        Assert.assertEquals("running", OBlock.getSystemStatusName(Bundle.getMessage("running")));
        Assert.assertEquals("outOfService", OBlock.getSystemStatusName(Bundle.getMessage("outOfService")));
        Assert.assertEquals("dark", OBlock.getSystemStatusName(Bundle.getMessage("dark")));
        Assert.assertEquals("powerError", OBlock.getSystemStatusName(Bundle.getMessage("powerError")));
    }
    
    @Test
    public void testStatusIs() {
        OBlock oblock = new OBlock("OB99");
        
        oblock.setState(OBlock.UNOCCUPIED);
        Assert.assertTrue(oblock.statusIs("unoccupied"));
        oblock.setState(OBlock.OCCUPIED);
        Assert.assertTrue(oblock.statusIs("occupied"));
        oblock.setState(OBlock.ALLOCATED);
        Assert.assertTrue(oblock.statusIs("allocated"));
        oblock.setState(OBlock.RUNNING);
        Assert.assertTrue(oblock.statusIs("running"));
        oblock.setState(OBlock.OUT_OF_SERVICE);
        Assert.assertTrue(oblock.statusIs("outOfService"));
        oblock.setState(OBlock.UNDETECTED);
        Assert.assertTrue(oblock.statusIs("dark"));
        oblock.setState(OBlock.TRACK_ERROR);
        Assert.assertTrue(oblock.statusIs("powerError"));
    }
    
    @Test
    public void testOBlockStatusEnum() {
        Assert.assertEquals(OBlock.UNOCCUPIED, OBlock.OBlockStatus.Unoccupied.getStatus());
        Assert.assertEquals("unoccupied", OBlock.OBlockStatus.Unoccupied.getName());
        Assert.assertEquals(Bundle.getMessage("unoccupied"), OBlock.OBlockStatus.Unoccupied.getDescr());
        
        Assert.assertEquals(OBlock.OCCUPIED, OBlock.OBlockStatus.Occupied.getStatus());
        Assert.assertEquals("occupied", OBlock.OBlockStatus.Occupied.getName());
        Assert.assertEquals(Bundle.getMessage("occupied"), OBlock.OBlockStatus.Occupied.getDescr());
        
        Assert.assertEquals(OBlock.ALLOCATED, OBlock.OBlockStatus.Allocated.getStatus());
        Assert.assertEquals("allocated", OBlock.OBlockStatus.Allocated.getName());
        Assert.assertEquals(Bundle.getMessage("allocated"), OBlock.OBlockStatus.Allocated.getDescr());
        
        Assert.assertEquals(OBlock.RUNNING, OBlock.OBlockStatus.Running.getStatus());
        Assert.assertEquals("running", OBlock.OBlockStatus.Running.getName());
        Assert.assertEquals(Bundle.getMessage("running"), OBlock.OBlockStatus.Running.getDescr());
        
        Assert.assertEquals(OBlock.OUT_OF_SERVICE, OBlock.OBlockStatus.OutOfService.getStatus());
        Assert.assertEquals("outOfService", OBlock.OBlockStatus.OutOfService.getName());
        Assert.assertEquals(Bundle.getMessage("outOfService"), OBlock.OBlockStatus.OutOfService.getDescr());
        
        Assert.assertEquals(OBlock.UNDETECTED, OBlock.OBlockStatus.Dark.getStatus());
        Assert.assertEquals("dark", OBlock.OBlockStatus.Dark.getName());
        Assert.assertEquals(Bundle.getMessage("dark"), OBlock.OBlockStatus.Dark.getDescr());
        
        Assert.assertEquals(OBlock.TRACK_ERROR, OBlock.OBlockStatus.TrackError.getStatus());
        Assert.assertEquals("powerError", OBlock.OBlockStatus.TrackError.getName());
        Assert.assertEquals(Bundle.getMessage("powerError"), OBlock.OBlockStatus.TrackError.getDescr());
    }
    
    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        blkMgr = new OBlockManager();
    }

    @AfterEach
    public void tearDown() {
        blkMgr = null;
        JUnitUtil.tearDown();
    }

}
