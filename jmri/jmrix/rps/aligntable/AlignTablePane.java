// AlignTableFrame.java

package jmri.jmrix.rps.aligntable;

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.Border;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Pane for user management of RPS alignment.
 
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class AlignTablePane extends javax.swing.JPanel {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.aligntable.AlignTableBundle");
		
    /**
     * Constructor method
     */
    public AlignTablePane() {
    	super();
    }

    AlignModel alignModel = null;
    JLabel status;
    
    /** 
     *  Initialize the window
     */
    public void initComponents() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        alignModel = new AlignModel();

        JTable alignTable = jmri.util.JTableUtil.sortableDataModel(alignModel);

        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
                alignTable.setDefaultRenderer(JButton.class,buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
                alignTable.setDefaultEditor(JButton.class,buttonEditor);
        
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)alignTable.getModel());
            tmodel.setSortingStatus(alignModel.NUMCOL, jmri.util.com.sun.TableSorter.DESCENDING);
        } catch (ClassCastException e3) {}  // if not a sortable table model
        alignTable.setRowSelectionAllowed(false);
        alignTable.setPreferredScrollableViewportSize(new java.awt.Dimension(580,80));

        JScrollPane scrollPane = new JScrollPane(alignTable);
        add(scrollPane);
        
        // status info on bottom
        JPanel p = new JPanel() {
            public Dimension getMaximumSize() { 
                int height = getPreferredSize().height;
                int width = super.getMaximumSize().width;
                return new Dimension(width, height); }
        };
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
        JButton b = new JButton(rb.getString("ButtonCheck"));
        b.setToolTipText(rb.getString("TipCheck"));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // startPoll();
            }
        });
        p.add(b);
        status = new JLabel("");
        p.add(status);
        
        p.add(Box.createHorizontalGlue());
        
        add(p);
    }
    
    
    /**
     * Set up table for showing individual recievers
     *<ol>
     *<li>Address
     *<li>Present Y/N
     *<li>Edit button
     *</ol>
     */
    public class AlignModel extends AbstractTableModel {
        static private final int NUMCOL = 0;
        static private final int XCOL = 1;
        static private final int YCOL = 2;
        static private final int ZCOL = 3;
        
        static private final int LAST = 3;
        
        public int getColumnCount () {return LAST+1;}

        public int getRowCount () {
            return 127;
        }

        public String getColumnName(int c) {
            switch (c) {
            case NUMCOL:
                return rb.getString("TitleColNum");
            case XCOL:
                return "X";
            case YCOL:
                return "Y";
            case ZCOL:
                return "Z";
            default:
                return "";
            }
        }

        public Class getColumnClass(int c) {
            if (c == XCOL || c == YCOL || c == ZCOL)
                return Double.class;
            else if (c == NUMCOL)
                return Integer.class;
            else 
                return String.class;
        }

        public boolean isCellEditable(int r,int c) {
            if (c == XCOL || c == YCOL || c == ZCOL)
                return true;
            else 
                return false;
        }

        public Object getValueAt (int r,int c) {
            // r is row number, from 0, and therefore r+1 is receiver number
            switch (c) {
            case NUMCOL:
                return new Integer(r+1);
            case XCOL:
            case YCOL:
            case ZCOL:
                
            default:
                return null;
            }
        }

        public void setValueAt(Object type,int r,int c) {
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AlignTableFrame.class.getName());

}
