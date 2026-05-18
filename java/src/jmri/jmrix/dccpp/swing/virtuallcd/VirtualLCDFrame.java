package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.List;
import java.util.*;

import javax.swing.*;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.dccpp.*;
import jmri.util.JmriJFrame;

/**
 * Frame to image the DCC-EX command station's OLED display
 *   Also sends request to DCC-EX to send copies of all LCD messages to this instance of JMRI
 *
 * @author BobJacobsen  Copyright (C) 2023
 * @author MSteveTodd   Copyright (C) 2023
 */
public class VirtualLCDFrame extends JmriJFrame implements DCCppListener  {

    private final DCCppTrafficController _tc;
    private final DCCppSystemConnectionMemo _memo;
    private final PropertyChangeListener _listener;
    private Font font;

    static final int TOTALLINES = 64;
    private final Map<Integer, List<JLabel>> linesMap = new HashMap<>();

    public VirtualLCDFrame(DCCppSystemConnectionMemo memo) {
        super(false, true); // Save window position but not window size
        _tc = memo.getDCCppTrafficController();
        _memo = memo;
        _tc.sendDCCppMessage(DCCppMessage.makeLCDRequestMsg(), null);

        _listener = evt -> {
            if (ConnectionStatus.CONNECTION_UP.equals(
                    ConnectionStatus.instance().getConnectionState(memo))) {
                _tc.sendDCCppMessage(DCCppMessage.makeLCDRequestMsg(), null);
            }
        };
        ConnectionStatus.instance().addPropertyChangeListener(_memo, _listener);
    }

    @Override
    public void dispose() {
        ConnectionStatus.instance().removePropertyChangeListener(_memo, _listener);
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(DCCppMessage msg) {
    }

    private List<JLabel> createNewDisplay(int displayNumber) {
        // Add space between displays if this is not the first display
        if (this.getContentPane().getComponentCount() > 0) {
            Component c = Box.createHorizontalStrut(10);
            this.add(c);
        }

        var lines = new ArrayList<JLabel>(TOTALLINES + 1);
        var pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        // initialize the list of display lines
        for (int i = 0; i<TOTALLINES; i++) {
            var label = new JLabel();
            if (font != null) label.setFont(font);
            label.setOpaque(true);
            label.setBackground(Color.BLACK);
            label.setForeground(Color.WHITE);
            lines.add(label);
            pane.add(lines.get(i));
        }
        pane.setOpaque(true);
        pane.setBackground(Color.BLACK);
        pane.setAlignmentY(Component.TOP_ALIGNMENT);
        this.add(pane);
        pack();
        return lines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(DCCppReply msg) {
        if (msg.isLCDTextReply()) { // <@ display# line# "message text">
            int displayNumber = msg.getLCDDisplayNumInt();
            var lines = linesMap.computeIfAbsent(displayNumber, display -> createNewDisplay(display));
            int lineNumber = msg.getLCDLineNumInt();
            if (lineNumber < TOTALLINES) {
                lines.get(lineNumber).setText(msg.getLCDTextString()+"   "); // padding for appearance
                pack();
            } else {
                log.warn("Received LCD message for line {}, but configured for TOTALLINES limit of {}",
                            lineNumber, TOTALLINES-1);
            }
            log.debug("Received LCD message for display# {}.", displayNumber);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

        // load the custom 5x8 found
        try {
            InputStream stream = new FileInputStream(new File("resources/fonts/5x8_lcd_hd44780u_a02.ttf"));
            font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(16f).deriveFont(Font.BOLD);
        } catch (IOException e1) { log.error("failed to find or open font file");
        } catch (FontFormatException e2) { log.error("font file not valid");
        }

        // set the title, include prefix in event of multiple connections
        setTitle(Bundle.getMessage("VirtualLCDFrameTitle") + " (" + _memo.getSystemPrefix() + ")");

        // pack to layout display
        pack();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDFrame.class);

}
