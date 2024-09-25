package jmri.jmrix.can.cbus.swing.console;

import java.io.File;

import javax.swing.JFrame;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.ThreadingUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test simple functioning of CbusConsoleDisplayOptionsPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusConsoleDisplayOptionsPaneTest  {

    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        CbusConsoleDisplayOptionsPane t = new CbusConsoleDisplayOptionsPane(mainConsolePane);
        assertNotNull(t);
        
    }

    @Test
    public void testCheckBoxes() {
        JFrame f = new JFrame("CbusConsoleDisplayOptionsPaneTest.testCheckBoxes");
        f.add(mainConsolePane);
        f.pack();
        ThreadingUtil.runOnGUI(() -> f.setVisible(true));
        JFrameOperator jfo = new JFrameOperator( f.getTitle());

        JCheckBoxOperator logCbo = new JCheckBoxOperator(jfo,Bundle.getMessage("Logging"));
        Assertions.assertNotNull(logCbo);
        Assertions.assertFalse(logCbo.isSelected());
        logCbo.doClick();
        logCbo.getQueueTool().waitEmpty();
        JButtonOperator startLoggingButton = new JButtonOperator(jfo, Bundle.getMessage("ButtonAddMessage"));
        assertNotNull(startLoggingButton);
        Assertions.assertTrue(logCbo.isSelected());
        logCbo.doClick(); // and hide again
        logCbo.getQueueTool().waitEmpty();
        Assertions.assertFalse(logCbo.isSelected());

        JCheckBoxOperator statsCbo = new JCheckBoxOperator(jfo,Bundle.getMessage("StatisticsTitle"));
        Assertions.assertNotNull(statsCbo);
        Assertions.assertFalse(statsCbo.isSelected());
        statsCbo.doClick();
        statsCbo.getQueueTool().waitEmpty();
        JButtonOperator resetStatsButton = new JButtonOperator(jfo, Bundle.getMessage("ButtonClear"));
        assertNotNull(resetStatsButton);
        statsCbo.doClick(); // and hide again
        statsCbo.getQueueTool().waitEmpty();
        Assertions.assertFalse(statsCbo.isSelected());

        JCheckBoxOperator packetsCbo = new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowPackets"));
        Assertions.assertNotNull(packetsCbo);
        Assertions.assertFalse(packetsCbo.isSelected());
        packetsCbo.doClick();
        packetsCbo.getQueueTool().waitEmpty();
        JButtonOperator copyButton = new JButtonOperator(jfo, Bundle.getMessage("ButtonCopy"));
        assertNotNull(copyButton);
        packetsCbo.doClick(); // and hide again
        packetsCbo.getQueueTool().waitEmpty();
        Assertions.assertFalse(packetsCbo.isSelected());

        JCheckBoxOperator sendCbo = new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonSendEvent"));
        Assertions.assertNotNull(sendCbo);
        Assertions.assertFalse(sendCbo.isSelected());
        sendCbo.doClick();
        sendCbo.getQueueTool().waitEmpty();
        JButtonOperator sendButton = new JButtonOperator(jfo, Bundle.getMessage("ButtonSend"));
        assertNotNull(sendButton);
        sendCbo.doClick(); // and hide again
        sendCbo.getQueueTool().waitEmpty();
        Assertions.assertFalse(statsCbo.isSelected());        

        JUnitUtil.dispose(f);
        jfo.waitClosed();
    }

    @Test
    public void testCheckBoxPersistence() {
        
        JFrame f = new JFrame("CbusConsoleDisplayOptionsPaneTest.setCheckBoxPersistence");
        f.add(mainConsolePane);
        f.pack();
        ThreadingUtil.runOnGUI(() -> f.setVisible(true));
        JFrameOperator jfo = new JFrameOperator( f.getTitle());
        
        new JCheckBoxOperator(jfo,Bundle.getMessage("Logging")).doClick();
        new JCheckBoxOperator(jfo,Bundle.getMessage("StatisticsTitle")).doClick();
        new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowPackets")).doClick();
        new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonSendEvent")).doClick();
        jfo.getQueueTool().waitEmpty();

        mainConsolePane.dispose();
        JUnitUtil.dispose(f);
        jfo.waitClosed();

        CbusConsolePane newMainConsolePane = new CbusConsolePane();
        newMainConsolePane.initComponents(memo, false);

        JFrame ff = new JFrame("CbusConsoleDisplayOptionsPaneTest.getCheckBoxPersistence");
        ff.add(newMainConsolePane);
        ff.pack();
        ThreadingUtil.runOnGUI(() -> ff.setVisible(true));
        JFrameOperator jffo = new JFrameOperator( ff.getTitle());

        assertTrue(new JCheckBoxOperator(jffo,Bundle.getMessage("Logging")).isSelected());
        assertTrue(new JCheckBoxOperator(jffo,Bundle.getMessage("StatisticsTitle")).isSelected());
        assertTrue(new JCheckBoxOperator(jffo,Bundle.getMessage("ButtonShowPackets")).isSelected());
        assertTrue(new JCheckBoxOperator(jffo,Bundle.getMessage("ButtonSendEvent")).isSelected());

        JButtonOperator startLoggingButton = new JButtonOperator(jffo, Bundle.getMessage("ButtonAddMessage"));
        assertNotNull(startLoggingButton,"Logging pane opened by persistence");

        JButtonOperator resetStatsButton = new JButtonOperator(jffo, Bundle.getMessage("ButtonClear"));
        assertNotNull(resetStatsButton,"States pane opened by persistence");

        JButtonOperator copyButton = new JButtonOperator(jffo, Bundle.getMessage("ButtonCopy"));
        assertNotNull(copyButton,"Packets pane opened by persistence");

        JButtonOperator sendButton = new JButtonOperator(jffo, Bundle.getMessage("ButtonSend"),1);
        assertNotNull(sendButton,"Send pane opened by persistence");

        JUnitUtil.dispose(ff);
        jffo.waitClosed();

    }

    @Test
    public void testEventCaptureButton() {
        JFrame f = new JFrame("CbusConsoleDisplayOptionsPaneTest.testEventCaptureButton");
        f.add(mainConsolePane);
        f.pack();
        ThreadingUtil.runOnGUI(() -> f.setVisible(true));
        JFrameOperator jfo = new JFrameOperator( f.getTitle());

        JButtonOperator eventCapButtonOper = new JButtonOperator(jfo, Bundle.getMessage("CapConfigTitle"));
        assertNotNull(eventCapButtonOper);
        eventCapButtonOper.doClick();
        eventCapButtonOper.getQueueTool().waitEmpty();

        JFrameOperator evCapturejfo = new JFrameOperator( "Event Capture paired to " + mainConsolePane.getTitle() + " Filter and Highlighter");
        assertNotNull(evCapturejfo);
        evCapturejfo.setVisible(false);
        evCapturejfo.getQueueTool().waitEmpty();
        
        eventCapButtonOper.doClick();
        eventCapButtonOper.getQueueTool().waitEmpty();
        
        JFrameOperator checkTheSameEvCapturejfo = new JFrameOperator( "Event Capture paired to " + mainConsolePane.getTitle() + " Filter and Highlighter");
        assertNotNull(checkTheSameEvCapturejfo);
        
        JUnitUtil.dispose(f);
        jfo.waitClosed();

    }

    @Test
    public void testFilterButton() {
        JFrame f = new JFrame("CbusConsoleDisplayOptionsPaneTest.testFilterButton");
        f.add(mainConsolePane);
        f.pack();
        ThreadingUtil.runOnGUI(() -> f.setVisible(true));
        JFrameOperator jfo = new JFrameOperator( f.getTitle());

        JButtonOperator filterButtonOper = new JButtonOperator(jfo, Bundle.getMessage("ButtonFilter"));
        assertNotNull(filterButtonOper);
        filterButtonOper.doClick();
        filterButtonOper.getQueueTool().waitEmpty();

        JFrameOperator filterJfo = new JFrameOperator( mainConsolePane.getTitle() + " " + Bundle.getMessage("EventFilterTitleX", ""));
        assertNotNull(filterJfo);
        filterJfo.setVisible(false);
        filterJfo.getQueueTool().waitEmpty();

        filterButtonOper.doClick();
        filterButtonOper.getQueueTool().waitEmpty();
        
        JFrameOperator checkTheSameFilterjfo = new JFrameOperator( mainConsolePane.getTitle() + " " + Bundle.getMessage("EventFilterTitleX", ""));
        assertNotNull(checkTheSameFilterjfo);

        JUnitUtil.dispose(f);
        jfo.waitClosed();
    }

    @Test
    public void testHighlightButton() {
        JFrame f = new JFrame("CbusConsoleDisplayOptionsPaneTest.testHighlightButton");
        f.add(mainConsolePane);
        f.pack();
        ThreadingUtil.runOnGUI(() -> f.setVisible(true));
        JFrameOperator jfo = new JFrameOperator( f.getTitle());

        JButtonOperator highlightButtonOper = new JButtonOperator(jfo, Bundle.getMessage("ButtonHighlight"));
        assertNotNull(highlightButtonOper);
        highlightButtonOper.doClick();
        highlightButtonOper.getQueueTool().waitEmpty();

        JFrameOperator highJfo = new JFrameOperator( mainConsolePane.getTitle() + " " + Bundle.getMessage("EventHighlightTitle"));
        assertNotNull(highJfo);
        highJfo.setVisible(false);
        highJfo.getQueueTool().waitEmpty();

        highlightButtonOper.doClick();
        highlightButtonOper.getQueueTool().waitEmpty();
        
        JFrameOperator checkTheSameHighjfo = new JFrameOperator( mainConsolePane.getTitle() + " " + Bundle.getMessage("EventHighlightTitle", ""));
        assertNotNull(checkTheSameHighjfo);

        JUnitUtil.dispose(f);
        jfo.waitClosed();
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tc = null;
    private CbusConsolePane mainConsolePane = null;

    // use temporary directory so reliable state at start of each test.
    @BeforeEach
    public void setUp(@TempDir File folder) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        mainConsolePane = new CbusConsolePane();
        mainConsolePane.initComponents(memo, false);
    }

    @AfterEach
    public void tearDown() {
        assertNotNull(mainConsolePane);
        mainConsolePane.dispose();
        assertNotNull(tc);
        tc.terminateThreads();
        assertNotNull(memo);
        memo.dispose();
        mainConsolePane = null;
        tc = null;
        memo = null;
        JUnitUtil.tearDown();
    }

}
