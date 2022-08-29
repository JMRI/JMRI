package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test simple functioning of CbusSendEventPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CbusSendEventPaneTest  {

    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        CbusSendEventPane t = new CbusSendEventPane(mainConsolePane);
        assertThat(t).isNotNull();        
    }
    
    @Test
    public void testSendEvents() throws Exception{
        // for now, just makes sure there isn't an exception.
        CbusSendEventPane t = new CbusSendEventPane(mainConsolePane);
        JmriJFrame f = new JmriJFrame();
        
        f.add(t);
        f.setTitle("Test CBUS Send Event");
        f.pack();
        f.setVisible(true);
        
        JFrameOperator jfo = new JFrameOperator( "Test CBUS Send Event" );
        
        new JTextFieldOperator(jfo,1).setText("1");
        
        new JRadioButtonOperator(jfo,Bundle.getMessage("InitialStateOff")).setSelected(true);
        new JButtonOperator(jfo,Bundle.getMessage("ButtonSend")).doClick();
        Assertions.assertNotNull(tc);
        assertThat( tc.outbound.size()).isEqualTo(1);
        assertEquals("[5f8] 99 00 00 00 01",tc.outbound.get(0).toString());
        
        new JRadioButtonOperator(jfo,Bundle.getMessage("InitialStateOn")).setSelected(true);
        new JButtonOperator(jfo,Bundle.getMessage("ButtonSend")).doClick();
        assertThat( tc.outbound.size()).isEqualTo(2);
        assertEquals("[5f8] 98 00 00 00 01",tc.outbound.get(1).toString());
        
        new JRadioButtonOperator(jfo,Bundle.getMessage("CbusEventRequest")).setSelected(true);
        new JButtonOperator(jfo,Bundle.getMessage("ButtonSend")).doClick();
        assertThat( tc.outbound.size()).isEqualTo(3);
        assertEquals("[5f8] 9A 00 00 00 01",tc.outbound.get(2).toString());
        
        f.dispose();
    }
    
    
    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tc = null;
    private CbusConsolePane mainConsolePane = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        mainConsolePane = new CbusConsolePane();
        mainConsolePane.initComponents(memo,false);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(mainConsolePane);
        mainConsolePane.dispose();
        Assertions.assertNotNull(tc);
        tc.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        tc = null;
        memo = null;
        JUnitUtil.tearDown();
    }

}
