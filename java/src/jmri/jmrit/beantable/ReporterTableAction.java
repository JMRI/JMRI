package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.ReporterManager;
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
     * of Reporters.
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
    CheckedTextField hardwareAddressTextField = new CheckedTextField(20);
    JTextField userNameTextField = new JTextField(20);
    JComboBox<String> prefixBox = new JComboBox<String>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JButton addButton;
    PropertyChangeListener colorChangeListener;
    JLabel statusBar = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    String userNameError = this.getClass().getName() + ".DuplicateUserName"; // only used in this package
    String connectionChoice = "";
    jmri.UserPreferencesManager pref;

    @Override
    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddReporter"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.ReporterAddEdit", true);
            ActionListener createListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            };
            ActionListener cancelListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            };
            ActionListener rangeListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canAddRange(e);
                }
            };
            if (reportManager.getClass().getName().contains("ProxyReporterManager")) {
                jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) reportManager;
                List<Manager<Reporter>> managerList = proxy.getManagerList();
                for (int x = 0; x < managerList.size(); x++) {
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    Boolean addToPrefix = true;
                    // Simple test not to add a system with a duplicate System prefix
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
            userNameTextField.setName("userName"); // NOI18N
            prefixBox.setName("prefixBox"); // NOI18N
            addButton = new JButton(Bundle.getMessage("ButtonCreate"));
            addButton.addActionListener(createListener);
            // Define PropertyChangeListener
            colorChangeListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    String property = propertyChangeEvent.getPropertyName();
                    if ("background".equals(property)) {
                        if ((Color) propertyChangeEvent.getNewValue() == Color.white) { // valid entry
                            addButton.setEnabled(true);
                        } else { // invalid
                            addButton.setEnabled(false);
                        }
                    }
                }
            };
            hardwareAddressTextField.addPropertyChangeListener(colorChangeListener);
            // create panel
            addFrame.add(new AddNewHardwareDevicePanel(hardwareAddressTextField, userNameTextField, prefixBox,
                    numberToAdd, range, addButton, cancelListener, rangeListener, statusBar));
            // tooltip for hardwareAddressTextField will be assigned next by canAddRange()
            canAddRange(null);
        }
        hardwareAddressTextField.setName("sysName"); // for GUI test NOI18N
        hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
        hardwareAddressTextField.setBackground(Color.yellow);
        addButton.setEnabled(false); // start as disabled (false) until a valid entry is typed in
        addButton.setName("createButton"); // for GUI test NOI18N
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("HardwareAddStatusEnter"));
        statusBar.setForeground(Color.gray);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
        addButton.removePropertyChangeListener(colorChangeListener);
    }

    void createPressed(ActionEvent e) {

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
        String rName = null;
        String reporterPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()); // Add "R" later
        String curAddress = hardwareAddressTextField.getText().trim();
        // initial check for empty entry
        if (curAddress.length() < 1) {
            statusBar.setText(Bundle.getMessage("WarningEmptyHardwareAddress"));
            statusBar.setForeground(Color.red);
            hardwareAddressTextField.setBackground(Color.red);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the ReporterManager
        String statusMessage = Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameReporter"));
        String errorMessage = null;
        for (int x = 0; x < numberOfReporters; x++) {
            curAddress = reportManager.getNextValidAddress(curAddress, reporterPrefix);
            if (curAddress == null) {
                log.debug("Error converting HW or getNextValidAddress");
                errorMessage = (Bundle.getMessage("WarningInvalidEntry"));
                statusBar.setForeground(Color.red);
                // The next address returned an error, therefore we stop this attempt and go to the next address.
                break;
            }

            // Compose the proposed system name from parts:
            rName = reporterPrefix + reportManager.typeLetter() + curAddress;
            // rName = prefix + InstanceManager.reportManagerInstance().typeLetter() + curAddress;
            Reporter r = null;
            try {
                r = reportManager.provideReporter(rName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(rName); // displays message dialog to the user
                // add to statusBar as well
                errorMessage = Bundle.getMessage("WarningInvalidEntry");
                statusBar.setText(errorMessage);
                statusBar.setForeground(Color.red);
                return; // without creating
            }

            String user = userNameTextField.getText().trim();
            if ((x != 0) && user != null && !user.equals("")) {
                user = user + ":" + x; // add :x to user name starting with 2nd item
            }
            if (user != null && !user.equals("") && (reportManager.getByUserName(user) == null)) {
                r.setUserName(user);
            } else if (user != null && !user.equals("") && reportManager.getByUserName(user) != null && !pref.getPreferenceState(getClassName(), userNameError)) {
                pref.showErrorMessage(Bundle.getMessage("ErrorTitle"),
                        Bundle.getMessage("ErrorDuplicateUserName", user), userNameError, "", false, true);
            }

            // add first and last names to statusMessage user feedback string
            if (x == 0 || x == numberOfReporters - 1) {
                statusMessage = statusMessage + " " + rName + " (" + user + ")";
            }
            if (x == numberOfReporters - 2) {
                statusMessage = statusMessage + " " + Bundle.getMessage("ItemCreateUpTo") + " ";
            }
            // only mention first and last of range added

            // end of for loop creating range of Reporters
        }
        // provide feedback to user
        if (errorMessage == null) {
            statusBar.setText(statusMessage);
            statusBar.setForeground(Color.gray);
        } else {
            statusBar.setText(errorMessage);
            // statusBar.setForeground(Color.red); // handled when errorMassage is set to differentiate urgency
        }

        pref.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
        addButton.removePropertyChangeListener(colorChangeListener);
    }

    private String addEntryToolTip;

    /**
     * Activate Add a range option if manager accepts adding more than 1 Reporter
     * and set a manager specific tooltip on the AddNewHardwareDevice pane.
     */
    private void canAddRange(ActionEvent e) {
        range.setEnabled(false);
        range.setSelected(false);
        connectionChoice = (String) prefixBox.getSelectedItem(); // store in Field for CheckedTextField
        if (connectionChoice == null) {
            // Tab All or first time opening, default tooltip
            connectionChoice = "TBD";
        }
        if (reportManager.getClass().getName().contains("ProxyReporterManager")) {
            jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) reportManager;
            List<Manager<Reporter>> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName(connectionChoice);
            for (int x = 0; x < managerList.size(); x++) {
                jmri.ReporterManager mgr = (jmri.ReporterManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)) {
                    range.setEnabled(true);
                    // get tooltip from ProxyReporterManager
                    addEntryToolTip = mgr.getEntryToolTip();
                    log.debug("R add box set");
                    break;
                }
            }
        } else if (reportManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))) {
            range.setEnabled(true);
            log.debug("R add box enabled2");
            // get tooltip from sensor manager
            addEntryToolTip = reportManager.getEntryToolTip();
            log.debug("ReporterManager tip");
        }
        // show hwAddressTextField field tooltip in the Add Reporter pane that matches system connection selected from combobox
        hardwareAddressTextField.setToolTipText("<html>"
                + Bundle.getMessage("AddEntryToolTipLine1", connectionChoice, Bundle.getMessage("Sensors"))
                + "<br>" + addEntryToolTip + "</html>");
        hardwareAddressTextField.setBackground(Color.yellow); // reset
        addButton.setEnabled(true); // ambiguous, so start enabled
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorReporterAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Extends JTextField to provide a data validation function.
     *
     * @author Egbert Broerse 2017, based on
     * jmri.jmrit.util.swing.ValidatedTextField by B. Milhaupt
     */
    public class CheckedTextField extends JTextField {

        CheckedTextField fld;
        boolean allow0Length = false; // for Add new bean item, a value that is zero-length is considered invalid.
        private MyVerifier verifier; // internal mechanism used for verifying field data before focus is lost

        /**
         * Text entry field with an active key event checker.
         *
         * @param len field length
         */
        public CheckedTextField(int len) {
            super("", len);
            fld = this;

            // configure InputVerifier
            verifier = new MyVerifier();
            fld = this;
            fld.setInputVerifier(verifier);

            fld.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    setEditable(true);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    setEditable(true);
                }
            });
        }

        /**
         * Validate the field information. Does not make any GUI changes.
         * <p>
         * During validation, logging is capped at the Error level to keep the Console clean from repeated validation.
         * This is reset to default level afterwards.
         *
         * @return 'true' if current field entry is valid according to the
         *         system manager; otherwise 'false'
         */
        @Override
        public boolean isValid() {
            String value;
            String prefix = ConnectionNameFromSystemName.getPrefixFromName(connectionChoice); // connectionChoice is set by canAddRange()

            if (fld == null) {
                return false;
            }
            value = getText().trim();
            if ((value.length() < 1) && (allow0Length == false)) {
                return false;
            } else if ((allow0Length == true) && (value.length() == 0)) {
                return true;
            } else {
                boolean validFormat = false;
                    // try {
                    validFormat = (InstanceManager.getDefault(ReporterManager.class).validSystemNameFormat(prefix + "R" + value) == Manager.NameValidity.VALID);
                    // } catch (jmri.JmriException e) {
                    // use it for the status bar?
                    // }
                if (validFormat) {
                    addButton.setEnabled(true); // directly update Create button
                    return true;
                } else {
                    addButton.setEnabled(false); // directly update Create button
                    return false;
                }
            }
        }

        /**
         * Private class used in conjunction with CheckedTextField to provide
         * the mechanisms required to validate the text field data upon loss of
         * focus, and colorize the text field in case of validation failure.
         */
        private class MyVerifier extends javax.swing.InputVerifier implements java.awt.event.ActionListener {

            // set default background color for invalid field data
            Color mark = Color.orange;

            @Override
            public boolean shouldYieldFocus(javax.swing.JComponent input) {
                if (input.getClass() == CheckedTextField.class) {

                    boolean inputOK = verify(input);
                    if (inputOK) {
                        input.setBackground(Color.white);
                        return true;
                    } else {
                        input.setBackground(mark);
                        ((javax.swing.text.JTextComponent) input).selectAll();
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            public boolean verify(javax.swing.JComponent input) {
                if (input.getClass() == CheckedTextField.class) {
                    return ((CheckedTextField) input).isValid();
                } else {
                    return false;
                }
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JTextField source = (JTextField) e.getSource();
                shouldYieldFocus(source); //ignore return value
                source.selectAll();
            }
        }
    }

    @Override
    protected String getClassName() {
        return ReporterTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleReporterTable");
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterTableAction.class);

}
