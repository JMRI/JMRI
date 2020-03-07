package jmri.jmrix.can.cbus.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusEventHighlightFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusEventHighlightFrameTest extends jmri.util.JmriJFrameTestBase{

    @Test
    public void testPaneCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusConsolePane pane = new CbusConsolePane();
        CbusEventHighlightFrame cbframe = new CbusEventHighlightFrame(pane,null);
        Assert.assertNotNull("exists", cbframe);
    }
    
    @Test
    public void testCanMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CanMessage m = new CanMessage(123,1);
        m.setElement(0, 1);
        Assert.assertEquals("No Highlight CanMessage by default",-1,t.highlight(m));
    
    }
    
    @Test
    public void testCanReply() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CanReply r = new CanReply(123);
        r.setNumDataElements(1);
        r.setElement(0, 1);
        Assert.assertEquals("No Highlight CanReply by default",-1,t.highlight(r));
    
    }
    
    @Test
    public void testEnableWithCanReplyAndConsole() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        
        CbusConsolePane pane = new CbusConsolePane();
        pane.initComponents(memo);
        CbusEventHighlightFrame cbframe = new CbusEventHighlightFrame(pane,null);
        
        Assert.assertTrue("Title set to console",cbframe.getTitle().startsWith("CAN CBUS Console "));
                
        CanReply r = new CanReply(123);
        r.setNumDataElements(1);
        r.setElement(0, 1);
        Assert.assertEquals("No Highlight CanReply by default",-1,cbframe.highlight(r));

        Assert.assertTrue("console starts empty", pane.monTextPaneCbus.getText().isEmpty());

        // highlight short event 1 On, either direction
        cbframe.enable(0, 0, true, 1, true, CbusConstants.EVENT_ON, CbusConstants.EVENT_DIR_EITHER);
        
        JUnitUtil.waitFor(()->{ return(!pane.monTextPaneCbus.getText().isEmpty()); }, "Change in Highlighter not passed to console");
        
        Assert.assertEquals("console updated","Node 0 Event 1 On Received by JMRI OR sent by JMRI\n",pane.monTextPaneCbus.getText());
        
        pane.dispose();
        memo.dispose();
    
    }
    
    @Test
    public void testColours() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull(t.getColor(0));
        Assert.assertNotNull(t.getColor(1));
        Assert.assertNotNull(t.getColor(2));
        Assert.assertNotNull(t.getColor(3));
    
    }
    
    private CbusEventHighlightFrame t;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = t = new CbusEventHighlightFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();    
    }

}
