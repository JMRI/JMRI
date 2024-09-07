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
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.PortalManager;
import jmri.util.JmriJFrame;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.JmriJOptionPane;

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
    protected OBlockManager oblockManager = InstanceManager.getDefault(OBlockManager.class);
    @Nonnull
    protected PortalManager portalManager = InstanceManager.getDefault(PortalManager.class);

    TableFrames tf;
    OBlockTableFrame otf;
    OBlockTablePanel otp;

    // edit frames
    //OBlockEditFrame oblockFrame; // instead we use NewBean util + Edit
    PortalEditFrame portalFrame;
    SignalEditFrame signalFrame;
    // on demand frames
    //    PathTurnoutFrame ptFrame; created from TableFrames
    //    BlockPathEditFrame bpFrame; created from TableFrames

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public OBlockTableAction(String actionName) {
        super(actionName);
        includeAddButton = false; // not required per se as we override the actionPerformed method
    }

    /**
     * Default constructor
     */
    public OBlockTableAction() {
        this(Bundle.getMessage("TitleOBlockTable"));
    }

    /**
     * Configure managers for all tabs on OBlocks table pane.
     * @param om the manager to assign
     */
    @Override
    public void setManager(@Nonnull Manager<OBlock> om) {
        oblockManager.removePropertyChangeListener(this);
        if (om instanceof OBlockManager) {
            oblockManager = (OBlockManager) om;
            if (m != null) { // model
                m.setManager(oblockManager);
            }
        }
        oblockManager.addPropertyChangeListener(this);
    }

    // add the 3 buttons to add new OBlock, Portal, Signal
    @Override
    public void addToFrame(@Nonnull BeanTableFrame<OBlock> f) {
        JButton addOblockButton = new JButton(Bundle.getMessage("ButtonAddOBlock"));
        otp.addToBottomBox(addOblockButton);
        addOblockButton.addActionListener(this::addOBlockPressed);

        JButton addPortalButton = new JButton(Bundle.getMessage("ButtonAddPortal"));
        otp.addToBottomBox(addPortalButton);
        addPortalButton.addActionListener(this::addPortalPressed);

        JButton addSignalButton = new JButton(Bundle.getMessage("ButtonAddSignal"));
        otp.addToBottomBox(addSignalButton);
        addSignalButton.addActionListener(this::addSignalPressed);
    }

    /**
     * Open OBlock tables action handler.
     * @see jmri.jmrit.beantable.oblock.TableFrames
     * @param e menu action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        initTableFrames();
    }

    private void initTableFrames() {
        // initialise core OBlock Edit functionality
        tf = new TableFrames(); // tf contains OBlock Edit methods and links to tableDataModels, is a JmriJFrame that must be hidden

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
                    addToFrame(this); //creates multiple sets, wrong level to call
                }
            };
            setTitle();

            //tf.setParentFrame(otf); // needed?
            //tf.makePrivateWindow(); // prevents tf "OBlock Tables..." to show up in the Windows menu
            //tf.setVisible(false); // hide the TableFrames container when _tabbed

            otf.pack();
            otf.setVisible(true);
        } else {
            tf.initComponents();
            // original simulated desktop interface is created in tf.initComponents() and takes care of itself if !_tabbed
            // only required for _desktop, creates InternalFrames
            //tf.setVisible(true);
        }
    }

    /**
     * Create the JTable DataModel, along with the extra stuff for this specific NamedBean type.
     * Is directly called to prepare the Tables &gt; OBlock Table entry in the left sidebar list, bypassing actionPerformed(a)
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

        otp = new OBlockTablePanel(oblocks, portals, signals, blockportals, tf, helpTarget());

//        if (f == null) {
//            f = new OBlockTableFrame(otp, helpTarget());
//        }
//        setMenuBar(f); // comes after the Help menu is added by f = new
        // BeanTableFrame(etc.) handled in stand alone application
//        setTitle(); // TODO see if some of this is required or should be turned off to prevent/hide the stray JFrame that opens
//        addToFrame(f);
//        f.pack();
//        f.setVisible(true); <--- another empty pane!

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
            otf.setTitle(Bundle.getMessage("TitleOBlocksTabbedFrame"));
        }
    }

    @Override
    public void setMenuBar(BeanTableFrame<OBlock> f) {
        if (_tabbed) {
            //final JmriJFrame finalF = f;   // needed for anonymous ActionListener class on dialogs, see TurnoutTableAction ?
            JMenuBar menuBar = f.getJMenuBar();
            if (menuBar == null) {
                log.debug("NULL MenuBar");
                return;
            }
            MenuElement[] subElements;
            JMenu fileMenu = null;
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                if (menuBar.getComponent(i) instanceof JMenu) {
                    if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuFile"))) {
                        fileMenu = menuBar.getMenu(i);
                    }
                }
            }
            if (fileMenu == null) {
                log.debug("NULL FileMenu");
                return;
            }
            subElements = fileMenu.getSubElements();
            for (MenuElement subElement : subElements) {
                MenuElement[] popsubElements = subElement.getSubElements();
                for (MenuElement popsubElement : popsubElements) {
                    if (popsubElement instanceof JMenuItem) {
                        if (((JMenuItem) popsubElement).getText().equals(Bundle.getMessage("PrintTable"))) {
                            JMenuItem printMenu = (JMenuItem) popsubElement;
                            fileMenu.remove(printMenu);
                            break;
                        }
                    }
                }
            }
            fileMenu.add(otp.getPrintItem());

            menuBar.add(otp.getOptionMenu());
            menuBar.add(otp.getTablesMenu());
            log.debug("setMenuBar for OBLOCKS");

            // check for menu (copied from TurnoutTableAction)
//            boolean menuAbsent = true;
//            for (int m = 0; m < menuBar.getMenuCount(); ++m) {
//                String name = menuBar.getMenu(m).getAccessibleContext().getAccessibleName();
//                if (name.equals(Bundle.getMessage("MenuOptions"))) {
//                    // using first menu for check, should be identical to next JMenu Bundle
//                    menuAbsent = false;
//                    break;
//                }
//            }
//            if (menuAbsent) { // create it
//                int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenu before 'Window' and 'Help'
//                int offset = 1;
//                log.debug("setMenuBar number of menu items = {}", pos);
//                for (int i = 0; i <= pos; i++) {
//                    if (menuBar.getComponent(i) instanceof JMenu) {
//                        if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
//                            offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
//                        }
//                    }
//                }
                // add separate items, actionhandlers? next 2 menuItem examples copied from TurnoutTableAction

        //            JMenuItem item = new JMenuItem(Bundle.getMessage("TurnoutAutomationMenuItemEdit"));
        //            opsMenu.add(item);
        //            item.addActionListener(new ActionListener() {
        //                @Override
        //                public void actionPerformed(ActionEvent e) {
        //                    new TurnoutOperationFrame(finalF);
        //                }
        //            });
        //            menuBar.add(opsMenu, pos + offset); // test
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
        //    }
            f.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        }
    }

    JTextField startAddress = new JTextField(10);
    JTextField userName = new JTextField(40);
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    JCheckBox rangeBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JCheckBox autoSystemNameBox = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    JLabel statusBar = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    jmri.UserPreferencesManager pref;
    JmriJFrame addOBlockFrame = null;
    // for prefs persistence
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    // Three [Addx...] buttons on tabbed bottom box handlers
    
    @Override
    protected void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }

    protected void addOBlockPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        if (addOBlockFrame == null) {
            addOBlockFrame = new JmriJFrame(Bundle.getMessage("TitleAddOBlock"), false, true);
            addOBlockFrame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
            addOBlockFrame.getContentPane().setLayout(new BoxLayout(addOBlockFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = this::createObPressed;
            ActionListener cancelListener = this::cancelObPressed;

            AddNewBeanPanel anbp = new AddNewBeanPanel(startAddress, userName, numberToAddSpinner, rangeBox, autoSystemNameBox, "ButtonCreate", okListener, cancelListener, statusBar);
            addOBlockFrame.add(anbp);
            addOBlockFrame.getRootPane().setDefaultButton(anbp.ok);
            addOBlockFrame.setEscapeKeyClosesWindow(true);
            startAddress.setToolTipText(Bundle.getMessage("SysNameToolTip", "OB")); // override tooltip with bean specific letter
        }
        startAddress.setBackground(Color.white);
        // reset status bar text
        status(Bundle.getMessage("AddBeanStatusEnter"), false);
        if (pref.getSimplePreferenceState(systemNameAuto)) {
            autoSystemNameBox.setSelected(true);
        }
        addOBlockFrame.pack();
        addOBlockFrame.setVisible(true);
    }

    void cancelObPressed(ActionEvent e) {
        addOBlockFrame.setVisible(false);
        addOBlockFrame.dispose();
        addOBlockFrame = null;
    }

    /**
     * Respond to Create new OBlock button pressed on Add OBlock pane.
     * Adapted from {@link MemoryTableAction#addPressed(ActionEvent)}
     *
     * @param e the click event
     */
    void createObPressed(ActionEvent e) {
        int numberOfOblocks = 1;

        if (rangeBox.isSelected()) {
            numberOfOblocks = (Integer) numberToAddSpinner.getValue();
        }

        if (numberOfOblocks >= 65 // limited by JSpinnerModel to 100
            && JmriJOptionPane.showConfirmDialog(addOBlockFrame,
                Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("OBlocks"), numberOfOblocks),
                Bundle.getMessage("WarningTitle"),
                JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION ) {
                return;
        }

        String uName = NamedBean.normalizeUserName(userName.getText());
        if (uName != null && uName.isEmpty()) {
            uName = null;
        }
        String sName = startAddress.getText().trim();
        // initial check for empty entries
        if (autoSystemNameBox.isSelected()) {
            startAddress.setBackground(Color.white);
        } else if (sName.equals("")) {
            status(Bundle.getMessage("WarningSysNameEmpty"), true);
            startAddress.setBackground(Color.red);
            return;
        } else if (!sName.startsWith("OB")) {
            sName = "OB" + sName;
        }
        // Add some entry pattern checking, before assembling sName and handing it to the OBlockManager
        StringBuilder statusMessage = new StringBuilder(Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameOBlock")));
        String errorMessage = null;
        for (int x = 0; x < numberOfOblocks; x++) {
            if (uName != null && !uName.isEmpty() && oblockManager.getByUserName(uName) != null && !pref.getPreferenceState(getClassName(), "duplicateUserName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", uName), getClassName(), "duplicateUserName", false, true);
                // show in status bar
                errorMessage = Bundle.getMessage("ErrorDuplicateUserName", uName);
                status(errorMessage, true);
                uName = null; // new OBlock objects always receive a valid system name using the next free index, but uName names must not be in use so use none in that case
            }
            if (!sName.isEmpty() && oblockManager.getBySystemName(sName) != null && !pref.getPreferenceState(getClassName(), "duplicateSystemName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateSystemName", sName), getClassName(), "duplicateSystemName", false, true);
                // show in status bar
                errorMessage = Bundle.getMessage("ErrorDuplicateSystemName", sName);
                status(errorMessage, true);
                return; // new OBlock objects are always valid, but system names must not be in use so skip in that case
            }
            OBlock oblk;
            String xName = "";
            try {
                if (autoSystemNameBox.isSelected()) {
                    assert uName != null;
                    oblk = oblockManager.createNewOBlock(uName);
                    if (oblk == null) {
                        xName = uName;
                        throw new java.lang.IllegalArgumentException();
                    }
                } else {
                    oblk = oblockManager.createNewOBlock(sName, uName);
                    if (oblk == null) {
                        xName = sName;
                        throw new java.lang.IllegalArgumentException();
                    }
                }
            } catch (IllegalArgumentException ex) {
                // uName input no good
                handleCreateException(xName);
                errorMessage = Bundle.getMessage("ErrorAddFailedCheck");
                status(errorMessage, true);
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
        } // end of for loop creating rangeBox of OBlocks

        // provide feedback to uName
        if (errorMessage == null) {
            status(statusMessage.toString(), false);
            // statusBar.setForeground(Color.red); handled when errorMassage is set to differentiate urgency
        }

        pref.setSimplePreferenceState(systemNameAuto, autoSystemNameBox.isSelected());
        // Notify changes
        oblocks.fireTableDataChanged();
    }

    void addPortalPressed(ActionEvent e) {
        if (portalFrame == null) {
            portalFrame = new PortalEditFrame(Bundle.getMessage("TitleAddPortal"), null, portals);
        }
        //portalFrame.updatePortalList();
        portalFrame.resetFrame();
        portalFrame.pack();
        portalFrame.setVisible(true);
    }

    void addSignalPressed(ActionEvent e) {
        if (!signals.editMode()) {
            signals.setEditMode(true);
            if (signalFrame == null) {
                signalFrame = new SignalEditFrame(Bundle.getMessage("TitleAddSignal"), null, null, signals);
            }
            //signalFrame.updateSignalList();
            signalFrame.resetFrame();
            signalFrame.pack();
            signalFrame.setVisible(true);
        }
    }

    void handleCreateException(String sysName) {
        JmriJOptionPane.showMessageDialog(addOBlockFrame,
                Bundle.getMessage("ErrorOBlockAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JmriJOptionPane.ERROR_MESSAGE);
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
            default:
                //isFastClockUsed.setSelected(portalManager.isFastClockUsed());
                break;
        }
    }

    void status(String message, boolean warn){
        statusBar.setText(message);
        statusBar.setForeground(warn ? Color.red : Color.gray);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.OBlockTable";
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OBlockTableAction.class);

}
