// RouteTableAction.java

package jmri.jmrit.beantable;

import jmri.RouteManager;
import jmri.DefaultRoute;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Route;
import jmri.Turnout;
import jmri.Sensor;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import com.sun.java.util.collections.List;

import jmri.util.com.sun.Comparator;


/**
 * Swing action to create and register a Route Table
 
 * Based in part on SignalHeadTableAction.java by Bob Jacobson
 *
 * @author	Dave Duchamp    Copyright (C) 2004
 * @version     $Revision: 1.12 $
 */

public class RouteTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public RouteTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Route manager available
        if (jmri.InstanceManager.routeManagerInstance()==null) {
            setEnabled(false);
        }
        
        // check a constraint required by this implementation!
        if ( Route.ONACTIVE != 0 || Route.ONINACTIVE != 1
            || Route.VETOACTIVE != 2 || Route.VETOINACTIVE !=3 )
            log.error("assumption invalid in RouteTable implementation");
    }
    public RouteTableAction() { this("Route Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Routes
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public Manager getManager() { return jmri.InstanceManager.routeManagerInstance(); }
            public NamedBean getBySystemName(String name) { 
                    return jmri.InstanceManager.routeManagerInstance().getBySystemName(name);
            }
            public void clickOn(NamedBean t) {
               ((Route)t).setRoute();
            }
            public String getValue(String s) {
                return "Set";
            }
            public JButton configureButton() {
                return new JButton(" Set ");
            }
        };
    }

    void setTitle() {
        f.setTitle("Route Table");
    }

	String setStateClosed = 
			"Set "+InstanceManager.turnoutManagerInstance().getClosedText();
	String setStateThrown =
			"Set "+InstanceManager.turnoutManagerInstance().getThrownText();
//	String setStateToggle = "Toggle";

    String[] sensorModes = new String[]{"On Active", "On Inactive", "Veto Active", "Veto Inactive"};
    
    JFrame addFrame = null;
    TableModel routeTurnoutModel = null;
    JTextField name = new JTextField(10);
    JTextField userName = new JTextField(22);
    JTextField sensor1 = new JTextField(8);
    JComboBox  sensor1mode = new JComboBox(sensorModes);
    JTextField sensor2 = new JTextField(8);
    JComboBox  sensor2mode = new JComboBox(sensorModes);
    JTextField sensor3 = new JTextField(8);
    JComboBox  sensor3mode = new JComboBox(sensorModes);
    JTextField cTurnout = new JTextField(8);

    JComboBox cTurnoutStateBox = new JComboBox();
    int turnoutClosedIndex = 0;
    int turnoutThrownIndex = 1;
    
    ButtonGroup selGroup = null;
    JRadioButton allButton = null;   
    JRadioButton includedButton = null; 
      
    JLabel nameLabel = new JLabel("Route System Name:");
    JLabel userLabel = new JLabel("Route User Name:");
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");
    
    JButton create = new JButton("Add Route");
    JButton edit = new JButton("Edit Route");
    JButton delete = new JButton("Delete Route");
    JButton update = new JButton("Update Route");
    JButton cancel = new JButton("Cancel");
    
    String createInst = "To create a new Route, enter definition, then click 'Add Route'.";
    String editInst = "To edit an existing Route, enter system name, then click 'Edit Route'.";
    String updateInst = "To change this Route, make changes above, then click 'Update Route'.";
    String cancelInst = "To leave Edit mode, without changing this Route, click 'Cancel',";

    JLabel status1 = new JLabel(createInst);
    JLabel status2 = new JLabel(editInst);
    
    JPanel p2x = null;   // Turnout list table

    Route curRoute = null;
    boolean routeCreated = false;
    boolean editMode = false;

    void addPressed(ActionEvent e) {
        // Initialize the turnout list
		String setStateClosed = "Set "+InstanceManager.turnoutManagerInstance().getClosedText();
        for (int i = 0; i<MAX_TURNOUTS ; i++ ) {
            includeTurnout[i] = false;
			setState[i] = setStateClosed;
            includedPosition[i] = 0;
        }
        turnoutSysNameList = InstanceManager.turnoutManagerInstance().getSystemNameList();
        numTurnouts = turnoutSysNameList.size();
        if (numTurnouts > MAX_TURNOUTS) {
            numTurnouts = MAX_TURNOUTS;
        }
        // Set up window
        if (addFrame==null) {
            addFrame = new JFrame("Add/Edit Route");
            addFrame.setLocation(100,30);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            Container contentPane = addFrame.getContentPane();        
            // add system name
            JPanel ps = new JPanel(); 
            ps.setLayout(new FlowLayout());
            ps.add(nameLabel);
            ps.add(name);
            name.setToolTipText("Enter system name for new Route, e.g. R12.");
            ps.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            contentPane.add(ps);
            // add user name
            JPanel p = new JPanel(); 
            p.setLayout(new FlowLayout());
            p.add(userLabel);
            p.add(userName);
            userName.setToolTipText("Enter user name for new Route, e.g. Clear Mainline.");
            contentPane.add(p);
            // add Turnout Display Choice
            JPanel py = new JPanel();
            py.add(new JLabel("Show "));
            selGroup = new ButtonGroup();
            allButton = new JRadioButton("All",true);
            selGroup.add(allButton);
            py.add(allButton);
            allButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        // Setup for display of all Turnouts, if needed
                        if (!showAll) {
                            showAll = true;
                            ((RouteTurnoutModel)routeTurnoutModel).
                                                fireTableDataChanged();
                        }
                    }
                });
            includedButton = new JRadioButton("Included",false);
            selGroup.add(includedButton);
            py.add(includedButton);
            includedButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        // Setup for display of included Turnouts only, if needed
                        if (showAll) {
                            showAll = false;
                            initializeIncludedList();
                            ((RouteTurnoutModel)routeTurnoutModel).
                                                fireTableDataChanged();
                        }
                    }
                });
            py.add(new JLabel("  Turnouts"));
            contentPane.add(py);
            // add turnout table
            p2x = new JPanel();
            JPanel p2xSpace = new JPanel();
            p2xSpace.setLayout(new BoxLayout(p2xSpace, BoxLayout.Y_AXIS));
            p2xSpace.add(new JLabel("XXX"));
            p2x.add(p2xSpace);
            JPanel p21 = new JPanel();
            p21.setLayout(new BoxLayout(p21, BoxLayout.Y_AXIS));
            p21.add(new JLabel("Please select "));
            p21.add(new JLabel(" Turnouts to "));
            p21.add(new JLabel(" be included "));
            p21.add(new JLabel(" in this Route."));
            p2x.add(p21);
            routeTurnoutModel = new RouteTurnoutModel();
            JTable routeTurnoutTable = jmri.util.JTableUtil.sortableDataModel(routeTurnoutModel);
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)routeTurnoutTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(RouteTurnoutModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            routeTurnoutTable.setRowSelectionAllowed(false);
            routeTurnoutTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(480,100));
            JComboBox stateCombo = new JComboBox();
   			stateCombo.addItem(setStateClosed);
			stateCombo.addItem(setStateThrown);
//			stateCombo.addItem("Toggle");
            TableColumnModel routeTurnoutColumnModel = routeTurnoutTable.getColumnModel();
            TableColumn includeColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.INCLUDE_COLUMN);
            includeColumn.setResizable(false);
            includeColumn.setMinWidth(50);
            includeColumn.setMaxWidth(60);
            TableColumn sNameColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.SNAME_COLUMN);
            sNameColumn.setResizable(true);
            sNameColumn.setMinWidth(75);
            sNameColumn.setMaxWidth(95);
            TableColumn uNameColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.UNAME_COLUMN);
            uNameColumn.setResizable(true);
            uNameColumn.setMinWidth(210);
            uNameColumn.setMaxWidth(260);
            TableColumn stateColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.STATE_COLUMN);
            stateColumn.setCellEditor(new DefaultCellEditor(stateCombo));
            stateColumn.setResizable(false);
            stateColumn.setMinWidth(90);
            stateColumn.setMaxWidth(100);
            JScrollPane routeTurnoutScrollPane = new JScrollPane(routeTurnoutTable);
            p2x.add(routeTurnoutScrollPane,BorderLayout.CENTER);
            contentPane.add(p2x);
            p2x.setVisible(true);
            // add control sensor table
            JPanel p3 = new JPanel();
            p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
            JPanel p31 = new JPanel();
            p31.add(new JLabel("Enter Sensors that trigger this Route (optional)"));
            p3.add(p31);
            JPanel p32 = new JPanel();
            p32.add(new JLabel("Sensors: "));
            p32.add(sensor1);
            p32.add(sensor1mode);
            p32.add(sensor2);
            p32.add(sensor2mode);
            p32.add(sensor3);
            p32.add(sensor3mode);
            sensor1.setText("");
            sensor2.setText("");
            sensor3.setText("");
            String sensorHint = "Enter a Sensor system name or nothing.";
            sensor1.setToolTipText(sensorHint);
            sensor2.setToolTipText(sensorHint);
            sensor3.setToolTipText(sensorHint);
            p3.add(p32);
            // add control turnout
            JPanel p33 = new JPanel();
            p33.add(new JLabel("Enter a Turnout that triggers this Route (optional)"));
            p3.add(p33);
            JPanel p34 = new JPanel();
            p34.add(new JLabel("Turnout: "));
            p34.add(cTurnout);
            cTurnout.setText("");
            cTurnout.setToolTipText("Enter a Turnout system name (real or phantom) for throttle control.");
            p34.add(new JLabel("   Turnout State: "));
            cTurnoutStateBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
            turnoutClosedIndex = 0;
            cTurnoutStateBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
            turnoutThrownIndex = 1;
            cTurnoutStateBox.setToolTipText("Setting control Turnout to selected state will trigger Route.");
            p34.add(cTurnoutStateBox);
            p3.add(p34);
            Border p3Border = BorderFactory.createEtchedBorder();
            p3.setBorder(p3Border);
            contentPane.add(p3);
            // add notes panel
            JPanel pa = new JPanel();
            pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(status1);
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout());
            p2.add(status2);
            pa.add(p1);
            pa.add(p2);
            Border pBorder = BorderFactory.createEtchedBorder();
            pa.setBorder(pBorder);
            contentPane.add(pa);
            // add buttons - Add Route button
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout());
            pb.add(create);
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText("Add a new Route using data entered above");
            // Edit Route button 
            pb.add(edit);
            edit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editPressed(e);
                }
            });
            edit.setToolTipText("Set up to edit Route in System Name");
            // Delete Route button
            pb.add(delete);
            delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            delete.setToolTipText("Delete the Route in System Name");
            // Update Route button
            pb.add(update);
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            update.setToolTipText("Change this Route and leave Edit mode");
            // Cancel button  
            pb.add(cancel);
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancel.setToolTipText("Leave Edit mode without changing the Route");
            // Show the initial buttons, and hide the others
            cancel.setVisible(false);
            update.setVisible(false);
            edit.setVisible(true);
            create.setVisible(true);
            delete.setVisible(false);  // keep off for the time being
            contentPane.add(pb);
            // pack and release space
            addFrame.pack();
            p2xSpace.setVisible(false);
        }
        // set listener for window closing
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    // remind to save, if Route was created or edited
                    if (routeCreated) {
                        javax.swing.JOptionPane.showMessageDialog(addFrame,
                            "Remember to save your Route information.","Reminder",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        routeCreated = false;
                    }
                    // hide addFrame
                    addFrame.setVisible(false);
                    // if in Edit, cancel edit mode
                    if (editMode) {
                        cancelEdit();
                    }
                }
            });
        // display the window
        addFrame.show();
        ((RouteTurnoutModel)routeTurnoutModel).fireTableDataChanged();
    }

    /**
     * Initialize list of included turnout positions
     */
    void initializeIncludedList() {
        numIncludedTurnouts = 0;
        if (numTurnouts > 0) {
            for (int i = 0; i<numTurnouts; i++) {
                if (includeTurnout[i]) {
                    includedPosition[numIncludedTurnouts] = i;
                    numIncludedTurnouts ++;
                }
            }
        }
    }

    /**
     * Responds to the Add button
     */
    void createPressed(ActionEvent e) {
        // Get system name and user name
        String sName = name.getText().toUpperCase();
        String uName = userName.getText();
        // check if a Route with this system name already exists
        Route g = jmri.InstanceManager.routeManagerInstance().getBySystemName(sName);
        if (g!=null) {
            // Route already exists
            status1.setText("Error - Route with this system name already exists.");
            return;
        }
        // check if a Route with the same user name exists
        if (!uName.equals("")) {
            g = jmri.InstanceManager.routeManagerInstance().getByUserName(uName);
            if (g!=null) {
                // Route with this user name already exists
                status1.setText("Error - Route with this user name already exists.");
                return;
            }
        }
        // Create the new Route
        g = jmri.InstanceManager.routeManagerInstance().createNewRoute(sName,uName);
        if (g==null) {
            // should never get here
            log.error("Unknown failure to create Route with System Name: "+sName);
            return;
        }
        // Set Turnout information 
        int numIncluded = setTurnoutInformation(g);
        // Set optional control Sensor and control Turnout information
        setControlInformation(g);
        // Provide feedback to user
        status1.setText("New Route created: "+sName+", "+uName+", "+
                                                        numIncluded+" Turnouts");
        status2.setText(editInst);
        // activate the route
        g.activateRoute();
        
        // mark as dirty so save prompt displayed
        routeCreated = true;
    }

    /**
     * Sets the Turnout information for adding or editting
     */
    int setTurnoutInformation(Route g) {
        int numIncluded = 0;
        int state = 0;
        for (int i = 0; i<numTurnouts; i++) {
            if (includeTurnout[i]) {
                if (setState[i].equals(setStateClosed) ) {
                    state = Turnout.CLOSED;
                }
                else if (setState[i].equals(setStateThrown) ) {
                    state = Turnout.THROWN;
                }
//				else {
//					state = Route.TOGGLE;
                g.addTurnoutToRoute((String)turnoutSysNameList.get(i),state);
                numIncluded ++;
            }
        }
        return numIncluded;
    }

    /**
     * Service routine to turn a Sensor mode string into a value
     *
     */
    int sensorMode(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        if (mode.equals(sensorModes[0])) return Route.ONACTIVE;
        else if (mode.equals(sensorModes[1])) return Route.ONINACTIVE;
        else if (mode.equals(sensorModes[2])) return Route.VETOACTIVE;
        else if (mode.equals(sensorModes[3])) return Route.VETOINACTIVE;
        else {
            log.warn("unexpected mode string in sensorMode: "+mode);
            throw new IllegalArgumentException();
       }
    }
    
    /**
     * Sets the Sensor and Turnout control information for adding or editting if any
     */
    void setControlInformation(Route g) {
        // Get sensor control information if any
        String sensorSystemName = sensor1.getText();
        if (sensorSystemName.length() > 2) {
            Sensor s1 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s1==null) || (!g.addSensorToRoute(s1.getSystemName(), sensorMode(sensor1mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        sensorSystemName = sensor2.getText();
        if (sensorSystemName.length() > 2) {
            Sensor s2 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s2==null) || (!g.addSensorToRoute(s2.getSystemName(), sensorMode(sensor2mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        sensorSystemName = sensor3.getText();
        if (sensorSystemName.length() > 2) {
            Sensor s3 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s3==null) || (!g.addSensorToRoute(s3.getSystemName(), sensorMode(sensor3mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        // Set turnout information if there is any
        String turnoutSystemName = cTurnout.getText();
        if (turnoutSystemName.length() > 2) {
            Turnout t = InstanceManager.turnoutManagerInstance().
                                    provideTurnout(turnoutSystemName);
            if (t!=null) {
                g.setControlTurnout(t.getSystemName());
            }
            else {
                g.setControlTurnout("");
                log.error("Unexpected failure to add control Turnout '"+
                        t.getSystemName()+"' to Route '"+g.getSystemName()+"'.");
            }
            // set up control turnout state
            if ( cTurnoutStateBox.getSelectedItem().equals(setStateThrown) ) {
                g.setControlTurnoutState(jmri.Turnout.THROWN);
            }
            else {
                g.setControlTurnoutState(jmri.Turnout.CLOSED);
            }
        }
        else {
            // No control Turnout was entered
            g.setControlTurnout("");
        }
    }
        
    /**
     * Responds to the Edit button
     */
    void editPressed(ActionEvent e) {
        // identify the Route with this name if it already exists
        String sName = name.getText().toUpperCase();
        Route g = jmri.InstanceManager.routeManagerInstance().getBySystemName(sName);
        if (g==null) {
            // Route does not exist, so cannot be edited
            status1.setText("Route with the entered System Name was not found.");
            return;
        }
        // Route was found, make its system name not changeable
        curRoute = g;
        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        name.setVisible(false);
        // deactivate this Route
        curRoute.deActivateRoute();
        // get information for this Light
        userName.setText(g.getUserName());
        // set up Turnout list for this route
        if (numTurnouts>0) {
            int tState = 0;
            for (int i=0; i<numTurnouts; i++) {
                String tSysName = (String)turnoutSysNameList.get(i);
                if (g.isTurnoutIncluded(tSysName)) {
                    includeTurnout[i] = true;
                    tState = g.getTurnoutSetState(tSysName);
					if (tState==Turnout.CLOSED) {
						setState[i] = setStateClosed;
					}
                    else if (tState==Turnout.THROWN) {
                        setState[i] = setStateThrown;
                    }
//					else if (tState==Route.TOGGLE) {
//						setState[i] = setStateToggle;
//					}
                }
                else {
                    includeTurnout[i] = false;
					setState[i] = setStateClosed;
                }
            }
        }
        // set up Sensors if there are any
        String[] temNames = new String[Route.MAX_CONTROL_SENSORS];
        int[] temModes = new int[Route.MAX_CONTROL_SENSORS];
        for (int k = 0; k<Route.MAX_CONTROL_SENSORS; k++) {
            temNames[k] = g.getRouteSensorName(k);
            temModes[k] = g.getRouteSensorMode(k);
        }
        sensor1.setText(temNames[0]);
        sensor1mode.setSelectedItem(sensorModes[temModes[0]]);
        sensor2.setText(temNames[1]);
        sensor2mode.setSelectedItem(sensorModes[temModes[1]]);
        sensor3.setText(temNames[2]);
        sensor3mode.setSelectedItem(sensorModes[temModes[2]]);
        // set up control Turnout if there is one
        cTurnout.setText(g.getControlTurnout()); 
        cTurnoutStateBox.setSelectedIndex(turnoutClosedIndex);
        if (g.getControlTurnoutState()==Turnout.THROWN) {
            cTurnoutStateBox.setSelectedIndex(turnoutThrownIndex);
        }
        // begin with showing all Turnouts   
        cancelIncludedOnly();
        // set up buttons and notes
        status1.setText(updateInst);
        status2.setText(cancelInst);
        status2.setVisible(true);
        cancel.setVisible(true);
        update.setVisible(true);
        edit.setVisible(false);
        create.setVisible(false);
        fixedSystemName.setVisible(true);
        name.setVisible(false);
        editMode = true;
    }

    /**
     * Responds to the Delete button
     */
    void deletePressed(ActionEvent e) {
// place holder for future function    
    }

    /**
     * Responds to the Update button
     */
    void updatePressed(ActionEvent e) {
        Route g = curRoute;
        // Check if the User Name has been changed
        String uName = userName.getText();
        if ( !(uName.equals(g.getUserName())) && !uName.equals("") ) {
            // user name has changed - check if already in use
            Route p = jmri.InstanceManager.routeManagerInstance().getByUserName(uName);
            if (p!=null) {
                // Route with this user name already exists
                status1.setText("Error - Route: '"+p.getSystemName()+
                                        "' is already using the entered user name.");
                return;
            }
            // user name is unique, change it
            g.setUserName(uName);     
        }
        // clear the current Turnout information for this Route
        g.clearRouteTurnouts();
        // add those indicated in the window
        int numIncluded = setTurnoutInformation(g);
        // clear the current Sensor information for this Route
        g.clearRouteSensors();
        // add control Sensors and a control Turnout if entered in the window
        setControlInformation(g);
        // move to show all turnouts if not there
        cancelIncludedOnly();
        // Provide feedback to user
        status1.setText("Route updated: "+g.getSystemName()+", "+uName+", "+
                                                            numIncluded+" Turnouts");
        // set up buttons and notes
        status2.setText(editInst);
        status2.setVisible(true);
        cancel.setVisible(false);
        update.setVisible(false);
        edit.setVisible(true);
        create.setVisible(true);
        fixedSystemName.setVisible(false);
        name.setVisible(true);
        // reactivate the Route
        curRoute.activateRoute();    
        routeCreated = true;
        // get out of edit mode
        editMode = false;
        curRoute = null;
    }

    /**
     * Responds to the Cancel button
     */
    void cancelPressed(ActionEvent e) {
        cancelEdit();
    }
    
    /** 
     * Cancels edit mode
     */
    void cancelEdit() {
        if (editMode) {
            status1.setText(createInst);
            status2.setText(editInst);
            status2.setVisible(true);
            cancel.setVisible(false);
            update.setVisible(false);
            edit.setVisible(true);
            create.setVisible(true);
            fixedSystemName.setVisible(false);
            name.setVisible(true);
            // reactivate the Route
            curRoute.activateRoute();
            // get out of edit mode
            editMode = false;
            curRoute = null;
            // move to show all turnouts if not there
            cancelIncludedOnly();
        }
    }
    
    /** 
     * Cancels included Turnouts only option
     */
    void cancelIncludedOnly() {
        if (!showAll) {
            allButton.doClick();
        }
    }

    /**
     * Set up table for selecting Turnouts and Turnout State
     */
    public class RouteTurnoutModel extends AbstractTableModel
    {
        public String getColumnName(int c) {return routeTurnoutColumnNames[c];}
        public Class getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }
        public int getColumnCount () {return 4;}
        public int getRowCount () {
            if (showAll)
                return numTurnouts;
            else
                return numIncludedTurnouts;
        }
        public Object getValueAt (int r,int c) {
            int rx = r;
            if (rx > numTurnouts) {
                return null;
            }
            if (!showAll) {
                rx = includedPosition[r];
            }
            switch (c) {
            case INCLUDE_COLUMN:
                if (includeTurnout[rx]) {
                    return Boolean.TRUE;
                }
                else {
                    return Boolean.FALSE;
                }
            case SNAME_COLUMN:  // slot number
                return turnoutSysNameList.get(rx);
            case UNAME_COLUMN:  //
                return InstanceManager.turnoutManagerInstance().
                        getBySystemName((String)turnoutSysNameList.get(rx)).getUserName();
            case STATE_COLUMN:  //
                return setState[rx];
            default:
                return null;
            }
        }
        public void setValueAt(Object type,int r,int c) {
            int rx = r;
            if (rx > numTurnouts) {
                return;
            }
            if (!showAll) {
                rx = includedPosition[r];
            }
            switch (c) {
                case INCLUDE_COLUMN:  
					if (!((Boolean)type).booleanValue()) {
                        includeTurnout[rx] = false;
                    }
                    else {
                        includeTurnout[rx] = true;
                    }
                    break;
                case STATE_COLUMN: 
                    setState[rx] = (String)type;
                    break;
            }
        }
        public boolean isCellEditable(int r,int c) {
            return ( (c==INCLUDE_COLUMN) || (c==STATE_COLUMN) );
        }
		
        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;
    }
    private boolean showAll = true;   // false indicates show only included Turnouts
    private int numIncludedTurnouts = 0;
    private int numTurnouts = 0;
    private static final int MAX_TURNOUTS = 1000;
    private String[] routeTurnoutColumnNames = {"System Name","User Name",
                                        "Include", "Set State"};
    private List turnoutSysNameList = null;
    private boolean[] includeTurnout = new boolean[MAX_TURNOUTS];
    private String[] setState = new String[MAX_TURNOUTS];
    private int[] includedPosition = new int[MAX_TURNOUTS];  // indexed by row of included turnout in included
                                                            // Turnouts only list.  Contains position in full
                                                            // Turnout list.

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RouteTableAction.class.getName());
}
/* @(#)RouteTableAction.java */
