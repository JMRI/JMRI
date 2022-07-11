/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.util;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test ConnectionUtil.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class ConnectionUtilTest {

    private LnTrafficController lnis1;
    private LnTrafficController lnis2;
    private LocoNetSystemConnectionMemo lnMemo1;
    private LocoNetSystemConnectionMemo lnMemo2;
    private MqttSystemConnectionMemo mqttMemo;

    @Test
    public void testConnectionUtil() {
        LocoNetSystemConnectionMemo myMemo1 =
                ConnectionUtil.getConnection("L", LocoNetSystemConnectionMemo.class);
        Assert.assertEquals(lnMemo1, myMemo1);

        LocoNetSystemConnectionMemo myMemo2 =
                ConnectionUtil.getConnection("L2", LocoNetSystemConnectionMemo.class);
        Assert.assertEquals(lnMemo2, myMemo2);

        LocoNetSystemConnectionMemo myMemo3 =
                ConnectionUtil.getConnection("L15", LocoNetSystemConnectionMemo.class);
        Assert.assertNull(myMemo3);

        MqttSystemConnectionMemo myMqttMemo1 =
                ConnectionUtil.getConnection("M", MqttSystemConnectionMemo.class);
        Assert.assertEquals(mqttMemo, myMqttMemo1);

        MqttSystemConnectionMemo myMqttMemo2 =
                ConnectionUtil.getConnection("L2", MqttSystemConnectionMemo.class);
        // This memo is not found since it's a LocoNet memo, not a Mqtt memo.
        Assert.assertNull(myMqttMemo2);

        MqttSystemConnectionMemo myMqttMemo3 =
                ConnectionUtil.getConnection("M3", MqttSystemConnectionMemo.class);
        Assert.assertNull(myMqttMemo3);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();

//        JUnitUtil.initInternalSignalHeadManager();
//        JUnitUtil.initDefaultSignalMastManager();
//        JUnitUtil.initSignalMastLogicManager();
//        JUnitUtil.initOBlockManager();
//        JUnitUtil.initWarrantManager();

//        JUnitUtil.initLogixNGManager();

        // The class under test uses LocoNet connections that it pulls from the InstanceManager.
        lnMemo1 = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis1 = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(lnMemo1);
        lnMemo1.setLnTrafficController(lnis1);
        lnMemo1.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false);
        lnMemo1.configureManagers();
        jmri.InstanceManager.store(lnMemo1, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        // The class under test uses LocoNet connections that it pulls from the InstanceManager.
        lnMemo2 = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo("L2", "LocoNet");
        lnis2 = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(lnMemo2);
        lnMemo2.setLnTrafficController(lnis2);
        lnMemo2.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false);
        lnMemo2.configureManagers();
        jmri.InstanceManager.store(lnMemo2, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        mqttMemo = new MqttSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        mqttMemo = null;
        lnMemo1 = null;
        lnMemo2 = null;
        lnis1 = null;
        lnis2 = null;

        // JUnitAppender.clearBacklog();    // REMOVE THIS!!!

//        JUnitUtil.removeMatchingThreads("LnPowerManager LnTrackStatusUpdateThread");
//        JUnitUtil.removeMatchingThreads("LnSensorUpdateThread");
//        JUnitUtil.removeMatchingThreads("LocoNetThrottledTransmitter");

//        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
//        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
