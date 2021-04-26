package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.beantable.turnout.TurnoutTableDataModel;
import jmri.jmrit.turnoutoperations.TurnoutOperationFrame;
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;
import jmri.util.JmriJFrame;
import jmri.util.swing.TriStateJCheckBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a TurnoutTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @author Egbert Broerse Copyright (C) 2017
 */
public class TurnoutTableAction extends AbstractTableAction<Turnout> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public TurnoutTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary turnout manager available
        if (turnoutManager == null) {
            super.setEnabled(false);
        }
    }

    public TurnoutTableAction() {
        this(Bundle.getMessage("TitleTurnoutTable"));
    }

    protected TurnoutManager turnoutManager = InstanceManager.getDefault(TurnoutManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<Turnout> man) {
        if (man instanceof TurnoutManager) {
            log.debug("setting manager of TTAction {} to {}",this,man.getClass());
            turnoutManager = (TurnoutManager) man;
            if (m!=null){ // also update Table Model
                m.setManager(man);
            }            
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Turnouts.
     */
    @Override
    protected void createModel() {
        m = new TurnoutTableDataModel(turnoutManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleTurnoutTable"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    }

    JmriJFrame addFrame = null;

    JTextField hardwareAddressTextField = new JTextField(20);
    // initially allow any 20 char string, updated to prefixBox selection by canAddRange()
    JTextField userNameTextField = new JTextField(40);
    ManagerComboBox<Turnout> prefixBox = new ManagerComboBox<>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    JCheckBox rangeBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JButton addButton;
    JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    jmri.UserPreferencesManager pref;
    SystemNameValidator hardwareAddressValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddTurnout"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.TurnoutAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener cancelListener = this::cancelPressed;
            // add rangeBox box turned on/off
            ActionListener rangeListener = this::canAddRange;

            /* We use the proxy manager in this instance so that we can deal with
             duplicate usernames in multiple classes */
            configureManagerComboBox(prefixBox, turnoutManager, TurnoutManager.class);
            userNameTextField.setName("userNameTextField"); // NOI18N
            prefixBox.setName("prefixBox"); // NOI18N
            // set up validation, zero text = false
            addButton = new JButton(Bundle.getMessage("ButtonCreate"));
            addButton.addActionListener(this::createPressed);
            // create panel
            
            if (hardwareAddressValidator==null){
                hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, Objects.requireNonNull(prefixBox.getSelectedItem()), true);
            } else {
                hardwareAddressValidator.setManager(prefixBox.getSelectedItem());
            }
            
            addFrame.add(new AddNewHardwareDevicePanel(hardwareAddressTextField, hardwareAddressValidator, userNameTextField, prefixBox,
                    numberToAddSpinner, rangeBox, addButton, cancelListener, rangeListener, statusBarLabel));
            // tooltip for hardwareAddressTextField will be assigned next by canAddRange()
            canAddRange(null);
        }
        hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
        addButton.setName("createButton"); // for GUI test NOI18N

        addFrame.setEscapeKeyClosesWindow(true);
        addFrame.getRootPane().setDefaultButton(addButton);
        
        // reset statusBarLabel text
        statusBarLabel.setText(Bundle.getMessage("HardwareAddStatusEnter"));
        statusBarLabel.setForeground(Color.gray);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    /**
     * Add the content and make the appropriate selection to a combo box for a
     * turnout's automation choices.
     *
     * @param t  turnout
     * @param cb the JComboBox
     */
    public static void updateAutomationBox(Turnout t, JComboBox<String> cb) {
        TurnoutOperation[] ops = InstanceManager.getDefault(TurnoutOperationManager.class).getTurnoutOperations();
        cb.removeAllItems();
        Vector<String> strings = new Vector<>(20);
        Vector<String> defStrings = new Vector<>(20);
        log.debug("opsCombo start {}", ops.length);
        for (TurnoutOperation op : ops) {
            if (log.isDebugEnabled()) {
                log.debug("isDef {} mFMM {} isNonce {}", op.isDefinitive(), op.matchFeedbackMode(t.getFeedbackMode()), op.isNonce());
            }
            if (!op.isDefinitive() && op.matchFeedbackMode(t.getFeedbackMode()) && !op.isNonce()) {
                strings.addElement(op.getName());
            }
        }
        log.debug("opsCombo end");
        for (TurnoutOperation op : ops) {
            if (op.isDefinitive() && op.matchFeedbackMode(t.getFeedbackMode())) {
                defStrings.addElement(op.getName());
            }
        }
        java.util.Collections.sort(strings);
        java.util.Collections.sort(defStrings);
        strings.insertElementAt(Bundle.getMessage("TurnoutOperationOff"), 0);
        strings.insertElementAt(Bundle.getMessage("TurnoutOperationDefault"), 1);
        for (int i = 0; i < defStrings.size(); ++i) {
            try {
                strings.insertElementAt(defStrings.elementAt(i), i + 2);
            } catch (java.lang.ArrayIndexOutOfBoundsException obe) {
                // just catch it
            }
        }
        for (int i = 0; i < strings.size(); ++i) {
            cb.addItem(strings.elementAt(i));
        }
        if (t.getInhibitOperation()) {
            cb.setSelectedIndex(0);
        } else {
            TurnoutOperation turnOp = t.getTurnoutOperation();
            if (turnOp == null) {
                cb.setSelectedIndex(1);
            } else {
                if (turnOp.isNonce()) {
                    cb.setSelectedIndex(2);
                } else {
                    cb.setSelectedItem(turnOp.getName());
                }
            }
        }
    }

    /**
     * Show a pane to configure closed and thrown turnout speed defaults.
     *
     * @param _who parent JFrame to center the pane on
     */
    protected void setDefaultSpeeds(JFrame _who) {
        JComboBox<String> thrownCombo = new JComboBox<>((( TurnoutTableDataModel)m).speedListThrown);
        JComboBox<String> closedCombo = new JComboBox<>((( TurnoutTableDataModel)m).speedListClosed);
        thrownCombo.setEditable(true);
        closedCombo.setEditable(true);

        JPanel thrown = new JPanel();
        thrown.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ThrownSpeed"))));
        thrown.add(thrownCombo);

        JPanel closed = new JPanel();
        closed.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ClosedSpeed"))));
        closed.add(closedCombo);

        thrownCombo.removeItem((( TurnoutTableDataModel)m).defaultThrownSpeedText);
        closedCombo.removeItem((( TurnoutTableDataModel)m).defaultClosedSpeedText);

        thrownCombo.setSelectedItem(turnoutManager.getDefaultThrownSpeed());
        closedCombo.setSelectedItem(turnoutManager.getDefaultClosedSpeed());

        // block of options above row of buttons; gleaned from Maintenance.makeDialog()
        // can be accessed by Jemmy in GUI test
        String title = Bundle.getMessage("TurnoutGlobalSpeedMessageTitle");
        // build JPanel for comboboxes
        JPanel speedspanel = new JPanel();
        speedspanel.setLayout(new BoxLayout(speedspanel, BoxLayout.PAGE_AXIS));
        speedspanel.add(new JLabel(Bundle.getMessage("TurnoutGlobalSpeedMessage")));
        //default LEFT_ALIGNMENT
        thrown.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedspanel.add(thrown);
        closed.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedspanel.add(closed);

        int retval = JOptionPane.showConfirmDialog(_who,
                speedspanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        log.debug("Retval = {}", retval);
        if (retval != JOptionPane.OK_OPTION) { // OK button not clicked
            return;
        }
        String closedValue = (String) closedCombo.getSelectedItem();
        String thrownValue = (String) thrownCombo.getSelectedItem();

        // We will allow the turnout manager to handle checking whether the values have changed
        try {
            assert thrownValue != null;
            turnoutManager.setDefaultThrownSpeed(thrownValue);
        } catch (jmri.JmriException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + thrownValue);
        }

        try {
            assert closedValue != null;
            turnoutManager.setDefaultClosedSpeed(closedValue);
        } catch (jmri.JmriException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + closedValue);
        }
    }

    private final JCheckBox doAutomationBox = new JCheckBox(Bundle.getMessage("AutomaticRetry"));
    private final TriStateJCheckBox showFeedbackBox = new TriStateJCheckBox(Bundle.getMessage("ShowFeedbackInfo"));
    private final TriStateJCheckBox showLockBox = new TriStateJCheckBox(Bundle.getMessage("ShowLockInfo"));
    private final TriStateJCheckBox showTurnoutSpeedBox = new TriStateJCheckBox(Bundle.getMessage("ShowTurnoutSpeedDetails"));
    private final TriStateJCheckBox showStateForgetAndQueryBox = new TriStateJCheckBox(Bundle.getMessage("ShowStateForgetAndQuery"));

    private void initCheckBoxes(){
        doAutomationBox.setSelected(InstanceManager.getDefault(TurnoutOperationManager.class).getDoOperations());
        doAutomationBox.setToolTipText(Bundle.getMessage("TurnoutDoAutomationBoxTooltip"));
        doAutomationBox.addActionListener(e -> InstanceManager.getDefault(TurnoutOperationManager.class).setDoOperations(doAutomationBox.isSelected()));
        
        showFeedbackBox.setToolTipText(Bundle.getMessage("TurnoutFeedbackToolTip"));
        showLockBox.setToolTipText(Bundle.getMessage("TurnoutLockToolTip"));
        showTurnoutSpeedBox.setToolTipText(Bundle.getMessage("TurnoutSpeedToolTip"));
        showStateForgetAndQueryBox.setToolTipText(Bundle.getMessage("StateForgetAndQueryBoxToolTip"));
    }
    
    @Override
    protected void configureTable(JTable table){
        super.configureTable(table);
        showStateForgetAndQueryBox.addActionListener(e ->
            ((TurnoutTableDataModel) m).showStateForgetAndQueryChanged(showStateForgetAndQueryBox.isSelected(),table));
        showTurnoutSpeedBox.addActionListener(e ->
            ((TurnoutTableDataModel) m).showTurnoutSpeedChanged(showTurnoutSpeedBox.isSelected(),table));
        showFeedbackBox.addActionListener(e ->
            ((TurnoutTableDataModel) m).showFeedbackChanged(showFeedbackBox.isSelected(), table));
        showLockBox.addActionListener(e ->
            ((TurnoutTableDataModel) m).showLockChanged(showLockBox.isSelected(),table));
    }
    
    /**
     * Add the check boxes to show/hide extra columns to the Turnout table
     * frame.
     * <p>
     * Keep contents synchronized with
     * {@link #addToPanel(AbstractTableTabAction)}
     *
     * @param f a Turnout table frame
     */
    @Override
    public void addToFrame(BeanTableFrame<Turnout> f) {
        initCheckBoxes();
        f.addToBottomBox(doAutomationBox, this.getClass().getName());
        f.addToBottomBox(showFeedbackBox, this.getClass().getName());
        f.addToBottomBox(showLockBox, this.getClass().getName());
        f.addToBottomBox(showTurnoutSpeedBox, this.getClass().getName());
        f.addToBottomBox(showStateForgetAndQueryBox, this.getClass().getName());
    }

    /**
     * Place the check boxes to show/hide extra columns to the tabbed Turnout
     * table panel.
     * <p>
     * Keep contents synchronized with {@link #addToFrame(BeanTableFrame)}
     *
     * @param f a Turnout table action
     */
    @Override
    public void addToPanel(AbstractTableTabAction<Turnout> f) {
        String connectionName = turnoutManager.getMemo().getUserName();
        if (turnoutManager.getClass().getName().contains("ProxyTurnoutManager")) {
            connectionName = "All"; // NOI18N
        }
        initCheckBoxes();
        f.addToBottomBox(doAutomationBox, connectionName);
        f.addToBottomBox(showFeedbackBox, connectionName);
        f.addToBottomBox(showLockBox, connectionName);
        f.addToBottomBox(showTurnoutSpeedBox, connectionName);
        f.addToBottomBox(showStateForgetAndQueryBox, connectionName);
    }
    
    /**
     * Override to update column select checkboxes.
     * {@inheritDoc}
     */
    @Override
    protected void columnsVisibleUpdated(boolean[] colsVisible){
        log.debug("columns updated {}",colsVisible);
        showFeedbackBox.setState(new boolean[]{
            colsVisible[TurnoutTableDataModel.KNOWNCOL],
            colsVisible[TurnoutTableDataModel.MODECOL],
            colsVisible[TurnoutTableDataModel.SENSOR1COL],
            colsVisible[TurnoutTableDataModel.SENSOR2COL],
            colsVisible[TurnoutTableDataModel.OPSONOFFCOL],
            colsVisible[TurnoutTableDataModel.OPSEDITCOL]});
        
        showLockBox.setState(new boolean[]{
            colsVisible[TurnoutTableDataModel.LOCKDECCOL],
            colsVisible[TurnoutTableDataModel.LOCKOPRCOL]});
        
        showTurnoutSpeedBox.setState(new boolean[]{
            colsVisible[TurnoutTableDataModel.STRAIGHTCOL],
            colsVisible[TurnoutTableDataModel.DIVERGCOL]});
        
        showStateForgetAndQueryBox.setState(new boolean[]{
            colsVisible[TurnoutTableDataModel.FORGETCOL],
            colsVisible[TurnoutTableDataModel.QUERYCOL]});
        
    }

    /**
     * Insert table specific Automation and Speeds menus. Account for the Window and Help
     * menus, which are already added to the menu bar as part of the creation of
     * the JFrame, by adding the Automation menu 2 places earlier unless the
     * table is part of the ListedTableFrame, that adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame<Turnout> f) {
        final jmri.util.JmriJFrame finalF = f;   // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        // check for menu
        boolean menuAbsent = true;
        for (int i = 0; i < menuBar.getMenuCount(); ++i) {
            String name = menuBar.getMenu(i).getAccessibleContext().getAccessibleName();
            if (name.equals(Bundle.getMessage("TurnoutAutomationMenu"))) {
                // using first menu for check, should be identical to next JMenu Bundle
                menuAbsent = false;
                break;
            }
        }
        if (menuAbsent) { // create it
            int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenu before 'Window' and 'Help'
            int offset = 1;
            log.debug("setMenuBar number of menu items = {}", pos);
            for (int i = 0; i <= pos; i++) {
                if (menuBar.getComponent(i) instanceof JMenu) {
                    if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                        offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                    }
                }
            }
            JMenu opsMenu = new JMenu(Bundle.getMessage("TurnoutAutomationMenu"));
            JMenuItem item = new JMenuItem(Bundle.getMessage("TurnoutAutomationMenuItemEdit"));
            opsMenu.add(item);
            item.addActionListener(e -> new TurnoutOperationFrame(finalF));
            menuBar.add(opsMenu, pos + offset);

            JMenu speedMenu = new JMenu(Bundle.getMessage("SpeedsMenu"));
            item = new JMenuItem(Bundle.getMessage("SpeedsMenuItemDefaults"));
            speedMenu.add(item);
            item.addActionListener(e -> setDefaultSpeeds(finalF));
            menuBar.add(speedMenu, pos + offset + 1); // add this menu to the right of the previous
        }
    }

    void cancelPressed(ActionEvent e) {
        removePrefixBoxListener(prefixBox);
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    /**
     * Respond to Create new item button pressed on Add Turnout pane.
     *
     * @param e the click event
     */
    void createPressed(ActionEvent e) {

        int numberOfTurnouts = 1;

        if (rangeBox.isSelected()) {
            numberOfTurnouts = (Integer) numberToAddSpinner.getValue();
        }
        if (numberOfTurnouts >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Turnouts"), numberOfTurnouts),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }

        String sName;
        String prefix = Objects.requireNonNull(prefixBox.getSelectedItem()).getSystemPrefix();

        String curAddress = hardwareAddressTextField.getText();
        // initial check for empty entry
        if (curAddress.length() < 1) {
            statusBarLabel.setText(Bundle.getMessage("WarningEmptyHardwareAddress"));
            statusBarLabel.setForeground(Color.red);
            hardwareAddressTextField.setBackground(Color.red);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }

        String uName = userNameTextField.getText();
        if (uName.isEmpty()) {
            uName = null;
        }

        // Add some entry pattern checking, before assembling sName and handing it to the TurnoutManager
        StringBuilder statusMessage = new StringBuilder(Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameTurnout")));
        String lastSuccessfulAddress;

        int iType = 0;
        int iNum = 1;
        boolean useLastBit = false;
        boolean useLastType = false;

        for (int x = 0; x < numberOfTurnouts; x++) {
            try {
                curAddress = InstanceManager.getDefault(TurnoutManager.class).getNextValidAddress(curAddress, prefix, false);
            } catch (jmri.JmriException ex) {
                displayHwError(curAddress, ex);
                // directly add to statusBarLabel (but never called?)
                statusBarLabel.setText(Bundle.getMessage("ErrorConvertHW", curAddress));
                statusBarLabel.setForeground(Color.red);
                return;
            }

            lastSuccessfulAddress = curAddress;
            // Compose the proposed system name from parts:
            sName = prefix + InstanceManager.getDefault(TurnoutManager.class).typeLetter() + curAddress;

            // test for a Light by the same hardware address (number):
            String testSN = prefix + "L" + curAddress;
            jmri.Light testLight = InstanceManager.lightManagerInstance().
                    getBySystemName(testSN);
            if (testLight != null) {
                // Address (number part) is already used as a Light
                log.warn("Requested Turnout {} uses same address as Light {}", sName, testSN);
                if (!noWarn) {
                    int selectedValue = JOptionPane.showOptionDialog(addFrame,
                            Bundle.getMessage("TurnoutWarn1", sName, testSN)
                            + ".\n" + Bundle.getMessage("TurnoutWarn3"), Bundle.getMessage("WarningTitle"),
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                                Bundle.getMessage("ButtonYesPlus")}, Bundle.getMessage("ButtonNo")); // default choice = No
                    if (selectedValue == 1) {
                        // Show error message in statusBarLabel
                        statusBarLabel.setText(Bundle.getMessage("WarningOverlappingAddress", sName));
                        statusBarLabel.setForeground(Color.gray);
                        return;   // return without creating if "No" response
                    }
                    if (selectedValue == 2) {
                        // Suppress future warnings, and continue
                        noWarn = true;
                    }
                }
            }

            // Ask about two bit turnout control if appropriate (eg. MERG)
            if (!useLastBit) {
                iNum = InstanceManager.getDefault(TurnoutManager.class).askNumControlBits(sName);
                if ((InstanceManager.getDefault(TurnoutManager.class).isNumControlBitsSupported(sName)) && (rangeBox.isSelected())) {
                    // Add a pop up here asking if the user wishes to use the same value for all
                    if (JOptionPane.showConfirmDialog(addFrame,
                            Bundle.getMessage("UseForAllTurnouts"), Bundle.getMessage("UseSetting"),
                            JOptionPane.YES_NO_OPTION) == 0) {
                        useLastBit = true;
                    }
                } else {
                    // as isNumControlBits is not supported, we will always use the same value.
                    useLastBit = true;
                }
            }
            if (iNum == 0) {
                // User specified more bits, but bits are not available - return without creating
                // Display message in statusBarLabel
                statusBarLabel.setText(Bundle.getMessage("WarningBitsNotSupported", lastSuccessfulAddress));
                statusBarLabel.setForeground(Color.red);
                return;
            } else {

                // Create the new turnout
                Turnout t;
                try {
                    t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(ex, sName); // displays message dialog to the user
                    return; // without creating
                }
                if ((uName != null) && !uName.isEmpty()) {
                    if (InstanceManager.getDefault(TurnoutManager.class).getByUserName(uName) == null) {
                        t.setUserName(uName);
                    } else if (!pref.getPreferenceState(getClassName(), "duplicateUserName")) {
                        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showErrorMessage(Bundle.getMessage("ErrorTitle"),
                                        Bundle.getMessage("ErrorDuplicateUserName", uName),
                                        getClassName(), "duplicateUserName", false, true);
                    }
                }

                t.setNumberOutputBits(iNum);
                // Ask about the type of turnout control if appropriate
                if (!useLastType) {
                    iType = InstanceManager.getDefault(TurnoutManager.class).askControlType(sName);
                    if ((InstanceManager.getDefault(TurnoutManager.class).isControlTypeSupported(sName)) && (rangeBox.isSelected())) {
                        if (JOptionPane.showConfirmDialog(addFrame,
                                Bundle.getMessage("UseForAllTurnouts"), Bundle.getMessage("UseSetting"),
                                JOptionPane.YES_NO_OPTION) == 0) // Add a pop up here asking if the uName wishes to use the same value for all
                        {
                            useLastType = true;
                        }
                    }
                }
                t.setControlType(iType);

                // add first and last names to statusMessage uName feedback string
                if (x == 0 || x == numberOfTurnouts - 1) {
                    statusMessage.append(" ").append(sName).append(" (").append(uName).append(")");
                }
                if (x == numberOfTurnouts - 2) {
                    statusMessage.append(" ").append(Bundle.getMessage("ItemCreateUpTo")).append(" ");
                }
                // only mention first and last of rangeBox added
            }
            if ((uName != null) && !uName.isEmpty()) {
                uName = nextName(uName);
            }

            // end of for loop creating rangeBox of Turnouts
        }

        // provide successfeedback to uName
        statusBarLabel.setText(statusMessage.toString());
        statusBarLabel.setForeground(Color.gray);

        pref.setComboBoxLastSelection(systemSelectionCombo, prefixBox.getSelectedItem().getMemo().getUserName()); // store user pref
        removePrefixBoxListener(prefixBox);
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    private String addEntryToolTip;

    /**
     * Activate Add a rangeBox option if manager accepts adding more than 1
     * Turnout and set a manager specific tooltip on the AddNewHardwareDevice
     * pane.
     */
    private void canAddRange(ActionEvent e) {
        rangeBox.setEnabled(false);
        log.debug("T Add box disabled");
        rangeBox.setSelected(false);
        if (prefixBox.getSelectedIndex() == -1) {
            prefixBox.setSelectedIndex(0);
        }
        Manager<Turnout> manager = prefixBox.getSelectedItem();
        assert manager != null;
        String systemPrefix = manager.getSystemPrefix();
        rangeBox.setEnabled(((TurnoutManager) manager).allowMultipleAdditions(systemPrefix));
        addEntryToolTip = manager.getEntryToolTip();
        // show sysName (HW address) field tooltip in the Add Turnout pane that matches system connection selected from combobox
        hardwareAddressTextField.setToolTipText(
                Bundle.getMessage("AddEntryToolTipLine1",
                        manager.getMemo().getUserName(),
                        Bundle.getMessage("Turnouts"),
                        addEntryToolTip));
        hardwareAddressValidator.setToolTipText(hardwareAddressTextField.getToolTipText());
        hardwareAddressValidator.verify(hardwareAddressTextField);
    }

    void handleCreateException(Exception ex, String sysName) {
        String err = Bundle.getMessage("ErrorBeanCreateFailed",
            InstanceManager.getDefault(TurnoutManager.class).getBeanTypeHandled(),sysName);
        if (ex.getMessage() != null) {
            statusBarLabel.setText(ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(addFrame,
                    ex.getLocalizedMessage(),
                    err,
                    JOptionPane.ERROR_MESSAGE);
        } else {
            statusBarLabel.setText(Bundle.getMessage("WarningInvalidRange"));
            JOptionPane.showMessageDialog(addFrame,
                    err + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                    err,
                    JOptionPane.ERROR_MESSAGE);
        }
        statusBarLabel.setForeground(Color.red);
    }

    private boolean noWarn = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassName() {
        return TurnoutTableAction.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessagePreferencesDetails() {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class)
                .setPreferenceItemDetails(getClassName(), "duplicateUserName", Bundle.getMessage("DuplicateUserNameWarn"));
        super.setMessagePreferencesDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleTurnoutTable");
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutTableAction.class);

}
