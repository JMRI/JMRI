package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanels for the various item types that can be added to a Panel - e.g. Sensors,
 * Turnouts, etc.
 *
 * Devices such as these have sets of icons to display their various states.
 * Such sets are called a "family" in the code. These devices then may have sets
 * of families to provide the user with a choice of the icon set to use for a
 * particular device.
 * These sets/families are defined in an xml file stored as xml/defaultPanelIcons.xml
 * including the icon file paths, to be loaded by an iterator.
 * The subclass FamilyItemPanel.java and its subclasses handles these devices.
 *
 * Other devices, e.g. Backgrounds or Memory, may use only one or no icon to
 * display. The subclass IconItemPanel.java and its subclasses handles these
 * devices.
 * @see jmri.jmrit.display.DisplayFrame for class diagram for the palette package.
 *
 * @author Pete Cressman Copyright (c) 2010
 * @author Egbert Broerse Copyright 2017
 */
public abstract class ItemPanel extends JPanel {

    protected DisplayFrame _paletteFrame;
    protected String _itemType;
    protected Editor _editor;
    protected boolean _initialized = false; // has init() been run
    protected boolean _update = false;      // editing existing icon, do not allow icon dragging. Set in init()
    protected boolean _suppressDragging;
    protected JTextField _linkName = new JTextField(30);
    static Color _grayColor = new Color(235, 235, 235);
    static Color _darkGrayColor = new Color(150, 150, 150);
    static protected Color[] colorChoice = new Color[]{Color.white, _grayColor, _darkGrayColor}; // panel bg color picked up directly
    /**
     * Array of BufferedImage backgrounds loaded as background image in Preview (not shared across tabs)
     */
    protected BufferedImage[] _backgrounds;
    /**
     * JComboBox to choose the above backgrounds
     */
    protected JComboBox<String> _bgColorBox = null;
    /**
     * Constructor for all item types.
     *
     * @param parentFrame ItemPalette instance
     * @param type        identifier of the ItemPanel type
     * @param editor      Editor that called this ItemPalette
     */
    public ItemPanel(DisplayFrame parentFrame, String type, Editor editor) {
        _paletteFrame = parentFrame;
        _itemType = type;
        updateBackgrounds(editor);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Initialize panel for selecting a new Control Panel item or for updating
     * an existing item. Adds table if item is a bean. i.e. customizes for the
     * item type.
     * Called by enclosing TabbedPanel on change of displayed tab Pane.
     */
    public void init() {
        if (!_initialized) {
            add(Box.createVerticalGlue());
            _initialized = true;
        }
    }

    /**
     * Initialization for conversion of plain track to indicator track by CircuitBuilder.
     * @param doneAction Callback action for Done button
     */
    abstract public void init(ActionListener doneAction);


    protected void setEditor(Editor ed) {
        updateBackgrounds(ed);    // editor change may change panel background
        if (_bgColorBox != null) {
            _bgColorBox.setSelectedIndex(_paletteFrame.getPreviewBg());
        }
    }

    /*
     * Notification to itemPanel to update child dialogs, if any
     */
    abstract protected void setPreviewBg(int index);
    
    abstract protected void updateBackground0(BufferedImage im);

    public boolean oktoUpdate() {
        return true;
    }

    protected void initLinkPanel() {
//        Font font = new Font("SansSerif", Font.BOLD, 12);
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("ToLinkToURL", "Text")));
        blurb.add(new JLabel(Bundle.getMessage("enterPanel")));
        blurb.add(new JLabel(Bundle.getMessage("enterURL")));
        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("LinkName")));
        panel.add(_linkName);
        _linkName.setToolTipText(Bundle.getMessage("ToolTipLink"));
        panel.setToolTipText(Bundle.getMessage("ToolTipLink"));
        blurb.add(panel);
        add(blurb);
    }

    /**
     * Create panel element containing [Set background:] drop down list.
     *
     * @see DecoratorPanel
     * @param preview1 preview pane1 to set background image on
     * @param preview2 (optional) second preview pane1 to set background image on
     * @return JPanel with label and drop down with actions
     */
    protected JPanel makeBgButtonPanel(ImagePanel preview1, ImagePanel preview2) {
        if (_bgColorBox == null) {
            _bgColorBox = new JComboBox<>();
            _bgColorBox.addItem(Bundle.getMessage("PanelBgColor")); // PanelColor key is specific for CPE, but too long for combo
            _bgColorBox.addItem(Bundle.getMessage("White"));
            _bgColorBox.addItem(Bundle.getMessage("LightGray"));
            _bgColorBox.addItem(Bundle.getMessage("DarkGray"));
            _bgColorBox.addItem(Bundle.getMessage("Checkers"));
            _bgColorBox.setSelectedIndex(_paletteFrame.getPreviewBg()); // Global field, starts as 0 = panel bg color
            _bgColorBox.addActionListener((ActionEvent e) -> {
                if (_backgrounds != null) {
                    int previewBgSet = _bgColorBox.getSelectedIndex();
                    _paletteFrame.setPreviewBg(previewBgSet); // store user choice in field on parent
                    setPreviewBg(previewBgSet);
                    // load background image
                    log.debug("ItemPalette setImage called {}", previewBgSet);
                    if (preview1 != null) {
                        preview1.setImage(_backgrounds[previewBgSet]);
                        preview1.revalidate(); // force redraw
                    }
                    if (preview2 != null) {
                        preview2.setImage(_backgrounds[previewBgSet]);
                        preview2.revalidate(); // force redraw
                    }
                } else {
                    log.debug("imgArray is empty");
                }
            });
        }
        JPanel backgroundPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backgroundPanel.add(new JLabel(Bundle.getMessage("setBackground")));
        backgroundPanel.add(_bgColorBox);
        return backgroundPanel;
    }

    /**
     * Create array of backgrounds for preview pane.
     * @param ed Panel editor
     */
    protected void updateBackgrounds(Editor ed) {
        _editor = ed;
        Color currentBackground = ed.getTargetPanel().getBackground(); // start using Panel background color
        _backgrounds = makeBackgrounds(_backgrounds, currentBackground);
    }

    static protected BufferedImage[] makeBackgrounds(BufferedImage[] backgrounds, Color panelBackground) {
        if (backgrounds == null) { // reduces load but will not redraw for new size
            backgrounds = new BufferedImage[5];
            for (int i = 1; i <= 3; i++) {
                backgrounds[i] = DrawSquares.getImage(500, 400, 10, colorChoice[i - 1], colorChoice[i - 1]);
                // [i-1] because choice 0 is not in colorChoice[]
            }
            backgrounds[4] = DrawSquares.getImage(500, 400, 10, Color.white, _grayColor);
        }
        // always update background from Panel Editor
        backgrounds[0] = DrawSquares.getImage(500, 400, 10, panelBackground, panelBackground);
        log.debug("makeBackgrounds backgrounds[0] = {}", backgrounds[0]);
        return backgrounds;
    }

    protected JPanel makePreviewPanel(ImagePanel panel1, ImagePanel panel2) {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("PreviewBorderTitle")));
        makeBgButtonPanel(panel1, panel2);
        if (_bgColorBox != null) {
            JPanel bkgdBoxPanel = new JPanel();
            bkgdBoxPanel.add(new JLabel(Bundle.getMessage("setBackground"), SwingConstants.RIGHT));
            bkgdBoxPanel.add(_bgColorBox);
            previewPanel.add(bkgdBoxPanel);            
            _bgColorBox.setSelectedIndex(_paletteFrame.getPreviewBg());
        }
        if (panel1 != null) {
            previewPanel.add(panel1);            
        }
        if (panel2 != null) {
            previewPanel.add(panel2);            
        }
        return previewPanel;
    }

    public void closeDialogs() {
    }

    protected void reset() {
        closeDialogs();
    }

    protected final boolean isUpdate() {
        return _update;
    }

    /*
     * ****** Default family icon names *******
     *
     * NOTE: Names supplied must be available as properties keys and also match the
     * element names defined in xml/defaultPanelIcons.xml
     */
    static final String[] TURNOUT = {"TurnoutStateClosed", "TurnoutStateThrown",
            "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] SENSOR = {"SensorStateActive", "SensorStateInactive",
            "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] SIGNALHEAD = {"SignalHeadStateRed", "SignalHeadStateYellow",
            "SignalHeadStateGreen", "SignalHeadStateDark",
            "SignalHeadStateHeld", "SignalHeadStateLunar",
            "SignalHeadStateFlashingRed", "SignalHeadStateFlashingYellow",
            "SignalHeadStateFlashingGreen", "SignalHeadStateFlashingLunar"};
    static final String[] LIGHT = {"StateOff", "StateOn",
            "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] MULTISENSOR = {"SensorStateInactive", "BeanStateInconsistent",
            "BeanStateUnknown", "first", "second", "third"};
    // SIGNALMAST family is empty for now
    static final String[] RPSREPORTER = {"active", "error"};
    static final String[] ICON = {"Icon"};
    static final String[] BACKGROUND = {"Background"};
    static final String[] INDICATOR_TRACK = {"ClearTrack", "OccupiedTrack", "AllocatedTrack",
            "PositionTrack", "DontUseTrack", "ErrorTrack"};
    static final String[] PORTAL = {PortalIcon.HIDDEN, PortalIcon.VISIBLE, PortalIcon.PATH,
            PortalIcon.TO_ARROW, PortalIcon.FROM_ARROW};

    @Nonnull
    static private String[] getNames(String type) {
        if (type.equals("Turnout")) {
            return TURNOUT;
        } else if (type.equals("Sensor")) {
            return SENSOR;
        } else if (type.equals("SignalHead")) {
            return SIGNALHEAD;
        } else if (type.equals("Light")) {
            return LIGHT;
        } else if (type.equals("MultiSensor")) {
            return MULTISENSOR;
        } else if (type.equals("Icon")) {
            return ICON;
        } else if (type.equals("Background")) {
            return BACKGROUND;
        } else if (type.equals("RPSReporter")) {
            return RPSREPORTER;
        } else if (type.equals("IndicatorTrack")) {
            return INDICATOR_TRACK;
        } else if (type.equals("IndicatorTO")) {
            return INDICATOR_TRACK;
        } else if (type.equals("Portal")) {
            return PORTAL;
        } else {
            log.error("Item type \"{}\" cannot create icon sets!", type);
            return new String[]{};
        }
    }

    static String redX = "resources/icons/misc/X-red.gif";

    @Nonnull
    static protected HashMap<String, NamedIcon> makeNewIconMap(String type) {
        HashMap<String, NamedIcon> newMap = new HashMap<>();
        String[] names = getNames(type);
        for (int i = 0; i < names.length; i++) {
            NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(redX, redX);
            newMap.put(names[i], icon);
        }
        return newMap;
    }

    static protected void checkIconMap(String type, HashMap<String, NamedIcon> map) {
        String[] names = getNames(type);
        for (int i = 0; i < names.length; i++) {
            if (map.get(names[i]) == null) {
                NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(redX, redX);
                // store RedX as default icon if icon not set
                map.put(names[i], icon);
            }
        }
    }

    protected DisplayFrame getParentFrame() {
        return _paletteFrame;
    }

    protected void reSizeDisplay(boolean isPalette, Dimension oldDim, Dimension totalDim) {
        Dimension newDim = getPreferredSize();
        Dimension deltaDim = new Dimension(totalDim.width - oldDim.width, totalDim.height - oldDim.height);
        if (log.isDebugEnabled()) 
            log.debug("resize by {} Dim= ({}, {}) \"{}\" OldDim= ({}, {}) NewDim= ({}, {})",
                    (isPalette?"TabPane":"JFrame"), totalDim.width, totalDim.height,
                    this._itemType, oldDim.width, oldDim.height, newDim.width, newDim.height);

        if (isPalette && _initialized) {
            _paletteFrame.reSize(ItemPalette._tabPane, deltaDim, newDim, _editor);            
        } else if (_update || _initialized) {
            _paletteFrame.reSize(_paletteFrame, deltaDim, newDim, _editor);                            
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ItemPanel.class);

}
