package jmri.jmrix.can.cbus.swing.simulator;

import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatter;
import jmri.jmrix.can.cbus.simulator.CbusSimCanListener;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane for setting listened to and sent from directions.
 *
 * @author Steve Young Copyright (C) 2019
 * @since 4.15.1
 */
public class DirectionPane extends JPanel {
    
    private JCheckBox processIn;
    private JCheckBox processOut;
    private JCheckBox sendIn;
    private JCheckBox sendOut;
    
    private JSpinner delaySpinner;
    
    private CbusSimCanListener cbl;
    
    private JPanel processPane;
    private JPanel sendPane;
    private JPanel delayPane;

    public DirectionPane() {
        super();
    }
    
    public DirectionPane(CbusSimCanListener cbcl) {
        super();
        cbl = cbcl;
        init();
    }

    public void setSimCanListener( CbusSimCanListener cbcl) {
        cbl = cbcl;
        setWhichActive();
        setListeners();
    }

    private void init() {
        
        initPanes();
        initCheckBoxes();
        
        processPane.add(processIn);
        processPane.add(processOut);
        this.add(processPane);
        
        sendPane.add(sendIn);
        sendPane.add(sendOut);
        this.add(sendPane);
        
        delayPane.add(getNewJSpinner());
        this.add(delayPane);
        setWhichActive();
        setListeners();

    }
    
    private void initPanes() {
    
        // Pane to hold Process
        processPane = new JPanel();
        processPane.setLayout(new BoxLayout(processPane, BoxLayout.X_AXIS));
        processPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), (Bundle.getMessage("Listen"))));
        // Pane to hold Send
        sendPane = new JPanel();
        sendPane.setLayout(new BoxLayout(sendPane, BoxLayout.X_AXIS));
        sendPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), (Bundle.getMessage("Send"))));
        // Pane to hold Send Delay
        delayPane = new JPanel();
        delayPane.setLayout(new BoxLayout(delayPane, BoxLayout.X_AXIS));
        delayPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), (Bundle.getMessage("Delay"))));
    
    }
    
    private void initCheckBoxes() {
        processIn = new JCheckBox(Bundle.getMessage("processIn"));
        processOut = new JCheckBox(Bundle.getMessage("processOut"));
        sendIn = new JCheckBox(Bundle.getMessage("sendIn"));
        sendOut = new JCheckBox(Bundle.getMessage("sendOut"));
        
        processIn.setToolTipText(Bundle.getMessage("processInTip"));
        processOut.setToolTipText(Bundle.getMessage("processOutTip"));
        sendIn.setToolTipText(Bundle.getMessage("sendInTip"));
        sendOut.setToolTipText(Bundle.getMessage("sendOutTip"));

        processIn.setVisible(true);
        processOut.setVisible(true);
        sendIn.setVisible(true);
        sendOut.setVisible(true);
    }
    
    private JSpinner getNewJSpinner(){
        delaySpinner = new JSpinner(new SpinnerNumberModel(80,0,999999,1));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(delaySpinner, "#");
        delaySpinner.setEditor(editor);
        JFormattedTextField fieldEv = (JFormattedTextField) editor.getComponent(0);
        DefaultFormatter formatterEv = (DefaultFormatter) fieldEv.getFormatter();
        fieldEv.setColumns(4);
        formatterEv.setCommitsOnValidEdit(true);
        delaySpinner.setToolTipText(Bundle.getMessage("DelayMs"));
        return delaySpinner;
    
    }

    private void setListeners(){
        if (cbl == null ) {
            return;
        }
        processIn.addActionListener ((ActionEvent e) -> {
            cbl.setProcessIn(processIn.isSelected());
        }); 

        processOut.addActionListener ((ActionEvent e) -> {
            cbl.setProcessOut(processOut.isSelected());
        });

        sendIn.addActionListener ((ActionEvent e) -> {
            cbl.setSendIn(sendIn.isSelected());
        });

        sendOut.addActionListener ((ActionEvent e) -> {
            cbl.setSendOut(sendOut.isSelected());
        });
        
        delaySpinner.addChangeListener((ChangeEvent e) -> {
            cbl.setDelay((Integer) delaySpinner.getValue());
        });
    }

    private void setWhichActive(){
        processOut.setEnabled(cbl != null);
        processIn.setEnabled(cbl != null);
        sendOut.setEnabled(cbl != null);
        sendIn.setEnabled(cbl != null);
        delaySpinner.setEnabled(cbl != null);

        if (cbl == null ) {
            return;
        }
        processOut.setSelected(cbl.getProcessOut());
        processIn.setSelected(cbl.getProcessIn());
        sendOut.setSelected(cbl.getSendOut());
        sendIn.setSelected(cbl.getSendIn());
        delaySpinner.setValue(cbl.getDelay());
    }
    
    // private final static Logger log = LoggerFactory.getLogger(DirectionPane.class);
    
}
