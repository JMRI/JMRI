// AlignTableFrame.java

package jmri.jmrix.rps.aligntable;

import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Receiver;

import javax.vecmath.Point3d;

import java.beans.PropertyChangeListener;

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
 * @version	$Revision: 1.5 $
 */
public class AlignTablePane extends javax.swing.JPanel {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.aligntable.AlignTableBundle");
		
    /**
     * Constructor method
     */
    public AlignTablePane(jmri.ModifiedFlag flag) {
    	super();
    	this.flag = flag;
    }

    AlignModel alignModel = null;
    jmri.ModifiedFlag flag;
    
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
            tmodel.setSortingStatus(alignModel.NUMCOL, jmri.util.com.sun.TableSorter.ASCENDING);
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
        p.setLayout(new FlowLayout());

        p.add(new JLabel(rb.getString("LabelNumCol")));
        num.setText(""+Engine.instance().getReceiverCount());
        p.add(num);
        
        JButton b = new JButton(rb.getString("ButtonSet"));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // set number of columns
                Engine.instance().setReceiverCount(
                    Integer.parseInt(num.getText()));
                // mark modification
                flag.setModifiedFlag(true);
                // resize table
                alignModel.fireTableStructureChanged();
                
            }
        });
        p.add(b);
        add(p);
        
        p = new JPanel() {
            public Dimension getMaximumSize() { 
                int height = getPreferredSize().height;
                int width = super.getMaximumSize().width;
                return new Dimension(width, height); }
        };
        p.setLayout(new FlowLayout());

        p.add(new JLabel(rb.getString("LabelVSound")));
        vsound.setText(""+Engine.instance().getVSound());
        p.add(vsound);
        
        p.add(new JLabel(rb.getString("LabelOffset")));
        offset.setText(""+Engine.instance().getOffset());
        p.add(offset);
        
        b = new JButton(rb.getString("ButtonSet"));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // set number of vsound, offset
                Engine.instance().setOffset(
                    Integer.parseInt(offset.getText()));
                Engine.instance().setVSound(
                    Double.parseDouble(vsound.getText()));
                // mark modification
                flag.setModifiedFlag(true);
            }
        });
        p.add(b);        
        add(p);

        //
        add(loadStore = new jmri.jmrix.rps.swing.LoadStorePanel(){
            // make sure we redisplay if changed
            public void load() {
                super.load();
                alignModel.fireTableStructureChanged();
                // modified (to force store of default after load new values)
                flag.setModifiedFlag(true);
            }
            public void storeDefault() {
                super.storeDefault();
                // no longer modified after storeDefault
                flag.setModifiedFlag(false);
            }
        });

        // add sound listener
        Engine.instance().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if(e.getPropertyName().equals("vSound")) {
                    // update sound display
                    vsound.setText(""+e.getNewValue());
                }
            }
        });
    }
    
    
    jmri.jmrix.rps.swing.LoadStorePanel loadStore;

    void storeDefault() {
        loadStore.storeDefault();
        // no longer modified after storeDefault
        flag.setModifiedFlag(false);
    }
    
    JTextField num      = new JTextField(4);
    JTextField vsound   = new JTextField(8);
    JTextField offset   = new JTextField(4);
    
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
            return Engine.instance().getReceiverCount();
        }

        public String getColumnName(int c) {
            switch (c) {
            case NUMCOL:
                return rb.getString("TitleColNum");
            case XCOL:
                return rb.getString("TitleColX");
            case YCOL:
                return rb.getString("TitleColY");
            case ZCOL:
                return rb.getString("TitleColZ");
            default:
                return "";
            }
        }

        public Class getColumnClass(int c) {
            if (c == XCOL || c == YCOL || c == ZCOL)
                return Double.class;
            else if (c == NUMCOL)
                return Double.class;
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
            // r is row number, from 0; receiver addresses start at 1
            Receiver rc;
            switch (c) {
            case NUMCOL:
                return new Integer(r+1);
            case XCOL:
                rc = Engine.instance().getReceiver(r+1);
                if (rc==null) return null;
                return new Double(rc.getPosition().x);
            case YCOL:
                rc = Engine.instance().getReceiver(r+1);
                if (rc==null) return null;
                return new Double(rc.getPosition().y);
            case ZCOL:
                rc = Engine.instance().getReceiver(r+1);
                if (rc==null) return null;
                return new Double(rc.getPosition().z);
            default:
                return null;
            }
        }

        public void setValueAt(Object val,int r,int c) {
            // r is row number, from 0
            Receiver rc;
            Point3d p;
            switch (c) {
            case XCOL:
                rc = Engine.instance().getReceiver(r+1);
                if (rc == null) {
                    rc = new Receiver(new Point3d(0.,0.,0.));
                    Engine.instance().setReceiver(r+1, rc);
                }
                p = rc.getPosition();
                p.x = ((Double)val).doubleValue();
                Engine.instance().setReceiverPosition(r+1, p);
                flag.setModifiedFlag(true);
                break;
            case YCOL:
                rc = Engine.instance().getReceiver(r+1);
                if (rc == null) {
                    rc = new Receiver(new Point3d(0.,0.,0.));
                    Engine.instance().setReceiver(r+1, rc);
                }
                p = rc.getPosition();
                p.y = ((Double)val).doubleValue();
                Engine.instance().setReceiverPosition(r+1, p);
                flag.setModifiedFlag(true);
                break;
            case ZCOL:
                rc = Engine.instance().getReceiver(r+1);
                if (rc == null) {
                    rc = new Receiver(new Point3d(0.,0.,0.));
                    Engine.instance().setReceiver(r+1, rc);
                }
                p = rc.getPosition();
                p.z = ((Double)val).doubleValue();
                Engine.instance().setReceiverPosition(r+1, p);
                flag.setModifiedFlag(true);
                break;
            default:
                log.error("setValueAt of column "+c);
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AlignTablePane.class.getName());

}
