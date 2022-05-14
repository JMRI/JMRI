package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.UserPreferencesManager;
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;
import jmri.util.JmriJFrame;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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
            super.setEnabled(false);
        }
    }

    protected ReporterManager reporterManager = InstanceManager.getDefault(ReporterManager.class);

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
        m = new ReporterTableDataModel(reporterManager);
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

    private JmriJFrame addFrame = null;
    private final JTextField hardwareAddressTextField = new JTextField(20);
    private final JTextField userNameTextField = new JTextField(20);
    private final ManagerComboBox<Reporter> prefixBox = new ManagerComboBox<>();
    private final SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    private final JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    private final JCheckBox rangeCheckBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    private final String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    private JButton addButton;
    private final JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    private final String userNameError = this.getClass().getName() + ".DuplicateUserName"; // only used in this package
    private Manager<Reporter> connectionChoice = null;
    private UserPreferencesManager pref;
    private SystemNameValidator hardwareAddressValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPressed(ActionEvent e) {
        pref = InstanceManager.getDefault(UserPreferencesManager.class);
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

            if (hardwareAddressValidator==null){
                hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, java.util.Objects.requireNonNull(prefixBox.getSelectedItem()), true);
            } else {
                hardwareAddressValidator.setManager(prefixBox.getSelectedItem());
            }

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
        addFrame.setEscapeKeyClosesWindow(true);
        addFrame.getRootPane().setDefaultButton(addButton);
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
        String uName = userNameTextField.getText();


        // Compose the proposed system name from parts:
        rName = reporterPrefix + reporterManager.typeLetter() + curAddress;

       for (int x = 0; x < numberOfReporters; x++) {

            // create the next reporter
            Reporter r;
            try {
                r = reporterManager.provideReporter(rName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(ex, rName); // displays message dialog to the user
                return; // without creating
            }

            // handle setting user name
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

            // bump system name
            try {
                rName = InstanceManager.getDefault(ReporterManager.class).getNextValidSystemName(r);
            } catch (jmri.JmriException ex) {
                displayHwError(r.getSystemName(), ex);
                // directly add to statusBarLabel (but never called?)
                statusBarLabel.setText(Bundle.getMessage("ErrorConvertHW", rName));
                statusBarLabel.setForeground(Color.red);
                return;
            }

            // bump user name
            if (!uName.isEmpty()) {
                uName = nextName(uName);
            }

            // end of for loop creating rangeCheckBox of Reporters
        }
        // provide success feedback to uName
        statusBarLabel.setText(statusMessage);
        statusBarLabel.setForeground(Color.gray);

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

    void handleCreateException(Exception ex, String sysName) {
        statusBarLabel.setText(ex.getLocalizedMessage());
        statusBarLabel.setForeground(Color.red);
        String err = Bundle.getMessage("ErrorBeanCreateFailed",
            InstanceManager.getDefault(ReporterManager.class).getBeanTypeHandled(),sysName);
        JOptionPane.showMessageDialog(addFrame, err + "\n" + ex.getLocalizedMessage(),
                err, JOptionPane.ERROR_MESSAGE);
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

    // private final static Logger log = LoggerFactory.getLogger(ReporterTableAction.class);

}
