// SingleIconDialog.java
package jmri.jmrit.display.palette;

import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import jmri.jmrit.catalog.NamedIcon;

/**
 * Plain icons have a single family but like MultiSensorIcons,
 * icons can be added and deleted from a family
 * @author Pete Cressman  Copyright (c) 2010
 */

public class SingleIconDialog extends MultiSensorIconDialog {

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
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
    * Action item for makeAddIconButtonPanel needs a name for icon
    */
    protected String getIconName() {
        return _familyName.getText();
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
//        getContentPane().remove(_iconPanel);
//        updateFamiliesPanel();
        return true;
    }
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleIconDialog.class.getName());
}

