package jmri.jmrix.can.cbus.swing.eventrequestmonitor;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusEventRequestTablePane
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class CbusEventRequestTablePaneTest {

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;
    
    @Test
    public void testCtor() {
        CbusEventRequestTablePane t = new CbusEventRequestTablePane();
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testDisplayEventRequest() {
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
        Assertions.assertNotNull(memo);
        memo.dispose();
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        memo = null;
        tcis = null;
        JUnitUtil.tearDown();
        
    }

}
