package jmri.jmrix.can.cbus.swing;

import javax.swing.JFrame;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test simple functioning of CbusSendEventPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusSendEventPaneTest  {

    @Test
    public void testCbusSendEventPaneCtor() {
        CbusSendEventPane t = new CbusSendEventPane(mainConsolePane);
        assertNotNull(t);
    }

    @Test
    public void testSendEvents() {

        CbusSendEventPane t = new CbusSendEventPane(mainConsolePane);
        JFrame f = new JFrame();

        f.add(t);
        f.setTitle("Test CBUS Send Event");
        jmri.util.ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator( "Test CBUS Send Event" );

        new JTextFieldOperator(jfo,1).setText("1");

        new JRadioButtonOperator(jfo,Bundle.getMessage("InitialStateOff")).setSelected(true);
        new JButtonOperator(jfo,Bundle.getMessage("ButtonSend")).doClick();
        assertNotNull(tc);
        assertEquals( 1, tc.outbound.size());
        assertEquals("[5f8] 99 00 00 00 01",tc.outbound.get(0).toString());

        new JRadioButtonOperator(jfo,Bundle.getMessage("InitialStateOn")).setSelected(true);
        new JButtonOperator(jfo,Bundle.getMessage("ButtonSend")).doClick();
        assertEquals( 2, tc.outbound.size());
        assertEquals("[5f8] 98 00 00 00 01",tc.outbound.get(1).toString());

        new JRadioButtonOperator(jfo,Bundle.getMessage("CbusEventRequest")).setSelected(true);
        new JButtonOperator(jfo,Bundle.getMessage("ButtonSend")).doClick();
        assertEquals( 3, tc.outbound.size());
        assertEquals("[5f8] 9A 00 00 00 01",tc.outbound.get(2).toString());

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
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
        assertNotNull(mainConsolePane);
        mainConsolePane.dispose();
        assertNotNull(tc);
        tc.terminateThreads();
        assertNotNull(memo);
        memo.dispose();
        tc = null;
        memo = null;
        JUnitUtil.tearDown();
    }

}
