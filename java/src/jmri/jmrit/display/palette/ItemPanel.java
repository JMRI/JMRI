package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanels for the various item types that come from tool Tables - e.g. Sensors,
 * Turnouts, etc.
 * 
* Devices such as these have sets of icons to display their various states.
 * such sets are called a "family" in the code. These devices then may have sets
 * of families to provide the user with a choice of the icon set to use for a
 * particular device. The subclass FamilyItemPanel.java and its subclasses
 * handles these devices.
 * 
* Other devices, e.g. backgrounds or memory, may use only one or no icon to
 * display. The subclass IconItemPanel.java and its subclasses handles these
 * devices.
 */
public abstract class ItemPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -6256134896588725084L;
    protected JmriJFrame _paletteFrame;
    protected String _itemType;
    protected Editor _editor;
    protected boolean _initialized = false;    // Has init() been run
    protected boolean _update = false;    // Editing existing icon, do not allow icon dragging. set in init()
    JTextField _linkName = new JTextField(30);

    /**
     * Constructor for all table types. When item is a bean, the itemType is the
     * name key for the item in jmri.NamedBeanBundle.properties
     */
    public ItemPanel(JmriJFrame parentFrame, String type, Editor editor) {
        _paletteFrame = parentFrame;
        _itemType = type;
        _editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Initializes panel for selecting a new control panel item or for updating
     * an existing item. Adds table if item is a bean. i.e. customizes for the
     * item type
     */
    public void init() {
        _initialized = true;
    }

    protected void initLinkPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
        panel.add(new JLabel(Bundle.getMessage("LinkName")));
        panel.add(_linkName);
        _linkName.setToolTipText(Bundle.getMessage("ToolTipLink"));
        panel.setToolTipText(Bundle.getMessage("ToolTipLink"));

        add(panel);
    }

    protected void closeDialogs() {
    }

    protected void reset() {
        _paletteFrame.repaint();
    }

    protected final boolean isUpdate() {
        return _update;
    }

    /**
     * ****** Default family icon names *******
     */
    static final String[] TURNOUT = {"TurnoutStateClosed", "TurnoutStateThrown",
        "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] SENSOR = {"SensorStateActive", "SensorStateInactive",
        "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] SIGNAL = {"SignalHeadStateRed", "SignalHeadStateYellow",
        "SignalHeadStateGreen", "SignalHeadStateDark",
        "SignalHeadStateHeld", "SignalHeadStateLunar",
        "SignalHeadStateFlashingRed", "SignalHeadStateFlashingYellow",
        "SignalHeadStateFlashingGreen", "SignalHeadStateFlashingLunar"};
    static final String[] LIGHT = {"LightStateOff", "LightStateOn",
        "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] MULTISENSOR = {"SensorStateInactive", "BeanStateInconsistent",
        "BeanStateUnknown", "first", "second", "third"};

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
            return SIGNAL;
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
            log.error("Item type \"" + type + "\" cannot create icon sets!");
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
                map.put(names[i], icon);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ItemPanel.class.getName());
}
