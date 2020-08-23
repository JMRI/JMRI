package jmri.jmrix.can.cbus.swing.nodeconfig;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertThat(t).isNotNull();
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
