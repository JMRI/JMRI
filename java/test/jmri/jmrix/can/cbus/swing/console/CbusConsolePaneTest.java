package jmri.jmrix.can.cbus.swing.console;

import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusConsolePane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusConsolePaneTest extends jmri.util.swing.JmriPanelTest {

    @Override 
    @Test
    public void testInitComponents() {
        Assertions.assertDoesNotThrow( () ->
            ((CbusConsolePane) panel).initComponents(memo));
    }

    @Test
    @DisabledIfHeadless
    public void testInitComponentsNoArgs() {
        Assertions.assertDoesNotThrow( () ->
            panel.initComponents());
    }

    @Test
    public void testInitContext() {
        Assertions.assertDoesNotThrow( () ->
            ((CbusConsolePane) panel).initContext(memo));
    }
    
    @Test
    @DisabledIfHeadless
    public void testSendCanMessageCanReply() {
        
        cbPanel.initComponents(memo);
        JFrameOperator jfo = createNewFrameWithPanel( cbPanel );
        
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        m.setElement(0, CbusConstants.CBUS_RTON);
        cbPanel.decodePane.message(m);
        JUnitUtil.waitFor(() ->{ return !getCbusPaneText(jfo).isEmpty(); },"pane text still empty");

        Assertions.assertTrue( getCbusPaneText(jfo).contains("RTON"), 
            "RTON logged in console");
        Assertions.assertTrue( getCbusPaneText(jfo).contains("Request Track On"), 
            "Request Track On logged in console");

        CanReply r = new CanReply(tc.getCanid());
        r.setNumDataElements(1);
        r.setElement(0, CbusConstants.CBUS_TON);
        clearCbusPaneText(jfo);
        cbPanel.decodePane.reply(r);
        Assertions.assertTrue( getCbusPaneText(jfo).contains(" TON"), 
            "TON logged in console");
        Assertions.assertTrue( getCbusPaneText(jfo).contains("Track On"), 
            "Track On logged in console");

        // new JButtonOperator(jfo, "Not a Button").doClick();  // NOI18N
        jfo.requestClose();
    
    }

    @Test
    @DisabledIfHeadless
    public void testDisplayRtrFrame() {
    
        cbPanel.initComponents(memo);
        JFrameOperator jfo = createNewFrameWithPanel( cbPanel );
        
        // click to display rtr info
        new JCheckBoxOperator(jfo, Bundle.getMessage("RtrCheckbox")).setSelected(true);
        
        CanReply r = new CanReply();
        r.setRtr(true);
        cbPanel.decodePane.reply(r);
        JUnitUtil.waitFor(() ->{ return !getCbusPaneText(jfo).isEmpty(); },"pane text still empty");
        Assertions.assertTrue( getCbusPaneText(jfo).contains("RTR:R"), 
            "RTR CanReply logged in console");
        
        r.setRtr(false);
        clearCbusPaneText(jfo);
        
        cbPanel.decodePane.reply(r);
        
        JUnitUtil.waitFor(() ->{ return !getCbusPaneText(jfo).isEmpty(); },"pane text still empty");
        Assertions.assertTrue( getCbusPaneText(jfo).contains("RTR:N"), 
            "Non-RTR CanReply logged in console");
        
        r.setRtr(true);
        r.setNumDataElements(0);
        clearCbusPaneText(jfo);
        
        cbPanel.decodePane.reply(r);
        JUnitUtil.waitFor(() ->{ return !getCbusPaneText(jfo).isEmpty(); },"pane text still empty");
        Assertions.assertTrue( getCbusPaneText(jfo).contains("RTR:R"), 
            "RTR CanReply 0 length logged in console");
        
        
        // now check CanMessage
        CanMessage m = new CanMessage(r);
        Assertions.assertTrue(m.getNumDataElements()==0);
        Assertions.assertTrue(m.isRtr());
        clearCbusPaneText(jfo);
        
        cbPanel.decodePane.message(m);
        JUnitUtil.waitFor(() ->{ return !getCbusPaneText(jfo).isEmpty(); },"pane text still empty");
        Assertions.assertTrue( getCbusPaneText(jfo).contains("RTR:R"), 
            "RTR CanMessage 0 length logged in console");
        
        m.setRtr(false);
        clearCbusPaneText(jfo);
        
        // new JButtonOperator(jfo, "Not a Button").doClick();  // NOI18N
        jfo.requestClose();
    }

    @Test
    @DisabledIfHeadless
    public void testHighlightsOfTrimmedLinesAreRemoved() {

        cbPanel.initComponents(memo);
        JFrameOperator jfo = createNewFrameWithPanel( cbPanel );
        cbPanel.monTextPaneCan.setMaxLines(10);
        cbPanel.monTextPaneCbus.setMaxLines(10);

        for (int i = 0; i < 100; i++) {
            cbPanel.nextLine("frame " + i + "\n", "decoded " + i + "\n", 0);
        }
        // the lines are shown later, on the GUI thread: wait for the last of them, then for the trim
        JUnitUtil.waitFor(() -> cbPanel.monTextPaneCbus.getText().contains("decoded 99")
            && cbPanel.monTextPaneCan.getText().contains("frame 99"), "every line shown");
        // 10 lines and the one being added to; a pane may keep a line or two more before it trims
        JUnitUtil.waitFor(() -> cbPanel.monTextPaneCbus.getLineCount() <= 13
            && cbPanel.monTextPaneCan.getLineCount() <= 13, "panes trimmed");

        // the highlights of the trimmed lines go when the next line is shown
        cbPanel.nextLine("frame\n", "decoded\n", 0);
        JUnitUtil.waitFor(() -> highlights(cbPanel.monTextPaneCbus) <= 14
            && highlights(cbPanel.monTextPaneCan) <= 14, "highlights of trimmed lines removed");

        new JButtonOperator(jfo, Bundle.getMessage("ButtonClearScreen")).doClick();
        Assertions.assertEquals( 0, highlights(cbPanel.monTextPaneCbus), "clear removes the highlights");
        Assertions.assertEquals( 0, highlights(cbPanel.monTextPaneCan), "clear removes the highlights");

        jfo.requestClose();
    }

    private static int highlights(javax.swing.JTextArea pane) {
        return pane.getHighlighter().getHighlights().length;
    }

    private JFrameOperator createNewFrameWithPanel(CbusConsolePane p){
        JmriJFrame f = new JmriJFrame(p.getName());
        f.add(p);
        jmri.util.ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });
        return new JFrameOperator( p.getName() );
    }

    private void clearCbusPaneText(JFrameOperator jfoo){
        new JTextAreaOperator(jfoo,1).setText("");
        JUnitUtil.waitFor(() ->{ return getCbusPaneText(jfoo).isEmpty(); },"pane text still empty");
    }

    private String getCbusPaneText(JFrameOperator jfoo){
        return new JTextAreaOperator(jfoo,1).getText().replaceAll("\\r\\n|\\r|\\n", "");
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
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        cbPanel = new CbusConsolePane();
        panel = cbPanel;
        helpTarget="package.jmri.jmrix.can.cbus.swing.console.CbusConsoleFrame";
        title="CBUS Console";
    }

    @AfterEach
    @Override
    public void tearDown() {
        
        CbusEventTableDataModel evMod = memo.get(CbusEventTableDataModel.class);
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
