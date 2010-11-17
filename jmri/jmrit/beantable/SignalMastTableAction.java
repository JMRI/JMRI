// SignalMastTableAction.java

package jmri.jmrit.beantable;

import jmri.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import jmri.util.com.sun.TableSorter;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * SignalMastTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009, 2010
 * @version     $Revision: 1.9 $
 */

public class SignalMastTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public SignalMastTableAction(String actionName) {
        super(actionName);
    }
    public SignalMastTableAction() { this("Signal Mast Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    protected void createModel() {
        m = new jmri.jmrit.beantable.signalmast.SignalMastTableDataModel();
    }

    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleSignalMastTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalMastTable";
    }

    JmriJFrame addFrame = null;
    
    // has to agree with number in SignalMastDataModel
    final static int VALUECOL = BeanTableDataModel.VALUECOL;
    final static int SYSNAMECOL = BeanTableDataModel.SYSNAMECOL;
    
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new BeanTableFrame(m, helpTarget()){
            TableSorter sorter;
    
            /**
             * Include an "add" button
             */
            void extras() {
                JButton addButton = new JButton(this.rb.getString("ButtonAdd"));
                addToBottomBox(addButton);
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addPressed(e);
                    }
                });
            }
            protected JTable makeJTable(TableSorter srtr) {
                this.sorter = srtr;
                return new JTable(sorter)  {
                    public boolean editCellAt(int row, int column, java.util.EventObject e) {
                        boolean res = super.editCellAt(row, column, e);
                        java.awt.Component c = this.getEditorComponent();
                        if (c instanceof javax.swing.JTextField) {
                            ( (JTextField) c).selectAll();
                        }            
                        return res;
                    }
                    public TableCellRenderer getCellRenderer(int row, int column) {
                        if (column == VALUECOL) {
                            return getRenderer(row);
                        } else
                            return super.getCellRenderer(row, column);
                    }
                    public TableCellEditor getCellEditor(int row, int column) {
                        if (column == VALUECOL) {
                            return getEditor(row);
                        } else
                            return super.getCellEditor(row, column);
                    }
                    TableCellRenderer getRenderer(int row) {
                        TableCellRenderer retval = rendererMap.get(sorter.getValueAt(row,SYSNAMECOL));
                        if (retval == null) {
                            // create a new one with right aspects
                            retval = new MyComboBoxRenderer(getAspectVector(row));
                            rendererMap.put(sorter.getValueAt(row,SYSNAMECOL), retval);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellRenderer> rendererMap = new Hashtable<Object, TableCellRenderer>();
                
                    TableCellEditor getEditor(int row) {
                        TableCellEditor retval = editorMap.get(sorter.getValueAt(row,SYSNAMECOL));
                        if (retval == null) {
                            // create a new one with right aspects
                            retval = new MyComboBoxEditor(getAspectVector(row));
                            editorMap.put(sorter.getValueAt(row,SYSNAMECOL), retval);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellEditor> editorMap = new Hashtable<Object, TableCellEditor>();
                
                    Vector<String> getAspectVector(int row) {
                        Vector<String> retval = boxMap.get(sorter.getValueAt(row,SYSNAMECOL));
                        if (retval == null) {
                            // create a new one with right aspects
                            Vector<String> v = InstanceManager.signalMastManagerInstance()
                                                .getSignalMast((String)sorter.getValueAt(row,SYSNAMECOL)).getValidAspects();
                            retval = v;
                            boxMap.put(sorter.getValueAt(row,SYSNAMECOL), retval);
                        }
                        return retval;
                    }
                    Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();
                };
            }
        };
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }
    
    protected void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new jmri.jmrit.beantable.signalmast.AddSignalMastJFrame();
        }
        addFrame.setVisible(true);
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastTableAction.class.getName());

    public static class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {
        public MyComboBoxRenderer(Vector<String> items) {
            super(items);
        }
    
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
    
            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }
    
    public static class MyComboBoxEditor extends DefaultCellEditor {
        public MyComboBoxEditor(Vector<String> items) {
            super(new JComboBox(items));
        }
    }
}


/* @(#)SignalMastTableAction.java */
