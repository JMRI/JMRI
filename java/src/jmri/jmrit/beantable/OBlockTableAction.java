package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.*;

//import jmri.TurnoutManager;
import jmri.jmrit.beantable.oblock.*;
import jmri.jmrit.logix.OBlock;
import jmri.InstanceManager;
//import jmri.swing.SystemNameValidator;
import jmri.util.JmriJFrame;
import jmri.util.gui.GuiLafPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks, OPaths and Portals
 *
 * @author Pete Cressman (C) 2009, 2010
 * @author Egbert Broerse (C) 2020
 */
public class OBlockTableAction extends AbstractTableAction<OBlock> {

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
    // tables created on demand
    BlockPathTableModel blockpaths;

    // edit frames
    //OBlockFrame oblockFrame;
//    PathTurnoutFrame ptFrame;
//    BlockpathFrame bpFrame;

    TableFrames tf;
    OBlockTableFrame otf;
    OBlockTablePanel otp;

    public OBlockTableAction(String actionName) {
        super(actionName);
        //includeAddButton = false; // not required as we override the actionPerformed method
    }

    public OBlockTableAction() {
        this(Bundle.getMessage("TitleOBlockTable"));
    }

    @Override
    public void addToFrame(BeanTableFrame f) {
        JButton addOblockButton = new JButton(Bundle.getMessage("ButtonAdd"));
        otp.addToBottomBox(addOblockButton);
        addOblockButton.addActionListener(this::addOBlockPressed);

//        JButton addPortalButton = new JButton(Bundle.getMessage("ButtonAddAudioSource"));
//        otp.addToBottomBox(addPortalButton);
//        addPortalButton.addActionListener(this::addPortalPressed);
    }

    /**
     * Open OBlock tables action handler.
     * @see jmri.jmrit.beantable.oblock.TableFrames
     * @param e menu action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        _tabbed = InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed();
        if (_tabbed && f == null) {
            f = new BeanTableFrame<>();
        }
        initTableFrames();
    }

    private void initTableFrames() {
        // initialise core OBlock Edit functionality
        tf = new TableFrames(); // tf contains OBlock methods and links to tableDataModels, is a JmriJFrame that must be hidden
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
            otf.pack();
            otf.setVisible(true);

            tf.setVisible(false); // hide the TableFrames container when _tabbed
            tf.makePrivateWindow();
            // removes tf "OBlock Tables..." from the Windows menu
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
        otp = new OBlockTablePanel(oblocks, portals, signals, blockportals, helpTarget());

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
        if (_tabbed) {
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
            //f.setTitle(jmri.jmrit.beantable.oblock.Bundle.getMessage("TitleOBlocks")); //TitleBlockTable = korter
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

    @Override
    protected void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }

    JmriJFrame addOBlockFrame = null;
    jmri.UserPreferencesManager pref;

    void addOBlockPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addOBlockFrame == null) {
            addOBlockFrame = new JmriJFrame(Bundle.getMessage("TitleAddOBlock"), false, true);
            addOBlockFrame.addHelpMenu("package.jmri.jmrit.beantable.OBlockAddEdit", true); // NOI18N
            addOBlockFrame.getContentPane().setLayout(new BoxLayout(addOBlockFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener oklistener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            };
            ActionListener cancellistener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            };
            //addOBlockFrame.add(new AddNewBeanPanel(sysName, userName, numberToAddSpinner, addRangeCheckBox, _autoSystemNameCheckBox, "ButtonCreate", oklistener, cancellistener, statusBar));
            //sysName.setToolTipText(Bundle.getMessage("SysNameToolTip", "B")); // override tooltip with bean specific letter

            addOBlockFrame.getContentPane().setLayout(new BoxLayout(addOBlockFrame.getContentPane(), BoxLayout.Y_AXIS));
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
            addButton.addActionListener(createListener);
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

    void cancelPressed(ActionEvent e) {
        //removePrefixBoxListener(prefixBox);
        addOBlockFrame.setVisible(false);
        addOBlockFrame.dispose();
        addOBlockFrame = null;
    }

    /**
     * Respond to Create new item button pressed on Add OBlock pane.
     *
     * @param e the click event
     */
    void createPressed(ActionEvent e) {
        // TODO adapt from TurnoutTableAction using methods in oblock.TableFrames
        // ...
//        addOBlockFrame.setVisible(false);
//        addOBlockFrame.dispose();
//        addOBlockFrame = null;
    }

    /**
     * Create or update the blockPathTableModel. Used in EditBlockPath pane.
     *
//     * @param block to build a table for
     */
//    private void setBlockPathTableModel(OBlock block) {
//        BlockPathTableModel blockPathTableModel = tf.getBlockPathTableModel(block);
//    }

    @Override
    public void dispose() {
        //jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setSimplePreferenceState(getClassName() + ":LengthUnitMetric", centimeterBox.isSelected());
        f.dispose();
        super.dispose();
    }

    @Override
    protected String getClassName() {
        return OBlockTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleOBlockTable");
    }

    //setMenuBar(f); // comes after the Help menu is added by f = new
    // BeanTableFrame(etc.) in stand alone application


//    @Override
//    public void addToPanel(AbstractTableTabAction<OBlock> f) {
//        // not used (checkboxes etc.)
//    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.OBlockTable";
    }

    private final static Logger log = LoggerFactory.getLogger(OBlockTableAction.class);

}
