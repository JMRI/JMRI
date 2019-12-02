package jmri.jmrix.can.cbus.swing.console;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane;
import jmri.jmrix.can.cbus.swing.CbusFilterFrame;
import jmri.jmrix.can.cbus.swing.CbusEventHighlightFrame;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.TextAreaFIFO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Cbus Console
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsolePane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    protected static int console_instance_num;
    static final private int CAN = 0;
    static final private int CBUS = 1;
    static final private int MAX_LINES = 5000;

    transient private int _sent=0;
    transient private int _rcvd=0;
    transient private int _events=0;
    transient private int _dcc=0;
    transient private int _total=0;
    private boolean _decimal = false;
    
    public JPanel _evCap;

    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    
    protected JScrollPane jScrollPane1Can = new JScrollPane();
    protected JScrollPane jScrollPane1Cbus = new JScrollPane();
    protected JSplitPane split;
    protected TextAreaFIFO monTextPaneCan = new TextAreaFIFO(MAX_LINES);
    protected TextAreaFIFO monTextPaneCbus = new TextAreaFIFO(MAX_LINES);
    protected Highlighter cbusHighlighter;
    protected Highlighter canHighlighter;
    
    protected JButton startLogButton = new JButton();
    protected JButton stopLogButton = new JButton();
    protected JButton openLogFileButton = new JButton();
    protected JCheckBox timeCheckBox = new JCheckBox();
    protected JCheckBox priCheckBox = new JCheckBox();
    protected JCheckBox canidCheckBox = new JCheckBox();
    protected JCheckBox showarrowsCheckBox = new JCheckBox();
    protected JCheckBox showRtrCheckBox = new JCheckBox();
    protected JCheckBox showOpcCheckBox = new JCheckBox();
    protected JCheckBox showOpcExtraCheckBox = new JCheckBox();
    protected JCheckBox showAddressCheckBox = new JCheckBox();
    protected JCheckBox showCanCheckBox = new JCheckBox();
    
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton logenterButton = new JButton();

    protected JCheckBox showLogCheckBox = new JCheckBox();
    protected JCheckBox showStatsCheckBox = new JCheckBox();
    protected JCheckBox showPacketCheckBox = new JCheckBox();
    protected JCheckBox showSendEventCheckBox = new JCheckBox();
    public JButton filterButton = new JButton();
    public JButton highlightButton = new JButton();
    protected JButton evCaptureButton = new JButton();
    protected JCheckBox decimalCheckBox = new JCheckBox();
    protected JCheckBox decimalCheckBoxB = new JCheckBox();
    protected JCheckBox decimalCheckBoxC = new JCheckBox();
        
    protected JTextField sentCountField = new JTextField("0", 8);
    protected JTextField rcvdCountField = new JTextField("0", 8);
    protected JTextField eventsCountField = new JTextField("0", 8);
    protected JTextField dccCountField = new JTextField("0", 8);
    protected JTextField totalCountField = new JTextField("0", 8);
    protected JButton statsClearButton = new JButton();

    protected JTextField lastDynPriField = new JTextField();
    protected JTextField lastMinPriField = new JTextField();
    protected JTextField[] lastRxDataFields = new JTextField[8];
    protected JButton copyButton = new JButton();

    protected JTextField dynPriField = new JTextField();
    protected JTextField minPriField = new JTextField();
    protected JTextField[] dataFields = new JTextField[8];
    protected JButton sendPacketButton = new JButton();
    protected JButton dataClearButton = new JButton();

    protected JPanel statsPane;
    protected JPanel rxPacketPane;
    protected JPanel sendPacketPane;
    protected JPanel sendEvPane;
    protected JPanel logOptionspane;

    protected JRadioButton onButton = new JRadioButton();
    protected JRadioButton offButton = new JRadioButton();
    protected ButtonGroup onOffGroup = new ButtonGroup();
    protected JLabel nodeNumberLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("NodeNumberCol")));
    protected JLabel eventLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("EventCol")));
    protected JTextField nnField = new JTextField();
    protected JTextField evField = new JTextField();
    protected JButton sendEvButton = new JButton();

    protected int i;

    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    private CbusFilterFrame _filterFrame;
    private CbusEventHighlightFrame _highlightFrame;
    public ConfigToolPane _evCapFrame;
    public JmriJFrame _ecf;
    
    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    // members for handling the CBUS interface
    //CanMessage msg;
    TrafficController tc = null;
    String replyString;

    public CbusConsolePane() {
        super();
        _filterFrame = null;
        _highlightFrame = null;
        _evCapFrame = null;
        _ecf = null;
        incrementInstance();
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
            title.append(memo.getUserName());
            title.append(" ");
            title.append(Bundle.getMessage("CbusConsoleTitle"));
            if (getConsoleInstanceNum() > 1) {
                title.append(" ");
                title.append( getConsoleInstanceNum() );
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

    public void init() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        monTextPaneCan.dispose();
        monTextPaneCbus.dispose();
        if (tc != null) {
            tc.removeCanListener(this);
        }
        if (_highlightFrame != null) {
            _highlightFrame.dispose();
            _highlightFrame=null;
        }
        if (_filterFrame != null) {
            _filterFrame.dispose();
            _filterFrame=null;
        }
        if (_ecf != null) {
            _ecf.dispose();
            _ecf=null;
        }
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        addTc(tc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));
        
        setLayout(new BorderLayout());        
        
        // Pane to select display type
        JPanel showPane = new JPanel();
        showPane.setLayout(new BoxLayout(showPane, BoxLayout.X_AXIS));
        showPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), Bundle.getMessage("Display")));
        
        showLogCheckBox.setText(Bundle.getMessage("Logging"));
        showLogCheckBox.setVisible(true);
        showLogCheckBox.setToolTipText(Bundle.getMessage("LoggingTip"));
        
        showStatsCheckBox.setText(Bundle.getMessage("StatisticsTitle"));
        showStatsCheckBox.setVisible(true);
        showStatsCheckBox.setToolTipText(Bundle.getMessage("ButtonShowStats"));

        showPacketCheckBox.setText(Bundle.getMessage("ButtonShowPackets"));
        showPacketCheckBox.setVisible(true);
        showPacketCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPackets"));

        showSendEventCheckBox.setText(Bundle.getMessage("ButtonSendEvent"));
        showSendEventCheckBox.setVisible(true);
        showSendEventCheckBox.setToolTipText(Bundle.getMessage("TooltipShowEvents"));
        
        filterButton.setText(Bundle.getMessage("ButtonFilter"));
        filterButton.setVisible(true);
        filterButton.setToolTipText(Bundle.getMessage("TooltipFilter"));
        
        highlightButton.setText(Bundle.getMessage("ButtonHighlight"));
        highlightButton.setVisible(true);
        highlightButton.setToolTipText(Bundle.getMessage("TooltipHighlighter"));

        evCaptureButton.setText(Bundle.getMessage("CapConfigTitle"));
        evCaptureButton.setVisible(true);
        // evCaptureButton.setToolTipText(Bundle.getMessage("TooltipHighlighter"));        
        
        showPane.add(showLogCheckBox);        
        showPane.add(showStatsCheckBox);
        showPane.add(showPacketCheckBox);
        showPane.add(showSendEventCheckBox);
        showPane.add(filterButton);
        showPane.add(highlightButton);
        showPane.add(evCaptureButton);

        add(showPane, BorderLayout.NORTH);
        
        monTextPaneCan.setVisible(true);
        monTextPaneCan.setToolTipText(Bundle.getMessage("TooltipMonTextPaneCan"));
        monTextPaneCan.setEditable(false);
        monTextPaneCan.setRows(5);
        monTextPaneCan.setColumns(5);

        monTextPaneCbus.setVisible(true);
        monTextPaneCbus.setToolTipText(Bundle.getMessage("TooltipMonTextPaneCbus"));
        monTextPaneCbus.setEditable(false);
        monTextPaneCbus.setRows(5);
        monTextPaneCbus.setColumns(20);
        
        cbusHighlighter = monTextPaneCbus.getHighlighter();
        canHighlighter = monTextPaneCan.getHighlighter();

        entryField.setToolTipText(Bundle.getMessage("EntryAddtoLogTip"));


        jScrollPane1Can.getViewport().add(monTextPaneCan);
        jScrollPane1Can.setVisible(true);
        jScrollPane1Can.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("CanFrameTitle")));
            
        jScrollPane1Cbus.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("CbusMessageTitle")));
        jScrollPane1Cbus.getViewport().add(monTextPaneCbus);
        jScrollPane1Cbus.setVisible(true);
        
        jScrollPane1Can.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1Cbus.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);        
        jScrollPane1Can.setVerticalScrollBar(jScrollPane1Cbus.getVerticalScrollBar());
        

        timeCheckBox.setText(Bundle.getMessage("ButtonShowTimestamp"));
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps"));

        priCheckBox.setText(Bundle.getMessage("ButtonShowPriorities"));
        priCheckBox.setVisible(true);
        priCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPrios"));

        canidCheckBox.setText(Bundle.getMessage("CanID"));
        canidCheckBox.setVisible(true);
        canidCheckBox.setToolTipText(Bundle.getMessage("CanID"));

        showarrowsCheckBox.setText(Bundle.getMessage("TrafficDirection"));
        showarrowsCheckBox.setVisible(true);
        showarrowsCheckBox.setToolTipText(Bundle.getMessage("TrafficDirectionTip"));
        showarrowsCheckBox.setSelected(true);
        
        showRtrCheckBox.setText(Bundle.getMessage("RtrCheckbox"));
        showRtrCheckBox.setVisible(true);
        showRtrCheckBox.setToolTipText(Bundle.getMessage("RtrCheckboxTip"));

        showOpcCheckBox.setText(Bundle.getMessage("showOpcCheckbox"));
        showOpcCheckBox.setVisible(true);
        showOpcCheckBox.setToolTipText(Bundle.getMessage("showOpcCheckboxTip"));
        showOpcCheckBox.setSelected(true);
        
        showOpcExtraCheckBox.setText(Bundle.getMessage("OpcExtraCheckbox"));
        showOpcExtraCheckBox.setVisible(true);
        showOpcExtraCheckBox.setToolTipText(Bundle.getMessage("OpcExtraCheckboxTip"));
        
        showAddressCheckBox.setText(Bundle.getMessage("showAddressCheckBox"));
        showAddressCheckBox.setVisible(true);
        showAddressCheckBox.setToolTipText(Bundle.getMessage("showAddressCheckBoxTip"));
        
        showCanCheckBox.setText(Bundle.getMessage("showCanCheckBox"));
        showCanCheckBox.setVisible(true);
        showCanCheckBox.setToolTipText(Bundle.getMessage("showCanCheckBoxTip"));
        
        decimalCheckBox.setText(Bundle.getMessage("ButtonDecimal"));
        decimalCheckBox.setVisible(true);
        decimalCheckBox.setToolTipText(Bundle.getMessage("TooltipDecimal"));
        decimalCheckBox.setSelected(_decimal);
        
        decimalCheckBoxB.setText(Bundle.getMessage("ButtonDecimal"));
        decimalCheckBoxB.setVisible(true);
        decimalCheckBoxB.setToolTipText(Bundle.getMessage("TooltipDecimal"));
        decimalCheckBoxB.setSelected(_decimal);        
        
        decimalCheckBoxC.setText(Bundle.getMessage("ButtonDecimal"));
        decimalCheckBoxC.setVisible(true);
        decimalCheckBoxC.setToolTipText(Bundle.getMessage("TooltipDecimal"));
        decimalCheckBoxC.setSelected(_decimal);        
        
        sendPacketButton.setText(Bundle.getMessage("ButtonSend"));
        sendPacketButton.setVisible(true);
        sendPacketButton.setToolTipText(Bundle.getMessage("TooltipSendPacket"));

        dataClearButton.setText(Bundle.getMessage("ButtonClear"));
        dataClearButton.setVisible(true);
        dataClearButton.setToolTipText(Bundle.getMessage("TooltipClearFields"));

        copyButton.setText(Bundle.getMessage("ButtonCopy"));
        copyButton.setVisible(true);
        copyButton.setToolTipText(Bundle.getMessage("TooltipCopyEvent"));

        onButton.setText(Bundle.getMessage("InitialStateOn"));
        onButton.setVisible(true);
        onButton.setToolTipText(Bundle.getMessage("TooltipSendOnEvent"));
        onButton.setSelected(true);

        offButton.setText(Bundle.getMessage("InitialStateOff"));
        offButton.setVisible(true);
        offButton.setToolTipText(Bundle.getMessage("TooltipSendOffEvent"));

        sendEvButton.setText(Bundle.getMessage("ButtonSend"));
        sendEvButton.setVisible(true);
        sendEvButton.setToolTipText(Bundle.getMessage("TooltipSendEvent"));

        // add items to GUI
        // Pane to hold packet history
        JPanel historyPane = new JPanel();
        historyPane.setLayout(new BorderLayout());
        historyPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("PacketHistoryTitle")));

        // scroll panels to be side-by-side
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            jScrollPane1Can, jScrollPane1Cbus);
        split.setResizeWeight(0.3);
        split.setContinuousLayout(true);

        historyPane.add(split, BorderLayout.CENTER);
        

        // Sub-pane to hold buttons
        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

        
        clearButton.setText(Bundle.getMessage("ButtonClearScreen"));
        clearButton.setVisible(true);
        clearButton.setToolTipText(Bundle.getMessage("ButtonClearLogTip"));

        freezeButton.setText(Bundle.getMessage("ButtonFreezeScreen"));
        freezeButton.setVisible(true);
        freezeButton.setToolTipText(Bundle.getMessage("TooltipStopScroll"));
        
        JPanel messageButtonOptionpane = new JPanel();
        messageButtonOptionpane.setLayout(new BoxLayout(messageButtonOptionpane, BoxLayout.X_AXIS));
        messageButtonOptionpane.add(clearButton);
        messageButtonOptionpane.add(freezeButton);
        paneA.add(messageButtonOptionpane);
        
        JPanel messageCheckOptionpane = new JPanel();
        messageCheckOptionpane.setLayout(new BoxLayout(messageCheckOptionpane, BoxLayout.X_AXIS));        
        
        messageCheckOptionpane.add(timeCheckBox);
        messageCheckOptionpane.add(priCheckBox);
        messageCheckOptionpane.add(showarrowsCheckBox);
        messageCheckOptionpane.add(canidCheckBox);
        messageCheckOptionpane.add(showRtrCheckBox);
        messageCheckOptionpane.add(showOpcCheckBox);        
        messageCheckOptionpane.add(showOpcExtraCheckBox);
        messageCheckOptionpane.add(showAddressCheckBox);        
        messageCheckOptionpane.add(showCanCheckBox);           
        
        paneA.add(messageCheckOptionpane);

        logOptionspane = new JPanel();
        
        logOptionspane.setLayout(new BoxLayout(logOptionspane, BoxLayout.X_AXIS));
        logOptionspane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("Logging"))); 
        
        logenterButton.setText(Bundle.getMessage("ButtonAddMessage"));
        logenterButton.setVisible(true);
        logenterButton.setToolTipText(Bundle.getMessage("TooltipAddMessage"));
        
        openFileChooserButton.setText(Bundle.getMessage("ButtonChooseLogFile"));
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText(Bundle.getMessage("TooltipChooseLogFile"));        
        
        startLogButton.setText(Bundle.getMessage("ButtonStartLogging"));
        startLogButton.setVisible(true);
        startLogButton.setToolTipText(Bundle.getMessage("TooltipStartLogging") + " " +
        Bundle.getMessage("ButtonStartLogTipExtra"));

        openLogFileButton.setText(Bundle.getMessage("OpenLogFile"));
        openLogFileButton.setVisible(true);
        openLogFileButton.setToolTipText(Bundle.getMessage("OpenLogFileTip"));
        
        stopLogButton.setText(Bundle.getMessage("ButtonStopLogging"));
        stopLogButton.setVisible(false);
        stopLogButton.setToolTipText(Bundle.getMessage("TooltipStopLogging"));
        stopLogButton.setForeground(Color.red);

        // logpane.add(logOptionspane);

        logOptionspane.add(startLogButton);
        logOptionspane.add(stopLogButton);
        logOptionspane.add(openFileChooserButton);
        logOptionspane.add(openLogFileButton);
        logOptionspane.add(logenterButton);
        logOptionspane.add(entryField);
    
        historyPane.add(paneA, BorderLayout.SOUTH);
        add(historyPane, BorderLayout.CENTER);

        JPanel southPane = new JPanel();
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));

        logOptionspane.setVisible(false);        
        southPane.add(logOptionspane);    
        
        sentCountField.setToolTipText(Bundle.getMessage("TooltipSent"));
        sentCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SentTitle")));

        rcvdCountField.setToolTipText(Bundle.getMessage("TooltipReceived"));
        rcvdCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("ReceivedTitle")));

        eventsCountField.setToolTipText(Bundle.getMessage("eventsCountFieldTip"));
        eventsCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CbusEvents")));

        dccCountField.setToolTipText(Bundle.getMessage("dccCountFieldTip"));
        dccCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("dccCountField")));                
                
        totalCountField.setToolTipText(Bundle.getMessage("totalCountFieldTip"));
        totalCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("totalCountField")));                
        
        statsClearButton.setText(Bundle.getMessage("ButtonClear"));
        statsClearButton.setVisible(true);
        statsClearButton.setToolTipText(Bundle.getMessage("TooltipClearCounters"));
        
        // Pane for network statistics
        statsPane = new JPanel();

        statsPane.setVisible(false);
        showStatsCheckBox.setSelected(false);
        
        showLogCheckBox.setSelected(false);

        statsPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("StatisticsTitle")));
        statsPane.add(sentCountField);
        statsPane.add(rcvdCountField);
        statsPane.add(totalCountField);
        statsPane.add(eventsCountField);
        statsPane.add(dccCountField);
        statsPane.add(statsClearButton);

        southPane.add(statsPane);
        

        // Pane for most recently recived packet
        rxPacketPane = new JPanel();
        rxPacketPane.setLayout(new BoxLayout(rxPacketPane, BoxLayout.X_AXIS));
        rxPacketPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("MostRecentPacketTitle")));

        // Construct data fields for Priority and up to 8 bytes
        lastDynPriField = new JTextField("", 4);
        lastDynPriField.setToolTipText(Bundle.getMessage("TooltipDynPri"));
        lastDynPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("DynPriTitle")));
        rxPacketPane.add(lastDynPriField);
        lastMinPriField = new JTextField("", 4);
        lastMinPriField.setToolTipText(Bundle.getMessage("TooltipMinPri"));
        lastMinPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("MinPriTitle")));
        rxPacketPane.add(lastMinPriField);
        for (i = 0; i < 8; i++) {
            lastRxDataFields[i] = new JTextField("", 4);
            if (i == 0) {
                lastRxDataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d0 (OPC)"));
                lastRxDataFields[i].setToolTipText(Bundle.getMessage("TooltipOpc"));
            } else {
                lastRxDataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d" + i));
                lastRxDataFields[i].setToolTipText(Bundle.getMessage("TooltipDbX", i));
            }
            rxPacketPane.add(lastRxDataFields[i]);
        }
        
        rxPacketPane.add(copyButton);
        rxPacketPane.add(decimalCheckBox);
        southPane.add(rxPacketPane);

        // Pane for constructing packet to send
        sendPacketPane = new JPanel();
        sendPacketPane.setLayout(new BoxLayout(sendPacketPane, BoxLayout.X_AXIS));
        sendPacketPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("SendPacketTitle")));

        // Construct data fields for Priority and up to 8 bytes
        dynPriField = new JTextField("2", 4);
        dynPriField.setToolTipText(Bundle.getMessage("TooltipDynPri"));
        dynPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("DynPriTitle")));
        sendPacketPane.add(dynPriField);
        minPriField = new JTextField("3", 4);
        minPriField.setToolTipText(Bundle.getMessage("TooltipDinPri03"));
        minPriField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("MinPriTitle")));
        sendPacketPane.add(minPriField);
        for (i = 0; i < 8; i++) {
            dataFields[i] = new JTextField("", 4);
            if (i == 0) {
                dataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d0 (OPC)"));
                dataFields[i].setToolTipText(Bundle.getMessage("TooltipOpc"));
            } else {
                dataFields[i].setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "d" + i));
                dataFields[i].setToolTipText(Bundle.getMessage("TooltipDbX", i));
            }
            sendPacketPane.add(dataFields[i]);
        }
        sendPacketPane.add(sendPacketButton);
        sendPacketPane.add(dataClearButton);
        sendPacketPane.add(decimalCheckBoxB);
        sendPacketPane.setVisible(false);

        southPane.add(sendPacketPane);

        showPacketCheckBox.setSelected(false);
        rxPacketPane.setVisible(false);
        sendPacketPane.setVisible(false);

        showPacketCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPacketCheckBox.isSelected()) {
                    rxPacketPane.setVisible(true);
                    sendPacketPane.setVisible(true);
                } else {
                    rxPacketPane.setVisible(false);
                    sendPacketPane.setVisible(false);
                }
            }
        });

        // Pane for constructing event to send
        sendEvPane = new JPanel();

        sendEvPane.setVisible(true);
        showSendEventCheckBox.setSelected(false);

        
        sendEvPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("ButtonSendEvent")));

        nnField = new JTextField("0", 5);
        nnField.setToolTipText("<html>" + Bundle.getMessage("ToolTipNodeNumber") + "<br>" +
            Bundle.getMessage("ToolTipPrefix") + "</html>");
        sendEvPane.add(nodeNumberLabel);
        sendEvPane.add(nnField);

        evField = new JTextField("0", 5);
        evField.setToolTipText("<html>" + Bundle.getMessage("ToolTipEvent") + "<br>" +
            Bundle.getMessage("ToolTipPrefix") + "</html>");
        sendEvPane.add(eventLabel);
        sendEvPane.add(evField);

        onOffGroup.add(onButton);
        onOffGroup.add(offButton);
        sendEvPane.add(onButton);
        sendEvPane.add(offButton);
        sendEvPane.add(sendEvButton);
        sendEvPane.add(decimalCheckBoxC);
        sendEvPane.setVisible(false);
        southPane.add(sendEvPane);

        add(southPane, BorderLayout.SOUTH);

        ActionListener freezeButtonaction = ae -> {
            if (freezeButton.isSelected()) {
                freezeButton.setForeground(Color.red);
            } else {
                freezeButton.setForeground(new JTextField().getForeground()); // reset to default
            }
        };
        freezeButton.addActionListener(freezeButtonaction); 
        
        showLogCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logOptionspane.setVisible(showLogCheckBox.isSelected());
            }
        });        
        
        showStatsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statsPane.setVisible(showStatsCheckBox.isSelected());
            }
        });        
        
        showSendEventCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendEvPane.setVisible(showSendEventCheckBox.isSelected());
            }
        });

        // connect actions to buttons
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearButtonActionPerformed(e);
            }
        });
        
        startLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startLogButtonActionPerformed(e);
                openFileChooserButton.setVisible(false);
                startLogButton.setVisible(false);
                stopLogButton.setVisible(true);                
            }
        });
        
        stopLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopLogButtonActionPerformed(e);
                openFileChooserButton.setVisible(true);
                stopLogButton.setVisible(false);
                startLogButton.setVisible(true);
            }
        });
        
        openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openFileChooserButtonActionPerformed(e);
            }
        });
        
        openLogFileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    openLogFileActionPerformed(e);
                } catch (Exception ex) {
                    log.error("log file open exception " + ex);
                }
            }
        });
        
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                filterButtonActionPerformed(e);
            }
        });

        highlightButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                highlightButtonActionPerformed(e);
            }
        });
        
        evCaptureButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                evCaptureButtonActionPerformed(e);
            }
        });
        
        ActionListener logenteraction = ae -> {
            textToLogButtonActionPerformed(ae);
        };
        
        logenterButton.addActionListener(logenteraction);
        entryField.addActionListener(logenteraction);
        
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                copyButtonActionPerformed(e);
            }
        });
       
        ActionListener sendPacketaction = ae -> {
            sendPacketButtonActionPerformed(ae);
        };
        
        sendPacketButton.addActionListener(sendPacketaction);
        
        ActionListener sendEventaction = ae -> {
            sendEvButtonActionPerformed(ae);
        };
        
        nnField.addActionListener(sendEventaction);        
        evField.addActionListener(sendEventaction);
        sendEvButton.addActionListener(sendEventaction);

        dataClearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dataClearButtonActionPerformed(e);
            }
        });

        statsClearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                statsClearButtonActionPerformed(e);
            }
        });

        decimalCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decimalCheckBoxActionPerformed(e);
            }
        });
        
        decimalCheckBoxB.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decimalCheckBoxActionPerformedB(e);
            }
        });        
        
        decimalCheckBoxC.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decimalCheckBoxActionPerformedC(e);
            }
        });        
        
        linesBuffer[CAN] = new StringBuilder();
        linesBuffer[CBUS] = new StringBuilder();
    }

    /**
     * Handle display of traffic.
     * @param line        string the traffic in 'normal form',
     * @param decoded     string the decoded, protocol specific, form.
     * Both should contain the same number of well-formed lines, e.g. end with \n
     * @param priorities string
     * @param highlight   int
     */
    public void nextLine(String line, String decoded, String priorities, int highlight) {

        StringBuilder sbCan = new StringBuilder(180);
        StringBuilder sbCbus = new StringBuilder(180);
        final int highlightIndex = highlight;
        // log.debug("_highlightFrame: " + _highlightFrame + " highlight: " + highlight);
        if (highlightIndex >= 0) {
            final Color highlightColor = _highlightFrame.getColor(highlight);
            cbusHighlightPainter = new CbusHighlightPainter(highlightColor);
        }

        // display the timestamp if requested
        if (timeCheckBox.isSelected()) {
           // sbCan.append(df.format(new Date()));
            sbCbus.append(df.format(new Date()) + " ");
        }

        // display CBUS the priorities if requested
        if (priCheckBox.isSelected()) {
            sbCbus.append((priorities) + " ");
        }

        if (highlightIndex >= 0) {
            sbCan.append((Bundle.getMessage("ButtonHighlight") + (highlightIndex + 1) + ": "));
            sbCbus.append((Bundle.getMessage("ButtonHighlight") + (highlightIndex + 1) + ": "));
        }

        // display decoded data
        sbCan.append(line);
        sbCbus.append(decoded);
        synchronized (linesBuffer) {
            linesBuffer[CAN].append(sbCan.toString());
            linesBuffer[CBUS].append(sbCbus.toString());
        }

        // if not frozen, display it in the Swing thread
        if (!freezeButton.isSelected()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    synchronized (linesBuffer) {
                        final int start = monTextPaneCbus.getText().length();
                        final int startc= monTextPaneCan.getText().length();
                        monTextPaneCan.append(linesBuffer[CAN].toString());
                        monTextPaneCbus.append(linesBuffer[CBUS].toString());
                        final int end = monTextPaneCbus.getText().length();
                        final int endc = monTextPaneCan.getText().length();

                        try {
                            if (highlightIndex >= 0) {
                                // log.debug("Add highlight start: " + start + " end: " + end);
                                cbusHighlighter.addHighlight(start, end - 1, cbusHighlightPainter);
                                canHighlighter.addHighlight(startc, endc - 1, cbusHighlightPainter);
                            }
                        } catch (BadLocationException e) {
                            // do nothing
                        }
                        linesBuffer[CAN].setLength(0);
                        linesBuffer[CBUS].setLength(0);
                    }
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

        // if requested, log to a file.
        if (logStream != null) {
            String logLine = sbCbus.toString();
            if (!newline.equals("\n")) {
                // have to massage the line-ends
                int j;
                int lim = sbCbus.length();
                StringBuilder out = new StringBuilder(sbCbus.length() + 10);  // arbitrary guess at space
                for (j = 0; j < lim; j++) {
                    if (sbCbus.charAt(j) == '\n') {
                        out.append(newline);
                    } else {
                        out.append(sbCbus.charAt(j));
                    }
                }
                logLine = new String(out);
            }
            logStream.print(logLine);
        }
    }

    String newline = System.getProperty("line.separator");

    public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
        // clear the monitoring history
        synchronized (linesBuffer) {
            linesBuffer[CAN].setLength(0);
            linesBuffer[CBUS].setLength(0);
            monTextPaneCan.setText("");
            monTextPaneCbus.setText("");
        }
    }

    public synchronized void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start logging by creating the stream
        if (logStream == null) {  // successive clicks don't restart the file
            // start logging
            try {
                logStream = new PrintStream(new FileOutputStream(logFileChooser.getSelectedFile()));
            } catch (Exception ex) {
                log.error("exception " + ex);
            }
        }
    }

    public synchronized void stopLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // stop logging by removing the stream
        if (logStream != null) {
            logStream.flush();
            logStream.close();
        }
        logStream = null;
    }

    public void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start at current file, show dialog
        int retVal = logFileChooser.showSaveDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            boolean loggingNow = (logStream != null);
            stopLogButtonActionPerformed(e);  // stop before changing file
            //File file = logFileChooser.getSelectedFile();
            // if we were currently logging, start the new file
            if (loggingNow) {
                startLogButtonActionPerformed(e);
            }
        }
    }
    
    public void openLogFileActionPerformed(java.awt.event.ActionEvent e) throws IOException {
        // start at current file, show dialog
        Desktop desktop = Desktop.getDesktop();
        File dirToOpen = null;
        
        try {
            dirToOpen = logFileChooser.getSelectedFile();
            desktop.open(dirToOpen);
        } catch (IllegalArgumentException iae) {
            // log.info("Merg Cbus Console Log File Not Found");
            JOptionPane.showMessageDialog(null, 
                (Bundle.getMessage("NoOpenLogFile")), Bundle.getMessage("WarningTitle"),
                JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    public void textToLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText() + "\n", entryField.getText() + "\n", "", -1);
    }

    public void filterButtonActionPerformed(java.awt.event.ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if (_filterFrame == null) {
            _filterFrame = new CbusFilterFrame(this,_evCapFrame);
            try {
                _filterFrame.initComponents();
            } catch (Exception ex) {
                log.error("Exception: " + ex.toString());
            }
            _filterFrame.setVisible(true);
            if (_evCapFrame != null ) {
                _evCapFrame.setFilter(_filterFrame);
            }
        } else {
            _filterFrame.setState(Frame.NORMAL);
            _filterFrame.setVisible(true);
        }
    }
    
    public void highlightButtonActionPerformed(java.awt.event.ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if (_highlightFrame == null) {
            _highlightFrame = new CbusEventHighlightFrame(this,_evCapFrame);
            try {
                _highlightFrame.initComponents();
            } catch (Exception ex) {
                log.error("Exception: " + ex.toString());
            }
            _highlightFrame.setVisible(true);
            if (_evCapFrame != null ) {
                _evCapFrame.setHighlighter(_highlightFrame);
            }
        } else {
            _highlightFrame.setState(Frame.NORMAL);
            _highlightFrame.setVisible(true);
        }
    }

    public void evCaptureButtonActionPerformed(java.awt.event.ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if (_evCapFrame == null ) {
            _ecf = new JmriJFrame("Event Capture paired to " + getTitle() + " Filter and Highlighter");
            _evCapFrame = new ConfigToolPane(this, _filterFrame, _highlightFrame);
            _ecf.add(_evCapFrame);
            _evCapFrame.initComponents(memo);
            _ecf.pack();
            _ecf.setState(Frame.NORMAL);
            _ecf.setVisible(true);
        } else {
            _ecf.setState(Frame.NORMAL);
            _ecf.setVisible(true);
        }
            // jmri.jmrix.can.cbus.swing.configtool.ConfigToolAction();
    }

    public void highlightOn(int index, int nn, boolean nnEn, int ev, boolean evEn, int ty, int dr) {
        // log.debug("Cbus Console highlight applied");
        StringBuilder sb = new StringBuilder(80);
        if (nnEn) {
            sb.append((Bundle.getMessage("CbusNode") + nn + " "));
        }
        if (evEn) {
            sb.append((Bundle.getMessage("CbusEvent") + ev + " "));
        }
        if (ty == CbusConstants.EVENT_ON) {
            sb.append(Bundle.getMessage("CbusEventOn"));
        } else if (ty == CbusConstants.EVENT_OFF) {
            sb.append(Bundle.getMessage("CbusEventOff"));
        } else {
            sb.append(Bundle.getMessage("CbusEventOnOrOff"));
        }
        if (dr == CbusConstants.EVENT_DIR_IN) {
            sb.append(Bundle.getMessage("InEventsTooltip"));
        } else if (dr == CbusConstants.EVENT_DIR_OUT) {
            sb.append(Bundle.getMessage("OutEventsTooltip"));
        } else {
            sb.append(Bundle.getMessage("InOrOutEventsToolTip"));
        }        
        sb.append("\n");
        nextLine(sb.toString(), sb.toString(), "", index);
    }
    
    public void filterChanged(String text) {
        // log.debug("Cbus Console filter update");
        nextLine( text + " \n", text + " \n", "", -1);
    }

    public void highlightOff(int index) {
        // log.debug("Cbus Console highlight removed");
        nextLine( Bundle.getMessage("HighlightDisabled") + " \n", Bundle.getMessage("HighlightDisabled") + " \n", "", index);
    }

    public void sendPacketButtonActionPerformed(java.awt.event.ActionEvent e) {
        int j;
        int data, data2;
        CanMessage m = new CanMessage(tc.getCanid());
        data = parseBinDecHexByte(dynPriField.getText(), 2, _decimal, Bundle.getMessage("CbusConsoleTitle"), Bundle.getMessage("DynPriErrorDialog"));
        if (data == -1) {
            return;
        }
        data2 = parseBinDecHexByte(minPriField.getText(), 3, _decimal, Bundle.getMessage("CbusConsoleTitle"), Bundle.getMessage("MinPriErrorDialog"));
        if (data2 == -1) {
            return;
        }
        m.setHeader(data * 4 + data2);
        for (j = 0; j < 8; j++) {
            if (!dataFields[j].getText().isEmpty()) {
                data = parseBinDecHexByte(dataFields[j].getText(), 255, _decimal, Bundle.getMessage("CbusConsoleTitle"),
                        Bundle.getMessage("DbxErrorDialog", j));
                if (data == -1) {
                    return;
                }
                m.setElement(j, data);
                if (j == 0) {
                    data2 = data;
                }  // save OPC(d0) for later
            } else {
                break;
            }
        }
        if (j == 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("OpcErrorDialog"),
                    Bundle.getMessage("CbusConsoleTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Does the number of data match the opcode?
        // Subtract one as loop variable will have incremented
        if ((j - 1) != (data2 >> 5)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("OpcCountErrorDialog", (data2 >> 5)),
                    Bundle.getMessage("CbusConsoleTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        m.setNumDataElements(j);
        // Messages sent by us will not be forwarded back so add to display manually
        message(m);
        tc.sendCanMessage(m, this);
    }

    public void dataClearButtonActionPerformed(java.awt.event.ActionEvent e) {
        int j;
        dynPriField.setText("2");
        minPriField.setText("3");
        for (j = 0; j < 8; j++) {
            dataFields[j].setText("");
        }
    }

    synchronized public void statsClearButtonActionPerformed(java.awt.event.ActionEvent e) {
        _sent = 0;
        _rcvd = 0;
        _events = 0;
        _dcc = 0;
        _total = 0;
        sentCountField.setText("0");
        rcvdCountField.setText("0");
        eventsCountField.setText("0");
        dccCountField.setText("0");
        totalCountField.setText("0");
    }

    public void decimalCheckBoxActionPerformed(java.awt.event.ActionEvent e) {
        _decimal =decimalCheckBox.isSelected();
        decimalCheckBoxB.setSelected(_decimal);
        decimalCheckBoxC.setSelected(_decimal);        
    }
    
    public void decimalCheckBoxActionPerformedB(java.awt.event.ActionEvent e) {
        _decimal =decimalCheckBoxB.isSelected();
        decimalCheckBox.setSelected(_decimal);
        decimalCheckBoxC.setSelected(_decimal);
    }    
    
    public void decimalCheckBoxActionPerformedC(java.awt.event.ActionEvent e) {
        _decimal =decimalCheckBoxC.isSelected();
        decimalCheckBox.setSelected(_decimal);
        decimalCheckBoxB.setSelected(_decimal);
    }

    public void copyButtonActionPerformed(java.awt.event.ActionEvent e) {
        dynPriField.setText(lastDynPriField.getText());
        minPriField.setText(lastMinPriField.getText());
        for (int j = 0; j < 8; j++) {
            dataFields[j].setText(lastRxDataFields[j].getText());
        }
    }

    public void sendEvButtonActionPerformed(java.awt.event.ActionEvent e) {
        int nn, ev;
        CanMessage m = new CanMessage(tc.getCanid());
        nn = parseBinDecHexByte(nnField.getText(), 65535, _decimal, 
        Bundle.getMessage("CbusConsoleTitle"), Bundle.getMessage("SendEventNodeError"));
        if (nn == -1) {
            return;
        }
        ev = parseBinDecHexByte(evField.getText(), 65535, _decimal, 
        Bundle.getMessage("CbusConsoleTitle"), Bundle.getMessage("SendEventInvalidError"));
        if (ev == -1) {
            return;
        }
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        if (onButton.isSelected()) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACON);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASON);
            }
        } else {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACOF);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASOF);
            }
        }
        m.setElement(1, nn >> 8);
        m.setElement(2, nn & 0xff);
        m.setElement(3, ev >> 8);
        m.setElement(4, ev & 0xff);
        m.setNumDataElements(5);
        // Messages sent by us will not be forwarded back so add to display manually
        message(m);
        tc.sendCanMessage(m, this);
    }

    public synchronized String getCanFrameText() {
        return new String(linesBuffer[CAN]);
    }

    public synchronized String getCbusFrameText() {
        return new String(linesBuffer[CBUS]);
    }

    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "separately interlocked")
    PrintStream logStream = null;


    final StringBuilder[] linesBuffer = new StringBuilder[2];
    
    
    @Override
    public synchronized void message(CanMessage m) {  // process an outgoing message and log it
        if ( ( _filterFrame!=null ) && ( _filterFrame.filter(m)) ) {
            return;
        }
        sentCountField.setText(Integer.toString(++_sent));
        totalCountField.setText(Integer.toString(++_total));
        int opc = CbusMessage.getOpcode(m);
        if (CbusOpCodes.isEvent(opc)) {
            eventsCountField.setText(Integer.toString(++_events));
        }

        if (CbusOpCodes.isDcc(opc)) {
            dccCountField.setText(Integer.toString(++_dcc));
        }
        
        StringBuilder output = new StringBuilder();
        
        if (showarrowsCheckBox.isSelected()) {
            output.append(Bundle.getMessage("CBUS_OUT") + " ");
        }

        if (canidCheckBox.isSelected()) {
            output.append(Bundle.getMessage("CanID") + ": " + CbusMessage.getId(m) + " ");
        }
        
        if (showRtrCheckBox.isSelected()) {
            if (m.isRtr()) { 
                output.append(Bundle.getMessage("IsRtrFrame") + " ");
            } else { 
                output.append(Bundle.getMessage("IsNotRtrFrame") + " ");
            }
        }
        
        if (showOpcCheckBox.isSelected()) {
            output.append(decodeopc(m, m.isExtended(), m.getHeader())+ " ");
        }
        
        output.append(decode(m, m.isExtended(), m.getHeader()) + " ");

        if (showOpcExtraCheckBox.isSelected()) {
            if (!m.isExtended()) {
                String cbusopc = "CTIP_" + decodeopc(m, m.isExtended(), m.getHeader());
                output.append(Bundle.getMessage(cbusopc)+ " ");
            }
        }
        
        if (showAddressCheckBox.isSelected()) {
            output.append(" [" + CbusMessage.toAddress(m) + "] ");
        }
        
        if (showCanCheckBox.isSelected()) {
            output.append( m.toString() + " ");
        }   
        
        output.append("\n");

        nextLine( Bundle.getMessage("EventSent") + ": " + m.toMonitorString() + "\n",
                output.toString() ,
                Bundle.getMessage("DynPriTitle") + ": " + CbusMessage.getPri(m) / 4 + " " + 
                Bundle.getMessage("MinPriTitle") + ": " + (CbusMessage.getPri(m) & 3),
                (_highlightFrame != null) ? _highlightFrame.highlight(m) : -1);
                
        
    }

    @Override
    public synchronized void reply(CanReply r) {  // receive a reply message and log it
        if ( ( _filterFrame!=null ) && ( _filterFrame.filter(r) ) ) {
            return;
        }
        int j;
        // Capture most recent received packet
        if (_decimal) {
            lastDynPriField.setText(Integer.toString(CbusMessage.getPri(r) / 4));
            lastMinPriField.setText(Integer.toString(CbusMessage.getPri(r) & 3));
        } else {
            lastDynPriField.setText(Integer.toHexString(CbusMessage.getPri(r) / 4));
            lastMinPriField.setText(Integer.toHexString(CbusMessage.getPri(r) & 3));
        }
        // Pay attention to data length in op-code
        for (j = 0; j < (r.getElement(0) >> 5) + 1; j++) {
            if (_decimal) {
                lastRxDataFields[j].setText(Integer.toString(r.getElement(j)));
            } else {
                lastRxDataFields[j].setText(Integer.toHexString(r.getElement(j)));
            }
        }
        
        totalCountField.setText(Integer.toString(++_total));
        int opc = CbusMessage.getOpcode(r);
        if (CbusOpCodes.isEvent(opc)) {
            eventsCountField.setText(Integer.toString(++_events));
        }

        if (CbusOpCodes.isDcc(opc)) {
            dccCountField.setText(Integer.toString(++_dcc));
        }
        
        StringBuilder output = new StringBuilder();
        
        if (showarrowsCheckBox.isSelected()) {
            output.append(Bundle.getMessage("CBUS_IN") + " ");
        }

        if (canidCheckBox.isSelected()) {
            output.append(Bundle.getMessage("CanID") + ": " + CbusMessage.getId(r) + " ");
        }        

        if (showRtrCheckBox.isSelected()) {
            if (r.isRtr()) { 
                output.append(Bundle.getMessage("IsRtrFrame"));
            } else { 
                output.append(Bundle.getMessage("IsNotRtrFrame"));
            }
        }

        if (showOpcCheckBox.isSelected()) {
            output.append(decodeopc(r, r.isExtended(), r.getHeader())+ " ");
        }
        
        output.append(decode(r, r.isExtended(), r.getHeader()) + " ");

        if (showOpcExtraCheckBox.isSelected() && !r.isExtended() ) {
            String cbusopc = "CTIP_" + decodeopc(r, r.isExtended(), r.getHeader());
            output.append(Bundle.getMessage(cbusopc)+ " ");
        }

        if (showAddressCheckBox.isSelected()) {
            output.append(" [" + CbusMessage.toAddress(r) + "] ");
        }
        
        if (showCanCheckBox.isSelected()) {
            output.append(r.toString() + " ");
        }
        
        output.append("\n");
        
        nextLine( Bundle.getMessage("EventReceived") + ": " + r.toMonitorString() + "\n",
                output.toString(),
                Bundle.getMessage("DynPriTitle") + ": " + CbusMessage.getPri(r) / 4 + " " + 
                Bundle.getMessage("MinPriTitle") + ": " + (CbusMessage.getPri(r) & 3),
                (_highlightFrame != null) ? _highlightFrame.highlight(r) : -1);
        rcvdCountField.setText(Integer.toString(++_rcvd));
    }

    /**
     * Return a string representation of a decoded canMessage
     *
     * @param msg CanMessage to be decoded Return String decoded message
     */
    public String decode(AbstractMessage msg, Boolean ext, int header) {
        String str = CbusOpCodes.decode(msg, ext, header);
        return (str);
    }

    /**
     * Return an opc string of a canMessage
     *
     * @param msg CanMessage to be decoded
     */
    public String decodeopc(AbstractMessage msg, Boolean ext, int header) {
        String str = CbusOpCodes.decodeopc(msg, ext, header);
        return (str);
    }    
    
    
    /**
     * Parse a string for binary, decimal or hex byte value
     * <p>
     * 0b, 0d or 0x prefix will force parsing of binary, decimal or hex,
     * respectively. Entries with no prefix are parsed as decimal if decimal
     * flag is true, otherwise hex.
     *
     * @param s        string to be parsed
     * @param limit    upper bound of value to be parsed
     * @param decimal  flag for decimal or hex default
     * @param errTitle Title of error dialogue box if Number FormatException
     *                 encountered
     * @param errMsg   Message to be displayed if Number FormatException
     *                 encountered
     * @return the byte value, -1 indicates failure
     */
    public int parseBinDecHexByte(String s, int limit, boolean decimal, String errTitle, String errMsg) {
        int data = -1;
        boolean error = false;
        int radix = 16;

        if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0x")) {
            // hex, remove the prefix
            s = s.substring(2);
            radix = 16;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0d")) {
            // decimal, remove the prefix
            s = s.substring(2);
            radix = 10;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0b")) {
            // binary, remove the prefix
            s = s.substring(2);
            radix = 2;
        } else if (decimal) {
            radix = 10;
        }

        try {
            data = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            error = true;
        }
        if ((data < 0) || (data > limit)) {
            error = true;
        }
        if (error) {
            JOptionPane.showMessageDialog(null, errMsg,
                    errTitle, JOptionPane.ERROR_MESSAGE);
            data = -1;
        }
        return data;
    }

    // An instance of the private subclass of the default highlight painter
    Highlighter.HighlightPainter cbusHighlightPainter;  

    // A private subclass of the default highlight painter
    class CbusHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

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

    private final static Logger log = LoggerFactory.getLogger(CbusConsolePane.class);
}
