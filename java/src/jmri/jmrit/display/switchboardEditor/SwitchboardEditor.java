package jmri.jmrit.display.switchboardEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.beantable.AddNewDevicePanel;
import jmri.jmrit.display.CoordinateEdit;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.util.ColorUtil;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple editor for adding jmri.jmrit.display.switchBoard items
 * to a JLayeredPane inside a captive JFrame. Primary use is for new users.
 * <p>
 * GUI is structured as a separate setup panel to set the visible range and type
 * plus menus.
 * <p>
 * All created objects are put insite a GridLayout grid.
 * No special use of the LayeredPane layers. Inspired by Oracle JLayeredPane demo.
 * <p>
 * The "contents" List keeps track of all the objects added to the target frame
 * for later manipulation. May be used in an update to store mixed switchboards
 * with more than 1 connection and more than 1 bean type/range.
 * <p>
 * No DnD as panels will be automatically populated in order of the DCC address.
 * New beans may be created from the Switchboard by right clicking an unconnected switch.
 *
 * @author Pete Cressman Copyright (c) 2009, 2010, 2011
 * @author Egbert Broerse Copyright (c) 2017
 *
 */
public class SwitchboardEditor extends Editor {

    protected JMenuBar _menuBar;
    private JMenu _editorMenu;
    protected JMenu _editMenu;
    protected JMenu _fileMenu;
    protected JMenu _optionMenu;
    //protected JMenu _zoomMenu;
    private ArrayList<Positionable> _secondSelectionGroup;
    private ItemPalette _itemPalette;
    //private boolean _disableShapeSelection;
    private boolean panelChanged = false;

    // Switchboard items
    private JPanel navBarPanel = null;
    ImageIcon iconPrev = new ImageIcon("resources/icons/misc/gui3/LafLeftArrow_m.gif");
    private JLabel prev = new JLabel(iconPrev);
    ImageIcon iconNext = new ImageIcon("resources/icons/misc/gui3/LafRightArrow_m.gif");
    private JLabel next = new JLabel(iconNext);
    String rootPath = "resources/icons/misc/switchboard/";
    String iconOffPath = rootPath + "appslide-off-s.png";
    String iconOnPath = rootPath + "appslide-on-s.png";
    String keyOffPath = rootPath + "markl-off-s.png";
    String keyOnPath = rootPath + "markl-on-s.png";
    String symbolOffPath; // = rootPath + "T-off-s.png"; // default for Turnout, replace T by S or L
    String symbolOnPath; // = rootPath + "T-on-s.png";
    private int rangeMin = 1;
    private int rangeMax = 32;
    private int _range = rangeMax - rangeMin;
    JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(rangeMin, rangeMin, rangeMax, 1));
    JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(rangeMax, rangeMin, rangeMax, 1));
    private TargetPane switchboardLayeredPane; // JLayeredPane
    private JCheckBox hideUnconnected;
    static final String _turnout = Bundle.getMessage("BeanNameTurnout");
    static final String _sensor = Bundle.getMessage("BeanNameSensor");
    static final String _light = Bundle.getMessage("BeanNameLight");
    private String[] beanTypeStrings = {_turnout, _sensor, _light};
    private JComboBox beanTypeList;
    private char beanTypeChar;
    JSpinner Columns = new JSpinner(new SpinnerNumberModel(8, 1, 16, 1));
    private String[] switchShapeStrings = {
            Bundle.getMessage("Buttons"),
            Bundle.getMessage("Sliders"),
            Bundle.getMessage("Keys"),
            Bundle.getMessage("Symbols")
    };
    private JComboBox switchShapeList;
    private List<String> beanManuPrefixes = new ArrayList<String>();
    private JComboBox beanManuNames;

    // toolbar (from LE)
    private JPanel floatEditHelpPanel = null;
    private JPanel editToolBarPanel = null;
    private JScrollPane editToolBarScroll = null;
    private JPanel editToolBarContainer = null;
    private Color defaultBackgroundColor = Color.lightGray;
    private Color defaultTextColor = Color.black;
    private ButtonGroup textColorButtonGroup = null;
    private ButtonGroup backgroundColorButtonGroup = null;
    private JRadioButtonMenuItem[] backgroundColorMenuItems = new JRadioButtonMenuItem[13];
    private JRadioButtonMenuItem[] textColorMenuItems = new JRadioButtonMenuItem[13];
    private Color[] textColors = new Color[13];
    private Color[] backgroundColors = new Color[13];
    private int textColorCount = 0;
    private int backgroundColorCount = 0;

    //private JPanel helpBarPanel = null;
    //private JPanel helpBar = new JPanel();
    // option menu items not in Editor
    private boolean showHelpBar = true;
    private boolean _hideUnconnected = false;
    //saved state of options when panel was loaded or created
    private boolean savedEditMode = true; // TODO store/load accordingly
    private boolean savedControlLayout = true; // TODO add menu option to turn this off
    private int height = 100;
    private int width = 100;

    private JCheckBoxMenuItem controllingBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxControlling"));
    private JCheckBoxMenuItem hideUnconnectedBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxHideUnconnected"));
    private JCheckBoxMenuItem showTooltipBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxShowTooltips"));
    private JRadioButtonMenuItem scrollBoth = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
    private JRadioButtonMenuItem scrollNone = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
    private JRadioButtonMenuItem scrollHorizontal = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
    private JRadioButtonMenuItem scrollVertical = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));

    //Action commands
    private static String HIDE_COMMAND = "hideUnconnected";
    private static String LAYER_COMMAND = "layer";
    private static String MANU_COMMAND = "manufacturer";
    private static String SWITCHTYPE_COMMAND = "switchshape";

    private List<String> switchlist = new ArrayList<String>();
    protected ArrayList<BeanSwitch> _switches = new ArrayList<BeanSwitch>();

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
     * Initialize the newly created SwitchBoard.
     *
     * @param name name of the switchboard frame
     */
    @Override
    protected void init(String name) {
        //setVisible(false);
        Container contentPane = this.getContentPane(); // Editor
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // make menus
        setGlobalSetsLocalFlag(false);
        setUseGlobalFlag(false);
        _menuBar = new JMenuBar();
        //makeZoomMenu();
        makeOptionMenu();
        //makeEditMenu();
        makeFileMenu();

        setJMenuBar(_menuBar);
        addHelpMenu("package.jmri.jmrit.display.SwitchboardEditor", true);

        //super.setTargetPanel(null, makeFrame(name)); // original CPE version
        switchboardLayeredPane = new TargetPane(); //extends JLayeredPane();
        switchboardLayeredPane.setPreferredSize(new Dimension(300, 310));
        switchboardLayeredPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(defaultTextColor),
                Bundle.getMessage("SwitchboardBanner"),
                TitledBorder.LEADING,
                TitledBorder.ABOVE_BOTTOM,
                getFont(),
                defaultTextColor));
        // create contrast with background, should also specify border style
        // specify title for turnout, sensor, light, mixed? (wait for the Editor to be created)
        switchboardLayeredPane.addMouseMotionListener(this);

        //Add control pane and layered pane to this JPanel
        JPanel beanSetupPane = new JPanel();
        beanSetupPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JLabel beanTypeTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanTypeLabel")));
        beanSetupPane.add(beanTypeTitle);
        beanTypeList = new JComboBox(beanTypeStrings);
        beanTypeList.setSelectedIndex(0); // select Turnout in comboBox
        beanTypeList.setActionCommand(LAYER_COMMAND);
        beanTypeList.addActionListener(this);
        beanSetupPane.add(beanTypeList);

        //Add connection selection comboBox
        beanTypeChar = getSwitchType().charAt(0); // translate from selectedIndex to char
        log.debug("beanTypeChar set to [{}]", beanTypeChar);
        JLabel beanManuTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ConnectionLabel")));
        beanSetupPane.add(beanManuTitle);
        beanManuNames = new JComboBox();
        if (getManager(beanTypeChar) instanceof jmri.managers.AbstractProxyManager) { // from abstractTableTabAction
            jmri.managers.AbstractProxyManager proxy = (jmri.managers.AbstractProxyManager) getManager(beanTypeChar);
            List<jmri.Manager> managerList = proxy.getManagerList();
            for (int x = 0; x < managerList.size(); x++) {
                String manuPrefix = managerList.get(x).getSystemPrefix();
                log.debug("Prefix = [{}]", manuPrefix);
                String manuName = ConnectionNameFromSystemName.getConnectionName(manuPrefix);
                log.debug("Connection name = [{}]", manuName);
                beanManuNames.addItem(manuName); // add to comboBox
                beanManuPrefixes.add(manuPrefix); // add to list
            }
        } else {
            String manuPrefix = getManager(beanTypeChar).getSystemPrefix();
            String manuName = ConnectionNameFromSystemName.getConnectionName(manuPrefix);
            beanManuNames.addItem(manuName);
            beanManuPrefixes.add(manuPrefix); // add to list (as only item)
        }
        beanManuNames.setSelectedIndex(0);
        beanManuNames.setActionCommand(MANU_COMMAND);
        beanManuNames.addActionListener(this);
        beanSetupPane.add(beanManuNames);
        add(beanSetupPane);

        // add shape combobox
        JPanel switchShapePane = new JPanel();
        switchShapePane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JLabel switchShapeTitle = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SwitchShape")));
        switchShapePane.add(switchShapeTitle);
        switchShapeList = new JComboBox(switchShapeStrings);
        switchShapeList.setSelectedIndex(0); // select Button in comboBox
        switchShapeList.setActionCommand(SWITCHTYPE_COMMAND);
        switchShapeList.addActionListener(this);
        switchShapePane.add(switchShapeList);
        // add column spinner
        JLabel columnLabel = new JLabel(Bundle.getMessage("NumberOfColumns"));
        switchShapePane.add(columnLabel);
        switchShapePane.add(Columns);
        add(switchShapePane);

        JCheckBox hideUnconnected = new JCheckBox(Bundle.getMessage("CheckBoxHideUnconnected"));
        hideUnconnected.setSelected(hideUnconnected());
        hideUnconnected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setHideUnconnected(hideUnconnected.isSelected());
            }
        });
        add(hideUnconnected);

        // Next, add the buttons to the layered pane.
        switchboardLayeredPane.setLayout(new GridLayout(_range % ((Integer) Columns.getValue()), (Integer) Columns.getValue())); // vertical, horizontal
        // TODO do some calculation from JPanel size, icon size and determine optimal cols/rows
        addSwitchRange((Integer) minSpinner.getValue(), (Integer) maxSpinner.getValue(),
                beanTypeList.getSelectedIndex(),
                beanManuPrefixes.get(beanManuNames.getSelectedIndex()),
                switchShapeList.getSelectedIndex());

        // provide a JLayeredPane to place the switches on
        super.setTargetPanel(switchboardLayeredPane, makeFrame(name));
        super.setTargetPanelSize(300, 300);
        // To do: Add component listener to handle frame resizing event


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
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                log.debug("Update clicked");
                updatePressed();
            }
        });
        updatePanel.add(updateButton);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(updatePanel);

        setupToolBar(); //re-layout all the toolbar items
        updatePressed(); // refresh default Switchboard
        pack();
        setVisible(true);

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
     * Called from Update button and after resizing Switchboard.
     */
    public void updatePressed() {
        for (int i = switchlist.size() - 1; i >=0; i--) {
            // deleting items starting from 0 will result in skipping the even numbered items
            switchboardLayeredPane.remove(i);
        }
                switchlist.clear(); // reset list
                switchboardLayeredPane.setSize(300,300);
        // update selected address range
        _range =(Integer)minSpinner.getValue()-(Integer)maxSpinner.getValue();
                switchboardLayeredPane.setLayout(new GridLayout(_range %((Integer) Columns.getValue()),
                        (Integer)Columns.getValue())); // vertical, horizontal
        addSwitchRange((Integer) minSpinner.
        getValue(), (Integer)maxSpinner.getValue(),
            beanTypeList.getSelectedIndex(),
            beanManuPrefixes.get(beanManuNames.getSelectedIndex()),
            switchShapeList.getSelectedIndex());
        //log.debug("bgcolor: {}", getBackgroundColor().toString() );
        switchboardLayeredPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(defaultTextColor),
                beanManuNames.getSelectedItem().toString() + " " + beanTypeList.getSelectedItem().toString() + " - " + Bundle.getMessage("SwitchboardBanner"),
                TitledBorder.LEADING,
                TitledBorder.ABOVE_BOTTOM,
                getFont(),defaultTextColor));
        pack();
        repaint();
    }

    /**
     * From default or user entry in Editor, fill the _targetpane
     * with a series of Switches.
     * <p>
     * Items in range that can connect
     * to existing beans in JMRI are active. The others are greyed out.
     * Option to later connect (new) beans to switches.
     *
     * @param rangeMin starting ordinal of Switch address range
     * @param rangeMax highest ordinal of Switch address range
     * @param beanType index of selected item in Type comboBox, either T, S or L
     * @param manuPrefix selected item in Connection comboBox, filled from active connections
     * @param switchShape index of selected visual presentation of Switch shape selected in Type comboBox, choose either a JButton showing the name or (to do) a graphic image
     */
    private void addSwitchRange(int rangeMin, int rangeMax, int beanType, String manuPrefix, int switchShape) {
        log.debug("hideUnconnected = {}", hideUnconnected());
        String name;
        BeanSwitch _switch;
        NamedBean nb = null;
        int _currentState;
        String _manu = manuPrefix; // cannot use All group as in Tables
        String _insert = "";
        if (_manu.startsWith("M")) { _insert = "+"; }; // create CANbus.MERG On event
        for (int i = rangeMin; i <= rangeMax; i++) {
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
            _switch = new BeanSwitch(i, nb, name, switchShape); // add button instance i
            log.debug("Added switch {} ({})", i + "", name);
            if (nb == null) {
                _switch.setEnabled(false); // not connected
                if(!hideUnconnected()) {
                    switchboardLayeredPane.add(_switch); // or setVisible(false)
                    switchlist.add(name); // add to total number of switches on JLayeredPane
                } else {
                    // do nothing
                }
            } else {
                // set switch to display current bean state
                _switch.displayState(nb.getState());
                switchboardLayeredPane.add(_switch);
                switchlist.add(name); // add to total number of switches on JLayeredPane
            }
        }
    }

    /**
     * Class for a switchboard object.
     * Contains a JButton or JPanel to control existing turnouts, sensors and lights.
     */
    public class BeanSwitch extends JPanel implements java.beans.PropertyChangeListener, ActionListener {

        //protected HashMap<Integer, NamedIcon> _iconStateMap;     // state int to icon
        //protected HashMap<String, Integer> _name2stateMap;       // name to state
        protected HashMap<Integer, String> _state2nameMap;       // state to name

        private JButton beanButton;
        private final boolean connected = false;
        private int _shape;
        private String _label;
        private String _uname = "unconnected";
        protected String switchLabel;
        protected String switchTooltip;
        protected boolean _text;
        protected boolean _icon = false;
        protected boolean _control = false;
        //protected NamedIcon _namedIcon;

        // the associated Bean object
        private NamedBean _bname;
        private NamedBeanHandle<?> namedBean = null; // could be Turnout, Sensor or Light
        protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
        private IconSwitch beanIcon;
        private IconSwitch beanKey;
        private IconSwitch beanSymbol;
        private String beanManuPrefix;
        private char beanTypeChar;
        private float opac = 0.5f;

        /**
         * Ctor
         *
         * @param index DCC address
         * @param bean layout object to connect to
         * @param switchName descriptive name corresponding with system name to display in switch tooltip, i.e. LT1
         * @param shapeChoice Button, Icon (static) or Drawing (vector graphics)
         */
        public BeanSwitch(int index, NamedBean bean, String switchName, int shapeChoice) {
            _label = switchName;
            log.debug("Name = [{}]", switchName);
            beanButton = new JButton(_label + ":?"); // initial state unknown
            switchLabel = _label + ":?"; // initial state unknown, used on icons
            _bname = bean;
            if (bean != null) {
                _uname = bean.getUserName();
                log.debug("UserName: {}", _uname);
                if (_uname == null){
                    _uname = Bundle.getMessage("NoUserName");
                }
            }
            switchTooltip = switchName + " (" + _uname + ")";
            this.setLayout(new BorderLayout()); // makes JButtons expand to the whole grid cell
            if (shapeChoice != 0) {
                _shape = shapeChoice; // Button
            } else {
                _shape = 0;
            }
            beanManuPrefix = getSwitchManu(); // connection/manufacturer i.e. M for MERG
            beanTypeChar = _label.charAt(beanManuPrefix.length()); // bean type, i.e. L, usually at char(1)
            // check for space char which might be caused by connection name > 2 chars and/or space in name
            if (beanTypeChar != 'T' && beanTypeChar != 'S' && beanTypeChar != 'L') { // add if more bean types are supported
                log.error("invalid char in Switchboard Button \"" + _label + "\". Check connection name.");
                JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSwitchAddFailed"),
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            beanIcon = new IconSwitch(iconOffPath, iconOnPath);
            beanKey = new IconSwitch(keyOffPath, keyOnPath);
            beanSymbol = new IconSwitch(rootPath + beanTypeChar + "-off-s.png", rootPath + beanTypeChar + "-on-s.png");

            // look for bean to connect to by name
            log.debug("beanconnect = {}, beantype = {}", beanManuPrefix, beanTypeChar);
            try {
                if (bean != null) {
                    namedBean = nbhm.getNamedBeanHandle(switchName, bean);
                }
            } catch (IllegalArgumentException e) {
                log.error("invalid bean name= \"" + switchName + "\" in Switchboard Button");
            }
            // attach shape specific code to this beanSwitch
            switch (_shape) {
                case 1: // icon shape
                    log.debug("create Icon");
                    beanIcon.addMouseListener(new MouseAdapter() { // handled by JPanel
                        @Override
                        public void mouseClicked(MouseEvent me) {
                            operate(me, switchName);
                        }
                        @Override
                        public void mousePressed(MouseEvent e) {
                            setToolTip(null); // ends tooltip if displayed
                            if (e.isPopupTrigger()) {
                                // display the popup:
                                showPopUp(e);
                            }
                        }
                    });
                    _text = false;
                    _icon = true;
                    beanIcon.setLabel(switchLabel);
                    beanIcon.positionLabel(17, 45); // provide x, y offset, depending on image size and free space
                    if (showTooltip()) {
                        beanIcon.setToolTipText(switchTooltip);
                    }
                    beanIcon.setBackground(defaultBackgroundColor);
                    //remove the line around icon switches?
                    this.add(beanIcon);
                    break;
                case 2: // Maerklin style keyboard
                    log.debug("create Key");
                    beanKey.addMouseListener(new MouseAdapter() { // handled by JPanel
                        @Override
                        public void mouseClicked(MouseEvent me) {
                            operate(me, switchName);
                        }
                        @Override
                        public void mousePressed(MouseEvent e) {
                            setToolTip(null); // ends tooltip if displayed
                            if (e.isPopupTrigger()) {
                                // display the popup:
                                showPopUp(e);
                            }
                        }
                    });
                    _text = false;
                    _icon = true;
                    beanKey.setLabel(switchLabel);
                    beanKey.positionLabel(14, 60); // provide x, y offset, depending on image size and free space
                    if (showTooltip()) {
                        beanKey.setToolTipText(switchTooltip);
                    }
                    beanKey.setBackground(defaultBackgroundColor);
                    //remove the line around icon switches?
                    this.add(beanKey);
                    break;
                case 3: // drawing turnout/sensor/light symbol (selecting image by letter in switch name/label)
                    log.debug("create Symbols");
                    beanSymbol.addMouseListener(new MouseAdapter() { // handled by JPanel
                        @Override
                        public void mouseClicked(MouseEvent me) {
                            operate(me, switchName);
                        }
                        @Override
                        public void mousePressed(MouseEvent e) {
                            setToolTip(null); // ends tooltip if displayed
                            if (e.isPopupTrigger()) {
                                // display the popup:
                                showPopUp(e);
                            }
                        }
                    });
                    _text = false;
                    _icon = true;
                    beanSymbol.setLabel(switchLabel);
                    beanSymbol.positionLabel(24, 20); // provide x, y offset, depending on image size and free space
                    if (showTooltip()) {
                        beanSymbol.setToolTipText(switchTooltip);
                    }
                    beanSymbol.setBackground(defaultBackgroundColor);
                    //remove the line around icon switches?
                    this.setBorder(BorderFactory.createLineBorder(defaultBackgroundColor, 3));
                    this.add(beanSymbol);
                    break;
                default: // 0 = "Button" shape
                    log.debug("create Button");
                    beanButton.addMouseListener(new MouseAdapter() { // handled by JPanel
                        @Override
                        public void mouseClicked(MouseEvent me) {
                            operate(me, switchName);
                        }
                        @Override
                        public void mousePressed(MouseEvent e) {
                            setToolTip(null); // ends tooltip if displayed
                            if (e.isPopupTrigger()) {
                                // display the popup:
                                showPopUp(e);
                            }
                        }
                    });
                    _text = true;
                    _icon = false;
                    beanButton.setBackground(defaultBackgroundColor);
                    beanButton.setOpaque(false);
                    if (showTooltip()) {
                        beanButton.setToolTipText(switchTooltip);
                    }
                    this.add(beanButton);
                    break;
            }
            // connect to object or dim switch
            if (bean == null) {
                if(!hideUnconnected()) {
                    switch (_shape) {
                        case 0:
                            beanButton.setEnabled(false);
                            break;
                        case 1:
                            beanIcon.setOpacity(opac);
                            break;
                    }
                }
            } else {
                _control = true;
                switch (beanTypeChar) {
                    case 'T':
                        getTurnout().addPropertyChangeListener(this, _label, "Switchboard Editor Turnout Switch");
                        break;
                    case 'S':
                        getSensor().addPropertyChangeListener(this, _label, "Switchboard Editor Sensor Switch");
                        break;
                    case 'L':
                        getLight().addPropertyChangeListener(this, _label, "Switchboard Editor Light Switch");
                        break;
                }
            }
            // from finishClone
            setTristate(getTristate());
            setMomentary(getMomentary());
            log.debug("Created switch {}", index + "");
            return;
        }

        public NamedBean getNamedBean() {
            return _bname;
        }

        public void setNamedBean(@Nonnull NamedBean bean) {
            try {
                if (bean != null) {
                    namedBean = nbhm.getNamedBeanHandle(_label, bean);
                }
            } catch (IllegalArgumentException e) {
                log.error("invalid bean name= \"" + _label + "\" in Switchboard Button");
            }
            _uname = bean.getUserName();
            _control = true;
        }

        public Turnout getTurnout() {
            if (namedBean == null) {
                return null;
            }
            return (Turnout) namedBean.getBean();
        }

        public Sensor getSensor() {
            if (namedBean == null) {
                return null;
            }
            return (Sensor) namedBean.getBean();
        }

        public Light getLight() {
            if (namedBean == null) {
                return null;
            }
            return (Light) namedBean.getBean();
        }

        public int getType() {
            return _shape;
        }

        // ******************* Display ***************************

        @Override
        public void actionPerformed(ActionEvent e) {
            //updateBean();
        }

        public String getNameString() {
            return _label;
        }

        /**
         * Drive the current state of the display from the state of the connected bean.
         *
         * @param state integer representing the new state e.g. Turnout.CLOSED
         */
        public void displayState(int state) {
            log.debug("heard change");
            if (getNamedBean() == null) {
                log.debug("Display state " + state + ", disconnected");
            } else {
                // display abbreviated name of state instead of state index
                log.debug("bean: {} state: {}", _label, state + ""); //getNameString() + " displayState " + _state2nameMap.get(state));
                if (isText()) {
                    beanButton.setText(_label + ":" + state); //_state2nameMap.get(state));
                }
                if (isIcon() && beanIcon != null && beanKey != null && beanSymbol != null) {
                    log.debug("set icon to {}", state);
                    beanIcon.showSwitchIcon(state);
                    beanIcon.setLabel(_label + ":" + state);
                    beanKey.showSwitchIcon(state);
                    beanKey.setLabel(_label + ":" + state);
                    beanSymbol.showSwitchIcon(state);
                    beanSymbol.setLabel(_label + ":" + state);
                }
            }
        }

        public final boolean isIcon() {
            return _icon;
        }

        public final boolean isText() {
            return _text;
        }

        /**
         * Get current state of attached turnout
         *
         * @return A state variable from a Turnout, e.g. Turnout.CLOSED
         */
        int turnoutState() {
            if (namedBean != null) {
                return getTurnout().getKnownState();
            } else {
                return Turnout.UNKNOWN;
            }
        }

        /**
         *  Update switch as state of turnout changes.
         *
         *  @param e the PropertyChangeEvent heard
         */
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("property change: " + _label + " " + e.getPropertyName() + " is now: "
                        + e.getNewValue());
            }
            // when there's feedback, transition through inconsistent icon for better animation
            if (getTristate()
                    && (getTurnout().getFeedbackMode() != Turnout.DIRECT)
                    && (e.getPropertyName().equals("CommandedState"))) {
                if (getTurnout().getCommandedState() != getTurnout().getKnownState()) {
                    int now = Turnout.INCONSISTENT;
                    displayState(now);
                }
                // this takes care of the quick double click
                if (getTurnout().getCommandedState() == getTurnout().getKnownState()) {
                    int now = ((Integer) e.getNewValue()).intValue();
                    displayState(now);
                }
            }
            if (e.getPropertyName().equals("KnownState")) {
                int now = ((Integer) e.getNewValue()).intValue();
                displayState(now);
                log.debug("Turnout state changed");
            }
            if (e.getPropertyName().equals("UserName")) {
                // update tooltip
                String newUserName = "unconnected";
                if (showTooltip()) {
                    newUserName = ((String) e.getNewValue());
                    if (newUserName == null || newUserName.equals("")) {
                        newUserName = Bundle.getMessage("NoUserName");
                    }
                    beanButton.setToolTipText(_label + " (" + newUserName + ")");
                    beanIcon.setToolTipText(_label + " (" + newUserName + ")");
                    beanKey.setToolTipText(_label + " (" + newUserName + ")");
                    beanSymbol.setToolTipText(_label + " (" + newUserName + ")");
                    log.debug("User Name changed to {}", newUserName);
                }
            }
        }

        public String getStateName(int state) {
            return _state2nameMap.get(Integer.valueOf(state));
        }

        public void mousePressed(MouseEvent e) {
            setToolTip(null); // ends tooltip if displayed
            if (e.isPopupTrigger()) {
                // display the popup:
                showPopUp(e);
            }
        }

        public void mouseExited(MouseEvent e) {
            //super.mouseExited(e);
        }

        /**
         * Process mouseClick on switch.
         
         * @param e the event heard
         */
        public void operate(MouseEvent e, String name) {
            log.debug("Button {} clicked", name);
            //if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            //    return;
            //}
            if (namedBean == null || e.isMetaDown()) { // || e.isAltDown() || !buttonLive() || getMomentary()) {
                return;
            }
            alternateOnClick();
        }

        void cleanup() {
            if (namedBean != null) {
                getTurnout().removePropertyChangeListener(this);
            }
            namedBean = null;
        }

        public void setTristate(boolean set) {
            tristate = set;
        }

        public boolean getTristate() {
            return tristate;
        }

        private boolean tristate = false;

        boolean momentary = false;

        public boolean getMomentary() {
            return momentary;
        }

        public void setMomentary(boolean m) {
            momentary = m;
        }

        JMenuItem connectNewMenu = new JMenuItem(Bundle.getMessage("ConnectNewMenu", "..."));
        JPopupMenu switchPopup;

        /**
         * Show pop-up on a switch with its unique attributes including the (un)connected bean.
         * Derived from {@link #showPopUp(Positionable, MouseEvent)}
         *
         * @param e the event
         */
        public boolean showPopUp(MouseEvent e) {
            if (switchPopup != null) {
                switchPopup.removeAll();
            } else {
                switchPopup = new JPopupMenu();
            }
            JPopupMenu switchPopup = new JPopupMenu();

            switchPopup.add(getNameString());

            if (isEditable()) {
                // add tristate option if turnout has feedback
                if (namedBean != null) {
                    addTristateEntry(switchPopup);
                    addEditUserName(switchPopup);
                } else {
                    // show option to attach a new bean
                    switchPopup.add(connectNewMenu);
                    connectNewMenu.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            connectNew(_label);
                        }
                    });
                }
            }
            // display the popup
            switchPopup.show(this, this.getWidth() / 3 + (int) ((getPaintScale() - 1.0) * this.getX()),
                    this.getHeight() / 3 + (int) ((getPaintScale() - 1.0) * this.getY()));

            return true;
        }

        javax.swing.JCheckBoxMenuItem tristateItem = null;

        void addTristateEntry(JPopupMenu popup) {
            tristateItem = new javax.swing.JCheckBoxMenuItem(Bundle.getMessage("Tristate"));
            tristateItem.setSelected(getTristate());
            popup.add(tristateItem);
            tristateItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setTristate(tristateItem.isSelected());
                }
            });
        }

        javax.swing.JMenuItem EditItem = null;

        void addEditUserName(JPopupMenu popup) {
            EditItem = new javax.swing.JMenuItem(Bundle.getMessage("EditNameTitle", "..."));
            popup.add(EditItem);
            EditItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    renameBean();
                }
            });
        }

        /**
         * Edit user name on a switch using N11N.
         * Copied from BeanTableDataModel.
         */
        public void renameBean() {
            NamedBean nb = null;
            String oldName = _uname;
            // show input dialog
            String newUserName = (String) JOptionPane.showInputDialog(null,
                        Bundle.getMessage("EnterNewName", _label),
                        Bundle.getMessage("EditNameTitle", ""), JOptionPane.PLAIN_MESSAGE, null, null, oldName);
            if (newUserName == null ) { // user cancelled
                log.debug("NewName dialog returned Null, cancelled");
                return;
            }
            newUserName = newUserName.trim(); // N11N
            log.debug("New name: {}", newUserName);
            if (newUserName.length() == 0) {
                log.debug("new user name is empty");
                JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningEmptyUserName"),
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // old style dialog in BeanTableDataModel, ToDo update thare in same form
//            JTextField _newName = new JTextField(20);
//            _newName.setText(oldName);
//            Object[] renameBeanOption = {Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), _newName};
//            int retval = JOptionPane.showOptionDialog(null,
//                    Bundle.getMessage("EnterNewName", oldName), Bundle.getMessage("EditNameTitle", ""),
//                    0, JOptionPane.INFORMATION_MESSAGE, null,
//                    renameBeanOption, renameBeanOption[2]);
//            if (retval != 1) {
//                return;
//            }
            if (newUserName.equals(oldName)) { // name was not changed by user
                return;
            } else { // check if name is already in use
                switch (beanTypeChar) {
                    case 'T':
                        nb = jmri.InstanceManager.turnoutManagerInstance().getTurnout(newUserName);
                        break;
                    case 'S':
                        nb = jmri.InstanceManager.sensorManagerInstance().getSensor(newUserName);
                        break;
                    case 'L':
                        nb = jmri.InstanceManager.lightManagerInstance().getLight(newUserName);
                        break;
                    default:
                        log.error("Check userName: cannot parse bean name. userName = {}", newUserName);
                        return;
                }
                if (nb != null) {
                    log.error("User name is not unique " + newUserName);
                    String msg = Bundle.getMessage("WarningUserName", new Object[]{("" + newUserName)});
                    JOptionPane.showMessageDialog(null, msg,
                            Bundle.getMessage("WarningTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            _bname.setUserName(newUserName);
            if (!newUserName.equals("")) {
                if (oldName == null || oldName.equals("")) {
                    if (!nbhm.inUse(_label, _bname)) {
                        return; // no problem, so stop
                    }
                    String msg = Bundle.getMessage("UpdateToUserName", new Object[]{type, newUserName, _label});
                    int optionPane = JOptionPane.showConfirmDialog(null,
                            msg, Bundle.getMessage("UpdateToUserNameTitle"),
                            JOptionPane.YES_NO_OPTION);
                    if (optionPane == JOptionPane.YES_OPTION) {
                        //This will update the bean reference from the systemName to the userName
                        try {
                            nbhm.updateBeanFromSystemToUser(_bname);
                        } catch (JmriException ex) {
                            //We should never get an exception here as we already check that the username is not valid
                        }
                    }

                } else {
                    nbhm.renameBean(oldName, newUserName, _bname);
                }

            } else {
                //This will update the bean reference from the old userName to the SystemName
                nbhm.updateBeanFromUserToSystem(_bname);

            }
        }

        public void doMouseClicked(java.awt.event.MouseEvent e) {
            log.debug("Switch clicked", e);
            //if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            //    return;
            //}
            if (namedBean == null || e.isMetaDown()) { //|| e.isAltDown() || !buttonLive() || getMomentary()) {
                return;
            }
            alternateOnClick();
        }

        /**
         * Change state of either turnout, light or sensor.
         */
        void alternateOnClick() {
            switch (beanTypeChar) {
            case 'T':
                log.debug("T clicked");
                if (getTurnout().getKnownState() == jmri.Turnout.CLOSED) // if clear known state, set to opposite
                {
                    getTurnout().setCommandedState(jmri.Turnout.THROWN);
                } else if (getTurnout().getKnownState() == jmri.Turnout.THROWN) {
                    getTurnout().setCommandedState(jmri.Turnout.CLOSED);
                } else if (getTurnout().getCommandedState() == jmri.Turnout.CLOSED) {
                    getTurnout().setCommandedState(jmri.Turnout.THROWN);  // otherwise, set to opposite of current commanded state if known
                } else {
                    getTurnout().setCommandedState(jmri.Turnout.CLOSED);  // just force Closed
                }
                break;
            case 'L': // Light
                log.debug("L clicked");
                if (getLight().getState() == jmri.Light.OFF) {
                    getLight().setState(jmri.Light.ON);
                } else {
                    getLight().setState(jmri.Light.OFF);
                }
                break;
            case 'S': // Sensor
                log.debug("S clicked");
                try {
                    if (getSensor().getKnownState() == jmri.Sensor.INACTIVE) {
                        getSensor().setKnownState(jmri.Sensor.ACTIVE);
                    } else {
                        getSensor().setKnownState(jmri.Sensor.INACTIVE);
                    }
                } catch (jmri.JmriException reason) {
                    log.warn("Exception flipping sensor: " + reason);
                }
                break;
            default:
                log.error("invalid char in Switchboard Button \"" + _label + "\". State not set.");
            }
        }

        public void setBackgroundColor(Color bgcolor) {
            this.setBackground(bgcolor);
        }

    }

    /** Create the setup pane for the top of the frame.
     * From layeredpane demo
     */
    private JPanel createControlPanel() {
        JPanel controls = new JPanel();

        // navigation top row and to set range
        navBarPanel = new JPanel();
        navBarPanel.setLayout(new BoxLayout(navBarPanel, BoxLayout.X_AXIS));

        navBarPanel.add(prev);
        prev.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int oldMin = (Integer) minSpinner.getValue();
                int oldMax = (Integer) maxSpinner.getValue();
                _range = oldMax - oldMin;
                minSpinner.setValue(Math.max(rangeMin, oldMin - _range - 1));
                maxSpinner.setValue(Math.max(oldMax - _range - 1, Math.max(rangeMax, oldMax - _range - 1)));
                log.debug("oldMin =" + oldMin + ", oldMax =" + oldMax);
                //rangeMin = (Integer) minSpinner.getValue();
                //rangeMax = (Integer) maxSpinner.getValue();
            }
        });
        prev.setToolTipText(Bundle.getMessage("PreviousToolTip"));
        navBarPanel.add(new JLabel (Bundle.getMessage("MakeLabel", Bundle.getMessage("From"))));
        navBarPanel.add(minSpinner);
        navBarPanel.add(new JLabel (Bundle.getMessage("MakeLabel", Bundle.getMessage("UpTo"))));
        navBarPanel.add(maxSpinner);
        navBarPanel.add(next);

        next.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int oldMin = (Integer) minSpinner.getValue();
                int oldMax = (Integer) maxSpinner.getValue();
                _range = oldMax - oldMin;
                minSpinner.setValue(oldMax + 1);
                maxSpinner.setValue(oldMax + _range + 1);
                //rangeMin = (Integer) minSpinner.getValue();
                //rangeMax = (Integer) maxSpinner.getValue();
            }
        });
        next.setToolTipText(Bundle.getMessage("NextToolTip"));
        navBarPanel.add(Box.createHorizontalGlue());

        // put on which Frame?
        controls.add(navBarPanel); // on 2nd Editor Panel
        //super.getTargetFrame().add(navBarPanel); // on (top of) Switchboard Frame/Panel

        controls.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectRangeTitle")));
        return controls;
    }

    private void setupToolBar() {
        //Initial setup for both horizontal and vertical
        Container contentPane = getContentPane();

        //remove these (if present) so we can add them back (without duplicates)
        if (editToolBarContainer != null) {
            editToolBarContainer.setVisible(false);
            contentPane.remove(editToolBarContainer);
        }

//        if (helpBarPanel != null) {
//            contentPane.remove(helpBarPanel);
//        }

        editToolBarPanel = new JPanel();
        editToolBarPanel.setLayout(new BoxLayout(editToolBarPanel, BoxLayout.PAGE_AXIS));

        JPanel innerBorderPanel = new JPanel();
        innerBorderPanel.setLayout(new BoxLayout(innerBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder TitleBorder = BorderFactory.createTitledBorder(Bundle.getMessage("SwitchboardHelpTitle"));
        innerBorderPanel.setBorder(TitleBorder);
        innerBorderPanel.add(new JTextArea (Bundle.getMessage("Help1")));
        if (!hideUnconnected()) {
            innerBorderPanel.add(new JTextArea (Bundle.getMessage("Help2")));
            // TODO hide this panel when hideUnconnected() is set to false from menu or checkbox
        }
        contentPane.add(innerBorderPanel);
        //Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        editToolBarScroll = new JScrollPane(editToolBarPanel);
        height = 60; //editToolBarScroll.getPreferredSize().height;
        editToolBarContainer = new JPanel();
        editToolBarContainer.setLayout(new BoxLayout(editToolBarContainer, BoxLayout.PAGE_AXIS));
        editToolBarContainer.add(editToolBarScroll);
        editToolBarContainer.setMinimumSize(new Dimension(width, height));
        editToolBarContainer.setPreferredSize(new Dimension(width, height));

//        helpBarPanel = new JPanel();
//        helpBarPanel.add(helpBar);
//        for (Component c : helpBar.getComponents()) {
//            if (c instanceof JTextArea) {
//                JTextArea j = (JTextArea) c;
//                //j.setSize(new Dimension(width, j.getSize().height));
//                j.setLineWrap(true);
//                j.setWrapStyleWord(true);
//            }
//        }
//        contentPane.setLayout(new BoxLayout(contentPane, false ? BoxLayout.LINE_AXIS : BoxLayout.PAGE_AXIS));
//        contentPane.add(editToolBarContainer);
//        contentPane.add(helpBarPanel);
        //helpBarPanel.setVisible(isEditable() && showHelpBar);
    }

    //@Override
    protected void makeOptionMenu() {
        _optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));
        _menuBar.add(_optionMenu, 0);
        // controllable item
        _optionMenu.add(controllingBox);
        controllingBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setAllControlling(controllingBox.isSelected());
            }
        });
        controllingBox.setSelected(allControlling());
        // hideUnconnected item
        _optionMenu.add(hideUnconnectedBox);
        hideUnconnectedBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setHideUnconnected(hideUnconnectedBox.isSelected());
            }
        });
        hideUnconnectedBox.setSelected(hideUnconnected());
        // show tooltip item
        _optionMenu.add(showTooltipBox);
        showTooltipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllShowTooltip(showTooltipBox.isSelected());
            }
        });
        showTooltipBox.setSelected(showTooltip());

        // Show/Hide Scroll Bars
        JMenu scrollMenu = new JMenu(Bundle.getMessage("ComboBoxScrollable"));
        _optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setScroll(SCROLL_BOTH);
            }
        });
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setScroll(SCROLL_NONE);
            }
        });
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setScroll(SCROLL_HORIZONTAL);
            }
        });
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setScroll(SCROLL_VERTICAL);
            }
        });
        //add background color menu item
        JMenu backgroundColorMenu = new JMenu(Bundle.getMessage("SetBackgroundColor"));
        backgroundColorButtonGroup = new ButtonGroup();
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Black"),     Color.black);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("DarkGray"),  Color.darkGray);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Gray"),      Color.gray);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("LightGray"), Color.lightGray);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("White"),     Color.white);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Red"),       Color.red);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Pink"),      Color.pink);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Orange"),    Color.orange);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Yellow"),    Color.yellow);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Green"),     Color.green);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Blue"),      Color.blue);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Magenta"),   Color.magenta);
        addBackgroundColorMenuEntry(backgroundColorMenu,    Bundle.getMessage("Cyan"),      Color.cyan);
        _optionMenu.add(backgroundColorMenu);
        //add text color menu item
        JMenu textColorMenu = new JMenu(Bundle.getMessage("DefaultTextColor"));
        textColorButtonGroup = new ButtonGroup();
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Black"),     Color.black);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("DarkGray"),  Color.darkGray);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Gray"),      Color.gray);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("LightGray"), Color.lightGray);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("White"),     Color.white);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Red"),       Color.red);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Pink"),      Color.pink);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Orange"),    Color.orange);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Yellow"),    Color.yellow);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Green"),     Color.green);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Blue"),      Color.blue);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Magenta"),   Color.magenta);
        addTextColorMenuEntry(textColorMenu,  Bundle.getMessage("Cyan"),      Color.cyan);
        _optionMenu.add(textColorMenu);
    }

//    protected void makeZoomMenu() {
//        _zoomMenu = new JMenu(Bundle.getMessage("MenuZoom"));
//        _menuBar.add(_zoomMenu, 0);
//        JMenuItem addItem = new JMenuItem(Bundle.getMessage("NoZoom"));
//        _zoomMenu.add(addItem);
//        addItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent event) {
//                zoomRestore();
//            }
//        });
//
//        addItem = new JMenuItem(Bundle.getMessage("Zoom", "..."));
//        _zoomMenu.add(addItem);
//        PositionableJComponent z = new PositionableJComponent(this);
//        z.setScale(getPaintScale());
//        addItem.addActionListener(CoordinateEdit.getZoomEditAction(z));
//
//        addItem = new JMenuItem(Bundle.getMessage("ZoomFit"));
//        _zoomMenu.add(addItem);
//        addItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent event) {
//                zoomToFit();
//            }
//        });
//    }

    private void makeFileMenu() {
        _fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        _menuBar.add(_fileMenu, 0);
        _fileMenu.add(new jmri.jmrit.display.NewPanelAction(Bundle.getMessage("MenuItemNew")));

        _fileMenu.add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStore")));
        JMenuItem storeIndexItem = new JMenuItem(Bundle.getMessage("MIStoreImageIndex"));
        _fileMenu.add(storeIndexItem);
        storeIndexItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                jmri.jmrit.catalog.ImageIndexEditor.storeImageIndex();
            }
        });

        JMenuItem editItem = new JMenuItem(Bundle.getMessage("renamePanelMenu", "..."));
        PositionableJComponent z = new PositionableJComponent(this);
        z.setScale(getPaintScale());
        editItem.addActionListener(CoordinateEdit.getNameEditAction(z));
        _fileMenu.add(editItem);

//        editItem = new JMenuItem(Bundle.getMessage("editIndexMenu"));
//        _fileMenu.add(editItem);
//        editItem.addActionListener(new ActionListener() {
//            SwitchboardEditor panelEd;
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                ImageIndexEditor ii = ImageIndexEditor.instance(panelEd);
//                ii.pack();
//                ii.setVisible(true);
//            }
//
//            ActionListener init(SwitchboardEditor pe) {
//                panelEd = pe;
//                return this;
//            }
//        }.init(this));

        _fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("DeletePanel"));
        _fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (deletePanel()) {
                    dispose(true);
                }
            }
        });
        _fileMenu.addSeparator();
        editItem = new JMenuItem(Bundle.getMessage("CloseEditor"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setAllEditable(false);
            }
        });
    }

    void addBackgroundColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultBackgroundColor.equals(desiredColor)) {
                    // if new bgColor matches the defaultTextColor, ask user as labels will become unreadable
                    if (desiredColor == defaultTextColor) {
                        int retval = JOptionPane.showOptionDialog(null,
                                Bundle.getMessage("ColorIdenticalWarning"), Bundle.getMessage("WarningTitle"),
                                0, JOptionPane.INFORMATION_MESSAGE, null,
                                new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")}, null);
                        log.debug("Retval: "+retval);
                        if (retval != 0) {
                            return;
                        }
                    }
                    defaultBackgroundColor = desiredColor;
                    setBackgroundColor(desiredColor);
                    setDirty(true);
                    repaint();
                }
            }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        backgroundColorButtonGroup.add(r);

        if (defaultBackgroundColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        backgroundColorMenuItems[backgroundColorCount] = r;
        backgroundColors[backgroundColorCount] = color;
        backgroundColorCount++;
    }

    void addTextColorMenuEntry(JMenu menu, final String name, final Color color) {
        ActionListener a = new ActionListener() {
            final Color desiredColor = color;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!defaultTextColor.equals(desiredColor)) {
                    // if new defaultTextColor matches bgColor, ask user as labels will become unreadable
                    if (desiredColor == defaultBackgroundColor) {
                        int retval = JOptionPane.showOptionDialog(null,
                                Bundle.getMessage("ColorIdenticalWarning"), Bundle.getMessage("WarningTitle"),
                                0, JOptionPane.INFORMATION_MESSAGE, null,
                                new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")}, null);
                        log.debug("Retval: "+retval);
                        if (retval != 0) {
                            return;
                        }
                    }
                    defaultTextColor = desiredColor;
                    setDirty(true);
                    repaint();
                }
            }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);

        r.addActionListener(a);
        textColorButtonGroup.add(r);

        if (defaultTextColor.equals(color)) {
            r.setSelected(true);
        } else {
            r.setSelected(false);
        }
        menu.add(r);
        textColorMenuItems[textColorCount] = r;
        textColors[textColorCount] = color;
        textColorCount++;
    }

    protected void setOptionMenuTextColor() {
        for (int i = 0; i < textColorCount; i++) {
            if (textColors[i].equals(defaultTextColor)) {
                textColorMenuItems[i].setSelected(true);
            } else {
                textColorMenuItems[i].setSelected(false);
            }
        }
    }

    protected void setOptionMenuBackgroundColor() {
        for (int i = 0; i < backgroundColorCount; i++) {
            if (backgroundColors[i].equals(defaultBackgroundColor)) {
                backgroundColorMenuItems[i].setSelected(true);
            } else {
                backgroundColorMenuItems[i].setSelected(false);
            }
        }
    }

    public void setDefaultTextColor(String color) {
        defaultTextColor = ColorUtil.stringToColor(color);
        setOptionMenuTextColor();
    }

    public String getDefaultTextColor() {
        return ColorUtil.colorToString(defaultTextColor);
    }

    /**
     * Load from xml and set bg color of _targetpanel as well as variable.
     *
     * @param color RGB Color for switchboard background and beanSwitches
     */
    public void setDefaultBackgroundColor(Color color) {
        setBackgroundColor(color); // via Editor
        defaultBackgroundColor = color;
        setOptionMenuBackgroundColor();
    }

//    private JRadioButtonMenuItem makeSelectTypeButton(String label, String className) {
//        JRadioButtonMenuItem button = new JRadioButtonMenuItem(Bundle.getMessage(label));
//        button.addActionListener(new ActionListener() {
//            String cName;
//
//            ActionListener init(String name) {
//                cName = name;
//                return this;
//            }
//
//            @Override
//            public void actionPerformed(ActionEvent event) {
//                selectType(cName);
//            }
//        }.init(className));
//        return button;
//    }
//
//    private void selectType(String name) {
//        try {
//            Class<?> cl = Class.forName(name);
//            _selectionGroup = new ArrayList<Positionable>();
//            Iterator<Positionable> it = _contents.iterator();
//            while (it.hasNext()) {
//                Positionable pos = it.next();
//                if (cl.isInstance(pos)) {
//                    _selectionGroup.add(pos);
//                }
//            }
//        } catch (ClassNotFoundException cnfe) {
//            log.error("selectType Menu " + cnfe.toString());
//        }
//        _targetPanel.repaint();
//    }

//    private JMenu makeSelectLevelMenu() {
//        JMenu menu = new JMenu(Bundle.getMessage("SelectLevel"));
//        ButtonGroup levelGroup = new ButtonGroup();
//        JRadioButtonMenuItem button = null;
//        for (int i = 0; i < 11; i++) {
//            button = new JRadioButtonMenuItem(Bundle.getMessage("selectLevel", "" + i));
//            levelGroup.add(button);
//            menu.add(button);
//            button.addActionListener(new ActionListener() {
//                int j;
//
//                ActionListener init(int k) {
//                    j = k;
//                    return this;
//                }
//
//                @Override
//                public void actionPerformed(ActionEvent event) {
//                    selectLevel(j);
//                }
//            }.init(i));
//        }
//        return menu;
//    }

//    private void selectLevel(int i) {
//        _selectionGroup = new ArrayList<Positionable>();
//        Iterator<Positionable> it = _contents.iterator();
//        while (it.hasNext()) {
//            Positionable pos = it.next();
//            if (pos.getDisplayLevel() == i) {
//                _selectionGroup.add(pos);
//            }
//        }
//        _targetPanel.repaint();
//    }

    // *********************** end Menus ************************

    @Override
    public void setAllEditable(boolean edit) {
        if (edit) {
            if (_editorMenu != null) {
                _menuBar.remove(_editorMenu);
            }
//            if (_iconMenu == null) {
//                makeIconMenu();
//            } else {
//                _menuBar.add(_iconMenu, 0);
//            }
//            if (_zoomMenu == null) {
//                makeZoomMenu();
//            } else {
//                _menuBar.add(_zoomMenu, 0);
//            }
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
            //contentPane.SetUpdateButtonEnabled(false);
        } else {
            if (_fileMenu != null) {
                _menuBar.remove(_fileMenu);
            }
            if (_optionMenu != null) {
                _menuBar.remove(_optionMenu);
            }
//            if (_iconMenu != null) {
//                _menuBar.remove(_iconMenu);
//            }
            if (_editorMenu == null) {
                _editorMenu = new JMenu(Bundle.getMessage("MenuEdit"));
                _editorMenu.add(new AbstractAction(Bundle.getMessage("OpenEditor")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setAllEditable(true);
                    }
                });
            }
            _menuBar.add(_editorMenu, 0);
            //contentPane.SetUpdateButtonEnabled(true);
        }
        super.setAllEditable(edit);
        setTitle();
        _menuBar.revalidate();
    }

    @Override
    public void setUseGlobalFlag(boolean set) {
        //positionableBox.setEnabled(set);
        controllingBox.setEnabled(set);
        super.setUseGlobalFlag(set);
    }

    private void zoomRestore() {
        List<Positionable> contents = getContents();
        for (Positionable sw : contents) {
            sw.setLocation(sw.getX() + _fitX, sw.getY() + _fitY);
        }
        setPaintScale(1.0);
    }

    int _fitX = 0;
    int _fitY = 0;

    private void zoomToFit() {
        double minX = 1000.0;
        double maxX = 0.0;
        double minY = 1000.0;
        double maxY = 0.0;
        List<Positionable> contents = getContents();
        for (Positionable sw : contents) {
            minX = Math.min(sw.getX(), minX);
            minY = Math.min(sw.getY(), minY);
            maxX = Math.max(sw.getX() + sw.getWidth(), maxX);
            maxY = Math.max(sw.getY() + sw.getHeight(), maxY);
        }
        _fitX = (int) Math.floor(minX);
        _fitY = (int) Math.floor(minY);

        JFrame frame = getTargetFrame();
        Container contentPane = getTargetFrame().getContentPane();
        Dimension dim = contentPane.getSize();
        Dimension d = getTargetPanel().getSize();
        getTargetPanel().setSize((int) Math.ceil(maxX - minX), (int) Math.ceil(maxY - minY));

        JScrollPane scrollPane = getPanelScrollPane();
        scrollPane.getHorizontalScrollBar().setValue(0);
        scrollPane.getVerticalScrollBar().setValue(0);
        JViewport viewPort = scrollPane.getViewport();
        Dimension dv = viewPort.getExtentSize();

        int dX = frame.getWidth() - dv.width;
        int dY = frame.getHeight() - dv.height;
        log.debug("zoomToFit: layoutWidth= {}, layoutHeight= {}\n\tframeWidth= {}, frameHeight= {}, viewWidth= {}, viewHeight= {}\n\tconWidth= {}, conHeight= {}, panelWidth= {}, panelHeight= {}",
                (maxX - minX), (maxY - minY), frame.getWidth(), frame.getHeight(), dv.width, dv.height, dim.width, dim.height, d.width, d.height);
        double ratioX = dv.width / (maxX - minX);
        double ratioY = dv.height / (maxY - minY);
        double ratio = Math.min(ratioX, ratioY);
        /*
         if (ratioX<ratioY) {
         if (ratioX>1.0) {
         ratio = ratioX;
         } else {
         ratio = ratioY;
         }
         } else {
         if (ratioY<1.0) {
         ratio = ratioX;
         } else {
         ratio = ratioY;
         }
         } */
        _fitX = (int) Math.floor(minX);
        _fitY = (int) Math.floor(minY);
        for (Positionable sw : contents) {
            sw.setLocation(sw.getX() - _fitX, sw.getY() - _fitY);
        }
        setScroll(SCROLL_BOTH);
        setPaintScale(ratio);
        setScroll(SCROLL_NONE);
        scrollNone.setSelected(true);
        //getTargetPanel().setSize((int)Math.ceil(maxX), (int)Math.ceil(maxY));
        frame.setSize((int) Math.ceil((maxX - minX) * ratio) + dX, (int) Math.ceil((maxY - minY) * ratio) + dY);
        scrollPane.getHorizontalScrollBar().setValue(0);
        scrollPane.getVerticalScrollBar().setValue(0);
        log.debug("zoomToFit: ratio= {}, w= {}, h= {}, frameWidth= {}, frameHeight= {}",
                ratio, (maxX - minX), (maxY - minY), frame.getWidth(), frame.getHeight());
    }

    @Override
    public void setTitle() {
        String name = getName(); // get name of JFrame
        log.debug("JFrame name = {}", name);
        if (name == null || name.length() == 0) {
            name = Bundle.getMessage("SwitchboardDefaultName","");
        }
        if (isEditable()) {
            super.setTitle(name + " " + Bundle.getMessage("LabelEditor"));
        } else {
            super.setTitle(name);
        }
    }

    /**
     * Control whether target panel items without a connection to the layout
     * are displayed.
     *
     * @param state true to hide all in range
     */
    public void setHideUnconnected(boolean state) {
        _hideUnconnected = state;
        if (!state) {
            // hide Help2
        }
    }

    public boolean hideUnconnected() {
        return _hideUnconnected;
    }

    /**
     * Allow external reset of dirty bit.
     */
    public void resetDirty() {
        setDirty(false);
        savedEditMode = isEditable();
        savedControlLayout = allControlling();
        //savedShowHelpBar = showHelpBar;
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
        rangeMin = rangemin;
    }

    /**
     * Load Range maximum.
     *
     * @param rangemax highest address to show
     */
    public void setPanelMenuRangeMax(int rangemax) {
        maxSpinner.setValue(rangemax);
        rangeMax = rangemax;
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
        if (switchType.equals(_light)) { // switch-case doesn't work here
            typePrefix = "L";
        } else if (switchType.equals(_sensor)) {
            typePrefix = "S";
        } else { // Turnout
            typePrefix = "T";
        }
        return typePrefix;
    }

    /**
     * Load bean type.
     *
     * @param prefix the bean type prefix
     */
    public void setSwitchType(String prefix) {
        typePrefix = prefix;
//        String type;
        switch (typePrefix) {
            case "L":
                type = _light;
                break;
            case "S":
                type = _sensor;
                break;
            default: // Turnout
                type = _turnout;
        }
        try {
            beanTypeList.setSelectedItem(type);
        } catch (IllegalArgumentException e) {
            log.error("invalid bean type [" + typePrefix + "] in Switchboard");
        }
    }

    /**
     * Store connection type.
     *
     * @return bean connection prefix
     */
    public String getSwitchManu() {
        return (String) this.beanManuPrefixes.get(beanManuNames.getSelectedIndex());
    }

    /**
     * Load connection type.
     *
     * @param  manuPrefix connection prefix
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
        beanManuNames.setSelectedItem(beanManuPrefixes.get(choice));
        } catch (IllegalArgumentException e) {
            log.error("invalid connection [" + manuPrefix + "] in Switchboard");
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
        if (shapeChoice == 1) {
            shape = "icon";
        } else if (shapeChoice == 2) {
            shape = "drawing";
        } else if (shapeChoice == 3) {
            shape = "symbol";
        } else { // Turnout
            shape = "button";
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
            log.error("invalid switch shape [" + shape + "] in Switchboard");
        }
    }

    /**
     * Store Switchboard column spinner.
     *
     * @return the number of switches to display per row
     */
    public int getColumns() {
        return (Integer) Columns.getValue();
    }

    /**
     * Load Switchboard column spinner.
     *
     * @param cols the number of switches to display per row (as text)
     */
    public void setColumns(int cols) {
        Columns.setValue(cols);
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
        showTooltipBox.setSelected(showTooltip());
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
            case SCROLL_VERTICAL:
                scrollVertical.setSelected(true);
                break;
        }
        log.debug("InitView done");
    }

    protected Manager getManager(char typeChar) {
        switch (typeChar) {
            case 'T':
                return InstanceManager.turnoutManagerInstance();
            case 'S':
                return InstanceManager.sensorManagerInstance();
            case 'L': // Light
                return InstanceManager.lightManagerInstance();
            default:
                log.error("Unexpected bean type character \"{}\" found.", typeChar);
                return null;
        }
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(12);
    JTextField userName = new JTextField(15);

    /**
     * Create new bean and connect it to this switch.
     * Use type letter from switch label (S, T or L).
     */
    protected void connectNew(String systemName) {
        log.debug("Request new bean");
        sysName.setText(systemName);
        userName.setText("");
        // provide etc.
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("ConnectNewMenu", ""), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.display.switchboardEditor.SwitchboardEditor", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = (ActionEvent ev) -> {
                okAddPressed(ev);
            };
            ActionListener cancelListener = (ActionEvent ev) -> {
                cancelAddPressed(ev);
            };
            AddNewDevicePanel switchConnect = new AddNewDevicePanel(sysName, userName, "ButtonOK", okListener, cancelListener);
            switchConnect.setSystemNameFieldIneditable(); // prevent user interference with switch label
            switchConnect.setOK(); // activate OK button on Add new device pane
            addFrame.add(switchConnect);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    protected void cancelAddPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    protected void okAddPressed(ActionEvent e) {
        NamedBean nb = null;
        String manuPrefix = getSwitchManu();
        String user = userName.getText().trim();
        if (user.equals("")) {
            user = null;
        }
        String sName = sysName.getText(); // can't be changed, but pick it up from panel

        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;

        switch (sName.charAt(manuPrefix.length())) {
            case 'T':
                Turnout t;
                try {
                    // add turnout to JMRI (w/appropriate manager)
                    t = InstanceManager.turnoutManagerInstance().provideTurnout(sName);
                    t.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(sName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.turnoutManagerInstance().getTurnout(sName);
                break;
            case 'S':
                Sensor s;
                try {
                    // add Sensor to JMRI (w/appropriate manager)
                    s = InstanceManager.sensorManagerInstance().provideSensor(sName);
                    s.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(sName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.sensorManagerInstance().getSensor(sName);
                break;
            case 'L':
                Light l;
                try {
                    // add Light to JMRI (w/appropriate manager)
                    l = InstanceManager.lightManagerInstance().provideLight(sName);
                    l.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(sName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.lightManagerInstance().getLight(sName);
                break;
            default:
                log.error("connectNew - OKpressed: cannot parse bean name. sName = {}", sName);
                return;
        }
        if (nb == null) {
            log.warn("failed to connect switch to item {}", sName);
        } else {
            // set switch on Switchboard to display current state of just connected bean
            log.debug("sName state: {}", nb.getState());
            try {
                if (getSwitch(sName) == null) {
                    log.warn("failed to update switch to state of {}", sName);
                } else {
                    updatePressed();
//                    getSwitch(sName).setNamedBean(nb);
//                    getSwitch(sName).displayState(nb.getState());
//                    getSwitch(sName).setEnabled(true);
                }
            }
            catch (NullPointerException npe) {
                handleCreateException(sName);
                return; // without updating
            }
        }

    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorSwitchAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
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
                //_shapeDrawer.delete();
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_INSERT:
            case KeyEvent.VK_PLUS:
                //_shapeDrawer.add(e.isShiftDown());
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

    // ********************* Mouse Methods ***********************

    private long _clickTime;

    @Override
    public void mousePressed(MouseEvent event) {};

    @Override
    public void mouseReleased(MouseEvent event) {};

    @Override
    public void mouseClicked(MouseEvent event) {};

    @Override
    public void mouseDragged(MouseEvent event) {};

    @Override
    public void mouseMoved(MouseEvent event) {};

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
     * Handle close of editor window.
     * <P>
     * Overload/override method in JmriJFrame parent, which by default is
     * permanently closing the window. Here, we just want to make it invisible,
     * so we don't dispose it (yet).
     *
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        setAllEditable(false);
    }

    // ************* implementation of Abstract Editor methods **********

    /**
     * The target window has been requested to close, don't delete it at this
     * time. Deletion must be accomplished via the Delete this panel menu item.
     */
    @Override
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        targetWindowClosing(true);
    }

    /**
     * Create sequence of panels, etc. for layout: JFrame contains its
     * ContentPane which contains a JPanel with BoxLayout (p1) which contains a
     * JScollPane (js) which contains the targetPane.
     *
     * @param name name for the Switchboard
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
     * Class to display individual bean state switches on a JMRI Switchboard
     * using 2 image files.
     */
    public class IconSwitch extends JPanel {

        private BufferedImage image;
        private BufferedImage image1;
        private BufferedImage image2;
        private String tag = "tag";
        private int labelX = 16;
        private int labelY = 53;

        /**
         * Create an icon from 2 alternating png images.
         *
         * @param filepath1 the ON image
         * @param filepath2 the OFF image
         */
        public IconSwitch(String filepath1, String filepath2) {
            // resize to maximum 100
            try {
                image1 = ImageIO.read(new File(filepath1));
                image2 = ImageIO.read(new File(filepath2));
                image = image1;
            } catch (IOException ex) {
                log.error("error reading image from {}-{}", filepath1, filepath2, ex);
            }
        }

        protected void setOpacity(float opac) {
            //this.opacity = opac; // not functional, use alfa instead
        }

        protected void showSwitchIcon(int stateIndex) {
            log.debug("showSwitchIcon {}", stateIndex);
            if (image1 != null && image2 != null) {
                switch (stateIndex) {
                    case 4:
                        image = image2;
                        break;
                    default:
                        image = image1; // off, also for connected & unknown
                        break;
                };
                this.repaint();
            }
        }

        protected void setImage1(String newImagePath) {
            try {
                image1 = ImageIO.read(new File(newImagePath));
            } catch (IOException ex) {
                log.error("error reading image from {}", newImagePath, ex);
            }
        }

        /**
         * Set or change label text on switch.
         *
         * @param text string to display
         */
        protected void setLabel(String text) {
            tag = text;
            this.repaint();
        }

        /**
         * Position label on switch.
         *
         * @param x    horizontal offset from top left corner, positive to the right
         * @param y    vertical offset from top left corner, positive down
         */
        protected void positionLabel(int x, int y) {
            labelX = x;
            labelY = y;
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
            g.setFont(getFont());
            g.setColor(defaultTextColor); // getTextColor()
            g.drawString(tag, labelX, labelY); // draw name on top of button image (vertical, horizontal offset from top left)
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

//    public List<BeanSwitch> getSwitches() {
//        for (int i = 0; i < switchlist.size(); i++) {
//            _switches.add(switchboardLayeredPane.getComponent(i));
//        }
//        return _switches;
//    }

    /**
     * Set up item(s) to be copied by paste.
     * <p>
     * Not used on switchboards but has to override Editor
     */
    @Override
    protected void copyItem(Positionable p) {
    };

    /**
     * Set an object's location when it is created.
     * <p>
     * Not used on switchboards but has to override Editor
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
     * Popup items common to all positionable objects are done before and
     * after the items that pertain only to specific Positionable types.
     * <p>
     * Not used on switchboards but has to override Editor
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

    private final static Logger log = LoggerFactory.getLogger(SwitchboardEditor.class.getName());
}
