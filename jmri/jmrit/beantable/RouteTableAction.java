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

/**
 * Swing action to create and register a Route Table
 
 * Based in part on SignalHeadTableAction.java by Bob Jacobson
 *
 * @author	Dave Duchamp    Copyright (C) 2004
 * @version     $Revision: 1.1 $
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
                return new JButton("Set");
            }
        };
    }

    void setTitle() {
        f.setTitle("Route Table");
    }

    JFrame addFrame = null;
    JTextField name = new JTextField(10);
    JTextField userName = new JTextField(10);
    JTextField sensor1 = new JTextField(8);
    JTextField sensor2 = new JTextField(8);
    JTextField sensor3 = new JTextField(8);
    
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

    Route curRoute = null;
    boolean routeCreated = false;
    boolean editMode = false;

    void addPressed(ActionEvent e) {
        // Initialize the turnout list
        for (int i = 0; i<MAX_TURNOUTS ; i++ ) {
            includeTurnout[i] = false;
            setState[i] = "CLOSED";
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
            ps.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            contentPane.add(ps);
            // add user name
            JPanel p = new JPanel(); 
            p.setLayout(new FlowLayout());
            p.add(userLabel);
            p.add(userName);
            contentPane.add(p);
            // add turnout table
            JPanel p2x = new JPanel();
            JPanel p21 = new JPanel();
            p21.setLayout(new BoxLayout(p21, BoxLayout.Y_AXIS));
            p21.add(new JLabel("Please select "));
            p21.add(new JLabel(" Turnouts to "));
            p21.add(new JLabel(" be included "));
            p21.add(new JLabel(" in this Route."));
            p2x.add(p21);
            TableModel routeTurnoutModel = new RouteTurnoutModel();
            JTable routeTurnoutTable = new JTable(routeTurnoutModel);
            routeTurnoutTable.setRowSelectionAllowed(false);
            routeTurnoutTable.setPreferredScrollableViewportSize(new 
                                                            java.awt.Dimension(330,100));
            JComboBox stateCombo = new JComboBox();
            stateCombo.addItem("CLOSED");
            stateCombo.addItem("THROWN");
            TableColumnModel routeTurnoutColumnModel = routeTurnoutTable.getColumnModel();
            TableColumn includeColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.INCLUDE_COLUMN);
            includeColumn.setMinWidth(70);
            includeColumn.setMaxWidth(80);
            TableColumn sNameColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.SNAME_COLUMN);
            sNameColumn.setMinWidth(70);
            sNameColumn.setMaxWidth(80);
            TableColumn uNameColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.UNAME_COLUMN);
            uNameColumn.setMinWidth(100);
            uNameColumn.setMaxWidth(110);
            TableColumn stateColumn = routeTurnoutColumnModel.
                                                getColumn(RouteTurnoutModel.STATE_COLUMN);
            stateColumn.setCellEditor(new DefaultCellEditor(stateCombo));
            stateColumn.setResizable(false);
            stateColumn.setMinWidth(70);
            stateColumn.setMaxWidth(80);
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
            p32.add(sensor1);
            p32.add(sensor2);
            p32.add(sensor3);
            sensor1.setText("");
            sensor2.setText("");
            sensor3.setText("");
            p3.add(p32);
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
        addFrame.pack();
        addFrame.show();
    }

    /**
     * Responds to the Add button
     */
    void createPressed(ActionEvent e) {
        // Get system name and user name
        String sName = name.getText();
        String uName = userName.getText();
        // check if a Route with this system name already exists
        Route g = jmri.InstanceManager.routeManagerInstance().getBySystemName(sName);
        if (g!=null) {
            // Route already exists
            status1.setText("Error - Route with this system name already exists.");
            return;
        }
        // check if a Route with the same user name exists
        g = jmri.InstanceManager.routeManagerInstance().getByUserName(uName);
        if (g!=null) {
            // Route with this user name already exists
            status1.setText("Error - Route with this user name already exists.");
            return;
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
        // Set optional Sensor information
        setSensorInformation(g);
        // Provide feedback to user
        status1.setText("New Route created: "+sName+", "+uName+", "+
                                                        numIncluded+" Turnouts");
        status2.setText(editInst);
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
                if (setState[i].equals("CLOSED") ) {
                    state = Turnout.CLOSED;
                }
                else {
                    state = Turnout.THROWN;
                }
                g.addTurnoutToRoute((String)turnoutSysNameList.get(i),state);
                numIncluded ++;
            }
        }
        return numIncluded;
    }

    /**
     * Sets the Sensor information for adding or editting if there is any
     */
    void setSensorInformation(Route g) {
        // Get sensor control information
        String sensorSystemName = sensor1.getText();
        if (sensorSystemName.length() > 2) {
            Sensor s1 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if (s1!=null) {
                if (!g.addSensorToRoute(s1.getSystemName())) {
                    log.error("Unexpected failure to add Sensor '"+s1.getSystemName()+
                                            "' to Route '"+g.getSystemName()+"'.");
                }
            }
        }
        sensorSystemName = sensor2.getText();
        if (sensorSystemName.length() > 2) {
            Sensor s2 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if (s2!=null) {
                if (!g.addSensorToRoute(s2.getSystemName())) {
                    log.error("Unexpected failure to add Sensor '"+s2.getSystemName()+
                                            "' to Route '"+g.getSystemName()+"'.");
                }
            }
        }
        sensorSystemName = sensor3.getText();
        if (sensorSystemName.length() > 2) {
            Sensor s3 = InstanceManager.sensorManagerInstance().
                            provideSensor(sensorSystemName);
            if (s3!=null) {
                if (!g.addSensorToRoute(s3.getSystemName())) {
                    log.error("Unexpected failure to add Sensor '"+s3.getSystemName()+
                                            "' to Route '"+g.getSystemName()+"'.");
                }
            }
        }
    }
        
    /**
     * Responds to the Edit button
     */
    void editPressed(ActionEvent e) {
        // identify the Route with this name if it already exists
        String sName = name.getText();
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
                    setState[i] = "CLOSED";
                    if (tState==Turnout.THROWN) {
                        setState[i] = "THROWN";
                    }
                }
                else {
                    includeTurnout[i] = false;
                }
            }
        }
        // set up Sensors if there are any
        String[] temNames = new String[Route.MAX_CONTROL_SENSORS];
        for (int k = 0; k<Route.MAX_CONTROL_SENSORS; k++) {
            temNames[k] = g.getRouteSensor(k);
        }
        sensor1.setText(temNames[0]);
        sensor2.setText(temNames[1]);
        sensor3.setText(temNames[2]);    
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
        // reactivate the Route
        curRoute.deActivateRoute();
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
        if ( !(uName.equals(g.getUserName())) ) {
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
        // add those Sensors entered in the window if any
        setSensorInformation(g);
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
        public int getRowCount () {return numTurnouts;}
        public Object getValueAt (int r,int c) {
            switch (c) {
            case INCLUDE_COLUMN:
                if (includeTurnout[r]) {
                    return Boolean.TRUE;
                }
                else {
                    return Boolean.FALSE;
                }
            case SNAME_COLUMN:  // slot number
                return turnoutSysNameList.get(r);
            case UNAME_COLUMN:  //
                return InstanceManager.turnoutManagerInstance().
                        getBySystemName((String)turnoutSysNameList.get(r)).getUserName();
            case STATE_COLUMN:  //
                return setState[r];
            default:
                return null;
            }
        }
        public void setValueAt(Object type,int r,int c) {
            switch (c) {
                case INCLUDE_COLUMN:  
                    if (type == Boolean.FALSE) {
                        includeTurnout[r] = false;
                    }
                    else {
                        includeTurnout[r] = true;
                    }
                    break;
                case STATE_COLUMN: 
                    setState[r] = (String)type;
                    break;
            }
        }
        public boolean isCellEditable(int r,int c) {
            return ( (c==INCLUDE_COLUMN) || (c==STATE_COLUMN) );
        }
		
        public static final int INCLUDE_COLUMN = 0;
        public static final int SNAME_COLUMN = 1;
        public static final int UNAME_COLUMN = 2;
        public static final int STATE_COLUMN = 3;
    }
    private int numTurnouts = 0;
    private static final int MAX_TURNOUTS = 200;
    private String[] routeTurnoutColumnNames = {"Include","System Name","User Name",
                                        "Set State"};
    private List turnoutSysNameList = null;
    private boolean[] includeTurnout = new boolean[MAX_TURNOUTS];
    private String[] setState = new String[MAX_TURNOUTS];

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RouteTableAction.class.getName());
}
/* @(#)RouteTableAction.java */
