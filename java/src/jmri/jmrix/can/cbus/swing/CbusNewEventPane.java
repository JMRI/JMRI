package jmri.jmrix.can.cbus.swing;

import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatter;
import jmri.jmrix.can.cbus.swing.eventrequestmonitor.CbusEventRequestTablePane;
import jmri.jmrix.can.cbus.swing.eventtable.CbusEventTablePane;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Creates Panel for displaying New Event JSpinners and Button.
 * @author Steve Young Copyright (C) 2018, 2020
 */
public class CbusNewEventPane extends JPanel {
    
    private JSpinner newnodenumberSpinner;
    private JSpinner newevnumberSpinner;
    private JButton newevbutton;
    
    private final CbusEventTablePane _evPanel;
    private final CbusEventRequestTablePane _evReqPanel;
    
    private jmri.UserPreferencesManager p;
    private static final String EVENTNUM_PREF = "EventNum"; // NOI18N
    private static final String NODENUM_PREF = "NodeNum"; // NOI18N
    
    public CbusNewEventPane(CbusEventTablePane evPanel){
        super();
        _evPanel = evPanel;
        _evReqPanel = null;
        init();
        
    }
    
    public CbusNewEventPane( CbusEventRequestTablePane evReqPanel ){
        super();
        _evPanel = null;
        _evReqPanel = evReqPanel;
        init();
    }
    
    final void init() {
        
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        int nodenum = 0; // default node 0
        int eventnum = 1; // default event 1
        Object nn = p.getProperty(this.getClass().getName(), NODENUM_PREF);
        if ( nn!=null ){
            nodenum = (Integer) nn;
        }
        Object en = p.getProperty(this.getClass().getName(), EVENTNUM_PREF);
        if ( en!=null ){
            eventnum = (Integer) en;
        }
        
        newnodenumberSpinner = new JSpinner(new SpinnerNumberModel(nodenum, 0, 65535, 1));
        newevnumberSpinner = new JSpinner(new SpinnerNumberModel(eventnum, 0, 65535, 1));
    
        this.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), (Bundle.getMessage("NewEvent"))));
        
        newevbutton = new JButton((Bundle.getMessage("NewEvent")));
        newevbutton.addActionListener((ActionEvent e) -> newEvent());
        
        this.add(newSpinnerPanel(newnodenumberSpinner, 
                Bundle.getMessage("CbusNode"), Bundle.getMessage("NewNodeTip")));
        this.add(newSpinnerPanel(newevnumberSpinner, 
                Bundle.getMessage("CbusEvent"), null)); 
        this.add(newevbutton);  
        
    }
    
    private JPanel newSpinnerPanel( final JSpinner spinner, String label, String tip  ){
        JPanel newnode = new JPanel();
        newnode.add(new JLabel(label));
        JComponent comp = spinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        spinner.addChangeListener((ChangeEvent e) ->  eventChanged() );
        newnode.add(spinner);
        newnode.setToolTipText(tip);
        spinner.setToolTipText(tip);
        return newnode;
    }

    private void eventChanged(){
        if (_evPanel!=null){
            _evPanel.cbEvTable.tableChanged(null);
        }
        else if (_evReqPanel!=null){
            _evReqPanel.tableChanged(null);
        }
        p.setProperty(this.getClass().getName(), NODENUM_PREF, newnodenumberSpinner.getValue());
        p.setProperty(this.getClass().getName(), EVENTNUM_PREF, newevnumberSpinner.getValue());
    }
    
    public void setNewButtonActive(boolean newState){
        newevbutton.setEnabled(newState);
    }
    
    public int getNn(){
        return (Integer) newnodenumberSpinner.getValue();
    }
    
    public int getEn(){
        return (Integer) newevnumberSpinner.getValue();
    }
    
    private void newEvent(){
        if (_evPanel!=null){
            _evPanel.cbEvTable.addEvent(getNn(),getEn());
        }
        else if (_evReqPanel!=null){
            _evReqPanel.addEvent(getNn(),getEn());
        }
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusNewEventPane.class);
}
