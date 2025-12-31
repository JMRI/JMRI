package jmri.jmrit.logix;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
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
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder( bNorth, "NorthToWest", "", "NorthWest"));
        BlockOrder viaOrder = new BlockOrder( bWest, "SouthToNorth", "NorthWest", "SouthWest");
        orders.add(viaOrder);
        BlockOrder lastOrder = new BlockOrder( bSouth, "SouthToWest", "SouthWest", null);
        orders.add(lastOrder);

        assertThat(((SCWarrant) warrant).isRouteFree()).withFailMessage("Route Free").isTrue();
        assertThat(((SCWarrant) warrant).isRouteAllocated()).withFailMessage("Route Allocated").isTrue();
        assertThat(orders.size()).withFailMessage("Order size not 3").isEqualTo(3);
        // TODO: use orders in test?
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
        orders.add(new BlockOrder( bNorth, "NorthToWest", "", "NorthWest"));
        BlockOrder viaOrder = new BlockOrder( bWest, "SouthToNorth", "NorthWest", "SouthWest");
        orders.add(viaOrder);
        BlockOrder lastOrder = new BlockOrder( bSouth, "SouthToWest", "SouthWest", null);
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
        warrant.setBlockOrders(orders);
        warrant.setRoute(false, orders);
        warrant.checkStartBlock();
        warrant.checkRoute();

        warrant.setTrainName("TestTrain");
        PropertyChangeListener listener = new WarrantListener(warrant);
        warrant.addPropertyChangeListener(listener);

        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        Assertions.assertNull( msg, "setRunMode - " + msg);

        assertThat(((SCWarrant) warrant).inStartBlock()).withFailMessage("in start block").isTrue();

        JUnitUtil.waitFor(() -> {
            String m = warrant.getRunningMessage();
            return m.endsWith("IH1 showing appearance 16");
        }, "Train starts to move after 2nd command");
        JUnitUtil.waitFor(100); // What should we specifically waitFor?

        // confirm one message logged
        //jmri.util.JUnitAppender.assertWarnMessage("Path NorthToWest in block North has length zero. Cannot run NXWarrants or ramp speeds through blocks with zero length.");
        ThreadingUtil.runOnLayoutWithJmriException( () ->
            sWest.setState(Sensor.ACTIVE));
        JUnitUtil.waitFor(100); // What should we specifically waitFor?

        ThreadingUtil.runOnLayoutWithJmriException( () ->
            sWest.setState(Sensor.INACTIVE));
        JUnitUtil.waitFor(100); // What should we specifically waitFor?

        ThreadingUtil.runOnLayoutWithJmriException( () ->
            sSouth.setState(Sensor.ACTIVE));
        JUnitUtil.waitFor(100); // What should we specifically waitFor?

        // wait for done
        JUnitUtil.waitFor(() -> {
            return warrant.getRunningMessage().equals("Idle");
        }, "warrant not done");
    }

    @BeforeEach
    @Override
    public void setUp(@TempDir File tempDir) throws IOException  {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));

        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();

        // setup the sc warrant preliminaries.
        WarrantPreferences.getDefault().setSpeedAssistance(0);
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        bEast = _OBlockMgr.createNewOBlock("OB2", "East");
        bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        bSouth = _OBlockMgr.createNewOBlock("OB4", "South");

        assertNotNull(bWest);
        assertNotNull(bEast);
        assertNotNull(bNorth);
        assertNotNull(bSouth);

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

        OPath path = new OPath("NorthToWest", bNorth, null, _portalMgr.getPortal("NorthWest"), settings);
        bNorth.addPath(path);

        settings = new ArrayList<>();
        settings.add(new BeanSetting(northSwitch, "NorthSwitch", Turnout.THROWN));
        path = new OPath("NorthToEast", bNorth, null, _portalMgr.getPortal("NorthEast"), settings);
        bNorth.addPath(path);

        Turnout southSwitch = _turnoutMgr.newTurnout("IT2", "SouthSwitch");

        settings = new ArrayList<>();
        settings.add(new BeanSetting(southSwitch, "SouthSwitch", Turnout.THROWN));
        path = new OPath("SouthToEast", bSouth, null, _portalMgr.getPortal("SouthEast"), settings);
        bSouth.addPath(path);
        settings = new ArrayList<>();
        settings.add(new BeanSetting(southSwitch, "SouthSwitch", Turnout.CLOSED));
        path = new OPath("SouthToWest", bSouth, null, bSouth.getPortalByName("SouthWest"), settings);
        bSouth.addPath(path);

        bSouth.setLength(100);

        settings = new ArrayList<>();

        path = new OPath("SouthToNorth", bWest, _portalMgr.getPortal("NorthWest"), _portalMgr.getPortal("SouthWest"), settings);
        bWest.addPath(path);
        path.setLength(200);
        settings = new ArrayList<>();

        path = new OPath("NorthToSouth", bEast, bSouth.getPortalByName("SouthEast"), bNorth.getPortalByName("NorthEast"), settings);
        bEast.addPath(path);

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

    @AfterEach
    @Override
    public void tearDown() {
        _turnoutMgr.dispose();
        _turnoutMgr = null;
        _OBlockMgr.dispose();
        _OBlockMgr = null;
        _sensorMgr.dispose();
        _sensorMgr = null;
        //JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SCWarrantTest.class);
}
