// ItemDialog.java
package jmri.jmrit.display.palette;

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
        super(parent._paletteFrame, title, mode);
        _type = type;
        _family = family;
        _parent = parent;
    }

    protected void init() {
        setSize(_parent.getSize().width, this.getPreferredSize().height);
        setLocationRelativeTo(_parent);
        setVisible(true);
        pack();
    }
    
    protected void addFamily(String family, Hashtable<String, NamedIcon> iconMap) {
        ItemPalette.addFamily(_type, family, iconMap);
        _parent._family = family;
        _parent.reset();
    }

    protected String getType() {
        return _type;
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ItemDialog.class.getName());
}
