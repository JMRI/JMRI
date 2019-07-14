package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.annotation.Nonnull;
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
public class ReporterTableAction extends AbstractTableAction<Reporter> {

    /**
     * Create an action with a specific title.
     * <p>
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<Reporter> man) {
        if (man instanceof ReporterManager) {
            reportManager = (ReporterManager) man;
        }
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
        m = new BeanTableDataModel<Reporter>() {
            public static final int LASTREPORTCOL = NUMCOLUMN;

            /**
             * {@inheritDoc}
             */
            @Override
            public String getValue(String name) {
                Object value;
                Reporter r = reportManager.getBySystemName(name);
                if (r == null) {
                    return "";
                }
                value = r.getCurrentReport();
                if(value == null) {
                   return null;
                } else if(value instanceof jmri.Reportable) {
                   return ((jmri.Reportable)value).toReportString();
                } else {
                   return value.toString();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public ReporterManager getManager() {
                return reportManager;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Reporter getBySystemName(String name) {
                return reportManager.getBySystemName(name);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Reporter getByUserName(String name) {
                return reportManager.getByUserName(name);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void clickOn(Reporter t) {
                // don't do anything on click; not used in this class, because
                // we override setValueAt
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == VALUECOL) {
                    Reporter t = getBySystemName(sysNameList.get(row));
                    t.setReport(value);
                    fireTableRowsUpdated(row, row);
                }
                if (col == LASTREPORTCOL) {
                    // do nothing
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getColumnCount() {
                return LASTREPORTCOL + 1;
            }

            /**
             * {@inheritDoc}
             */
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

            /**
             * {@inheritDoc}
             */
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

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == LASTREPORTCOL) {
                    return false;
                }
                return super.isCellEditable(row, col);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Object getValueAt(int row, int col) {
                if (col == LASTREPORTCOL) {
                    Reporter t = getBySystemName(sysNameList.get(row));
                    Object value = t.getLastReport();
                    if(value == null) {
                       return null;
                    } else if(value instanceof jmri.Reportable) {
                       return ((jmri.Reportable)value).toReportString();
                    } else {
                       return value.toString();
                    }
                }
                return super.getValueAt(row, col);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int getPreferredWidth(int col) {
                if (col == LASTREPORTCOL) {
                    return super.getPreferredWidth(VALUECOL);
                }
                return super.getPreferredWidth(col);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("Report")>=0);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public JButton configureButton() {
                log.error("configureButton should not have been called");
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameReporter");
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleReporterTable"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.ReporterTable";
    }

    JmriJFrame addFrame = null;
    CheckedTextField hardwareAddressTextField = new CheckedTextField(20);
    JTextField userNameTextField = new JTextField(20);
    JComboBox<String> prefixBox = new JComboBox<String>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    JCheckBox rangeCheckBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JButton addButton;
    PropertyChangeListener colorChangeListener;
    JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    String userNameError = this.getClass().getName() + ".DuplicateUserName"; // only used in this package
    String connectionChoice = "";
    jmri.UserPreferencesManager pref;

    /**
     * {@inheritDoc}
     */
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
            if (InstanceManager.getDefault(ReporterManager.class).getClass().getName().contains("ProxyReporterManager")) {            
            jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) InstanceManager.getDefault(ReporterManager.class);          
                List<Manager<Reporter>> managerList = proxy.getDisplayOrderManagerList();
                for (Manager<Reporter> reporter : managerList) {
                    String manuName = ConnectionNameFromSystemName.getConnectionName(reporter.getSystemPrefix());
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
                @Override
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
                    numberToAddSpinner, rangeCheckBox, addButton, cancelListener, rangeListener, statusBarLabel));
            // tooltip for hardwareAddressTextField will be assigned next by canAddRange()
            canAddRange(null);
        }
        hardwareAddressTextField.setName("sysName"); // for GUI test NOI18N
        hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
        hardwareAddressTextField.setBackground(Color.yellow);
        addButton.setEnabled(false); // start as disabled (false) until a valid entry is typed in
        addButton.setName("createButton"); // for GUI test NOI18N
        // reset statusBarLabel text
        statusBarLabel.setText(Bundle.getMessage("HardwareAddStatusEnter"));
        statusBarLabel.setForeground(Color.gray);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
        addButton.removePropertyChangeListener(colorChangeListener);
    }

    /**
     * Respond to Create new item button pressed on Add Reporter pane.
     *
     * @param e the click event
     */
    void createPressed(ActionEvent e) {

        int numberOfReporters = 1;

        if (rangeCheckBox.isSelected()) {
            numberOfReporters = (Integer) numberToAddSpinner.getValue();
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
        String curAddress = hardwareAddressTextField.getText();
        // initial check for empty entry
        if (curAddress.isEmpty()) {
            statusBarLabel.setText(Bundle.getMessage("WarningEmptyHardwareAddress"));
            statusBarLabel.setForeground(Color.red);
            hardwareAddressTextField.setBackground(Color.red);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the ReporterManager
        String statusMessage = Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameReporter"));
        String errorMessage = null;
        String uName = userNameTextField.getText();
        for (int x = 0; x < numberOfReporters; x++) {
            curAddress = reportManager.getNextValidAddress(curAddress, reporterPrefix);
            if (curAddress == null) {
                log.debug("Error converting HW or getNextValidAddress");
                errorMessage = (Bundle.getMessage("WarningInvalidEntry"));
                statusBarLabel.setForeground(Color.red);
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
                // add to statusBarLabel as well
                errorMessage = Bundle.getMessage("WarningInvalidEntry");
                statusBarLabel.setText(errorMessage);
                statusBarLabel.setForeground(Color.red);
                return; // without creating
            }

            if (!uName.isEmpty()) {
                if ((reportManager.getByUserName(uName) == null)) {
                    r.setUserName(uName);
                } else {
                    pref.showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorDuplicateUserName", uName), userNameError, "", false, true);
                }
            }

            // add first and last names to statusMessage user feedback string
            // only mention first and last of rangeCheckBox added
            if (x == 0 || x == numberOfReporters - 1) {
                statusMessage = statusMessage + " " + rName + " (" + uName + ")";
            }
            if (x == numberOfReporters - 2) {
                statusMessage = statusMessage + " " + Bundle.getMessage("ItemCreateUpTo") + " ";
            }

            // bump user name
            if (!uName.isEmpty()) {
                uName = nextName(uName);
            }

            // end of for loop creating rangeCheckBox of Reporters
        }
        // provide feedback to uName
        if (errorMessage == null) {
            statusBarLabel.setText(statusMessage);
            statusBarLabel.setForeground(Color.gray);
        } else {
            statusBarLabel.setText(errorMessage);
            // statusBarLabel.setForeground(Color.red); // handled when errorMassage is set to differentiate urgency
        }

        pref.setComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
        addButton.removePropertyChangeListener(colorChangeListener);
    }

    private String addEntryToolTip;

    /**
     * Activate Add a rangeCheckBox option if manager accepts adding more than 1
     * Reporter and set a manager specific tooltip on the AddNewHardwareDevice pane.
     */
    private void canAddRange(ActionEvent e) {
        rangeCheckBox.setEnabled(false);
        rangeCheckBox.setSelected(false);
        connectionChoice = (String) prefixBox.getSelectedItem(); // store in Field for CheckedTextField
        if (connectionChoice == null) {
            // Tab All or first time opening, default tooltip
            connectionChoice = "TBD";
        }
        String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName(connectionChoice);
        if (InstanceManager.getDefault(ReporterManager.class).getClass().getName().contains("ProxyReporterManager")) {            
            jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) InstanceManager.getDefault(ReporterManager.class);    
            List<Manager<Reporter>> managerList = proxy.getDisplayOrderManagerList();
            for (Manager<Reporter> mgr : managerList) {
                if (mgr.getSystemPrefix().equals(systemPrefix)) {
                    rangeCheckBox.setEnabled(((ReporterManager) mgr).allowMultipleAdditions(systemPrefix));
                    // get tooltip from ProxyReporterManager
                    addEntryToolTip = mgr.getEntryToolTip();
                    log.debug("R add box set");
                    break;
                }
            }
        } else if (reportManager.allowMultipleAdditions(systemPrefix)) {
            rangeCheckBox.setEnabled(true);
            log.debug("R add box enabled2");
            // get tooltip from reporter manager
            addEntryToolTip = reportManager.getEntryToolTip();
        }
        else {
            log.warn("Unable to set reporter tooltip or rangecheckbox");
        }
        // show hwAddressTextField field tooltip in the Add Reporter pane that matches system connection selected from combobox
        hardwareAddressTextField.setToolTipText("<html>"
                + Bundle.getMessage("AddEntryToolTipLine1", connectionChoice, Bundle.getMessage("Reporters"))
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
         * During validation, logging is capped at the Error level to keep the
         * Console clean from repeated validation. This is reset to default
         * level afterwards.
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
            if (value.isEmpty()) {
                return allow0Length;
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

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean shouldYieldFocus(javax.swing.JComponent input) {
                if (input instanceof CheckedTextField ) {
                    if (verify(input)) {
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

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean verify(javax.swing.JComponent input) {
                if (input.getClass() == CheckedTextField.class) {
                    return input.isValid();
                } else {
                    return false;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JTextField source = (JTextField) e.getSource();
                shouldYieldFocus(source); //ignore return value
                source.selectAll();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassName() {
        return ReporterTableAction.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleReporterTable");
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterTableAction.class);

}
