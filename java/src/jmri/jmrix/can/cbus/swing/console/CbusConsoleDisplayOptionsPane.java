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

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Frame for CBUS Console
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleDisplayOptionsPane extends javax.swing.JPanel {
    
    private final CbusConsolePane _mainPane;
    
    private JCheckBox showLogCheckBox;
    private JCheckBox showStatsCheckBox;
    private JCheckBox showPacketCheckBox;
    private JCheckBox showSendEventCheckBox;
    public JButton filterButton;
    public JButton highlightButton;
    protected JButton evCaptureButton;
    
    protected CbusFilterFrame filterFrame;
    protected CbusEventHighlightFrame highlightFrame;
    private ConfigToolPane _evCapFrame;
    private JmriJFrame _ecf;
    
    public CbusConsoleDisplayOptionsPane(CbusConsolePane mainPane){
        super();
        _mainPane = mainPane;
        makePane();
    }
    
    private void makePane() {
    
        setDefaults();
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
    
    }

    private void setDefaults() {
        
        showLogCheckBox = new JCheckBox();
        showStatsCheckBox = new JCheckBox();
        showPacketCheckBox = new JCheckBox();
        showSendEventCheckBox = new JCheckBox();
        filterButton = new JButton();
        highlightButton = new JButton();
        evCaptureButton = new JButton();
                
        showStatsCheckBox.setSelected(false);
        showLogCheckBox.setSelected(false);
        showPacketCheckBox.setSelected(false);
        showSendEventCheckBox.setSelected(false);
        
        setText();
    
    }
    
    private void setText() {
    
        showLogCheckBox.setText(Bundle.getMessage("Logging"));
        showLogCheckBox.setToolTipText(Bundle.getMessage("LoggingTip"));
        
        showStatsCheckBox.setText(Bundle.getMessage("StatisticsTitle"));
        showStatsCheckBox.setToolTipText(Bundle.getMessage("ButtonShowStats"));

        showPacketCheckBox.setText(Bundle.getMessage("ButtonShowPackets"));
        showPacketCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPackets"));

        showSendEventCheckBox.setText(Bundle.getMessage("ButtonSendEvent"));
        showSendEventCheckBox.setToolTipText(Bundle.getMessage("TooltipShowEvents"));
        
        filterButton.setText(Bundle.getMessage("ButtonFilter"));
        filterButton.setToolTipText(Bundle.getMessage("TooltipFilter"));
        
        highlightButton.setText(Bundle.getMessage("ButtonHighlight"));
        highlightButton.setToolTipText(Bundle.getMessage("TooltipHighlighter"));

        evCaptureButton.setText(Bundle.getMessage("CapConfigTitle"));
        
        addListeners();
    
    }
    
    private void addListeners(){
    
        showLogCheckBox.addActionListener((ActionEvent e) -> {
            _mainPane.logPane.setVisible(showLogCheckBox.isSelected());
        });        
        
        showStatsCheckBox.addActionListener((ActionEvent e) -> {
            _mainPane.statsPane.setVisible(showStatsCheckBox.isSelected());
        });        
        
        showSendEventCheckBox.addActionListener((ActionEvent e) -> {
            _mainPane.sendPane.setVisible(showSendEventCheckBox.isSelected());
        });
        
        filterButton.addActionListener(this::filterButtonActionPerformed);
        highlightButton.addActionListener(this::highlightButtonActionPerformed);
        evCaptureButton.addActionListener(this::evCaptureButtonActionPerformed);
        
        showPacketCheckBox.addActionListener((ActionEvent e) -> {
            _mainPane.packetPane.setVisible(showPacketCheckBox.isSelected());
        });
    }
    
    public void filterButtonActionPerformed(java.awt.event.ActionEvent e) {
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
    
    public void highlightButtonActionPerformed(java.awt.event.ActionEvent e) {
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

    public void evCaptureButtonActionPerformed(java.awt.event.ActionEvent e) {
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
    
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusConsoleDisplayOptionsPane.class);
}
