
// IndicatorTOIconDialog.java
package jmri.jmrit.display.palette;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
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
        log.debug("ctor type= \""+type+"\", family= \""+family+"\", key= \""+key+"\"");
        _key = key;
        if (_family!=null) {
            _familyName.setEditable(false);
            _iconMap = clone(parent._iconGroupsMap.get(key));
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
        java.awt.Container comp = getContentPane();
        while (!(comp instanceof JPanel)) {
            comp = (java.awt.Container)comp.getComponent(0);
        }
        ((JPanel)comp).remove(1);
        _iconPanel = makeIconPanel(_iconMap);
        _iconPanel.setVisible(true);
        ((JPanel)comp).add(_iconPanel, 1);
        sizeLocate();
        log.debug("IndicatorTOIconDialog ctor done. type= \""+type+"\", family= \""+
                                        family+"\", key= \""+key+"\"");
    }

    // override IconDialog initMap. Make placeholder for _iconPanel
    protected void initMap(String type, String key) {
        _iconPanel = new JPanel(); 
    }

    private Hashtable<String, NamedIcon> clone(Hashtable<String, NamedIcon> map) {
        Hashtable<String, NamedIcon> clone = new Hashtable<String, NamedIcon>();
        if (map!=null) {
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        return clone;
    }

    /**
    * Add/Delete icon family for types that may have more than 1 fammily
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
    //    IndicatorTOItemPanel parent = (IndicatorTOItemPanel)_parent;
   //     Iterator <String> iter = ItemPalette.getFamilyMaps(_type).keySet().iterator();
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
//        if (ItemPalette.addLevel4Family(_parent._paletteFrame, _type, family, subFamily, _iconMap)) {
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTOIconDialog.class.getName());
}
