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
public class CbusNodeSingleEventTableDataModelTest {

    @Test
    public void testCTor() {
        
        CbusNodeEvent ev = new CbusNodeEvent(0,1,0,0,0);
        
        CbusNodeSingleEventTableDataModel nodeModel = new CbusNodeSingleEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,ev);
        
        Assert.assertNotNull("exists",nodeModel);
        
        nodeModel.dispose();
        ev = null;
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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeSingleEventTableDataModelTest.class);

}
