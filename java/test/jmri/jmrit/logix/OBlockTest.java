package jmri.jmrit.logix;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 *
 * @author	Bob Jacobsen Copyright 2010, 2014
 */
public class OBlockTest extends TestCase {

    OBlockManager blkMgr;

    /* OBlock.DARK replaced with Block.UNDETECTED - 12/10/2016 pwc
    @SuppressWarnings("all") // otherwise, you get "Comparing identical" warning (until something breaks!)
    public void testEqualCoding() {
        // the following match is required by the JavaDoc
        Assert.assertTrue("Block.UNKNOWN == OBlock.DARK", Block.UNKNOWN == OBlock.DARK);
    }*/

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
    
    public void testSetSensor() {
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
        
        b.setState(b.getState() | OBlock.ALLOCATED|OBlock.RUNNING);
        try {
            s1.setState(Sensor.ACTIVE);
        } catch (Exception je) { }        
        Assert.assertTrue("setSensor sensor1", b.setSensor("sensor1"));
        Assert.assertEquals("state allocated&running", OBlock.OCCUPIED|OBlock.ALLOCATED|OBlock.RUNNING, b.getState());
    }

    public void testSetErrorSensor() {
        OBlock b = blkMgr.createNewOBlock("OB101", "b");
        Assert.assertFalse("setErrorSensor foo", b.setErrorSensor("foo"));
        Assert.assertNull("getErrorSensor foo", b.getErrorSensor());
        
        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor se = sensorMgr.newSensor("ISE1", "error1");
        try {
            se.setState(Sensor.ACTIVE);
        } catch (Exception je) { }        
        Assert.assertTrue("setErrorSensor only", b.setErrorSensor("error1"));
        Assert.assertEquals("getErrorSensor only", se, b.getErrorSensor());
        Assert.assertEquals("state error only", OBlock.TRACK_ERROR | OBlock.UNDETECTED, b.getState());
        
        Sensor s1 = sensorMgr.newSensor("IS1", "sensor1");
        try {
            s1.setState(Sensor.ACTIVE);
        } catch (Exception je) { }        
        Assert.assertTrue("setSensor", b.setSensor("IS1"));
        Assert.assertEquals("getErrorSensor se", se, b.getErrorSensor());
        Assert.assertEquals("state error", OBlock.TRACK_ERROR | OBlock.OCCUPIED, b.getState());

        Assert.assertTrue("setErrorSensor", b.setErrorSensor("  "));
        Assert.assertNull("getErrorSensor none", b.getErrorSensor());
        Assert.assertEquals("state dark", OBlock.OCCUPIED, b.getState());
    }

    public void testAllocate() {
        Warrant w1 = new Warrant("IW1", null);
        Warrant w2 = new Warrant("IW2", null);
        OBlock b = blkMgr.createNewOBlock("OB102", "c");
        Assert.assertNull("Allocate w1", b.allocate(w1));
        Assert.assertEquals("state allocated & dark", OBlock.ALLOCATED|OBlock.UNDETECTED, b.getState());
        Assert.assertEquals("Allocate w2", Bundle.getMessage("AllocatedToWarrant", w1.getDisplayName(), b.getDisplayName()), b.allocate(w2));
        Assert.assertNull("DeAllocate w1", b.deAllocate(null));

        b.setOutOfService(true);
        Assert.assertEquals("Allocate oos", Bundle.getMessage("BlockOutOfService", b.getDisplayName()), b.allocate(w2));
        Assert.assertEquals("state not allocated, dark", OBlock.UNDETECTED|OBlock.OUT_OF_SERVICE, b.getState());
    }
    
    public void testSensorChanges() {
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
        try {
            s0.setState(Sensor.ACTIVE);
        } catch (Exception je) { }        
        Assert.assertEquals("state allocated & unknown", OBlock.ALLOCATED|OBlock.OCCUPIED, b.getState());
        Assert.assertNull("DeAllocate w0", b.deAllocate(w0));
        b.setOutOfService(true);
        try {
            s0.setState(Sensor.INACTIVE);
        } catch (Exception je) { }        
        Assert.assertEquals("state allocated & unknown", OBlock.OUT_OF_SERVICE|OBlock.UNOCCUPIED, b.getState());
        b.setError(false);
        try {
            s0.setState(Sensor.INCONSISTENT);
        } catch (Exception je) { }        
        Assert.assertEquals("state  OutOfService & inconsistent", OBlock.OUT_OF_SERVICE|OBlock.INCONSISTENT, b.getState());
        Assert.assertTrue("setSensor none", b.setSensor(null));
        Assert.assertEquals("state  OutOfService & dark", OBlock.OUT_OF_SERVICE|OBlock.UNDETECTED, b.getState());
    }

    public void testAddPortal() {
        OBlock b = blkMgr.createNewOBlock("OB0", "");
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);;
        Portal p = portalMgr.providePortal("foop");
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
        
        jmri.util.JUnitAppender.assertWarnMessage("Portal \"foop\" from block \"null\" to block \"null\" not in block OB0"); 
        jmri.util.JUnitAppender.assertWarnMessage("Portal \"barp\" from block \"null\" to block \"null\" not in block OB0"); 
    }
        
    public void testAddPath() {
        OBlock b = blkMgr.createNewOBlock("OB1", "");
        OPath path1 = new OPath(b, "path1");
        Assert.assertTrue("add path1 to block", b.addPath(path1));
        Assert.assertEquals("One path", 1, b.getPaths().size());
        OBlock bb = blkMgr.createNewOBlock("OB2", "");
        OPath path2 = new OPath(bb, "path2");
        Assert.assertFalse("path2 not in block", b.addPath(path2));
        Assert.assertEquals("path2 not in block", 1, b.getPaths().size());
        Assert.assertFalse("path1 already in block", b.addPath(path1));
        Assert.assertEquals("path1 already in block", 1, b.getPaths().size());
        OPath path11 = new OPath(b, "path1");
        Assert.assertFalse("path with name \"path1\" already in block", b.addPath(path11));
        Assert.assertEquals("path with name \"path1\" already in block", 1, b.getPaths().size());
        
        b.removePath(path1);
        Assert.assertEquals("no paths", 0, b.getPaths().size());
    }
    
    // from here down is testing infrastructure
    public OBlockTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OBlockTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(OBlockTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        blkMgr = new OBlockManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
