package jmri.jmrix.can.cbus.eventtable;

import jmri.jmrix.can.CanSystemConnectionMemo;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusBasicEventTableModelTest {

    @Test
    public void testCTor() {
        
        CbusBasicEventTableModel t = new CbusBasicEventTableModel(memo);
        assertThat(t).isNotNull();
        
    }

    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);
        memo.configureManagers();
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusBasicEventTableModelTest.class);

}
