package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;
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
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCanFrames(){
        frame.initComponents();
        assertThat(frame).isNotNull();
        
        frame.setVisible(true);
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        
        CbusFilterFrame cff = (CbusFilterFrame) frame;
        
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RTON);
        assertThat(cff.filter(m)).isFalse();
        
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_TON);
        assertThat(cff.filter(r)).isFalse();
        
        new JToggleButtonOperator(jfo,0).clickMouse(); // Filter Incoming
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>1); }, "Not all increments passed" + stringOutputList.size());
        assertEquals("Filter Window Active \n",stringOutputList.get(0),"Filter Active sent to console");
        assertEquals("Incoming: Filter \n",stringOutputList.get(1),"Filter Change sent to console");
        
        assertThat(cff.filter(r)).isTrue();
        assertThat(cff.filter(m)).isFalse();
        
        new JToggleButtonOperator(jfo,0).clickMouse(); // Pass Incoming
        new JToggleButtonOperator(jfo,1).clickMouse(); // Filter Outgoing
        
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>3); }, "Not all increments passed" + stringOutputList.size());
        assertEquals("Incoming: Pass \n",stringOutputList.get(2),"Filter Active sent to console");
        assertEquals("Outgoing: Filter \n",stringOutputList.get(3),"Filter Change sent to console");
               
        assertThat(cff.filter(m)).isTrue();
        assertThat(cff.filter(r)).isFalse();
        
        new JToggleButtonOperator(jfo,2).clickMouse(); // event children
        
        new JToggleButtonOperator(jfo,3).clickMouse(); // All Events Filtered
        
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>4); }, "Not all increments passed " + stringOutputList.size());
        
        assertEquals("Filter ( 0 / 0 ) ",
            new JToggleButtonOperator(jfo,4).getText(),"text says filter");
        
        new JToggleButtonOperator(jfo,3).clickMouse(); // All Events Passed
        
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>5); }, "Not all increments passed " + stringOutputList.size());
        
        
        assertEquals("Pass ( 0 / 0 ) ",
            new JToggleButtonOperator(jfo,4).getText(),"text says pass");
        
        
        JSpinnerOperator spinner = new JSpinnerOperator(jfo, 0);
            
        JTextFieldOperator jtfo = new JTextFieldOperator(spinner);
        assertEquals( "0", jtfo.getText(),"original Min Event 0");
        
        jtfo.setText("123");
        new JToggleButtonOperator(jfo,4).clickMouse(); // Min Event Filter
        JUnitUtil.waitFor(()->{ return(stringOutputList.size()>6); }, "Not all increments passed " + stringOutputList.size());

        
        assertEquals("Mixed ( 0 / 0 ) ",
            new JToggleButtonOperator(jfo,3).getText(),"text says mixed");
        
        
        // Outgoing event
        
        CanReply mEvent = new CanReply(0x12);
        mEvent.setNumDataElements(5);
        mEvent.setElement(0, CbusConstants.CBUS_ACON);
        mEvent.setElement(1, 0x01); // Node 257
        mEvent.setElement(2, 0x01); // Node 257
        mEvent.setElement(3, 0x00); // Event > 123
        mEvent.setElement(4, 0xff); // Event > 123

        assertThat(cff.filter(mEvent)).isFalse();
        
        assertEquals("Filter ( 1 / 0 ) ",
            new JToggleButtonOperator(jfo,4).getText(),"text says pass");
        
        mEvent.setElement(4, 0x01); // Event < 123
        
        assertThat(cff.filter(mEvent)).isTrue();
        assertEquals("Filter ( 1 / 1 ) ",
            new JToggleButtonOperator(jfo,4).getText(),"text says pass");
        
        frame.dispose();
        
        // JemmyUtil.pressButton(jfo,("Pause Test"));
        
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
            stringOutputList = new ArrayList<>();
            _testConsole = new FtTestConsole();
            frame = new CbusFilterFrame(_testConsole,null);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if(!GraphicsEnvironment.isHeadless() &&_testConsole !=null){
            _testConsole.dispose();
        }
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventFilterTest.class);

}
