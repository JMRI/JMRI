package jmri.jmrix.can.cbus.swing.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;

import jmri.jmrix.can.cbus.simulator.CbusDummyCS;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.CbusEventResponder;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane for setting listened to and sent from directions
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
    
    private CbusDummyCS _cs=null;
    private CbusDummyNode _nd=null;
    private CbusEventResponder _ev=null;

    public DirectionPane() {
        super();
        init();
    }
    
    public DirectionPane(CbusDummyCS cs ) {
        super();
        _cs = cs;
        init();
    }
    
    public DirectionPane(CbusDummyNode nd ) {
        super();
        _nd = nd;
        init();
    }    

    public DirectionPane(CbusEventResponder ev ) {
        super();
        _ev = ev;
        init();
    }
    
    private void init() {
        // Pane to hold Process
        JPanel processPane = new JPanel();
        processPane.setLayout(new BoxLayout(processPane, BoxLayout.X_AXIS));
        processPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), (Bundle.getMessage("Listen"))));
        // Pane to hold Send
        JPanel sendPane = new JPanel();
        sendPane.setLayout(new BoxLayout(sendPane, BoxLayout.X_AXIS));
        sendPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), (Bundle.getMessage("Send"))));

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

        processPane.add(processIn);
        processPane.add(processOut);
        this.add(processPane);
        
        sendPane.add(sendIn);
        sendPane.add(sendOut);
        this.add(sendPane);
        
        JPanel delayPane = new JPanel();
        delayPane.setLayout(new BoxLayout(delayPane, BoxLayout.X_AXIS));
        delayPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), (Bundle.getMessage("Delay"))));
        
        delaySpinner = new JSpinner(new SpinnerNumberModel(777,0,999999,1));
        JComponent compEv = delaySpinner.getEditor();
        JFormattedTextField fieldEv = (JFormattedTextField) compEv.getComponent(0);
        DefaultFormatter formatterEv = (DefaultFormatter) fieldEv.getFormatter();
        fieldEv.setColumns(4);
        formatterEv.setCommitsOnValidEdit(true);
        delaySpinner.setToolTipText(Bundle.getMessage("DelayMs"));
        
        delayPane.add(delaySpinner);
        
        this.add(delayPane);
        setWhichActive();
        setListeners();

    }

    private void setListeners(){
        processIn.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( _cs != null ) { _cs.setProcessIn(processIn.isSelected()); }
                if ( _nd != null ) { _nd.setProcessIn(processIn.isSelected()); }
                if ( _ev != null ) { _ev.setProcessIn(processIn.isSelected()); }
            }
        }); 

        processOut.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( _cs != null ) { _cs.setProcessOut(processOut.isSelected()); }
                if ( _nd != null ) { _nd.setProcessOut(processOut.isSelected()); }
                if ( _ev != null ) { _ev.setProcessOut(processOut.isSelected()); }                
            }
        });

        sendIn.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( _cs != null ) { _cs.setSendIn(sendIn.isSelected()); }
                if ( _nd != null ) { _nd.setSendIn(sendIn.isSelected()); }
                if ( _ev != null ) { _ev.setSendIn(sendIn.isSelected()); }  
            }
        });

        sendOut.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( _cs != null ) { _cs.setSendOut(sendOut.isSelected()); }
                if ( _nd != null ) { _nd.setSendOut(sendOut.isSelected()); }
                if ( _ev != null ) { _ev.setSendOut(sendOut.isSelected()); }
            }
        });
        
        delaySpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newDelay;
                newDelay = (Integer) delaySpinner.getValue();
                if ( _cs != null ) { _cs.setDelay(newDelay); }
                if ( _nd != null ) { _nd.setDelay(newDelay); }
                if ( _ev != null ) { _ev.setDelay(newDelay); }
            }
        });
    }

    private void setWhichActive(){
        if ( _cs != null ) {
            processOut.setSelected(_cs.getProcessOut());
            processIn.setSelected(_cs.getProcessIn());
            sendOut.setSelected(_cs.getSendOut());
            sendIn.setSelected(_cs.getSendIn());
            delaySpinner.setValue(_cs.getDelay());
        }
        if ( _nd != null ) {
            processOut.setSelected(_nd.getProcessOut());
            processIn.setSelected(_nd.getProcessIn());
            sendOut.setSelected(_nd.getSendOut());
            sendIn.setSelected(_nd.getSendIn());
            delaySpinner.setValue(_nd.getDelay());
        }        
        if ( _ev != null ) {
            processOut.setSelected(_ev.getProcessOut());
            processIn.setSelected(_ev.getProcessIn());
            sendOut.setSelected(_ev.getSendOut());
            sendIn.setSelected(_ev.getSendIn());
            delaySpinner.setValue(_ev.getDelay());
        }
    }
    
    // private final static Logger log = LoggerFactory.getLogger(DirectionPane.class);
    
}