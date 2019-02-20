package jmri.jmrix.can.cbus.eventtable;

// import jmri.jmrix.can.CanSystemConnectionMemo;
// import jmri.jmrix.can.TrafficControllerScaffold;

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
public class CbusEventTableActionTest {


    @Test
    public void testCTor() {
        
       // TrafficControllerScaffold tcis = new TrafficControllerScaffold();
      //  CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
      //  memo.setTrafficController(tcis);

        CbusEventTableAction t = new CbusEventTableAction(null);
        Assert.assertNotNull("exists",t);
        
        t = null;
      //  memo = null;
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

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableActionTest.class);

}
