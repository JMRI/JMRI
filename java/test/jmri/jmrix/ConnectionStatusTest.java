package jmri.jmrix;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ConnectionStatus
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionStatusTest {

    private LnTrafficController lnis1;
    private LnTrafficController lnis2;
    private LocoNetSystemConnectionMemo lnMemo1;
    private LocoNetSystemConnectionMemo lnMemo2;

    @Test
    public void testInstance() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertNotNull("exists", cs);
    }

    @Test
    public void test2ParamterGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UNKNOWN, cs.getConnectionState(lnMemo1));
    }

    @Test
    public void testAddAnd2ParameterGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.addConnection(lnMemo1);
        // set the status of the new connection so we know we are not
        // retreiving a new value.
        cs.setConnectionState(lnMemo1, ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UP, cs.getConnectionState(lnMemo1));
    }

    @Test
    public void test2ParameterSetAndGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(lnMemo1, ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UP, cs.getConnectionState(lnMemo1));
    }

    @Test
    public void test2ParamterIsConnectionOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(lnMemo1, ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk(lnMemo1));
        cs.setConnectionState(lnMemo1, ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isConnectionOk(lnMemo1));
    }

    @Test
    public void testIsSystemOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(lnMemo1, ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isSystemOk("Foo"));
        cs.setConnectionState(lnMemo1, ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isSystemOk("Foo"));
    }

    @Test
    public void testIsUnrecognizedSystemOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertTrue("connection OK", cs.isConnectionOk(lnMemo1));
    }

    @Test
    public void testGetSateForSystemName() {
        ConnectionStatus cs = ConnectionStatus.instance();
//        cs.addConnection(lnMemo1);
        // set the status of the new connection so we know we are not
        // retreiving a new value.
        cs.setConnectionState(lnMemo1, ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk(lnMemo1));
    }

    @Test
    public void testIsConnectionOkWNull() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(lnMemo2, ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk(lnMemo2));
        cs.setConnectionState(lnMemo2, ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isConnectionOk(lnMemo2));
    }

    @BeforeEach
    public void setUp() {
        ConnectionStatus.clearInstance();
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
    }

    @AfterEach
    public void tearDown() {
        lnMemo1.dispose();
        lnMemo1 = null;
        lnMemo2.dispose();
        lnMemo2 = null;
        lnis1 = null;
        lnis2 = null;

        JUnitUtil.tearDown();
    }

}
