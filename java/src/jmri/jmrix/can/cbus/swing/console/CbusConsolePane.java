package jmri.jmrix.can.cbus.swing.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.swing.CbusEventHighlightFrame;
import jmri.jmrix.can.cbus.swing.CbusSendEventPane;
import jmri.util.ThreadingUtil;
import jmri.util.swing.TextAreaFIFO;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Frame for CBUS Console
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsolePane extends jmri.jmrix.can.swing.CanPanel {

    static int console_instance_num;
    static final private int MAX_LINES = 5000;

    private final ConcurrentLinkedDeque<CbusConsoleLogEntry> logBuffer;

    private JToggleButton freezeButton;

    public TextAreaFIFO monTextPaneCan;
    public TextAreaFIFO monTextPaneCbus;
    private Highlighter cbusHighlighter;
    private Highlighter canHighlighter;

    protected final CbusConsoleStatsPane statsPane;
    protected final CbusConsolePacketPane packetPane;
    protected final CbusSendEventPane sendPane;
    protected CbusConsoleDecodeOptionsPane decodePane;
    protected final CbusConsoleLoggingPane logPane;
    public final CbusConsoleDisplayOptionsPane displayPane;

    // members for handling the CBUS interface
    protected TrafficController tc;

    public CbusConsolePane() {
        super();
        incrementInstance();
        logBuffer = new ConcurrentLinkedDeque<>();
        statsPane = new CbusConsoleStatsPane(this);
        packetPane = new CbusConsolePacketPane(this);
        sendPane = new CbusSendEventPane(this);
        logPane = new CbusConsoleLoggingPane(this);
        displayPane = new CbusConsoleDisplayOptionsPane(this);

    }

    public static int getConsoleInstanceNum() {
        return console_instance_num;
    }

    public static void incrementInstance() {
        console_instance_num++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (memo != null) {
            StringBuilder title = new StringBuilder(20);
            title.append(memo.getUserName()).append(" ");
            title.append(Bundle.getMessage("CbusConsoleTitle"));
            if (getConsoleInstanceNum() > 1) {
                title.append(" ").append( getConsoleInstanceNum() );
            }
            return title.toString();
        }
        return Bundle.getMessage("CbusConsoleTitle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.console.CbusConsoleFrame";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (decodePane!=null) {
            decodePane.dispose();
        }
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        initComponents( memo, true);
    }

    /**
     * Constructor For testing purposes, not for general use.
     * @param memo System Connection
     * @param launchEvTable true to launch a CBUS Event Table Model, else false.
     */
    public void initComponents(CanSystemConnectionMemo memo, boolean launchEvTable) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        decodePane = new CbusConsoleDecodeOptionsPane(this);
        if (launchEvTable){
            CbusEventTableDataModel.checkCreateNewEventModel(memo);
        }
        init();
    }

    public void init() {

        initTextAreas();

        // Sub-pane to hold buttons
        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));
        paneA.add(getClearFreezeButtonPane());
        paneA.add(decodePane);

        JPanel historyPane = new JPanel();
        historyPane.setLayout(new BorderLayout());
        historyPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("PacketHistoryTitle")));

        historyPane.add(getSplitPane(), BorderLayout.CENTER);
        historyPane.add(paneA, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(displayPane, BorderLayout.NORTH);
        add(historyPane, BorderLayout.CENTER);
        add(getAllBottomPanes(), BorderLayout.SOUTH);

    }

    private void initTextAreas() {

        monTextPaneCan = new TextAreaFIFO(MAX_LINES);
        monTextPaneCan.setVisible(true);
        monTextPaneCan.setToolTipText(Bundle.getMessage("TooltipMonTextPaneCan"));
        monTextPaneCan.setEditable(false);
        monTextPaneCan.setRows(5);
        monTextPaneCan.setColumns(5);

        monTextPaneCbus = new TextAreaFIFO(MAX_LINES);
        monTextPaneCbus.setVisible(true);
        monTextPaneCbus.setToolTipText(Bundle.getMessage("TooltipMonTextPaneCbus"));
        monTextPaneCbus.setEditable(false);
        monTextPaneCbus.setRows(5);
        monTextPaneCbus.setColumns(20);

        cbusHighlighter = monTextPaneCbus.getHighlighter();
        canHighlighter = monTextPaneCan.getHighlighter();

    }

    private JSplitPane getSplitPane(){

        JScrollPane jScrollPane1Can = new JScrollPane();
        jScrollPane1Can.getViewport().add(monTextPaneCan);
        jScrollPane1Can.setVisible(true);
        jScrollPane1Can.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("CanFrameTitle")));

        JScrollPane jScrollPane1Cbus = new JScrollPane();
        jScrollPane1Cbus.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("CbusMessageTitle")));
        jScrollPane1Cbus.getViewport().add(monTextPaneCbus);
        jScrollPane1Cbus.setVisible(true);

        jScrollPane1Can.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1Cbus.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1Can.setVerticalScrollBar(jScrollPane1Cbus.getVerticalScrollBar());

        // scroll panels to be side-by-side
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            jScrollPane1Can, jScrollPane1Cbus);
        split.setResizeWeight(0.3);
        split.setContinuousLayout(true);

        return split;
    }

    private JPanel getClearFreezeButtonPane() {

        JPanel messageButtonOptionpane = new JPanel();

        JButton clearButton = new JButton();
        freezeButton = new JToggleButton();

        clearButton.setText(Bundle.getMessage("ButtonClearScreen"));
        clearButton.setToolTipText(Bundle.getMessage("ButtonClearLogTip"));

        freezeButton.setText(Bundle.getMessage("ButtonFreezeScreen"));
        freezeButton.setToolTipText(Bundle.getMessage("TooltipStopScroll"));

        messageButtonOptionpane.setLayout(new BoxLayout(messageButtonOptionpane, BoxLayout.X_AXIS));
        messageButtonOptionpane.add(clearButton);
        messageButtonOptionpane.add(freezeButton);

        clearButton.addActionListener(this::clearButtonActionPerformed);
        freezeButton.addActionListener(this::freezeButtonActionPerformed);
        return messageButtonOptionpane;

    }

    private JPanel getAllBottomPanes() {

        JPanel southPane = new JPanel();
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));

        logPane.setVisible(false);
        statsPane.setVisible(false);
        packetPane.setVisible(false);
        sendPane.setVisible(false);

        southPane.add(logPane);
        southPane.add(statsPane);
        southPane.add(packetPane);
        southPane.add(sendPane);

        return southPane;

    }

    /**
     * Handle display of traffic.
     * @param line        string the traffic in 'normal form',
     * @param decoded     string the decoded, protocol specific, form.
     * Both should contain the same number of well-formed lines, e.g. end with \n
     * @param highlight   int
     */
    public void nextLine(String line, String decoded, int highlight) {

        logBuffer.add( new CbusConsoleLogEntry(line,decoded,highlight));

        // if not frozen, display it in the Swing thread
        if (!freezeButton.isSelected()) {
            ThreadingUtil.runOnGUIEventually( ()->{
                processLogBuffer();
            });
        }

        // if requested, log to a file.
        logPane.sendLogToFile( decoded );

    }

    private void processLogBuffer() {
        while (logBuffer.size()>0){
            CbusConsoleLogEntry next = logBuffer.removeFirst();

            final int start = monTextPaneCbus.getText().length();
            final int startc= monTextPaneCan.getText().length();

            monTextPaneCan.append(next.getFrameText());
            monTextPaneCbus.append(next.getDecodedText());

            if (next.getHighlighter() > -1) {
                try {
                    CbusHighlightPainter cbusHighlightPainter = new CbusHighlightPainter(
                        CbusEventHighlightFrame.highlightColors[next.getHighlighter()]);
                    // log.debug("Add highlight start: " + start + " end: " + end);
                    cbusHighlighter.addHighlight(start, monTextPaneCbus.getText().length() - 1, cbusHighlightPainter);
                    canHighlighter.addHighlight(startc, monTextPaneCan.getText().length() - 1, cbusHighlightPainter);
                } catch (BadLocationException e) {} // do nothing
            }
        }
    }

    // clear the monitoring history
    private void clearButtonActionPerformed(ActionEvent e) {
        logBuffer.clear();
        monTextPaneCan.setText("");
        monTextPaneCbus.setText("");
    }

    private void freezeButtonActionPerformed(ActionEvent e) {
        if (freezeButton.isSelected()) {
            freezeButton.setForeground(Color.red);
        } else {
            freezeButton.setForeground(new JTextField().getForeground()); // reset to default
            nextLine("","",-1); // poke with zero content to refresh screen
        }
    }

    // A private subclass of the default highlight painter
    private class CbusHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public CbusHighlightPainter(Color color) {
            super(color);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("CbusConsoleTitle"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusConsolePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusConsolePane.class);
}
