package jmri.jmrix.can.cbus.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusEventHighlightFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CbusEventHighlightFrameTest extends jmri.util.JmriJFrameTestBase{

    @Test
    public void testPaneCtor() {
        CbusConsolePane pane = new CbusConsolePane();
        CbusEventHighlightFrame cbframe = new CbusEventHighlightFrame(pane,null);
        assertNotNull(cbframe);
    }
    
    @Test
    public void testCanMessage() {
        assertNotNull(t);
        CanMessage m = new CanMessage(123,1);
        m.setElement(0, 1);
        assertEquals(-1,t.highlight(m),"No Highlight CanMessage by default");
    
    }
    
    @Test
    public void testCanReply() {
        assertNotNull(t);
        CanReply r = new CanReply(123);
        r.setNumDataElements(1);
        r.setElement(0, 1);
        assertEquals(-1,t.highlight(r),"No Highlight CanReply by default");
    
    }
    
    @Test
    public void testEnableWithCanReplyAndConsole() {

        jmri.jmrix.can.TrafficControllerScaffold tc = new jmri.jmrix.can.TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);

        CbusConsolePane pane = new CbusConsolePane();
        pane.initComponents(memo,false);
        CbusEventHighlightFrame cbframe = new CbusEventHighlightFrame(pane,null);

        Assertions.assertTrue(cbframe.getTitle().startsWith("CAN CBUS Console "));

        CanReply r = new CanReply(123);
        r.setNumDataElements(1);
        r.setElement(0, 1);
        assertEquals(-1,cbframe.highlight(r),"No Highlight CanReply by default");

        Assertions.assertTrue(pane.monTextPaneCbus.getText().isEmpty());

        // highlight short event 1 On, either direction
        cbframe.enable(0, 0, true, 1, true, CbusConstants.EVENT_ON, CbusConstants.EVENT_DIR_EITHER);
        
        JUnitUtil.waitFor(()->{ return(!pane.monTextPaneCbus.getText().isEmpty()); }, "Change in Highlighter not passed to console");
        
        assertEquals("Node 0 Event 1 On Received by JMRI OR sent by JMRI\n",pane.monTextPaneCbus.getText(),"console updated");
        
        pane.dispose();
        tc.terminateThreads();
        memo.dispose();
    
    }
    
    @Test
    public void testColours() {
        assertNotNull(t);
        assertNotNull(t.getColor(0));
        assertNotNull(t.getColor(1));
        assertNotNull(t.getColor(2));
        assertNotNull(t.getColor(3));
    
    }

    private CbusEventHighlightFrame t = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        t = new CbusEventHighlightFrame();
        frame = t;
    }

}
