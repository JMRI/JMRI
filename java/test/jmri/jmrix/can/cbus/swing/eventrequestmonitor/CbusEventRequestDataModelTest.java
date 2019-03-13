package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
// import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigToolPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventRequestDataModelTest {

    @Test
    public void testCtor() {
        // Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusEventRequestDataModel t = new CbusEventRequestDataModel(memo,0,0);
        Assert.assertNotNull("exists", t);
        
        t = null;
        tcis = null;
        memo = null;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
