// MemoryTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Memory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

/**
 * Swing action to create and register a
 * MemoryTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

public class MemoryTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public MemoryTableAction(String actionName) { 
	super(actionName);

        // disable ourself if there is no primary Memory manager available
        if (jmri.InstanceManager.memoryManagerInstance()==null) {
            setEnabled(false);
        }

    }

    public MemoryTableAction() { this("Memory Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Memorys
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
            	Object m = InstanceManager.memoryManagerInstance().getBySystemName(name).getValue();
            	if (m!=null)
                	return m.toString();
                else
                	return "";
            }
            public Manager getManager() { return InstanceManager.memoryManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.memoryManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
            	// don't do anything on click
            }
    		public void setValueAt(Object value, int row, int col) {
        		if (col==VALUECOL) {
            		Memory t = (Memory)getBySystemName((String)sysNameList.get(row));
					t.setValue(value);
            		fireTableRowsUpdated(row,row);
        		} else super.setValueAt(value, row, col);
    		}
	   		public String getColumnName(int col) {
        		if (col==VALUECOL) return "Value";
        		return super.getColumnName(col);
        	}
    		public void configureTable(JTable table) {
        		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        		// no columns hold buttons, so don't make any configure buttons calls
		    }
			boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
				return (e.getPropertyName().indexOf("alue")>=0);
			}
			public JButton configureButton() {
				log.error("configureButton should not have been called");
				return null;
			}
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleMemoryTable"));
    }
    JFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JFrame(rb.getString("TitleAddMemory"));
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysName);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.pack();
        addFrame.show();
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText();
        InstanceManager.memoryManagerInstance().newMemory(sName, user);
    }
    private boolean noWarn = false;

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryTableAction.class.getName());
}

/* @(#)MemoryTableAction.java */
