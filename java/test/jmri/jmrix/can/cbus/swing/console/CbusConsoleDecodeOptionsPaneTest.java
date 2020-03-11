package jmri.jmrix.can.cbus.swing.console;

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
 * Test simple functioning of CbusConsoleDecodeOptionsPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusConsoleDecodeOptionsPaneTest  {

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // for now, just makes sure there isn't an exception.
        CbusConsoleDecodeOptionsPane t = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        Assert.assertNotNull("exists",t);
        
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tc;
    private CbusConsolePane mainConsolePane;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        mainConsolePane = new CbusConsolePane();
        mainConsolePane.initComponents(memo);
    }

    @After
    public void tearDown() {
        
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;
        
        JUnitUtil.tearDown();
    }

}
