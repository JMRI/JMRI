
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
    public ItemPanel(JmriJFrame parentFrame, String  itemType, Editor editor) {
        _paletteFrame = parentFrame;
        _itemType = itemType;
        _editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    abstract public void init();

    /* Methods used upon return from Icon dialogs
    * to update the panel for TableItemPanel item types.
    */    
    protected void initIconFamiliesPanel() {
    }
    protected void setFamily(String family) {
    }
    protected void removeIconFamiliesPanel() {
    }
    protected void removeIconMap(String family) {
    }
    protected void reset() {
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
    * SignalHeadItemPanel overrides for valid states when SignalHead is known
    */
    protected Hashtable<String, NamedIcon> getFilteredIconMap() {
        Hashtable<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
        if (map==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return map;
    }

    /**
    * TableItemPanel.java overrides for its itemTypes.  This is for the remainder
    */
    protected void openEditDialog() {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\"");
        if (_itemType.equals("RPSReporter")) {
            new IconDialog(_itemType, _family, this);
        } else {
            new SingleIconDialog(_itemType, _family, this);
        }
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
