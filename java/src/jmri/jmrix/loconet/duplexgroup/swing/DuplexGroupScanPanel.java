package jmri.jmrix.loconet.duplexgroup.swing;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.duplexgroup.LnDplxGrpInfoImplConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a GUI and associated logic to perform energy scan operations on
 * Duplex radio channels. Displays energy scan data in a graphical form.
 * <p>
 * This tool works equally well with UR92 and UR92CE devices. The UR92 and
 * UR92CE behave identically with respect to this tool. For the purpose of
 * clarity, only the term UR92 is used herein.
 *
 * @author B. Milhaupt Copyright 2010, 2011
 */
public class DuplexGroupScanPanel extends jmri.jmrix.loconet.swing.LnPanel
        implements LocoNetListener, javax.swing.event.ChangeListener {

    DuplexChannelInfo dci[] = new DuplexChannelInfo[LnDplxGrpInfoImplConstants.DPLX_MAX_CH - LnDplxGrpInfoImplConstants.DPLX_MIN_CH + 1];
    private javax.swing.Timer tmr;
    DuplexGroupScanPanel safe;

    private final static int DEFAULT_SCAN_COUNT = 25;
    private boolean isInitialized = false;

    public DuplexGroupScanPanel() {
        super();
        memo = null;
        safe = this;
    }

    javax.swing.JButton scanLoopButton = null;
    javax.swing.JLabel scanLoopLabel = null;
    javax.swing.JButton clearButton = null;
    javax.swing.JLabel grStatusValue = null;
    boolean stopRequested;
    Integer scanLoopDelay;
    boolean waitingForPreviousGroupChannel;
    int previousGroupChannel;
//    Dimension channelTextSize;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        int i;
        int j;
        int minWindowWidth = 0;
        JPanel p;
        j = 0;

        for (i = LnDplxGrpInfoImplConstants.DPLX_MIN_CH; i <= LnDplxGrpInfoImplConstants.DPLX_MAX_CH; ++i) {
            dci[j] = new DuplexChannelInfo();
            dci[j].channel = i;
            dci[j].numSamples = 0;
            dci[j].maxSigValue = -1;
            dci[j].minSigValue = 256;
            dci[j].sumSamples = 0;
            dci[j].avgSamples = 0;
            dci[j].mostRecentSample = -1;
            j++;
        }

        grStatusValue = new javax.swing.JLabel(" ");
        clearButton = new javax.swing.JButton(Bundle.getMessage("ButtonClearScanData"));
        scanLoopButton = new javax.swing.JButton(Bundle.getMessage("ButtonScanChannelsLoop"));
        clearButton.setToolTipText(Bundle.getMessage("ToolTipButtonClearScanData"));
        scanLoopButton.setToolTipText(Bundle.getMessage("ToolTipButtonScanChannelsLoop"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        p = new JPanel();
        graphicArea = new DuplexGroupChannelScanGuiCanvas();
        p.add(graphicArea);
        add(p);

        p = new JPanel();
        p.setLayout(new java.awt.GridLayout(4, 1));

        JLabel graphicAreaLabel1 = new JLabel(Bundle.getMessage("LabelGraphicArea1"));
        graphicAreaLabel1.setFont(new Font("Dialog", Font.PLAIN, 10));
        p.add(graphicAreaLabel1);

        JLabel graphicAreaLabel2 = new JLabel(Bundle.getMessage("LabelGraphicArea2"));
        graphicAreaLabel2.setFont(new Font("Dialog", Font.PLAIN, 10));
        p.add(graphicAreaLabel2);

        JLabel graphicAreaLabel3 = new JLabel(Bundle.getMessage("LabelGraphicArea3"));
        graphicAreaLabel3.setFont(new Font("Dialog", Font.PLAIN, 10));
        p.add(graphicAreaLabel3);
        add(p);

        JLabel graphicAreaLabel4 = new JLabel(Bundle.getMessage("LabelGraphicArea4"));
        graphicAreaLabel4.setFont(new Font("Dialog", Font.PLAIN, 10));
        p.add(graphicAreaLabel4);
        add(p);

        p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(clearButton);
        p.add(scanLoopButton);
        stopRequested = false;
        add(p);

        p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        add(new JSeparator());
        p.add(grStatusValue);
        add(p);
        p = new JPanel();
        // Apply a rigid area with a width that is wide enough to display the longest status message
        try {
            minWindowWidth = Integer.parseInt(Bundle.getMessage("MinimumWidthForWindow"), 10);
        } catch (Exception e) {
            minWindowWidth = 400;
        }

        p.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(minWindowWidth, 0)));
        add(p);

        scanLoopButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (scanLoopButton.getText().equals(Bundle.getMessage("ButtonScanChannelsStop"))) {
                    scanLoopStopButtonActionPerformed();
                } else {
                    scanLoopButton.setText(Bundle.getMessage("ButtonScanChannelsStop"));
                    scanLoopButtonActionPerformed();
                }
            }
        });

        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                scanLoopStopButtonActionPerformed();
                clearButtonActionPerformed();
                graphicArea.repaint();
            }
        });

        // send message to get current Duplex Channel number
        try {
            scanLoopDelay = Integer.parseInt(Bundle.getMessage("SetupDefaultChannelDelayInMilliSec"));
        } catch (Exception e) {
            log.error("Bad value in prop files for SetupDefaultChannelDelayInMilliSec.");
            scanLoopDelay = 200;
        }
        if (memo != null) {
            isInitialized = true;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel"; // NOI18N replacement UR92
    } // NOI18N

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("ScanTitle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        // connect to the LnTrafficController
        connect(memo.getLnTrafficController());
        waitingForPreviousGroupChannel = true;
        memo.getLnTrafficController().sendLocoNetMessage(createGetGroupChannelPacketInt());
        if (grStatusValue != null) {
            isInitialized = true;
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Process all incoming LocoNet messages to look for Duplex Group
     * information operations. Only pays attention to LocoNet report of Duplex
     * Group Name/password/channel/groupID, and ignores all other LocoNet
     * messages.
     * <p>
     * If tool has sent a query for Duplex group information and has not yet
     * received a Duplex group report, the method updates the GUI with the
     * received information.
     * <p>
     * If the tool is not currently waiting for a response to a query, then the
     * method compares the received information against the information
     * currently displayed in the GUI. If the received information does not
     * match, a message is displayed on the status line in the GUI, else nothing
     * is displayed in the GUI status line.
     */
    @Override
    public void message(LocoNetMessage m) {
        if (stopRequested == true) {
            return;
        }
        if (handleMessageDuplexScanReport(m)) {
            return;
        }
        if (handleMessageDuplexChannelReport(m)) {
            return;
        }
        return;
    }

    /**
     * Examines incoming LocoNet messages to see if the message is a Duplex
     * Group Channel Report. If so, captures the group number.
     *
     * @param m  incoming LocoNetMessage
     * @return true if message m is a Duplex Group Channel Report
     */
    private boolean handleMessageDuplexChannelReport(LocoNetMessage m) {
        if ((m.getOpCode() != LnConstants.OPC_PEER_XFER)
                || (m.getElement(1) != LnConstants.RE_DPLX_OP_LEN)
                || (m.getElement(2) != LnConstants.RE_DPLX_GP_CHAN_TYPE)
                || (m.getElement(3) != LnConstants.RE_DPLX_SCAN_REPORT_B3)) {
            return false;
        }
        if (waitingForPreviousGroupChannel) {
            waitingForPreviousGroupChannel = false;
            previousGroupChannel = m.getElement(5);  // capture Group Channel Number
        }
        return true;
    }

    /**
     * Interprets a received LocoNet message. If message is an IPL report of
     * attached IPL-capable equipment, check to see if it reports a UR92 device
     * as attached. If so, increment count of UR92 devices. Else ignore.
     *
     * @return true if message is an IPL device report indicating a UR92
     *         present, else return false.
     */
    private boolean handleMessageDuplexScanReport(LocoNetMessage m) {
        if ((m.getOpCode() != LnConstants.OPC_PEER_XFER)
                || (m.getElement(1) != LnConstants.RE_DPLX_SCAN_OP_LEN)
                || (m.getElement(2) != LnConstants.RE_DPLX_SCAN_REPORT_B2)
                || (m.getElement(3) != LnConstants.RE_DPLX_SCAN_REPORT_B3)) {
            return false;
        }
        handleChannelSignalReport(m.getElement(4), m.getElement(5), m.getElement(6));
        return true;
    }

    private void handleChannelSignalReport(int extendedVal, int channelNum, int signalValue) {
        int index = -1;
        int fullSignal;
        fullSignal = signalValue + 128 * (((extendedVal & 0x2) == 2) ? 1 : 0);
        for (int i = 0; i < dci.length; i++) {
            if (dci[i].channel == channelNum) {
                index = i;
            }
        }
        if (index != -1) {
            if (index == 16) {
                log.error(Bundle.getMessage("ErrorLogUnexpectedChannelNumber", channelNum) + "\n");

            }
            dci[index].numSamples++;
            dci[index].mostRecentSample = fullSignal;
            if (fullSignal > dci[index].maxSigValue) {
                dci[index].maxSigValue = fullSignal;
            }
            if (fullSignal < dci[index].minSigValue) {
                dci[index].minSigValue = fullSignal;
            }
            dci[index].sumSamples += fullSignal;
            dci[index].avgSamples = dci[index].sumSamples / dci[index].numSamples;

            graphicArea.repaint();

        } else {
            log.error(Bundle.getMessage("ErrorLogUnexpectedChannelNumber", channelNum) + "\n");
        }
    }

    /**
     * Creates a LocoNet message containing a channel-specific query for signal
     * information from UR92 device(s).
     *
     * @param channelNum  integer between 11 and 26, inclusive
     * @return LocoNetMessage - query for Dulpex Channel Scan information
     */
    private LocoNetMessage createDuplexScanQueryPacket(int channelNum) {
        int i = 0;
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);

        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_SCAN_QUERY_B2);   // Duplex Group Scan Query type
        m.setElement(i++, LnConstants.RE_DPLX_SCAN_QUERY_B3);   // Query Operation
        m.setElement(i++, LnConstants.RE_DPLX_SCAN_QUERY_B4);
        m.setElement(i++, channelNum);                                  // Duplex Channel Number
        for (; i < (LnConstants.RE_DPLX_OP_LEN - 1); i++) {
            m.setElement(i, 0);   // always 0 for duplex group ID write
        }
        // LocoNet send process will compute and add checksum byte in correct location
        return m;
    }

    /**
     * Create a LocoNet packet to get the current Duplex group channel number.
     *
     * @return The packet which writes the Group Channel Number to the UR92
     *         device(s)
     */
    private LocoNetMessage createGetGroupChannelPacketInt() {
        int i;

        // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);

        i = 0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_CHAN_TYPE);   // Group Channel Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_QUERY);   // Write Operation
        for (; i < (LnConstants.RE_DPLX_OP_LEN - 1); i++) {
            m.setElement(i, 0);   // always 0 for duplex group channel query
        }
        // LocoNet send process will compute and add checksum byte in correct location
        return m;
    }

    int channelIndexToScan;
    int maxChannelIndexToScan;
    int loopNum;
    Integer whenToStop;

    private void updateScanLoopCountStatus(int current, int total) {
//        String countStatus = Bundle.getMessage("StatusCurrentLoopCounter"); // much easier using Bundle.getMessage variables
//        String begin = countStatus.substring(0, countStatus.indexOf("%count")); // NOI18N
//
//        String middle = countStatus.substring(begin.length() + 6, countStatus.indexOf("%loops")); // NOI18N
//        String end = countStatus.substring(countStatus.indexOf("%loops") + 6); // NOI18N
//        countStatus = begin + Integer.toString(current) + middle + Integer.toString(total) + end;
        grStatusValue.setText(Bundle.getMessage("StatusCurrentLoopCounter", current, total));
    }

    private void scanLoopButtonActionPerformed() {
        loopNum = 1;
        try {
            whenToStop = Integer.parseInt(Bundle.getMessage("SetupNumberOfLoops"));
        } catch (Exception e) {
            whenToStop = DEFAULT_SCAN_COUNT;
        }
        if ((whenToStop <= 0) || (whenToStop > 1000)) {
            grStatusValue.setText(Bundle.getMessage("ErrorBadLoopCount"));
            return;
        }
        grStatusValue.setText(" ");
        stopRequested = false;

        channelIndexToScan = 0;
        maxChannelIndexToScan = LnDplxGrpInfoImplConstants.DPLX_MAX_CH - LnDplxGrpInfoImplConstants.DPLX_MIN_CH;
        updateScanLoopCountStatus(loopNum, whenToStop);

        tmr = new javax.swing.Timer(scanLoopDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tmr.stop();
                if (stopRequested == true) {
                    stopRequested = false;
                    showOnlyMaxAvgValues();
                } else if (channelIndexToScan <= maxChannelIndexToScan) {

                    graphicArea.setChannelBeingScanned(dci[channelIndexToScan].channel);
                    graphicArea.repaint();
                    memo.getLnTrafficController().sendLocoNetMessage(createDuplexScanQueryPacket(dci[channelIndexToScan].channel));
                    tmr.setInitialDelay(scanLoopDelay);
                    tmr.setRepeats(false);
                    tmr.start();
                    channelIndexToScan++;
                } else if (loopNum < whenToStop) {
                    loopNum++;
                    // update displayed Loop Count
                    updateScanLoopCountStatus(loopNum, whenToStop);

                    channelIndexToScan = 0;
                    graphicArea.setChannelBeingScanned(dci[channelIndexToScan].channel);
                    graphicArea.repaint();

                    memo.getLnTrafficController().sendLocoNetMessage(createDuplexScanQueryPacket(dci[channelIndexToScan].channel));
                    tmr.setInitialDelay(scanLoopDelay);
                    tmr.setRepeats(false);
                    tmr.start();
                    channelIndexToScan++;
                } else {
                    // must be done with all channels and all loops.
                    showOnlyMaxAvgValues();
                    scanLoopButton.setText(Bundle.getMessage("ButtonScanChannelsLoop"));
                    scanLoopStopButtonActionPerformed();
                    grStatusValue.setText(" ");
                    graphicArea.setChannelBeingScanned(-1);
                    graphicArea.repaint();
                }
            }
        });
        // need to trigger first delay to get first channel to be scanned
        tmr.setInitialDelay(scanLoopDelay);
        tmr.setRepeats(false);
        tmr.start();
        return;
    }

    private void scanLoopStopButtonActionPerformed() {
        scanLoopButton.setText(Bundle.getMessage("ButtonScanChannelsLoop"));
        graphicArea.setChannelBeingScanned(-1);
        graphicArea.repaint();
        grStatusValue.setText(" ");
        stopRequested = true;
    }

    private void clearButtonActionPerformed() {
        int index;
        int maxIndex;
        maxIndex = LnDplxGrpInfoImplConstants.DPLX_MAX_CH - LnDplxGrpInfoImplConstants.DPLX_MIN_CH;
        for (index = 0; index <= maxIndex; ++index) {
            dci[index].numSamples = 0;
            dci[index].maxSigValue = -1;
            dci[index].minSigValue = 256;
            dci[index].sumSamples = 0;
            dci[index].avgSamples = 0;
            dci[index].mostRecentSample = -1;
        }
        return;
    }

    public void connect(LnTrafficController t) {
        if (t != null) {
            // connect to the LnTrafficController if the connection is a valid LocoNet connection
            t.addLocoNetListener(~0, this);
        }
    }

    /**
     * Break connection with the LnTrafficController and stop timers.
     */
    @Override
    public void dispose() {
        javax.swing.Timer exitTmr;

        stopRequested = true;
        if (tmr != null) {
            tmr.stop();
        }
        tmr = null;

        if (waitingForPreviousGroupChannel == false) {
            exitTmr = new javax.swing.Timer(200, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (memo.getLnTrafficController() != null) {
                        memo.getLnTrafficController().removeLocoNetListener(~0, safe);
                    }
                    safe.dispose();
                }
            });
            exitTmr.setInitialDelay(200);
            exitTmr.setRepeats(false);
            exitTmr.start();
            while (exitTmr.isRunning()) {
                // wait for timer to run out before releasing LocoNet traffic controller listener
            }
            exitTmr.stop();
        }
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(DuplexGroupScanPanel.class);

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        graphicArea.repaint();
    }

    private void showOnlyMaxAvgValues() {
        for (int i = 0; i < (LnDplxGrpInfoImplConstants.DPLX_MAX_CH - LnDplxGrpInfoImplConstants.DPLX_MIN_CH) + 1; ++i) {
            dci[i].mostRecentSample = -1;
        }
        graphicArea.repaint();
    }

    private DuplexGroupChannelScanGuiCanvas graphicArea;

    private class DuplexGroupChannelScanGuiCanvas extends java.awt.Canvas {

        private int barWidth = 7;
        private int barSpace = barWidth + 8;
        private int barOffset = (barSpace - barWidth) / 2;
        private final static int channelCount = 26 - 11 + 1;
        private final static int barGraphScale = 2;
        private final static int maxScanValue = 255;
        private final static int maxScaledBarValue = ((maxScanValue + 1) / barGraphScale);
        private int baseline = maxScaledBarValue + 5;

        public int requiredMinWindowWidth = (channelCount * barSpace);
        public int requiredMinWindowHeight = (baseline + 10);
        private static final int HORIZ_PADDING = 12;
        private static final int VERT_PADDING = 4;
        private int indexBeingScanned = -1;
        private Dimension channelTextSize;

        Font signalBarsFont;
        private final java.awt.Color foregroundColor = java.awt.Color.WHITE;
        private final java.awt.Color backgroundColor = java.awt.Color.BLACK;
        private final java.awt.Color recommendationLineColor = java.awt.Color.YELLOW;
        private final java.awt.Color valueBarColor = java.awt.Color.CYAN;
        private final java.awt.Color maxLineColor = java.awt.Color.RED;
        private final java.awt.Color averageLineColor = java.awt.Color.GREEN;
        private final java.awt.Color lowerLimitLineColor = java.awt.Color.LIGHT_GRAY;

        public DuplexGroupChannelScanGuiCanvas() {
            super();
            setBackground(backgroundColor);
            setForeground(foregroundColor);
            // create a smaller font
            signalBarsFont = new Font("Dialog", Font.PLAIN, 8);

            int textHeight = 0;
            int textWidth = 0;

            // get metrics from the graphics
            java.awt.FontMetrics metrics = getFontMetrics(signalBarsFont);
            // get the height of a line of text in this font and render context
            textHeight = metrics.getHeight();
            // get the advance of my text in this font and render context
            textWidth = metrics.stringWidth("38");  // representative (but not accurate) example text string // NOI18N
            // calculate the size of a box to hold the text with some padding.
            channelTextSize = new Dimension(textWidth + HORIZ_PADDING, textHeight + VERT_PADDING);
            requiredMinWindowWidth = channelCount * channelTextSize.width;
            requiredMinWindowHeight += (2 * channelTextSize.height);
            baseline += channelTextSize.height;
            barSpace = channelTextSize.width;
            barWidth = textWidth;
            barOffset = (barSpace - barWidth) / 2;
            textWidth = 0;
            setSize(requiredMinWindowWidth, requiredMinWindowHeight);
        }

        /**
         * Used by this class to specify a channel number to highlight in the
         * GUI. An invalid channel number may be used to cause the class to
         * clear the channel highlight. After invoking this method, a call to
         * this class' repaint() method is required to cause the GUI update.
         *
     * @param channelNum  integer representing a Duplex Group channel
         *                   number.
         */
        public void setChannelBeingScanned(int channelNum) {
            if ((channelNum < 11) || (channelNum > 26)) {
                indexBeingScanned = -1;
                return;
            }
            indexBeingScanned = channelNum - 11;
        }

        final float dash1[] = {7.0f, 3.0f};
        final BasicStroke dashedStroke = new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, dash1, 0.0f);

        final BasicStroke plainStroke = new BasicStroke(1.0f);

        @Override
        public void paint(java.awt.Graphics g) {
            int channelIndex;
            java.awt.Graphics2D g2;
            if (g instanceof java.awt.Graphics2D) {
                g2 = (java.awt.Graphics2D) g;
            } else {
                log.error("paint() cannot cast object g to Graphics2D.  Aborting paint().");
                return;
            }
            for (int i = 11; i <= 26; ++i) {
                g2.drawString(Integer.toString(i), (i - 11) * channelTextSize.width, channelTextSize.height);
                g2.drawString(Integer.toString(i), (i - 11) * channelTextSize.width, requiredMinWindowHeight - 1);
            }

            // draw a simple line to act as a bottom line in the graphic block
            g2.setColor(lowerLimitLineColor);
            g2.draw(new java.awt.geom.Line2D.Float(0, baseline + 1,
                    requiredMinWindowWidth - 1, baseline + 1));

            // draw a bar, average line and max line for each channel
            for (channelIndex = 0; channelIndex < 16; ++channelIndex) {
                redrawSignalBar(g2, dci[channelIndex]);
            }
            // draw a diamond for the area showing the channel being scanned
            redrawChannelAtIndicator(g2, indexBeingScanned);

            // draw the recommended limit line
            g2.setColor(recommendationLineColor);
            g2.setStroke(dashedStroke);
            g2.draw(new java.awt.geom.Line2D.Float(1, baseline - (96 / barGraphScale),
                    requiredMinWindowWidth - 1, baseline - (96 / barGraphScale)));
            g2.setStroke(plainStroke);
        }

        private void redrawSignalBar(java.awt.Graphics2D g2, DuplexChannelInfo dci) {
            int index = dci.channel - 11;
            int current = dci.mostRecentSample;
            int max = dci.maxSigValue;
            int avg = dci.avgSamples;
            if (avg < 0) {
                avg = 0;
            }
            int numSamples = dci.numSamples;

            if (current > 0) {
                int upperX;
                int upperY;
                int width;
                int height;

                upperX = (barSpace * index);
                width = barSpace;

                // clear anything above the "bottoms line"
                upperY = baseline - maxScaledBarValue;
                height = (maxScaledBarValue - 1);
                g2.setColor(backgroundColor);
                g2.fillRect(upperX, upperY,
                        width, height - 1);

                // draw the filled rectangle for the current value.
                upperY = baseline - (current / barGraphScale);
                g2.setColor(valueBarColor);
                g2.fillRect(upperX + barOffset, upperY,
                        barWidth, (current / barGraphScale));

            } else {
                // clear anything above the "bottoms line"
                g2.setColor(backgroundColor);
                g2.fillRect(
                        (barOffset + (barSpace * index)), ((baseline - maxScaledBarValue) - 1),
                        barWidth, maxScaledBarValue);
            }

            if (numSamples > 1) {
                // draw the line for the average value.
                g2.setColor(averageLineColor);
                g2.draw(new java.awt.geom.Line2D.Float(
                        (barSpace * index) + 1, ((baseline - (avg / barGraphScale)) - 1),
                        (barSpace * (index + 1)) - 2, (baseline - (avg / barGraphScale)) - 1));
            }

            // draw the line for the max value.
            if (max >= 0) {
                g2.setColor(maxLineColor);
                g2.draw(new java.awt.geom.Line2D.Float(
                        (barSpace * index) + 1, ((baseline - (max / barGraphScale)) - 1),
                        (barSpace * (index + 1)) - 2, (baseline - (max / barGraphScale)) - 1));
            }
        }

        private void redrawChannelAtIndicator(java.awt.Graphics2D g2, int channelIndex) {
            int upperX;
            int upperY;
            int width;
            int height;
            // clear anything below the "bottoms line"
            upperY = baseline + 2;
            height = requiredMinWindowHeight - upperY - channelTextSize.height - 2;
            upperX = 0;
            width = requiredMinWindowWidth;
            g2.setColor(backgroundColor);
            g2.fillRect(upperX, upperY,
                    width, height - 1);

            // show the highlight only if a valid channel index (1-16) is specified
            if ((channelIndex >= 0) && (channelIndex < 16)) {
                // draw a diamond in black using polyline mechanisms
                g2.setColor(foregroundColor);
                int x2Points[] = {(channelIndex * barSpace) + (barSpace / 2),
                    (channelIndex * barSpace) + barOffset,
                    channelIndex * barSpace + (barSpace / 2),
                    (channelIndex * barSpace) + (barSpace - barOffset)};
                int y2Points[] = {baseline + 2, baseline + 5, baseline + 8, baseline + 5};
                java.awt.geom.GeneralPath polygon
                        = new java.awt.geom.GeneralPath(java.awt.geom.GeneralPath.WIND_EVEN_ODD,
                                x2Points.length);

                polygon.moveTo(x2Points[0], y2Points[0]);

                for (int index = 1; index < x2Points.length; index++) {
                    polygon.lineTo(x2Points[index], y2Points[index]);
                }
                polygon.closePath();
                g2.draw(polygon);
            }
        }
    }

    /**
     * Implements a basic structure for tracking Duplex Radio channel energy
     * scan information.
     *
     * @author B. Milhaupt Copyright 2010, 2011
     */
    private static class DuplexChannelInfo {

        public int channel;
        public int numSamples;
        public int maxSigValue;
        public int minSigValue;
        public int sumSamples;
        public int avgSamples;
        public int mostRecentSample;

        public DuplexChannelInfo() {
            channel = -1;
            numSamples = 0;
            maxSigValue = -1;
            minSigValue = 1000;
            sumSamples = 0;
            avgSamples = 0;
            mostRecentSample = -1;
        }
    }

}
