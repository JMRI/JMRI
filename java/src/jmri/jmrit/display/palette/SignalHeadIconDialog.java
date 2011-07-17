
// SignalHeadIconDialog(.java
package jmri.jmrit.display.palette;

/**
 *
 * @author Pete Cressman  Copyright (c) 2010
 */

public class SignalHeadIconDialog extends IconDialog {

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public SignalHeadIconDialog(String type, String family, ItemPanel parent) {
        super(type, family,parent);
    }

    protected void initMap(String type, String family) {
        _familyName.setEditable(true);
        if (family!=null) {
            _iconMap = ItemPalette.getIconMap(type, family);
            _iconMap = ((SignalHeadItemPanel)_parent).getFilteredIconMap(_iconMap);
        }
        if (_iconMap==null) {
            _iconMap = ItemPanel.makeNewIconMap(type);
            _family = null;
            _familyName.setText("");
        }
        _iconPanel = makeIconPanel(_iconMap);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadIconDialog.class.getName());
}
