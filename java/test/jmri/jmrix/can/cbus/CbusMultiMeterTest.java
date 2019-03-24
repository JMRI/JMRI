package jmri.jmrix.can.cbus;

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
 * @author Paul Bender Copyright (C) 2019
 */
public class CbusMultiMeterTest extends jmri.implementation.AbstractMultiMeterTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        mm = new CbusMultiMeter(memo);
    }
    
    @Test
    @Override
    public void testUpdateAndGetVoltage(){
        Assert.assertEquals("no voltage", false, mm.hasVoltage() );
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusMultiMeterTest.class);

}
