package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeSetupPane extends JPanel {
    
    private JScrollPane eventScroll;
    private CbusNodeTableDataModel nodeModel = null;
    private int _nodeNum;
    private CbusNode nodeOfInterest;
    private ActionListener setNameListener;
    private ActionListener removeListener;
    private jmri.util.swing.BusyDialog busy_dialog;
    
    /**
     * Create a new instance of CbusNodeSetupPane.
     */
    protected CbusNodeSetupPane( NodeConfigToolPane main ) {
        super();
        //  mainpane = main;
    }

    public void initComponents(int node) {
        
        if (node == _nodeNum){
            return;
        }
        if (eventScroll != null ){ 
            eventScroll.setVisible(false);
        }
        eventScroll = null;
        
        _nodeNum = node;

        try {
            nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
        } catch (NullPointerException e) {
            log.error("Unable to get Node Table from Instance Manager");
        }
        
        
        try {

            // Pane to hold Event
            JPanel evPane = new JPanel();
            evPane.setLayout(new BoxLayout(evPane, BoxLayout.Y_AXIS));
            JLabel header;
            nodeOfInterest = nodeModel.getNodeByNodeNum(_nodeNum);
            
            header = new JLabel("<html><h2>" 
                + CbusNodeConstants.getManu(nodeOfInterest.getParameter(1)) 
                + " " 
                + nodeOfInterest.getNodeTypeName()
                + "<p>" +
                CbusNodeConstants.getModuleTypeExtra(nodeOfInterest.getParameter(1),nodeOfInterest.getParameter(3))
                + "</p></html>");

            JPanel headerPanel = new JPanel();
            JPanel namePanel = new JPanel();
            JPanel canIdPanel = new JPanel();
            JPanel nodeEventsPanel = new JPanel();
            JPanel removePanel = new JPanel();
            
            nodeEventsPanel.setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createEtchedBorder(), Bundle.getMessage("EventCol")));
            JButton clearAllEventsButton = new JButton("Clear All Events");
            
            nodeEventsPanel.add(clearAllEventsButton);
            
            headerPanel.add(header);
            
            namePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), ("JMRI Node User Name : " 
                
                + nodeOfInterest.getNodeNumberName() ) ) );
            JButton setNameButton = new JButton("Set Module Name");
            JTextField textFieldName = new JTextField(20);
            textFieldName.setText( nodeOfInterest.getUserName() );
            namePanel.add(textFieldName);
            namePanel.add(setNameButton);
      
            removePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), ("Node Manager")));
            JButton removeNodeButton = new JButton("Remove from Table");
            removePanel.add(removeNodeButton);
            
            String canIdText;
            
            if ( nodeOfInterest.getNodeCanId() > 0 ){
                canIdText = "Current CAN ID : " + nodeOfInterest.getNodeCanId();
            } else {
                canIdText = "CAN ID";
            }
            
            
            canIdPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), ( canIdText)));
            JButton selfCanEnumerateButton = new JButton("CAN ID Self Enumeration");
            JButton setCanIdButton = new JButton("Force set CAN ID");
            
            canIdPanel.add(selfCanEnumerateButton);
            canIdPanel.add(setCanIdButton);
            
            evPane.add(headerPanel);
            evPane.add(namePanel);
            evPane.add(canIdPanel);
            evPane.add(nodeEventsPanel);
            evPane.add(removePanel);
            
            setLayout(new BorderLayout() );
            
            eventScroll = new JScrollPane(evPane);
            
            this.add(eventScroll);
            
            validate();
            repaint();
            
            setNameListener = ae -> {
                nodeModel.setValueAt( textFieldName.getText() , 
                    nodeModel.getNodeRowFromNodeNum(_nodeNum), 
                    CbusNodeTableDataModel.NODE_USER_NAME_COLUMN );
                initComponents(_nodeNum);
            };
            setNameButton.addActionListener(setNameListener);
            
            javax.swing.JCheckBox checkbox = new javax.swing.JCheckBox(
                    ("Remove node xml File"));
            
            Object[] params = {("Remove Node from Manager?"), checkbox};
            
            removeListener = ae -> {
                int option = JOptionPane.showOptionDialog(null, 
                    params, 
                    "Please Confirm", 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (option == JOptionPane.CANCEL_OPTION) {
                    return;
                } else if (option == JOptionPane.OK_OPTION) {
                    nodeModel.removeRow( nodeModel.getNodeRowFromNodeNum(_nodeNum),checkbox.isSelected() );
                }
            };
            removeNodeButton.addActionListener(removeListener);
            
            ActionListener selfEnumerateListener = ae -> {
                // start busy
                busy_dialog = new jmri.util.swing.BusyDialog(null, "CAN ID", false);
                busy_dialog.start();
                // CbusNode will pick the outgoing message up, start timer and show dialogue on error / timeout
                nodeOfInterest.send.eNUM(_nodeNum);
                // cancel the busy
                ThreadingUtil.runOnGUIDelayed( () -> {
                    initComponents(_nodeNum); // refresh pane with new CAN ID
                    busy_dialog.finish();
                    busy_dialog=null;
                },CbusNode.SINGLE_MESSAGE_TIMEOUT_TIME );
            };
            selfCanEnumerateButton.addActionListener(selfEnumerateListener);
            
            ActionListener setCanIdListener = ae -> {
                newCanIdDialogue();
            };
            setCanIdButton.addActionListener(setCanIdListener);

            ActionListener clearAllEventsListener = ae -> {
                int option = JOptionPane.showOptionDialog(null, 
                    "Delete All Events from Node?", 
                    "Please Confirm", 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (option == JOptionPane.CANCEL_OPTION) {
                    return;
                } else if (option == JOptionPane.OK_OPTION) {
                    
                    // check for existing nodes in learn mode
                    if ( nodeModel.getAnyNodeInLearnMode() > -1 ) {
                        log.warn("Cancelling action, node {} is already in learn mode",nodeModel.getAnyNodeInLearnMode());
                        return;
                    }
                    
                    // start busy
                    busy_dialog = new jmri.util.swing.BusyDialog(null, "Clear All Events", false);
                    busy_dialog.start();
                    
                    // node enter learn mode
                    nodeOfInterest.send.nodeEnterLearnEvMode( _nodeNum ); // no response expected but we add a mini delay for other traffic
                    
                    ThreadingUtil.runOnLayoutDelayed( () -> {
                        nodeOfInterest.send.nNCLR(_nodeNum);// no response expected
                    }, 150 );
                    
                    ThreadingUtil.runOnLayoutDelayed( () -> {
                        // node exit learn mode
                        nodeOfInterest.send.nodeExitLearnEvMode( _nodeNum ); // no response expected
                    }, CbusNode.SINGLE_MESSAGE_TIMEOUT_TIME );
                    
                    ThreadingUtil.runOnGUIDelayed( () -> {
                    
                        // stop 
                        busy_dialog.finish();
                        busy_dialog=null;
                    
                        // query new num events which should be 0
                        // RQEVN
                        nodeOfInterest.send.rQEVN( _nodeNum );
                    
                    }, ( CbusNode.SINGLE_MESSAGE_TIMEOUT_TIME + 150 ) );
                    
                }
            };
            clearAllEventsButton.addActionListener(clearAllEventsListener);
        
        }
        catch( NullPointerException e ) {
            // on startup no node selected which will cause this
        }
    }
    
    private boolean CANID_DIALOGUE_OPEN = false;
    private JFormattedTextField rqfield;
    private JLabel rqNNspinnerlabel;
    
    private void newCanIdDialogue() {

        if (CANID_DIALOGUE_OPEN) {
            return;
        }
        
        log.debug("allocating new can id");
        
        CANID_DIALOGUE_OPEN=true;
        
        JPanel rqNNpane = new JPanel();
        JPanel bottomrqNNpane = new JPanel();
        String spinnerlabel="";
        rqNNspinnerlabel = new JLabel(spinnerlabel);
        
        bottomrqNNpane.setLayout(new GridLayout(2, 1));
        rqNNpane.setLayout(new BorderLayout());
        
        String popuplabel;
        popuplabel=("Please Select a new CAN ID");
        
        // forces a value between 1-99
        JSpinner rqnnSpinner = new JSpinner(
            new SpinnerNumberModel(Math.min(99,(Math.max(1,nodeOfInterest.getNodeCanId()))), 1, 99, 1));
        JComponent rqcomp = rqnnSpinner.getEditor();
        rqfield = (JFormattedTextField) rqcomp.getComponent(0);
        DefaultFormatter rqformatter = (DefaultFormatter) rqfield.getFormatter();
        rqformatter.setCommitsOnValidEdit(true);
        rqfield.setBackground(Color.white);
        rqnnSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newval = (Integer) rqnnSpinner.getValue();
                log.debug("new canid selected value {}",newval);
                updateSpinnerFeedback(newval);
            }
        });
        
        bottomrqNNpane.add(rqNNspinnerlabel);
        bottomrqNNpane.add(rqnnSpinner);
        
        rqNNpane.add(bottomrqNNpane, BorderLayout.CENTER);
        
        // forces a value between 1-99
        updateSpinnerFeedback( Math.min(99,(Math.max(1,nodeOfInterest.getNodeCanId()))) );
        
        int option = JOptionPane.showOptionDialog(null, 
            rqNNpane, 
            popuplabel, 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (option == JOptionPane.CANCEL_OPTION) {
            CANID_DIALOGUE_OPEN=false;
        } else if (option == JOptionPane.OK_OPTION) {
            int newval = (Integer) rqnnSpinner.getValue();
           // baseNodeNum = newval;
            
            busy_dialog = new jmri.util.swing.BusyDialog(null, "CAN ID", false);
            busy_dialog.start();
            // CbusNode will pick the outgoing message up, start timer and show dialogue on error / timeout
            
            nodeOfInterest.send.cANID(_nodeNum, newval);
            
            // cancel the busy
            ThreadingUtil.runOnGUIDelayed( () -> {
                initComponents(_nodeNum); // refresh pane with new CAN ID
                busy_dialog.finish();
                busy_dialog=null;
                CANID_DIALOGUE_OPEN=false;
                
            },CbusNode.SINGLE_MESSAGE_TIMEOUT_TIME );            
        }
    }
    
    private void updateSpinnerFeedback( int newval ) {
        if ( nodeModel.getNodeNameFromCanId(newval).isEmpty() ) {
            rqfield.setBackground(Color.white);
            rqNNspinnerlabel.setText("");
        }
        else {
            rqfield.setBackground(Color.yellow);
            rqNNspinnerlabel.setText("In Use by " + nodeModel.getNodeNameFromCanId(newval) );
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeSetupPane.class);
    
}
