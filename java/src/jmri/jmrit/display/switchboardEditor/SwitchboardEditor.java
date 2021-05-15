package jmri.jmrit.display.switchboardEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.annotation.Nonnull;
//import javax.annotation.concurrent.GuardedBy;
import javax.swing.*;
import javax.swing.border.TitledBorder;

//import com.alexandriasoftware.swing.Validation;
import jmri.*;
import jmri.jmrit.display.CoordinateEdit;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrix.SystemConnectionMemoManager;
import jmri.swing.ManagerComboBox;
//import jmri.swing.SystemNameValidator;
import jmri.util.ColorUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriColorChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jmri.util.ColorUtil.contrast;

/**
 * Provides a simple editor for adding jmri.jmrit.display.switchBoard items to a
 * JLayeredPane inside a captive JFrame. Primary use is for new users.
 * <p>
 * GUI is structured as a separate setup panel to set the visible range and type
 * plus menus.
 * <p>
 * All created objects are placed in a GridLayout grid. No special use of the
 * LayeredPane layers. Inspired by Oracle JLayeredPane demo.
 * <p>
 * The "switchesOnBoard" LinkedHashMap keeps track of all the objects added to the target
 * frame for later manipulation. May be used in an update to store mixed
 * switchboards with more than 1 connection and more than 1 bean type/range.<br>
 * The 'ready' flag protects the map during regeneration.
 * <p>
 * No DnD as panels will be automatically populated in order of the DCC address.
 * New beans may be created from the Switchboard by right clicking an
 * unconnected switch.
 * TODO allow user entry of connection specific starting name, validated in manager
 * using hardwareAddressValidator
 *
 * @author Pete Cressman Copyright (c) 2009, 2010, 2011
 * @author Egbert Broerse Copyright (c) 2017, 2018, 2021
 */
public class SwitchboardEditor extends Editor {

    protected JMenuBar _menuBar;
    private JMenu _editorMenu;
    //protected JMenu _editMenu;
    protected JMenu _fileMenu;
    protected JMenu _optionMenu;
    private transient boolean panelChanged = false;

    // Switchboard items
    ImageIcon iconPrev = new ImageIcon("resources/icons/misc/gui3/LafLeftArrow_m.gif");
    private final JLabel prev = new JLabel(iconPrev);
    ImageIcon iconNext = new ImageIcon("resources/icons/misc/gui3/LafRightArrow_m.gif");
    private final JLabel next = new JLabel(iconNext);
    private final int rangeBottom = 1;
    private final int rangeTop = 100000; // for MERG etc where thousands = node number, total number on board limited to unconnectedRangeLimit anyway
    private final static int unconnectedRangeLimit = 400;
    private final static int rangeSizeWarning = 250;
    private final static int initialMax = 24;
    private final JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(rangeBottom, rangeBottom, rangeTop - 1, 1));
    private final JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(initialMax, rangeBottom + 1, rangeTop, 1));
    private final JCheckBox hideUnconnected = new JCheckBox(Bundle.getMessage("CheckBoxHideUnconnected"));
    private final JCheckBox autoItemRange = new JCheckBox(Bundle.getMessage("CheckBoxAutoItemRange"));
    private JButton allOffButton;
    private JButton allOnButton;
    private TargetPane switchboardLayeredPane; // is a JLayeredPane
    static final String TURNOUT = Bundle.getMessage("Turnouts");
    static final String SENSOR = Bundle.getMessage("Sensors");
    static final String LIGHT = Bundle.getMessage("Lights");
    private final String[] beanTypeStrings = {TURNOUT, SENSOR, LIGHT};
    private JComboBox<String> beanTypeList;
    private String _type = TURNOUT;
    private final String[] switchShapeStrings = {
        Bundle.getMessage("Buttons"),
        Bundle.getMessage("Sliders"),
        Bundle.getMessage("Keys"),
        Bundle.getMessage("Symbols")
    };
    private JComboBox<String> shapeList;
    final static int BUTTON = 0;
    final static int SLIDER = 1;
    final static int KEY = 2;
    final static int SYMBOL = 3;
    //final static int ICON = 4;
    ManagerComboBox<Turnout> turnoutManComboBox = new ManagerComboBox<>();
    ManagerComboBox<Sensor> sensorManComboBox = new ManagerComboBox<>();
    ManagerComboBox<Light> lightManComboBox = new ManagerComboBox<>();
    protected TurnoutManager turnoutManager = InstanceManager.getDefault(TurnoutManager.class);
    protected SensorManager sensorManager = InstanceManager.getDefault(SensorManager.class);
    protected LightManager lightManager = InstanceManager.getDefault(LightManager.class);
    private SystemConnectionMemo memo;
    private int shape = BUTTON; // for: button
    //SystemNameValidator hardwareAddressValidator;
    JTextField addressTextField = new JTextField(10);
    private TitledBorder border;
    private final String interact = Bundle.getMessage("SwitchboardInteractHint");
    private final String noInteract = Bundle.getMessage("SwitchboardNoInteractHint");

    // editor items (adapted from LayoutEditor toolbar)
    private Color defaultTextColor = Color.BLACK;
    private Color defaultActiveColor = Color.RED; // user configurable since 4.21.3
    protected final static Color darkActiveColor = new Color(180, 50, 50);
    private Color defaultInactiveColor = Color.GREEN; // user configurable since 4.21.3
    protected final static Color darkInactiveColor = new Color(40, 150, 30);
    private boolean _hideUnconnected = false;
    private boolean _autoItemRange = true;
    private int rows = 4; // matches initial autoRows pref for default pane size
    private final float cellProportion = 1.0f; // TODO analyse actual W:H per switch type/shape: worthwhile?
    private int _tileSize = 100;
    private int _iconSquare = 75;
    // tmp @GuardedBy("this")
    private final JSpinner rowsSpinner = new JSpinner(new SpinnerNumberModel(rows, 1, 25, 1));
    private final JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
    // number of rows displayed on switchboard, disabled when autoRows is on
    private final JTextArea help2 = new JTextArea(Bundle.getMessage("Help2"));
    private final JTextArea help3 = new JTextArea(Bundle.getMessage("Help3", Bundle.getMessage("CheckBoxHideUnconnected")));
    // saved state of options when panel was loaded or created
    private transient boolean savedEditMode = true;
    private transient boolean savedControlLayout = true; // menu option to turn this off
    private final int height = 455;
    private final int width = 544;
    private int verticalMargin = 55; // for Nimbus and CDE/Motif

    private final JCheckBoxMenuItem controllingBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxControlling"));
    private final JCheckBoxMenuItem hideUnconnectedBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxHideUnconnected"));
    private final JCheckBoxMenuItem autoItemRangeBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxAutoItemRange"));
    private final JCheckBoxMenuItem showToolTipBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxShowTooltips"));
    //tmp @GuardedBy("this")
    private final JCheckBoxMenuItem autoRowsBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxAutoRows"));
    private final JCheckBoxMenuItem showUserNameBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxUserName"));
    private final JRadioButtonMenuItem scrollBoth = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
    private final JRadioButtonMenuItem scrollNone = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
    private final JRadioButtonMenuItem scrollHorizontal = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
    private final JRadioButtonMenuItem scrollVertical = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));
    private final JRadioButtonMenuItem sizeSmall = new JRadioButtonMenuItem(Bundle.getMessage("optionSmaller"));
    private final JRadioButtonMenuItem sizeDefault = new JRadioButtonMenuItem(Bundle.getMessage("optionDefault"));
    private final JRadioButtonMenuItem sizeLarge = new JRadioButtonMenuItem(Bundle.getMessage("optionLarger"));
    final static int SIZE_MIN = 50;
    final static int SIZE_INIT = 100;
    final static int SIZE_MAX = 150;

    /**
     * To count number of displayed beanswitches, this array holds all beanswitches to be displayed
     * until the GridLayout is configured, used to determine the total number of items to be placed.
     * Accounts for "hide unconnected" setting, so it can be empty. Not synchronized for risk of locking up.
     */
    private final LinkedHashMap<String, BeanSwitch> switchesOnBoard = new LinkedHashMap<>();
    private volatile boolean ready = true;

    /**
     * Ctor
     */
    public SwitchboardEditor() {
    }

    /**
     * Ctor by a given name.
     *
     * @param name title to assign to the new SwitchBoard
     */
    public SwitchboardEditor(String name) {
        super(name, false, true);
        init(name);
    }

    /**
     * Initialize the newly created Switchboard.
     *
     * @param name the title of the switchboard content frame
     */
    @Override
    protected void init(String name) {
        //memo = SystemConnectionMemoManager.getDefault().getSystemConnectionMemoForUserName("Internal");
        // always available (?) and supports all types, not required now, will be set by listener

        Container contentPane = getContentPane(); // the actual Editor configuration pane
        setVisible(false);      // start with Editor window hidden
        setUseGlobalFlag(true); // always true for a Switchboard
        // handle Editor close box clicked without deleting the Switchboard panel
        super.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        super.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                log.debug("switchboardEditor close box selected");
                setAllEditable(false);
                setVisible(false); // hide Editor window
            }
        });
        // make menus
        _menuBar = new JMenuBar();
        makeOptionMenu();
        makeFileMenu();

        setJMenuBar(_menuBar);
        addHelpMenu("package.jmri.jmrit.display.SwitchboardEditor", true);
        // set GUI dependant margin if not Nimbus, CDE/Motif (or undefined)
        if (UIManager.getLookAndFeel() != null) {
            if (UIManager.getLookAndFeel().getName().equals("Metal")) {
                verticalMargin = 47;
            } else if (UIManager.getLookAndFeel().getName().equals("Mac OS X")) {
                verticalMargin = 25;
            }
        }
        switchboardLayeredPane = new TargetPane(); // extends JLayeredPane();
        switchboardLayeredPane.setPreferredSize(new Dimension(width, height));
        border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(defaultTextColor),
                "temp",
                TitledBorder.LEADING,
                TitledBorder.ABOVE_BOTTOM,
                getFont(),
                defaultTextColor);
        switchboardLayeredPane.setBorder(border);
        // create contrast with background, should also specify border style
        // specify title for turnout, sensor, light, mixed? (wait for the Editor to be created)
        switchboardLayeredPane.addMouseMotionListener(this);

        // add control pane and layered pane to this JPanel
        JPanel beanSetupPane = new JPanel();
        beanSetupPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JLabel beanTypeTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanTypeLabel")));
        beanSetupPane.add(beanTypeTitle);
        beanTypeList = new JComboBox<>(beanTypeStrings);
        beanTypeList.setSelectedIndex(0); // select bean type T in comboBox
        beanTypeList.addActionListener((ActionEvent event) -> {
            String typeChoice = (String) beanTypeList.getSelectedItem();
            if (typeChoice != null) {
                displayManagerComboBoxes(typeChoice); // so these boxes should already be instantiated by now
            }
            updatePressed();
            setDirty();
        });
        beanSetupPane.add(beanTypeList);

        // add connection selection comboBox
        char beanTypeChar = getSwitchType().charAt(0); // translate from selectedIndex to char
        log.debug("beanTypeChar set to [{}]", beanTypeChar);
        JLabel beanManuTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ConnectionLabel")));
        beanSetupPane.add(beanManuTitle);

        beanSetupPane.add(turnoutManComboBox);
        beanSetupPane.add(sensorManComboBox);
        beanSetupPane.add(lightManComboBox);

        turnoutManComboBox.setToolTipText(Bundle.getMessage("ManComboBoxTip", Bundle.getMessage("BeanNameTurnout")));
        sensorManComboBox.setToolTipText(Bundle.getMessage("ManComboBoxTip", Bundle.getMessage("BeanNameSensor")));
        lightManComboBox.setToolTipText(Bundle.getMessage("ManComboBoxTip", Bundle.getMessage("BeanNameLight")));

        configureManagerComboBoxes(); // fill the combos
        displayManagerComboBoxes(TURNOUT); // show TurnoutManagerBox (matches the beanType combo

//        hardwareAddressValidator = new SystemNameValidator(addressTextField,
//                turnoutManComboBox.getItemAt(0),
//                false); // initial system (for type Turnout)
//        addressTextField.setInputVerifier(hardwareAddressValidator);

//        hardwareAddressValidator.addPropertyChangeListener("validation", (evt) -> { // NOI18N
//            Validation validation = hardwareAddressValidator.getValidation();
//            Validation.Type valid = validation.getType();
//            updateButton.setEnabled(valid != Validation.Type.WARNING && valid != Validation.Type.DANGER);
//            help2.setText(validation.getMessage());
//        });
//        hardwareAddressValidator.setManager(turnoutManComboBox.getItemAt(0)); // initial system (for type Turnout)
//        hardwareAddressValidator.verify(addressTextField);

        add(beanSetupPane);

        // add shape combobox
        JPanel switchShapePane = new JPanel();
        switchShapePane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JLabel switchShapeTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SwitchShape")));
        switchShapePane.add(switchShapeTitle);
        shapeList = new JComboBox<>(switchShapeStrings);
        shapeList.setSelectedIndex(0); // select Button choice in comboBox
        shapeList.addActionListener((ActionEvent event) -> {
            shape = (Math.max(shapeList.getSelectedIndex(), 0)); // picks 1st item when no selection
            updatePressed();
            setDirty();
        });
        switchShapePane.add(shapeList);
        // add column spinner
        JLabel rowsLabel = new JLabel(Bundle.getMessage("NumberOfRows"));
        switchShapePane.add(rowsLabel);
        rowsSpinner.setToolTipText(Bundle.getMessage("RowsSpinnerOnTooltip"));
        rowsSpinner.addChangeListener(e -> {
            //tmp synchronized (this) {
                if (!autoRowsBox.isSelected()) { // spinner is disabled when autoRows is on, but just in case
                    rows = (Integer) rowsSpinner.getValue();
                    updatePressed();
                    setDirty();
                }
            //tmp }
        });
        switchShapePane.add(rowsSpinner);
        rowsSpinner.setEnabled(false);
        add(switchShapePane);

        JPanel checkboxPane = new JPanel();
        checkboxPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        // autoItemRange checkbox on panel
        autoItemRange.setSelected(autoItemRange());
        log.debug("autoItemRangeBox set to {}", autoItemRange.isSelected());
        autoItemRange.addActionListener((ActionEvent event) -> {
            setAutoItemRange(autoItemRange.isSelected());
            autoItemRangeBox.setSelected(autoItemRange()); // also (un)check the box on the menu
            // if set to checked, store the current range from the spinners
        });
        checkboxPane.add(autoItemRange);
        autoItemRange.setToolTipText(Bundle.getMessage("AutoItemRangeTooltip"));
        // hideUnconnected checkbox on panel
        hideUnconnected.setSelected(_hideUnconnected);
        log.debug("hideUnconnectedBox set to {}", hideUnconnected.isSelected());
        hideUnconnected.addActionListener((ActionEvent event) -> {
            setHideUnconnected(hideUnconnected.isSelected());
            hideUnconnectedBox.setSelected(_hideUnconnected); // also (un)check the box on the menu
            help2.setVisible(!_hideUnconnected && (switchesOnBoard.size() != 0)); // and show/hide instruction line unless no items on board
            updatePressed();
            setDirty();
        });
        checkboxPane.add(hideUnconnected);
        add(checkboxPane);

        /* Construct special JFrame to hold the actual switchboard */
        switchboardLayeredPane.setLayout(new GridLayout(3, 8)); // initial layout params
        // Add at least 1 switch to pane to create switchList: done later, would be deleted soon if added now
        // see updatePressed()

        // provide a JLayeredPane to place the switches on
        super.setTargetPanel(switchboardLayeredPane, makeFrame(name));
        super.getTargetFrame().setSize(550, 330); // width x height
        //super.getTargetFrame().setSize(width + 6, height + 25); // width x height

        // set scrollbar initial state
        setScroll(SCROLL_NONE);
        scrollNone.setSelected(true);
        // set icon size initial state
        _iconSquare = SIZE_INIT;
        sizeDefault.setSelected(true);
        // register the resulting panel for later configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(this);
        }

        //add(addressTextField);
        add(createControlPanel());

        updateButton.addActionListener((ActionEvent event) -> {
            updatePressed();
            setDirty();
        });
        allOnButton = new JButton(Bundle.getMessage("AllOn"));
        allOnButton.addActionListener((ActionEvent event) -> switchAllLights(Light.ON));
        allOffButton = new JButton(Bundle.getMessage("AllOff"));
        allOffButton.addActionListener((ActionEvent event) -> switchAllLights(Light.OFF));
        JPanel allPane = new JPanel();
        allPane.setLayout(new BoxLayout(allPane, BoxLayout.PAGE_AXIS));
        allPane.add(allOnButton);
        allPane.add(allOffButton);

        JPanel updatePanel = new JPanel();
        updatePanel.add(updateButton);
        updatePanel.add(allPane);

        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(updatePanel);
        setupEditorPane(); // re-layout all the toolbar items

        lightManComboBox.addActionListener((ActionEvent event) -> {
            Manager<Light> manager = lightManComboBox.getSelectedItem();
            if (manager != null) {
                memo = manager.getMemo();
                addressTextField.setText("");     // Reset input before switching managers
                //hardwareAddressValidator.setManager(manager);
                log.debug("Lbox set to {}. Updating", memo.getUserName());
                updatePressed();
                setDirty();
            }
        });
        sensorManComboBox.addActionListener((ActionEvent event) -> {
            Manager<Sensor> manager = sensorManComboBox.getSelectedItem();
            if (manager != null) {
                memo = manager.getMemo();
                addressTextField.setText("");     // Reset input before switching managers
                //hardwareAddressValidator.setManager(manager);
                log.debug("Sbox set to {}. Updating", memo.getUserName());
                updatePressed();
                setDirty();
            }
        });
        turnoutManComboBox.addActionListener((ActionEvent event) -> {
            Manager<Turnout> manager = turnoutManComboBox.getSelectedItem();
            if (manager != null) {
                memo = manager.getMemo();
                addressTextField.setText("");     // Reset input before switching managers
                //hardwareAddressValidator.setManager(manager);
                log.debug("Tbox set to {}. Updating", memo.getUserName());
                updatePressed();
                setDirty();
            }
        });
        turnoutManComboBox.setSelectedItem("Internal"); // order of items in combo may vary on init() wait till now for init completed
        lightManComboBox.setSelectedItem("Internal"); // NOI18N
        sensorManComboBox.setSelectedItem("Internal"); // NOI18N
        log.debug("boxes are set to Internal, attaching listeners");

        updatePressed(); // refresh default Switchboard, rebuilds and resizes all switches, required for tests

        // component listener handles frame resizing event
        super.getTargetFrame().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //log.debug("PANEL RESIZED");
                resizeInFrame();
            }
        });
    }

    /**
     * Just repaint the Switchboard target panel.
     * Fired on componentResized(e) event.
     */
    private void resizeInFrame() {
        Dimension frSize = super.getTargetFrame().getSize(); // 5 px for border, var px for footer, autoRows(int)
        // some GUIs include (wide) menu bar inside frame
        switchboardLayeredPane.setSize(new Dimension((int) frSize.getWidth() - 6, (int) frSize.getHeight() - verticalMargin));
        switchboardLayeredPane.repaint();
        //tmp synchronized (this) {
            if (autoRowsBox.isSelected()) { // check if autoRows is active
                int oldRows = rows;
                rows = autoRows(cellProportion); // if it suggests a different value for rows, call updatePressed()
                if (rows != oldRows) {
                    rowsSpinner.setValue(rows); // updatePressed will update rows spinner in display, but spinner will not propagate when disabled
                    updatePressed(); // redraw if rows value changed
                }
            }
        //tmp }
    }

    /**
     * Create a new set of switches after removing the current array.
     * <p>
     * Called by Update button click, and automatically after loading a panel
     * from XML (with all saved options set).
     * Switchboard JPanel WindowResize() event is handled by resizeInFrame()
     */
    public void updatePressed() {
        log.debug("updatePressed START _tileSize = {}", _tileSize);

        if (_autoItemRange && !autoItemRange.isSelected()) {
            autoItemRange.setSelected(true);
        }
        setVisible(_editable); // show/hide editor

        // update selected address range
        int range = (Integer) maxSpinner.getValue() - (Integer) minSpinner.getValue() + 1;
        if (range > unconnectedRangeLimit && !_hideUnconnected) {
            // fixed maximum number of items on a Switchboard to prevent memory overflow
            range = unconnectedRangeLimit;
            maxSpinner.setValue((Integer) minSpinner.getValue() + range - 1);
        }
        // check for extreme number of items
        log.debug("address range = {}", range);
        if (range > rangeSizeWarning) {
            // ask user if range is indeed desired
            log.debug("Warning for big range");
            int retval = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("LargeRangeWarning", range, Bundle.getMessage("CheckBoxHideUnconnected")),
                    Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonCancel")}, null);
            log.debug("Retval: {}", retval);
            if (retval != 0) {
                return;
            }
        }
        ready = false; // set flag for updating
        // if range is confirmed, go ahead with switchboard update
        for (int i = switchesOnBoard.size() - 1; i >= 0; i--) {
            //            if (i >= switchboardLayeredPane.getComponentCount()) { // turn off this check for now
            //                continue;
            //            }
            // remove listeners before removing switches from JLayeredPane
            ((BeanSwitch) switchboardLayeredPane.getComponent(i)).cleanup();
            // deleting items starting from 0 will result in skipping the even numbered items
            switchboardLayeredPane.remove(i);
        }
        switchesOnBoard.clear(); // reset beanswitches LinkedHashMap
        log.debug("switchesOnBoard cleared, size is now: 0"); // always 0 at this point
        switchboardLayeredPane.setSize(width, height);

        String memoName = (memo != null ? memo.getUserName() : "UNKNOWN");
        log.debug("creating range for manu index {}", memoName);

//        Validation.Type valid = hardwareAddressValidator.getValidation().getType();
        String startAddress = "";
//        if (addressTextField.getText() != null && valid != Validation.Type.WARNING && valid != Validation.Type.DANGER) {
//            startAddress = addressTextField.getText();
//        }
        // fill switchesOnBoard LinkedHashMap, uses memo/manager already set
        createSwitchRange((Integer) minSpinner.getValue(),
                (Integer) maxSpinner.getValue(),
                beanTypeList.getSelectedIndex(),
                shapeList.getSelectedIndex(),
                startAddress);

        if (autoRowsBox.isSelected()) {
            rows = autoRows(cellProportion); // TODO: use specific proportion value per Type/Shape choice?
            log.debug("autoRows() called in updatePressed(). Rows = {}", rows);
            rowsSpinner.setValue(rows);
        }
        // disable the Rows spinner & Update button on the Switchboard Editor pane
        // param: GridLayout(vertical, horizontal), at least 1x1
        switchboardLayeredPane.setLayout(new GridLayout(Math.max(rows, 1), 1));

        // add switches to LayeredPane
        for (BeanSwitch bs : switchesOnBoard.values()) {
            switchboardLayeredPane.add(bs);
        }
        ready = true; // reset flag
        help3.setVisible(switchesOnBoard.size() == 0); // show/hide help3 warning
        help2.setVisible(switchesOnBoard.size() != 0); // hide help2 when help3 is shown vice versa (as no items are dimmed or not)
        // update the title at the bottom of the switchboard to match (no) layout control
        if (beanTypeList.getSelectedIndex() >= 0) {
            border.setTitle(memoName + " " +
                    beanTypeList.getSelectedItem() + " - " + (allControlling() ? interact : noInteract));
        }
        // hide AllOn/Off buttons unless type is Light and control is allowed
        allOnButton.setVisible((beanTypeList.getSelectedIndex() == 2) && allControlling());
        allOffButton.setVisible((beanTypeList.getSelectedIndex() == 2) && allControlling());
        pack();
        // must repaint again to fit inside frame
        Dimension frSize = super.getTargetFrame().getSize(); // 2x3 px for border, var px for footer + optional UI menubar, autoRows(int)
        switchboardLayeredPane.setSize(new Dimension((int) frSize.getWidth() - 6, (int) frSize.getHeight() - verticalMargin));
        switchboardLayeredPane.repaint();

        log.debug("updatePressed END _tileSize = {}", _tileSize);
    }

    /**
     * From default or user entry in Editor, create a LinkedHashMap of Switches.
     * <p>
     * Items in range that can connect to existing beans in JMRI are active. The
     * others are greyed out. Option to later connect (new) beans to switches.
     *
     * @param min         starting ordinal of Switch address range
     * @param max         highest ordinal of Switch address range
     * @param beanType    index of selected item in Type comboBox, either T, S
     *                    or L
     * @param shapeChoice index of selected visual presentation of Switch shape
     *                    selected in Type comboBox, choose either a JButton
     *                    showing the name or (to do) a graphic image
     */
    private void createSwitchRange(int min, int max, int beanType, int shapeChoice, @Nonnull String startAddress) {
        log.debug("createSwitchRange - _hideUnconnected = {}", _hideUnconnected);
        String name;
        BeanSwitch _switch;
        NamedBean nb;
        if (memo == null) {
            log.error("createSwitchRange - null memo, can't create range");
            return;
        }
        String prefix = memo.getSystemPrefix();
        // TODO handling of non-numeric system names such as MERG, C/MRI using validator textField
        // if (!startAddress.equals("")) { // use as start address, spinners are only for the number of items
        log.debug("createSwitchRange - _manuprefix={} beanType={}", prefix, beanType);
        // use validated bean names
        for (int i = min; i <= max; i++) {
            switch (beanType) {
                case 0:
                    try {
                        name = ((TurnoutManager)memo.get(TurnoutManager.class)).createSystemName(i + "", prefix);
                    } catch (jmri.JmriException ex) {
                        log.error("Error creating range at turnout {}", i);
                        return;
                    }
                    nb = jmri.InstanceManager.getDefault(TurnoutManager.class).getTurnout(name);
                    break;
                case 1:
                    try { // was: InstanceManager.getDefault(SensorManager.class)
                        name = ((SensorManager)memo.get(SensorManager.class)).createSystemName(i + "", prefix);
                    } catch (jmri.JmriException | NullPointerException ex) {
                        log.trace("Error creating range at sensor {}. Connection {}", i, memo.getUserName(), ex);
                        return;
                    }
                    nb = jmri.InstanceManager.getDefault(SensorManager.class).getSensor(name);
                    break;
                case 2:
                    try {
                        name = ((LightManager)memo.get(LightManager.class)).createSystemName(i + "", prefix);
                    } catch (jmri.JmriException ex) {
                        log.error("Error creating range at light {}", i);
                        return;
                    }
                    nb = jmri.InstanceManager.lightManagerInstance().getLight(name);
                    break;
                default:
                    log.error("addSwitchRange: cannot parse bean name. Prefix = {}; i = {}; type={}", prefix, i, beanType);
                    return;
            }
            if (nb == null && _hideUnconnected) {
                continue; // skip bean i
            }
            log.debug("Creating Switch for {}", name);
            _switch = new BeanSwitch(i, nb, name, shapeChoice, this); // add button instance i
            if (nb == null) {
                _switch.setEnabled(false); // not connected
            } else {
                // set switch to display current bean state
                _switch.displayState(nb.getState());
            }
            switchesOnBoard.put(name, _switch); // add to LinkedHashMap of switches for later placement on JLayeredPane
            log.debug("Added switch {}", name);
            // keep total number of switches below practical total of 400 (20 x 20 items)
            if (switchesOnBoard.size() >= unconnectedRangeLimit) {
                log.warn("switchboards are limited to {} items", unconnectedRangeLimit);
                break;
            }
            // was already checked in first counting loop in init()
        }
    }

    /**
     * Create the setup pane for the top of the frame. From layeredpane demo.
     */
    private JPanel createControlPanel() {
        JPanel controls = new JPanel();

        // navigation top row and range to set
        JPanel navBarPanel = new JPanel();
        navBarPanel.setLayout(new BoxLayout(navBarPanel, BoxLayout.X_AXIS));

        navBarPanel.add(prev);
        prev.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int oldMin = getMinSpinner();
                int oldMax = getMaxSpinner();
                int range = Math.max(oldMax - oldMin + 1, 1); // make sure range > 0
                log.debug("prev range was {}, oldMin ={}, oldMax ={}", range, oldMin, oldMax);
                setMinSpinner(Math.max((oldMin - range), rangeBottom)); // first set new min
                if (_autoItemRange) {
                    setMaxSpinner(Math.max((oldMax - range), range));   // set new max (only if auto)
                }
                updatePressed();
                setDirty();
                log.debug("new prev range = {}, newMin ={}, newMax ={}", range, getMinSpinner(), getMaxSpinner());
            }
        });
        prev.setToolTipText(Bundle.getMessage("PreviousToolTip", Bundle.getMessage("CheckBoxAutoItemRange")));
        navBarPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("From"))));
        JComponent minEditor = minSpinner.getEditor();
        // enlarge minSpinner editor text field width
        JFormattedTextField minTf = ((JSpinner.DefaultEditor) minEditor).getTextField();
        minTf.setColumns(5);
        minSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int value = (int)spinner.getValue();
            // stop if value >= maxSpinner -1 (range <= 0)
            if (value >= (Integer) maxSpinner.getValue() - 1) {
                maxSpinner.setValue(value + 1);
            }
            updatePressed();
            setDirty();
        });
        navBarPanel.add(minSpinner);
        navBarPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("UpTo"))));
        // enlarge maxSpinner editor text field width
        JComponent maxEditor = maxSpinner.getEditor();
        JFormattedTextField maxTf = ((JSpinner.DefaultEditor) maxEditor).getTextField();
        maxTf.setColumns(5);
        maxSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int value = (int)spinner.getValue();
            // stop if value <= minSpinner + 1 (range <= 0)
            if (value <= (Integer) minSpinner.getValue() + 1) {
                minSpinner.setValue(value - 1);
            }
            updatePressed();
            setDirty();
        });
        navBarPanel.add(maxSpinner);

        navBarPanel.add(next);
        next.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int oldMin = getMinSpinner();
                int oldMax = getMaxSpinner();
                int range = Math.max(oldMax - oldMin + 1, 1); // make sure range > 0
                log.debug("next range was {}, oldMin ={}, oldMax ={}", range, oldMin, oldMax);
                setMaxSpinner(Math.min((oldMax + range), rangeTop));               // first set new max
                if (_autoItemRange) {
                    setMinSpinner(Math.min(oldMin + range, rangeTop - range + 1)); // set new min (only if auto)
                }
                updatePressed();
                setDirty();
                log.debug("new next range = {}, newMin ={}, newMax ={}", range, getMinSpinner(), getMaxSpinner());
            }
        });
        next.setToolTipText(Bundle.getMessage("NextToolTip", Bundle.getMessage("CheckBoxAutoItemRange")));
        navBarPanel.add(Box.createHorizontalGlue());

        controls.add(navBarPanel); // put items on 2nd Editor Panel
        controls.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRangeTitle")));
        return controls;
    }

    private int getMinSpinner() { //tmp
        return (Integer) minSpinner.getValue();
    }

    private int getMaxSpinner() { //tmp synchronized
        return (Integer) maxSpinner.getValue();
    }

    protected void setMinSpinner(int value) { //tmp synchronized
        if (value >= rangeBottom && value < rangeTop) { // allows to set above MaxSpinner temporarily
            minSpinner.setValue(value);
        }
    }

    protected void setMaxSpinner(int value) { //tmp synchronized
        if (value > rangeBottom && value <= rangeTop) { // allows to set above MinSpinner temporarily
            maxSpinner.setValue(value);
        }
    }

    private void setupEditorPane() {
        // Initial setup
        Container contentPane = getContentPane(); // Editor (configuration) pane

        JPanel innerBorderPanel = new JPanel();
        innerBorderPanel.setLayout(new BoxLayout(innerBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder TitleBorder = BorderFactory.createTitledBorder(Bundle.getMessage("SwitchboardHelpTitle"));
        innerBorderPanel.setBorder(TitleBorder);
        innerBorderPanel.add(new JTextArea(Bundle.getMessage("Help1")));
        // help2 explains: dimmed icons = unconnected
        innerBorderPanel.add(help2);
        if (!_hideUnconnected) {
            help2.setVisible(false); // hide this text when _hideUnconnected is set to true from menu or checkbox
        }
        // help3 warns: no icons to show on switchboard
        help3.setForeground(Color.red);
        innerBorderPanel.add(help3);
        help3.setVisible(false); // initially hide help3 warning text
        contentPane.add(innerBorderPanel);
    }

    //@Override
    protected void makeOptionMenu() {
        _optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));
        _menuBar.add(_optionMenu, 0);
        // controllable item
        _optionMenu.add(controllingBox);
        controllingBox.addActionListener((ActionEvent event) -> {
            setAllControlling(controllingBox.isSelected());
            // update the title on the switchboard to match (no) layout control
            if (beanTypeList.getSelectedItem() != null) {
                border.setTitle(memo.getUserName() + " " +
                        beanTypeList.getSelectedItem().toString() + " - " + (allControlling() ? interact : noInteract));
            }
            allOnButton.setVisible((beanTypeList.getSelectedIndex() == 2) && allControlling());
            allOffButton.setVisible((beanTypeList.getSelectedIndex() == 2) && allControlling());
            switchboardLayeredPane.repaint();
            log.debug("border title updated");
        });
        controllingBox.setSelected(allControlling());

        // autoItemRange item
        _optionMenu.add(autoItemRangeBox);
        autoItemRangeBox.addActionListener((ActionEvent event) -> {
            setAutoItemRange(autoItemRangeBox.isSelected());
            autoItemRange.setSelected(autoItemRange()); // also (un)check the box on the editor
        });
        autoItemRangeBox.setSelected(autoItemRange());

        _optionMenu.addSeparator();

        // auto rows item
        _optionMenu.add(autoRowsBox);
        //tmp synchronized (this) {
            autoRowsBox.setSelected(true); // default on
        //tmp }
        autoRowsBox.addActionListener((ActionEvent event) -> {
            //tmp synchronized (this) {
                if (autoRowsBox.isSelected()) {
                    log.debug("autoRows was turned ON");
                    int oldRows = rows;
                    rows = autoRows(cellProportion); // recalculates rows x columns and redraws pane
                    // sets _tileSize TODO: specific proportion value per Type/Shape choice?
                    rowsSpinner.setEnabled(false);
                    rowsSpinner.setToolTipText(Bundle.getMessage("RowsSpinnerOffTooltip"));
                    // hide rowsSpinner + rowsLabel?
                    if (rows != oldRows) {
                        // rowsSpinner will be recalculated by auto so we don't copy the old value
                        updatePressed(); // redraw if rows value changed
                    }
                } else {
                    log.debug("autoRows was turned OFF");
                    rowsSpinner.setValue(rows); // autoRows turned off, copy current auto value to spinner
                    rowsSpinner.setEnabled(true); // show rowsSpinner + rowsLabel?
                    rowsSpinner.setToolTipText(Bundle.getMessage("RowsSpinnerOnTooltip"));
                    // calculate tile size
                    int colNum = (((getTotal() > 0) ? (getTotal()) : 1) + rows - 1) / Math.max(rows, 1);
                    int maxW = (super.getTargetFrame().getWidth() - 10) / colNum; // int division, subtract 2x3px for border
                    int maxH = (super.getTargetFrame().getHeight() - verticalMargin) / Math.max(rows, 1); // for footer
                    _tileSize = Math.min(maxW, maxH); // store for tile graphics
                }
            //tmp }
        });
        // show tooltip item
        _optionMenu.add(showToolTipBox);
        showToolTipBox.addActionListener((ActionEvent e) -> setAllShowToolTip(showToolTipBox.isSelected()));
        showToolTipBox.setSelected(showToolTip());
        // show user name on switches item
        _optionMenu.add(showUserNameBox);
        showUserNameBox.addActionListener((ActionEvent e) -> updatePressed());
        showUserNameBox.setSelected(true); // default on

        // hideUnconnected item
        _optionMenu.add(hideUnconnectedBox);
        hideUnconnectedBox.setSelected(_hideUnconnected);
        hideUnconnectedBox.addActionListener((ActionEvent event) -> {
            setHideUnconnected(hideUnconnectedBox.isSelected());
            hideUnconnected.setSelected(_hideUnconnected); // also (un)check the box on the editor
            help2.setVisible(!_hideUnconnected && (switchesOnBoard.size() != 0)); // and show/hide instruction line unless no items on board
            updatePressed();
            setDirty();
        });

        // Show/Hide Scroll Bars
        JMenu scrollMenu = new JMenu(Bundle.getMessage("ComboBoxScrollable"));
        _optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.addActionListener((ActionEvent event) -> setScroll(SCROLL_BOTH));
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.addActionListener((ActionEvent event) -> setScroll(SCROLL_NONE));
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.addActionListener((ActionEvent event) -> setScroll(SCROLL_HORIZONTAL));
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.addActionListener((ActionEvent event) -> setScroll(SCROLL_VERTICAL));

        // add beanswitch size menu item
        JMenu iconSizeMenu = new JMenu(Bundle.getMessage("MenuIconSize"));
        _optionMenu.add(iconSizeMenu);
        ButtonGroup sizeGroup = new ButtonGroup();
        sizeGroup.add(sizeSmall);
        iconSizeMenu.add(sizeSmall);
        sizeSmall.addActionListener((ActionEvent event) -> setIconScale(SIZE_MIN));
        sizeGroup.add(sizeDefault);
        iconSizeMenu.add(sizeDefault);
        sizeDefault.addActionListener((ActionEvent event) -> setIconScale(SIZE_INIT));
        sizeGroup.add(sizeLarge);
        iconSizeMenu.add(sizeLarge);
        sizeLarge.addActionListener((ActionEvent event) -> setIconScale(SIZE_MAX));

        JMenu colorMenu = new JMenu(Bundle.getMessage("Colors"));
        _optionMenu.add(colorMenu);
        // add text color menu item
        JMenuItem textColorMenuItem = new JMenuItem(Bundle.getMessage("DefaultTextColor", "..."));
        colorMenu.add(textColorMenuItem);
        textColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                                 Bundle.getMessage("DefaultTextColor", ""),
                                 defaultTextColor);
            if (desiredColor != null && !defaultTextColor.equals(desiredColor)) {
                // if new defaultTextColor matches bgColor, ask user as labels will become unreadable
                if (desiredColor.equals(defaultBackgroundColor)) {
                    int retval = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("ColorIdenticalWarningF"), Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonInvert"), Bundle.getMessage("ButtonCancel")}, null);
                    if (retval == 1) { // invert the other color
                        setDefaultBackgroundColor(contrast(defaultBackgroundColor));
                    } else if (retval != 0) {
                        return; // cancel
                    }
                }
                defaultTextColor = desiredColor;
                border.setTitleColor(desiredColor);
                setDirty(true);
                JmriColorChooser.addRecentColor(desiredColor);
                updatePressed();
            }
        });
        // add background color menu item
        JMenuItem backgroundColorMenuItem = new JMenuItem(Bundle.getMessage("SetBackgroundColor", "..."));
        colorMenu.add(backgroundColorMenuItem);
        backgroundColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("SetBackgroundColor", ""),
                    defaultBackgroundColor);
            if (desiredColor != null && !defaultBackgroundColor.equals(desiredColor)) {
                // if new bgColor matches the defaultTextColor, ask user as labels will become unreadable
                if (desiredColor.equals(defaultTextColor)) {
                    int retval = JOptionPane.showOptionDialog(null,
                            Bundle.getMessage("ColorIdenticalWarningR"), Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                            new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonInvert"), Bundle.getMessage("ButtonCancel")}, null);
                    if (retval == 1) { // invert the other color
                        defaultTextColor = contrast(defaultTextColor);
                        border.setTitleColor(defaultTextColor);
                    } else if (retval != 0) {
                        return; // cancel
                    }
                }
                defaultBackgroundColor = desiredColor;
                setBackgroundColor(desiredColor);
                setDirty(true);
                JmriColorChooser.addRecentColor(desiredColor);
                updatePressed();
            }
        });
        // add ActiveColor menu item
        JMenuItem activeColorMenuItem = new JMenuItem(Bundle.getMessage("SetActiveColor", "..."));
        colorMenu.add(activeColorMenuItem);
        activeColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("SetActiveColor", ""),
                    defaultActiveColor);
            if (desiredColor != null && !defaultActiveColor.equals(desiredColor)) {
                // if new ActiveColor matches InactiveColor, ask user as state will become unreadable
                if (desiredColor.equals(defaultInactiveColor)) {
                    int retval = JOptionPane.showOptionDialog(null,
                            Bundle.getMessage("ColorIdenticalWarningF"), Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                            new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonInvert"), Bundle.getMessage("ButtonCancel")}, null);
                    if (retval == 1) { // invert the other color
                        setDefaultInactiveColor(contrast(defaultInactiveColor));
                    } else if (retval != 0) {
                        return; // cancel
                    }
                }
                defaultActiveColor = desiredColor;
                setDirty(true);
                JmriColorChooser.addRecentColor(desiredColor);
                updatePressed();
            }
        });
        // add InctiveColor menu item
        JMenuItem inactiveColorMenuItem = new JMenuItem(Bundle.getMessage("SetInactiveColor", "..."));
        colorMenu.add(inactiveColorMenuItem);
        inactiveColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("SetInactiveColor", ""),
                    defaultInactiveColor);
            if (desiredColor != null && !defaultInactiveColor.equals(desiredColor)) {
                // if new InactiveColor matches ActiveColor, ask user as state will become unreadable
                if (desiredColor.equals(defaultInactiveColor)) {
                    int retval = JOptionPane.showOptionDialog(null,
                            Bundle.getMessage("ColorIdenticalWarningF"), Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                            new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonInvert"), Bundle.getMessage("ButtonCancel")}, null);
                    if (retval == 1) { // invert the other color
                        setDefaultActiveColor(contrast(defaultActiveColor));
                    } else if (retval != 0) {
                        return; // cancel
                    }
                }
                defaultInactiveColor = desiredColor;
                setDirty(true);
                JmriColorChooser.addRecentColor(desiredColor);
                updatePressed();
            }
        });
    }

    private void makeFileMenu() {
        _fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        _menuBar.add(_fileMenu, 0);
        _fileMenu.add(new jmri.jmrit.display.NewPanelAction(Bundle.getMessage("MenuItemNew")));

        _fileMenu.add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("FileMenuItemStore")));

        JMenuItem editItem = new JMenuItem(Bundle.getMessage("renamePanelMenu", "..."));
        PositionableJComponent z = new PositionableJComponent(this);
        z.setScale(getPaintScale());
        editItem.addActionListener(CoordinateEdit.getNameEditAction(z));
        _fileMenu.add(editItem);

        _fileMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("DeletePanel"));
        _fileMenu.add(deleteItem);
        deleteItem.addActionListener((ActionEvent event) -> {
            if (deletePanel()) {
                getTargetFrame().dispose();
                dispose();
            }
        });
        _fileMenu.addSeparator();
        editItem = new JMenuItem(Bundle.getMessage("CloseEditor"));
        _fileMenu.add(editItem);
        editItem.addActionListener((ActionEvent event) -> {
            log.debug("switchboardeditor edit menu CloseEditor selected");
            setAllEditable(false);
            setVisible(false); // hide Editor pane
        });
    }

    public void setDefaultTextColor(Color color) {
        defaultTextColor = color;
        border.setTitleColor(color);
    }

    /**
     * @param color the string containing a color settable using {@link jmri.util.ColorUtil#stringToColor(String)}
     * @deprecated since 4.15.7; use {@link #setDefaultTextColor(Color)} instead
     */
    @Deprecated
    public void setDefaultTextColor(String color) {
        setDefaultTextColor(ColorUtil.stringToColor(color));
    }

    public String getDefaultTextColor() {
        return ColorUtil.colorToColorName(defaultTextColor);
    }

    public Color getDefaultTextColorAsColor() {
        return defaultTextColor;
    }

    public String getActiveSwitchColor() {
        return ColorUtil.colorToColorName(defaultActiveColor);
    }
    public Color getActiveColorAsColor() {
        return defaultActiveColor;
    }
    public void setDefaultActiveColor(Color color) {
        defaultActiveColor = color;
    }

    public String getInactiveSwitchColor() {
        return ColorUtil.colorToColorName(defaultInactiveColor);
    }
    public Color getInactiveColorAsColor() {
        return defaultInactiveColor;
    }
    public void setDefaultInactiveColor(Color color) {
        defaultInactiveColor = color;
    }

    /**
     * Load from xml and set bg color of _targetpanel as well as variable.
     *
     * @param color RGB Color for switchboard background and beanSwitches
     */
    public void setDefaultBackgroundColor(Color color) {
        setBackgroundColor(color); // via Editor to update bg color of JPanel
        defaultBackgroundColor = color;
    }

    /**
     * Get current default background color.
     *
     * @return background color of this Switchboard
     */
    public Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    // *********************** end Menus ************************

    @Override
    public void setAllEditable(boolean edit) {
        log.debug("_editable set to {} in super", edit);
        if (edit) {
            if (_editorMenu != null) {
                _menuBar.remove(_editorMenu);
            }
            if (_optionMenu == null) {
                makeOptionMenu();
            } else {
                _menuBar.add(_optionMenu, 0);
            }
            if (_fileMenu == null) {
                makeFileMenu();
            } else {
                _menuBar.add(_fileMenu, 0);
            }
            log.debug("added File and Options menubar");
            //contentPane.SetUpdateButtonEnabled(false);
        } else {
            if (_fileMenu != null) {
                _menuBar.remove(_fileMenu);
            }
            if (_optionMenu != null) {
                _menuBar.remove(_optionMenu);
            }
            if (_editorMenu == null) {
                _editorMenu = new JMenu(Bundle.getMessage("MenuEdit"));
                _editorMenu.add(new AbstractAction(Bundle.getMessage("OpenEditor")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setAllEditable(true);
                        log.debug("Switchboard Editor Open Editor menu called");
                    }
                });
                _menuBar.add(_editorMenu, 0);
            }
            //contentPane.SetUpdateButtonEnabled(true);
        }
        super.setAllEditable(edit);
        super.setTitle();
        _menuBar.revalidate();
    }

    @Override
    public void setUseGlobalFlag(boolean set) {
        controllingBox.setEnabled(set);
        super.setUseGlobalFlag(set);
    }

    @Override
    public void setTitle() {
        String name = getName(); // get name of JFrame
        log.debug("JFrame name = {}", name);
        if (name == null || name.length() == 0) {
            name = Bundle.getMessage("SwitchboardDefaultName", "");
        }
        super.setTitle(name + " " + Bundle.getMessage("LabelEditor"));
        super.getTargetFrame().setTitle(name);
    }

    /**
     * Control whether target panel items without a connection to the layout are
     * displayed.
     *
     * @param state true to hide all in range
     */
    public void setHideUnconnected(boolean state) {
        _hideUnconnected = state;
    }

    public boolean hideUnconnected() {
        return _hideUnconnected;
    }

    /**
     * Control whether range of items is automatically preserved.
     *
     * @param state true to calculate upper limit from lowest value range value set (default)
     */
    public void setAutoItemRange(boolean state) {
        _autoItemRange = state;
    }

    public boolean autoItemRange() {
        return _autoItemRange;
    }

    /**
     * Determine optimal cols/rows inside JPanel using switch range, icon proportions of beanswitch icons +
     * web canvas W:H proportions range from 1.5 (3:2) to 0.7 (1:1.5), assume squares for now.
     *
     * @param cellProp the W:H proportion of image, currently 1.0f for all shapes
     * @return number of rows on current target pane size/proportions displaying biggest tiles
     */
    private int autoRows(float cellProp) {
        // find cell matrix that allows largest size icons
        double paneEffectiveWidth = Math.ceil((super.getTargetFrame().getWidth() - 6)/ Math.max(cellProp, 0.1f)); // -2x3px for border
        //log.debug("paneEffectiveWidth: {}", paneEffectiveWidth); // compare to resizeInFrame()
        double paneHeight = super.getTargetFrame().getHeight() - verticalMargin; // for footer
        int columnsNum = 1;
        int rowsNum = 1;
        float tileSize = 0.1f; // start value
        float tileSizeOld = 0.0f;
        int totalDisplayed = ((getTotal() > 0) ? (getTotal()) : 1);
        // if all items unconnected and set to be hidden, use 1
        if (totalDisplayed >= unconnectedRangeLimit) {
            log.warn("switchboards are limited to {} items", unconnectedRangeLimit);
        }

        while (tileSize > tileSizeOld) {
            rowsNum = (totalDisplayed + columnsNum - 1) / Math.max(columnsNum, 1); // int roundup
            tileSizeOld = tileSize; // store for comparison
            tileSize = (float) Math.min(paneEffectiveWidth / Math.max(columnsNum, 1), paneHeight / Math.max(rowsNum, 1));
            //log.debug("C>R Cols {} x Rows {}, tileSize {} was {}", columnsNum, rowsNum, String.format("%.2f", tileSize),
            //        String.format("%.2f", tileSizeOld));
            if (tileSize < tileSizeOld) {
                rowsNum = (totalDisplayed + columnsNum - 2) / Math.max((columnsNum - 1), 1);
                break;
            }
            columnsNum++;
        }

        // start over stepping columns instead of rows
        int columnsNumC;
        int rowsNumC = 1;
        float tileSizeC = 0.1f;
        float tileSizeCOld = 0.0f;
        while (tileSizeC > tileSizeCOld) {
            columnsNumC = (totalDisplayed + rowsNumC - 1) / Math.max(rowsNumC, 1); // int roundup
            tileSizeCOld = tileSizeC; // store for comparison
            tileSizeC = (float) Math.min(paneEffectiveWidth / Math.max(columnsNumC, 1), paneHeight / Math.max(rowsNumC, 1));
            //log.debug("R>C Cols {} x Rows {}, tileSizeC {} was {}", columnsNumC, rowsNumC, String.format("%.2f", tileSizeC),
            //        String.format("%.2f", tileSizeCOld));
            if (tileSizeC < tileSizeCOld) {
                rowsNumC--;
                break;
            }
            rowsNumC++;
        }

        if (tileSizeC > tileSize) { // we must choose the largest solution
            rowsNum = rowsNumC;
        }
        // Math.min(1,... to prevent >100% width calc (when hide unconnected selected)
        // rows = (total + columns - 1) / columns (int roundup) to account for unused tiles in grid:
        // for 23 switches we need at least 24 tiles (4x6, 3x8, 2x12 etc)
        // similar calculations repeated in panel.js for web display
        //log.debug("CELL SIZE optimum found: CxR = {}x{}, tileSize = {}", ((totalDisplayed + rowsNum - 1) / rowsNum), rowsNum, tileSize);

        _tileSize = Math.round((float) paneHeight / Math.max(rowsNum, 1)); // recalculate tileSize from rowNum, store for tile graphics
        return rowsNum;
    }

    /**
     * Allow external reset of dirty bit.
     */
    public void resetDirty() {
        setDirty(false);
        savedEditMode = isEditable();
        savedControlLayout = allControlling();
    }

    /**
     * Allow external set of dirty bit.
     * @param val new dirty flag value, true dirty, false clean.
     */
    public void setDirty(boolean val) {
        panelChanged = val;
    }

    public void setDirty() {
        setDirty(true);
    }

    /**
     * Check the dirty state.
     * @return true if panel changed, else false.
     */
    public boolean isDirty() {
        return panelChanged;
    }

    // ********************** load/store board *******************
    /**
     * Load Range minimum.
     *
     * @param rangemin lowest address to show
     */
    public void setPanelMenuRangeMin(int rangemin) {
        minSpinner.setValue(rangemin);
    }

    /**
     * Load Range maximum.
     *
     * @param rangemax highest address to show
     */
    public void setPanelMenuRangeMax(int rangemax) {
        maxSpinner.setValue(rangemax);
    }

    /**
     * Store Range minimum.
     *
     * @return lowest address shown
     */
    public int getPanelMenuRangeMin() {
        return (int) minSpinner.getValue();
    }

    /**
     * Store Range maximum.
     *
     * @return highest address shown
     */
    public int getPanelMenuRangeMax() {
        return (int) maxSpinner.getValue();
    }

    // ***************** Store & Load xml ********************
    /**
     * Store bean type.
     *
     * @return bean type prefix as set for Switchboard
     */
    public String getSwitchType() {
        String typePref;
        String switchType = "";
        if (beanTypeList.getSelectedItem() != null) {
            switchType = beanTypeList.getSelectedItem().toString();
        }
        if (switchType.equals(LIGHT)) { // switch-case doesn't work here
            typePref = "L";
        } else if (switchType.equals(SENSOR)) {
            typePref = "S";
        } else { // Turnout
            typePref = "T";
        }
        return typePref;
    }

    /**
     * Get bean type name.
     *
     * @return bean type name
     */
    public String getSwitchTypeName() {
        return _type;
    }

    /**
     * Load bean type from xml.
     *
     * @param prefix the bean type prefix
     */
    public void setSwitchType(String prefix) {
        switch (prefix) {
            case "L":
                _type = LIGHT;
                break;
            case "S":
                _type = SENSOR;
                break;
            case "T":
            default:
                _type = TURNOUT;
        }
        try {
            beanTypeList.setSelectedItem(_type);
        } catch (IllegalArgumentException e) {
            log.error("invalid bean type [{}] in Switchboard", prefix);
        }
    }

    /**
     * Store connection type.
     *
     * @return active bean connection prefix
     */
    public String getSwitchManu() {
        return memo.getSystemPrefix();
    }

    /**
     * Load connection type.
     *
     * @param manuPrefix connection prefix
     */
    public void setSwitchManu(String manuPrefix) {
        try {
            memo = SystemConnectionMemoManager.getDefault().getSystemConnectionMemoForSystemPrefix(manuPrefix);
            if (memo.get(TurnoutManager.class) != null) { // just for initial view
                turnoutManComboBox.setSelectedItem(memo.get(TurnoutManager.class));
                log.debug("turnoutManComboBox set to {} for {}", memo.getUserName(), manuPrefix);
            }
            if (memo.get(SensorManager.class) != null) { // we expect the user has same preference for the other types
                sensorManComboBox.setSelectedItem(memo.get(SensorManager.class));
                // TODO LocoNet does not provide a sensormanager via the memo
                log.debug("sensorManComboBox set to {} for {}", memo.getUserName(), manuPrefix);
            }
            if (memo.get(LightManager.class) != null) { // so we set them the same (only 1 value stored as set on store)
                lightManComboBox.setSelectedItem(memo.get(LightManager.class));
                log.debug("lightManComboBox set to {} for {}", memo.getUserName(), manuPrefix);
            }
        } catch (IllegalArgumentException e) {
            log.error("invalid connection [{}] in Switchboard", manuPrefix);
        } catch (NullPointerException e) {
            log.error("NPE setting prefix to [{}] in Switchboard", manuPrefix);
        }
    }

    /**
     * Store switch shape.
     *
     * @return bean shape prefix
     */
    public String getSwitchShape() {
        String shapeAsString;
        switch (shape) {
            case SLIDER:
                shapeAsString = "icon";
                break;
            case KEY:
                shapeAsString = "drawing";
                break;
            case SYMBOL:
                shapeAsString = "symbol";
                break;
            case (BUTTON):
            default: // 0 = basic labelled button
                shapeAsString = "button";
        }
        return shapeAsString;
    }

    /**
     * Load switch shape.
     *
     * @param switchShape name of switch shape
     */
    public void setSwitchShape(String switchShape) {
        switch (switchShape) {
            case "icon":
                shape = SLIDER;
                break;
            case "drawing":
                shape = KEY;
                break;
            case "symbol":
                shape = SYMBOL;
                break;
            default: // button
                shape = BUTTON;
        }
        try {
            shapeList.setSelectedIndex(shape);
        } catch (IllegalArgumentException e) {
            log.error("invalid switch shape [{}] in Switchboard", shape);
        }
    }

    /**
     * Store Switchboard rowsNum JSpinner or turn on autoRows option.
     *
     * @return the number of switches to display per row or 0 if autoRowsBox (menu-setting) is selected
     */
    public int getRows() { //tmp synchronized
        if (autoRowsBox.isSelected()) {
            return 0;
        } else {
            return rows;
        }
    }

    /**
     * Load Switchboard rowsNum JSpinner.
     *
     * @param rws the number of switches displayed per row (as text) or 0 te activate autoRowsBox setting
     */
    public void setRows(int rws) { //tmp synchronized
        autoRowsBox.setSelected(rws == 0);
        if (rws > 0) {
            rowsSpinner.setValue(rws); // rows is set via rowsSpinner
            rowsSpinner.setEnabled(true);
        } else {
            rowsSpinner.setEnabled(false);
            rowsSpinner.setToolTipText(Bundle.getMessage("RowsSpinnerOffTooltip"));
            rows = autoRows(cellProportion); // recalculate, TODO: specific proportion value for Type/Shape choice?
            rowsSpinner.setValue(rows);
        }
    }

    /**
     * @return the number of switches displayed per row
     * @deprecated since 4.21.2, replaced by {@link #getRows()} because that is what it holds
     */
    @Deprecated
    public int getColumns() {
        return getRows();
    }

    /**
     * @param rws the number of switches to display per row
     * @deprecated since 4.21.2, replaced by {@link #setRows(int)} because that is what it holds
     */
    @Deprecated
    public void setColumns(int rws) {
        setRows(rws);
    }

    /**
     * Store total number of switches displayed (unconnected/hidden excluded).
     *
     * @return the total number of switches displayed
     */
    public int getTotal() {
        return switchesOnBoard.size();
    }

    // all content loaded from file.
    public void loadComplete() {
        log.debug("loadComplete");
    }

    public String showUserName() {
        return (showUserNameBox.isSelected() ? "yes" : "no");
    }

    public void setShowUserName(Boolean on) {
        showUserNameBox.setSelected(on);
    }

    /**
     * After construction, initialize all the widgets to their saved config
     * settings.
     */
    @Override
    public void initView() {
        controllingBox.setSelected(allControlling());
        showToolTipBox.setSelected(showToolTip());
        switch (_scrollState) {
            case SCROLL_NONE:
                scrollNone.setSelected(true);
                break;
            case SCROLL_BOTH:
                scrollBoth.setSelected(true);
                break;
            case SCROLL_HORIZONTAL:
                scrollHorizontal.setSelected(true);
                break;
            default:
                scrollVertical.setSelected(true);
        }
        log.debug("InitView done");
    }

    protected Manager<?> getManager(char typeChar) {
        switch (typeChar) {
            case 'T': // Turnout
                return InstanceManager.getNullableDefault(TurnoutManager.class);
            case 'S': // Sensor
                return InstanceManager.getNullableDefault(SensorManager.class);
            case 'L': // Light
                return InstanceManager.getNullableDefault(LightManager.class);
            default:
                log.error("Unsupported bean type character \"{}\" found.", typeChar);
                return null;
        }
    }

    /**
     * Get the currently active manager.
     *
     * @return manager in use for the currently selected bean type and connection
     */
    protected Manager<?> getManager() {
        if (_type.equals(TURNOUT)) {
            return turnoutManComboBox.getSelectedItem();
        } else if (_type.equals(SENSOR)) {
                return sensorManComboBox.getSelectedItem();
        } else if (_type.equals(LIGHT)) {
            return lightManComboBox.getSelectedItem();
        } else {
            log.error("Unsupported bean type character \"{}\" found.", _type);
            return null;
        }
    }

    /**
     * KeyListener of Editor.
     *
     * @param e the key event heard
     */
    @Override
    public void keyPressed(KeyEvent e) {
        repaint();
        // TODO select another switch using keypad? accessibility
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mouseDragged(MouseEvent event) {
    }

    @Override
    public void mouseMoved(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        _targetPanel.repaint();
    }

    @Override
    public void mouseExited(MouseEvent event) {
        setToolTip(null);
        _targetPanel.repaint(); // needed for ToolTip on targetPane
    }

    /**
     * Handle close of Editor window.
     * <p>
     * Overload/override method in JmriJFrame parent, which by default is
     * permanently closing the window. Here, we just want to make it invisible,
     * so we don't dispose it (yet).
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        setAllEditable(false);
        log.debug("windowClosing");
    }

    /**
     * Handle opening of Editor window.
     * <p>
     * Overload/override method in JmriJFrame parent to reset _menuBar.
     */
    @Override
    public void windowOpened(java.awt.event.WindowEvent e) {
        _menuBar.revalidate();
    }

    // ************* implementation of Abstract Editor methods **********

    /**
     * The target window has been requested to close. Don't delete it at this
     * time. Deletion must be accomplished via the "Delete this Panel" menu item.
     */
    @Override
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        boolean save = (isDirty() || (savedEditMode != isEditable())
                || (savedControlLayout != allControlling()));
        log.trace("Temp fix to disable CI errors: save = {}", save);
        targetWindowClosing();
    }

    /**
     * changeView is not supported by SwitchBoards.
     * {@inheritDoc}
     */
    @Override
    protected Editor changeView(String className) {
        return null;
    }

    /**
     * Create sequence of panels, etc. for switches: JFrame contains its
     * ContentPane which contains a JPanel with BoxLayout (p1) which contains a
     * JScrollPane (js) which contains the targetPane.
     * Note this is a private menuBar, looking identical to the Editor's _menuBar
     *
     * @param name title for the Switchboard.
     * @return frame containing the switchboard editor.
     */
    public JmriJFrame makeFrame(String name) {
        JmriJFrame targetFrame = new JmriJFrame(name);
        targetFrame.setVisible(true);

        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        menuBar.add(editMenu);
        editMenu.add(new AbstractAction(Bundle.getMessage("OpenEditor")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
                setAllEditable(true);
                log.debug("Switchboard Open Editor menu called");
            }
        });
        targetFrame.setJMenuBar(menuBar);

        targetFrame.addHelpMenu("package.jmri.jmrit.display.SwitchboardEditor", true);
        return targetFrame;
    }

    @Override
    protected void paintTargetPanel(Graphics g) {
        // Switch shapes not directly available from switchboardEditor
    }

    /**
     * Get a beanSwitch object from this SwitchBoard panel by a given name.
     *
     * @param sName name of switch label/connected bean
     * @return BeanSwitch switch object with the given name
     */
    protected BeanSwitch getSwitch(String sName) {
        if (ready && switchesOnBoard.containsKey(sName)) {
            return switchesOnBoard.get(sName);
        }
        log.warn("Switch {} not found on panel. Number of switches displayed: {}", sName, switchesOnBoard.size());
        return null;
    }

    /**
     * Get a list with copies of BeanSwitch objects currently displayed to transfer to
     * Web Server for display.
     *
     * @return list of all BeanSwitch switch object
     */
    public List<BeanSwitch> getSwitches() {
        ArrayList<BeanSwitch> _switches = new ArrayList<>();
        log.debug("N = {}", switchesOnBoard.size());
        if (ready) {
            for (Map.Entry<String, BeanSwitch> bs : switchesOnBoard.entrySet()) {
                _switches.add(bs.getValue());
            }
        }
        return _switches;
    }

    /**
     * Set up item(s) to be copied by paste.
     * <p>
     * Not used on switchboards but has to override Editor.
     */
    @Override
    protected void copyItem(Positionable p) {
    }

    /**
     * Set an object's location when it is created.
     * <p>
     * Not used on switchboards but has to override Editor.
     *
     * @param obj object to position
     */
    @Override
    public void setNextLocation(Positionable obj) {
    }

    /**
     * Create popup for a Positionable object.
     * <p>
     * Not used on switchboards but has to override Editor.
     *
     * @param p     the item on the Panel
     * @param event MouseEvent heard
     */
    @Override
    protected void showPopUp(Positionable p, MouseEvent event) {
    }

    protected ArrayList<Positionable> getSelectionGroup() {
        return null;
    }

    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            getSwitches().forEach((beanSwitch) -> {
                if (bean.equals(beanSwitch.getNamedBean())) {
                    report.add(new NamedBeanUsageReport("SwitchBoard", getName()));
                }
            });
        }
        return report;
    }

    public int getTileSize() { //tmp synchronized
        return _tileSize; // initially 100
    }

    /**
     * Set connected Lights (only).
     *
     * @param on state to set Light.ON or Light.OFF
     */
    public void switchAllLights(int on) {
        if (ready) {
            for (BeanSwitch bs : switchesOnBoard.values()) {
                bs.switchLight(on);
            }
        }
    }

    /**
     * Configure the combo box listing managers.
     * Adapted from AbstractTableAction.
     */
    protected void configureManagerComboBoxes() {
        LightManager defaultManagerL = InstanceManager.getDefault(LightManager.class);
        if (defaultManagerL instanceof ProxyManager) {
            lightManComboBox.setManagers(defaultManagerL);
        } else {
            lightManComboBox.setManagers(lightManager);
        }

        SensorManager defaultManagerS = InstanceManager.getDefault(SensorManager.class);
        if (defaultManagerS instanceof ProxyManager) {
            sensorManComboBox.setManagers(defaultManagerS);
            log.debug("using PROXYmanager for Sensors");
        } else {
            sensorManComboBox.setManagers(sensorManager);
        }

        TurnoutManager defaultManagerT = InstanceManager.getDefault(TurnoutManager.class);
        if (defaultManagerT instanceof ProxyManager) {
            turnoutManComboBox.setManagers(defaultManagerT);
            log.debug("using PROXYmanager for Turnouts");
        } else {
            turnoutManComboBox.setManagers(turnoutManager);
        }
    }
        // TODO store current selection in prefman

    /**
     * Show only one of the manuf (manager) combo boxes.
     *
     * @param type one of the three NamedBean types as String
     */
    protected void displayManagerComboBoxes(String type) {
        _type = type;
        if (type.equals(LIGHT)) {
            Manager<Light> manager = lightManComboBox.getSelectedItem();
            if (manager != null) {
                memo = manager.getMemo();
            }
            turnoutManComboBox.setVisible(false);
            sensorManComboBox.setVisible(false);
            lightManComboBox.setVisible(true);
            log.debug("BOX for LightManager set. LightManComboVisible={}", lightManComboBox.isVisible());
        } else if (type.equals(SENSOR)) {
            Manager<Sensor> manager = sensorManComboBox.getSelectedItem();
            if (manager != null) {
                memo = manager.getMemo();
            }
            turnoutManComboBox.setVisible(false);
            sensorManComboBox.setVisible(true);
            lightManComboBox.setVisible(false);
            log.debug("BOX for SensorManager set. SensorManComboVisible={}", sensorManComboBox.isVisible());
        } else { // TURNOUT
            Manager<Turnout> manager = turnoutManComboBox.getSelectedItem();
            if (manager != null) {
                memo = manager.getMemo();
            }
            turnoutManComboBox.setVisible(true);
            sensorManComboBox.setVisible(false);
            lightManComboBox.setVisible(false);
            log.debug("BOX for TurnoutManager set. TurnoutManComboVisible={}", turnoutManComboBox.isVisible());
        }
    }

    public void setIconScale(int size) {
        _iconSquare = size;
        // also set the scale radio menu items, all 3 are in sizeGroup so will auto deselect
        if (size < 100) {
            sizeSmall.setSelected(true);
        } else if (size > 100) {
            sizeLarge.setSelected(true);
        } else {
            sizeDefault.setSelected(true);
        }
        updatePressed();
    }

    public int getIconScale() {
    return _iconSquare;
    }

    private final static Logger log = LoggerFactory.getLogger(SwitchboardEditor.class);

}
