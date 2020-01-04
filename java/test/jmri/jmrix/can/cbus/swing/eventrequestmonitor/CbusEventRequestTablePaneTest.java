package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusEventRequestTablePane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventRequestTablePaneTest {

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusEventRequestTablePane t = new CbusEventRequestTablePane();
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testDisplayEventRequest() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusEventRequestTablePane t = new CbusEventRequestTablePane();
        t.initComponents(memo);
        
        Assert.assertEquals("title","CAN CBUS Event Request Monitor",t.getTitle());
        
    }
    
    

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
    }

    @After
    public void tearDown() { 
        memo = null;
        tcis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
        
    }


}
