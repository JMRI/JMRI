package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.ReporterManager;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a ReporterTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class ReporterTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public ReporterTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary Reporter manager available
        if (reportManager == null) {
            setEnabled(false);
        }
    }

    protected ReporterManager reportManager = InstanceManager.getDefault(jmri.ReporterManager.class);

    public void setManager(ReporterManager man) {
        reportManager = man;
    }

    public ReporterTableAction() {
        this(Bundle.getMessage("TitleReporterTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Reporters
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel() {
            public static final int LASTREPORTCOL = NUMCOLUMN;

            @Override
            public String getValue(String name) {
                Object value;
                Reporter r = reportManager.getBySystemName(name);
                if (r == null) {
                    return "";
                }
                return (value = r.getCurrentReport()) == null ? "" : value.toString();
            }

            @Override
            public Manager getManager() {
                return reportManager;
            }

            @Override
            public NamedBean getBySystemName(String name) {
                return reportManager.getBySystemName(name);
            }

            @Override
            public NamedBean getByUserName(String name) {
                return reportManager.getByUserName(name);
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void clickOn(NamedBean t) {
                // don't do anything on click; not used in this class, because 
                // we override setValueAt
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == VALUECOL) {
                    Reporter t = (Reporter) getBySystemName(sysNameList.get(row));
                    t.setReport(value);
                    fireTableRowsUpdated(row, row);
                }
                if (col == LASTREPORTCOL) {
                    // do nothing
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
            public int getColumnCount() {
                return LASTREPORTCOL + 1;
            }

            @Override
            public String getColumnName(int col) {
                if (col == VALUECOL) {
                    return Bundle.getMessage("LabelReport");
                }
                if (col == LASTREPORTCOL) {
                    return Bundle.getMessage("LabelLastReport");
                }
                return super.getColumnName(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == VALUECOL) {
                    return String.class;
                }
                if (col == LASTREPORTCOL) {
                    return String.class;
                }
                return super.getColumnClass(col);
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == LASTREPORTCOL) {
                    return false;
                }
                return super.isCellEditable(row, col);
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col == LASTREPORTCOL) {
                    Reporter t = (Reporter) getBySystemName(sysNameList.get(row));
                    return t.getLastReport();
                }
                return super.getValueAt(row, col);
            }

            @Override
            public int getPreferredWidth(int col) {
                if (col == LASTREPORTCOL) {
                    return super.getPreferredWidth(VALUECOL);
                }
                return super.getPreferredWidth(col);
            }

            @Override
            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("Report")>=0);
            }

            @Override
            public JButton configureButton() {
                log.error("configureButton should not have been called");
                return null;
            }

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameReporter");
            }
        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleReporterTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.ReporterTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(10);
    JTextField userName = new JTextField(20);
    JComboBox<String> prefixBox = new JComboBox<String>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JLabel sysNameLabel = new JLabel("Hardware Address");
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    String userNameError = this.getClass().getName() + ".DuplicateUserName";
    jmri.UserPreferencesManager pref;

    @Override
    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddReporter"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.ReporterAddEdit", true);
            ActionListener okListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            ActionListener cancelListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { cancelPressed(e); }
            };
            ActionListener rangeListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canAddRange(e);
                }
            };
            if (reportManager.getClass().getName().contains("ProxyReporterManager")) {
                jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) reportManager;
                List<Manager> managerList = proxy.getManagerList();
                for (int x = 0; x < managerList.size(); x++) {
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    Boolean addToPrefix = true;
                    //Simple test not to add a system with a duplicate System prefix
                    for (int i = 0; i < prefixBox.getItemCount(); i++) {
                        if ((prefixBox.getItemAt(i)).equals(manuName)) {
                            addToPrefix = false;
                        }
                    }
                    if (addToPrefix) {
                        prefixBox.addItem(manuName);
                    }
                }
                if (pref.getComboBoxLastSelection(systemSelectionCombo) != null) {
                    prefixBox.setSelectedItem(pref.getComboBoxLastSelection(systemSelectionCombo));
                }
            } else {
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(reportManager.getSystemPrefix()));
            }
            sysName.setName("sysName");
            userName.setName("userName");
            prefixBox.setName("prefixBox");
            addFrame.add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, "ButtonOK", okListener, cancelListener, rangeListener));
            canAddRange(null);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    void okPressed(ActionEvent e) {

        int numberOfReporters = 1;

        if (range.isSelected()) {
            numberOfReporters = (Integer) numberToAdd.getValue();
        }
        if (numberOfReporters >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Reporters"), numberOfReporters),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String reporterPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());

        String rName = null;
        String curAddress = sysName.getText();

        for (int x = 0; x < numberOfReporters; x++) {
            curAddress = reportManager.getNextValidAddress(curAddress, reporterPrefix);
            if (curAddress == null) {
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            rName = reporterPrefix + reportManager.typeLetter() + curAddress;
            Reporter r = null;
            try {
                r = reportManager.provideReporter(rName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(rName);
                return; // without creating       
            }

            String user = userName.getText();
            if ((x != 0) && user != null && !user.equals("")) {
                user = userName.getText() + ":" + x;
            }
            if (user != null && !user.equals("") && (reportManager.getByUserName(user) == null)) {
                r.setUserName(user);
            } else if (user != null && !user.equals("") && reportManager.getByUserName(user) != null && !pref.getPreferenceState(getClassName(), userNameError)) {
                pref.showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", user), userNameError, "", false, true);
            }

        }
        pref.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }

    private void canAddRange(ActionEvent e) {
        range.setEnabled(false);
        range.setSelected(false);
        if (reportManager.getClass().getName().contains("ProxyReporterManager")) {
            jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) reportManager;
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
            for (int x = 0; x < managerList.size(); x++) {
                jmri.ReporterManager mgr = (jmri.ReporterManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)) {
                    range.setEnabled(true);
                    return;
                }
            }
        } else if (reportManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))) {
            range.setEnabled(true);
        }
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorReporterAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    @Override
    protected String getClassName() {
        return ReporterTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleReporterTable");
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterTableAction.class.getName());
}
