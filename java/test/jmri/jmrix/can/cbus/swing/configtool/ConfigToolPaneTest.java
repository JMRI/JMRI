package jmri.jmrix.can.cbus.swing.configtool;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;
/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class ConfigToolPaneTest extends jmri.util.swing.JmriPanelTest {

    private CanSystemConnectionMemo memo; 
    private TrafficControllerScaffold tcis;
 
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        title = Bundle.getMessage("CapConfigTitle");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.configtool.ConfigToolFrame";
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        panel = new ConfigToolPane();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
        Assert.assertEquals("no listener after dispose",0,tcis.numListeners());
        tcis = null;
        memo = null;
    }
    
    @Test
    public void testInitComp() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertEquals("no listener to start with",0,tcis.numListeners());
        
        ((ConfigToolPane)panel).initComponents(memo);
        
        Assert.assertEquals("listening",1,tcis.numListeners());
        
        Assert.assertNotNull("exists", panel);
        Assert.assertEquals("name with memo","CAN " + Bundle.getMessage("CapConfigTitle"),panel.getTitle());
        
        
        // check pane has loaded something
        JmriJFrame f = new JmriJFrame();
        f.add(panel);
        f.setTitle(panel.getTitle());
        
        f.pack();
        f.setVisible(true);
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );
        
        Assert.assertTrue(getResetButtonEnabled(jfo));
        
        Assert.assertEquals("nothing in capture slot 1","",getStringCaptureOne(jfo) );
        Assert.assertEquals("nothing in capture slot 2","",getStringCaptureTwo(jfo) );
        
        CanMessage m = new CanMessage(tcis.getCanid());
        m.setNumDataElements(5);
        m.setElement(0, 0x91); // ACOF OPC
        m.setElement(1, 0xd4); // nn 
        m.setElement(2, 0x31); // nn 
        m.setElement(3, 0x30); // en 
        m.setElement(4, 0x39); // en 
        
        ((ConfigToolPane)panel).message(m);
        Assert.assertEquals("event in capture slot 1","-n54321e12345",getStringCaptureOne(jfo) );
        
        CanReply r = new CanReply(tcis.getCanid());
        r.setNumDataElements(5);
        r.setElement(0, 0x98); // ASON OPC
        r.setElement(1, 0x00); // nn 0
        r.setElement(2, 0x00); // nn 0
        r.setElement(3, 0xff); // en 
        r.setElement(4, 0x39); // en 
        
        ((ConfigToolPane)panel).reply(r);
        Assert.assertEquals("event in capture slot 2","+65337",getStringCaptureTwo(jfo) );

        // Ask to close window
        jfo.requestClose(); 
    }
    
    private boolean getResetButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ButtonResetCapture")).isEnabled() );
    }
    
    private String getStringCaptureOne( JFrameOperator jfo ){
        return ( new JTextFieldOperator(jfo,0).getText() );
    }
    
    private String getStringCaptureTwo( JFrameOperator jfo ){
        return ( new JTextFieldOperator(jfo,1).getText() );
    }

    // private final static Logger log = LoggerFactory.getLogger(ConfigToolPaneTest.class);

}
