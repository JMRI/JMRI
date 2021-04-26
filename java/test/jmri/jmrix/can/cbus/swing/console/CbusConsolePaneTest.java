package jmri.jmrix.can.cbus.swing.console;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusConsolePane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusConsolePaneTest extends jmri.util.swing.JmriPanelTest {

    @Override 
    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes ure there isn't an exception.
        ((CbusConsolePane) panel).initComponents(memo);
    }

    @Test
    public void testInitComponentsNoArgs() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // for now, just makes ure there isn't an exception.
        ((CbusConsolePane) panel).initComponents();
    }

    @Test
    public void testInitContext() throws Exception {
        // for now, just makes ure there isn't an exception.
        ((CbusConsolePane) panel).initContext(memo);
    }
    
    @Test
    public void testSendCanMessageCanReply() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        cbPanel.initComponents(memo);
        
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RTON);
        cbPanel.decodePane.message(m);
        
        CanReply r = new CanReply(tc.getCanid());
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_TON);
        cbPanel.decodePane.reply(r);
    
    }

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tc;
    private CbusConsolePane cbPanel;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        panel = cbPanel = new CbusConsolePane();
        helpTarget="package.jmri.jmrix.can.cbus.swing.console.CbusConsoleFrame";
        title="CBUS Console";
    }

    @AfterEach
    @Override
    public void tearDown() {
        
        CbusEventTableDataModel evMod = jmri.InstanceManager.getNullableDefault(CbusEventTableDataModel.class);
        if ( evMod != null){
            evMod.skipSaveOnDispose();
            evMod.dispose();
        }
        
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;
        
        JUnitUtil.tearDown();
    }


}
