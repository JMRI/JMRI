package jmri.jmrit.display.switchboardEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import jmri.util.ConnectionNameFromSystemName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBean;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.CoordinateEdit;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrack;
import jmri.jmrit.display.LinkingObject;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.MemoryIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrit.display.PositionableJPanel;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.controlPanelEditor.shape.ShapeDrawer;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.util.HelpUtil;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple editor for adding jmri.jmrit.display.switchBoard items
 * to a JLayeredPane inside a captive JFrame.
 * <P>
 * GUI is structured as a separate control panel to set the visible range and type
 * plus menus.
 * <P>
 * All created objects are put insite a GridLayout grid.
 * No special use of the LayeredPane layers.
 * <P>
 * The "contents" List keeps track of all the objects added to the target frame
 * for later manipulation.
 * No DnD as panels will be automatically populated in order of the DCC address.
 * <P>
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
    protected JMenu _iconMenu;
    protected JMenu _zoomMenu;
    private JMenu _markerMenu;
    private JMenu _drawMenu;
    private ArrayList<Positionable> _secondSelectionGroup;
    //private ShapeDrawer _shapeDrawer;
    private ItemPalette _itemPalette;
    private boolean _disableShapeSelection;

    // Switchboard items
    private JPanel navBarPanel = null;
    ImageIcon iconPrev = new ImageIcon("resources/icons/misc/gui3/LafLeftArrow_m.gif");
    private JLabel prev = new JLabel(iconPrev);
    ImageIcon iconNext = new ImageIcon("resources/icons/misc/gui3/LafRightArrow_m.gif");
    private JLabel next = new JLabel(iconNext);
    ImageIcon appslideOff = new ImageIcon("resources/icons/misc/switchboard/appslide-off");
    ImageIcon appslideOn = new ImageIcon("resources/icons/misc/switchboard/appslide-on");
    private int rangeMin = 1;
    private int rangeMax = 32;
    private int _range = rangeMax - rangeMin;
    JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(rangeMin, rangeMin, rangeMax, 1));
    JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(rangeMax, rangeMin, rangeMax, 1));
    // toolbar (from LE)
    private JPanel floatEditHelpPanel = null;
    private JPanel editToolBarPanel = null;
    private JScrollPane editToolBarScroll = null;
    private JPanel editToolBarContainer = null;

    //private JPanel helpBarPanel = null;
    //private JPanel helpBar = new JPanel();
    // option menu items not in Editor
    private boolean showHelpBar = true;
    private boolean _hideUnconnected = false;
    private int height = 100;
    private int width = 100;

    private JCheckBoxMenuItem useGlobalFlagBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxGlobalFlags"));
//    private JCheckBoxMenuItem editableBox = new JCheckBoxMenuItem(Bundle.getMessage("CloseEditor"));
    private JCheckBoxMenuItem positionableBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxPositionable"));
    private JCheckBoxMenuItem controllingBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxControlling"));
    private JCheckBoxMenuItem hideUnconnectedBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxHideUnconnected"));
    private JCheckBoxMenuItem showTooltipBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxShowTooltips"));
    private JCheckBoxMenuItem hiddenBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxHidden"));
    private JCheckBoxMenuItem disableShapeSelect = new JCheckBoxMenuItem(Bundle.getMessage("disableShapeSelect"));
    private JRadioButtonMenuItem scrollBoth = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
    private JRadioButtonMenuItem scrollNone = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
    private JRadioButtonMenuItem scrollHorizontal = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
    private JRadioButtonMenuItem scrollVertical = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));

    // From Oracle JLayeredPane demo
    private TargetPane switchboardLayeredPane; // JLayeredPane
    private JCheckBox hideUnconnected;
    private String[] beanTypeStrings = { Bundle.getMessage("BeanNameTurnout"),
            Bundle.getMessage("BeanNameSensor"),
            Bundle.getMessage("BeanNameLight")
    };
    private JComboBox beanTypeList = new JComboBox(beanTypeStrings);
    private char beanTypeChar;
    private Color[] layerColors = { Color.yellow, Color.magenta,
            Color.cyan, Color.red, Color.green };
    private String[] switchTypeStrings = {
            Bundle.getMessage("Button"),
            Bundle.getMessage("Icon"),
            Bundle.getMessage("Drawing")
    };
    private JComboBox switchTypeList;
    private List<String> beanManuPrefixes = new ArrayList<String>();
    private JComboBox beanManuNames;
    //Action commands
    private static String HIDE_COMMAND = "hideUnconnected";
    private static String LAYER_COMMAND = "layer";
    private static String MANU_COMMAND = "manufacturer";
    private static String SWITCHTYPE_COMMAND = "switchtype";

    private List<String> switchlist = new ArrayList<String>();

    /**
     * Ctor
     */
    public SwitchboardEditor() {
    }

    /**
     * Ctor by a given name
     *
     * @param name title to assign to the new SwitchBoard
     */
    public SwitchboardEditor(String name) {
        super(name, false, true);
        init(name);
    }

    /**
     * Initialize the SwitchBoard.
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
        //makeDrawMenu();
        //makeIconMenu();
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
                Bundle.getMessage("SwitchboardTitle", "TO")));
        switchboardLayeredPane.addMouseMotionListener(this);

        //Add control pane and layered pane to this JPanel.
        JPanel beanSetupPane = new JPanel();
        beanSetupPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JLabel beanTypeTitle = new JLabel("Bean type:");
        beanSetupPane.add(beanTypeTitle);
        beanTypeList.setSelectedIndex(0);    // select Turnout in comboBox
        beanTypeList.setActionCommand(LAYER_COMMAND);
        beanTypeList.addActionListener(this);
        beanSetupPane.add(beanTypeList);
        add(beanSetupPane);

        //Add connection selection comboBox.
        beanTypeChar = beanTypeList.getSelectedItem().toString().charAt(0);
        JLabel beanManuTitle = new JLabel("Connection:");
        beanSetupPane.add(beanManuTitle);
        beanManuNames = new JComboBox();
        if (getManager(beanTypeChar) instanceof jmri.managers.AbstractProxyManager) { // from abstractTableTabAction
            jmri.managers.AbstractProxyManager proxy = (jmri.managers.AbstractProxyManager) getManager(beanTypeChar);
            List<jmri.Manager> managerList = proxy.getManagerList();
            //beanManuList.addItem(Bundle.getMessage("All")); // NOI18N
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

        //Add the buttons to the layered pane.
        switchboardLayeredPane.setLayout(new GridLayout(8,4)); // vertical, horizontal
        addSwitchRange(rangeMin, rangeMax,
                beanTypeList.getSelectedItem().toString(),
                beanManuPrefixes.get(beanManuNames.getSelectedIndex()),
                0);

        JPanel switchTypePane = new JPanel();
        JLabel switchTypeTitle = new JLabel("Switch shape:");
        switchTypePane.add(switchTypeTitle);
        switchTypeList = new JComboBox(switchTypeStrings);
        switchTypeList.setSelectedIndex(0);    //button
        switchTypeList.setActionCommand(SWITCHTYPE_COMMAND);
        switchTypeList.addActionListener(this);
        switchTypePane.add(switchTypeList);
        add(switchTypePane);

        JCheckBox hideUnconnected = new JCheckBox(Bundle.getMessage("CheckBoxHideUnconnected"));
        hideUnconnected.setSelected(false);
        hideUnconnected.setActionCommand(HIDE_COMMAND);
        hideUnconnected.addActionListener(this);
        add(hideUnconnected);

        super.setTargetPanel(switchboardLayeredPane, makeFrame(name)); // provide a JLayeredPane to use

        super.setTargetPanelSize(300, 300);

        // set scrollbar initial state
        setScroll(SCROLL_BOTH);
        scrollBoth.setSelected(true);
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
                for (int i = switchlist.size() - 1; i > 0 ; i--) {
                    switchboardLayeredPane.remove(i);
                }
                switchlist.clear(); // reset list
                addSwitchRange(rangeMin, rangeMax,
                        beanTypeList.getSelectedItem().toString(),
                        beanManuPrefixes.get(beanManuNames.getSelectedIndex()),
                        switchTypeList.getSelectedIndex());
                pack();
            }
        });
        updatePanel.add(updateButton);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(updatePanel);

        setupToolBar(); //re-layout all the toolbar items

        pack();
        setVisible(true);

        // TODO use an icon
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

    private void addSwitchRange(int rangeMin, int rangeMax, String beanType, String manuPrefix, int switchType) {
        log.debug("hideUnconnected = {}", hideUnconnected());
        String name = "";
        BeanSwitch _switch;
        NamedBean nb = null;
        String _manu = manuPrefix; // cannot use All
        for (int i = rangeMin; i <= rangeMax; i++) {
            switch (beanType) {
                case "Turnout":
                    //_manu = turnoutManager.getSystemPrefix();
                    name = _manu + "T" + i;
                    nb = jmri.InstanceManager.turnoutManagerInstance().getTurnout(name);
                    break;
                case "Sensor":
                    name = _manu + "S" + i;
                    nb = jmri.InstanceManager.sensorManagerInstance().getSensor(name);
                    break;
                case "Light":
                    name = _manu + "L" + i;
                    nb = jmri.InstanceManager.lightManagerInstance().getLight(name);
                    break;
            }
            _switch = new BeanSwitch(i, nb, name, switchType); // add 1 button instance
            log.debug("Added switch {}", i + "");
            //_switch.setText(name);
            switchlist.add(name); // to count total number of switche on JLayeredPane
            if (nb == null) {
                if(!hideUnconnected()) {
                    _switch.setEnabled(false);
                    switchboardLayeredPane.add(_switch); // or setVisible(false)
                }
            } else {
                switchboardLayeredPane.add(_switch);
            }
        }
    }

    /**
     * Class for a switchboard object.
     * For now just a JButton to control existing turnouts, sensors and lights.
     */
    public class BeanSwitch extends JPanel implements java.beans.PropertyChangeListener, ActionListener {

        protected HashMap<Integer, NamedIcon> _iconStateMap;     // state int to icon
        protected HashMap<String, Integer> _name2stateMap;       // name to state
        protected HashMap<Integer, String> _state2nameMap;       // state to name

        private JButton beanButton;
        private final boolean connected = false;
        private int _type;
        private String _label;
        protected JLabel typeLabel;
        protected boolean _text;
        protected boolean _icon = false;
        protected boolean _control = false;
        //protected NamedIcon _namedIcon;

        // the associated Bean object
        private NamedBean _bname;
        private NamedBeanHandle<?> namedBean = null; // could be Turnout, Sensor or Light
        protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
        private JLabel PicLabel;
        private char beanTypeChar;
        private char beanManuChar;

        /**
         * Ctor
         * @param index DCC address
         * @param bean layout object type to connect to
         * @param name descriptive name corresponding with system name to display in switch tooltip, i.e. LT1
         * @param type Button, Icon (static) or Drawing (vector graphics)
         */
        public BeanSwitch(@Nonnull int index, NamedBean bean, String name, int type) {
            _label = name;
            beanButton = new JButton(_label);
            _bname = bean;
            this.setLayout(new BorderLayout());
            if (type != 0) {
                _type = type;
            } else {
                _type = 0;
            }
            beanManuChar = _label.charAt(0); // connection/manufacturer i.e. M for MERG
            beanTypeChar = _label.charAt(1); // bean type, i.e. L
            log.debug("beanconnect = {}, beantype = {}", beanManuChar, beanTypeChar);
            switch (_type) {
                case 2:
                    log.debug("create Image");
                    PicLabel = new JLabel(appslideOn);
                    this.add(PicLabel);
                    break;
                case 1:
                    log.debug("not yet working, try Buttons");
                default: //  "Button" = 0
                    log.debug("create Button");
                    try {
                        //Turnout tn = InstanceManager.turnoutManagerInstance().getTurnout(name);
                        if (bean != null) {
                            namedBean = nbhm.getNamedBeanHandle(name, bean); //tn);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("invalid turnout name= \"" + name + "\" in Switchboard Button");
                    }
                    //beanButton.setIcon(appslideOff);
                    //beanButton.setPressedIcon(appslideOn);
                    beanButton.addMouseListener(new MouseAdapter() { // could we leave this out and let the JPanel handle it?
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            super.mouseClicked(e);
//                            if (bean != null && getTurnout().getCommandedState() != Turnout.CLOSED) {
//                                // no need to set a state already set
//                                getTurnout().setCommandedState(Turnout.CLOSED);
//                                beanButton.setText(name + ": C");
//                            } else if (bean != null && getTurnout().getCommandedState() != Turnout.THROWN) {
//                                getTurnout().setCommandedState(Turnout.THROWN);
//                                beanButton.setText(name + ": T");
//                            }
                            log.debug("Button {} clicked", name);
                        }
                    });
                    _text = true;
                    this.add(beanButton);
                    break;
            }
            //setPopupUtility(new PositionablePopupUtil(this, this));
            if (bean == null) {
                if(!hideUnconnected()) {
                    beanButton.setEnabled(false);
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
            this.setToolTipText(name);
            // from finishClone
            setTristate(getTristate());
            setMomentary(getMomentary());
            setDirectControl(getDirectControl());
            log.debug("Created button {}", index + "");
            return;
        }

        public NamedBean getNamedBean() {
            return _bname;
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
            return _type;
        }

        /**
         * Display
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            //updateBean();
        }

        public String getNameString() {
            return _label;
        }

        /**
         * Drive the current state of the display from the state of the turnout.
         */
        public void displayState(int state) {
            if (getNamedBean() == null) {
                log.debug("Display state " + state + ", disconnected");
            } else {
                log.debug("TO {} state: {}", _label, state + ""); //getNameString() +" displayState "+_state2nameMap.get(state));
                if (isText()) {
                    //beanButton.setText("C"); //(name + ": C");
                    beanButton.setText(_label + ":" + state); //_state2nameMap.get(state));
                    //super.setText(_state2nameMap.get(state));
                }
                if (isIcon()) { // TODO
                    NamedIcon icon = getIcon(state);
                    if (icon != null) {
                        //super.setIcon(icon);
                    }
                }
            }
            //updateSize();
        }

        /**
         * Get icon by its localized bean state name.
         */
        public NamedIcon getIcon(String state) {
            return _iconStateMap.get(_name2stateMap.get(state));
        }

        public NamedIcon getIcon(int state) {
            return _iconStateMap.get(Integer.valueOf(state));
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
         *  Update switch as state of turnout changes
         */

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("property change: " + _label + " " + e.getPropertyName() + " is now: "
                        + e.getNewValue());
            }

            // when there's feedback, transition through inconsistent icon for better
            // animation
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
                log.debug("Turnout changed");
            }
        }

        public String getStateName(int state) {
            return _state2nameMap.get(Integer.valueOf(state));
        }

        public void mouseExited(MouseEvent e) {
            //typeLabel.setFocusable(false);
            //typeLabel.transferFocus();
            //super.mouseExited(e);
        }

        void cleanup() {
            if (namedBean != null) {
                getTurnout().removePropertyChangeListener(this);
            }
            if (typeLabel != null) {
//            _comboBox.removeMouseMotionListener(this);
                //typeLabel.removeMouseListener(this);
                typeLabel = null;
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

        boolean directControl = false;

        public boolean getDirectControl() {
            return directControl;
        }

        public void setDirectControl(boolean m) {
            directControl = m;
        }

        JCheckBoxMenuItem momentaryItem = new JCheckBoxMenuItem(Bundle.getMessage("Momentary"));
        JCheckBoxMenuItem directControlItem = new JCheckBoxMenuItem(Bundle.getMessage("DirectControl"));

        /**
         * Pop-up displays unique attributes of turnouts
         */
        public boolean showPopUp(JPopupMenu popup) {
            if (isEditable()) {
                // add tristate option if turnout has feedback
                if (namedBean != null && getTurnout().getFeedbackMode() != Turnout.DIRECT) {
                    addTristateEntry(popup);
                }

                popup.add(momentaryItem);
                momentaryItem.setSelected(getMomentary());
                momentaryItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        setMomentary(momentaryItem.isSelected());
                    }
                });

                popup.add(directControlItem);
                directControlItem.setSelected(getDirectControl());
                directControlItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        setDirectControl(directControlItem.isSelected());
                    }
                });
            } else if (getDirectControl()) {
                getTurnout().setCommandedState(jmri.Turnout.THROWN);
            }
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

        public void doMouseClicked(java.awt.event.MouseEvent e) {
            //if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            //    return;
            //}
            //if (e.isMetaDown() || e.isAltDown() || !buttonLive() || getMomentary()) {
            //    return;
            //}
            //if (getDirectControl() && !isEditable()) {
            //    getTurnout().setCommandedState(jmri.Turnout.CLOSED);
            //} else {
                alternateOnClick();
            //}
        }

        void alternateOnClick() {
            // TODO add for lights and sensors with a universal: getBean() ?
            char beanTypeLetter = _label.charAt(1);
            switch (beanTypeLetter) {
            case 'T':
                if (getTurnout().getKnownState() == jmri.Turnout.CLOSED) // if clear known state, set to opposite
                {
                    getTurnout().setCommandedState(jmri.Turnout.THROWN);
                } else if (getTurnout().getKnownState() == jmri.Turnout.THROWN) {
                    getTurnout().setCommandedState(jmri.Turnout.CLOSED);
                } else if (getTurnout().getCommandedState() == jmri.Turnout.CLOSED) {
                    getTurnout().setCommandedState(jmri.Turnout.THROWN);  // otherwise, set to opposite of current commanded state if known
                } else {
                    getTurnout().setCommandedState(jmri.Turnout.CLOSED);  // just force closed.
                }
                break;
            case 'L': // Light
                if (getLight().getState() == jmri.Light.OFF) {
                    getLight().setState(jmri.Light.ON);
                } else {
                    getLight().setState(jmri.Light.OFF);
                }
                break;
            case 'S': // Sensor
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
            }
        }

    }

    /*    //Create and set up a colored label. In Grid
    private JLabel createColoredLabel(String text,
                                      Color color) {
        JLabel label = new JLabel(text);
        label.setVerticalAlignment(JLabel.TOP);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.black);
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        label.setPreferredSize(new Dimension(140, 140));
        return label;
    }*/

    // Create the control pane for the top of the frame.
    // From layeredpane demo
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
        prev.setToolTipText("Previous");
        navBarPanel.add(new JLabel ("From:"));
        navBarPanel.add(minSpinner);
        navBarPanel.add(new JLabel ("to:"));
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
        next.setToolTipText("Next");
        navBarPanel.add(Box.createHorizontalGlue());

        //getTargetPane(name).switchboardLayeredPane.setBackgroundColor(Color.RED); // test
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

        //JPanel outerBorderPanel = editToolBarPanel;
        //JPanel innerBorderPanel = editToolBarPanel;

        //Border blacklineBorder = BorderFactory.createLineBorder(Color.black);

        JPanel innerBorderPanel = new JPanel();
        innerBorderPanel.setLayout(new BoxLayout(innerBorderPanel, BoxLayout.PAGE_AXIS));
        TitledBorder TitleBorder = BorderFactory.createTitledBorder("Help ");
        innerBorderPanel.setBorder(TitleBorder);
        innerBorderPanel.add(new JTextArea (Bundle.getMessage("Help1")));
        if (!hideUnconnected()) {
            innerBorderPanel.add(new JTextArea (Bundle.getMessage("Help2")));
        }
        contentPane.add(innerBorderPanel);
        //Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        editToolBarScroll = new JScrollPane(editToolBarPanel);
        //width = screenDim.width;
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
    protected void makeIconMenu() {
//        _iconMenu = new JMenu(Bundle.getMessage("MenuIcon"));
//        _menuBar.add(_iconMenu, 0);
//        JMenuItem mi = new JMenuItem(Bundle.getMessage("MenuItemItemPalette"));
//        mi.addActionListener(new ActionListener() {
//            Editor editor;
//
//            ActionListener init(Editor ed) {
//                editor = ed;
//                return this;
//            }
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (_itemPalette == null) {
//                    _itemPalette = new ItemPalette(Bundle.getMessage("MenuItemItemPalette"), editor);
//                }
//                _itemPalette.setVisible(true);
//            }
//        }.init(this));
//        if (SystemType.isMacOSX()) {
//            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.META_MASK));
//        } else {
//            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
//        }
//        _iconMenu.add(mi);
//        _iconMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemTableList")));
//        mi = (JMenuItem) _iconMenu.getMenuComponent(2);
//        if (SystemType.isMacOSX()) {
//            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.META_MASK));
//        } else {
//            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
//        }
    }

//    protected void makeDrawMenu() {
//        if (_drawMenu == null) {
//            _drawMenu = _shapeDrawer.makeMenu();
//            _drawMenu.add(disableShapeSelect);
//            disableShapeSelect.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent event) {
//                    _disableShapeSelection = disableShapeSelect.isSelected();
//                }
//            });
//        }
//        _menuBar.add(_drawMenu, 0);
//    }

//    public boolean getShapeSelect() {
//        return !_disableShapeSelection;
//    }
//
//    public void setShapeSelect(boolean set) {
//        _disableShapeSelection = !set;
//        disableShapeSelect.setSelected(_disableShapeSelection);
//    }

    //public ShapeDrawer getShapeDrawer() {
//        return _shapeDrawer;
//    }

    //@Override
    protected void makeOptionMenu() {
        _optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));
        _menuBar.add(_optionMenu, 0);
        // use globals item
        _optionMenu.add(useGlobalFlagBox);
        useGlobalFlagBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setUseGlobalFlag(useGlobalFlagBox.isSelected());
            }
        });
        useGlobalFlagBox.setSelected(useGlobalFlag());
        // positionable item
        _optionMenu.add(positionableBox);
        positionableBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setAllPositionable(positionableBox.isSelected());
            }
        });
        positionableBox.setSelected(allPositionable());
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
        // hidden item
        _optionMenu.add(hiddenBox);
        hiddenBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setShowHidden(hiddenBox.isSelected());
            }
        });
        hiddenBox.setSelected(showHidden());

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
    }

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

        editItem = new JMenuItem(Bundle.getMessage("editIndexMenu"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
            SwitchboardEditor panelEd;

            @Override
            public void actionPerformed(ActionEvent e) {
                ImageIndexEditor ii = ImageIndexEditor.instance(panelEd);
                ii.pack();
                ii.setVisible(true);
            }

            ActionListener init(SwitchboardEditor pe) {
                panelEd = pe;
                return this;
            }
        }.init(this));

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

    private JMenu makeSelectTypeMenu() {
        JMenu menu = new JMenu(Bundle.getMessage("SelectType"));
        ButtonGroup typeGroup = new ButtonGroup();
        // I18N use existing jmri.NamedBeanBundle keys
        JRadioButtonMenuItem button = makeSelectTypeButton("BeanNameTurnout", "jmri.jmrit.display.TurnoutIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("BeanNameSensor", "jmri.jmrit.display.SensorIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("Shape", "jmri.jmrit.display.controlPanelEditor.shape.PositionableShape");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("MemoryInput", "jmri.jmrit.display.PositionableJPanel");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("BeanNameLight", "jmri.jmrit.display.LightIcon");
        typeGroup.add(button);
        menu.add(button);
        return menu;
    }

    private JRadioButtonMenuItem makeSelectTypeButton(String label, String className) {
        JRadioButtonMenuItem button = new JRadioButtonMenuItem(Bundle.getMessage(label));
        button.addActionListener(new ActionListener() {
            String cName;

            ActionListener init(String name) {
                cName = name;
                return this;
            }

            @Override
            public void actionPerformed(ActionEvent event) {
                selectType(cName);
            }
        }.init(className));
        return button;
    }

    private void selectType(String name) {
        try {
            Class<?> cl = Class.forName(name);
            _selectionGroup = new ArrayList<Positionable>();
            Iterator<Positionable> it = _contents.iterator();
            while (it.hasNext()) {
                Positionable pos = it.next();
                if (cl.isInstance(pos)) {
                    _selectionGroup.add(pos);
                }
            }
        } catch (ClassNotFoundException cnfe) {
            log.error("selectType Menu " + cnfe.toString());
        }
        _targetPanel.repaint();
    }

    private JMenu makeSelectLevelMenu() {
        JMenu menu = new JMenu(Bundle.getMessage("SelectLevel"));
        ButtonGroup levelGroup = new ButtonGroup();
        JRadioButtonMenuItem button = null;
        for (int i = 0; i < 11; i++) {
            button = new JRadioButtonMenuItem(Bundle.getMessage("selectLevel", "" + i));
            levelGroup.add(button);
            menu.add(button);
            button.addActionListener(new ActionListener() {
                int j;

                ActionListener init(int k) {
                    j = k;
                    return this;
                }

                @Override
                public void actionPerformed(ActionEvent event) {
                    selectLevel(j);
                }
            }.init(i));
        }
        return menu;
    }

    private void selectLevel(int i) {
        _selectionGroup = new ArrayList<Positionable>();
        Iterator<Positionable> it = _contents.iterator();
        while (it.hasNext()) {
            Positionable pos = it.next();
            if (pos.getDisplayLevel() == i) {
                _selectionGroup.add(pos);
            }
        }
        _targetPanel.repaint();
    }

    // *********************** end Menus ************************

    @Override
    public void setAllEditable(boolean edit) {
        if (edit) {
            if (_editorMenu != null) {
                _menuBar.remove(_editorMenu);
            }
            if (_iconMenu == null) {
                makeIconMenu();
            } else {
                _menuBar.add(_iconMenu, 0);
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
            //contentPane.SetUpdateButtonEnabled(false);
        } else {
            if (_fileMenu != null) {
                _menuBar.remove(_fileMenu);
            }
            if (_optionMenu != null) {
                _menuBar.remove(_optionMenu);
            }
            if (_iconMenu != null) {
                _menuBar.remove(_iconMenu);
            }
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
        for (Positionable p : contents) {
            p.setLocation(p.getX() + _fitX, p.getY() + _fitY);
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
        for (Positionable p : contents) {
            minX = Math.min(p.getX(), minX);
            minY = Math.min(p.getY(), minY);
            maxX = Math.max(p.getX() + p.getWidth(), maxX);
            maxY = Math.max(p.getY() + p.getHeight(), maxY);
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
        for (Positionable p : contents) {
            p.setLocation(p.getX() - _fitX, p.getY() - _fitY);
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
        String name = getName();
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
    }

    public boolean hideUnconnected() {
        return _hideUnconnected;
    }

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

    /**
     * Store bean type.
     *
     * @return bean type prefix
     */
    public String getSwitchType() {
        String typePrefix;
        String switchType = beanTypeList.getSelectedItem();
        String light = Bundle.getMessage("BeanNameLight");
        String sensor = Bundle.getMessage("BeanNameSensor");
        switch (switchType) {
            case light:
                typePrefix = "L";
                break;
            case sensor:
                    typePrefix = "S";
                break;
            default: // Turnout
                typePrefix = "T";
        }
        return typePrefix;
    }

    /**
     * Load bean type.
     *
     * @param  bean type prefix
     */
    public void setSwitchType(String typePrefix) {
        String type;
        switch (typePrefix) {
            case "L":
                type = Bundle.getMessage("BeanNameLight");
                break;
            case "S":
                type = Bundle.getMessage("BeanNameSensor");
                break;
            default: // Turnout
                type = Bundle.getMessage("BeanNameTurnout");
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
        return (String) beanManuPrefixes.get(beanManuNames.getSelectedIndex());
    }

    /**
     * Load connection type.
     *
     * @param  bean connection prefix
     */
    public void setSwitchManu(String manuPrefix) {
        int choice = 0;
        for (int i = 0; i < beanManuPrefixes.length; i++) {
            if (beanManuPrefixes.get(i).equals(manuPrefix)) {
                choice = i;
                break;
            }
        }
        try {
        beanManuNames.setSelectedIndex(beanManuPrefixes.getIndex(choice));
        } catch (IllegalArgumentException e) {
            log.error("invalid connection [" + typePrefix + "] in Switchboard");
        }
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
        //positionableBox.setSelected(allPositionable());
        controllingBox.setSelected(allControlling());
        //showCoordinatesBox.setSelected(showCoordinates());
        showTooltipBox.setSelected(showTooltip());
        hiddenBox.setSelected(showHidden());
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

    /**
     * set up item(s) to be copied by paste
     *
     */
    @Override
    protected void copyItem(Positionable p) {
    };

    protected Manager getManager(char typeChar) {
        switch (typeChar) {
            case 'T':
                return InstanceManager.turnoutManagerInstance();
            case 'S':
                return InstanceManager.sensorManagerInstance();
            default: // Light
                return InstanceManager.lightManagerInstance();
        }
    }
    /**
     * ********* KeyListener of Editor ********
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

    /*
 * ********************* Mouse Methods ***********************
 */

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
        _targetPanel.repaint();  // needed for ToolTip
    }

    /**
     * ************* implementation of Abstract Editor methods **********
     */
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
     * JScollPane (js) which contains the targetPane
     *
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
        editMenu.addSeparator();
        editMenu.add(new AbstractAction(Bundle.getMessage("DeletePanel")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (deletePanel()) {
                    dispose(true);
                }
            }
        });
        targetFrame.setJMenuBar(menuBar);
        // add switchboard menu
        JMenu sbMenu = new JMenu(Bundle.getMessage("SwitchboardMenu"));
        sbMenu.add(makeSelectTypeMenu());
        menuBar.add(sbMenu);

        targetFrame.addHelpMenu("package.jmri.jmrit.display.PanelTarget", true);
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
     * Set an object's location when it is created.
     */
    @Override
    public void setNextLocation(Positionable obj) {
        obj.setLocation(0, 0);
    }

    /**
     * Create popup for a Positionable object.
     * <p>
     * Popup items common to all
     * positionable objects are done before and after the items that pertain
     * only to specific Positionable types.
     */
    @Override
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent) p).isVisible()) {
            return;     // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        PositionablePopupUtil util = p.getPopupUtility();
        if (p.isEditable()) {
            // items common to all
            if (p.doViemMenu()) {
                popup.add(p.getNameString());
                setPositionableMenu(p, popup);
                if (p.isPositionable()) {
                    setShowCoordinatesMenu(p, popup);
                    setShowAlignmentMenu(p, popup);
                }
                setDisplayLevelMenu(p, popup);
                setHiddenMenu(p, popup);
                popup.addSeparator();
                //setCopyMenu(p, popup);
            }

            // items with defaults or using overrides
            boolean popupSet = false;
//            popupSet |= p.setRotateOrthogonalMenu(popup);
            popupSet |= p.setRotateMenu(popup);
            popupSet |= p.setScaleMenu(popup);
            if (popupSet) {
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setEditItemMenu(popup);
            if (popupSet) {
                popup.addSeparator();
                popupSet = false;
            }
            if (p instanceof PositionableLabel) {
                PositionableLabel pl = (PositionableLabel) p;
                /*                if (pl.isIcon() && "javax.swing.JLabel".equals(pl.getClass().getSuperclass().getName()) ) {
                    popupSet |= setTextAttributes(pl, popup);       // only for plain icons
                }   Add backgrounds & text over icons later */
                if (!pl.isIcon()) {
                    popupSet |= setTextAttributes(pl, popup);
                    if (p instanceof MemoryIcon) {
                        popupSet |= p.setTextEditMenu(popup);
                    }
                } else if (p instanceof SensorIcon) {
                    popup.add(CoordinateEdit.getTextEditAction(p, "OverlayText"));
                    if (pl.isText()) {
                        popupSet |= setTextAttributes(p, popup);
                    }
                } else {
                    popupSet = p.setTextEditMenu(popup);
                }
            } else if (p instanceof PositionableJPanel) {
                popupSet |= setTextAttributes(p, popup);
            }
            if (p instanceof LinkingObject) {
                ((LinkingObject) p).setLinkMenu(popup);
            }
            if (popupSet) {
                popup.addSeparator();
                popupSet = false;
            }
            p.setDisableControlMenu(popup);
            if (util != null) {
                util.setAdditionalEditPopUpMenu(popup);
            }
            // for Positionables with unique settings
            p.showPopUp(popup);

            if (p.doViemMenu()) {
                setShowTooltipMenu(p, popup);
                setRemoveMenu(p, popup);
            }
        } else {
            p.showPopUp(popup);
            if (util != null) {
                util.setAdditionalViewPopUpMenu(popup);
            }
        }
        popup.show((Component) p, p.getWidth() / 2 + (int) ((getPaintScale() - 1.0) * p.getX()),
                p.getHeight() / 2 + (int) ((getPaintScale() - 1.0) * p.getY()));

        _currentSelection = null;
    }

    protected ArrayList<Positionable> getSelectionGroup() {
        return _selectionGroup;
    }

    private final static Logger log = LoggerFactory.getLogger(SwitchboardEditor.class.getName());
}
