package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
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
 * @author Pete Cressman Copyright (c) 2010, 2011, 2020
 */
public class IconDialog extends ItemDialog {

    protected String _family;
    protected HashMap<String, NamedIcon> _iconMap;
    protected ImagePanel _iconEditPanel;
    protected CatalogPanel _catalog;
    protected final JLabel _nameLabel;

    /**
     * Constructor for an existing family to change icons, add/delete icons, or to
     * delete the family entirely.
     * @param type itemType
     * @param family icon family name
     * @param parent the ItemPanel calling this class
     */
    public IconDialog(String type, String family, FamilyItemPanel parent) {
        super(type, Bundle.getMessage("ShowIconsTitle", family), parent);
        _family = family;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));

        JPanel p = new JPanel();
        _nameLabel = new JLabel(Bundle.getMessage("FamilyName", family));
        p.add(_nameLabel);
        panel.add(p);

        _iconEditPanel = new ImagePanel();
        _iconEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("PreviewBorderTitle")));
        if (!_parent.isUpdate()) {
            _iconEditPanel.setImage(_parent._frame.getPreviewBackground());
        } else {
            _iconEditPanel.setImage(_parent._frame.getBackground(0));   //update always should be the panel background
        }
        panel.add(_iconEditPanel); // put icons above buttons

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        makeDoneButtonPanel(buttonPanel, "ButtonDone");
        panel.add(buttonPanel);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(panel);
        _catalog = makeCatalog();
        p.add(_catalog);

        JScrollPane sp = new JScrollPane(p);
        setContentPane(sp);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });
    }

    protected void setMap(HashMap<String, NamedIcon> iconMap) {
        if (iconMap != null) {
            _iconMap = IconDialog.clone(iconMap);
        } else {
            _iconMap = _parent.makeNewIconMap(_type);
        }        
        if (!(_type.equals("MultiSensor") || _type.equals("SignalHead"))) {
            ItemPanel.checkIconMap(_type, _iconMap);
        }
        _parent.addIconsToPanel(_iconMap, _iconEditPanel, true);
        setLocationRelativeTo(_parent);
        setVisible(true);
        pack();
        log.debug("setMap: initialization done.");
    }

    private CatalogPanel makeCatalog() {
        CatalogPanel catalog = CatalogPanel.makeDefaultCatalog(false, false, true);
        catalog.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        ImagePanel panel = catalog.getPreviewPanel();
        if (!_parent.isUpdate()) {
            panel.setImage(_parent._frame.getPreviewBackground());
        } else {
            panel.setImage(_parent._frame.getBackground(0));   //update always should be the panel background
        }
        return catalog;
    }

    // for _parent to update when background is changed
    protected ImagePanel getIconEditPanel() {
        return _iconEditPanel;
    }

    // for _parent to update when background is changed
    protected ImagePanel getCatalogPreviewPanel() {
        return _catalog.getPreviewPanel();
    }

    /**
     * Action for both create new family and change existing family.
     * @return true if success
     */
    protected boolean doDoneAction() {
        if (log.isDebugEnabled()) {
            log.debug("doDoneAction: {} for {} family= {}", (_parent._update?"Update":""), _type, _family);
        }
        if (_family == null || _family.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("NoFamilyName"),
                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_type);
        String family;
        HashMap<String, NamedIcon> catalogSet = families.get(_family);
        boolean nameUsed;
        if (catalogSet == null) {
            family = _parent.findFamilyOfMap(null, _iconMap, families);
            nameUsed = false;
        } else {
            family = _parent.findFamilyOfMap(_family, _iconMap, families);
            nameUsed = true;        // the map is found under another name than _family
        }
        if (family != null ) {  // "new" map is stored
            boolean sameMap = _parent.mapsAreEqual(_iconMap, families.get(family));
            if (!mapInCatalogOK(sameMap, nameUsed, _family, family)) {
                return false;
            }
        } else {
            boolean sameMap;
            if (catalogSet == null) {
                sameMap = false;
            } else {
                sameMap = _parent.mapsAreEqual(catalogSet, _iconMap);
            }
            if (!mapNotInCatalogOK(sameMap, nameUsed, _family)) {
                return false;
            }
        }
        _parent.dialogDoneAction(_family, _iconMap);
        return true;
    }

    /**
     * 
     * @param sameMap   Map edited in dialog is the same as map found in catalog
     * @param nameUsed  Name as edited in dialog is the same as name found in catalog
     * @param editFamily Map name as edited in this dialog
     * @param catalogFamily Map name as found in the catalog
     * @return false if not OK
     */
    protected boolean mapInCatalogOK(boolean sameMap, boolean nameUsed, String editFamily, String catalogFamily) {
        log.debug("doDoneAction: map of {} in storage with name= {}", editFamily, catalogFamily);
        if (_parent._update) {
            if (!catalogFamily.equals(editFamily)) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("CannotChangeName", editFamily, catalogFamily),
                        Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        } else {
            log.debug("doDoneAction: name {} {} used and map {} same as {}",
                    editFamily, (nameUsed?"is":"NOT"), (sameMap?"":"NOT"), catalogFamily);
            if (catalogFamily.equals(editFamily)) {
                if (!sameMap) {
                    JOptionPane.showMessageDialog(this, 
                            Bundle.getMessage("DuplicateFamilyName", editFamily, _type, Bundle.getMessage("UseAnotherName")),
                            Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }             
            } else {
                if (sameMap) {
                    String oldFamily = _parent.getFamilyName(); // if oldFamily != null, this is an edit, not new set
                    if (oldFamily != null) {    // editing an catalog set
                        if (nameUsed) {
                            JOptionPane.showMessageDialog(this, Bundle.getMessage("SameNameSet", editFamily, catalogFamily),
                                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                            return false;
                        }
                    } else {
                        if (!nameUsed) {
                            JOptionPane.showMessageDialog(this, 
                                    Bundle.getMessage("DuplicateFamilyName", editFamily,
                                            _type, Bundle.getMessage("CannotUseName", catalogFamily)),
                                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                            return false;
                        }
                    }
                }
           }
        }
        return true;
    }

    /**
     * Edited map is not in the catalog.
     * 
     * @param sameMap  Map edited in dialog is the same as map currently held in parent item panel
     * @param nameUsed Name as edited in dialog is the same as a name found in catalog
     * @param editFamily Map name as edited in this dialog
     * @return false if not OK
     */
    protected boolean mapNotInCatalogOK(boolean sameMap, boolean nameUsed, String editFamily) {
        String oldFamily = _parent.getFamilyName();
        if (_parent._update) {
            if (nameUsed) {    // name is a key to stored map
                log.debug("{} keys a stored map. name is used", editFamily); 
                JOptionPane.showMessageDialog(this, Bundle.getMessage("NeedDifferentName", editFamily),
                        Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        } else {
            if (oldFamily != null) {    // editing an catalog set from parent
                log.debug("Editing set {}. {} {} a stored map.", oldFamily, editFamily, (nameUsed?"is":"NOT")); 
                if (nameUsed) { // map in catalog under another name
                    if (!editFamily.equals(oldFamily)) { // named changed
                        if (!sameMap) { // also map changed
                            JOptionPane.showMessageDialog(this, Bundle.getMessage("badReplaceIconSet", oldFamily, editFamily),
                                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                            return false;
                        }
                    }
                } else {
                    int result = JOptionPane.showOptionDialog(this, Bundle.getMessage("ReplaceFamily", oldFamily, editFamily),
                            Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_CANCEL_OPTION, 
                            JOptionPane.QUESTION_MESSAGE, null,
                            new Object[] {oldFamily, editFamily, Bundle.getMessage("ButtonCancel")},
                            Bundle.getMessage("ButtonCancel"));
                    if (result == JOptionPane.YES_OPTION) {
                        _family = oldFamily;
                    } else if (result == JOptionPane.CANCEL_OPTION) {
                        return true;
                    } else if (result == JOptionPane.CLOSED_OPTION) {
                        return true;
                    }
                }
            } else {
                if (nameUsed) { // map in catalog under another name
                    JOptionPane.showMessageDialog(this, 
                            Bundle.getMessage("DuplicateFamilyName", editFamily, _type, Bundle.getMessage("UseAnotherName")),
                            Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Action item to rename an icon family.
     */
    protected void renameFamily() {
        String family = _parent.getValidFamilyName(null, _iconMap);
        if (family != null) {
            _family = family;
            _nameLabel.setText(Bundle.getMessage("FamilyName", _family));
            invalidate();
        }
    }
    
    protected void makeDoneButtonPanel(JPanel buttonPanel, String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton doneButton = new JButton(Bundle.getMessage(text));
        doneButton.addActionListener(a -> {
            if (doDoneAction()) {
                dispose();
            }
        });
        panel.add(doneButton);

        JButton renameButton = new JButton(Bundle.getMessage("renameFamily"));
        renameButton.addActionListener(a -> renameFamily());
        panel.add(renameButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(a -> cancel());
        panel.add(cancelButton);
        buttonPanel.add(panel);
    }

    protected void cancel() {
        _parent.setFamily();
        _parent._cntlDown = false;
        super.dispose();
    }
    static protected HashMap<String, NamedIcon> clone(HashMap<String, NamedIcon> map) {
        HashMap<String, NamedIcon> clone = null;
        if (map != null) {
            clone = new HashMap<>();
            for (Entry<String, NamedIcon> entry : map.entrySet()) {
                clone.put(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        return clone;
    }

    private final static Logger log = LoggerFactory.getLogger(IconDialog.class);
}
