package jmri.jmrix.can.cbus.swing.console;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import jmri.jmrix.can.cbus.swing.CbusEventHighlightFrame;
import jmri.jmrix.can.cbus.swing.CbusFilterFrame;
import jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane;
import jmri.util.JmriJFrame;

/**
 * Frame for CBUS Console
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleDisplayOptionsPane extends javax.swing.JPanel {

    private final CbusConsolePane _mainPane;

    private final JCheckBox showLogCheckBox;
    private final JCheckBox showStatsCheckBox;
    private final JCheckBox showPacketCheckBox;
    private final JCheckBox showSendEventCheckBox;
    public JButton filterButton;
    public JButton highlightButton;
    protected JButton evCaptureButton;

    protected CbusFilterFrame filterFrame;
    protected CbusEventHighlightFrame highlightFrame;
    private ConfigToolPane _evCapFrame;
    private JmriJFrame _ecf;
    private final jmri.UserPreferencesManager p;

    private static final String SHOW_LOG = ".ShowLog";
    private static final String SHOW_STATS = ".ShowStats";
    private static final String SHOW_PACKET = ".ShowPacket";
    private static final String SHOW_SEND_EVENT = ".ShowSendEvent";

    public CbusConsoleDisplayOptionsPane(CbusConsolePane mainPane){
        super();
        _mainPane = mainPane;
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        showLogCheckBox = new JCheckBox(Bundle.getMessage("Logging"));
        showStatsCheckBox = new JCheckBox(Bundle.getMessage("StatisticsTitle"));
        showPacketCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowPackets"));
        showSendEventCheckBox = new JCheckBox(Bundle.getMessage("ButtonSendEvent"));
        filterButton = new JButton(Bundle.getMessage("ButtonFilter"));
        highlightButton = new JButton(Bundle.getMessage("ButtonHighlight"));
        evCaptureButton = new JButton(Bundle.getMessage("CapConfigTitle"));
        makePane();
    }

    private void makePane() {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("Display")));

        add(showLogCheckBox);        
        add(showStatsCheckBox);
        add(showPacketCheckBox);
        add(showSendEventCheckBox);
        add(filterButton);
        add(highlightButton);
        add(evCaptureButton);

        setDefaults();
        setToolTipText();
        addListeners();
    }

    private void setDefaults() {
        showLogCheckBox.setSelected(p.getSimplePreferenceState(getClass().getName() + SHOW_LOG));
        showStatsCheckBox.setSelected(p.getSimplePreferenceState(getClass().getName() + SHOW_STATS));
        showPacketCheckBox.setSelected(p.getSimplePreferenceState(getClass().getName() + SHOW_PACKET));
        showSendEventCheckBox.setSelected(p.getSimplePreferenceState(getClass().getName() + SHOW_SEND_EVENT));
    }

    private void setToolTipText() {
        showLogCheckBox.setToolTipText(Bundle.getMessage("LoggingTip"));
        showStatsCheckBox.setToolTipText(Bundle.getMessage("ButtonShowStats"));
        showPacketCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPackets"));
        showSendEventCheckBox.setToolTipText(Bundle.getMessage("TooltipShowEvents"));

        filterButton.setToolTipText(Bundle.getMessage("TooltipFilter"));
        highlightButton.setToolTipText(Bundle.getMessage("TooltipHighlighter"));
    }

    private void addListeners(){
        showLogCheckBox.addActionListener(this::matchVisisbleToCheckBoxes);        
        showStatsCheckBox.addActionListener(this::matchVisisbleToCheckBoxes);        
        showPacketCheckBox.addActionListener(this::matchVisisbleToCheckBoxes);
        showSendEventCheckBox.addActionListener(this::matchVisisbleToCheckBoxes);

        filterButton.addActionListener(this::filterButtonActionPerformed);
        highlightButton.addActionListener(this::highlightButtonActionPerformed);
        evCaptureButton.addActionListener(this::evCaptureButtonActionPerformed);
    }

    // triggered by CbusConsolePane when all panes are available
    public void matchVisisbleToCheckBoxes(ActionEvent e){
        _mainPane.logPane.setVisible(showLogCheckBox.isSelected());
        _mainPane.statsPane.setVisible(showStatsCheckBox.isSelected());
        _mainPane.packetPane.setVisible(showPacketCheckBox.isSelected());
        _mainPane.sendPane.setVisible(showSendEventCheckBox.isSelected());
        _mainPane.validate();
    }

    public void filterButtonActionPerformed(ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if (filterFrame == null) {
            filterFrame = new CbusFilterFrame(_mainPane,_evCapFrame);
            filterFrame.initComponents();
            if (_evCapFrame != null ) {
                _evCapFrame.setFilter(filterFrame);
            }
        } else {
            filterFrame.setState(Frame.NORMAL);
        }
        filterFrame.setVisible(true);
    }

    public void highlightButtonActionPerformed(ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if (highlightFrame == null) {
            highlightFrame = new CbusEventHighlightFrame(_mainPane,_evCapFrame);
            highlightFrame.initComponents();
            if (_evCapFrame != null ) {
                _evCapFrame.setHighlighter(highlightFrame);
            }
        } else {
            highlightFrame.setState(Frame.NORMAL);
        }
        highlightFrame.setVisible(true);
    }

    public void evCaptureButtonActionPerformed(ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if (_evCapFrame == null ) {
            _ecf = new JmriJFrame("Event Capture paired to " + _mainPane.getTitle() + " Filter and Highlighter");
            _evCapFrame = new ConfigToolPane(_mainPane, filterFrame, highlightFrame);
            _ecf.add(_evCapFrame);
            _evCapFrame.initComponents(_mainPane.getMemo());
            _ecf.pack();
            _ecf.setState(Frame.NORMAL);
        } else {
            _ecf.setState(Frame.NORMAL);
        }
        _ecf.setVisible(true);
    }

    public void dispose() {
    
        if (highlightFrame != null) {
            highlightFrame.dispose();
            highlightFrame=null;
        }
        if (filterFrame != null) {
            filterFrame.dispose();
            filterFrame=null;
        }
        if (_ecf != null) {
            _ecf.dispose();
            _ecf=null;
        }
        p.setSimplePreferenceState(getClass().getName() + SHOW_LOG, showLogCheckBox.isSelected());
        p.setSimplePreferenceState(getClass().getName() + SHOW_STATS, showStatsCheckBox.isSelected());
        p.setSimplePreferenceState(getClass().getName() + SHOW_SEND_EVENT, showSendEventCheckBox.isSelected());
        p.setSimplePreferenceState(getClass().getName() + SHOW_PACKET, showPacketCheckBox.isSelected());
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusConsoleDisplayOptionsPane.class);
}
