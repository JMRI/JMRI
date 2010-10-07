
package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public abstract class ItemPanel extends JPanel {

    protected ItemPalette  _paletteFrame;
    protected String    _itemType;
    protected String    _family;
    protected Editor    _editor;

    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public ItemPanel(ItemPalette parentFrame, String  itemType, Editor editor) {
        _paletteFrame = parentFrame;
        _itemType = itemType;
        _editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    abstract public void init();

    /* Methods to customize panel for TableItemPanel item types.  */    

    protected void initIconFamiliesPanel() {
    }
    protected void setFamily(String family) {
    }
    protected void removeIconFamiliesPanel() {
    }
    protected void removeIconMap(String family) {
    }
    protected void hideIcons() {
    }


    /**
    * TableItemPanel.java overrides for its itemTypes.  This is for the remainder
    */
    protected void openEditDialog() {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\"");
        if (_family!=null) {
            if (_itemType.equals("RPSReporter")) {
                new IconDialog(_itemType, _family, ItemPalette.getIconMap(_itemType, _family), this);
            } else {
                new SingleIconDialog(_itemType, _family, ItemPalette.getIconMap(_itemType, _family), this);
            }
        } else {
            Hashtable<String, NamedIcon> map = makeNewIconMap(_itemType);
            if (_itemType.equals("RPSReporter")) {
                new IconDialog(_itemType, map, this);
            } else {
                new SingleIconDialog(_itemType, map, this);
            }
        }
    }

    protected ItemPalette getPaletteFrame() {
       return _paletteFrame;
    }

    /******** Default family icon names ********/
    static protected String[] TURNOUT = {"TurnoutStateClosed", "TurnoutStateThrown",
                                         "BeanStateInconsistent", "BeanStateUnknown"};
    static protected String[] SENSOR = {"SensorStateActive", "SensorStateInactive",
                                        "BeanStateInconsistent", "BeanStateUnknown"};
    static protected String[] SIGNAL = {"SignalHeadStateRed", "SignalHeadStateYellow",
                                        "SignalHeadStateGreen", "SignalHeadStateDark",
                                        "SignalHeadStateHeld", "SignalHeadStateLunar",
                                        "SignalHeadStateFlashingRed", "SignalHeadStateFlashingYellow",
                                        "SignalHeadStateFlashingGreen", "SignalHeadStateFlashingLunar"};
    static protected String[] LIGHT = {"LightStateOff", "LightStateOn",
                                       "BeanStateInconsistent", "BeanStateUnknown"};
    static protected String[] MULTISENSOR = {"SensorStateInactive", "BeanStateInconsistent",
                                             "BeanStateUnknown", "first", "second", "third"};

    static protected String[] RPSREPORTER = {"active", "error"};
    static protected String[] ICON = {"Icon"};
    static protected String[] BACKGROUND = {"Background"};


    protected Hashtable<String, NamedIcon> makeNewIconMap(String type) {
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
