package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;
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
        if (reporterManager == null) {
            setEnabled(false);
        }
    }

    protected ReporterManager reporterManager = InstanceManager.getDefault(jmri.ReporterManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<Reporter> man) {
        if (man instanceof ReporterManager) {
            reporterManager = (ReporterManager) man;
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
                Reporter r = reporterManager.getBySystemName(name);
                if (r == null) {
                    return "";
                }
                value = r.getCurrentReport();
                if (value == null) {
                    return null;
                } else if (value instanceof jmri.Reportable) {
                    return ((jmri.Reportable) value).toReportString();
                } else {
                    return value.toString();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public ReporterManager getManager() {
                return reporterManager;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Reporter getBySystemName(String name) {
                return reporterManager.getBySystemName(name);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Reporter getByUserName(String name) {
                return reporterManager.getByUserName(name);
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
                    if (value == null) {
                        return null;
                    } else if (value instanceof jmri.Reportable) {
                        return ((jmri.Reportable) value).toReportString();
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
    JTextField hardwareAddressTextField = new JTextField(20);
    JTextField userNameTextField = new JTextField(20);
    ManagerComboBox<Reporter> prefixBox = new ManagerComboBox<>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    JCheckBox rangeCheckBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JButton addButton;
    JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    String userNameError = this.getClass().getName() + ".DuplicateUserName"; // only used in this package
    Manager<Reporter> connectionChoice = null;
    jmri.UserPreferencesManager pref;
    SystemNameValidator hardwareAddressValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddReporter"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.ReporterAddEdit", true);
            ActionListener createListener = this::createPressed;
            ActionListener cancelListener = this::cancelPressed;
            ActionListener rangeListener = this::canAddRange;
            configureManagerComboBox(prefixBox, reporterManager, ReporterManager.class);
            userNameTextField.setName("userName"); // NOI18N
            prefixBox.setName("prefixBox"); // NOI18N
            addButton = new JButton(Bundle.getMessage("ButtonCreate"));
            addButton.addActionListener(createListener);
            hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, prefixBox.getSelectedItem(), true);
            // create panel
            addFrame.add(new AddNewHardwareDevicePanel(hardwareAddressTextField, hardwareAddressValidator, userNameTextField, prefixBox,
                    numberToAddSpinner, rangeCheckBox, addButton, cancelListener, rangeListener, statusBarLabel));
            // tooltip for hardwareAddressTextField will be assigned next by canAddRange()
            canAddRange(null);
        }
        hardwareAddressTextField.setName("sysName"); // for GUI test NOI18N
        hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
        addButton.setName("createButton"); // for GUI test NOI18N
        // reset statusBarLabel text
        statusBarLabel.setText(Bundle.getMessage("HardwareAddStatusEnter"));
        statusBarLabel.setForeground(Color.gray);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        removePrefixBoxListener(prefixBox);
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
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
        String rName;
        String reporterPrefix = prefixBox.getSelectedItem().getSystemPrefix(); // Add "R" later
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
            curAddress = reporterManager.getNextValidAddress(curAddress, reporterPrefix);
            if (curAddress == null) {
                log.debug("Error converting HW or getNextValidAddress");
                errorMessage = (Bundle.getMessage("WarningInvalidEntry"));
                statusBarLabel.setForeground(Color.red);
                // The next address returned an error, therefore we stop this attempt and go to the next address.
                break;
            }

            // Compose the proposed system name from parts:
            rName = reporterPrefix + reporterManager.typeLetter() + curAddress;
            // rName = prefix + InstanceManager.reportManagerInstance().typeLetter() + curAddress;
            Reporter r;
            try {
                r = reporterManager.provideReporter(rName);
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
                if ((reporterManager.getByUserName(uName) == null)) {
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

        pref.setComboBoxLastSelection(systemSelectionCombo, prefixBox.getSelectedItem().getMemo().getUserName());
        removePrefixBoxListener(prefixBox);
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    private String addEntryToolTip;

    /**
     * Activate Add a rangeCheckBox option if manager accepts adding more than 1
     * Reporter and set a manager specific tooltip on the AddNewHardwareDevice
     * pane.
     */
    private void canAddRange(ActionEvent e) {
        rangeCheckBox.setEnabled(false);
        rangeCheckBox.setSelected(false);
        if (prefixBox.getSelectedIndex() == -1) {
            prefixBox.setSelectedIndex(0);
        }
        connectionChoice = prefixBox.getSelectedItem(); // store in Field for CheckedTextField
        String systemPrefix = connectionChoice.getSystemPrefix();
        rangeCheckBox.setEnabled(((ReporterManager) connectionChoice).allowMultipleAdditions(systemPrefix));
        addEntryToolTip = connectionChoice.getEntryToolTip();
        // show hwAddressTextField field tooltip in the Add Reporter pane that matches system connection selected from combobox
        hardwareAddressTextField.setToolTipText(
                Bundle.getMessage("AddEntryToolTipLine1",
                        connectionChoice.getMemo().getUserName(),
                        Bundle.getMessage("Reporters"),
                        addEntryToolTip));
        hardwareAddressValidator.setToolTipText(hardwareAddressTextField.getToolTipText());
        hardwareAddressValidator.verify(hardwareAddressTextField);
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorReporterAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
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
