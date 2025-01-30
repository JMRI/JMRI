package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusNodeBackupTableModel
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupTableModelTest {

    @Test
    public void testCtor() {
        t = new CbusNodeBackupTableModel(null);
        Assertions.assertNotNull(t);
    }

    private CbusNodeBackupTableModel t;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePaneTest.class);

}
