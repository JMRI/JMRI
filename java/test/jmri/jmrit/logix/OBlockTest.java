package jmri.jmrit.logix;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author	Bob Jacobsen Copyright 2010, 2014
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
       Assert.assertNotNull("OBlock Creation",new OBlock("OB01"));
    }
 
   @Test
    public void testCTor2Param(){
       Assert.assertNotNull("OBlock Creation",new OBlock("OB01","test OBlock"));
    }

    @Test
    public void testSeparateCoding() {
        Assert.assertTrue("Block.OCCUPIED != OBlock.ALLOCATED", Block.OCCUPIED != OBlock.ALLOCATED);
        Assert.assertTrue("Block.OCCUPIED != OBlock.RUNNING", Block.OCCUPIED != OBlock.RUNNING);
        Assert.assertTrue("Block.OCCUPIED != OBlock.OUT_OF_SERVICE", Block.OCCUPIED != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.OCCUPIED != OBlock.TRACK_ERROR", Block.OCCUPIED != OBlock.TRACK_ERROR);
        Assert.assertTrue("Block.OCCUPIED != OBlock.UNOCCUPIED", Block.OCCUPIED != OBlock.UNOCCUPIED);

        Assert.assertTrue("Block.UNOCCUPIED != OBlock.ALLOCATED", Block.UNOCCUPIED != OBlock.ALLOCATED);
        Assert.assertTrue("Block.UNOCCUPIED != OBlock.RUNNING", Block.UNOCCUPIED != OBlock.RUNNING);
        Assert.assertTrue("Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE", Block.UNOCCUPIED != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.UNOCCUPIED != OBlock.TRACK_ERROR", Block.UNOCCUPIED != OBlock.TRACK_ERROR);

        Assert.assertTrue("Block.UNDETECTED != OBlock.ALLOCATED", Block.UNDETECTED != OBlock.ALLOCATED);
        Assert.assertTrue("Block.UNDETECTED != OBlock.RUNNING", Block.UNDETECTED != OBlock.RUNNING);
        Assert.assertTrue("Block.UNDETECTED != OBlock.OUT_OF_SERVICE", Block.UNDETECTED != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.UNDETECTED != OBlock.TRACK_ERROR", Block.UNDETECTED != OBlock.TRACK_ERROR);
        Assert.assertTrue("Block.UNDETECTED != OBlock.UNOCCUPIED", Block.UNDETECTED != OBlock.UNOCCUPIED);

        Assert.assertTrue("Block.UNKNOWN != OBlock.ALLOCATED", Block.UNKNOWN != OBlock.ALLOCATED);
        Assert.assertTrue("Block.UNKNOWN != OBlock.RUNNING", Block.UNKNOWN != OBlock.RUNNING);
        Assert.assertTrue("Block.UNKNOWN != OBlock.OUT_OF_SERVICE", Block.UNKNOWN != OBlock.OUT_OF_SERVICE);
        Assert.assertTrue("Block.UNKNOWN != OBlock.TRACK_ERROR", Block.UNKNOWN != OBlock.TRACK_ERROR);
        Assert.assertTrue("Block.UNKNOWN != OBlock.UNOCCUPIED", Block.UNKNOWN != OBlock.UNOCCUPIED);
    }
    
    @Test
    public void testSetSensor()  throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB100", "a");
        Assert.assertFalse("setSensor", b.setSensor("foo"));
        Assert.assertNull("getSensor", b.getSensor());
        
        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor s1 = sensorMgr.newSensor("IS1", "sensor1");
        Assert.assertTrue("setSensor", b.setSensor("sensor1"));
        Assert.assertEquals("getSensor", s1, b.getSensor());
        Assert.assertEquals("state unknown", OBlock.UNKNOWN, b.getState());
        
        Assert.assertTrue("dup setSensor", b.setSensor("IS1"));
        Assert.assertEquals("dup getSensor s1", s1, b.getSensor());
        Assert.assertEquals("dup state unknown", OBlock.UNKNOWN, b.getState());

        Assert.assertTrue("setSensor none", b.setSensor("  "));
        Assert.assertNull("getSensor none", b.getSensor());
        Assert.assertEquals("none state dark", OBlock.UNDETECTED, b.getState());

        Assert.assertTrue("Not Free", b.isFree());
        b.setState(b.getState() | OBlock.ALLOCATED|OBlock.RUNNING);
        Assert.assertFalse("Is Free", b.isFree());
        s1.setState(Sensor.ACTIVE);
        Assert.assertTrue("setSensor sensor1", b.setSensor("sensor1"));
        Assert.assertEquals("state allocated&running", OBlock.OCCUPIED|OBlock.ALLOCATED|OBlock.RUNNING, b.getState());
    }

    @Test
    public void testSetErrorSensor() throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB101", "b");
        Assert.assertFalse("setErrorSensor foo", b.setErrorSensor("foo"));
        Assert.assertNull("getErrorSensor foo", b.getErrorSensor());
        
        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor se = sensorMgr.newSensor("ISE1", "error1");
        se.setState(Sensor.ACTIVE);
        Assert.assertTrue("setErrorSensor only", b.setErrorSensor("error1"));
        Assert.assertEquals("getErrorSensor only", se, b.getErrorSensor());
        Assert.assertEquals("state error only", OBlock.TRACK_ERROR | OBlock.UNDETECTED, b.getState());
        
        Sensor s1 = sensorMgr.newSensor("IS1", "sensor1");
        s1.setState(Sensor.ACTIVE);
        Assert.assertTrue("setSensor", b.setSensor("IS1"));
        Assert.assertEquals("getErrorSensor se", se, b.getErrorSensor());
        Assert.assertEquals("state error", OBlock.TRACK_ERROR | OBlock.OCCUPIED, b.getState());

        Assert.assertTrue("setErrorSensor", b.setErrorSensor("  "));
        Assert.assertNull("getErrorSensor none", b.getErrorSensor());
        Assert.assertEquals("state dark", OBlock.OCCUPIED, b.getState());
    }

    @Test
    public void testAllocate() {
        Warrant w1 = new Warrant("IW1", null);
        w1.setTrainName("T1");
        Warrant w2 = new Warrant("IW2", null);
        OBlock b = blkMgr.createNewOBlock("OB102", "c");
        Assert.assertNull("Allocate w1", b.allocate(w1));
        Assert.assertEquals("state allocated & dark", OBlock.ALLOCATED|OBlock.UNDETECTED, b.getState());
        Assert.assertEquals("Allocate w2",
                Bundle.getMessage("AllocatedToWarrant", w1.getDisplayName(), b.getDisplayName(), w1.getTrainName()),
                b.allocate(w2));
        
        Assert.assertEquals("path not found", Bundle.getMessage("PathNotFound", "PathName", b.getDisplayName()), b.setPath("PathName", w1));
        OPath path1 = new OPath(b, "path1");
        b.addPath(path1);
        Assert.assertNull("path set", b.setPath("path1", w1));
        Assert.assertFalse("Allocated to w2", b.isAllocatedTo(w2));
        Assert.assertTrue("Allocated to w1", b.isAllocatedTo(w1));
        Assert.assertNull("DeAllocate null", b.deAllocate(null));

        b.setOutOfService(true);
        Assert.assertEquals("Allocate oos", Bundle.getMessage("BlockOutOfService", b.getDisplayName()), b.allocate(w2));
        Assert.assertEquals("state not allocated, dark", OBlock.UNDETECTED|OBlock.OUT_OF_SERVICE, b.getState());
        
        b.setOutOfService(false);
        Assert.assertNull("DeAllocate w1", b.deAllocate(w1));
        Assert.assertEquals("path not set", Bundle.getMessage("PathNotSet", "path1", b.getDisplayName()), b.setPath("path1", w1));
        
        jmri.util.JUnitAppender.assertWarnMessage("Path \"PathName\" not found in block \"c\"."); 
        jmri.util.JUnitAppender.assertWarnMessage("Path \"path1\" not set in block \"c\". Block not allocated."); 
    }
    
    @Test
    public void testSensorChanges() throws Exception {
        OBlock b = blkMgr.createNewOBlock("OB103", null);
        Warrant w0 = new Warrant("IW0", "war0");
        b.setOutOfService(true);
        Assert.assertEquals("state OutOfService & dark", OBlock.UNDETECTED|OBlock.OUT_OF_SERVICE, b.getState());
       
        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor s0 = sensorMgr.newSensor("IS0", "sensor0");
        Assert.assertTrue("setSensor", b.setSensor("sensor0"));
        Assert.assertEquals("state unknown & dark", OBlock.UNKNOWN|OBlock.OUT_OF_SERVICE, b.getState());
        b.setOutOfService(false);
        Assert.assertNull("Allocate w0", b.allocate(w0));
        Assert.assertEquals("state allocated & unknown", OBlock.ALLOCATED|OBlock.UNKNOWN, b.getState());
        s0.setState(Sensor.ACTIVE);
        Assert.assertEquals("state allocated & unknown", OBlock.ALLOCATED|OBlock.OCCUPIED, b.getState());
        Assert.assertNull("DeAllocate w0", b.deAllocate(w0));
        b.setOutOfService(true);
        s0.setState(Sensor.INACTIVE);
        Assert.assertEquals("state allocated & unknown", OBlock.OUT_OF_SERVICE|OBlock.UNOCCUPIED, b.getState());
        b.setError(false);
        s0.setState(Sensor.INCONSISTENT);
        Assert.assertEquals("state  OutOfService & inconsistent", OBlock.OUT_OF_SERVICE|OBlock.INCONSISTENT, b.getState());
        Assert.assertTrue("setSensor none", b.setSensor(null));
        Assert.assertEquals("state  OutOfService & dark", OBlock.OUT_OF_SERVICE|OBlock.UNDETECTED, b.getState());
    }

    @Test
    public void testAddPortal() {
        OBlock b = blkMgr.createNewOBlock("OB0", "");
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal p = portalMgr.providePortal("Doop");
        b.addPortal(p);
        Assert.assertEquals("No portals", 0, b.getPortals().size());

        p.setFromBlock(b, true);
        b.addPortal(p);
        Assert.assertEquals("One portal", 1, b.getPortals().size());
        p.setToBlock(b, true);
        b.addPortal(p);
        Assert.assertEquals("One portal only", 1, b.getPortals().size());
        p = portalMgr.providePortal("barp");
        b.addPortal(p);
        p.setToBlock(b, false);
        Assert.assertEquals("Two portals", 2, b.getPortals().size());

        Assert.assertEquals("Same Portal", p, b.getPortalByName("barp"));
        p = b.getPortalByName("Doop");
        Assert.assertNotNull("Get Portal", p);
        b.removePortal(p);
        Assert.assertEquals("One portals", 1, b.getPortals().size());
        
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
        Assert.assertTrue("add path1 to block", b.addPath(path1));  //finally OK
        Assert.assertEquals("One path", 1, b.getPaths().size());
        OBlock bb = blkMgr.createNewOBlock("OB2", "");
        OPath path2 = new OPath(bb, "path2");
        Assert.assertFalse("path2 not in block", b.addPath(path2));
        Assert.assertEquals("path2 not in block", 1, b.getPaths().size());
        jmri.util.JUnitAppender.assertWarnMessage("Path \"path2\" already in block OB2, cannot be added to block OB1"); 
        
        Assert.assertFalse("path1 already in block", b.addPath(path1));
        Assert.assertEquals("path1 already in block", 1, b.getPaths().size());
        OPath path11 = new OPath(b, "path1");
        Assert.assertFalse("path with name \"path1\" already in block", b.addPath(path11));
        Assert.assertEquals("path with name \"path1\" already in block", 1, b.getPaths().size());
        
        path2 = new OPath("path2", b, portalMgr.providePortal("bar"), null, null);
        portalMgr.providePortal("bar").addPath(path2);
        portalMgr.providePortal("bar").setToBlock(b, false);
        Assert.assertTrue("path2 in block", b.addPath(path2));
        Assert.assertEquals("get \"path1\"", path1, b.getPathByName("path1"));
        
        b.removeOPath(path1);
        b.removeOPath(path2);
        Assert.assertEquals("no paths", 0, b.getPaths().size());
        portalMgr = null;
    }

    @Test
    public void testAddUserName() {
        OBlock b = blkMgr.provideOBlock("OB99");
        b.setUserName("99user");
        b = blkMgr.getBySystemName("OB99");
        Assert.assertEquals("UserName not kept", "99user", b.getUserName());
    }
    
    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        blkMgr = new OBlockManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        blkMgr = null;
    }

}
