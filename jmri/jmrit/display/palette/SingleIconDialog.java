// SingleIconDialog.java
package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Plain icons have a single family but like MultiSensorIcons,
 * icons can be added and deleted from a family
 * @author Pete Cressman  Copyright (c) 2010
 */

public class SingleIconDialog extends IconDialog {

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public SingleIconDialog(String type, String family, ItemPanel parent) {
        super(type, family, parent);
    }

    /**
    * _familyName is used for icon name.
    */
    protected void init() {
        _familyName.setEditable(true);
        _familyName.setText("");
        super.sizeLocate();
    }

    protected JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        makeAddIconButtonPanel(buttonPanel, "ToolTipAddIcon", "ToolTipDeleteIcon");
        makeDoneButtonPanel(buttonPanel);
        return buttonPanel;
    }

    /**
    * add/delete icon. For Multisensor, it adds another sensor position.
    */
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JButton addIcon = new JButton(ItemPalette.rbp.getString("addIcon"));
        addIcon.addActionListener(new ActionListener() {
                IconDialog dialog;
                public void actionPerformed(ActionEvent a) {
                    if (addNewIcon(_familyName.getText())) {
                        if (doDoneAction()) {
                            dialog.dispose();
                        }
                    }
                }
                ActionListener init(IconDialog d) {
                    dialog = d;
                    return this;
                }
        }.init(this));
        addIcon.setToolTipText(ItemPalette.rbp.getString(addTip));
        panel2.add(addIcon);

        JButton deleteIcon = new JButton(ItemPalette.rbp.getString("deleteIcon"));
        deleteIcon.addActionListener(new ActionListener() {
                IconDialog dialog;
                public void actionPerformed(ActionEvent a) {
                    if (deleteIcon()) {
                        if (doDoneAction()) {
                            dialog.dispose();
                        }
                    }
                }
                ActionListener init(IconDialog d) {
                    dialog = d;
                    return this;
                }
        }.init(this));
        deleteIcon.setToolTipText(ItemPalette.rbp.getString(deleteTip));
        panel2.add(deleteIcon);
        buttonPanel.add(panel2);
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean addNewIcon(String name) {
        if (log.isDebugEnabled()) log.debug("addNewIcon Action: iconMap.size()= "+_iconMap.size());
        if (name==null || name.length()==0) {
            JOptionPane.showMessageDialog(_parent._paletteFrame, ItemPalette.rbp.getString("NoIconName"),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (_iconMap.get(name)!=null) {
            JOptionPane.showMessageDialog(_parent._paletteFrame,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateIconName"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String fileName = "resources/icons/misc/X-red.gif";
        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
//        Hashtable<String, Hashtable<String, NamedIcon>> i = getFamilyMaps(_type);
        _iconMap.put(name, icon);
        return true;
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean deleteIcon() {
        if (log.isDebugEnabled()) log.debug("deleteNewIcon Action: iconMap.size()= "+_iconMap.size());
        String name = _familyName.getText();
        if (_iconMap.remove(name)==null) {
            JOptionPane.showMessageDialog(_parent._paletteFrame,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("IconNotFound"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
        
    /**
    * Action item for makeDoneButtonPanel
    */
    protected boolean doDoneAction() {
        //check text
        String family = _familyName.getText();
        if (addFamily(family, _iconMap)) {
            ImageIndexEditor.indexChanged(true);
            _parent.removeAll();
            _parent.init();
            return true;
        }
        return false;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleIconDialog.class.getName());
}

