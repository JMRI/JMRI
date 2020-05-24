package jmri.jmrit.display.palette;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.PreviewPanel;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
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
 * @author Pete Cressman Copyright (c) 2010, 2020
 * @author Egbert Broerse Copyright 2017
 */
public abstract class ItemPanel extends JPanel {

    protected DisplayFrame _frame;
    protected String _itemType;
    protected boolean _initialized = false; // has init() been run
    protected boolean _update = false;      // editing existing icon, do not allow icon dragging. Set in init()
    protected boolean _suppressDragging;
    protected JTextField _linkName = new JTextField(30);
    protected PreviewPanel _previewPanel;
    /**
     * Constructor for all item types.
     *
     * @param parentFrame ItemPalette instance
     * @param type        identifier of the ItemPanel type
     */
    public ItemPanel(DisplayFrame parentFrame, String type) {
        _frame = parentFrame;
        _itemType = type;
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

    /**
     * A different panel has the focus and may have a different panel background
     * or a PreviewPanel has changed a viewing background
     *
    protected void editorChange() {
    }*/

    protected void previewColorChange() {
        if (_previewPanel != null) {
            _previewPanel.setBackgroundSelection(_frame.getPreviewBg());
            _previewPanel.invalidate();
        }
    }

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

    @Nonnull
    static protected HashMap<String, NamedIcon> makeNewIconMap(String type) {
        HashMap<String, NamedIcon> newMap = new HashMap<>();
        String[] names = getNames(type);
        for (String name : names) {
            NamedIcon icon = new NamedIcon(ItemPalette.RED_X, ItemPalette.RED_X);
            newMap.put(name, icon);
        }
        return newMap;
    }

    static protected void checkIconMap(String type, HashMap<String, NamedIcon> map) {
        String[] names = getNames(type);
        for (String name : names) {
            if (map.get(name) == null) {
                NamedIcon icon = new NamedIcon(ItemPalette.RED_X, ItemPalette.RED_X);
                // store RedX as default icon if icon not set
                map.put(name, icon);
            }
        }
    }

    protected DisplayFrame getParentFrame() {
        return _frame;
    }

    // oldDim old panel size,
    // totalDim old frame size
    protected void reSizeDisplay(boolean isPalette, Dimension oldDim, Dimension totalDim) {
        Dimension newDim = getPreferredSize();
        Dimension frameDiffDim = new Dimension(totalDim.width - oldDim.width, totalDim.height - oldDim.height);
        if (log.isDebugEnabled()) {
            // Gather data for additional dimensions needed to display new panel in the total frame
            log.debug("resize {} for type {}. totalDim= ({}, {}) \"{}\" OldDim= ({}, {}) frameDiffDim= ({}, {})",
                    (isPalette?"tabPane":"update"), _itemType, totalDim.width, totalDim.height,
                    this._itemType, oldDim.width, oldDim.height, frameDiffDim.width, frameDiffDim.height);
        }
        Dimension deltaDim = shellDimension(this);
        if (isPalette && _initialized) {
            _frame.reSize(ItemPalette._tabPane, deltaDim, newDim);
        } else if (_update || _initialized) {
            _frame.reSize(_frame, deltaDim, newDim);                            
        }
    }

    public Dimension shellDimension(ItemPanel panel) {
        if (panel instanceof FamilyItemPanel) {
            if (panel._itemType.equals("SignalMast") || panel._itemType.equals("Reporter")) {
                return new Dimension(23, 136);
            }
            if (panel._itemType.equals("RPSReporter") || panel._itemType.equals("Portal")) {
                return new Dimension(10, 80);
            }
            return new Dimension(23, 122);
        } else if (panel instanceof IconItemPanel) {
            return new Dimension(20, 90);
        }
        return new Dimension(8, 120);
    }
    private final static Logger log = LoggerFactory.getLogger(ItemPanel.class);
}
