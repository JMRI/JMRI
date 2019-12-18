package jmri.jmrit.display.switchboardEditor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.jmrit.display.CoordinateEdit;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrit.display.ToolTip;
import jmri.util.ColorUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriColorChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple editor for adding jmri.jmrit.display.switchBoard items to a
 * JLayeredPane inside a captive JFrame. Primary use is for new users.
 * <p>
 * GUI is structured as a separate setup panel to set the visible range and type
 * plus menus.
 * <p>
 * All created objects are put insite a GridLayout grid. No special use of the
 * LayeredPane layers. Inspired by Oracle JLayeredPane demo.
 * <p>
 * The "switchlist" List keeps track of all the objects added to the target
 * frame for later manipulation. May be used in an update to store mixed
 * switchboards with more than 1 connection and more than 1 bean type/range.
 * <p>
 * No DnD as panels will be automatically populated in order of the DCC address.
 * New beans may be created from the Switchboard by right clicking an
 * unconnected switch.
 *
 * @author Pete Cressman Copyright (c) 2009, 2010, 2011
 * @author Egbert Broerse Copyright (c) 2017, 2018
 */
public class SwitchboardEditor extends Editor {

    protected JMenuBar _menuBar;
    private JMenu _editorMenu;
    protected JMenu _editMenu;
    protected JMenu _fileMenu;
    protected JMenu _optionMenu;
    private ArrayList<Positionable> _secondSelectionGroup;
    private transient boolean panelChanged = false;

    // Switchboard items
    private JPanel navBarPanel = null;
    ImageIcon iconPrev = new ImageIcon("resources/icons/misc/gui3/LafLeftArrow_m.gif");
    private final JLabel prev = new JLabel(iconPrev);
    ImageIcon iconNext = new ImageIcon("resources/icons/misc/gui3/LafRightArrow_m.gif");
    private final JLabel next = new JLabel(iconNext);
    private final int rangeBottom = 1;
    private final int rangeTop = 100000; // for MERG etc where thousands = node number, total number on board limited to unconnectedRangeLimit anyway
    private final int unconnectedRangeLimit = 400;
    private final int rangeSizeWarning = 250;
    private final int initialMax = 24;
    private final JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(rangeBottom, rangeBottom, rangeTop - 1, 1));
    private final JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(initialMax, rangeBottom + 1, rangeTop, 1));
    private final JCheckBox hideUnconnected = new JCheckBox(Bundle.getMessage("CheckBoxHideUnconnected"));
    private final JCheckBox autoItemRange = new JCheckBox(Bundle.getMessage("CheckBoxAutoItemRange"));
    private TargetPane switchboardLayeredPane; // JLayeredPane
    static final String TURNOUT = Bundle.getMessage("Turnouts");
    static final String SENSOR = Bundle.getMessage("Sensors");
    static final String LIGHT = Bundle.getMessage("Lights");
    private final String[] beanTypeStrings = {TURNOUT, SENSOR, LIGHT};
    private JComboBox beanTypeList;
    private char beanTypeChar;
    JSpinner columns = new JSpinner(new SpinnerNumberModel(3, 1, 25, 1)); // columns is actually used for the number of rows
    private final String[] switchShapeStrings = {
        Bundle.getMessage("Buttons"),
        Bundle.getMessage("Sliders"),
        Bundle.getMessage("Keys"),
        Bundle.getMessage("Symbols")
    };
    private JComboBox<String> switchShapeList;
    private List<String> beanManuPrefixes = new ArrayList<>();
    private JComboBox<String> beanManuNames;
    private TitledBorder border;
    private final String interact = Bundle.getMessage("SwitchboardInteractHint");
    private final String noInteract = Bundle.getMessage("SwitchboardNoInteractHint");

    // editor items (adapted from LayoutEditor toolbar)
    private Color defaultTextColor = Color.BLACK;
    private boolean _hideUnconnected = false;
    private boolean _autoItemRange = true;
    private final JTextArea help2 = new JTextArea(Bundle.getMessage("Help2"));
    private final JTextArea help3 = new JTextArea(Bundle.getMessage("Help3", Bundle.getMessage("CheckBoxHideUnconnected")));
    // saved state of options when panel was loaded or created
    private transient boolean savedEditMode = true;
    private transient boolean savedControlLayout = true; // menu option to turn this off
    private final int height = 300;
    private final int width = 300;

    private final JCheckBoxMenuItem controllingBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxControlling"));
    private final JCheckBoxMenuItem hideUnconnectedBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxHideUnconnected"));
    private final JCheckBoxMenuItem autoItemRangeBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxAutoItemRange"));
    private final JCheckBoxMenuItem showToolTipBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxShowTooltips"));
    private final JRadioButtonMenuItem scrollBoth = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
    private final JRadioButtonMenuItem scrollNone = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
    private final JRadioButtonMenuItem scrollHorizontal = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
    private final JRadioButtonMenuItem scrollVertical = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));

    // Action commands
    private static String LAYER_COMMAND = "layer";
    private static String MANU_COMMAND = "manufacturer";
    private static String SWITCHTYPE_COMMAND = "switchshape";

    /**
     * List of names/labels of all switches currently displayed. Refreshed
     * during {@link #updatePressed()}
     */
    private final List<String> switchlist = new ArrayList<>();
    /**
     * List with copies of BeanSwitch objects currently displayed to display on
     * Web Server. Created by {@link #getSwitches()}
     */
    protected ArrayList<BeanSwitch> _switches = new ArrayList<>();

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
    @SuppressWarnings("unchecked") // AbstractProxyManager of the right type is type-safe by definition
    @Override
    protected void init(String name) {
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
        beanTypeList.setSelectedIndex(0); // select bean type in comboBox
        beanTypeList.setActionCommand(LAYER_COMMAND);
        beanTypeList.addActionListener(this);
        beanSetupPane.add(beanTypeList);

        // add connection selection comboBox
        beanTypeChar = getSwitchType().charAt(0); // translate from selectedIndex to char
        log.debug("beanTypeChar set to [{}]", beanTypeChar);
        JLabel beanManuTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ConnectionLabel")));
        beanSetupPane.add(beanManuTitle);
        beanManuNames = new JComboBox<>();
        if (getManager(beanTypeChar) instanceof jmri.managers.AbstractProxyManager) { // from abstractTableTabAction
            jmri.managers.AbstractProxyManager proxy = (jmri.managers.AbstractProxyManager) getManager(beanTypeChar);
            List<jmri.Manager<?>> managerList = proxy.getManagerList(); // picks up all managers to fetch
            for (int x = 0; x < managerList.size(); x++) {
                String manuPrefix = managerList.get(x).getSystemPrefix();
                log.debug("Prefix{} = [{}]", x, manuPrefix);
                String manuName = managerList.get(x).getMemo().getUserName();
                log.debug("Connection name {} = [{}]", x, manuName);
                beanManuNames.addItem(manuName);  // add to comboBox
                beanManuPrefixes.add(manuPrefix); // add to list
            }
        } else {
            String manuPrefix = getManager(beanTypeChar).getSystemPrefix();
            String manuName = getManager(beanTypeChar).getMemo().getUserName();
            beanManuNames.addItem(manuName);
            beanManuPrefixes.add(manuPrefix); // add to list (as only item)
        }
        beanManuNames.setSelectedIndex(0); // defaults to Internal on init()
        beanManuNames.setActionCommand(MANU_COMMAND);
        beanManuNames.addActionListener(this);
        beanSetupPane.add(beanManuNames);
        add(beanSetupPane);

        // add shape combobox
        JPanel switchShapePane = new JPanel();
        switchShapePane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JLabel switchShapeTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SwitchShape")));
        switchShapePane.add(switchShapeTitle);
        switchShapeList = new JComboBox<>(switchShapeStrings);
        switchShapeList.setSelectedIndex(0); // select Button choice in comboBox
        switchShapeList.setActionCommand(SWITCHTYPE_COMMAND);
        switchShapeList.addActionListener(this);
        switchShapePane.add(switchShapeList);
        // add column spinner
        JLabel rowsLabel = new JLabel(Bundle.getMessage("NumberOfRows"));
        switchShapePane.add(rowsLabel);
        switchShapePane.add(columns);
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
        hideUnconnected.setSelected(hideUnconnected());
        log.debug("hideUnconnectedBox set to {}", hideUnconnected.isSelected());
        hideUnconnected.addActionListener((ActionEvent event) -> {
            setHideUnconnected(hideUnconnected.isSelected());
            hideUnconnectedBox.setSelected(hideUnconnected()); // also (un)check the box on the menu
            help2.setVisible(!hideUnconnected() && (switchlist.size() != 0)); // and show/hide instruction line unless no items on board
        });
        checkboxPane.add(hideUnconnected);
        add(checkboxPane);

        switchboardLayeredPane.setLayout(new GridLayout(3, 8)); // initial layout params
        // TODO do some calculation from JPanel size, icon size and determine optimal cols/rows
        // Add at least 1 switch to pane to create switchList: done later, would be deleted soon

        // provide a JLayeredPane to place the switches on
        super.setTargetPanel(switchboardLayeredPane, makeFrame(name));
        super.getTargetFrame().setSize(550, 330); // width x height
        // TODO: Add component listener to handle frame resizing event

        // set scrollbar initial state
        setScroll(SCROLL_NONE);
        scrollNone.setSelected(true);
        super.setDefaultToolTip(new ToolTip(null, 0, 0, new Font("Serif", Font.PLAIN, 12),
                Color.black, new Color(255, 250, 210), Color.black));
        // register the resulting panel for later configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(this);
        }

        add(createControlPanel());

        JPanel updatePanel = new JPanel();
        JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        updateButton.addActionListener((ActionEvent event) -> {
            log.debug("Update clicked");
            updatePressed();
            setDirty();
        });
        updatePanel.add(updateButton);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(updatePanel);

        setupEditorPane(); // re-layout all the toolbar items
        updatePressed();   // refresh default Switchboard, updates all buttons
        pack();

        // TODO choose your own icons
//        class makeCatalog extends SwingWorker<CatalogPanel, Object> {
//
//            @Override
//            public CatalogPanel doInBackground() {
//                return CatalogPanel.makeDefaultCatalog();
//            }
//        }
//        (new makeCatalog()).execute();
//        log.debug("Init SwingWorker launched");
    }

    /**
     * Create a new set of switches after removing the current array.
     * <p>
     * Called by Update button click and automatically after loading a panel
     * from XML (with all saved options set).
     */
    public void updatePressed() {
        log.debug("update _hideUnconnected = {}", _hideUnconnected);
        if (_hideUnconnected && !hideUnconnected.isSelected()){
            hideUnconnected.setSelected(true);
        }
        log.debug("update _autoItemRange = {}", _autoItemRange);
        if (_autoItemRange && !autoItemRange.isSelected()){
            autoItemRange.setSelected(true);
        }
        log.debug("update _editable = {}", _editable);
        setVisible(_editable); // show/hide editor
        log.debug("update _controlLayout = {}", allControlling());

        // update selected address range
        int range = (Integer) maxSpinner.getValue() - (Integer) minSpinner.getValue() + 1;
        if (range > unconnectedRangeLimit && !hideUnconnected()) {
            range = unconnectedRangeLimit;
            maxSpinner.setValue((Integer) minSpinner.getValue() + range - 1); // fixed maximum number of items on a Switchboard to prevent memory overflow
        }
        // check for extreme number of items
        log.debug("address range = {}", range);
        if (range > rangeSizeWarning) {
            // ask user if range is indeed desired
            log.debug("Warning for big range");
            int retval = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("LargeRangeWarning", range, Bundle.getMessage("CheckBoxHideUnconnected")),
                    Bundle.getMessage("WarningTitle"),
                    0, JOptionPane.INFORMATION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonCancel")}, null);
            log.debug("Retval: {}", retval);
            if (retval != 0) {
                return;
            }
        }
        // if range is confirmed, go ahead with switchboard update
        for (int i = switchlist.size() - 1; i >= 0; i--) {
            // deleting items starting from 0 will result in skipping the even numbered items
            switchboardLayeredPane.remove(i);
        }
        switchlist.clear(); // reset list
        log.debug("switchlist cleared, size is now: {}", switchlist.size());
        switchboardLayeredPane.setSize(width, height);

        switchboardLayeredPane.setLayout(new GridLayout(Math.max((Integer) columns.getValue() % range, 1),
                (Integer) columns.getValue())); // vertical, horizontal
        log.debug("adding range for manu index {}", beanManuNames.getSelectedIndex());
        addSwitchRange((Integer) minSpinner.getValue(),
                (Integer) maxSpinner.getValue(),
                beanTypeList.getSelectedIndex(),
                beanManuPrefixes.get(beanManuNames.getSelectedIndex()),
                switchShapeList.getSelectedIndex());
        // update the title at the bottom of the switchboard to match (no) layout control
        border.setTitle(beanManuNames.getSelectedItem().toString() + " "
                + beanTypeList.getSelectedItem().toString() + " - "
                + (allControlling() ? interact : noInteract));
        help3.setVisible(switchlist.size() == 0); // show/hide help3 warning
        help2.setVisible(switchlist.size() != 0); // hide help2 when help3 is shown vice versa (as no items are dimmed or not)
        pack();
        switchboardLayeredPane.repaint();
    }

    /**
     * From default or user entry in Editor, fill the _targetpane with a series
     * of Switches.
     * <p>
     * Items in range that can connect to existing beans in JMRI are active. The
     * others are greyed out. Option to later connect (new) beans to switches.
     *
     * @param min         starting ordinal of Switch address range
     * @param max         highest ordinal of Switch address range
     * @param beanType    index of selected item in Type comboBox, either T, S
     *                    or L
     * @param manuPrefix  selected item in Connection comboBox, filled from
     *                    active connections
     * @param switchShape index of selected visual presentation of Switch shape
     *                    selected in Type comboBox, choose either a JButton
     *                    showing the name or (to do) a graphic image
     */
    private void addSwitchRange(int min, int max, int beanType, String manuPrefix, int switchShape) {
        log.debug("_hideUnconnected = {}", hideUnconnected());
        String name;
        BeanSwitch _switch;
        NamedBean nb;
        String _manu = manuPrefix; // cannot use All group as in Tables
        log.debug("_manu = {}", _manu);
        String _insert = "";
        if (_manu.startsWith("M")) {
            _insert = "+"; // for CANbus.MERG On event
        }
        for (int i = min; i <= max; i++) {
            switch (beanType) {
                case 0:
                    name = _manu + "T" + _insert + i;
                    nb = jmri.InstanceManager.turnoutManagerInstance().getTurnout(name);
                    break;
                case 1:
                    name = _manu + "S" + _insert + i;
                    nb = jmri.InstanceManager.sensorManagerInstance().getSensor(name);
                    break;
                case 2:
                    name = _manu + "L" + _insert + i;
                    nb = jmri.InstanceManager.lightManagerInstance().getLight(name);
                    break;
                default:
                    log.error("addSwitchRange: cannot parse bean name. manuPrefix = {}; i = {}", manuPrefix, i);
                    return;
            }
            if (nb == null && hideUnconnected()) {
                continue; // skip i
            }
            _switch = new BeanSwitch(i, nb, name, switchShape, this); // add button instance i
            if (nb == null) {
                _switch.setEnabled(false); // not connected
            } else {
                // set switch to display current bean state
                _switch.displayState(nb.getState());
            }
            switchboardLayeredPane.add(_switch);
            switchlist.add(name); // add to total number of switches on JLayeredPane
            log.debug("Added switch {}", name);
            // keep total number of switches below practical total of 400 (20 x 20 items)
            if (switchlist.size() >= unconnectedRangeLimit) {
                log.warn("switchboards are limited to {} items", unconnectedRangeLimit);
                break;
            }
        }
    }

    /**
     * Create the setup pane for the top of the frame. From layeredpane demo
     */
    private JPanel createControlPanel() {
        JPanel controls = new JPanel();

        // navigation top row and range to set
        navBarPanel = new JPanel();
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
                log.debug("new prev range = {}, newMin ={}, newMax ={}", range, getMinSpinner(), getMaxSpinner());
            }
        });
        prev.setToolTipText(Bundle.getMessage("PreviousToolTip", Bundle.getMessage("CheckBoxAutoItemRange")));
        navBarPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("From"))));
        JComponent minEditor = minSpinner.getEditor();
        // enlarge minSpinner editor text field width
        JFormattedTextField minTf = ((JSpinner.DefaultEditor) minEditor).getTextField();
        minTf.setColumns(5);
        minSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                int value = (int)spinner.getValue();
                // stop if value >= maxSpinner -1 (range <= 0)
                if (value >= (Integer) maxSpinner.getValue() - 1) {
                    maxSpinner.setValue(value + 1);
                }
            }
        });
        navBarPanel.add(minSpinner);
        navBarPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("UpTo"))));
        // enlarge maxSpinner editor text field width
        JComponent maxEditor = maxSpinner.getEditor();
        JFormattedTextField maxTf = ((JSpinner.DefaultEditor) maxEditor).getTextField();
        maxTf.setColumns(5);
        maxSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                int value = (int)spinner.getValue();
                // stop if value <= minSpinner + 1 (range <= 0)
                if (value <= (Integer) minSpinner.getValue() + 1) {
                    minSpinner.setValue(value - 1);
                }
            }
        });
        navBarPanel.add(maxSpinner);

        navBarPanel.add(next);
        next.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int oldMin = getMinSpinner();
                int oldMax = getMaxSpinner();
                int range = Math.max(oldMax - oldMin + 1, 1); // make sure range > 0
                log.debug("nxt range was {}, oldMin ={}, oldMax ={}", range, oldMin, oldMax);
                setMaxSpinner(Math.min((oldMax + range), rangeTop));               // first set new max
                if (_autoItemRange) {
                    setMinSpinner(Math.min(oldMin + range, rangeTop - range + 1)); // set new min (only if auto)
                }
                log.debug("new nxt range = {}, newMin ={}, newMax ={}", range, getMinSpinner(), getMaxSpinner());
            }
        });
        next.setToolTipText(Bundle.getMessage("NextToolTip", Bundle.getMessage("CheckBoxAutoItemRange")));
        navBarPanel.add(Box.createHorizontalGlue());

        controls.add(navBarPanel); // put items on 2nd Editor Panel
        controls.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRangeTitle")));
        return controls;
    }

    private int getMinSpinner() {
        return (Integer) minSpinner.getValue();
    }

    private int getMaxSpinner() {
        return (Integer) maxSpinner.getValue();
    }

    private void setMinSpinner(int value) {
        minSpinner.setValue(value);
    }

    private void setMaxSpinner(int value) {
        maxSpinner.setValue(value);
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
        if (!hideUnconnected()) {
            help2.setVisible(false); // hide this text when hideUnconnected() is set to true from menu or checkbox
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
            border.setTitle(beanManuNames.getSelectedItem().toString() + " "
                    + beanTypeList.getSelectedItem().toString() + " - "
                    + (allControlling() ? interact : noInteract));
            switchboardLayeredPane.repaint();
            log.debug("border title updated");
        });
        controllingBox.setSelected(allControlling());
        // hideUnconnected item
        _optionMenu.add(hideUnconnectedBox);
        hideUnconnectedBox.addActionListener((ActionEvent event) -> {
            setHideUnconnected(hideUnconnectedBox.isSelected());
            hideUnconnected.setSelected(hideUnconnected()); // also (un)check the box on the editor
            help2.setVisible(!hideUnconnected() && (switchlist.size() != 0)); // and show/hide instruction line unless no items on board
        });
        hideUnconnectedBox.setSelected(hideUnconnected());
        // autoItemRange item
        _optionMenu.add(autoItemRangeBox);
        autoItemRangeBox.addActionListener((ActionEvent event) -> {
            setAutoItemRange(autoItemRangeBox.isSelected());
            autoItemRange.setSelected(autoItemRange()); // also (un)check the box on the editor
        });
        autoItemRangeBox.setSelected(autoItemRange());
        // show tooltip item
        _optionMenu.add(showToolTipBox);
        showToolTipBox.addActionListener((ActionEvent e) -> {
            setAllShowToolTip(showToolTipBox.isSelected());
        });
        showToolTipBox.setSelected(showToolTip());

        // Show/Hide Scroll Bars
        JMenu scrollMenu = new JMenu(Bundle.getMessage("ComboBoxScrollable"));
        _optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.addActionListener((ActionEvent event) -> {
            setScroll(SCROLL_BOTH);
        });
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.addActionListener((ActionEvent event) -> {
            setScroll(SCROLL_NONE);
        });
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.addActionListener((ActionEvent event) -> {
            setScroll(SCROLL_HORIZONTAL);
        });
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.addActionListener((ActionEvent event) -> {
            setScroll(SCROLL_VERTICAL);
        });
        // add background color menu item
        JMenuItem backgroundColorMenuItem = new JMenuItem(Bundle.getMessage("SetBackgroundColor", "..."));
        _optionMenu.add(backgroundColorMenuItem);

        backgroundColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                                 Bundle.getMessage("SetBackgroundColor", ""),
                                 defaultBackgroundColor);
            if (desiredColor!=null && !defaultBackgroundColor.equals(desiredColor)) {
               // if new bgColor matches the defaultTextColor, ask user as labels will become unreadable
               if (desiredColor.equals(defaultTextColor)) {
                  int retval = JOptionPane.showOptionDialog(null,
                               Bundle.getMessage("ColorIdenticalWarning"), Bundle.getMessage("WarningTitle"),
                               0, JOptionPane.INFORMATION_MESSAGE, null,
                               new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")}, null);
                  log.debug("Retval: {}", retval);
                  if (retval != 0) {
                     return;
                  }
               }
               defaultBackgroundColor = desiredColor;
               setBackgroundColor(desiredColor);
               setDirty(true);
               switchboardLayeredPane.repaint();
           }
        });

        // add text color menu item
        JMenuItem textColorMenuItem = new JMenuItem(Bundle.getMessage("DefaultTextColor", "..."));
        _optionMenu.add(textColorMenuItem);

        textColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                                 Bundle.getMessage("DefaultTextColor", ""),
                                 defaultTextColor);
            if (desiredColor!=null && !defaultTextColor.equals(desiredColor)) {
               // if new defaultTextColor matches bgColor, ask user as labels will become unreadable
               if (desiredColor.equals(defaultBackgroundColor)) {
                  int retval = JOptionPane.showOptionDialog(null,
                  Bundle.getMessage("ColorIdenticalWarning"), Bundle.getMessage("WarningTitle"),
                  0, JOptionPane.INFORMATION_MESSAGE, null,
                  new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")}, null);
                  log.debug("Retval: {}", retval);
                  if (retval != 0) {
                     return;
                  }
               }
               defaultTextColor = desiredColor;
               setDirty(true);
               switchboardLayeredPane.repaint();
               JmriColorChooser.addRecentColor(desiredColor);
            }
        });

    }

    private void makeFileMenu() {
        _fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        _menuBar.add(_fileMenu, 0);
        _fileMenu.add(new jmri.jmrit.display.NewPanelAction(Bundle.getMessage("MenuItemNew")));

        _fileMenu.add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStore")));

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

    /**
     * Load from xml and set bg color of _targetpanel as well as variable.
     *
     * @param color RGB Color for switchboard background and beanSwitches
     */
    public void setDefaultBackgroundColor(Color color) {
        setBackgroundColor(color); // via Editor
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
        log.debug("_editable set to {}", edit);
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
     * Control whether range of items is automatically limited.
     *
     * @param state true to calculate upper limit from lowest value set (default)
     */
    public void setAutoItemRange(boolean state) {
        _autoItemRange = state;
    }

    public boolean autoItemRange() {
        return _autoItemRange;
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
     */
    public void setDirty(boolean val) {
        panelChanged = val;
    }

    public void setDirty() {
        setDirty(true);
    }

    /**
     * Check the dirty state.
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
     * @return lowest address to show
     */
    public int getPanelMenuRangeMin() {
        return (int) minSpinner.getValue();
    }

    /**
     * Store Range maximum.
     *
     * @return highest address to show
     */
    public int getPanelMenuRangeMax() {
        return (int) maxSpinner.getValue();
    }

    private String typePrefix;
    private String type;

    // ***************** Store & Load xml ********************
    /**
     * Store bean type.
     *
     * @return bean type prefix
     */
    public String getSwitchType() {
        String switchType = beanTypeList.getSelectedItem().toString();
        if (switchType.equals(LIGHT)) { // switch-case doesn't work here
            typePrefix = "L";
        } else if (switchType.equals(SENSOR)) {
            typePrefix = "S";
        } else { // Turnout
            typePrefix = "T";
        }
        return typePrefix;
    }

    /**
     * Get bean type name.
     *
     * @return bean type name
     */
    public String getSwitchTypeName() {
        return type;
    }

    /**
     * Load bean type.
     *
     * @param prefix the bean type prefix
     */
    public void setSwitchType(String prefix) {
        typePrefix = prefix;
        switch (typePrefix) {
            case "L":
                type = LIGHT;
                break;
            case "S":
                type = SENSOR;
                break;
            default: // Turnouts
                type = TURNOUT;
        }
        try {
            beanTypeList.setSelectedItem(type);
        } catch (IllegalArgumentException e) {
            log.error("invalid bean type [{}] in Switchboard", typePrefix);
        }
    }

    /**
     * Store connection type.
     *
     * @return bean connection prefix
     */
    public String getSwitchManu() {
        return this.beanManuPrefixes.get(beanManuNames.getSelectedIndex());
    }

    /**
     * Load connection type.
     *
     * @param manuPrefix connection prefix
     */
    public void setSwitchManu(String manuPrefix) {
        int choice = 0;
        for (int i = 0; i < beanManuPrefixes.size(); i++) {
            if (beanManuPrefixes.get(i).equals(manuPrefix)) {
                choice = i;
                break;
            }
        }
        try {
            beanManuNames.setSelectedIndex(choice);
            log.debug("beanManuNames combo set to {} for {}", choice, manuPrefix);
        } catch (IllegalArgumentException e) {
            log.error("invalid connection [{}] in Switchboard", manuPrefix);
        }
    }

    /**
     * Store switch shape.
     *
     * @return bean shape prefix
     */
    public String getSwitchShape() {
        String shape;
        int shapeChoice = switchShapeList.getSelectedIndex();
        switch (shapeChoice) {
            case 1:
                shape = "icon";
                break;
            case 2:
                shape = "drawing";
                break;
            case 3:
                shape = "symbol";
                break;
            default:
                // Turnout
                shape = "button";
                break;
        }
        return shape;
    }

    /**
     * Load switch shape.
     *
     * @param switchShape name of switch shape
     */
    public void setSwitchShape(String switchShape) {
        int shape;
        switch (switchShape) {
            case "icon":
                shape = 1;
                break;
            case "drawing":
                shape = 2;
                break;
            case "symbol":
                shape = 3;
                break;
            default: // button
                shape = 0;
        }
        try {
            switchShapeList.setSelectedIndex(shape);
        } catch (IllegalArgumentException e) {
            log.error("invalid switch shape [{}] in Switchboard", shape);
        }
    }

    public void setBoardToolTip(ToolTip tip) {
        setToolTip(tip);
    }

    /**
     * Store Switchboard column spinner.
     *
     * @return the number of switches to display per row
     */
    public int getColumns() {
        return (Integer) columns.getValue();
    }

    /**
     * Load Switchboard column spinner.
     *
     * @param cols the number of switches to display per row (as text)
     */
    public void setColumns(int cols) {
        columns.setValue(cols);
    }

    // all content loaded from file.
    public void loadComplete() {
        log.debug("loadComplete");
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

    protected Manager getManager(char typeChar) {
        switch (typeChar) {
            case 'T': // Turnout
                return InstanceManager.turnoutManagerInstance();
            case 'S': // Sensor
                return InstanceManager.sensorManagerInstance();
            case 'L': // Light
                return InstanceManager.lightManagerInstance();
            default:
                log.error("Unexpected bean type character \"{}\" found.", typeChar);
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
        int x = 0;
        int y = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
            case KeyEvent.VK_NUMPAD8:
                y = -1;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
            case KeyEvent.VK_NUMPAD2:
                y = 1;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
            case KeyEvent.VK_NUMPAD4:
                x = -1;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
            case KeyEvent.VK_NUMPAD6:
                x = 1;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_A:
            case KeyEvent.VK_INSERT:
            case KeyEvent.VK_PLUS:
                break;
            default:
                return;
        }
        if (e.isShiftDown()) {
            x *= 5;
            y *= 5;
        }
        if (_selectionGroup != null) {
            for (Positionable comp : _selectionGroup) {
                moveItem(comp, x, y);
            }
        }
        repaint();
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
        _targetPanel.repaint(); // needed for ToolTip
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
        targetWindowClosing(save);
    }

    /**
     * Create sequence of panels, etc. for switches: JFrame contains its
     * ContentPane which contains a JPanel with BoxLayout (p1) which contains a
     * JScollPane (js) which contains the targetPane.
     * Note this is a private menuBar, looking identical to the Editor's _menuBar
     *
     * @param name title for the Switchboard
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

    protected void setSecondSelectionGroup(ArrayList<Positionable> list) {
        _secondSelectionGroup = list;
    }

    @Override
    protected void paintTargetPanel(Graphics g) {
        // needed to create PositionablePolygon
        //_shapeDrawer.paint(g);
        if (_secondSelectionGroup != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(150, 150, 255));
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            for (Positionable p : _secondSelectionGroup) {
                if (!(p instanceof jmri.jmrit.display.controlPanelEditor.shape.PositionableShape)) {
                    g.drawRect(p.getX(), p.getY(), p.maxWidth(), p.maxHeight());
                }
            }
        }
    }

    /**
     * Get a beanSwitch object from this switchBoard panel by a given name.
     *
     * @param sName name of switch label/connected bean
     * @return beanSwitch switch object with the given name
     */
    protected BeanSwitch getSwitch(String sName) {
        for (int i = 0; i < switchlist.size(); i++) {
            log.debug("comparing switch {} to {}", switchlist.get(i), sName);
            if (switchlist.get(i).equals(sName)) {
                return (BeanSwitch) switchboardLayeredPane.getComponent(i);
            } else {
                log.warn("switch {} not found on panel", sName);
            }
        }
        return null;
    }

    public List<BeanSwitch> getSwitches() {
        _switches.clear(); // reset list
        log.debug("N = {}", switchlist.size());
        for (int i = 0; i < switchlist.size(); i++) {
            _switches.add((BeanSwitch) switchboardLayeredPane.getComponent(i));
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
        obj.setLocation(0, 0);
    }

    /**
     * Create popup for a Positionable object.
     * <p>
     * Popup items common to all positionable objects are done before and after
     * the items that pertain only to specific Positionable types.
     * <p>
     * Not used on switchboards but has to override Editor.
     *
     * @param p     the item on the Panel
     * @param event MouseEvent heard
     */
    @Override
    protected void showPopUp(Positionable p, MouseEvent event) {
        _currentSelection = null;
    }

    protected ArrayList<Positionable> getSelectionGroup() {
        return _selectionGroup;
    }

    private final static Logger log = LoggerFactory.getLogger(SwitchboardEditor.class);

}
