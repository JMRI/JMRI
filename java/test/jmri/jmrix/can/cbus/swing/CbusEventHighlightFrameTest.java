package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;

/**
 * Test simple functioning of CbusEventHighlightFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusEventHighlightFrameTest extends jmri.util.JmriJFrameTestBase{

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testPaneCtor() {
        CbusConsolePane pane = new CbusConsolePane();
        CbusEventHighlightFrame cbframe = new CbusEventHighlightFrame(pane,null);
        assertThat(cbframe).isNotNull();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCanMessage() {
        CanMessage m = new CanMessage(123,1);
        m.setElement(0, 1);
        assertEquals(-1,t.highlight(m),"No Highlight CanMessage by default");
    
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCanReply() {
        CanReply r = new CanReply(123);
        r.setNumDataElements(1);
        r.setElement(0, 1);
        assertEquals(-1,t.highlight(r),"No Highlight CanReply by default");
    
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testEnableWithCanReplyAndConsole() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        
        CbusConsolePane pane = new CbusConsolePane();
        pane.initComponents(memo,false);
        CbusEventHighlightFrame cbframe = new CbusEventHighlightFrame(pane,null);
        
        assertThat(cbframe.getTitle().startsWith("CAN CBUS Console ")).isTrue();
                
        CanReply r = new CanReply(123);
        r.setNumDataElements(1);
        r.setElement(0, 1);
        assertEquals(-1,cbframe.highlight(r),"No Highlight CanReply by default");

        assertThat(pane.monTextPaneCbus.getText().isEmpty());

        // highlight short event 1 On, either direction
        cbframe.enable(0, 0, true, 1, true, CbusConstants.EVENT_ON, CbusConstants.EVENT_DIR_EITHER);
        
        JUnitUtil.waitFor(()->{ return(!pane.monTextPaneCbus.getText().isEmpty()); }, "Change in Highlighter not passed to console");
        
        assertEquals("Node 0 Event 1 On Received by JMRI OR sent by JMRI\n",pane.monTextPaneCbus.getText(),"console updated");
        
        pane.dispose();
        memo.dispose();
    
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testColours() {
        assertThat(t.getColor(0)).isNotNull();
        assertThat(t.getColor(1)).isNotNull();
        assertThat(t.getColor(2)).isNotNull();
        assertThat(t.getColor(3)).isNotNull();
    
    }
    
    private CbusEventHighlightFrame t;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
            t = new CbusEventHighlightFrame();
            frame = t;
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();    
    }

}
