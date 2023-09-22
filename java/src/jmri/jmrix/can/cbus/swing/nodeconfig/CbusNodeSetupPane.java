package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatter;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTimerManager;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeSetupPane extends CbusNodeConfigTab {
    
    private ActionListener setNameListener;
    private ActionListener removeListener;
    private ActionListener setCanIdListener;
    private ActionListener selfEnumerateListener;
    private ActionListener clearAllEventsListener;
    private jmri.util.swing.BusyDialog busy_dialog;
    
    private JButton setNameButton;
    private JButton removeNodeButton;
    private JButton selfCanEnumerateButton;
    private JButton setCanIdButton;
    private JButton clearAllEventsButton;
    private JTextField textFieldName;
    
    /**
     * Create a new instance of CbusNodeSetupPane.
     * @param main the main NodeConfigToolPane this is a pane of.
     */
    protected CbusNodeSetupPane( NodeConfigToolPane main ) {
        super(main);
        getInitPane();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(){
        return "Node Setup";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void changedNode(CbusNode newNode){
        
        textFieldName.setText( nodeOfInterest.getUserName() );
        validate();
        repaint();

    }
    
    private void getInitPane() {
    
        
        JPanel evPane = new JPanel();
        evPane.setLayout(new BoxLayout(evPane, BoxLayout.Y_AXIS));
        
        initListeners();

        JPanel nodeEventsPanel = new JPanel();
        nodeEventsPanel.setBorder(BorderFactory.createTitledBorder(
                  BorderFactory.createEtchedBorder(), Bundle.getMessage("EventCol")));
        clearAllEventsButton = new JButton("Clear All Events");
        clearAllEventsButton.addActionListener(clearAllEventsListener);
        nodeEventsPanel.add(clearAllEventsButton);
        
        evPane.add(getNamePanel());
        evPane.add(getCanIdPanel());
        evPane.add(nodeEventsPanel);
        evPane.add(getRemovePanel());

        JScrollPane eventScroll = new JScrollPane(evPane);
        
        add(eventScroll, BorderLayout.CENTER);
    
    }
    
    private JPanel getNamePanel() {
    
        JPanel namePanel = new JPanel();
        namePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), ("JMRI Node User Name" ) ) );
        setNameButton = new JButton("Set Module User Name");
        textFieldName = new JTextField(20);
        
        namePanel.add(textFieldName);
        namePanel.add(setNameButton);
        setNameButton.addActionListener(setNameListener);
        return namePanel;
    }
    
    private JPanel getCanIdPanel() {
        
        JPanel canIdPanel = new JPanel();
        canIdPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), ( "CAN ID")));
        selfCanEnumerateButton = new JButton("CAN ID Self Enumeration");
        selfCanEnumerateButton.addActionListener(selfEnumerateListener);
        setCanIdButton = new JButton("Force set CAN ID");
        setCanIdButton.addActionListener(setCanIdListener);
        canIdPanel.add(selfCanEnumerateButton);
        canIdPanel.add(setCanIdButton);
        
        return canIdPanel;
    }
    
    private JPanel getRemovePanel() {
        JPanel removePanel = new JPanel();
        removePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), ("Node Manager")));
        removeNodeButton = new JButton("Remove from Table");
        removePanel.add(removeNodeButton);
        removeNodeButton.addActionListener(removeListener);
        return removePanel;
    }
    
    private void initListeners() {   
        
        setNameListener = ae -> {
            nodeOfInterest.setUserName(textFieldName.getText());
            changedNode(nodeOfInterest);
        };
        
        removeListener = ae -> {
            JCheckBox checkbox = new JCheckBox(("Remove node xml File"));
            int oldRow = Math.max(0, getNodeRow()-1);
            int option = JmriJOptionPane.showConfirmDialog(this, 
                new Object[]{("Remove Node from Manager?"), checkbox}, 
                "Please Confirm", 
                JmriJOptionPane.OK_CANCEL_OPTION);
            if ( option == JmriJOptionPane.OK_OPTION ) {
                getMainPane().getNodeModel().
                removeRow( getMainPane().getNodeModel().getNodeRowFromNodeNum(nodeOfInterest.getNodeNumber())
                ,checkbox.isSelected() );
                if (getMainPane().nodeTable.getRowCount() > 0 ) {
                    getMainPane().nodeTable.getSelectionModel().setSelectionInterval(oldRow,oldRow);
                    getMainPane().tabbedPane.setSelectedIndex(0);
                }
            }
        };
        
        selfEnumerateListener = ae -> {
            // start busy
            busy_dialog = new jmri.util.swing.BusyDialog(null, "CAN ID", false);
            busy_dialog.start();
            // CbusNode will pick the outgoing message up, start timer and show dialogue on error / timeout
            nodeOfInterest.send.eNUM(nodeOfInterest.getNodeNumber());
            // cancel the busy
            ThreadingUtil.runOnGUIDelayed(() -> {
                changedNode(nodeOfInterest); // refresh pane with new CAN ID
                busy_dialog.finish();
                busy_dialog=null;
            },CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME );
        };
        
        setCanIdListener = ae -> {
            newCanIdDialogue();
        };
        

        clearAllEventsListener = ae -> {
            int option = JmriJOptionPane.showConfirmDialog(this, 
                "Delete All Events from Node?", 
                "Please Confirm", 
                JmriJOptionPane.OK_CANCEL_OPTION);
            if ( option == JmriJOptionPane.OK_OPTION ) {

                // check for existing nodes in learn mode
                if ( getMainPane().getNodeModel().getAnyNodeInLearnMode() > -1 ) {
                    log.warn("Cancelling action, node {} is already in learn mode",getMainPane().getNodeModel().getAnyNodeInLearnMode());
                    return;
                }

                // start busy
                busy_dialog = new jmri.util.swing.BusyDialog(null, "Clear All Events", false);
                busy_dialog.start();

                // node enter learn mode
                nodeOfInterest.send.nodeEnterLearnEvMode( nodeOfInterest.getNodeNumber() ); // no response expected but we add a mini delay for other traffic

                ThreadingUtil.runOnLayoutDelayed( () -> {
                    nodeOfInterest.send.nNCLR(nodeOfInterest.getNodeNumber());// no response expected
                }, 150 );

                ThreadingUtil.runOnLayoutDelayed(() -> {
                    // node exit learn mode
                    nodeOfInterest.send.nodeExitLearnEvMode( nodeOfInterest.getNodeNumber() ); // no response expected
                }, CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME );

                ThreadingUtil.runOnGUIDelayed(() -> {

                    // stop 
                    busy_dialog.finish();
                    busy_dialog=null;

                    // query new num events which should be 0
                    // RQEVN
                    nodeOfInterest.send.rQEVN( nodeOfInterest.getNodeNumber() );

                }, ( CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME + 150 ) );

            }
        };
        
        
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
        rqnnSpinner.addChangeListener((ChangeEvent e) -> {
            int newval = (Integer) rqnnSpinner.getValue();
            log.debug("new canid selected value {}",newval);
            updateSpinnerFeedback(newval);
        });
        
        bottomrqNNpane.add(rqNNspinnerlabel);
        bottomrqNNpane.add(rqnnSpinner);
        
        rqNNpane.add(bottomrqNNpane, BorderLayout.CENTER);
        
        // forces a value between 1-99
        updateSpinnerFeedback( Math.min(99,(Math.max(1,nodeOfInterest.getNodeCanId()))) );
        
        int option = JmriJOptionPane.showConfirmDialog(this, 
            rqNNpane, 
            popuplabel, 
            JmriJOptionPane.OK_CANCEL_OPTION);
        if ( option == JmriJOptionPane.CANCEL_OPTION || option == JmriJOptionPane.CLOSED_OPTION ) {
            CANID_DIALOGUE_OPEN=false;
        } else if ( option == JmriJOptionPane.OK_OPTION ) {
            int newval = (Integer) rqnnSpinner.getValue();
           // baseNodeNum = newval;
            
            busy_dialog = new jmri.util.swing.BusyDialog(null, "CAN ID", false);
            busy_dialog.start();
            // CbusNode will pick the outgoing message up, start timer and show dialogue on error / timeout
            
            nodeOfInterest.send.cANID(nodeOfInterest.getNodeNumber(), newval);
            
            // cancel the busy
            ThreadingUtil.runOnGUIDelayed(() -> {
                changedNode(nodeOfInterest); // refresh pane with new CAN ID
                busy_dialog.finish();
                busy_dialog=null;
                CANID_DIALOGUE_OPEN=false;
                
            },CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME );            
        }
    }
    
    private void updateSpinnerFeedback( int newval ) {
        if ( getMainPane().getNodeModel().getNodeNameFromCanId(newval).isEmpty() ) {
            rqfield.setBackground(Color.white);
            rqNNspinnerlabel.setText("");
        }
        else {
            rqfield.setBackground(Color.yellow);
            rqNNspinnerlabel.setText("In Use by " + getMainPane().getNodeModel().getNodeNameFromCanId(newval) );
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeSetupPane.class);

}
