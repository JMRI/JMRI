// NodeTableFrame.java

package jmri.jmrix.grapevine.nodetable;

import java.awt.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.Border;

import jmri.jmrix.grapevine.ActiveFlag;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialNode;
import jmri.jmrix.grapevine.SerialSensorManager;

import jmri.jmrix.grapevine.nodeconfig.NodeConfigFrame;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Pane for user management of serial nodes. Contains a table that 
 * does the real work.
 *
 * @author	Bob Jacobsen   Copyright (C) 2004, 2007
 * @author	Dave Duchamp   Copyright (C) 2004, 2006
 * @version	$Revision: 1.1 $
 */
public class NodeTablePane extends javax.swing.JPanel {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.grapevine.nodetable.NodeTableBundle");
		
    /**
     * Constructor method
     */
    public NodeTablePane() {
    	super();
    }

    NodesModel nodesModel = null;
    
    /** 
     *  Initialize the window
     */
    public void initComponents() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        nodesModel = new NodesModel();

        JTable nodesTable = jmri.util.JTableUtil.sortableDataModel(nodesModel);

        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
                nodesTable.setDefaultRenderer(JButton.class,buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
                nodesTable.setDefaultEditor(JButton.class,buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        //nodesTable.setRowHeight(sample.getPreferredSize().height);
        //nodesTable.getColumnModel().getColumn(column)
        //                .setPreferredWidth((sample.getPreferredSize().width)+4);
        
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)nodesTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(nodesModel.STATUSCOL, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {}  // if not a sortable table model
        nodesTable.setRowSelectionAllowed(false);
        nodesTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480,80));

/*             JComboBox stateSCombo = new JComboBox(); */
/*    			stateSCombo.addItem(setStateActive); */
/* 			stateSCombo.addItem(setStateInactive); */
/* 			stateSCombo.addItem(setStateToggle); */
/*             TableColumnModel routeSensorColumnModel = routeSensorTable.getColumnModel(); */
/*             TableColumn includeColumnS = routeSensorColumnModel. */
/*                                                 getColumn(RouteSensorModel.INCLUDE_COLUMN); */
/*             includeColumnS.setResizable(false); */
/*             includeColumnS.setMinWidth(50); */
/*             includeColumnS.setMaxWidth(60); */
/*             TableColumn sNameColumnS = routeSensorColumnModel. */
/*                                                 getColumn(RouteSensorModel.SNAME_COLUMN); */
/*             sNameColumnS.setResizable(true); */
/*             sNameColumnS.setMinWidth(75); */
/*             sNameColumnS.setMaxWidth(95); */
/*             TableColumn uNameColumnS = routeSensorColumnModel. */
/*                                                 getColumn(RouteSensorModel.UNAME_COLUMN); */
/*             uNameColumnS.setResizable(true); */
/*             uNameColumnS.setMinWidth(210); */
/*             uNameColumnS.setMaxWidth(260); */
/*             TableColumn stateColumnS = routeSensorColumnModel. */
/*                                                 getColumn(RouteSensorModel.STATE_COLUMN); */
/*             stateColumnS.setCellEditor(new DefaultCellEditor(stateSCombo)); */
/*             stateColumnS.setResizable(false); */
/*             stateColumnS.setMinWidth(90); */
/*             stateColumnS.setMaxWidth(100); */

        JScrollPane scrollPane = new JScrollPane(nodesTable);
        add(scrollPane);
    }
    
    /**
     * Start the check of the actual hardware
     */
    public void startPoll() {
    }

    /**
     * Set up table for selecting showing nodes.
     *<ol>
     *<li>Address
     *<li>Present Y/N
     *<li>Edit button
     *</ol>
     */
    public class NodesModel extends AbstractTableModel {
        static private final int ADDRCOL = 0;
        static private final int STATUSCOL = 1;
        static private final int EDITCOL = 2;
        
        static private final int LAST = 2;
        
        public int getColumnCount () {return LAST+1;}

        public int getRowCount () {
            return 127;
        }

        public String getColumnName(int c) {
            switch (c) {
            case ADDRCOL:
                return "Address";
            case STATUSCOL:
                return "Status";
            case EDITCOL:
                return "Configure";
            default:
                return "";
            }
        }

        public Class getColumnClass(int c) {
            if (c == EDITCOL) {
                return JButton.class;
            }
            else {
                return String.class;
            }
        }

        public boolean isCellEditable(int r,int c) {
            return (c==EDITCOL);
        }

        public Object getValueAt (int r,int c) {
            // r is row number, r+1 is node number
            switch (c) {
            case ADDRCOL:
                return ""+(r+1);
            case STATUSCOL:
                // see if node exists
                if (SerialTrafficController.instance().getNodeFromAddress(r)!=null)
                    return "Present";
                else
                    return "Unknown";
            case EDITCOL:
                return "Configure";
            default:
                return null;
            }
        }

        public void setValueAt(Object type,int r,int c) {
            switch (c) {
            case EDITCOL:
                NodeConfigFrame f = new NodeConfigFrame();
                f.initComponents();
                f.setNodeAddress(r+1);
                f.setVisible(true);
                return;
            default:
                return;
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NodeTableFrame.class.getName());

}
