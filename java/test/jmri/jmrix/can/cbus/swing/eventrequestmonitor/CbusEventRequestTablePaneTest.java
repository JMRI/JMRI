package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CbusEventRequestTablePane
 *
 * @author Paul Bender Copyright (C) 2016
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
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        tcis.terminateThreads();
        memo = null;
        tcis = null;
        JUnitUtil.tearDown();
        
    }

}
