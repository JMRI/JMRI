package jmri.jmrix.can.cbus.node;

import jmri.PowerManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPowerManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusBasicNodeWithMgrsCommandStationTest {

    @Test
    public void testCTor() {
        t = new CbusBasicNodeWithMgrsCommandStation(null,123);
        assertNotNull(t);
        t.dispose();
    }

    @Test
    public void testGetSetFlagAccurate() {

        t = new CbusBasicNodeWithMgrsCommandStation(memo,124);
        assertFalse(t.getStatResponseFlagsAccurate());

        assertEquals( -1, t.getCsNum());
        t.setCsNum(7);
        assertEquals( 7, t.getCsNum());

        t.setStatResponseFlagsAccurate(true);
        assertTrue(t.getStatResponseFlagsAccurate());

        t.dispose();
    }

    @Test
    public void testSetFlags() throws jmri.JmriException {
        assertNotNull(memo);
        CbusPowerManager pwr = (CbusPowerManager) memo.get(PowerManager.class);
        t = new CbusBasicNodeWithMgrsCommandStation(memo,125);
        t.setCsNum(0); // default CS
        t.setStatResponseFlagsAccurate(true);

        pwr.setPower(PowerManager.ON);

        t.setCsFlags(0b00000000);
        assertEquals( PowerManager.OFF, pwr.getPower());

        t.setCsFlags(0b00000100);
        assertEquals( PowerManager.ON, pwr.getPower());

        t.dispose();
    }

    private CbusBasicNodeWithMgrsCommandStation t;
    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();
    }

    @AfterEach
    public void tearDown() {
        assertNotNull(memo);
        memo.dispose();
        assertNotNull(tcis);
        tcis.terminateThreads();
        memo = null;
        tcis = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeWithMgrsCommandStationTest.class);

}
