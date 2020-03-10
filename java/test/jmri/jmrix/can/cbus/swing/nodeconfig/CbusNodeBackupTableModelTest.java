package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusNodeBackupTableModel
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupTableModelTest {

    @Test
    public void testCtor() {
        t = new CbusNodeBackupTableModel(null);
        Assert.assertNotNull("exists",t);
    }
    
    private CbusNodeBackupTableModel t;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePaneTest.class);

}
