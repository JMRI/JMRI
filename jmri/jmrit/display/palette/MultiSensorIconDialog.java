// MultiSensorIconDialog.java
package jmri.jmrit.display.palette;

import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.catalog.NamedIcon;

/**
 * @author Pete Cressman  Copyright (c) 2010
 */

public class MultiSensorIconDialog extends IconDialog {

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    * @param  family
    * @param 
    */
    public MultiSensorIconDialog(String type, String family, ItemPanel parent) {
        super(type, family, parent); 
    }

    /**
    * Constructor for creating a new family
    */
    public MultiSensorIconDialog(String type, Hashtable <String, NamedIcon> newMap, ItemPanel parent) {
        super(type, newMap, parent); 
    }

    protected JPanel makeButtonPanel() {
        _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.Y_AXIS));
        makeAddIconButtonPanel(_buttonPanel, "ToolTipAddPosition", "ToolTipDeletePosition");
        makeAddSetButtonPanel(_buttonPanel);
        makeDoneButtonPanel(_buttonPanel);
        return _buttonPanel;
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean addNewIcon() {
        if (log.isDebugEnabled()) log.debug("addNewIcon Action: iconMap.size()= "+_iconMap.size());
        String name = MultiSensorItemPanel.POSITION[_iconMap.size()-3];
        if (name==null || name.length()==0) {
            JOptionPane.showMessageDialog(_parent.getPaletteFrame(), ItemPalette.rbp.getString("NoIconName"),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (_iconMap.get(name)!=null) {
            JOptionPane.showMessageDialog(_parent.getPaletteFrame(),
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateIconName"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String fileName = "resources/icons/misc/X-red.gif";
        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
        _iconMap.put(name, icon);
//        getContentPane().remove(_iconPanel);
//        _iconPanel = makeIconPanel(_iconMap); 
//        getContentPane().add(_iconPanel, 1);
        return true;
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean deleteIcon() {
        if (log.isDebugEnabled()) log.debug("deleteSensor Action: iconMap.size()= "+_iconMap.size());
        if (_iconMap.size()<4) {
            return false;
        }
        String name = MultiSensorItemPanel.POSITION[_iconMap.size()-4];
        _iconMap.remove(name);
//        getContentPane().remove(_iconPanel);
//        _iconPanel = makeIconPanel(_iconMap); 
//        getContentPane().add(_iconPanel, 1);
        return true;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiSensorIconDialog.class.getName());
}

