package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.jmrit.display.palette.InitEventListener;
import jmri.jmrit.display.palette.ItemPalette;
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
    JTextField _linkName = new JTextField(30);
    static Color _grayColor = new Color(235, 235, 235);
    static Color _darkGrayColor = new Color(150, 150, 150);
    protected Color[] colorChoice = new Color[]{Color.white, _grayColor, _darkGrayColor}; // panel bg color picked up directly
    /**
     * Active base color for Preview background, read from active Panel where available.
     */
    protected Color _currentBackground = _grayColor;
    /**
     * Array of BufferedImage backgrounds loaded as background image in Preview (not shared across tabs)
     */
    protected BufferedImage[] _backgrounds;
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
        _editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Initialize panel for selecting a new Control Panel item or for updating
     * an existing item. Adds table if item is a bean. i.e. customizes for the
     * item type.
     * Called by enclosing TabbedPanel on change of displayed tab Pane.
     */
    public void init() {
        _initialized = true;
    }

    protected void setEditor(Editor ed) {
        _editor = ed;
    }

    protected void initLinkPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel(Bundle.getMessage("LinkName")));
        panel.add(_linkName);
        _linkName.setToolTipText(Bundle.getMessage("ToolTipLink"));
        panel.setToolTipText(Bundle.getMessage("ToolTipLink"));
        add(panel);
    }

    /**
     * Create panel element containing [Set background:] drop down list.
     *
     * @see jmri.jmrit.catalog.PreviewDialog#setupPanel()
     * @see DecoratorPanel
     * @param preview1 preview pane1 to set background image on
     * @param preview2 (optional) second preview pane1 to set background image on
     * @param imgArray the image array to choose from
     * @return JPanel with label and drop down with actions
     */
    protected JPanel makeBgButtonPanel(@Nonnull ImagePanel preview1, ImagePanel preview2, BufferedImage[] imgArray, DisplayFrame parent) {
        BgComboBox<String> bgColorBox = new BgComboBox<>();
        bgColorBox.addItem(Bundle.getMessage("PanelBgColor")); // PanelColor key is specific for CPE, but too long for combo
        bgColorBox.addItem(Bundle.getMessage("White"));
        bgColorBox.addItem(Bundle.getMessage("LightGray"));
        bgColorBox.addItem(Bundle.getMessage("DarkGray"));
        bgColorBox.addItem(Bundle.getMessage("Checkers"));
        bgColorBox.setSelectedIndex(parent.getPreviewBg()); // Global field, starts as 0 = panel bg color
        bgColorBox.addActionListener((ActionEvent e) -> {
            if (imgArray != null) {
                int previewBgSet = bgColorBox.getSelectedIndex();
                parent.setPreviewBg(previewBgSet); // store user choice in field on parent
                // load background image
                log.debug("ItemPalette setImage called {}", previewBgSet);
                preview1.setImage(imgArray[previewBgSet]);
                preview1.revalidate(); // force redraw
                if (preview2 != null) {
                    preview2.setImage(imgArray[previewBgSet]);
                    preview2.revalidate(); // force redraw
                }
            } else {
                log.debug("imgArray is empty");
            }
        });
        if (parent instanceof ItemPalette) { // if Panel is part of an ItemPalette JTabbedFrame
            parent.setInitEventListener(bgColorBox); // register custom init (show) event
        }
        JPanel backgroundPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backgroundPanel.add(new JLabel(Bundle.getMessage("setBackground")));
        backgroundPanel.add(bgColorBox);
        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
        return backgroundPanel;
    }

    /**
     * Create array of backgrounds for preview pane.
     */
    protected void updateBackgrounds() {
        if (_backgrounds == null) { // reduces load but will not redraw for new size
            _backgrounds = new BufferedImage[5];
            for (int i = 1; i <= 3; i++) {
                _backgrounds[i] = DrawSquares.getImage(500, 400, 10, colorChoice[i - 1], colorChoice[i - 1]);
                // [i-1] because choice 0 is not in colorChoice[]
            }
            _backgrounds[4] = DrawSquares.getImage(500, 400, 10, Color.white, _grayColor);
        }
        // always update background from Panel Editor
        _currentBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
        _backgrounds[0] = DrawSquares.getImage(500, 400, 10, _currentBackground, _currentBackground);
    }

    protected void closeDialogs() {
    }

    protected void reset() {
        // _paletteFrame.repaint();
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
            return null;
        }
    }

    static String redX = "resources/icons/misc/X-red.gif";

    static protected HashMap<String, NamedIcon> makeNewIconMap(String type) {
        HashMap<String, NamedIcon> newMap = new HashMap<String, NamedIcon>();
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

    /**
     * Class for bgColorCombo.
     *
     * @param <T> the type of combobox
     */
    public class BgComboBox<T> extends JComboBox<T> implements InitEventListener {

        /**
         * Store value from parent when changed on another tab.
         *
         * @param choice index of colorBox, to be applied to preview backgrounds
         */
        public void onInitEvent(int choice, int selectedPane) {
            setVisible(false);
            log.debug("InitEvent seen by tab#{} comboBox, was {}, set to {}", selectedPane, this.getSelectedIndex(), choice);
            setSelectedIndex(choice); // update bgColorBox when tab shows (doesn't always work)
            setVisible(true); // force redraw of updated selection
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ItemPanel.class);

}
