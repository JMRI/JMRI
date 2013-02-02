
// IndicatorTOIconDialog.java
package jmri.jmrit.display.palette;

import org.apache.log4j.Logger;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrit.catalog.NamedIcon;

/**
 *
 * @author Pete Cressman  Copyright (c) 2010
 */

public class IndicatorTOIconDialog extends IconDialog {
    
    String _key;

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public IndicatorTOIconDialog(String type, String family, IndicatorTOItemPanel parent, String key, 
    				Hashtable <String, NamedIcon> iconMap) {
        super(type, family, parent, iconMap);
        _key = key;
        _familyName.setText(_key);
        sizeLocate();
        log.debug("IndicatorTOIconDialog ctor done. type= \""+type+"\", family= \""+
                                        family+"\", key= \""+key+"\"");
    }

    /* override IconDialog initMap. Make place holder for _iconPanel
    protected void initMap(String type, String key) {
        _iconPanel = new JPanel(); 
    }*/

    /**
    * Add/Delete icon family for types that may have more than 1 family
    */
    protected void makeAddSetButtonPanel(JPanel buttonPanel) {
        super.makeAddSetButtonPanel(buttonPanel);
        _addFamilyButton.setText(ItemPalette.rbp.getString("addMissingStatus"));
        _addFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipMissingStatus"));
        _deleteButton.setText(ItemPalette.rbp.getString("deleteStatus"));
        _deleteButton.setToolTipText(ItemPalette.rbp.getString("ToolTipDeleteStatus"));
    }

    /**
    * NOT add a new family.  Create a status family when previous status was deleted
    */
    protected void createNewFamily() {
        log.debug("createNewFamily: type= \""+_type+"\", family= \""+_family+"\" key= "+_key);
        //check text        
        Hashtable<String, NamedIcon> iconMap = ItemPanel.makeNewIconMap("Turnout");
        String key = _familyName.getText();
        ItemPalette.addLevel4FamilyMap(_type, _parent._family, key, iconMap);
        dispose();
    }

    /**
    * Action item for add new status set in makeAddSetButtonPanel
    */
    protected void addFamilySet() {
        log.debug("addFamilySet: type= \""+_type+"\", family= \""+_family+"\" key= "+_key);
        setVisible(false);
        IndicatorTOItemPanel parent = (IndicatorTOItemPanel)_parent;
        if (parent._iconGroupsMap.size() < IndicatorTOItemPanel.STATUS_KEYS.length) {
            setVisible(false);
            new IndicatorTOIconDialog(_type, null, parent, _key, _iconMap);
        } else {
            JOptionPane.showMessageDialog(_parent._paletteFrame, 
                    ItemPalette.rbp.getString("AllStatus"), 
                    ItemPalette.rbp.getString("infoTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
    * Action item for add delete status set in makeAddSetButtonPanel
    */
    protected void deleteFamilySet() {
        ItemPalette.removeLevel4IconMap(_type, _parent._family, _familyName.getText());
        _family = null;
        _parent.updateFamiliesPanel();
    }

    /**
    * Action item for makeDoneButtonPanel
    */
    protected boolean doDoneAction() {
        //check text
        String subFamily = _familyName.getText();  // actually the key to status icon
        if (_family!=null && _family.equals(subFamily)) {
            ItemPalette.removeLevel4IconMap(_type, _parent._family, subFamily);
        }
        jmri.jmrit.catalog.ImageIndexEditor.indexChanged(true);
        return addFamily(_parent._family, _iconMap, subFamily);
    }

    protected boolean addFamily(String family, Hashtable<String, NamedIcon> iconMap, String subFamily) {
        log.debug("addFamily _type= \""+_type+"\", family= \""+family+"\""+", key= \""+
                  _familyName.getText()+"\", _iconMap.size= "+_iconMap.size());
        IndicatorTOItemPanel parent = (IndicatorTOItemPanel)_parent;
        parent.updateIconGroupsMap(subFamily, _iconMap);
        _parent.updateFamiliesPanel();
        _parent._family = family;
        return true;
    }

    static Logger log = Logger.getLogger(IndicatorTOIconDialog.class.getName());
}
