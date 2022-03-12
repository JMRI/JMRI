package jmri.jmrit.logix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Warrant creation.
 *
 * @author Pete Cressman 2015
 *
 * TODO - test error conditions
 */
public class WarrantTest {

    protected OBlockManager _OBlockMgr;
    protected PortalManager _portalMgr;
    protected SensorManager _sensorMgr;
    protected TurnoutManager _turnoutMgr;
    protected OBlock bWest;
    protected OBlock bEast;
    protected OBlock bNorth;
    protected OBlock bSouth;
    protected Warrant warrant;
    protected Sensor sWest;
    protected Sensor sEast;
    protected Sensor sNorth;
    protected Sensor sSouth;

    @Test
    public void testCTor() {
        assertThat(warrant).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testSetAndGetTrainName() {
        warrant.setTrainName("TestTrain");
        assertThat(warrant.getTrainName()).withFailMessage("Train Name").isEqualTo("TestTrain");
    }

    @Test
    public void testGetSpeedUtil() {
        SpeedUtil su = warrant.getSpeedUtil();
        assertThat(su).withFailMessage("SpeedUtil null").isNotNull();
    }

    @Test
    public void testAddPropertyChangeListener() {
        PropertyChangeListener listener = new WarrantListener(warrant);
        assertThat(listener).withFailMessage("PropertyChangeListener").isNotNull();
        warrant.addPropertyChangeListener(listener);
    }

    @Test
    public void testAllocateAndDeallocateWarrant() {
        try {
            sWest.setState(Sensor.INACTIVE);
            sEast.setState(Sensor.ACTIVE);
            sNorth.setState(Sensor.INACTIVE);
            sSouth.setState(Sensor.ACTIVE);
        } catch (JmriException ignore) {
        }
        bWest.allocate(warrant);
        bEast.allocate(warrant);
        assertThat(bWest.getState()).withFailMessage("Block Detection 3").isEqualTo(OBlock.UNOCCUPIED | OBlock.ALLOCATED);
        assertThat(bEast.getState()).withFailMessage("Block Detection 4").isEqualTo(OBlock.OCCUPIED | OBlock.ALLOCATED);
        try {
            sEast.setState(Sensor.INACTIVE);
            sSouth.setState(Sensor.INACTIVE);
            sNorth.setState(Sensor.ACTIVE);     // start block of warrant
        } catch (JmriException ignore) {
        }
        bWest.deAllocate(warrant);
        bEast.deAllocate(warrant);
        assertThat(bWest.getState()).withFailMessage("Block Detection 5").isEqualTo(OBlock.UNOCCUPIED);
        assertThat(bEast.getState()).withFailMessage("Block Detection 6").isEqualTo(OBlock.UNOCCUPIED);
    }

    @Test
    public void testSetRouteUsingViaOrders() {
        try {
            sEast.setState(Sensor.INACTIVE);
            sSouth.setState(Sensor.INACTIVE);
            sNorth.setState(Sensor.ACTIVE);     // start block of warrant
        } catch (JmriException ignore) {
        }
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(_OBlockMgr.getOBlock("North"), "NorthToWest", "", "NorthWest"));
        BlockOrder viaOrder = new BlockOrder(_OBlockMgr.getOBlock("West"), "SouthToNorth", "NorthWest", "SouthWest");
        orders.add(viaOrder);
        BlockOrder lastOrder = new BlockOrder(_OBlockMgr.getOBlock("South"), "SouthToWest", "SouthWest", null);
        orders.add(lastOrder);

        warrant.setViaOrder(viaOrder);
        warrant.setBlockOrders(orders);
        assertThat(lastOrder.toString()).withFailMessage("BlockOrder").isEqualTo(warrant.getLastOrder().toString());
        assertThat(viaOrder.toString()).withFailMessage("BlockOrder").isEqualTo(warrant.getViaOrder().toString());

        String msg = warrant.allocateRoute(false, orders);
        Assert.assertNull("allocateRoute - " + msg, msg);
        msg = warrant.checkStartBlock();
        Assert.assertNull("checkStartBlock - " + msg, msg);
        msg = warrant.checkRoute();
        Assert.assertNull("checkRoute - " + msg, msg);
    }

    @Test
    public void testSetRoute() {
        try {
            sEast.setState(Sensor.INACTIVE);
            sSouth.setState(Sensor.INACTIVE);
            sNorth.setState(Sensor.ACTIVE);     // start block of warrant
        } catch (JmriException ignore) {
        }
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(_OBlockMgr.getOBlock("North"), "NorthToWest", "", "NorthWest"));
        BlockOrder viaOrder = new BlockOrder(_OBlockMgr.getOBlock("West"), "SouthToNorth", "NorthWest", "SouthWest");
        orders.add(viaOrder);
        BlockOrder lastOrder = new BlockOrder(_OBlockMgr.getOBlock("South"), "SouthToWest", "SouthWest", null);
        orders.add(lastOrder);

        String msg = warrant.allocateRoute(false, orders);
        Assert.assertNull("allocateRoute - " + msg, msg);
        msg = warrant.checkStartBlock();
        Assert.assertNull("checkStartBlock - " + msg, msg);
        msg = warrant.checkRoute();
        Assert.assertNull("checkRoute - " + msg, msg);

        assertThat(lastOrder.toString()).withFailMessage("BlockOrder").isEqualTo(warrant.getLastOrder().toString());
    }

    @Test
    public void setThrottleCommands() {
        warrant.setThrottleCommands(new ArrayList<>());
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(10, "Speed", "0.4", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "West"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.5", "West"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "South"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.3", "South"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "South"));
        List<ThrottleSetting> list = warrant.getThrottleCommands();
        assertThat(list.size()).withFailMessage("ThrottleCommands").isEqualTo(7);
    }

    @Test
    public void testWarrant() throws JmriException {
        sEast.setState(Sensor.INACTIVE);
        sSouth.setState(Sensor.INACTIVE);
        sNorth.setState(Sensor.ACTIVE);     // start block of warrant

        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(_OBlockMgr.getOBlock("North"), "NorthToWest", "", "NorthWest"));
        BlockOrder viaOrder = new BlockOrder(_OBlockMgr.getOBlock("West"), "SouthToNorth", "NorthWest", "SouthWest");
        orders.add(viaOrder);
        BlockOrder lastOrder = new BlockOrder(_OBlockMgr.getOBlock("South"), "SouthToWest", "SouthWest", null);
        orders.add(lastOrder);

        warrant.setThrottleCommands(new ArrayList<>());
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(10, "Speed", "0.4", "North"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "West"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.5", "West"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "South"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.3", "South"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "South"));

        warrant.getSpeedUtil().setAddress("999(L)");
        String msg = warrant.allocateRoute(false, orders);

        warrant.setTrainName("TestTrain");
        PropertyChangeListener listener = new WarrantListener(warrant);
        warrant.addPropertyChangeListener(listener);

        msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        Assert.assertNull("setRunMode - " + msg, msg);

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = warrant.getRunningMessage();
            return m.endsWith("Cmd #2.") || m.endsWith("Cmd #3.");
        }, "Train starts to move after 2nd command");
        jmri.util.JUnitUtil.releaseThread(this, 100); // What should we specifically waitFor?

        jmri.util.ThreadingUtil.runOnLayout(() -> {
            try {
                sWest.setState(Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                Assert.fail("Unexpected Exception: " + e);
            }
        });
        jmri.util.JUnitUtil.releaseThread(this, 100); // What should we specifically waitFor?

        jmri.util.ThreadingUtil.runOnLayout(() -> {
            try {
                sSouth.setState(Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                Assert.fail("Unexpected Exception: " + e);
            }
        });
        jmri.util.JUnitUtil.releaseThread(this, 100);

        // wait for done
        jmri.util.JUnitUtil.waitFor(() -> {
            return warrant.getRunningMessage().equals("Idle");
        }, "warrant not done");

    }

    static class WarrantListener implements PropertyChangeListener {

        Warrant warrant;

        WarrantListener(Warrant w) {
            warrant = w;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
//            String property = e.getPropertyName();
//            System.out.println("propertyChange \""+property+
//                    "\" old= "+e.getOldValue()+" new= "+e.getNewValue());
            assertThat(e.getSource()).withFailMessage("propertyChange").isEqualTo(warrant);
//            System.out.println(warrant.getRunningMessage());
        }
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initRosterConfigManager();

        // setup the warrant preliminaries.
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        bEast = _OBlockMgr.createNewOBlock("OB2", "East");
        bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        bSouth = _OBlockMgr.createNewOBlock("OB4", "South");

        _portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal pNorthWest = _portalMgr.createNewPortal("NorthWest");
        pNorthWest.setToBlock(bWest, false);
        pNorthWest.setFromBlock(bNorth, false);
        Portal pSouthWest = _portalMgr.createNewPortal("SouthWest");
        pSouthWest.setToBlock(bWest, false);
        pSouthWest.setFromBlock(bSouth, false);

        Portal pNorthEast = _portalMgr.createNewPortal("NorthEast");
        pNorthEast.setToBlock(_OBlockMgr.getOBlock("OB2"), false);
        pNorthEast.setFromBlock(_OBlockMgr.getOBlock("North"), false);
        Portal pSouthEast = _portalMgr.createNewPortal("SouthEast");
        OBlock east = _OBlockMgr.getOBlock("OB2");
        pSouthEast.setToBlock(east, false);
        pSouthEast.setFromBlock(_OBlockMgr.getOBlock("South"), false);

        _turnoutMgr = InstanceManager.turnoutManagerInstance();
        Turnout northSwitch = _turnoutMgr.newTurnout("IT1", "NorthSwitch");
        ArrayList<BeanSetting> settings = new ArrayList<>();
        settings.add(new BeanSetting(northSwitch, "NorthSwitch", Turnout.CLOSED));
        OBlock north = _OBlockMgr.getOBlock("North");
        OPath path = new OPath("NorthToWest", north, null, _portalMgr.getPortal("NorthWest"), settings);
        north.addPath(path);

        settings = new ArrayList<>();
        settings.add(new BeanSetting(northSwitch, "NorthSwitch", Turnout.THROWN));
        path = new OPath("NorthToEast", north, null, _portalMgr.getPortal("NorthEast"), settings);
        north.addPath(path);

        Turnout southSwitch = _turnoutMgr.newTurnout("IT2", "SouthSwitch");
        OBlock south = _OBlockMgr.getOBlock("South");
        settings = new ArrayList<>();
        settings.add(new BeanSetting(southSwitch, "SouthSwitch", Turnout.THROWN));
        path = new OPath("SouthToEast", south, null, _portalMgr.getPortal("SouthEast"), settings);
        south.addPath(path);
        settings = new ArrayList<>();
        settings.add(new BeanSetting(southSwitch, "SouthSwitch", Turnout.CLOSED));
        path = new OPath("SouthToWest", south, null, south.getPortalByName("SouthWest"), settings);
        south.addPath(path);

        bSouth.setLength(100);

        settings = new ArrayList<>();
        OBlock block = _OBlockMgr.getOBlock("West");
        path = new OPath("SouthToNorth", block, _portalMgr.getPortal("NorthWest"), _portalMgr.getPortal("SouthWest"), settings);
        _OBlockMgr.getOBlock("West").addPath(path);
        path.setLength(200);
        settings = new ArrayList<>();
        block = _OBlockMgr.getOBlock("East");
        path = new OPath("NorthToSouth", block, south.getPortalByName("SouthEast"), north.getPortalByName("NorthEast"), settings);
        _OBlockMgr.getOBlock("East").addPath(path);

        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        sWest = _sensorMgr.newSensor("IS1", "WestSensor");
        sEast = _sensorMgr.newSensor("IS2", "EastSensor");
        sNorth = _sensorMgr.newSensor("IS3", "NorthSensor");
        sSouth = _sensorMgr.newSensor("IS4", "SouthSensor");
        bWest.setSensor("WestSensor");
        bEast.setSensor("IS2");
        bNorth.setSensor("NorthSensor");
        bSouth.setSensor("IS4");
        warrant = new Warrant("IW0", "AllTestWarrant");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
    }

    @AfterEach
    public void tearDown() {
        warrant.stopWarrant(true, true);
        _OBlockMgr = null;
        _portalMgr = null;
        _sensorMgr = null;
        _turnoutMgr = null;
        bWest = null;
        bEast = null;
        bNorth = null;
        bSouth = null;
        sWest = null;
        sEast = null;
        sNorth = null;
        sSouth = null;
        warrant = null;

        JUnitUtil.tearDown();
    }

}
