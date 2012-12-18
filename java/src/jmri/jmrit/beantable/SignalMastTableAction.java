// SignalMastTableAction.java

package jmri.jmrit.beantable;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import jmri.util.com.sun.TableSorter;

/**
 * Swing action to create and register a
 * SignalMastTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009, 2010
 * @version     $Revision$
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

    jmri.jmrit.beantable.signalmast.AddSignalMastJFrame addFrame = null;

    // has to agree with number in SignalMastDataModel
    final static int VALUECOL = BeanTableDataModel.VALUECOL;
    final static int SYSNAMECOL = BeanTableDataModel.SYSNAMECOL;
    
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableSorter sorter = new TableSorter(m);
    	JTable dataTable = m.makeJTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());
        // create the frame
        f = new BeanTableFrame(m, helpTarget(), dataTable){
    
            /**
             * Include an "add" button
             */
            void extras() {
                JButton addButton = new JButton(this.rb.getString("ButtonAdd"));
                addToBottomBox(addButton, this.getClass().getName());
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addPressed(e);
                    }
                });
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
        } else {
            addFrame.refresh();
        }
        addFrame.setVisible(true);
    }
    
    public void setMenuBar(BeanTableFrame f){
        JMenuBar menuBar = f.getJMenuBar();
        JMenu pathMenu = new JMenu(rb.getString("Tools"));
        menuBar.add(pathMenu);
        JMenuItem item = new JMenuItem(rb.getString("MenuItemRepeaters"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                jmri.jmrit.beantable.signalmast.SignalMastRepeaterJFrame frame = new jmri.jmrit.beantable.signalmast.SignalMastRepeaterJFrame(); 
                frame.setVisible(true);
        	}
        });
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
    
    protected String getClassName() { return SignalMastTableAction.class.getName(); }
    
    public String getClassDescription() { return rb.getString("TitleSignalGroupTable"); }
}


/* @(#)SignalMastTableAction.java */
