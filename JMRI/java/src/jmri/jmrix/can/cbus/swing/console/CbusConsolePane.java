package jmri.jmrix.can.cbus.swing.console;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
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
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Cbus Console
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public class CbusConsolePane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    protected JScrollPane jScrollPane1Can = new JScrollPane();
    protected JScrollPane jScrollPane1Cbus = new JScrollPane();
    protected JTextArea monTextPaneCan = new JTextArea();
    protected JTextArea monTextPaneCbus = new JTextArea();
    protected Highlighter cbusHighlighter;
    protected JButton startLogButton = new JButton();
    protected JButton stopLogButton = new JButton();
    protected JCheckBox timeCheckBox = new JCheckBox();
    protected JCheckBox priCheckBox = new JCheckBox();
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();

    protected JCheckBox showStatsCheckBox = new JCheckBox();
    protected JCheckBox showPacketCheckBox = new JCheckBox();
    protected JCheckBox showEventCheckBox = new JCheckBox();
    protected JButton filterButton = new JButton();
    protected JCheckBox decimalCheckBox = new JCheckBox();

    protected JTextField sentCountField = new JTextField("0", 8);
    protected JTextField rcvdCountField = new JTextField("0", 8);
    protected JButton statsClearButton = new JButton();

    protected JTextField lastDynPriField = new JTextField();
    protected JTextField lastMinPriField = new JTextField();
    protected JTextField[] lastRxDataFields = new JTextField[8];
    protected JButton copyButton = new JButton();

    protected JTextField dynPriField = new JTextField();
    protected JTextField minPriField = new JTextField();
    protected JTextField[] dataFields = new JTextField[8];
    protected JButton sendButton = new JButton();
    protected JButton dataClearButton = new JButton();

    protected JPanel statsPane;
    protected JPanel rxPane;
    protected JPanel sendPane;
    protected JPanel evPane;

    protected JRadioButton onButton = new JRadioButton();
    protected JRadioButton offButton = new JRadioButton();
    protected ButtonGroup onOffGroup = new ButtonGroup();
    protected JLabel nodeNumberLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("NodeNumberCol")));
    protected JLabel eventLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("EventCol")));
    protected JTextField nnField = new JTextField();
    protected JTextField evField = new JTextField();
    protected JButton sendEvButton = new JButton();

    protected int i;

    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    // members for handling the CBUS interface
    //CanMessage msg;
    TrafficController tc = null;

    String replyString;

    public CbusConsolePane() {
        super();
        _filterFrame = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("CbusConsoleTitle"));

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
        if (tc != null) {
            tc.removeCanListener(this);
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
        tc.addCanListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state
        _sent = 0;
        _rcvd = 0;

        clearButton.setText(Bundle.getMessage("ButtonClearScreen"));
        clearButton.setVisible(true);
        clearButton.setToolTipText(Bundle.getMessage("TooltipClearMonHistory"));

        freezeButton.setText(Bundle.getMessage("ButtonFreezeScreen"));
        freezeButton.setVisible(true);
        freezeButton.setToolTipText(Bundle.getMessage("TooltipStopScroll"));

        enterButton.setText(Bundle.getMessage("ButtonAddMessage"));
        enterButton.setVisible(true);
        enterButton.setToolTipText(Bundle.getMessage("TooltipAddMessage"));

        monTextPaneCan.setVisible(true);
        monTextPaneCan.setToolTipText(Bundle.getMessage("TooltipMonTextPane"));
        monTextPaneCan.setEditable(false);

        monTextPaneCbus.setVisible(true);
        monTextPaneCbus.setToolTipText(Bundle.getMessage("TooltipMonTextPane"));
        monTextPaneCbus.setEditable(false);
        cbusHighlighter = monTextPaneCbus.getHighlighter();

        entryField.setToolTipText(Bundle.getMessage("TooltipEntryPane", Bundle.getMessage("ButtonAddMessage")));

        // fix a width for raw field for current character set
        JTextField tCan = new JTextField(35);
        tCan.setDragEnabled(true);
        int x = jScrollPane1Can.getPreferredSize().width + tCan.getPreferredSize().width;
        int y = jScrollPane1Can.getPreferredSize().height + 10 * tCan.getPreferredSize().height;

        jScrollPane1Can.getViewport().add(monTextPaneCan);
        jScrollPane1Can.setPreferredSize(new Dimension(x, y));
        jScrollPane1Can.setVisible(true);

        // Add a nice border
        jScrollPane1Can.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CanFrameTitle")));

        // fix a width for Cbus field for current character set
        JTextField tCbus = new JTextField(40);
        tCbus.setDragEnabled(true);
        x = jScrollPane1Cbus.getPreferredSize().width + tCbus.getPreferredSize().width;
        y = jScrollPane1Cbus.getPreferredSize().height + 10 * tCbus.getPreferredSize().height;

        // Add a nice border
        jScrollPane1Cbus.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CbusMessageTitle")));

        jScrollPane1Cbus.getViewport().add(monTextPaneCbus);
        jScrollPane1Cbus.setPreferredSize(new Dimension(x, y));
        jScrollPane1Cbus.setVisible(true);

        startLogButton.setText(Bundle.getMessage("ButtonStartLogging"));
        startLogButton.setVisible(true);
        startLogButton.setToolTipText(Bundle.getMessage("TooltipStartLogging"));

        stopLogButton.setText(Bundle.getMessage("ButtonStopLogging"));
        stopLogButton.setVisible(true);
        stopLogButton.setToolTipText(Bundle.getMessage("TooltipStopLogging"));

        timeCheckBox.setText(Bundle.getMessage("ButtonShowTimestamps"));
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps"));

        priCheckBox.setText(Bundle.getMessage("ButtonShowPriorities"));
        priCheckBox.setVisible(true);
        priCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPrios"));

        openFileChooserButton.setText(Bundle.getMessage("ButtonChooseLogFile"));
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText(Bundle.getMessage("TooltipChooseLogFile"));

        showStatsCheckBox.setText(Bundle.getMessage("ButtonShowStats"));
        showStatsCheckBox.setVisible(true);
        showStatsCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPacketStats"));

        showPacketCheckBox.setText(Bundle.getMessage("ButtonShowPackets"));
        showPacketCheckBox.setVisible(true);
        showPacketCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPackets"));

        showEventCheckBox.setText(Bundle.getMessage("ButtonShowEvents"));
        showEventCheckBox.setVisible(true);
        showEventCheckBox.setToolTipText(Bundle.getMessage("TooltipShowEvents"));

        filterButton.setText(Bundle.getMessage("ButtonFilter"));
        filterButton.setVisible(true);
        filterButton.setToolTipText(Bundle.getMessage("TooltipFilter"));

        sentCountField.setToolTipText(Bundle.getMessage("TooltipSent"));
        sentCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SentTitle")));

        rcvdCountField.setToolTipText(Bundle.getMessage("TooltipReceived"));
        rcvdCountField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("ReceivedTitle")));

        statsClearButton.setText(Bundle.getMessage("ButtonClear"));
        statsClearButton.setVisible(true);
        statsClearButton.setToolTipText(Bundle.getMessage("TooltipClearCounters"));

        decimalCheckBox.setText(Bundle.getMessage("ButtonDecimal"));
        decimalCheckBox.setVisible(true);
        decimalCheckBox.setToolTipText(Bundle.getMessage("TooltipDecimal"));
        _decimal = true;
        decimalCheckBox.setSelected(_decimal);

        sendButton.setText(Bundle.getMessage("ButtonSend"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("TooltipSendPacket"));

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

        setLayout(new BorderLayout());

        // add items to GUI
        // Pane to hold packet history
        JPanel historyPane = new JPanel();
        historyPane.setLayout(new BorderLayout());
        historyPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("PacketHistoryTitle")));

        // sub-pane to hold scrolling text boxes
        JPanel paneB = new JPanel();
        // Constrain scroll panels to be side-by-side
        paneB.setLayout(new BoxLayout(paneB, BoxLayout.X_AXIS));
        paneB.add(jScrollPane1Can);
        paneB.add(jScrollPane1Cbus);
        historyPane.add(paneB, BorderLayout.CENTER);

        // Sub-pane to hold buttons
        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(clearButton);
        pane1.add(freezeButton);
        pane1.add(timeCheckBox);
        pane1.add(priCheckBox);
        paneA.add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(openFileChooserButton);
        pane2.add(startLogButton);
        pane2.add(stopLogButton);
        pane2.add(filterButton);
        paneA.add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(enterButton);
        pane3.add(entryField);
        paneA.add(pane3);

        historyPane.add(paneA, BorderLayout.SOUTH);
        add(historyPane, BorderLayout.CENTER);

        JPanel southPane = new JPanel();
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));

        // Pane for network statistics
        statsPane = new JPanel();

        statsPane.setVisible(false); // initial state is not displayed
        showStatsCheckBox.setSelected(false);

        statsPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("StatisticsTitle")));
        statsPane.add(sentCountField);
        statsPane.add(rcvdCountField);
        statsPane.add(statsClearButton);

        southPane.add(statsPane);
        showStatsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showStatsCheckBox.isSelected()) {
                    statsPane.setVisible(true);
                    //statsPane.revalidate();
                    packInside();
                    //statsPane.repaint();
                } else {
                    statsPane.setVisible(false);
                    //statsPane.revalidate();
                    packInside();
                    //statsPane.repaint();
                }
            }
        });

        // Pane for most recently recived packet
        rxPane = new JPanel();
        rxPane.setLayout(new BoxLayout(rxPane, BoxLayout.X_AXIS));
        rxPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("MostRecentPacketTitle")));

        // Construct data fields for Priority and up to 8 bytes
        lastDynPriField = new JTextField("", 4);
        lastDynPriField.setToolTipText(Bundle.getMessage("TooltipDynPri"));
        lastDynPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("DynPriTitle")));
        rxPane.add(lastDynPriField);
        lastMinPriField = new JTextField("", 4);
        lastMinPriField.setToolTipText(Bundle.getMessage("TooltipMinPri"));
        lastMinPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("MinPriTitle")));
        rxPane.add(lastMinPriField);
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
            rxPane.add(lastRxDataFields[i]);
        }
        rxPane.add(copyButton);
//        rxPane.setVisible(false);
//        getContentPane().add(rxPane);
        southPane.add(rxPane);

        // Pane for constructing packet to send
        sendPane = new JPanel();
        sendPane.setLayout(new BoxLayout(sendPane, BoxLayout.X_AXIS));
        sendPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SendPacketTitle")));

        // Construct data fields for Priority and up to 8 bytes
        dynPriField = new JTextField("2", 4);
        dynPriField.setToolTipText(Bundle.getMessage("TooltipDynPri"));
        dynPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("DynPriTitle")));
        sendPane.add(dynPriField);
        minPriField = new JTextField("3", 4);
        minPriField.setToolTipText(Bundle.getMessage("TooltipDinPri03"));
        minPriField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("MinPriTitle")));
        sendPane.add(minPriField);
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
            sendPane.add(dataFields[i]);
        }
        sendPane.add(sendButton);
        sendPane.add(dataClearButton);
        sendPane.setVisible(false);

        southPane.add(sendPane);

        showPacketCheckBox.setSelected(false);
        rxPane.setVisible(false); // initial state is not displayed
        sendPane.setVisible(false);

        showPacketCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPacketCheckBox.isSelected()) {
                    rxPane.setVisible(true);
                    //rxPane.revalidate();
                    sendPane.setVisible(true);
                    //sendPane.revalidate();
                    packInside();
                    //rxPane.repaint();
                    //sendPane.repaint();
                } else {
                    rxPane.setVisible(false);
                    //rxPane.revalidate();
                    sendPane.setVisible(false);
                    //sendPane.revalidate();
                    packInside();
                    //rxPane.repaint();
                    //sendPane.repaint();
                }
            }
        });

        // Pane for constructing event to send
        evPane = new JPanel();

        evPane.setVisible(true); // initial state is not displayed
        showEventCheckBox.setSelected(true);

        evPane.setLayout(new BoxLayout(evPane, BoxLayout.X_AXIS));
        evPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("ButtonSendEvent")));

        nnField = new JTextField("0", 5);
        nnField.setToolTipText("<html>" + Bundle.getMessage("ToolTipNodeNumber") + "<br>" +
                Bundle.getMessage("ToolTipPrefix") + "</html>");
        evPane.add(nodeNumberLabel);
        evPane.add(nnField);

        evField = new JTextField("0", 5);
        evField.setToolTipText("<html>" + Bundle.getMessage("ToolTipEvent") + "<br>" +
                Bundle.getMessage("ToolTipPrefix") + "</html>");
        evPane.add(eventLabel);
        evPane.add(evField);

        onOffGroup.add(onButton);
        onOffGroup.add(offButton);
        evPane.add(onButton);
        evPane.add(offButton);

        evPane.add(sendEvButton);
        evPane.setVisible(true);
//        getContentPane().add(evPane);
        southPane.add(evPane);

        // Pane to select display type
        JPanel showPane = new JPanel();
        showPane.setLayout(new BoxLayout(showPane, BoxLayout.X_AXIS));
        showPane.add(showStatsCheckBox);
        showPane.add(showPacketCheckBox);
        showPane.add(showEventCheckBox);
        showPane.add(decimalCheckBox);
//        getContentPane().add(showPane);
        southPane.add(showPane);

        add(southPane, BorderLayout.SOUTH);

        showEventCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showEventCheckBox.isSelected()) {
                    evPane.setVisible(true);
                    //evPane.revalidate();
                    packInside();
                    //evPane.repaint();
                } else {
                    evPane.setVisible(false);
                    //evPane.revalidate();
                    packInside();
                    //evPane.repaint();
                }
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
            }
        });
        stopLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopLogButtonActionPerformed(e);
            }
        });
        openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openFileChooserButtonActionPerformed(e);
            }
        });

        filterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                filterButtonActionPerformed(e);
            }
        });

        enterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enterButtonActionPerformed(e);
            }
        });

        copyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                copyButtonActionPerformed(e);
            }
        });

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

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

        sendEvButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendEvButtonActionPerformed(e);
            }
        });

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));

        linesBuffer[CAN] = new StringBuffer();
        linesBuffer[CBUS] = new StringBuffer();
    }

    /**
     * Special version of pack that holds the overall frame size constant.
     */
    void packInside() {
        if (getTopLevelAncestor() != null) {
            Dimension d = ((javax.swing.JFrame) getTopLevelAncestor()).getSize();
            ((javax.swing.JFrame) getTopLevelAncestor()).setMinimumSize(d);
            ((javax.swing.JFrame) getTopLevelAncestor()).setPreferredSize(d);
            ((javax.swing.JFrame) getTopLevelAncestor()).pack();
        }
    }

    public void nextLine(String line, String decoded, String priorities, int filter) {
        // Handle display of traffic.
        // line is the traffic in 'normal form',
        // decoded is the decoded, protocol specific, form.
        // Both should contain the same number of well-formed lines, e.g. end
        // with \n
        StringBuffer sbCan = new StringBuffer(80);
        StringBuffer sbCbus = new StringBuffer(80);
        final int filterIndex = filter;
        log.debug("_filterFrame: " + _filterFrame + " filter: " + filter);
        if (filterIndex >= 0) {
            final Color filterColor = _filterFrame.getColor(filter);
            cbusHighlightPainter = new CbusHighlightPainter(filterColor);
        }

        // display the timestamp if requested
        if (timeCheckBox.isSelected()) {
            sbCan.append(df.format(new Date())).append(": ");
            sbCbus.append(df.format(new Date())).append(": ");
        }

        // display CBUS the priorities if requested
        if (priCheckBox.isSelected()) {
            sbCbus.append((priorities + " "));
        }

        if (filterIndex >= 0) {
            sbCan.append(("Filter " + (filterIndex + 1) + ": ")); // TODO I18N
            sbCbus.append(("Filter " + (filterIndex + 1) + ": ")); // TODO NOI18N
        }

        // display decoded data
        sbCan.append(line);
        sbCbus.append(decoded);
//        synchronized( linesBufferCbus ) {
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
                        monTextPaneCan.append(linesBuffer[CAN].toString());
                        monTextPaneCbus.append(linesBuffer[CBUS].toString());
                        final int end = monTextPaneCbus.getText().length();
                        int LineCount = monTextPaneCan.getLineCount();
                        if (LineCount > MAX_LINES) {
                            LineCount -= MAX_LINES;
                            try {
                                int offset = monTextPaneCan.getLineStartOffset(LineCount);
                                monTextPaneCan.getDocument().remove(0, offset);
                                monTextPaneCbus.getDocument().remove(0, offset);
                            } catch (BadLocationException ex) {
                            }
                        }
                        try {
                            if (filterIndex >= 0) {
                                log.debug("Add highlight start: " + start + " end: " + end);
                                cbusHighlighter.addHighlight(start, end - 1, cbusHighlightPainter);
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
                StringBuffer out = new StringBuffer(sbCbus.length() + 10);  // arbitrary guess at space
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

    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText() + "\n", entryField.getText() + "\n", "", -1);
    }

    public void filterButtonActionPerformed(java.awt.event.ActionEvent e) {
        log.debug("Cbus Console filter button action performed");
        if (_filterFrame == null) {
            _filterFrame = new CbusEventFilterFrame(this);
            try {
                _filterFrame.initComponents();
            } catch (Exception ex) {
                log.error("Exception: " + ex.toString());
            }
            _filterFrame.setVisible(true);
        } else {
            _filterFrame.setState(Frame.NORMAL);
            _filterFrame.setVisible(true);
        }
    }

//    public void filterFrameClosed() {
//        log.debug("Cbus Console filter frame closed");
//        nextLine("All filters removed\n", "All filters removed\n", "", -1);
//        _filterFrame = null;
//    }
    public void filterOn(int index, int nn, boolean nnEn, int ev, boolean evEn, int ty) {
        log.debug("Cbus Console filter applied");
        StringBuffer sb = new StringBuffer(80);
        if (nnEn) {
            sb.append(("Node " + nn + " "));
        }
        if (evEn) {
            sb.append(("Event " + ev + " "));
        }
        if (ty == CbusConstants.EVENT_ON) {
            sb.append("ON"); // TODO I18N
        } else if (ty == CbusConstants.EVENT_OFF) {
            sb.append("OFF"); // TODO I18N
        } else {
            sb.append("On or OFF"); // TODO I18N
        }
        sb.append("\n");
        nextLine(sb.toString(), sb.toString(), "", index);
    }

    public void filterOff(int index) {
        log.debug("Cbus Console filter removed");
        nextLine(" closed\n", " closed\n", "", index);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
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
            if (!dataFields[j].getText().equals("")) {
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
        sentCountField.setText("0");
        rcvdCountField.setText("0");
    }

    public void decimalCheckBoxActionPerformed(java.awt.event.ActionEvent e) {
        if (decimalCheckBox.isSelected()) {
            _decimal = true;
        } else {
            _decimal = false;
        }
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
        nn = parseBinDecHexByte(nnField.getText(), 65535, _decimal, Bundle.getMessage("CbusConsoleTitle"), Bundle.getMessage("SendEventNodeError"));
        if (nn == -1) {
            return;
        }
        ev = parseBinDecHexByte(evField.getText(), 65535, _decimal, Bundle.getMessage("CbusConsoleTitle"), Bundle.getMessage("SendEventInvalidError"));
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

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    final StringBuffer[] linesBuffer = new StringBuffer[2];
    static private int CAN = 0;
    static private int CBUS = 1;
    static private int MAX_LINES = 500;

    @Override
    public synchronized void message(CanMessage m) {  // receive a message and log it TODO I18N?
        nextLine("sent: " + m.toString() + "\n",
                "ID:" + CbusMessage.getId(m) + " " + (m.isRtr() ? "R " : "N ") + decode(m, m.isExtended(), m.getHeader()) + " [" + CbusMessage.toAddress(m) + "]\n",
                "Dyn Pri:" + CbusMessage.getPri(m) / 4 + " Min Pri:" + (CbusMessage.getPri(m) & 3),
                (_filterFrame != null) ? _filterFrame.filter(m) : -1);
        sentCountField.setText(Integer.toString(++_sent));
    }

    @Override
    public synchronized void reply(CanReply r) {  // receive a reply message and log it TODO I18N?
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
        nextLine("rcvd: " + r.toString() + "\n",
                "ID:" + CbusMessage.getId(r) + " " + (r.isRtr() ? "R " : "N ") + decode(r, r.isExtended(), r.getHeader()) + " [" + CbusMessage.toAddress(r) + "]\n",
                "Dyn Pri:" + CbusMessage.getPri(r) / 4 + " Min Pri:" + (CbusMessage.getPri(r) & 3),
                (_filterFrame != null) ? _filterFrame.filter(r) : -1);
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
     * Parse a string for binary, decimal or hex byte value
     * <P>
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

    transient private int _sent;
    transient private int _rcvd;
    private boolean _decimal;
    private CbusEventFilterFrame _filterFrame;

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
