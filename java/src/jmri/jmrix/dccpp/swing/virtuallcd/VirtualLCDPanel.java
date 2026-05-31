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
import jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration.DisplayConfig;
import static jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration.DisplayConfig.ConfigureVirtualLCD_AllDisplays;
import static jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration.DisplayConfig.ConfigureVirtualLCD_IntervalDisplay;
import static jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration.DisplayConfig.ConfigureVirtualLCD_OneDisplay;
import static jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration.DisplayConfig.ConfigureVirtualLCD_SelectedDisplays;
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
public class VirtualLCDPanel extends JPanel
        implements DCCppListener, VirtualLCDConfiguration  {

    private final JmriJFrame _frame;
    private final Positionable _positionable;
    private DCCppTrafficController _tc;
    private final PropertyChangeListener _listener;
    private Font font;
    private DCCppSystemConnectionMemo _memo;
    private DisplayConfig _displayConfig = DisplayConfig.ConfigureVirtualLCD_AllDisplays;
    private int displayNo = -1;
    private int _minDisplayNo;
    private int _maxDisplayNo;
    private final Set<Integer> _selectedDisplays = new HashSet<>();

    static final int TOTALLINES = 64;
    private final Map<Integer, List<JLabel>> linesMap = new HashMap<>();

    public VirtualLCDPanel(JmriJFrame frame) {
        this(frame, null);
    }

    public VirtualLCDPanel(
            JmriJFrame frame,
            Positionable pos) {

        _frame = frame;
        _positionable = pos;

        _listener = evt -> {
            if (ConnectionStatus.CONNECTION_UP.equals(ConnectionStatus.instance().getConnectionState(_memo))) {
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

    @Override
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

    @Override
    public DCCppSystemConnectionMemo getMemo() {
        return _memo;
    }

    @Override
    public void setDisplayConfig(DisplayConfig displayConfig) {
        this._displayConfig = displayConfig;
        reset();
    }

    @Override
    public DisplayConfig getDisplayConfig() {
        return _displayConfig;
    }

    @Override
    public void setDisplayNo(int displayNo) {
        this.displayNo = displayNo;
        reset();
    }

    @Override
    public int getDisplayNo() {
        return displayNo;
    }

    @Override
    public void setMinDisplayNo(int minDisplayNo) {
        this._minDisplayNo = minDisplayNo;
        reset();
    }

    @Override
    public int getMinDisplayNo() {
        return _minDisplayNo;
    }

    @Override
    public void setMaxDisplayNo(int maxDisplayNo) {
        this._maxDisplayNo = maxDisplayNo;
        reset();
    }

    @Override
    public int getMaxDisplayNo() {
        return _maxDisplayNo;
    }

    @Override
    public void setSelectedDisplays(Set<Integer> displays) {
        _selectedDisplays.clear();
        _selectedDisplays.addAll(displays);
        reset();
    }

    @Override
    public Set<Integer> getSelectedDisplays() {
        return _selectedDisplays;
    }

    public String getNameString() {
        switch (_displayConfig) {
            case ConfigureVirtualLCD_AllDisplays:
                return Bundle.getMessage("VirtualLcdPositionable_AllDisplays");

            case ConfigureVirtualLCD_OneDisplay:
                return Bundle.getMessage(
                        "VirtualLcdPositionable_OneDisplay", displayNo);

            case ConfigureVirtualLCD_IntervalDisplay:
                return Bundle.getMessage(
                        "VirtualLcdPositionable_IntervalDisplay",
                        _minDisplayNo, _maxDisplayNo);

            case ConfigureVirtualLCD_SelectedDisplays:
                StringBuilder sb = new StringBuilder();
                for (int i : _selectedDisplays) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(i);
                }
                return Bundle.getMessage(
                        "VirtualLcdPositionable_SelectedDisplays", sb.toString());

            default:
                throw new IllegalArgumentException("Unknown displayConfig: "+_displayConfig.name());
        }
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

    private boolean showDisplay(int displayNumber) {
        switch (_displayConfig) {
            case ConfigureVirtualLCD_AllDisplays:
                return true;

            case ConfigureVirtualLCD_OneDisplay:
                return displayNo == displayNumber;

            case ConfigureVirtualLCD_IntervalDisplay:
                return _minDisplayNo <= displayNumber && displayNumber <= _maxDisplayNo;

            case ConfigureVirtualLCD_SelectedDisplays:
                return _selectedDisplays.contains(displayNumber);

            default:
                throw new IllegalArgumentException("Unknown displayConfig: "+_displayConfig.name());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(DCCppReply msg) {
        if (msg.isLCDTextReply()) { // <@ display# line# "message text">
            int displayNumber = msg.getLCDDisplayNumInt();

            if (showDisplay(displayNumber)) {
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
