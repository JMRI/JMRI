package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.StringIO;
import jmri.StringIOManager;
import jmri.UserPreferencesManager;
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * Swing action to create and register a StringIOTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 */
public class StringIOTableAction extends AbstractTableAction<StringIO> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public StringIOTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary StringIO manager available
        if (stringIOManager == null) {
            super.setEnabled(false);
        }
    }

    public StringIOTableAction() {
        this(Bundle.getMessage("TitleStringIOTable"));
    }

    protected StringIOManager stringIOManager = InstanceManager.getDefault(StringIOManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<StringIO> man) {
        if (man instanceof StringIOManager) {
            stringIOManager = (StringIOManager) man;
        }
    }


    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of StringIOs.
     */
    @Override
    protected void createModel() {
        m = new StringIOTableDataModel(stringIOManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleStringIOTable"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.StringIOTable";
    }

    private JmriJFrame addFrame = null;
    private final JTextField hardwareAddressTextField = new JTextField(20);
    private final JTextField userNameTextField = new JTextField(20);
    
    private final ManagerComboBox<StringIO> prefixBox = new ManagerComboBox<>();
    
    
    private final SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    private final JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    private final JCheckBox rangeCheckBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    private final String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    private JButton addButton;
    private final JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    private Manager<StringIO> connectionChoice = null;
    private UserPreferencesManager pref;
    private SystemNameValidator hardwareAddressValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPressed(ActionEvent e) {
        pref = InstanceManager.getDefault(UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddStringIO"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.StringIOAddEdit", true);
            ActionListener createListener = this::createPressed;
            ActionListener cancelListener = this::cancelPressed;
            ActionListener rangeListener = this::canAddRange;
            configureManagerComboBox(prefixBox, stringIOManager, StringIOManager.class);
            userNameTextField.setName("userName"); // NOI18N
            prefixBox.setName("prefixBox"); // NOI18N
            addButton = new JButton(Bundle.getMessage("ButtonCreate"));
            addButton.addActionListener(createListener);

            if (hardwareAddressValidator==null){
                hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, java.util.Objects.requireNonNull(prefixBox.getSelectedItem(), "encountered null system selection"), true);
            } else {
                log.trace("on add, prefixBox.getSelected is {}", prefixBox.getSelectedItem());
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
     * Respond to Create new item button pressed on Add StringIO pane.
     *
     * @param e the click event
     */
    void createPressed(ActionEvent e) {

        int numberOfStringIOs = 1;

        if (rangeCheckBox.isSelected()) {
            numberOfStringIOs = (Integer) numberToAddSpinner.getValue();
        }
        if (numberOfStringIOs >= 65 // limited by JSpinnerModel to 100
            && JmriJOptionPane.showConfirmDialog(addFrame,
                Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("StringIOs"), numberOfStringIOs),
                Bundle.getMessage("WarningTitle"),
                JmriJOptionPane.YES_NO_OPTION ) != JmriJOptionPane.YES_OPTION ) {
            return;
        }
        String rName;
        String stringIOPrefix = prefixBox.getSelectedItem().getSystemPrefix(); 
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

        // Add some entry pattern checking, before assembling sName and handing it to the StringIOManager
        StringBuilder statusMessage = new StringBuilder(
            Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameStringIO")));
        String uName = userNameTextField.getText();


        // Compose the proposed system name from parts:
        rName = stringIOPrefix + stringIOManager.typeLetter() + curAddress;
        log.trace("will create first with name {}", rName);
        
        for (int x = 0; x < numberOfStringIOs; x++) {

            // create the next StringIO
            StringIO r;
            try {
                r = stringIOManager.provideStringIO(rName);
                log.trace("created {} from {}", r, stringIOManager);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(ex, rName); // displays message dialog to the user
                return; // without creating
            }

            // handle setting user name
            if (!uName.isEmpty()) {
                if ((stringIOManager.getByUserName(uName) == null)) {
                    r.setUserName(uName);
                } else {
                    pref.showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorDuplicateUserName", uName),
                            getClassName(), "duplicateUserName", false, true);
                }
            }

            // add first and last names to statusMessage user feedback string
            // only mention first and last of rangeCheckBox added
            if (x == 0 || x == numberOfStringIOs - 1) {
                statusMessage.append(" ").append(rName).append(" (").append(uName).append(")");
            }
            if (x == numberOfStringIOs - 2) {
                statusMessage.append(" ").append(Bundle.getMessage("ItemCreateUpTo")).append(" ");
            }

            // except on last pass
            if (x < numberOfStringIOs-1) {
                // bump system name
                try {
                    rName = InstanceManager.getDefault(StringIOManager.class).getNextValidSystemName(r);
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
            }
            // end of for loop creating rangeCheckBox of StringIOs
        }
        // provide success feedback to uName
        statusBarLabel.setText(statusMessage.toString());
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
     * StringIO and set a manager specific tooltip on the AddNewHardwareDevice
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
        rangeCheckBox.setEnabled(((StringIOManager) connectionChoice).allowMultipleAdditions(systemPrefix));
        addEntryToolTip = connectionChoice.getEntryToolTip();
        // show hwAddressTextField field tooltip in the Add StringIO pane that matches system connection selected from combobox
        hardwareAddressTextField.setToolTipText(
                Bundle.getMessage("AddEntryToolTipLine1",
                        connectionChoice.getMemo().getUserName(),
                        Bundle.getMessage("StringIOs"),
                        addEntryToolTip));
        hardwareAddressValidator.setToolTipText(hardwareAddressTextField.getToolTipText());
        hardwareAddressValidator.verify(hardwareAddressTextField);
    }

    void handleCreateException(Exception ex, String sysName) {
        statusBarLabel.setText(ex.getLocalizedMessage());
        statusBarLabel.setForeground(Color.red);
        String err = Bundle.getMessage("ErrorBeanCreateFailed",
            InstanceManager.getDefault(StringIOManager.class).getBeanTypeHandled(),sysName);
        JmriJOptionPane.showMessageDialog(addFrame, err + "\n" + ex.getLocalizedMessage(),
                err, JmriJOptionPane.ERROR_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassName() {
        return StringIOTableAction.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleStringIOTable");
    }

    @Override
    public void setMessagePreferencesDetails() {
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                setPreferenceItemDetails(getClassName(), "duplicateUserName", Bundle.getMessage("DuplicateUserNameWarn"));  // NOI18N
        super.setMessagePreferencesDetails();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringIOTableAction.class);

}
