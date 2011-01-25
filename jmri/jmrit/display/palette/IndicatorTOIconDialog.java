
// IndicatorTOIconDialog.java
package jmri.jmrit.display.palette;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
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
    public IndicatorTOIconDialog(String type, String family, IndicatorTOItemPanel parent, String key) {
        super(type, family, parent);
        log.debug("ctor type= \""+type+"\", family= \""+
                  family+"\", key= \""+key+"\"");
        _key = key;
        if (_family!=null) {
            _familyName.setEditable(false);
            _iconMap = parent._iconGroupsMap.get(key);
        } else {
            ArrayList<String> keys = new ArrayList<String>(); 
            for (int i=0; i<IndicatorTOItemPanel.STATUS_KEYS.length; i++) {
                keys.add(IndicatorTOItemPanel.STATUS_KEYS[i]);
            }
            Enumeration<String> currentKeys = parent._iconGroupsMap.keys();
            while (currentKeys.hasMoreElements()) {
                String k= currentKeys.nextElement();
                keys.remove(k);
                log.debug("key= \""+k+"\"");
            }
            if (keys.size()>0) {
                _iconMap = ItemPanel.makeNewIconMap("Turnout");
                _key = keys.get(keys.size()-1);
            } else {
                log.error("Item type \""+type+"\" has null indicator family for key= "+key);
            }
        }
        _familyName.setText(_key);
        _iconPanel = makeIconPanel(_iconMap); 
        getContentPane().remove(1);
        getContentPane().add(_iconPanel, 1);
        sizeLocate();
        log.debug("IndicatorTOIconDialog ctor done. type= \""+type+"\", family= \""+
                                        family+"\", key= \""+key+"\"");
    }

    // override IconDialog initMap. Make placeholder for _iconPanel
    protected JPanel initMap(String type, String family) {
        return new JPanel(); 
    }

    /**
    * Add/Delete icon family for types that may have more than 1 fammily
    */
    protected void makeAddSetButtonPanel(JPanel buttonPanel) {
        super.makeAddSetButtonPanel(buttonPanel);
        _addFamilyButton.setText(ItemPalette.rbp.getString("addMissingStatus"));
        _addFamilyButton.setText(ItemPalette.rbp.getString("ToolTipMissingStatus"));
        _deleteButton.setText(ItemPalette.rbp.getString("deleteStatus"));
        _deleteButton.setText(ItemPalette.rbp.getString("ToolTipDeleteStatus"));
    }

    /**
    * Action item for add new status set in makeAddSetButtonPanel
    */
    protected void addFamilySet() {
        IndicatorTOItemPanel parent = (IndicatorTOItemPanel)_parent;
        if (parent._iconGroupsMap.size() < IndicatorTOItemPanel.STATUS_KEYS.length) {
            setVisible(false);
            new IndicatorTOIconDialog(_type, null, parent, _key);
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
        return addFamily(_parent._family, _iconMap, subFamily);
    }


    protected boolean addFamily(String family, Hashtable<String, NamedIcon> iconMap, String subFamily) {
        log.debug("addFamily _type= \""+_type+"\", family= \""+family+"\""+", key= \""+
                  _familyName.getText()+"\", _iconMap.size= "+_iconMap.size());
        if (ItemPalette.addLevel4Family(_parent._paletteFrame, _type, family, subFamily, _iconMap)) {
            _parent.updateFamiliesPanel();
            _parent._family = family;
            _parent.reset();
            return true;
        }
        return false;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTOIconDialog.class.getName());
}
