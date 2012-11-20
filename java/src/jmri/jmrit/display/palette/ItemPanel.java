
package jmri.jmrit.display.palette;

import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jmri.util.JmriJFrame;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*  
*  Devices such as these have sets of icons to display their various states.  such sets are called
*  a "family" in the code.  These devices then may have sets of families to provide the user with
*  a choice of the icon set to use for a particular device.  The subclass FamilyItemPanel.java
*  and its subclasses handles these devices.
*  
*  Other devices, e.g. backgrounds or memory, may use only one or no icon to display.  The subclass 
*  IconItemPanel.java and its subclasses handles these devices.
*/
public abstract class ItemPanel extends JPanel {

    protected JmriJFrame  _paletteFrame;
    protected String    _itemType;
    protected String    _family;
    protected Editor    _editor;
    protected boolean   _update = false;    // Editing existing icon, do not allow icon dragging. set in init()
    protected boolean   _initialized = false;    // Has init() been run

    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public ItemPanel(JmriJFrame parentFrame, String  type, String family, Editor editor) {
        _paletteFrame = parentFrame;
        _itemType = type;
        if (family!=null && family.trim().length()>0) {
            _family = family;
        } else {
            _family = null;
        }
        _editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Initializes panel for selecting a new control panel item or for updating an existing item.
     * Adds table if item is a bean.  i.e. customizes for the item type
     */
    public void init() {
    	_initialized = true;
    }

    /* Methods used upon return from Icon dialogs
    * to update the panel for TableItemPanel item types.
    */    
    protected void initIconFamiliesPanel(){
    }
    protected void addIconsToPanel(Hashtable<String, NamedIcon> iconMap){
    }
    protected void setFamily(String family) {
    }
    protected void removeIconFamiliesPanel() {
    }
    protected void removeIconMap(String family) {
    }
    protected void reset() {
      _paletteFrame.pack();
      _paletteFrame.invalidate();
      invalidate();
      _paletteFrame.repaint();
    }
    protected void updateFamiliesPanel() {
    }
    public void dispose() {
    } 
    
    public String getFamilyName() {
        return _family;
    }

    protected final boolean isUpdate() {
    	return _update;
    }
    
    /******** Default family icon names ********/
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

    static protected Hashtable<String, NamedIcon> makeNewIconMap(String type) {
        Hashtable <String, NamedIcon> newMap = new Hashtable <String, NamedIcon>();
        String[] names = null;
        if (type.equals("Turnout")) {
            names = TURNOUT;
        } else if (type.equals("Sensor")) {
            names = SENSOR;
        } else if (type.equals("SignalHead")) {
            names = SIGNAL;
        } else if (type.equals("Light")) {
            names = LIGHT;
        } else if (type.equals("MultiSensor")) {
            names = MULTISENSOR;
        } else if (type.equals("Icon")) {
            names = ICON;
        } else if (type.equals("Background")) {
            names = BACKGROUND;
        } else if (type.equals("RPSReporter")) {
            names = RPSREPORTER;
        } else if (type.equals("IndicatorTrack")) {
            names = INDICATOR_TRACK;
        } else if (type.equals("IndicatorTO")) {
            names = INDICATOR_TRACK;
        } else {
            log.error("Item type \""+type+"\" cannot create icon sets!");
            return null;
        }
        for (int i=0; i<names.length; i++) {
           String fileName = "resources/icons/misc/X-red.gif";
           NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
           newMap.put(names[i], icon);
        }
        return newMap;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ItemPanel.class.getName());
}
