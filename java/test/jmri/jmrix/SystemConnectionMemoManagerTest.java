package jmri.jmrix;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test SystemConnectionMemoManager.
 *
 * @author Paul Bender       Copyright (C) 2017
 * @author Daniel Bergqvist  Copyright (C) 2022
 */
public class SystemConnectionMemoManagerTest {

    private LnTrafficController lnis1;
    private LnTrafficController lnis2;
    private LocoNetSystemConnectionMemo lnMemo1;
    private LocoNetSystemConnectionMemo lnMemo2;
    private MqttSystemConnectionMemo mqttMemo;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SystemConnectionMemoManager t = new SystemConnectionMemoManager();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testConnectionUtil() {
        LocoNetSystemConnectionMemo myMemo1 =
                SystemConnectionMemoManager.getConnection("L", LocoNetSystemConnectionMemo.class);
        Assert.assertEquals(lnMemo1, myMemo1);

        myMemo1 = SystemConnectionMemoManager.getConnectionByUserName("LocoNet", LocoNetSystemConnectionMemo.class);
        Assert.assertEquals(lnMemo1, myMemo1);

        LocoNetSystemConnectionMemo myMemo2 =
                SystemConnectionMemoManager.getConnection("L2", LocoNetSystemConnectionMemo.class);
        Assert.assertEquals(lnMemo2, myMemo2);

        myMemo2 = SystemConnectionMemoManager.getConnectionByUserName("LocoNet2", LocoNetSystemConnectionMemo.class);
        Assert.assertEquals(lnMemo2, myMemo2);

        LocoNetSystemConnectionMemo myMemo3 =
                SystemConnectionMemoManager.getConnection("L15", LocoNetSystemConnectionMemo.class);
        Assert.assertNull(myMemo3);

        myMemo3 = SystemConnectionMemoManager.getConnectionByUserName("L15", LocoNetSystemConnectionMemo.class);
        Assert.assertNull(myMemo3);

        MqttSystemConnectionMemo myMqttMemo1 =
                SystemConnectionMemoManager.getConnection("M", MqttSystemConnectionMemo.class);
        Assert.assertEquals(mqttMemo, myMqttMemo1);

        myMqttMemo1 = SystemConnectionMemoManager.getConnectionByUserName("MQTT", MqttSystemConnectionMemo.class);
        Assert.assertEquals(mqttMemo, myMqttMemo1);

        MqttSystemConnectionMemo myMqttMemo2 =
                SystemConnectionMemoManager.getConnection("L2", MqttSystemConnectionMemo.class);
        // This memo is not found since it's a LocoNet memo, not a Mqtt memo.
        Assert.assertNull(myMqttMemo2);

        myMqttMemo2 = SystemConnectionMemoManager.getConnectionByUserName("LocoNet2", MqttSystemConnectionMemo.class);
        // This memo is not found since it's a LocoNet memo, not a Mqtt memo.
        Assert.assertNull(myMqttMemo2);

        MqttSystemConnectionMemo myMqttMemo3 =
                SystemConnectionMemoManager.getConnection("M3", MqttSystemConnectionMemo.class);
        Assert.assertNull(myMqttMemo3);

        myMqttMemo3 = SystemConnectionMemoManager.getConnectionByUserName("MQTT3", MqttSystemConnectionMemo.class);
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

        // The class under test uses LocoNet connections that it pulls from the InstanceManager.
        lnMemo1 = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis1 = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(lnMemo1);
        lnMemo1.setLnTrafficController(lnis1);
        lnMemo1.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false, false);
        lnMemo1.configureManagers();
        jmri.InstanceManager.store(lnMemo1, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        // The class under test uses LocoNet connections that it pulls from the InstanceManager.
        lnMemo2 = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo("L2", "LocoNet2");
        lnis2 = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(lnMemo2);
        lnMemo2.setLnTrafficController(lnis2);
        lnMemo2.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false, false);
        lnMemo2.configureManagers();
        jmri.InstanceManager.store(lnMemo2, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        mqttMemo = new MqttSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        mqttMemo.dispose();
        mqttMemo = null;
        lnMemo1.dispose();
        lnMemo1 = null;
        lnMemo2.dispose();
        lnMemo2 = null;
        lnis1 = null;
        lnis2 = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemConnectionMemoManagerTest.class);

}
