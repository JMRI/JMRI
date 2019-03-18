package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeFromFcuTableDataModelTest {

    @Test
    public void testCTor() {
        
        CbusNodeFromFcuTableDataModel nodeModel = new CbusNodeFromFcuTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeFromFcuTableDataModel.MAX_COLUMN);
        
        Assert.assertNotNull("exists",nodeModel);
        
        nodeModel.dispose();
        
        nodeModel = null;
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFromFcuTableDataModelTest.class);

}
