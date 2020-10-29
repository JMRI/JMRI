package jmri.jmrit.beantable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.beantable.oblock.*;
import jmri.jmrit.logix.OBlock;
//import jmri.swing.SystemNameValidator;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.PortalManager;
import jmri.util.JmriJFrame;
import jmri.util.gui.GuiLafPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks, OPaths and Portals. Overrides some of the AbstractTableAction methods as this is a hybrid pane.
 * Relies on {@link jmri.jmrit.beantable.oblock.TableFrames}.
 *
 * @author Pete Cressman (C) 2009, 2010
 * @author Egbert Broerse (C) 2020
 */
public class OBlockTableAction extends AbstractTableAction<OBlock> implements PropertyChangeListener {

    // for tabs or desktop interface
    protected boolean _tabbed = false; // updated from prefs
    protected JPanel dataPanel;
    protected JTabbedPane dataTabs;
    protected boolean init = false;

    // basic table models
    OBlockTableModel oblocks;
    PortalTableModel portals;
    SignalTableModel signals;
    BlockPortalTableModel blockportals;
    // tables created on demand inside TableFrames:
    // - BlockPathTable(block)
    // - PathTurnoutTable(block)

    @Nonnull
    protected OBlockManager oblockManager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
    @Nonnull
    protected PortalManager portalManager = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class);

    TableFrames tf;
    OBlockTableFrame otf;
    OBlockTablePanel otp;

    // edit frames
    OBlockEditFrame oblockFrame;
    PortalEditFrame portalFrame;

    // on demand frames
//    PathTurnoutFrame ptFrame;
//    BlockpathFrame bpFrame;


    public OBlockTableAction(String actionName) {
        super(actionName);
        //includeAddButton = false; // not required as we override the actionPerformed method
    }

    public OBlockTableAction() {
        this(Bundle.getMessage("TitleOBlockTable"));
    }

    /**
     * Configure managers for all tabs on OBlocks table pane.
     * @param om the manager to assign
     */
    @Override
    public void setManager(@Nonnull Manager<OBlock> om) {
        if (oblockManager != null){
            oblockManager.removePropertyChangeListener(this);
        }
        if (om instanceof OBlockManager) {
            oblockManager = (OBlockManager) om;
            if (m != null) { // model
                m.setManager(oblockManager);
            }
        }
        if (oblockManager != null){
            oblockManager.addPropertyChangeListener(this);
        }
    }

    @Override
    public void addToFrame(BeanTableFrame f) {
        JButton addOblockButton = new JButton(Bundle.getMessage("ButtonAddOBlock"));
        otp.addToBottomBox(addOblockButton);
        addOblockButton.addActionListener(this::addOBlockPressed);

        JButton addPortalButton = new JButton(Bundle.getMessage("ButtonAddPortal"));
        otp.addToBottomBox(addPortalButton);
        addPortalButton.addActionListener(this::addPortalPressed);
    }

    /**
     * Open OBlock tables action handler.
     * @see jmri.jmrit.beantable.oblock.TableFrames
     * @param e menu action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
//        if (_tabbed && f == null) {
//            f = new BeanTableFrame<>();
//        }
        initTableFrames();
    }

    private void initTableFrames() {
        // initialise core OBlock Edit functionality
        tf = new TableFrames(); // tf contains OBlock Edit methods and links to tableDataModels, is a JmriJFrame that must be hidden
        tf.initComponents();
        // original simulated desktop interface is created in tf.initComponents() and takes care of itself if !_tabbed
        if (_tabbed) { // add the tables on a JTabbedPane, choose in Preferences > Display > GUI
            log.debug("Tabbed starting");
            // create the JTable model, with changes for specific NamedBean
            createModel();
            // create the frame
            otf = new OBlockTableFrame(otp, helpTarget()) {

                /**
                 * Include "Add OBlock..." and "Add XYZ..." buttons
                 */
                @Override
                void extras() {
                    addToFrame(this);
                }
            };
            setTitle();

            //tf.setParentFrame(otf); // needed?
            tf.makePrivateWindow(); // prevents tf "OBlock Tables..." to show up in the Windows menu
            tf.setVisible(false); // hide the TableFrames container when _tabbed

            otf.pack();
            otf.setVisible(true);
        } else {
            tf.setVisible(true);
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific NamedBean type.
     * Directly called bypassing actionPerformed(a) to prepare the Tables > OBlock Table entry in the left sidebar list
     */
    @Override
    protected void createModel() { // Tabbed
        if (tf == null) {
            initTableFrames();
        }
        oblocks = tf.getOblockTableModel();
        portals = tf.getPortalTableModel();
        signals = tf.getSignalTableModel();
        blockportals = tf.getPortalXRefTableModel();
        //blockpaths = tf.getBlockPathTableModel(block);
        otp = new OBlockTablePanel(oblocks, portals, signals, blockportals, tf, helpTarget());

        if (f == null) {
            f = new OBlockTableFrame(otp, helpTarget());
        }
        setMenuBar(f); // comes after the Help menu is added by f = new
        // BeanTableFrame(etc.) handled in stand alone application
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);

        init = true;
    }

    @Override
    public JPanel getPanel() {
        createModel();
        return otp;
    }

    /**
     * Include the correct title.
     */
    @Override
    protected void setTitle() {
        if (_tabbed && otf != null) {
            otf.setTitle(Bundle.getMessage("TitleOBlockTable"));
        }
    }

    @Override
    public void setMenuBar(BeanTableFrame f) {
        if (tf == null) {
            initTableFrames();
        }
        if (_tabbed) {
            final jmri.util.JmriJFrame finalF = f;   // needed for anonymous ActionListener class, see TurnoutTableAction
            JMenuBar menuBar = f.getJMenuBar();
            if (menuBar == null) {
                menuBar = new JMenuBar();
            }
            setTitle();
            f.setTitle(jmri.jmrit.beantable.oblock.Bundle.getMessage("TitleOBlocks")); //TitleBlockTable = korter
            f.setJMenuBar(tf.addMenus(menuBar));
            // or add separate items, actionhandlers?
            f.addHelpMenu("package.jmri.jmrit.logix.OBlockTable", true);
        }
        // check for menu
        //        boolean menuAbsent = true;
        //        for (int m = 0; m < menuBar.getMenuCount(); ++m) {
        //            String name = menuBar.getMenu(m).getAccessibleContext().getAccessibleName();
        //            if (name.equals(Bundle.getMessage("OptionMenu"))) {
        //                // using first menu for check, should be identical to next JMenu Bundle
        //                menuAbsent = false;
        //                break;
        //            }
        //        }
        //        if (menuAbsent) { // create it
        //            int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenu before 'Window' and 'Help'
        //            int offset = 1;
        //            log.debug("setMenuBar number of menu items = {}", pos);
        //            for (int i = 0; i <= pos; i++) {
        //                if (menuBar.getComponent(i) instanceof JMenu) {
        //                    if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
        //                        offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
        //                    }
        //                }
        //            }
        //            JMenu opsMenu = new JMenu(Bundle.getMessage("TurnoutAutomationMenu"));
        //            JMenuItem item = new JMenuItem(Bundle.getMessage("TurnoutAutomationMenuItemEdit"));
        //            opsMenu.add(item);
        //            item.addActionListener(new ActionListener() {
        //                @Override
        //                public void actionPerformed(ActionEvent e) {
        //                    new TurnoutOperationFrame(finalF);
        //                }
        //            });
        //            menuBar.add(opsMenu, pos + offset);
        //
        //            JMenu speedMenu = new JMenu(Bundle.getMessage("SpeedsMenu"));
        //            item = new JMenuItem(Bundle.getMessage("SpeedsMenuItemDefaults"));
        //            speedMenu.add(item);
        //            item.addActionListener(new ActionListener() {
        //                @Override
        //                public void actionPerformed(ActionEvent e) {
        //                    //setDefaultSpeeds(finalF);
        //                }
        //            });
        //            menuBar.add(speedMenu, pos + offset + 1); // add this menu to the right of the previous
        //            menuBar = f.getJMenuBar();
        //            menuBar.add(tf.getMenuBar());
//              }
    }

    void addPortalPressed(ActionEvent e) {
        if (portalFrame == null) {
            portalFrame = new PortalEditFrame(Bundle.getMessage("TitleAddPortal"), portals);
        }
        //portalFrame.updatePortalList();
        portalFrame.resetFrame();
        portalFrame.pack();
        portalFrame.setVisible(true);
    }

    @Override
    protected void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }

    jmri.UserPreferencesManager pref;
    JmriJFrame addOBlockFrame = null;

    JTextField sysNameField = new JTextField(20);
    JTextField userNameField = new JTextField(20);
//    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
//    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    JCheckBox rangeBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JCheckBox autoSystemNameBox = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    JLabel statusBarLabel = new JLabel(Bundle.getMessage("AddBeanStatusEnter"), JLabel.LEADING);
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    protected void addOBlockPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addOBlockFrame == null) {
            addOBlockFrame = new JmriJFrame(Bundle.getMessage("TitleAddOBlock"), false, true);
            addOBlockFrame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
            addOBlockFrame.getContentPane().setLayout(new BoxLayout(addOBlockFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = this::createObPressed;
            ActionListener cancelListener = this::cancelObPressed;
            addOBlockFrame.add(new AddNewBeanPanel(sysNameField, userNameField, numberToAddSpinner, rangeBox, autoSystemNameBox, "ButtonCreate", okListener, cancelListener, statusBarLabel));
            sysNameField.setToolTipText(Bundle.getMessage("SysNameToolTip", "B")); // override tooltip with bean specific letter
        }
        sysNameField.setBackground(Color.white);
        // reset status bar text
        statusBarLabel.setText(Bundle.getMessage("AddBeanStatusEnter"));
        statusBarLabel.setForeground(Color.gray);
        if (pref.getSimplePreferenceState(systemNameAuto)) {
            autoSystemNameBox.setSelected(true);
        }
        addOBlockFrame.pack();
        addOBlockFrame.setVisible(true);
    }

    // heavier copy from ??
    void addOBlock2Pressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addOBlockFrame == null) {
            addOBlockFrame = new JmriJFrame(Bundle.getMessage("TitleAddOBlock"), false, true);
            addOBlockFrame.addHelpMenu("package.jmri.jmrit.beantable.OBlockAddEdit", true); // NOI18N
            addOBlockFrame.getContentPane().setLayout(new BoxLayout(addOBlockFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener oklistener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createObPressed(e);
                }
            };
            ActionListener cancellistener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelObPressed(e);
                }
            };
            //addOBlockFrame.add(new AddNewBeanPanel(sysName, userName, numberToAddSpinner, addRangeCheckBox, _autoSystemNameCheckBox, "ButtonCreate", oklistener, cancellistener, statusBar));
            //sysName.setToolTipText(Bundle.getMessage("SysNameToolTip", "B")); // override tooltip with bean specific letter

            addOBlockFrame.getContentPane().setLayout(new BoxLayout(addOBlockFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener createlistener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createObPressed(e);
                }
            };
//            ActionListener cancellistener = new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    cancelObPressed(e);
//                }
//            };
            //            ActionListener rangeListener = new ActionListener() { // add rangeBox box turned on/off
            //                @Override
            //                public void actionPerformed(ActionEvent e) {
            //                    canAddRange(e);
            //                }
            //            };
            /* We use the proxy manager in this instance so that we can deal with
             duplicate usernames in multiple classes */
            //            configureManagerComboBox(prefixBox, turnoutManager, TurnoutManager.class);
            //            userNameTextField.setName("userNameTextField"); // NOI18N
            //            prefixBox.setName("prefixBox"); // NOI18N
            // set up validation, zero text = false
            JButton addButton = new JButton(Bundle.getMessage("ButtonCreate"));
            //addButton.addActionListener(createListener);
            // create panel
            //            hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, prefixBox.getSelectedItem(), true);
            //            addOBlockFrame.add(new AddNewHardwareDevicePanel(hardwareAddressTextField, hardwareAddressValidator, userNameTextField, prefixBox,
            //                    numberToAddSpinner, rangeBox, addButton, cancelListener, rangeListener, statusBarLabel));
            //            // tooltip for hardwareAddressTextField will be assigned next by canAddRange()
            //            canAddRange(null);
            //        }
            //        hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
            addButton.setName("createButton"); // for GUI test NOI18N
            // reset statusBarLabel text
            //        statusBarLabel.setText(Bundle.getMessage("HardwareAddStatusEnter"));
            //        statusBarLabel.setForeground(Color.gray);

//            sysName.setBackground(Color.white);
//            // reset statusBar text
//            statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
//            statusBar.setForeground(Color.gray);
//            if (pref.getSimplePreferenceState(systemNameAuto)) {
//                _autoSystemNameCheckBox.setSelected(true);
//            }
//            addRangeCheckBox.setSelected(false);

            addOBlockFrame.pack();
            addOBlockFrame.setVisible(true);
        }
        //oblockFrame.updateOblockList(); // see AudioTableAction
        //oblockFrame.resetFrame();
    }

    void cancelObPressed(ActionEvent e) {
        //removePrefixBoxListener(prefixBox);
        addOBlockFrame.setVisible(false);
        addOBlockFrame.dispose();
        addOBlockFrame = null;
    }

    /**
     * Respond to Create new item button pressed on Add OBlock pane. Part of Add2 alternative
     *
     * @param e the click event
     */
    void createObPressed(ActionEvent e) {
        int numberOfOblocks = 1;

        if (rangeBox.isSelected()) {
            numberOfOblocks = (Integer) numberToAddSpinner.getValue();
        }

        if (numberOfOblocks >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addOBlockFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("OBlocks"), numberOfOblocks),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }

        String uName = NamedBean.normalizeUserName(userNameField.getText());
        if (uName == null || uName.isEmpty()) {
            uName = null;
        }
        String sName = sysNameField.getText();
        // initial check for empty entry
        if (sName.isEmpty() && !autoSystemNameBox.isSelected()) {
            statusBarLabel.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBarLabel.setForeground(Color.red);
            sysNameField.setBackground(Color.red);
            return;
        } else {
            sysNameField.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the memoryManager
        StringBuilder statusMessage = new StringBuilder(Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameOBlock")));
        String errorMessage = null;
        for (int x = 0; x < numberOfOblocks; x++) {
            if (uName != null && !uName.isEmpty() && jmri.InstanceManager.memoryManagerInstance().getByUserName(uName) != null && !pref.getPreferenceState(getClassName(), "duplicateUserName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", uName), getClassName(), "duplicateUserName", false, true);
                // show in status bar
                errorMessage = Bundle.getMessage("ErrorDuplicateUserName", uName);
                statusBarLabel.setText(errorMessage);
                statusBarLabel.setForeground(Color.red);
                uName = null; // new Memory objects always receive a valid system name using the next free index, but uName names must not be in use so use none in that case
            }
            if (!sName.isEmpty() && jmri.InstanceManager.memoryManagerInstance().getBySystemName(sName) != null && !pref.getPreferenceState(getClassName(), "duplicateSystemName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateSystemName", sName), getClassName(), "duplicateSystemName", false, true);
                // show in status bar
                errorMessage = Bundle.getMessage("ErrorDuplicateSystemName", sName);
                statusBarLabel.setText(errorMessage);
                statusBarLabel.setForeground(Color.red);
                return; // new Memory objects are always valid, but system names must not be in use so skip in that case
            }
            try {
                if (autoSystemNameBox.isSelected()) {
                    InstanceManager.getDefault(OBlockManager.class).createNewOBlock(uName, "");
                } else {
                    InstanceManager.getDefault(OBlockManager.class).createNewOBlock(sName, uName);
                }
            } catch (IllegalArgumentException ex) {
                // uName input no good
                handleCreateException(sName);
                errorMessage = "An error has occurred";
                statusBarLabel.setText(errorMessage);
                statusBarLabel.setForeground(Color.red);
                return; // without creating
            }

            // add first and last names to statusMessage uName feedback string
            // only mention first and last of rangeBox added
            if (x == 0 || x == numberOfOblocks - 1) {
                statusMessage.append(" ").append(sName).append(" (").append(uName).append(")");
            }
            if (x == numberOfOblocks - 2) {
                statusMessage.append(" ").append(Bundle.getMessage("ItemCreateUpTo")).append(" ");
            }

            // bump system & uName names
            if (!autoSystemNameBox.isSelected()) {
                sName = nextName(sName);
            }
            if (uName != null) {
                uName = nextName(uName);
            }
        } // end of for loop creating rangeBox of Memories

        // provide feedback to uName
        if (errorMessage == null) {
            statusBarLabel.setText(statusMessage.toString());
            statusBarLabel.setForeground(Color.gray);
        } else {
            statusBarLabel.setText(errorMessage);
            // statusBarLabel.setForeground(Color.red); // handled when errorMassage is set to differentiate urgency
        }

        pref.setSimplePreferenceState(systemNameAuto, autoSystemNameBox.isSelected());
        // Notify changes
        oblocks.fireTableDataChanged();
    }


    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addOBlockFrame,
                Bundle.getMessage("ErrorOBlockAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Create or update the blockPathTableModel. Used in EditBlockPath pane.
     *
//     * @param block to build a table for
     */
//    private void setBlockPathTableModel(OBlock block) {
//        BlockPathTableModel blockPathTableModel = tf.getBlockPathTableModel(block);
//    }

//    @Override // loops with ListedTableItem.dispose()
//    public void dispose() {
//        //jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setSimplePreferenceState(getClassName() + ":LengthUnitMetric", centimeterBox.isSelected());
//        f.dispose();
//        super.dispose();
//    }

    @Override
    protected String getClassName() {
        return OBlockTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleOBlockTable");
    }

//    @Override
//    public void addToPanel(AbstractTableTabAction<OBlock> f) {
//        // not used (checkboxes etc.)
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (log.isDebugEnabled()) {
            log.debug("PropertyChangeEvent property = {} source= {}", property, e.getSource().getClass().getName());
        }
        switch (property) {
            case "StateStored":
                //isStateStored.setSelected(oblockManager.isStateStored());
                break;
            case "UseFastClock":
                //isFastClockUsed.setSelected(portalManager.isFastClockUsed());
                break;
        }
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.OBlockTable";
    }

    private final static Logger log = LoggerFactory.getLogger(OBlockTableAction.class);

}
