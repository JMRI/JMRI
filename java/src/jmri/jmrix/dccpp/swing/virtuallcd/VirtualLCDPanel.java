package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.List;
import java.util.*;

import javax.swing.*;

import jmri.jmrit.display.Positionable;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.dccpp.*;
import jmri.util.JmriJFrame;

/**
 * Panel to image the DCC-EX command station's OLED display.
 * Also sends request to DCC-EX to send copies of all LCD messages to this
 * instance of JMRI.
 *
 * @author Bob Jacobsen      Copyright (C) 2023
 * @author M Steve Todd      Copyright (C) 2023
 * @author Daniel Bergqvist  Copyright (C) 2026
 */
public class VirtualLCDPanel extends JPanel implements DCCppListener  {

    private final JmriJFrame _frame;
    private final Positionable _positionable;
    private DCCppTrafficController _tc;
    private DCCppSystemConnectionMemo _memo;
    private int _displayNo;
    private final PropertyChangeListener _listener;
    private Font font;

    static final int TOTALLINES = 64;
    private final Map<Integer, List<JLabel>> linesMap = new HashMap<>();

    public VirtualLCDPanel(JmriJFrame frame, DCCppSystemConnectionMemo memo) {
        this(frame, null, memo, -1);
    }

    public VirtualLCDPanel(
            JmriJFrame frame,
            Positionable pos,
            DCCppSystemConnectionMemo memo,
            int displayNo) {

        _frame = frame;
        _positionable = pos;
        _memo = memo;
        _displayNo = displayNo;

        _listener = evt -> {
            if (ConnectionStatus.CONNECTION_UP.equals(
                    ConnectionStatus.instance().getConnectionState(memo))) {
                _tc.sendDCCppMessage(DCCppMessage.makeLCDRequestMsg(), null);
            }
        };
    }

    public void initComponents() {
        _tc = _memo.getDCCppTrafficController();
        _tc.addDCCppListener(DCCppInterface.CS_INFO, this);

        _tc.sendDCCppMessage(DCCppMessage.makeLCDRequestMsg(), null);
        ConnectionStatus.instance().addPropertyChangeListener(_memo, _listener);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // load the custom 5x8 found
        try {
            InputStream stream = new FileInputStream(new File("resources/fonts/5x8_lcd_hd44780u_a02.ttf"));
            font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(16f).deriveFont(Font.BOLD);
        } catch (IOException e1) { log.error("failed to find or open font file");
        } catch (FontFormatException e2) { log.error("font file not valid");
        }
    }

    public void reset() {
        this.removeAll();
        linesMap.clear();
    }

    public void dispose() {
        ConnectionStatus.instance().removePropertyChangeListener(_memo, _listener);
    }

    public void setMemo(DCCppSystemConnectionMemo memo) {
        ConnectionStatus.instance().removePropertyChangeListener(_memo, _listener);
        if (_tc != null) {
            _tc.removeDCCppListener(DCCppInterface.CS_INFO, this);
        }
        _memo = memo;
        _tc = memo.getDCCppTrafficController();
        _tc.addDCCppListener(DCCppInterface.CS_INFO, this);
        _tc.sendDCCppMessage(DCCppMessage.makeLCDRequestMsg(), null);
        ConnectionStatus.instance().addPropertyChangeListener(_memo, _listener);
        reset();
    }

    public void setDisplayNo(int displayNo) {
        _displayNo = displayNo;
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(DCCppMessage msg) {
    }

    private List<JLabel> createNewDisplay() {
        // Add space between displays if this is not the first display
        if (getComponentCount() > 0) {
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
        _frame.pack();
        return lines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(DCCppReply msg) {
        if (msg.isLCDTextReply()) { // <@ display# line# "message text">
            int displayNumber = msg.getLCDDisplayNumInt();

            // displayNo == -1 means every display
            if (_displayNo == -1 || _displayNo == displayNumber) {
                var lines = linesMap.computeIfAbsent(displayNumber, display -> createNewDisplay());
                int lineNumber = msg.getLCDLineNumInt();
                if (lineNumber < TOTALLINES) {
                    lines.get(lineNumber).setText(msg.getLCDTextString()+"   "); // padding for appearance
                    if (_positionable != null) {
                        var d = this.getPreferredSize();
                        this.setSize(d);
                        _positionable.setSize(d);
                    } else {
                        _frame.pack();
                    }
                } else {
                    log.warn("Received LCD message for line {}, but configured for TOTALLINES limit of {}",
                                lineNumber, TOTALLINES-1);
                }
                log.debug("Received LCD message for display# {}.", displayNumber);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDPanel.class);

}
