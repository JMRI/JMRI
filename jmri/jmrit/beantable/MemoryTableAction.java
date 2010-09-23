// MemoryTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Memory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * MemoryTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.20 $
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
     * for the specific case of Memory objects
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
                Memory mem = InstanceManager.memoryManagerInstance().getBySystemName(name);
                if (mem == null) return "?";
            	Object m = mem.getValue();
            	if (m!=null)
                	return m.toString();
                else
                	return "";
            }
            public Manager getManager() { return InstanceManager.memoryManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.memoryManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.memoryManagerInstance().getByUserName(name);}
            public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnMemoryInUse(); }
            public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnMemoryInUse(boo); }

            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }
    		public void setValueAt(Object value, int row, int col) {
        		if (col==VALUECOL) {
            		Memory t = (Memory)getBySystemName(sysNameList.get(row));
					t.setValue(value);
            		fireTableRowsUpdated(row,row);
        		} else super.setValueAt(value, row, col);
    		}
	   		public String getColumnName(int col) {
        		if (col==VALUECOL) return "Value";
        		return super.getColumnName(col);
        	}
    		public Class<?> getColumnClass(int col) {
    			if (col==VALUECOL) return String.class;
    			else return super.getColumnClass(col);
		    }
    		public void configValueColumn(JTable table) {
        		// value column isn't button, so config is null
		    }
			protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			    return true;
				// return (e.getPropertyName().indexOf("alue")>=0);
			}
			public JButton configureButton() {
				BeanTableDataModel.log.error("configureButton should not have been called");
				return null;
			}
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleMemoryTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.MemoryTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddMemory"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.MemoryAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okPressed(e);
                    }
                };
            addFrame.add(new AddNewDevicePanel(sysName, userName, "ButtonOK", listener));
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText();
        try {
            InstanceManager.memoryManagerInstance().newMemory(sName, user);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(sName);
            return; // without creating       
        }
    }
    //private boolean noWarn = false;

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                    rb.getString("ErrorMemoryAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryTableAction.class.getName());
}

/* @(#)MemoryTableAction.java */
