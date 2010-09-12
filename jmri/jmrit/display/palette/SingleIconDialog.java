// SingleIconDialog.java
package jmri.jmrit.display.palette;

import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import jmri.jmrit.catalog.NamedIcon;

/**
 * @author Pete Cressman  Copyright (c) 2010
 */

public class SingleIconDialog extends IconDialog {

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    * @param  family
    * @param 
    */
    public SingleIconDialog(String type, String family, ItemPanel parent) {
        super(type, family, parent); 
    }

    /**
    * Constructor for creating a new family
    */
    public SingleIconDialog(String type, Hashtable <String, NamedIcon> newMap, ItemPanel parent) {
        super(type, newMap, parent); 
    }

    protected JPanel makeButtonPanel() {
        _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.Y_AXIS));
        makeAddIconButtonPanel(_buttonPanel, "ToolTipAddIcon", "ToolTipDeleteIcon");
        makeDoneButtonPanel(_buttonPanel);
        return _buttonPanel;
    }

    /**
    * Top panel of both the edit dialog and the create dialog.  Has a text field for a name.
    * @param editable - can text field be edited.
    * Override to change caption
    */
    protected JPanel makeBannerPanel(boolean editable, String caption) {
        JPanel panel = super.makeBannerPanel(true, "IconName");
        _familyName.setText("?");
        return panel;
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean addNewIcon() {
        if (log.isDebugEnabled()) log.debug("addNewIcon Action: iconMap.size()= "+_iconMap.size());
        String name = _familyName.getText();
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
        updateFamiliesPanel();
        return true;
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean deleteIcon() {
        if (log.isDebugEnabled()) log.debug("deleteNewIcon Action: iconMap.size()= "+_iconMap.size());
        String name = _familyName.getText();
        if (_iconMap.remove(name)==null) {
            JOptionPane.showMessageDialog(_parent.getPaletteFrame(),
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("IconNotFound"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        getContentPane().remove(_iconPanel);
        updateFamiliesPanel();
        return true;
    }
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleIconDialog.class.getName());
}

