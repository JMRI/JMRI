// MemoryTableAction.java

package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * MemoryTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision$
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
    protected void createModel() {
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
            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getClassName(),"delete"); }
            public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "delete", boo); }*/
            protected String getMasterClassName() { return getClassName(); }

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
            
            protected String getBeanType(){
                return AbstractTableAction.rbean.getString("BeanNameMemory");
            }
        };
    }

    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleMemoryTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.MemoryTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    
    JTextField numberToAdd = new JTextField(10);
    JCheckBox range = new JCheckBox(rb.getString("LabelNumberToAdd"));
    JCheckBox autoSystemName = new JCheckBox(rb.getString("LabelAutoSysName"));
    jmri.UserPreferencesManager p;

    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddMemory"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.MemoryAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okPressed(e);
                    }
                };
            addFrame.add(new AddNewBeanPanel(sysName, userName, numberToAdd, range, autoSystemName, "ButtonOK", listener));
        }
        if(p.getSimplePreferenceState(systemNameAuto))
            autoSystemName.setSelected(true);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    String systemNameAuto = this.getClass().getName()+".AutoSystemName";
    
    void okPressed(ActionEvent e) {
    
        int numberOfMemory = 1;
        
        if (range.isSelected()){
            try {
                numberOfMemory = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");

                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showErrorMessage("Error","Number to memory items to Add must be a number!",""+ex, "",true, false);
                return;
            }
        
        }
        
        if (numberOfMemory>=65){
            if(JOptionPane.showConfirmDialog(addFrame,
                                                 "You are about to add " + numberOfMemory + " Memory Objects into the configuration\nAre you sure?","Warning",
                                                 JOptionPane.YES_NO_OPTION)==1)
                return;
        }
        
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText();
        StringBuilder b;
        for (int x = 0; x< numberOfMemory; x++){
            if (x!=0){
                if (user!=null){
                    b = new StringBuilder(userName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    user=b.toString();
                }
                if(!autoSystemName.isSelected()){
                    b = new StringBuilder(sysName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    sName=b.toString();
                }
            }
            try {
                if (autoSystemName.isSelected())
                    InstanceManager.memoryManagerInstance().newMemory(user);
                else
                    InstanceManager.memoryManagerInstance().newMemory(sName, user);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
        }
        p.setSimplePreferenceState(systemNameAuto, autoSystemName.isSelected());
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
    
    public String getClassDescription() { return rb.getString("TitleMemoryTable"); }
    
    protected String getClassName() { return MemoryTableAction.class.getName(); }
    
    static final Logger log = LoggerFactory.getLogger(MemoryTableAction.class.getName());
}

/* @(#)MemoryTableAction.java */
