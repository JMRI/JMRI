package jmri.jmrix.can.cbus.eventtable;

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
public class CbusEventTableDataModelTest {

    TrafficControllerScaffold tcis;
    CanSystemConnectionMemo memo;
    CbusEventTableDataModel t;

    @Test
    public void testCTor() {

        Assert.assertNotNull("exists",t);


    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        
        t = new CbusEventTableDataModel(memo,4,CbusEventTableDataModel.MAX_COLUMN);
        
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        t = null;
        tcis = null;
        memo = null;
        
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableActionTest.class);

}
