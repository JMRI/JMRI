package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.UserPreferencesManager;
import jmri.jmrit.beantable.block.BlockTableDataModel;
import jmri.BlockManager;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a BlockTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 * @author Egbert Broerse Copyright (C) 2017
 */
public class BlockTableAction extends AbstractTableAction<Block> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName the Action title
     */
    public BlockTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary Block manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.BlockManager.class) == null) {
            BlockTableAction.this.setEnabled(false);
        }
    }

    public BlockTableAction() {
        this(Bundle.getMessage("TitleBlockTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Block objects.
     */
    @Override
    protected void createModel() {
        m = new BlockTableDataModel(getManager());
    }
    
    @Nonnull
    @Override
    protected Manager<Block> getManager() {
        return InstanceManager.getDefault(BlockManager.class);
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleBlockTable")); // NOI18N
    }

    private final JRadioButton inchBox = new JRadioButton(Bundle.getMessage("LengthInches")); // NOI18N
    private final JRadioButton centimeterBox = new JRadioButton(Bundle.getMessage("LengthCentimeters")); // NOI18N
    public final static String BLOCK_METRIC_PREF = BlockTableAction.class.getName() + ":LengthUnitMetric"; // NOI18N

    private void initRadioButtons(){
        
        inchBox.setToolTipText(Bundle.getMessage("InchBoxToolTip")); // NOI18N
        centimeterBox.setToolTipText(Bundle.getMessage("CentimeterBoxToolTip")); // NOI18N
        
        ButtonGroup group = new ButtonGroup();
        group.add(inchBox);
        group.add(centimeterBox);
        inchBox.setSelected(true);
        centimeterBox.setSelected( InstanceManager.getDefault(UserPreferencesManager.class)
            .getSimplePreferenceState(BLOCK_METRIC_PREF));
        
        inchBox.addActionListener(this::metricSelectionChanged);
        centimeterBox.addActionListener(this::metricSelectionChanged);
        
        // disabling keyboard input as when focused, does not fire actionlistener 
        // and appears selected causing mismatch with button selected and what the table thinks is selected.
        inchBox.setFocusable(false);
        centimeterBox.setFocusable(false);
    }
    
    /**
     * Add the radioButtons (only 1 may be selected).
     */
    @Override
    public void addToFrame(BeanTableFrame<Block> f) {
        initRadioButtons();
        f.addToBottomBox(inchBox, this.getClass().getName());
        f.addToBottomBox(centimeterBox, this.getClass().getName());
    }

    /**
     * Insert 2 table specific menus.
     * <p>
     * Account for the Window and Help menus,
     * which are already added to the menu bar as part of the creation of the
     * JFrame, by adding the menus 2 places earlier unless the table is part of
     * the ListedTableFrame, that adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame<Block> f) {
        final jmri.util.JmriJFrame finalF = f; // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenus before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = {}", pos);
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                    offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }

        JMenu pathMenu = new JMenu(Bundle.getMessage("MenuPaths"));
        JMenuItem item = new JMenuItem(Bundle.getMessage("MenuItemDeletePaths"));
        pathMenu.add(item);
        item.addActionListener((ActionEvent e) -> {
            deletePaths(finalF);
        });
        menuBar.add(pathMenu, pos + offset);

        JMenu speedMenu = new JMenu(Bundle.getMessage("SpeedsMenu"));
        item = new JMenuItem(Bundle.getMessage("SpeedsMenuItemDefaults"));
        speedMenu.add(item);
        item.addActionListener((ActionEvent e) -> {
            ((BlockTableDataModel)m).setDefaultSpeeds(finalF);
        });
        menuBar.add(speedMenu, pos + offset + 1); // put it to the right of the Paths menu
    }
    
    private void metricSelectionChanged(ActionEvent e) {
        InstanceManager.getDefault(UserPreferencesManager.class)
            .setSimplePreferenceState(BLOCK_METRIC_PREF, centimeterBox.isSelected());
        ((BlockTableDataModel)m).setMetric(centimeterBox.isSelected());
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.BlockTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(20);
    JTextField userName = new JTextField(20);
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));

    SpinnerNumberModel numberToAddSpinnerNumberModel = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(numberToAddSpinnerNumberModel);
    JCheckBox addRangeCheckBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JCheckBox _autoSystemNameCheckBox = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    JLabel statusBar = new JLabel(Bundle.getMessage("AddBeanStatusEnter"), JLabel.LEADING);
    private JButton newButton = null;

    @Override
    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddBlock"), false, true);
            addFrame.setEscapeKeyClosesWindow(true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.BlockAddEdit", true); // NOI18N
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener oklistener = this::okPressed;
            ActionListener cancellistener = this::cancelPressed;
            
            AddNewBeanPanel anbp = new AddNewBeanPanel(sysName, userName, numberToAddSpinner, addRangeCheckBox, _autoSystemNameCheckBox, "ButtonCreate", oklistener, cancellistener, statusBar); 
            addFrame.add(anbp);
            newButton = anbp.ok;
            sysName.setToolTipText(Bundle.getMessage("SysNameToolTip", "B")); // override tooltip with bean specific letter
        }
        sysName.setBackground(Color.white);
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
        statusBar.setForeground(Color.gray);
        if (InstanceManager.getDefault(jmri.UserPreferencesManager.class).getSimplePreferenceState(systemNameAuto)) {
            _autoSystemNameCheckBox.setSelected(true);
        }
        if (newButton!=null){
            addFrame.getRootPane().setDefaultButton(newButton);
        }
        addRangeCheckBox.setSelected(false);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    /**
     * Respond to Create new item pressed on Add Block pane.
     *
     * @param e the click event
     */
    void okPressed(ActionEvent e) {

        int numberOfBlocks = 1;

        if (addRangeCheckBox.isSelected()) {
            numberOfBlocks = (Integer) numberToAddSpinner.getValue();
        }
        if (numberOfBlocks >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Blocks"), numberOfBlocks),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String user = NamedBean.normalizeUserName(userName.getText());
        if (user == null || user.isEmpty()) {
            user = null;
        }
        String uName = user; // keep result separate to prevent recursive manipulation
        String system = "";

        if (!_autoSystemNameCheckBox.isSelected()) {
            system = InstanceManager.getDefault(jmri.BlockManager.class).makeSystemName(sysName.getText());
        }
        String sName = system; // keep result separate to prevent recursive manipulation
        // initial check for empty entry using the raw name
        if (sName.length() < 3 && !_autoSystemNameCheckBox.isSelected()) {  // Using 3 to catch a plain IB
            statusBar.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBar.setForeground(Color.red);
            sysName.setBackground(Color.red);
            return;
        } else {
            sysName.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the blockManager
        StringBuilder statusMessage = new StringBuilder(Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameBlock")));

        for (int x = 0; x < numberOfBlocks; x++) {
            if (x != 0) { // start at 2nd Block
                if (!_autoSystemNameCheckBox.isSelected()) {
                    // Find first block with unused system name
                    while (true) {
                        system = nextName(system);
                        // log.warn("Trying " + system);
                        Block blk = InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(system);
                        if (blk == null) {
                            sName = system;
                            break;
                        }
                    }
                }
                if (user != null) {
                    // Find first block with unused user name
                    while (true) {
                        user = nextName(user);
                        //log.warn("Trying " + user);
                        Block blk = InstanceManager.getDefault(jmri.BlockManager.class).getByUserName(user);
                        if (blk == null) {
                            uName = user;
                            break;
                        }
                    }
                }
            }
            Block blk;
            String xName = "";
            try {
                if (_autoSystemNameCheckBox.isSelected()) {
                    blk = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(uName);
                    if (blk == null) {
                        xName = uName;
                        throw new java.lang.IllegalArgumentException();
                    }
                } else {
                    blk = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(sName, uName);
                    if (blk == null) {
                        xName = sName;
                        throw new java.lang.IllegalArgumentException();
                    }
                }
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(xName);
                statusBar.setText(Bundle.getMessage("ErrorAddFailedCheck"));
                statusBar.setForeground(Color.red);
                return; // without creating
            }
            
            // add first and last names to statusMessage user feedback string
            if (x == 0 || x == numberOfBlocks - 1) {
                statusMessage.append(" ").append(sName).append(" (").append(user).append(")");
            }
            if (x == numberOfBlocks - 2) {
                statusMessage.append(" ").append(Bundle.getMessage("ItemCreateUpTo")).append(" ");
            }
            // only mention first and last of addRangeCheckBox added
        } // end of for loop creating addRangeCheckBox of Blocks

        // provide feedback to user
        statusBar.setText(statusMessage.toString());
        statusBar.setForeground(Color.gray);

        InstanceManager.getDefault(UserPreferencesManager.class)
            .setSimplePreferenceState(systemNameAuto, _autoSystemNameCheckBox.isSelected());
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorBlockAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }
    //private boolean noWarn = false;

    void deletePaths(jmri.util.JmriJFrame f) {
        // Set option to prevent the path information from being saved.

        Object[] options = {Bundle.getMessage("ButtonRemove"),
            Bundle.getMessage("ButtonKeep")};

        int retval = JOptionPane.showOptionDialog(f,
                Bundle.getMessage("BlockPathMessage"),
                Bundle.getMessage("BlockPathSaveTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (retval != 0) {
            InstanceManager.getDefault(jmri.BlockManager.class).setSavedPathInfo(true);
            log.info("Requested to save path information via Block Menu.");
        } else {
            InstanceManager.getDefault(jmri.BlockManager.class).setSavedPathInfo(false);
            log.info("Requested not to save path information via Block Menu.");
        }
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleBlockTable");
    }

    @Override
    protected String getClassName() {
        return BlockTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(BlockTableAction.class);

}
