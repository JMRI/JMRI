// ReporterTableAction.java

package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.ReporterManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import java.util.List;

import jmri.util.JmriJFrame;
import jmri.util.ConnectionNameFromSystemName;

/**
 * Swing action to create and register a
 * ReporterTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision$
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
        if (reportManager==null) {
            setEnabled(false);
        }
    }
    
    protected ReporterManager reportManager = InstanceManager.reporterManagerInstance();
    public void setManager(ReporterManager man) { 
        reportManager = man;
    }


    public ReporterTableAction() { this("Reporter Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Reporters
     */
    protected void createModel() {
        m = new BeanTableDataModel() {

            public static final int LASTREPORTCOL = NUMCOLUMN;

            public String getValue(String name) {
                Object value;
                return (value=reportManager.getBySystemName(name).getCurrentReport())==null?"":value.toString();
            }
            public Manager getManager() { return reportManager; }
            public NamedBean getBySystemName(String name) { return reportManager.getBySystemName(name);}
            public NamedBean getByUserName(String name) { return reportManager.getByUserName(name);}
            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getClassName(),"delete"); }
            public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "delete", boo); }*/
            
            protected String getMasterClassName() { return getClassName(); }

            
            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }
    		public void setValueAt(Object value, int row, int col) {
        		if (col==VALUECOL) {
            		Reporter t = (Reporter)getBySystemName(sysNameList.get(row));
					t.setReport(value);
            		fireTableRowsUpdated(row,row);
                        }
                        if (col==LASTREPORTCOL) {
                            // do nothing
                        } else {
                            super.setValueAt(value, row, col);
                        }
    		}
                public int getColumnCount() {
                    return LASTREPORTCOL + 1;
                }
    		public String getColumnName(int col) {
        		if (col==VALUECOL) return "Report";
                        if (col==LASTREPORTCOL) return "Last Report";
        		return super.getColumnName(col);
        	}
    		public Class<?> getColumnClass(int col) {
    			if (col==VALUECOL) return String.class;
                        if (col==LASTREPORTCOL) return String.class;
    			return super.getColumnClass(col);
		    }
                public boolean isCellEditable(int row, int col) {
                    if (col==LASTREPORTCOL) return false;
                    return super.isCellEditable(row, col);
                }
                public Object getValueAt(int row, int col) {
                    if (col==LASTREPORTCOL) {
                        Reporter t = (Reporter) getBySystemName(sysNameList.get(row));
                        return t.getLastReport();
                    }
                    return super.getValueAt(row, col);
                }
                public int getPreferredWidth(int col) {
                    if (col==LASTREPORTCOL)
                        return super.getPreferredWidth(VALUECOL);
                    return super.getPreferredWidth(col);
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
            
            protected String getBeanType(){
                return AbstractTableAction.rbean.getString("BeanNameReporter");
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
    JTextField sysName = new JTextField(10);
    JTextField userName = new JTextField(20);
    JComboBox prefixBox = new JComboBox();
    JTextField numberToAdd = new JTextField(10);
    JCheckBox range = new JCheckBox("Add a range");
    JLabel sysNameLabel = new JLabel("Hardware Address");
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName()+".SystemSelected";
    String userNameError = this.getClass().getName()+".DuplicateUserName";
    jmri.UserPreferencesManager pref;

    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddReporter"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.ReporterAddEdit", true);
            ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okPressed(e);
                    }
                };
            ActionListener rangeListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        canAddRange(e);
                    }
                };
            if (reportManager.getClass().getName().contains("ProxyReporterManager")){
                jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) reportManager;
                List<Manager> managerList = proxy.getManagerList();
                for(int x = 0; x<managerList.size(); x++){
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    prefixBox.addItem(manuName);                      
                }
                if(pref.getComboBoxLastSelection(systemSelectionCombo)!=null)
                    prefixBox.setSelectedItem(pref.getComboBoxLastSelection(systemSelectionCombo));
            }
            else {
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(reportManager.getSystemPrefix()));
            }
            sysName.setName("sysName");
            userName.setName("userName");
            prefixBox.setName("prefixBox");
            addFrame.add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, "ButtonOK", listener, rangeListener));
            canAddRange(null);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void okPressed(ActionEvent e) {
        int numberOfReporters = 1;
        
        if(range.isSelected()){
            try {
                numberOfReporters = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showErrorMessage("Error","Number to Reporters to Add must be a number!",""+ex, "",true, false);
                return;
            }
        } 
        if (numberOfReporters>=65){
            if(JOptionPane.showConfirmDialog(addFrame,
                                                 "You are about to add " + numberOfReporters + " Reporters into the configuration\nAre you sure?","Warning",
                                                 JOptionPane.YES_NO_OPTION)==1)
                return;
        }
        String reporterPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        
        String rName = null;
        String curAddress = sysName.getText();
        
        for (int x = 0; x < numberOfReporters; x++){
            curAddress = reportManager.getNextValidAddress(curAddress, reporterPrefix);
            if (curAddress==null){
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            rName=reporterPrefix+reportManager.typeLetter()+curAddress;
            Reporter r = null;
            try {
                r = reportManager.provideReporter(rName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(rName);
                return; // without creating       
            }
            if (r!=null) {
                String user = userName.getText();
                if ((x!=0) && user != null && !user.equals(""))
                    user = userName.getText()+":"+x;
                if (user!= null && !user.equals("") && (reportManager.getByUserName(user)==null)){
                    r.setUserName(user);
                } else if (reportManager.getByUserName(user)!=null && !pref.getPreferenceState(getClassName(), userNameError)) {
                    pref.showErrorMessage("Duplicate UserName", "The username " + user + " specified is already in use and therefore will not be set", userNameError, "", false, true);
                }
            }
        }
        pref.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }
    
    private void canAddRange(ActionEvent e){
        range.setEnabled(false);
        range.setSelected(false);
        if (reportManager.getClass().getName().contains("ProxyReporterManager")){
            jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) reportManager;
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
            for(int x = 0; x<managerList.size(); x++){
                jmri.ReporterManager mgr = (jmri.ReporterManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)){
                    range.setEnabled(true);
                    return;
                }
            }
        }
        else if (reportManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))){
            range.setEnabled(true);
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

    protected String getClassName() { return ReporterTableAction.class.getName(); }
    
    public String getClassDescription() { return rb.getString("TitleReporterTable"); }
    
    static final Logger log = LoggerFactory.getLogger(ReporterTableAction.class.getName());
}

/* @(#)ReporterTableAction.java */
