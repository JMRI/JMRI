// LightTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Light;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

/**
 * Swing action to create and register a
 * LightTable GUI.
 * <P>
 * Based on SignalHeadTableAction.java
 *
 * @author	Dave Duchamp    Copyright (C) 2004
 * @version     $Revision: 1.3 $
 */

public class LightTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public LightTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Light manager available
        if (jmri.InstanceManager.lightManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .lightManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }
    }
    public LightTableAction() { this("Light Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Lights
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.lightManagerInstance().getBySystemName(name).getState();
                switch (val) {
                case Light.ON: return rbean.getString("LightStateOn");
                case Light.OFF: return rbean.getString("LightStateOff");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { 
                return InstanceManager.lightManagerInstance(); 
            }
            public NamedBean getBySystemName(String name) { 
                return InstanceManager.lightManagerInstance().getBySystemName(name);
            }
            public void clickOn(NamedBean t) {
                int oldState = ((Light)t).getState();
                int newState;
                switch (oldState) {
                    case Light.ON: 
                        newState = Light.OFF; 
                        break;
                    case Light.OFF: 
                        newState = Light.ON; 
                        break;
                    default: 
                        newState = Light.OFF; 
                        this.log.warn("Unexpected Light state "+oldState+" becomes OFF");
                        break;
                }
               ((Light)t).setState(newState);
            }
            public JButton configureButton() {
                return new JButton(rbean.getString("LightStateOff"));
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleLightTable"));
    }

    JFrame addFrame = null;
    Light curLight = null;
    boolean lightCreated = false;

    String sensorControl = rb.getString("LightSensorControl");
    String fastClockControl = rb.getString("LightFastClockControl");
    String panelSwitchControl = rb.getString("LightPanelSwitchControl");
    String signalHeadControl = rb.getString("LightSignalHeadControl");
    String turnoutStatusControl = rb.getString("LightTurnoutStatusControl");

    // fixed part of add frame
    JTextField systemName = new JTextField(10);
    JLabel systemNameLabel = new JLabel( rb.getString("LightSystemName") );
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");
    JTextField userName = new JTextField(10);
    JLabel userNameLabel = new JLabel( rb.getString("LightUserName") );
    JComboBox typeBox;
    JLabel typeBoxLabel = new JLabel( rb.getString("LightControlType") );
    int sensorControlIndex;
    int fastClockControlIndex;
    int panelSwitchControlIndex;
    int signalHeadControlIndex;
    int turnoutStatusControlIndex;
    JTextField field1 = new JTextField(10);
    JButton create;
    JButton edit;
    JButton update;
    JButton cancel;
    
    // variable part of add frame
    JLabel f1Label = new JLabel( rb.getString("LightSensor") );
    JTextField field2 = new JTextField(10);
    JLabel f2Label = new JLabel( rb.getString("LightSensorSense") );
    JComboBox stateBox;
    int sensorActiveIndex;
    int sensorInactiveIndex;
    int signalHeadGreenIndex;
    int signalHeadRedIndex;
    int signalHeadYellowIndex;
    int turnoutClosedIndex;
    int turnoutThrownIndex;
    JLabel stateBoxLabel = new JLabel( rb.getString("LightSensorSense") );

    JLabel status1 = new JLabel( rb.getString("LightCreateInst") );
    JLabel status2 = new JLabel( rb.getString("LightEditInst") );
        
    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JFrame( rb.getString("TitleAddLight") );
            Container contentPane = addFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
            panel1.add(systemNameLabel);
            panel1.add(systemName);
            panel1.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            systemName.setToolTipText( rb.getString("LightSystemNameHint") );
            contentPane.add(panel1);
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
            panel2.add(userNameLabel);
            panel2.add(userName);
            userName.setToolTipText( rb.getString("LightUserNameHint") );
            contentPane.add(panel2);
            
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            panel31.add(typeBoxLabel);
            panel31.add(typeBox = new JComboBox(new String[]{
// temporarily suppress PanelSwitch and FastClock
//            sensorControl,fastClockControl,panelSwitchControl,signalHeadControl,turnoutStatusControl
            sensorControl,signalHeadControl,turnoutStatusControl
            }));
            sensorControlIndex = 0;
            fastClockControlIndex = 1;
            panelSwitchControlIndex = 2;
// change the following when fast clock or panel switch are included
            signalHeadControlIndex = 1;      // eventually should be 3
            turnoutStatusControlIndex = 2;   // eventually should be 4
// end area to be changed
            typeBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    controlTypeChanged();
                }
            });
            typeBox.setToolTipText( rb.getString("LightControlTypeHint") );
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            panel32.add(f1Label);
            panel32.add(field1);
            field1.setToolTipText( rb.getString("LightSensorHint") );
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            panel33.add(f2Label);
            panel33.add(stateBox = new JComboBox(new String[]{
                rbean.getString("SensorStateActive"),rbean.getString("SensorStateInactive"),
            }));
            stateBox.setToolTipText( rb.getString("LightSensorSenseHint") );
            panel33.add(field2);
            field2.setVisible(false);
            panel3.add(panel31);
            panel3.add(panel32);
            panel3.add(panel33);
            Border panel3Border = BorderFactory.createEtchedBorder();
            Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                rb.getString("LightControlBorder") );
            panel3.setBorder(panel3Titled);                
            contentPane.add(panel3);
            
            JPanel panel4 = new JPanel();
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            panel41.add(status1);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            panel42.add(status2);
            panel4.add(panel41);
            panel4.add(panel42);
            Border panel4Border = BorderFactory.createEtchedBorder();
            panel4.setBorder(panel4Border);
            contentPane.add(panel4);

            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(create = new JButton(rb.getString("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText( rb.getString("LightCreateButtonHint") );
            panel5.add(edit = new JButton(rb.getString("ButtonEdit")));
            edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editPressed(e);
                }
            });
            edit.setToolTipText( rb.getString("LightEditButtonHint") );
            panel5.add(update = new JButton(rb.getString("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            update.setToolTipText( rb.getString("LightUpdateButtonHint") );
            panel5.add(cancel = new JButton(rb.getString("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancel.setToolTipText( rb.getString("LightCancelButtonHint") );
            cancel.setVisible(false);
            update.setVisible(false);
            edit.setVisible(true);
            create.setVisible(true);
            contentPane.add(panel5);
        }
        typeBox.setSelectedIndex(0);  // force GUI status consistent

        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    // remind to save, if Light was created or edited
                    if (lightCreated) {
                        javax.swing.JOptionPane.showMessageDialog(addFrame,
                            rb.getString("Reminder1")+"\n"+rb.getString("Reminder2"),
                                rb.getString("ReminderTitle"),
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    }
                    addFrame.setVisible(false);
                    addFrame.dispose();
                }
            });
            
        addFrame.pack();
        addFrame.show();
    }

    /**
     * Reacts to a control type change
     */
    void controlTypeChanged() {
        setUpControlType( (String)typeBox.getSelectedItem() );
    }

    /**
     * Sets the Control Information according to control type
     */
    void setUpControlType(String ctype) {
        if ( sensorControl.equals(ctype) ) {
            // set up window for sensor control
            f1Label.setText( rb.getString("LightSensor") );
            field1.setToolTipText( rb.getString("LightSensorHint") );
            f2Label.setText( rb.getString("LightSensorSense") );
            stateBox.removeAllItems();
            stateBox.addItem( rbean.getString("SensorStateActive") );
            sensorActiveIndex = 0;
            stateBox.addItem( rbean.getString("SensorStateInactive") );
            sensorInactiveIndex = 1;
            stateBox.setToolTipText( rb.getString("LightSensorSenseHint") );
            f2Label.setVisible(true);
            field2.setVisible(false);
            stateBox.setVisible(true);
        } 
        else if (fastClockControl.equals(ctype) ) {
            // set up window for fast clock control
            f1Label.setText( rb.getString("LightScheduleOn") );
            field1.setToolTipText( rb.getString("LightScheduleHint") );
            f2Label.setText( rb.getString("LightScheduleOff") );
            field2.setToolTipText( rb.getString("LightScheduleHint") );
            f2Label.setVisible(true);
            field2.setVisible(true);
            stateBox.setVisible(false);
        }
        else if (panelSwitchControl.equals(ctype) ) {
            // set up window for panel switch control
            f1Label.setText( rb.getString("LightPanelSwitch") );
            field1.setToolTipText( rb.getString("LightPanelSwitchHint") );
            f2Label.setVisible(false);
            field2.setVisible(false);
            stateBox.setVisible(false);
        }
        else if (signalHeadControl.equals(ctype) ) {
            // set up window for signal head control
            f1Label.setText( rb.getString("LightSignalHead") );
            field1.setToolTipText( rb.getString("LightSignalHeadHint") );
            f2Label.setText( rb.getString("LightSignalHeadAspect") );
            stateBox.removeAllItems();
            stateBox.addItem( rbean.getString("SignalHeadStateGreen") );
            signalHeadGreenIndex = 0;
            stateBox.addItem( rbean.getString("SignalHeadStateYellow") );
            signalHeadYellowIndex = 1;
            stateBox.addItem( rbean.getString("SignalHeadStateRed") );
            signalHeadRedIndex = 2;
            stateBox.setToolTipText( rb.getString("LightSignalHeadAspectHint") );
            f2Label.setVisible(true);
            field2.setVisible(false);
            stateBox.setVisible(true);
        }
        else if (turnoutStatusControl.equals(ctype) ) {
            // set up window for turnout status control
            f1Label.setText( rb.getString("LightTurnout") );
            field1.setToolTipText( rb.getString("LightTurnoutHint") );
            f2Label.setText( rb.getString("LightTurnoutSense") );
            stateBox.removeAllItems();
            stateBox.addItem( rbean.getString("TurnoutStateClosed") );
            turnoutClosedIndex = 0;
            stateBox.addItem( rbean.getString("TurnoutStateThrown") );
            turnoutThrownIndex = 1;
            stateBox.setToolTipText( rb.getString("LightTurnoutSenseHint") );
            f2Label.setVisible(true);
            field2.setVisible(false);
            stateBox.setVisible(true);
        }
        else log.error("Unexpected control type in controlTypeChanged: "+ctype);
    }
    
    /**
     * Responds to the Create button
     */
    void createPressed(ActionEvent e) {
        String suName = systemName.getText();
        String uName = userName.getText();
        // Does System Name have a valid format
        if (!InstanceManager.lightManagerInstance().validSystemNameFormat(suName)) {
            // Invalid System Name format
            log.warn("Invalid Light system name format entered: "+suName);
            status1.setText( rb.getString("LightError3") );
            status2.setText( rb.getString("LightError4") );
            status2.setVisible(true);
            return;
        }
        // Format is valid, normalize it
        String sName = InstanceManager.lightManagerInstance().normalizeSystemName(suName);
        // check if a Light with this name already exists
        Light g = InstanceManager.lightManagerInstance().getBySystemName(sName);
        if (g!=null) {
            // Light already exists
            status1.setText( rb.getString("LightError1") );
            status2.setText( rb.getString("LightError2") );
            status2.setVisible(true);
            return;
        }
        // check if Light exists under an alternate name if an alternate name exists
        String altName = InstanceManager.lightManagerInstance().convertSystemNameToAlternate(suName);
        if (altName != "") {
            g = InstanceManager.lightManagerInstance().getBySystemName(altName);
            if (g!=null) {
                // Light already exists
                status1.setText( rb.getString("LightError10")+" '"+altName+"' "+
                                    rb.getString("LightError11") );
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return;
            }
        }
        // check if a Light with the same user name exists
        g = InstanceManager.lightManagerInstance().getByUserName(uName);
        if (g!=null) {
            // Light with this user name already exists
            status1.setText( rb.getString("LightError8") );
            status2.setText( rb.getString("LightError9") );
            status2.setVisible(true);
            return;
        }
        // Does System Name correspond to configured hardware
        if (!InstanceManager.lightManagerInstance().validSystemNameConfig(sName)) {
            // System Name not in configured hardware
            status1.setText( rb.getString("LightError5") );
            status2.setText( rb.getString("LightError6") );
            status2.setVisible(true);
            return;
        }
        // Create the new Light
        g = InstanceManager.lightManagerInstance().newLight(sName,uName);
        if (g==null) {
            // should never get here
            log.error("Unknown failure to create Light with System Name: "+sName);
            return;
        }
        // Get control information 
        if (setControlInformation(g)) {
            // sucessful, change messages and activate Light
            status1.setText( rb.getString("LightCreateInst") );
            status2.setText( rb.getString("LightEditInst") );
            status2.setVisible(true);
            g.activateLight();
            lightCreated = true;
        }
    }
    
    /**
     * Responds to the Edit button
     */
    void editPressed(ActionEvent e) {
        // check if a Light with this name already exists
        String suName = systemName.getText();
        String sName = InstanceManager.lightManagerInstance().normalizeSystemName(suName);
        if (sName=="") {
            // Entered system name has invalid format
            status1.setText( rb.getString("LightError3") );
            status2.setText( rb.getString("LightError4") );
            status2.setVisible(true);
            return;
        }            
        Light g = InstanceManager.lightManagerInstance().getBySystemName(sName);
        if (g==null) {
            // check if Light exists under an alternate name if an alternate name exists
            String altName = InstanceManager.lightManagerInstance().convertSystemNameToAlternate(sName);
            if (altName != "") {
                g = InstanceManager.lightManagerInstance().getBySystemName(altName);
                if (g!=null) {
                    sName = altName;
                }
            }
            if (g==null) {
                // Light does not exist, so cannot be edited
                status1.setText( rb.getString("LightError7") );
                status2.setText( rb.getString("LightError6") );
                status2.setVisible(true);
                return;
            }
        }
        // Light was found, make its system name not changeable
        curLight = g;
        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        systemName.setVisible(false);
        // deactivate this light
        curLight.deactivateLight();
        // get information for this Light
        userName.setText(g.getUserName());
        int ctType = g.getControlType();
        switch (ctType) {
            case Light.SENSOR_CONTROL:
                setUpControlType(sensorControl);
                typeBox.setSelectedIndex(sensorControlIndex);
                Sensor s = g.getControlSensor();
                if (s==null) {
                    field1.setText("");
                }
                else {
                    field1.setText(s.getSystemName());
                }
                stateBox.setSelectedIndex(sensorActiveIndex);
                if (g.getControlSensorSense()==Sensor.INACTIVE) {
                    stateBox.setSelectedIndex(sensorInactiveIndex);
                }
                break;
            case Light.FAST_CLOCK_CONTROL:
                break;
            case Light.PANEL_SWITCH_CONTROL:
                break;
            case Light.SIGNAL_HEAD_CONTROL:
                setUpControlType(signalHeadControl);
                typeBox.setSelectedIndex(signalHeadControlIndex);
                SignalHead sh = g.getControlSignalHead();
                if (sh==null) {
                    field1.setText("");
                }
                else {
                    field1.setText(sh.getSystemName());
                }
                stateBox.setSelectedIndex(signalHeadGreenIndex);
                if (g.getControlSignalHeadAspect()==SignalHead.RED) {
                    stateBox.setSelectedIndex(signalHeadRedIndex);
                }
                else if (g.getControlSignalHeadAspect()==SignalHead.YELLOW) {
                    stateBox.setSelectedIndex(signalHeadYellowIndex);
                }
                break;
            case Light.TURNOUT_STATUS_CONTROL:
                setUpControlType(turnoutStatusControl);
                typeBox.setSelectedIndex(turnoutStatusControlIndex);
                Turnout t = g.getControlTurnout();
                if (t==null) {
                    field1.setText("");
                }
                else {
                    field1.setText(t.getSystemName());
                }
                stateBox.setSelectedIndex(turnoutClosedIndex);
                if (g.getControlTurnoutState()==Turnout.THROWN) {
                    stateBox.setSelectedIndex(turnoutThrownIndex);
                }
                break;
            case Light.NO_CONTROL:
                // Set up as undefined sensor control
                setUpControlType(sensorControl);
                typeBox.setSelectedIndex(sensorControlIndex);
                field1.setText("");
                stateBox.setSelectedIndex(sensorActiveIndex);
                break;
        }
        cancel.setVisible(true);
        update.setVisible(true);
        edit.setVisible(false);
        create.setVisible(false);
        status1.setText( rb.getString("LightUpdateInst") );
        status2.setText( "" );
    }

    /**
     * Responds to the Update button
     */
    void updatePressed(ActionEvent e) {
        Light g = curLight;
        // Check if the User Name has been changed
        String uName = userName.getText();
        if ( !(uName.equals(g.getUserName())) ) {
            // user name has changed - check if already in use
            Light p = InstanceManager.lightManagerInstance().getByUserName(uName);
            if (p!=null) {
                // Light with this user name already exists
                status1.setText( rb.getString("LightError8") );
                status2.setText( rb.getString("LightError9") );
                status2.setVisible(true);
                return;
            }
            // user name is unique, change it
            g.setUserName(uName);     
        }
        if (setControlInformation(g)) {
            status1.setText( rb.getString("LightCreateInst") );
            status2.setText( rb.getString("LightEditInst") );
            status2.setVisible(true);
        }
        cancel.setVisible(false);
        update.setVisible(false);
        edit.setVisible(true);
        create.setVisible(true);
        fixedSystemName.setVisible(false);
        systemName.setVisible(true);
        g.activateLight();
        lightCreated = true;
    }

    /**
     * Retrieve control information from window and update Light
     *    Returns 'true' if no errors or warnings.
     */
    private boolean setControlInformation(Light g) {
        // Get control information
        if (sensorControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.SENSOR_CONTROL);
            // Get sensor control information
            Sensor s = InstanceManager.sensorManagerInstance().
                            provideSensor(field1.getText());
            int sState = Sensor.ACTIVE;
            if ( stateBox.getSelectedItem().equals(rbean.getString
                                                    ("SensorStateInactive")) ) {
                sState = Sensor.INACTIVE;
            }
            g.setControlSensor(s);
            g.setControlSensorSense(sState);
            if (s==null) {
                status1.setText( rb.getString("LightWarn1") );
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return (false);
            }
        }
// place holder for fast clock control                 
//        else if (fastClockControl.equals(typeBox.getSelectedItem())) {
//            // Set type of control
//            g.setControlType(Light.FAST_CLOCK_CONTROL);
//        }
// place holder for panel switch control                 
//        else if (panelSwitchControl.equals(typeBox.getSelectedItem())) {
//            // Set type of control
//            g.setControlType(Light.PANEL_SWITCH_CONTROL);
//        }
        else if (signalHeadControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.SIGNAL_HEAD_CONTROL);
            // Get signal head control information
            SignalHead s = InstanceManager.signalHeadManagerInstance().
                            getBySystemName(field1.getText());
            int sState = SignalHead.GREEN;
            if ( stateBox.getSelectedItem().equals(rbean.getString
                                                    ("SignalHeadStateYellow")) ) {
                sState = SignalHead.YELLOW;
            }
            if ( stateBox.getSelectedItem().equals(rbean.getString
                                                    ("SignalHeadStateRed")) ) {
                sState = SignalHead.RED;
            }
            g.setControlSignalHead(s);
            g.setControlSignalHeadAspect(sState);
            if (s==null) {
                status1.setText( rb.getString("LightWarn3") );
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return (false);
            }
        }
        else if (turnoutStatusControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.TURNOUT_STATUS_CONTROL);
            // Get turnout control information
            Turnout t = InstanceManager.turnoutManagerInstance().
                            provideTurnout(field1.getText());
            int tState = Turnout.CLOSED;
            if ( stateBox.getSelectedItem().equals(rbean.getString
                                                    ("TurnoutStateThrown")) ) {
                tState = Turnout.THROWN;
            }
            g.setControlTurnout(t);
            g.setControlTurnoutState(tState);
            if (t==null) {
                status1.setText( rb.getString("LightWarn2") );
                status2.setText( rb.getString("LightEditInst") );
                status2.setVisible(true);
                return (false);
            }
        }
        else {
            log.error("Unexpected control type: "+typeBox.getSelectedItem());
        }
        return (true);
    }

    /**
     * Responds to the Cancel button
     */
    void cancelPressed(ActionEvent e) {
        status1.setText( rb.getString("LightCreateInst") );
        status2.setText( rb.getString("LightEditInst") );
        status2.setVisible(true);
        cancel.setVisible(false);
        update.setVisible(false);
        edit.setVisible(true);
        create.setVisible(true);
        fixedSystemName.setVisible(false);
        systemName.setVisible(true);
        // reactivate the light
        curLight.activateLight();
    }
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LightTableAction.class.getName());
}
/* @(#)LightTableAction.java */
