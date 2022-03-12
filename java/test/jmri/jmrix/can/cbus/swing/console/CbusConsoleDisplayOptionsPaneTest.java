package jmri.jmrix.can.cbus.swing.console;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CbusConsoleDisplayOptionsPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusConsoleDisplayOptionsPaneTest  {

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // for now, just makes sure there isn't an exception.
        CbusConsoleDisplayOptionsPane t = new CbusConsoleDisplayOptionsPane(mainConsolePane);
        Assert.assertNotNull("exists",t);
        
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tc;
    private CbusConsolePane mainConsolePane;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        mainConsolePane = new CbusConsolePane();
        mainConsolePane.initComponents(memo, false);
    }

    @AfterEach
    public void tearDown() {
        mainConsolePane.dispose();
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;
        
        JUnitUtil.tearDown();
    }

}
