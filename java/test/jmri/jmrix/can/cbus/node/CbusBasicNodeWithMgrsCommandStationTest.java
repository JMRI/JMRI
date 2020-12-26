package jmri.jmrix.can.cbus.node;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.PowerManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPowerManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusBasicNodeWithMgrsCommandStationTest {

    @Test
    public void testCTor() {
        t = new CbusBasicNodeWithMgrsCommandStation(null,123);
        assertThat(t).isNotNull();
        t.dispose();
    }
    
    @Test
    public void testGetSetFlagAccurate() {
    
        t = new CbusBasicNodeWithMgrsCommandStation(memo,124);
        assertThat(t.getStatResponseFlagsAccurate()).isFalse();
        
        assertThat(t.getCsNum()).isEqualTo(-1);
        t.setCsNum(7);
        assertThat(t.getCsNum()).isEqualTo(7);
        
        t.setStatResponseFlagsAccurate(true);
        assertThat(t.getStatResponseFlagsAccurate()).isTrue();
    
        t.dispose();
    }
    
    @Test
    public void testSetFlags() throws jmri.JmriException {
        
        CbusPowerManager pwr = (CbusPowerManager) memo.get(PowerManager.class);
        t = new CbusBasicNodeWithMgrsCommandStation(memo,125);
        t.setCsNum(0); // default CS
        t.setStatResponseFlagsAccurate(true);
        
        pwr.setPower(PowerManager.ON);
        
        t.setCsFlags(0b00000000);
        assertThat(pwr.getPower()).isEqualTo(PowerManager.OFF);
        
        t.setCsFlags(0b00000100);
        assertThat(pwr.getPower()).isEqualTo(PowerManager.ON);
        
        t.dispose();
    }
    
    private CbusBasicNodeWithMgrsCommandStation t;
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    
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
        
        memo.dispose();
        tcis.terminateThreads();
        memo = null;
        tcis = null;
        
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeWithMgrsCommandStationTest.class);

}
