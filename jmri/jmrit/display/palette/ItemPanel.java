
package jmri.jmrit.display.palette;

import java.util.Hashtable;
import jmri.util.JmriJFrame;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public abstract class ItemPanel extends JPanel {

    protected JmriJFrame  _paletteFrame;
    protected String    _itemType;
    protected String    _family;
    protected Editor    _editor;

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

    abstract public void init();

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
    }
    public void dispose() {
    }

    public String getFamilyName() {
        return _family;
    }

    protected void updateFamiliesPanel() {
        if (log.isDebugEnabled()) log.debug("updateFamiliesPanel for "+_itemType);
        removeIconFamiliesPanel();
        initIconFamiliesPanel();
        reset();
        validate();
        repaint();
        _paletteFrame.pack();
    }

    /**
    * overriden for many itemTypes.  This is for the remainder
    */
    protected void openEditDialog() {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\"");
        IconDialog dialog = new IconDialog(_itemType, _family, this);
        // call super ItemDialog to size and locate dialog
        dialog.sizeLocate();
    }

    /**
    * overriden for many itemTypes.  This is for the remainder
    */
    protected void createNewFamily(String type) {
        IconDialog dialog =new IconDialog(type, null, this);
        dialog.sizeLocate();
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
