// RouteTableAction.java

package jmri.jmrit.beantable;

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

/**
 * Swing action to create and register a Route Table
 
 * Based in part on SignalHeadTableAction.java by Bob Jacobsen
 *
 * @author	Dave Duchamp    Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007 
 *
 * @version     $Revision: 1.25 $
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
        
        // check a constraint required by this implementation,
        // because we assume that the codes are the same as the index
        // in a JComboBox
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
		    static public final int ENABLECOL = 3;
		    static public final int SETCOL = 4;
    		public int getColumnCount( ){ return NUMCOLUMN+2;}

    		public String getColumnName(int col) {
    			if (col==VALUECOL) return "";  // no heading on "Set"
    			if (col==SETCOL) return "";    // no heading on "Edit"
    			if (col==ENABLECOL) return "Enabled";
    			else return super.getColumnName(col);
		    }
    		public Class getColumnClass(int col) {
    			if (col==SETCOL) return JButton.class;
    			if (col==ENABLECOL) return Boolean.class;
    			else return super.getColumnClass(col);
		    }
    		public int getPreferredWidth(int col) {
    			if (col==SETCOL) return new JTextField(6).getPreferredSize().width;
    			if (col==ENABLECOL) return new JTextField(6).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }
    		public boolean isCellEditable(int row, int col) {
    			if (col==SETCOL) return true;
    			if (col==ENABLECOL) return true;
    			else return super.isCellEditable(row,col);
			}    		
    		public Object getValueAt(int row, int col) {
    			if (col==SETCOL) {
    				return "Edit";
    			}
    			else if (col==ENABLECOL) {
    				return new Boolean(((Route)getBySystemName((String)getValueAt(row, SYSNAMECOL))).getEnabled());
    			}
				else return super.getValueAt(row, col);
			}    		
    		public void setValueAt(Object value, int row, int col) {
    			if (col==SETCOL) {
                    // set up to edit
                    addPressed(null);
                    name.setText((String)getValueAt(row, SYSNAMECOL));
                    editPressed(null); // don't really want to stop Route w/o user action
    			}
    			else if (col==ENABLECOL) {
                    // alternate
                    Route r = (Route)getBySystemName((String)getValueAt(row, SYSNAMECOL));
                    boolean v = r.getEnabled();
                    r.setEnabled(!v);
    			}
    			else super.setValueAt(value, row, col);
    		}
    		// want to update when enabled parameter changes
            boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Enabled")) return true;
                else return super.matchPropertyName(e);
            }

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

	String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
	String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
	String setStateClosed = "Set "+stateClosed;
	String setStateThrown = "Set "+stateThrown;
	String setStateActive = "Set "+"Active";
	String setStateInactive = "Set "+"Inactive";
	String setStateToggle = "Toggle";

    String[] sensorInputModes = new String[]{"On Active", "On Inactive", "On Change", "Veto Active", "Veto Inactive"};
    int[] sensorInputModeValues = new int[]{Route.ONACTIVE, Route.ONINACTIVE, Route.ONCHANGE, Route.VETOACTIVE, Route.VETOINACTIVE};
        
    String[] turnoutInputModes = new String[]{"On "+stateClosed, "On "+stateThrown, "On Change", "Veto Closed", "Veto Thrown"};
    int[] turnoutInputModeValues = new int[]{Route.ONCLOSED, Route.ONTHROWN, Route.ONCHANGE, Route.VETOCLOSED, Route.VETOTHROWN};

    int sensorModeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, sensorInputModeValues, sensorInputModes);
        
        if (result<0) {
            log.warn("unexpected mode string in sensorMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
 
    void setSensorModeBox(int mode, JComboBox box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, sensorInputModeValues, sensorInputModes);
        box.setSelectedItem(result);
    }
       
    int turnoutModeFromBox(JComboBox box) {
        String mode = (String)box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutInputModeValues, turnoutInputModes);
        
        if (result<0) {
            log.warn("unexpected mode string in turnoutMode: "+mode);
            throw new IllegalArgumentException();
        }
        return result;
    }
 
    void setTurnoutModeBox(int mode, JComboBox box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, turnoutInputModeValues, turnoutInputModes);
        box.setSelectedItem(result);
    }
       
    JTextField name = new JTextField(10);
    JTextField userName = new JTextField(22);

    JFrame addFrame = null;
    RouteTurnoutModel routeTurnoutModel = null;
    RouteSensorModel routeSensorModel = null;

    JTextField soundFile = new JTextField(20);
    JTextField scriptFile = new JTextField(20);

    JTextField sensor1 = new JTextField(8);
    JComboBox  sensor1mode = new JComboBox(sensorInputModes);
    JTextField sensor2 = new JTextField(8);
    JComboBox  sensor2mode = new JComboBox(sensorInputModes);
    JTextField sensor3 = new JTextField(8);
    JComboBox  sensor3mode = new JComboBox(sensorInputModes);
    JTextField cTurnout = new JTextField(8);
	JTextField timeDelay = new JTextField(5);

    JComboBox cTurnoutStateBox = new JComboBox(turnoutInputModes);
    
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
    
    JPanel p2xt = null;   // Turnout list table
    JPanel p2xs = null;   // Sensor list table

    Route curRoute = null;
    boolean routeDirty = false;  // true to fire reminder to save work
    boolean editMode = false;

    void addPressed(ActionEvent e) {
		if (editMode) {
			cancelEdit();
		}
        // Initialize the turnout list
        for (int i = 0; i<MAX_TURNOUTS ; i++ ) {
            includeTurnout[i] = false;
            includeSensor[i] = false;
			setTurnoutState[i] = setStateClosed;
			setSensorState[i] = setStateInactive;
            includedTurnoutPosition[i] = 0;
            includedSensorPosition[i] = 0;
        }
        turnoutSysNameList = InstanceManager.turnoutManagerInstance().getSystemNameList();
        numTurnouts = turnoutSysNameList.size();
        if (numTurnouts > MAX_TURNOUTS) {
            log.warn("turnout list too long "+numTurnouts);
            numTurnouts = MAX_TURNOUTS;
        }
        sensorSysNameList = InstanceManager.sensorManagerInstance().getSystemNameList();
        numSensors = sensorSysNameList.size();
        if (numSensors > MAX_SENSORS) {
            log.warn("sensor list too long "+numSensors);
            numSensors = MAX_SENSORS;
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
                            routeTurnoutModel.fireTableDataChanged();
                            routeSensorModel.fireTableDataChanged();
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
                            routeTurnoutModel.fireTableDataChanged();
                            routeSensorModel.fireTableDataChanged();
                        }
                    }
                });
            py.add(new JLabel("  Turnouts and Sensors"));
            contentPane.add(py);

            // add turnout table
            p2xt = new JPanel();
            JPanel p2xtSpace = new JPanel();
            p2xtSpace.setLayout(new BoxLayout(p2xtSpace, BoxLayout.Y_AXIS));
            p2xtSpace.add(new JLabel("XXX"));
            p2xt.add(p2xtSpace);
            
            JPanel p21t = new JPanel();
            p21t.setLayout(new BoxLayout(p21t, BoxLayout.Y_AXIS));
            p21t.add(new JLabel("Please select "));
            p21t.add(new JLabel(" Turnouts to "));
            p21t.add(new JLabel(" be included "));
            p21t.add(new JLabel(" in this Route."));
            p2xt.add(p21t);
            routeTurnoutModel = new RouteTurnoutModel();
            JTable routeTurnoutTable = jmri.util.JTableUtil.sortableDataModel(routeTurnoutModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)routeTurnoutTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(RouteTurnoutModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {}  // if not a sortable table model
            routeTurnoutTable.setRowSelectionAllowed(false);
            routeTurnoutTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(480,100));
            JComboBox stateTCombo = new JComboBox();
   			stateTCombo.addItem(setStateClosed);
			stateTCombo.addItem(setStateThrown);
			stateTCombo.addItem(setStateToggle);
            TableColumnModel routeTurnoutColumnModel = routeTurnoutTable.getColumnModel();
            TableColumn includeColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.INCLUDE_COLUMN);
            includeColumnT.setResizable(false);
            includeColumnT.setMinWidth(50);
            includeColumnT.setMaxWidth(60);
            TableColumn sNameColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.SNAME_COLUMN);
            sNameColumnT.setResizable(true);
            sNameColumnT.setMinWidth(75);
            sNameColumnT.setMaxWidth(95);
            TableColumn uNameColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.UNAME_COLUMN);
            uNameColumnT.setResizable(true);
            uNameColumnT.setMinWidth(210);
            uNameColumnT.setMaxWidth(260);
            TableColumn stateColumnT = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.STATE_COLUMN);
            stateColumnT.setCellEditor(new DefaultCellEditor(stateTCombo));
            stateColumnT.setResizable(false);
            stateColumnT.setMinWidth(90);
            stateColumnT.setMaxWidth(100);
            JScrollPane routeTurnoutScrollPane = new JScrollPane(routeTurnoutTable);
            p2xt.add(routeTurnoutScrollPane,BorderLayout.CENTER);
            contentPane.add(p2xt);
            p2xt.setVisible(true);
 
             // add sensor table
            p2xs = new JPanel();
            JPanel p2xsSpace = new JPanel();
            p2xsSpace.setLayout(new BoxLayout(p2xsSpace, BoxLayout.Y_AXIS));
            p2xsSpace.add(new JLabel("XXX"));
            p2xs.add(p2xsSpace);
            
            JPanel p21s = new JPanel();
            p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
            p21s.add(new JLabel("Please select "));
            p21s.add(new JLabel(" Sensors to "));
            p21s.add(new JLabel(" be included "));
            p21s.add(new JLabel(" in this Route."));
            p2xs.add(p21s);
            routeSensorModel = new RouteSensorModel();
            JTable routeSensorTable = jmri.util.JTableUtil.sortableDataModel(routeSensorModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)routeSensorTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(RouteSensorModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {}  // if not a sortable table model
            routeSensorTable.setRowSelectionAllowed(false);
            routeSensorTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(480,100));
            JComboBox stateSCombo = new JComboBox();
   			stateSCombo.addItem(setStateActive);
			stateSCombo.addItem(setStateInactive);
			stateSCombo.addItem(setStateToggle);
            TableColumnModel routeSensorColumnModel = routeSensorTable.getColumnModel();
            TableColumn includeColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.INCLUDE_COLUMN);
            includeColumnS.setResizable(false);
            includeColumnS.setMinWidth(50);
            includeColumnS.setMaxWidth(60);
            TableColumn sNameColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.SNAME_COLUMN);
            sNameColumnS.setResizable(true);
            sNameColumnS.setMinWidth(75);
            sNameColumnS.setMaxWidth(95);
            TableColumn uNameColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.UNAME_COLUMN);
            uNameColumnS.setResizable(true);
            uNameColumnS.setMinWidth(210);
            uNameColumnS.setMaxWidth(260);
            TableColumn stateColumnS = routeSensorColumnModel.
                                                getColumn(RouteSensorModel.STATE_COLUMN);
            stateColumnS.setCellEditor(new DefaultCellEditor(stateSCombo));
            stateColumnS.setResizable(false);
            stateColumnS.setMinWidth(90);
            stateColumnS.setMaxWidth(100);
            JScrollPane routeSensorScrollPane = new JScrollPane(routeSensorTable);
            p2xs.add(routeSensorScrollPane,BorderLayout.CENTER);
            contentPane.add(p2xs);
            p2xs.setVisible(true);

            // Enter filenames for sound, script
            
            JPanel p25 = new JPanel();
            p25.setLayout(new FlowLayout());
            p25.add(new JLabel("Play sound file:"));
            p25.add(new JButton("Set"));
            p25.add(soundFile);
            contentPane.add(p25);
            
            JPanel p26 = new JPanel();
            p26.setLayout(new FlowLayout());
            p26.add(new JLabel("Run script:"));
            p26.add(new JButton("Set"));
            p26.add(scriptFile);
            contentPane.add(p26);
            
           
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
            p34.add(new JLabel("   Condition: "));
            cTurnoutStateBox.setToolTipText("Setting control Turnout to selected state will trigger Route.");
            p34.add(cTurnoutStateBox);
            p3.add(p34);
			// add added delay
            JPanel p35 = new JPanel();
            p35.add(new JLabel("Enter added delay between Turnout Commands (optional)"));
            p3.add(p35);
            JPanel p36 = new JPanel();
            p36.add(new JLabel("Added delay: "));
            p36.add(timeDelay);
            timeDelay.setText("0");
            timeDelay.setToolTipText("Enter time to add to the default of 250 milliseconds between turnout commands.");
            p36.add(new JLabel(" (milliseconds) "));
            p3.add(p36);
			// complete this panel
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
            delete.setVisible(false);
            contentPane.add(pb);
            // pack and release space
            addFrame.pack();
            p2xsSpace.setVisible(false);
            p2xtSpace.setVisible(false);
        }
        // set listener for window closing
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    // remind to save, if Route was created or edited
                    if (routeDirty) {
                        javax.swing.JOptionPane.showMessageDialog(addFrame,
                            "Remember to save your Route information.","Reminder",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        routeDirty = false;
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
        addFrame.setVisible(true);
        routeTurnoutModel.fireTableDataChanged();
        routeSensorModel.fireTableDataChanged();
    }

    /**
     * Initialize list of included turnout positions
     */
    void initializeIncludedList() {
        numIncludedTurnouts = 0;
        if (numTurnouts > 0) {
            for (int i = 0; i<numTurnouts; i++) {
                if (includeTurnout[i]) {
                    includedTurnoutPosition[numIncludedTurnouts] = i;
                    numIncludedTurnouts ++;
                }
            }
        }
        numIncludedSensors = 0;
        if (numSensors > 0) {
            for (int i = 0; i<numSensors; i++) {
                if (includeSensor[i]) {
                    includedSensorPosition[numIncludedSensors] = i;
                    numIncludedSensors++;
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
        int numIncludedT = setTurnoutInformation(g);
        // Set Sensor information 
        int numIncludedS = setSensorInformation(g);
        // save script names
        g.setOutputScriptName(scriptFile.getText());
        g.setOutputSoundName(soundFile.getText());
        
        // Set optional control Sensor and control Turnout information
        setControlInformation(g);
        // Provide feedback to user
        status1.setText("New Route created: "+sName+", "+uName+", "+
                                                        numIncludedT+" Turnouts, "+numIncludedS+" Sensors");
        status2.setText(editInst);
        // activate the route
        g.activateRoute();
        
        // mark as dirty so save prompt displayed
        routeDirty = true;
    }

    /**
     * Sets the Turnout information for adding or editting
     */
    int setTurnoutInformation(Route g) {
        int numIncluded = 0;
        int state = 0;
        for (int i = 0; i<numTurnouts; i++) {
            if (includeTurnout[i]) {
                if (setTurnoutState[i].equals(setStateClosed) ) {
                    state = Turnout.CLOSED;
                }
                else if (setTurnoutState[i].equals(setStateThrown) ) {
                    state = Turnout.THROWN;
                }
				else {
					state = Route.TOGGLE;
				}
                g.addOutputTurnout((String)turnoutSysNameList.get(i),state);
                numIncluded ++;
            }
        }
        return numIncluded;
    }

    /**
     * Sets the Sensor information for adding or editting
     */
    int setSensorInformation(Route g) {
        int numIncluded = 0;
        int state = 0;
        for (int i = 0; i<numSensors; i++) {
            if (includeSensor[i]) {
                if (setSensorState[i].equals(setStateInactive) ) {
                    state = Sensor.INACTIVE;
                }
                else if (setSensorState[i].equals(setStateActive) ) {
                    state = Sensor.ACTIVE;
                }
				else {
					state = Route.TOGGLE;
				}
                g.addOutputSensor((String)sensorSysNameList.get(i),state);
                numIncluded ++;
            }
        }
        return numIncluded;
    }

    /**
     * Sets the Sensor, Turnout, and delay control information for adding or editting if any
     */
    void setControlInformation(Route g) {
        // Get sensor control information if any
        String sensorSystemName = sensor1.getText();
        if (sensorSystemName.length() > 0) {
            Sensor s1 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s1==null) || (!g.addSensorToRoute(sensorSystemName, sensorModeFromBox(sensor1mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        sensorSystemName = sensor2.getText();
        if (sensorSystemName.length() > 0) {
            Sensor s2 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s2==null) || (!g.addSensorToRoute(sensorSystemName, sensorModeFromBox(sensor2mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        sensorSystemName = sensor3.getText();
        if (sensorSystemName.length() > 0) {
            Sensor s3 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if ( (s3==null) || (!g.addSensorToRoute(sensorSystemName, sensorModeFromBox(sensor3mode))) ) {
                log.error("Unexpected failure to add Sensor '"+sensorSystemName+
                                            "' to Route '"+g.getSystemName()+"'.");
            }
        }
        // Set turnout information if there is any
        String turnoutSystemName = cTurnout.getText();
        if (turnoutSystemName.length() > 0) {
            Turnout t = InstanceManager.turnoutManagerInstance().
                                    provideTurnout(turnoutSystemName);
            if (t!=null) {
                g.setControlTurnout(turnoutSystemName);
                // set up control turnout state
                g.setControlTurnoutState(turnoutModeFromBox(cTurnoutStateBox));
            }
            else {
                g.setControlTurnout("");
                log.error("Unexpected failure to add control Turnout '"+
                        turnoutSystemName+"' to Route '"+g.getSystemName()+"'.");
            }
        } else {
            // No control Turnout was entered
            g.setControlTurnout("");
        }
		// set delay information
		int addDelay = 0;
		try 
		{
			addDelay = Integer.parseInt(timeDelay.getText());
		}
		catch (Exception e)
		{
			addDelay = 0;
			timeDelay.setText("0");
		}
		if (addDelay<0) 
		{
			// added delay must be a positive integer
			addDelay = 0;
			timeDelay.setText("0");
		}
		g.setRouteCommandDelay(addDelay);		
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
                if (g.isOutputTurnoutIncluded(tSysName)) {
                    includeTurnout[i] = true;
                    tState = g.getOutputTurnoutSetState(tSysName);
					if (tState==Turnout.CLOSED) {
						setTurnoutState[i] = setStateClosed;
					}
                    else if (tState==Turnout.THROWN) {
                        setTurnoutState[i] = setStateThrown;
                    }
					else if (tState==Route.TOGGLE) {
						setTurnoutState[i] = setStateToggle;
					}
					else {
						setTurnoutState[i] = setStateClosed;
						log.error ("Unrecognized set state for Turnout " +
										tSysName + " in Route " + sName);
					}
                }
                else {
                    includeTurnout[i] = false;
					setTurnoutState[i] = setStateClosed;
                }
            }
        }
        // set up Sensor list for this route
        if (numSensors>0) {
            int sState = 0;
            for (int i=0; i<numSensors; i++) {
                String tSysName = (String)sensorSysNameList.get(i);
                if (g.isOutputSensorIncluded(tSysName)) {
                    includeSensor[i] = true;
                    sState = g.getOutputSensorSetState(tSysName);
					if (sState==Sensor.ACTIVE) {
						setSensorState[i] = setStateActive;
					}
                    else if (sState==Sensor.INACTIVE) {
                        setSensorState[i] = setStateInactive;
                    }
					else if (sState==Route.TOGGLE) {
						setSensorState[i] = setStateToggle;
					}
					else {
						setSensorState[i] = setStateInactive;
						log.error ("Unrecognized set state for Sensor " +
										tSysName + " in Route " + sName);
					}
                }
                else {
                    includeSensor[i] = false;
					setSensorState[i] = setStateInactive;
                }
            }
        }
        // get sound, script names
        scriptFile.setText(g.getOutputScriptName());
        soundFile.setText(g.getOutputSoundName());
        

        // set up Sensors if there are any
        String[] temNames = new String[Route.MAX_CONTROL_SENSORS];
        int[] temModes = new int[Route.MAX_CONTROL_SENSORS];
        for (int k = 0; k<Route.MAX_CONTROL_SENSORS; k++) {
            temNames[k] = g.getRouteSensorName(k);
            temModes[k] = g.getRouteSensorMode(k);
        }
        sensor1.setText(temNames[0]);
        setSensorModeBox(temModes[0], sensor1mode);

        sensor2.setText(temNames[1]);
        setSensorModeBox(temModes[1], sensor2mode);

        sensor3.setText(temNames[2]);
        setSensorModeBox(temModes[2], sensor3mode);

        // set up control Turnout if there is one
        cTurnout.setText(g.getControlTurnout()); 
        
        setTurnoutModeBox(g.getControlTurnoutState(), cTurnoutStateBox);
        
		// set up additional delay
		timeDelay.setText(Integer.toString(g.getRouteCommandDelay()));
        // begin with showing all Turnouts   
        cancelIncludedOnly();
        // set up buttons and notes
        status1.setText(updateInst);
        status2.setText(cancelInst);
        status2.setVisible(true);
        delete.setVisible(true);
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
        // route is already deactivated, just delete it
        InstanceManager.routeManagerInstance().deleteRoute(curRoute);
        // switch GUI back to selection mode
        status2.setText(editInst);
        status2.setVisible(true);
        delete.setVisible(false);
        cancel.setVisible(false);
        update.setVisible(false);
        edit.setVisible(true);
        create.setVisible(true);
        fixedSystemName.setVisible(false);
        name.setVisible(true);
        routeDirty = true;  // remind to save
        // get out of edit mode
        editMode = false;
        curRoute = null;
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
        // clear the current output information for this Route
        g.clearOutputTurnouts();
        g.clearOutputSensors();
        // add those indicated in the window
        int numTurnoutIncluded = setTurnoutInformation(g);
        int numSensorIncluded = setSensorInformation(g);
        // set the current values of the filenames
        g.setOutputScriptName(scriptFile.getText());
        g.setOutputSoundName(soundFile.getText());

        // clear the current Sensor information for this Route
        g.clearRouteSensors();
        // add control Sensors and a control Turnout if entered in the window
        setControlInformation(g);
        // move to show all turnouts if not there
        cancelIncludedOnly();
        // Provide feedback to user
        status1.setText("Route updated: "+g.getSystemName()+", "+uName+", "+
                                                            numTurnoutIncluded+" Turnouts, "+
                                                            numSensorIncluded+" Sensors");
        // set up buttons and notes
        status2.setText(editInst);
        status2.setVisible(true);
        delete.setVisible(false);
        cancel.setVisible(false);
        update.setVisible(false);
        edit.setVisible(true);
        create.setVisible(true);
        fixedSystemName.setVisible(false);
        name.setVisible(true);
        // reactivate the Route
        curRoute.activateRoute();    
        routeDirty = true;
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
            delete.setVisible(false);
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
     * Base table model for selecting outputs
     */
    public abstract class RouteOutputModel extends AbstractTableModel {
        public Class getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }

        public int getColumnCount () {return 4;}

        public boolean isCellEditable(int r,int c) {
            return ( (c==INCLUDE_COLUMN) || (c==STATE_COLUMN) );
        }
		
        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;

    }

    /**
     * Table model for selecting Turnouts and Turnout State
     */
    public class RouteTurnoutModel extends RouteOutputModel
    {
        public String getColumnName(int c) {return routeTurnoutColumnNames[c];}
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
                rx = includedTurnoutPosition[r];
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
                return setTurnoutState[rx];
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
                rx = includedTurnoutPosition[r];
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
                    setTurnoutState[rx] = (String)type;
                    break;
            }
        }
    }

    /**
     * Set up table for selecting Sensors and Sensor State
     */
    public class RouteSensorModel extends RouteOutputModel
    {
        public String getColumnName(int c) {return routeSensorColumnNames[c];}

        public int getRowCount () {
            if (showAll)
                return numSensors;
            else
                return numIncludedSensors;
        }

        public Object getValueAt (int r,int c) {
            int rx = r;
            if (rx > numSensors) {
                return null;
            }
            if (!showAll) {
                rx = includedSensorPosition[r];
            }
            switch (c) {
            case INCLUDE_COLUMN:
                if (includeSensor[rx]) {
                    return Boolean.TRUE;
                }
                else {
                    return Boolean.FALSE;
                }
            case SNAME_COLUMN:  // slot number
                return sensorSysNameList.get(rx);
            case UNAME_COLUMN:  //
                return InstanceManager.sensorManagerInstance().
                        getBySystemName((String)sensorSysNameList.get(rx)).getUserName();
            case STATE_COLUMN:  //
                return setSensorState[rx];
            default:
                return null;
            }
        }

        public void setValueAt(Object type,int r,int c) {
            int rx = r;
            if (rx > numSensors) {
                return;
            }
            if (!showAll) {
                rx = includedSensorPosition[r];
            }
            switch (c) {
                case INCLUDE_COLUMN:  
					if (!((Boolean)type).booleanValue()) {
                        includeSensor[rx] = false;
                    }
                    else {
                        includeSensor[rx] = true;
                    }
                    break;
                case STATE_COLUMN: 
                    setSensorState[rx] = (String)type;
                    break;
            }
        }
    }

    private boolean showAll = true;   // false indicates show only included Turnouts

    private int numIncludedTurnouts = 0;
    private int numTurnouts = 0;
    private static final int MAX_TURNOUTS = 1000;
    private String[] routeTurnoutColumnNames = {"System Name","User Name",
                                        "Include", "Set State"};
    private List turnoutSysNameList = null;
    private boolean[] includeTurnout = new boolean[MAX_TURNOUTS];
    private String[] setTurnoutState = new String[MAX_TURNOUTS];
    private int[] includedTurnoutPosition = new int[MAX_TURNOUTS];  // indexed by row of included turnout in included
                                                            // Turnouts only list.  Contains position in full
                                                            // Turnout list.

    private int numIncludedSensors = 0;
    private int numSensors = 0;
    private static final int MAX_SENSORS= 1000;
    private String[] routeSensorColumnNames = {"System Name","User Name",
                                        "Include", "Set State"};
    private List sensorSysNameList = null;
    private boolean[] includeSensor = new boolean[MAX_SENSORS];
    private String[] setSensorState = new String[MAX_SENSORS];
    private int[] includedSensorPosition = new int[MAX_SENSORS];  // indexed by row of included sensor in included
                                                            // Sensors only list.  Contains position in full
                                                            // Sensors list.

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RouteTableAction.class.getName());
}
/* @(#)RouteTableAction.java */
