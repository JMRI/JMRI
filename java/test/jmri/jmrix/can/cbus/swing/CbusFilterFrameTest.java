package jmri.jmrix.can.cbus.swing;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusFilterFrameTest extends jmri.util.JmriJFrameTestBase {

    private class FtTestConsole extends jmri.jmrix.can.cbus.swing.console.CbusConsolePane {
        
        public FtTestConsole() {
            super();
        }
        
        @Override
        public void nextLine(String lineOne, String lineTwo, int filterId){ 
            stringOutputList.add(lineOne);
        }
        
    }
    
    private FtTestConsole _testConsole;
    private ArrayList<String> stringOutputList;
    
    @Test
    public void testCanFrames(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.initComponents();
        Assert.assertNotNull(frame);
        
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        
        CbusFilterFrame cff = (CbusFilterFrame) frame;
        
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RTON);
        Assert.assertFalse(cff.filter(m));
        
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_TON);
        Assert.assertFalse(cff.filter(r));
        
        new JToggleButtonOperator(jfo,0).clickMouse(); // Filter Incoming
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>1); }, "Not all increments passed" + stringOutputList.size());
        Assert.assertEquals("Filter Active sent to console","Filter Window Active \n",stringOutputList.get(0));
        Assert.assertEquals("Filter Change sent to console","Incoming: Filter \n",stringOutputList.get(1));
        
        Assert.assertTrue(cff.filter(r));
        Assert.assertFalse(cff.filter(m));
        
        new JToggleButtonOperator(jfo,0).clickMouse(); // Pass Incoming
        new JToggleButtonOperator(jfo,1).clickMouse(); // Filter Outgoing
        
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>3); }, "Not all increments passed" + stringOutputList.size());
        Assert.assertEquals("Filter Active sent to console","Incoming: Pass \n",stringOutputList.get(2));
        Assert.assertEquals("Filter Change sent to console","Outgoing: Filter \n",stringOutputList.get(3));
               
        Assert.assertTrue(cff.filter(m));
        Assert.assertFalse(cff.filter(r));
        
        new JToggleButtonOperator(jfo,2).clickMouse(); // event children
        
        new JToggleButtonOperator(jfo,3).clickMouse(); // All Events Filtered
        
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>4); }, "Not all increments passed " + stringOutputList.size());
        
        Assert.assertEquals("text says filter","Filter ( 0 / 0 ) ",
            new JToggleButtonOperator(jfo,4).getText());
        
        new JToggleButtonOperator(jfo,3).clickMouse(); // All Events Passed
        
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>5); }, "Not all increments passed " + stringOutputList.size());
        
        
        Assert.assertEquals("text says pass","Pass ( 0 / 0 ) ",
            new JToggleButtonOperator(jfo,4).getText());
        
        
        JSpinnerOperator spinner = new JSpinnerOperator(jfo, 0);
            
        JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
        Assert.assertEquals("original Min Event 0", "0", jtfo.getText());
        
        jtfo.setText("123");
        new JToggleButtonOperator(jfo,4).clickMouse(); // Min Event Filter
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>6); }, "Not all increments passed " + stringOutputList.size());

        
        Assert.assertEquals("text says mixed","Mixed ( 0 / 0 ) ",
            new JToggleButtonOperator(jfo,3).getText());
        
        
        // Outgoing event
        
        CanReply mEvent = new CanReply(0x12);
        mEvent.setNumDataElements(5);
        mEvent.setElement(0, CbusConstants.CBUS_ACON);
        mEvent.setElement(1, 0x01); // Node 257
        mEvent.setElement(2, 0x01); // Node 257
        mEvent.setElement(3, 0x00); // Event > 123
        mEvent.setElement(4, 0xff); // Event > 123

        Assert.assertFalse(cff.filter(mEvent));
        
        Assert.assertEquals("text says pass","Filter ( 1 / 0 ) ",
            new JToggleButtonOperator(jfo,4).getText());
        
        mEvent.setElement(4, 0x01); // Event < 123
        
        Assert.assertTrue(cff.filter(mEvent));
        Assert.assertEquals("text says pass","Filter ( 1 / 1 ) ",
            new JToggleButtonOperator(jfo,4).getText());
        
        frame.dispose();
        
        // JemmyUtil.pressButton(jfo,("Pause Test"));
        
    }
    
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
            stringOutputList = new ArrayList<>();
            _testConsole = new FtTestConsole();
            frame = new CbusFilterFrame(_testConsole,null);
        }
    }

    @After
    @Override
    public void tearDown() {
        if(!GraphicsEnvironment.isHeadless() &&_testConsole !=null){
            _testConsole.dispose();
        }
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventFilterTest.class);

}
