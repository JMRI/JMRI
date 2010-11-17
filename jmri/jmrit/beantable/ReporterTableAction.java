// ReporterTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reporter;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * ReporterTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.22 $
 */

public class ReporterTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public ReporterTableAction(String actionName) {
	super(actionName);

        // disable ourself if there is no primary Reporter manager available
        if (jmri.InstanceManager.reporterManagerInstance()==null) {
            setEnabled(false);
        }

    }

    public ReporterTableAction() { this("Reporter Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Reporters
     */
    protected void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
                return InstanceManager.reporterManagerInstance().getBySystemName(name).getCurrentReport().toString();
            }
            public Manager getManager() { return InstanceManager.reporterManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.reporterManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.reporterManagerInstance().getByUserName(name);}
            public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnReporterInUse(); }
            public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnReporterInUse(boo); }
            
            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }
    		public void setValueAt(Object value, int row, int col) {
        		if (col==VALUECOL) {
            		Reporter t = (Reporter)getBySystemName(sysNameList.get(row));
					t.setReport(value);
            		fireTableRowsUpdated(row,row);
        		} else super.setValueAt(value, row, col);
    		}
    		public String getColumnName(int col) {
        		if (col==VALUECOL) return "Report";
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
				// return (e.getPropertyName().indexOf("Report")>=0);
			}
			public JButton configureButton() {
				BeanTableDataModel.log.error("configureButton should not have been called");
				return null;
			}
        };
    }

    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleReporterTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.ReporterTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    protected void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddReporter"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.ReporterAddEdit", true);
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
        String sName = sysName.getText().toUpperCase();
        try {
            InstanceManager.reporterManagerInstance().newReporter(sName, user);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(sysName.getText());
            return; // without creating       
        }
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                    rb.getString("ErrorReporterAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReporterTableAction.class.getName());
}

/* @(#)ReporterTableAction.java */
