package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
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
public class CbusNodeTableDataModelTest {

    @Test
    public void testCTor() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusNodeTableDataModel nodeModel = new CbusNodeTableDataModel(
            memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        
        Assert.assertNotNull("exists",nodeModel);
        
        nodeModel.dispose();
        
        nodeModel = null;
        tcis = null;
        memo = null;
        
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

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeTableDataModelTest.class);

}
