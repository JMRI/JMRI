
// SignalHeadIconDialog(.java
package jmri.jmrit.display.palette;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;

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

    protected JPanel initMap(String type, String family) {
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
        return makeIconPanel(_iconMap);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadIconDialog.class.getName());
}
