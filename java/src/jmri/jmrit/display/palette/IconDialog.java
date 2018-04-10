package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used when FamilyItemPanel classes add, modify or delete icon
 * families.
 *
 * Note this class cannot be used with super classes of FamilyItemPanel
 * (ItemPanel etc) since there are several casts to FamilyItemPanel.
 *
 * @author Pete Cressman Copyright (c) 2010, 2011
 */
public class IconDialog extends ItemDialog {

    protected String _family;
    protected HashMap<String, NamedIcon> _iconMap;
    protected ImagePanel _iconEditPanel;
    protected CatalogPanel _catalog;
    private final JLabel _nameLabel;

    /**
     * Constructor for an existing family to change icons, add/delete icons, or to
     * delete the family entirely.
     * @param type itemType
     * @param family icon family name
     * @param parent the ItemPanel calling this class
     * @param iconMap the map of icons in the family
     */
    public IconDialog(String type, String family, FamilyItemPanel parent, HashMap<String, NamedIcon> iconMap) {
        super(type, Bundle.getMessage("ShowIconsTitle", family), parent);
        if (log.isDebugEnabled()) {
            log.debug("IconDialog ctor: for {}, family = {}", type, family);
        }
        _family = family;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));

        JPanel p = new JPanel();
        _nameLabel = new JLabel(Bundle.getMessage("FamilyName", family));
        p.add(_nameLabel);
        panel.add(p);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        makeDoneButtonPanel(buttonPanel, iconMap);
        // null method for all except multisensor.
        makeAddIconButtonPanel(buttonPanel, "ToolTipAddPosition", "ToolTipDeletePosition");

        if (!(type.equals("IndicatorTO") || type.equals("MultiSensor") || type.equals("SignalHead"))) {
            ItemPanel.checkIconMap(type, _iconMap);
        }
        _iconEditPanel = new ImagePanel();
        makeIconPanel(_iconMap, _iconEditPanel);
        panel.add(_iconEditPanel); // put icons above buttons
        panel.add(buttonPanel);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(panel);
        _catalog = makeCatalog();
        p.add(_catalog);

        JScrollPane sp = new JScrollPane(p);
        setContentPane(sp);
        setLocationRelativeTo(_parent);
        setVisible(true);
        pack();
    }
    
    private CatalogPanel makeCatalog() {
        CatalogPanel catalog = CatalogPanel.makeDefaultCatalog(false, false, true);
        catalog.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        ImagePanel panel = catalog.getPreviewPanel();
        if (!_parent.isUpdate()) {
            panel.setImage(_parent._backgrounds[_parent.getParentFrame().getPreviewBg()]);
        } else {
            panel.setImage(_parent._backgrounds[0]);   //update always should be the panel background
        }
        return catalog;
    }

    // for _parent to update background of icon editing el
    protected ImagePanel getIconEditPanel() {
        return _iconEditPanel;
    }

    // for _parent to update background of icon editing el
    protected ImagePanel getCatalogPreviewPanel() {
        return _catalog.getPreviewPanel();
    }

    // Only multiSensor adds and deletes icons 
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
    }

    /**
     * Action for both create new family and change existing family.
     * @return true if success
     */
    protected boolean doDoneAction() {
        _parent.reset();
        _parent.setIconMap(_iconMap);
        if (log.isDebugEnabled()) {
            log.debug("doDoneAction: iconMap size= {} for {} \"{}\"", _iconMap.size(), _type, _family);            
        }
        if (!_parent.isUpdate()) {  // don't touch palette's maps. just modify individual device icons
            ItemPalette.removeIconMap(_type, _family);
            return _parent.addFamily(_type, _family, _iconMap);
        } else if (!_parent.isUnstoredMap()) {
            JOptionPane.showMessageDialog(_parent._paletteFrame,
                    Bundle.getMessage("DuplicateFamilyName", _family, _type),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else {
            _parent.updateFamiliesPanel();
        }
        return true;
    }

    /**
     * Action item to rename an icon family.
     */
    protected void renameFamily() {
        String family = JOptionPane.showInputDialog(_parent, Bundle.getMessage("EnterFamilyName"),
                Bundle.getMessage("renameFamily"), JOptionPane.QUESTION_MESSAGE);
        family = _parent.getValidFamilyName(family);
        if (!_parent.isUpdate()) {
            if (family != null && family.trim().length() > 0) {
                ItemPalette.removeIconMap(_type, _family);
                _family = family;
                _parent.addFamily(_type, _family, _iconMap);
            }
        } else {
            if (family == null || family.trim().length() == 0) {
                _family = Bundle.getMessage("unNamed");
            } else {
                _family = family;
            }
            _parent.setFamily(_family);
            _parent.updateFamiliesPanel();
        }
        _nameLabel.setText(Bundle.getMessage("FamilyName", _family));
        invalidate();
        repaint();
    }
    
    protected void makeDoneButtonPanel(JPanel buttonPanel, HashMap<String, NamedIcon> iconMap) {
        if (iconMap != null) {
            _iconMap = IconDialog.clone(iconMap);
            makeDoneButtonPanel(buttonPanel, "ButtonDone");
        } else {
            _iconMap = ItemPanel.makeNewIconMap(_type);
            makeDoneButtonPanel(buttonPanel, "addNewFamily");
        }        
    }

    protected void makeDoneButtonPanel(JPanel buttonPanel, String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton doneButton = new JButton(Bundle.getMessage(text));
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (doDoneAction()) {
                    dispose();
                }
            }
        });
        panel.add(doneButton);

        JButton renameButton = new JButton(Bundle.getMessage("renameFamily"));
        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                renameFamily();
            }
        });
        panel.add(renameButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                dispose();
            }
        });
        panel.add(cancelButton);
        buttonPanel.add(panel);
    }

    protected void makeIconPanel(HashMap<String, NamedIcon> iconMap, ImagePanel iconPanel) {
        iconPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("PreviewBorderTitle")));
        if (!_parent.isUpdate()) {
            iconPanel.setImage(_parent._backgrounds[_parent.getParentFrame().getPreviewBg()]);
        } else {
            iconPanel.setImage(_parent._backgrounds[0]);   //update always should be the panel background
        }
        log.debug("iconMap size = {}", _iconMap.size());
        _parent.addIconsToPanel(iconMap, iconPanel, true);
    }

    static protected HashMap<String, NamedIcon> clone(HashMap<String, NamedIcon> map) {
        HashMap<String, NamedIcon> clone = null;
        if (map != null) {
            clone = new HashMap<>();
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        return clone;
    }

    private final static Logger log = LoggerFactory.getLogger(IconDialog.class);
}
