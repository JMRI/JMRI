// ItemDialog.java
package jmri.jmrit.display.palette;

import java.util.Iterator;
import java.util.Hashtable;
import javax.swing.JDialog;

import jmri.jmrit.catalog.NamedIcon;

/**
 * @author Pete Cressman  Copyright (c) 2010
 */

public class ItemDialog extends JDialog {

    protected ItemPanel _parent;
    protected String    _type;
    protected String    _family;

    /**
    */
    public ItemDialog(String type, String family, String title, ItemPanel parent, boolean mode) {
        super(parent.getPaletteFrame(), title, mode);
        _type = type;
        _family = family;
        _parent = parent;
    }

    protected void init() {
        pack();
        setSize(_parent.getSize().width, this.getPreferredSize().height);
        setLocationRelativeTo(_parent);
        setVisible(true);
    }
    
    protected void createNewFamily(Hashtable<String, NamedIcon> newMap) {
        if (_type.equals("MultiSensor")) {
            new MultiSensorIconDialog(_type, newMap, _parent);
        } else if (_type.equals("Icon") || _type.equals("Background")) {
            new SingleIconDialog(_type, newMap, _parent);
        } else {
            new IconDialog(_type, newMap, _parent);
        }
    }

    protected void updateFamiliesPanel() {
        _parent.hideIcons();
        _parent.getPaletteFrame().updateFamiliesPanel(_type);
    }

    protected void addFamily(String family, Hashtable<String, NamedIcon> iconMap) {
        _parent.getPaletteFrame().addFamily(_type, family, iconMap);
    }

    protected Hashtable<String, Hashtable<String, NamedIcon>> getFamilyMaps() {
       return _parent.getPaletteFrame().getFamilyMaps(_type);
    }

    protected String getType() {
        return _type;
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ItemDialog.class.getName());
}
