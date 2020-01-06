package jmri.jmrit.logix;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.Turnout;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SCWarrantTest extends WarrantTest {

    @Test
    public void testIsRouteFree() throws JmriException {
        sEast.setState(Sensor.INACTIVE);
        sWest.setState(Sensor.INACTIVE);
        sSouth.setState(Sensor.INACTIVE);
        sNorth.setState(Sensor.ACTIVE);     // start block of warrant
        // TODO: use orders in test?
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(_OBlockMgr.getOBlock("North"), "NorthToWest", "", "NorthWest"));
        BlockOrder viaOrder = new BlockOrder(_OBlockMgr.getOBlock("West"), "SouthToNorth", "NorthWest", "SouthWest");
        orders.add(viaOrder);
        BlockOrder lastOrder = new BlockOrder(_OBlockMgr.getOBlock("South"), "SouthToWest", "SouthWest", null);
        orders.add(lastOrder);

        Assert.assertTrue("Route Free", ((SCWarrant) warrant).isRouteFree());
        Assert.assertTrue("Route Allocated", ((SCWarrant) warrant).isRouteAllocated());
    }

    @Test
    @Override
    public void testWarrant() throws JmriException {
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        sEast.setState(Sensor.INACTIVE);
        sWest.setState(Sensor.INACTIVE);
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

        warrant.getSpeedUtil().setDccAddress("999(L)");
        warrant.setBlockOrders(orders);
        warrant.setRoute(false, orders);
        warrant.checkStartBlock();
        warrant.checkRoute();
        SpeedUtil su = warrant.getSpeedUtil();
        su.setOrders(orders);

        warrant.setTrainName("TestTrain");
        PropertyChangeListener listener = new WarrantListener(warrant);
        warrant.addPropertyChangeListener(listener);

        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        Assert.assertNull("setRunMode - " + msg, msg);

        Assert.assertTrue("in start block", ((SCWarrant) warrant).inStartBlock());

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = warrant.getRunningMessage();
            return m.endsWith("IH1 showing appearance 16");
        }, "Train starts to move after 2nd command");
        jmri.util.JUnitUtil.releaseThread(this, 100); // What should we specifically waitFor?

        // confirm one message logged
        //jmri.util.JUnitAppender.assertWarnMessage("Path NorthToWest in block North has length zero. Cannot run NXWarrants or ramp speeds through blocks with zero length.");
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
                sWest.setState(Sensor.INACTIVE);
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

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initRosterConfigManager();

        // setup the sc warrant preliminaries.
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        bEast = _OBlockMgr.createNewOBlock("OB2", "East");
        bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        bSouth = _OBlockMgr.createNewOBlock("OB4", "South");

        SignalHeadManager shMgr = InstanceManager.getDefault(SignalHeadManager.class);
        SignalHead shNW = new VirtualSignalHead("IH1");
        shNW.setAppearance(SignalHead.GREEN);
        shMgr.register(shNW);
        SignalHead shSW = new VirtualSignalHead("IH2");
        shSW.setAppearance(SignalHead.GREEN);
        shMgr.register(shSW);
        SignalHead shNE = new VirtualSignalHead("IH3");
        shNE.setAppearance(SignalHead.GREEN);
        shMgr.register(shNE);
        SignalHead shSE = new VirtualSignalHead("IH4");
        shSE.setAppearance(SignalHead.GREEN);
        shMgr.register(shSE);

        _portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal pNorthWest = _portalMgr.createNewPortal("NorthWest");
        pNorthWest.setToBlock(bWest, false);
        pNorthWest.setFromBlock(bNorth, false);
        pNorthWest.setProtectSignal(shNW, 20, bWest);
        Portal pSouthWest = _portalMgr.createNewPortal("SouthWest");
        pSouthWest.setToBlock(bWest, false);
        pSouthWest.setFromBlock(bSouth, false);
        pSouthWest.setProtectSignal(shSW, 20, bWest);

        Portal pNorthEast = _portalMgr.createNewPortal("NorthEast");
        pNorthEast.setToBlock(bEast, false);
        pNorthEast.setFromBlock(bNorth, false);
        pNorthEast.setProtectSignal(shNE, 20, bEast);
        Portal pSouthEast = _portalMgr.createNewPortal("SouthEast");
        pSouthEast.setToBlock(bEast, false);
        pSouthEast.setFromBlock(bSouth, false);
        pSouthEast.setProtectSignal(shSE, 20, bEast);

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
        warrant = new SCWarrant("IW1", "SCWarrant test", 5);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SCWarrantTest.class);
}
