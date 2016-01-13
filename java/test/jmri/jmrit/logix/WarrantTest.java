// WarrantTest.java
package jmri.jmrit.logix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Warrant creation
 *
 * @author  Pete Cressman 2015
 * @version $Revision: 00000 $
 * 
 * todo - test error conditions
 */
public class WarrantTest extends TestCase {

    OBlockManager _OBlockMgr;
    PortalManager _portalMgr;
    SensorManager _sensorMgr;
    TurnoutManager _turnoutMgr;
    
    /**
     * It's considered bad form to write tests that depend on the order of execution.
     * So this will be one large test.
     */

    public void testWarrant() throws Exception {
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        OBlock bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        OBlock bEast = _OBlockMgr.createNewOBlock("OB2", "East");
        OBlock bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        OBlock bSouth = _OBlockMgr.createNewOBlock("OB4", "South");
        Assert.assertEquals("OBlock", bNorth, _OBlockMgr.getOBlock("North"));
        Assert.assertEquals("OBlock", bEast, _OBlockMgr.getOBlock("OB2"));
        
        _portalMgr = InstanceManager.getDefault(PortalManager.class);        
        Portal pNorthWest = _portalMgr.createNewPortal(null, "NorthWest");
        pNorthWest.setToBlock(bWest, false);
        pNorthWest.setFromBlock(bNorth, false);
        Portal pSouthWest = _portalMgr.createNewPortal(null, "SouthWest");
        pSouthWest.setToBlock(bWest, false);
        pSouthWest.setFromBlock(bSouth, false);        
        Assert.assertEquals("Portal", pNorthWest, _portalMgr.getPortal("NorthWest"));
        Assert.assertEquals("Portal Block", bSouth, _portalMgr.getPortal("SouthWest").getFromBlock());
        Assert.assertEquals("Portal", pSouthWest, bSouth.getPortalByName("SouthWest"));        
        Assert.assertEquals("Portal Block", "West", _portalMgr.getPortal("NorthWest").getToBlockName());
        Assert.assertEquals("Portal Block", "North", _portalMgr.getPortal("NorthWest").getFromBlockName());

        Portal pNorthEast = _portalMgr.createNewPortal(null, "NorthEast");
        pNorthEast.setToBlock(_OBlockMgr.getOBlock("OB2"), false);
        pNorthEast.setFromBlock(_OBlockMgr.getOBlock("North"), false);
        Portal pSouthEast = _portalMgr.createNewPortal(null, "SouthEast");
        OBlock east = _OBlockMgr.getOBlock("OB2");
        pSouthEast.setToBlock(east, false);
        pSouthEast.setFromBlock(_OBlockMgr.getOBlock("South"), false);
        
        Assert.assertEquals("Portal Block", east, _portalMgr.getPortal("SouthEast").getToBlock());
        Assert.assertEquals("Portal Block", "West", _portalMgr.getPortal("NorthWest").getToBlockName());
        Assert.assertEquals("Portal Block", _OBlockMgr.getOBlock("South"), _portalMgr.getPortal("SouthWest").getFromBlock());

        _turnoutMgr = InstanceManager.turnoutManagerInstance();
        Turnout northSwitch = _turnoutMgr.newTurnout("IT1", "NorthSwitch");
        ArrayList<BeanSetting> settings = new ArrayList<BeanSetting>();
        settings.add(new BeanSetting(northSwitch, "NorthSwitch", Turnout.CLOSED));
        OBlock north = _OBlockMgr.getOBlock("North");
        OPath path = new OPath("NorthToWest", north, null, _portalMgr.getPortal("NorthWest"), settings);
        north.addPath(path);
        
        settings = new ArrayList<BeanSetting>();
        settings.add(new BeanSetting(northSwitch, "NorthSwitch", Turnout.THROWN));
        path = new OPath("NorthToEast", north, null, _portalMgr.getPortal("NorthEast"), settings);
        north.addPath(path);        
        Assert.assertEquals("Path Block", path, north.getPathByName("NorthToEast"));
        Assert.assertEquals("Path Block", "NorthToWest", north.getPathByName("NorthToWest").getName());
        
        Turnout southSwitch = _turnoutMgr.newTurnout("IT2", "SouthSwitch");
        OBlock south = _OBlockMgr.getOBlock("South");
        settings = new ArrayList<BeanSetting>();
        settings.add(new BeanSetting(southSwitch, "SouthSwitch", Turnout.THROWN));
        path = new OPath("SouthToEast", south, null, _portalMgr.getPortal("SouthEast"), settings);
        south.addPath(path);
        settings = new ArrayList<BeanSetting>();
        settings.add(new BeanSetting(southSwitch, "SouthSwitch", Turnout.CLOSED));
        path = new OPath("SouthToWest", south, null, south.getPortalByName("SouthWest"), settings);
        south.addPath(path);
        Assert.assertEquals("Path Block", path, south.getPathByName("SouthToWest"));
        Assert.assertEquals("Path Block", "SouthToEast", south.getPathByName("SouthToEast").getName());
        
        
        settings = new ArrayList<BeanSetting>();
        OBlock block =  _OBlockMgr.getOBlock("West");
        path = new OPath("SouthToNorth", block, _portalMgr.getPortal("NorthWest"), _portalMgr.getPortal("SouthWest"), settings);
        _OBlockMgr.getOBlock("West").addPath(path);
        Assert.assertEquals("Path Block", path, block.getPathByName("SouthToNorth"));
        settings = new ArrayList<BeanSetting>();
        block =  _OBlockMgr.getOBlock("East");
        path = new OPath("NorthToSouth", block, south.getPortalByName("SouthEast"), north.getPortalByName("NorthEast"), settings);
        _OBlockMgr.getOBlock("East").addPath(path);
        Assert.assertEquals("Path Block", path, block.getPathByName("NorthToSouth"));
   
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor sWest = _sensorMgr.newSensor("IS1", "WestSensor");
        Sensor sEast = _sensorMgr.newSensor("IS2", "EastSensor");
        Sensor sNorth = _sensorMgr.newSensor("IS3", "NorthSensor");
        Sensor sSouth = _sensorMgr.newSensor("IS4", "SouthSensor");
        bWest.setSensor("WestSensor");
        bEast.setSensor("IS2");
        bNorth.setSensor("NorthSensor");
        bSouth.setSensor("IS4");
        Assert.assertEquals("Sensor Block", sNorth, bNorth.getSensor());
        Assert.assertEquals("Sensor Block", sSouth, bSouth.getSensor());
        try{
            sWest.setState(Sensor.INACTIVE);
            sEast.setState(Sensor.ACTIVE);
            sNorth.setState(Sensor.INACTIVE);
            sSouth.setState(Sensor.ACTIVE);            
        } catch (JmriException je) { }
        Assert.assertEquals("Block Detection", OBlock.UNOCCUPIED, bWest.getState());
        Assert.assertEquals("Block Detection", OBlock.OCCUPIED, bEast.getState());
        Warrant warrant = new Warrant("IW0", "AllTestWarrant");
        bWest.allocate(warrant);
        bEast.allocate(warrant);
        Assert.assertEquals("Block Detection", OBlock.UNOCCUPIED | OBlock.ALLOCATED, bWest.getState());
        Assert.assertEquals("Block Detection", OBlock.OCCUPIED | OBlock.ALLOCATED, bEast.getState());
        try{
            sEast.setState(Sensor.INACTIVE);
            sSouth.setState(Sensor.INACTIVE);            
            sNorth.setState(Sensor.ACTIVE);     // start block of warrant
        } catch (JmriException je) { }
        bWest.deAllocate(warrant);
        bEast.deAllocate(warrant);
        Assert.assertEquals("Block Detection", OBlock.UNOCCUPIED, bWest.getState());
        Assert.assertEquals("Block Detection", OBlock.UNOCCUPIED, bEast.getState());

        ArrayList <BlockOrder> orders = new ArrayList <BlockOrder>();
        orders.add(new BlockOrder(_OBlockMgr.getOBlock("North"), "NorthToWest", "", "NorthWest"));
        BlockOrder viaOrder = new BlockOrder(_OBlockMgr.getOBlock("West"), "SouthToNorth", "NorthWest", "SouthWest");
        orders.add(viaOrder);
        BlockOrder lastOrder = new BlockOrder(_OBlockMgr.getOBlock("South"), "SouthToWest", "SouthWest", null);
        orders.add(lastOrder);
        
        warrant.setViaOrder(viaOrder);
        warrant.setBlockOrders(orders);
        Assert.assertEquals("BlockOrder", warrant.getLastOrder().toString(), lastOrder.toString());
        Assert.assertEquals("BlockOrder", warrant.getViaOrder().toString(), viaOrder.toString());
        
        String msg = warrant.allocateRoute(orders);
        Assert.assertNull("allocateRoute - "+msg, msg);
        warrant.deAllocate();
        
        warrant.setThrottleCommands(new ArrayList<ThrottleSetting>());
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.4", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "West"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.5", "West"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "South"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.3", "South"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "South"));
        List<ThrottleSetting> list = warrant.getThrottleCommands();
        Assert.assertEquals("ThrottleCommands", 7, list.size());
        
//        DccLocoAddress dccAddress = new DccLocoAddress(999, true);
//        Assert.assertNotNull("dccAddress", dccAddress);
        warrant.setDccAddress("999(L)");
        msg = warrant.setRoute(0, orders);
        Assert.assertNull("setRoute - "+msg, msg);
        msg =  warrant.checkStartBlock(Warrant.MODE_RUN);
        Assert.assertNull("checkStartBlock - "+msg, msg);
        msg = warrant.checkRoute();
        Assert.assertNull("checkRoute - "+msg, msg);
        
        warrant.setTrainName("TestTrain");
        PropertyChangeListener listener = new WarrantListener(warrant);
        Assert.assertNotNull("PropertyChangeListener", listener);
        warrant.addPropertyChangeListener(listener);
        
        msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        Assert.assertNull("setRunMode - "+msg, msg);
        try {
            jmri.util.JUnitUtil.releaseThread(this); // nothing specific to wait for...
            sWest.setState(Sensor.ACTIVE);
            jmri.util.JUnitUtil.releaseThread(this);             
            sSouth.setState(Sensor.ACTIVE);
            jmri.util.JUnitUtil.releaseThread(this);             
        } catch (Exception e) {
            System.out.println(e);            
        }

        // confirm one message logged
        jmri.util.JUnitAppender.assertWarnMessage("RosterSpeedProfile not found. Using default ThrottleFactor 0.75");

        // wait for done
        jmri.util.JUnitUtil.waitFor(()->{return warrant.getThrottle()==null;}, "engineer blocked");

        msg = warrant.getRunningMessage();
        Assert.assertEquals("getRunningMessage", "Idle", msg);
    }
    
    
    class WarrantListener implements PropertyChangeListener {
        
        Warrant warrant;
        WarrantListener( Warrant w) {
            warrant = w;
        }
        public void propertyChange(PropertyChangeEvent e) {
//            String property = e.getPropertyName();
//            System.out.println("propertyChange \""+property+
//                    "\" old= "+e.getOldValue()+" new= "+e.getNewValue());
            Assert.assertEquals("propertyChange", warrant, e.getSource());           
//            System.out.println(warrant.getRunningMessage());
        }
    }

    public void testSetBlockToNull() {
        OBlock b1 = new OBlock("IB1");

        OPath op = new OPath(b1, "name");
        op.setBlock(null);

        Assert.assertEquals("block", null, op.getBlock());
    }

    // from here down is testing infrastructure
    public WarrantTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", WarrantTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(WarrantTest.class);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        JUnitUtil.initWarrantManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
