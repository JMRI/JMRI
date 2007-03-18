// SensorGroupFrame.java

package jmri.jmrit.sensorgroup;

import jmri.*;
import jmri.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import com.sun.java.util.collections.List;

/**
 * User interface for sending DCC packets.
 * <P>
 * This was originally made from jmrix.loconet.logogen, but note that
 * the logic is somewhat different here.  The LocoNet version waited for
 * the sent (LocoNet) packet to be echo'd, while this starts the timeout
 * immediately.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2003
 * @version			$Revision: 1.3 $
 */
public class SensorGroupFrame extends jmri.util.JmriJFrame {

    public SensorGroupFrame() {
    }

    private final static String namePrefix  = "SENSOR GROUP:";  // should be upper case
    private final static String nameDivider = ":";
    
    SensorTableModel sensorModel;
    JTextField name;
    JButton viewButton;
    JButton addButton;
    
    private static final int MAXSENSOR = 1000;
    Boolean[] includedSensors = new Boolean[MAXSENSOR];
    
    public void initComponents() throws Exception {
        addHelpMenu("package.jmri.jmrit.sensorgroup.SensorGroupFrame", true);
        
        for (int i = 0; i<MAXSENSOR; i++)
            includedSensors[i] = Boolean.FALSE;
                    
        setTitle("Define Sensor Group");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add the sensor table
        JPanel p2xs = new JPanel();
        
        JPanel p21s = new JPanel();
        p21s.setLayout(new BoxLayout(p21s, BoxLayout.Y_AXIS));
        p21s.add(new JLabel("Please select "));
        p21s.add(new JLabel(" Sensors to "));
        p21s.add(new JLabel(" be included "));
        p21s.add(new JLabel(" in this group."));
        p2xs.add(p21s);
        sensorModel = new SensorTableModel(){
            public Object getValueAt (int r,int c) {
                if (c == INCLUDE_COLUMN)
                    return includedSensors[r];
                else
                    return super.getValueAt(r,c);
            }
        
            public void setValueAt(Object type,int r,int c) {
                if (c == INCLUDE_COLUMN)
                    includedSensors[r] = (Boolean)type;
                else
                    super.setValueAt(type,r,c);
            }
        };
        JTable sensorTable = jmri.util.JTableUtil.sortableDataModel(sensorModel);
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)sensorTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(SensorTableModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {}  // if not a sortable table model
        sensorTable.setRowSelectionAllowed(false);
        sensorTable.setPreferredScrollableViewportSize(new 
                                                        java.awt.Dimension(480,100));
        TableColumnModel sensorColumnModel = sensorTable.getColumnModel();
        TableColumn includeColumnS = sensorColumnModel.
                                            getColumn(SensorTableModel.INCLUDE_COLUMN);
        includeColumnS.setResizable(false);
        includeColumnS.setMinWidth(50);
        includeColumnS.setMaxWidth(60);
        TableColumn sNameColumnS = sensorColumnModel.
                                            getColumn(SensorTableModel.SNAME_COLUMN);
        sNameColumnS.setResizable(true);
        sNameColumnS.setMinWidth(75);
        sNameColumnS.setMaxWidth(95);
        TableColumn uNameColumnS = sensorColumnModel.
                                            getColumn(SensorTableModel.UNAME_COLUMN);
        uNameColumnS.setResizable(true);
        uNameColumnS.setMinWidth(210);
        uNameColumnS.setMaxWidth(260);

        JScrollPane sensorScrollPane = new JScrollPane(sensorTable);
        p2xs.add(sensorScrollPane,BorderLayout.CENTER);
        getContentPane().add(p2xs);
        p2xs.setVisible(true);

        // add name field
        JPanel p3 = new JPanel();
        p3.add(new JLabel("Group Name:"));
        name = new JTextField(12);
        p3.add(name);
        getContentPane().add(p3);
        
        // button
        JPanel p4 = new JPanel();
        viewButton = new JButton(" View ");
        viewButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    viewPressed();
                }
            });
        p4.add(viewButton);
        addButton = new JButton(" Add/Update ");
        addButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    addPressed();
                }
            });
        p4.add(addButton);
        getContentPane().add(p4);
        
        // pack to cause display
        pack();
    }

    void addPressed() {
        log.debug("start with "+sensorModel.getRowCount()+" lines");
        RouteManager rm = InstanceManager.routeManagerInstance();
        String group = name.getText().toUpperCase();
        
        // remove the old routes
        List l = rm.getSystemNameList();     
        String prefix = (namePrefix+group+nameDivider).toUpperCase();
        
        for (int i = 0; i<l.size(); i++) {
            String name = (String) l.get(i);
            if (name.startsWith(prefix)) {
                // OK, kill this one
                Route r = rm.getBySystemName((String)l.get(i));
                r.deActivateRoute();
                rm.deleteRoute(r);
            }
        }        

        // add the new routes
        for (int i = 0; i<sensorModel.getRowCount(); i++) {
            if (includedSensors[i].booleanValue()) {
                String sensor = (String)sensorModel.getValueAt(i,sensorModel.SNAME_COLUMN);
                String name = namePrefix+group+nameDivider+sensor;
                Route r = new DefaultRoute(name);
                // add the control sensor
                r.addSensorToRoute(sensor, Route.ONACTIVE);
                // add the output sensors
                for (int j=0; j<sensorModel.getRowCount(); j++) {
                    if (includedSensors[j].booleanValue()) {
                        String outSensor = (String)sensorModel.getValueAt(j,sensorModel.SNAME_COLUMN);
                        int mode = Sensor.INACTIVE;
                        if (i==j) mode = Sensor.ACTIVE;
                        r.addOutputSensor(outSensor, mode);
                    }
                }
                // make it persistant & activate
                r.activateRoute();
                rm.register(r);
            }
        }
    }

    void viewPressed() {
        // find suitable 
        RouteManager rm = InstanceManager.routeManagerInstance();
        String group = name.getText().toUpperCase();
        List l = rm.getSystemNameList();
        String prefix = (namePrefix+group+nameDivider).toUpperCase();
        
        for (int i = 0; i<sensorModel.getRowCount(); i++) includedSensors[i] = Boolean.FALSE;
        
        for (int i = 0; i<l.size(); i++) {
            String name = (String) l.get(i);
            if (name.startsWith(prefix)) {
                String sensor = name.substring(prefix.length());
                // find and check that sensor
                for (int j=0; j<sensorModel.getRowCount(); j++) {
                    if (sensorModel.getValueAt(j,sensorModel.SNAME_COLUMN).equals(sensor))
                        includedSensors[j] = Boolean.TRUE;
                }
            }
        }  
        sensorModel.fireTableDataChanged();      
    }
    
    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }

        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorGroupFrame.class.getName());

}
