package jmri.jmrix.can.cbus.swing.console;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Panel for CBUS Console Stats
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleStatsPane extends javax.swing.JPanel {
    
    private JTextField sentCountField;
    private JTextField rcvdCountField;
    private JTextField eventsCountField;
    private JTextField dccCountField;
    private JTextField totalCountField;
    private JButton statsClearButton;
    
    transient private int _sent;
    transient private int _rcvd;
    transient private int _events;
    transient private int _dcc;
    transient private int _total;
    
    public CbusConsoleStatsPane(CbusConsolePane mainPane){
        super();
        initButtons();
        statsClearButtonActionPerformed(null);
        addToPanel();
    }

    private void addToPanel() {
    
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("StatisticsTitle")));
        add(sentCountField);
        add(rcvdCountField);
        add(totalCountField);
        add(eventsCountField);
        add(dccCountField);
        add(statsClearButton);
    
        statsClearButton.addActionListener(this::statsClearButtonActionPerformed);
        
    }
    
    protected void incremenetTotal(){
        totalCountField.setText(Integer.toString(++_total));
    }
    
    protected void incremenetReceived(){
        rcvdCountField.setText(Integer.toString(++_rcvd));
    }
    
    protected void incremenetSent(){
        sentCountField.setText(Integer.toString(++_sent));
    }
    
    
    protected void incrementEvents() {
        eventsCountField.setText(Integer.toString(++_events));
    }
    
    protected void incrementDcc() {
        dccCountField.setText(Integer.toString(++_dcc));
    }
    
    private void initButtons() {
        
        sentCountField = new JTextField("0", 8);
        rcvdCountField = new JTextField("0", 8);
        eventsCountField = new JTextField("0", 8);
        dccCountField = new JTextField("0", 8);
        totalCountField = new JTextField("0", 8);
        statsClearButton = new JButton();
        initButtonBorderToolTips();
    }
    
    
    private void initButtonBorderToolTips(){
    
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
    
    }
    
    private void statsClearButtonActionPerformed(java.awt.event.ActionEvent e) {
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

    // private final static Logger log = LoggerFactory.getLogger(CbusConsoleStatsPane.class);
}
