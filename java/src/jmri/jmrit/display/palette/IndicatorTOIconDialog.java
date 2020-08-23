package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class IndicatorTOIconDialog extends IconDialog {

    private HashMap<String, HashMap<String, NamedIcon>> _iconGroupsMap;

    public IndicatorTOIconDialog(String type, String family, IndicatorTOItemPanel parent) {
        super(type, family, parent); // temporarily use key for family to set JL
    }

    protected void setMaps(HashMap<String, HashMap<String, NamedIcon>> iconMaps) {
        IndicatorTOItemPanel p = (IndicatorTOItemPanel)_parent;
        if (iconMaps != null) {
            _iconGroupsMap = new HashMap<>();
            for (Entry<String, HashMap<String, NamedIcon>> entry : iconMaps.entrySet()) {
                _iconGroupsMap.put(entry.getKey(), IconDialog.clone(entry.getValue()));
            }
        } else {
            _iconGroupsMap = p.makeNewIconMap();
        }
        p.addIcons2Panel(_iconGroupsMap, _iconEditPanel, true);
        setLocationRelativeTo(p);
        setVisible(true);
        pack();
        log.debug("setMaps: initialization done.");
    }

    /**
     * Add/Delete icon family for types that may have more than 1 family.
     */
    @Override
    protected void makeDoneButtonPanel(JPanel buttonPanel, String text) {
        super.makeDoneButtonPanel(buttonPanel, text);
        IndicatorTOItemPanel p = (IndicatorTOItemPanel)_parent;
        if (!p._cntlDown) {
            return;
        }
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        JButton button = new JButton(Bundle.getMessage("addMissingStatus"));
        button.addActionListener(a -> {
            addMissingStatus();
        });
        button.setToolTipText(Bundle.getMessage("ToolTipMissingStatus"));
        panel1.add(button);

        button = new JButton(Bundle.getMessage("addStatus"));
        button.addActionListener(a -> {
            addStatusSet();
        });
        button.setToolTipText(Bundle.getMessage("addStatus"));
        panel1.add(button);

        button = new JButton(Bundle.getMessage("deleteStatus"));
        button.addActionListener(a -> {
            deleteStatusSet();
        });
        button.setToolTipText(Bundle.getMessage("ToolTipDeleteStatus"));
        panel1.add(button);
        
        buttonPanel.add(panel1);
    }

    /**
     * Action item to rename an icon family.
     */@Override
    protected void renameFamily() {
         IndicatorTOItemPanel p = (IndicatorTOItemPanel)_parent;
        String family = p.getValidFamily(null, _iconGroupsMap);
        if (family != null) {
            _family = family;
            _nameLabel.setText(Bundle.getMessage("FamilyName", _family));
            invalidate();
        }
    }
    
     /**
      * Action item for makeDoneButtonPanel.
      */
     @Override
     protected boolean doDoneAction() {
         if (log.isDebugEnabled()) {
             log.debug("doDoneAction: {} for {} family= {}", (_parent._update?"Update":""), _type, _family);
         }
         if (_family == null || _family.isEmpty()) {
             JOptionPane.showMessageDialog(this, Bundle.getMessage("NoFamilyName"),
                     Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
             return false;
         }
         IndicatorTOItemPanel p = (IndicatorTOItemPanel)_parent;
         HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> families =
                                         ItemPalette.getLevel4FamilyMaps(_type);
         String family;
         HashMap<String, HashMap<String, NamedIcon>> catalogSet = families.get(_family);
         boolean nameUsed;
         if (catalogSet == null) {
             family = p.findFamilyOfMaps(null, _iconGroupsMap, families);
             nameUsed = false;
         } else {
             family = p.findFamilyOfMaps(_family, _iconGroupsMap, families);
             nameUsed = true;        // the map is found under another name than _family
         }
         if (family != null ) {  // "new" map is stored
             boolean sameMap = p.familiesAreEqual(_iconGroupsMap, families.get(family));
             if (!mapInCatalogOK(sameMap, nameUsed, _family, family)) {
                 return false;
             }
         } else {
             boolean sameMap;
             if (catalogSet == null) {
                 sameMap = false;
             } else {
                 sameMap = p.familiesAreEqual(catalogSet, _iconGroupsMap);
             }
             if (!mapNotInCatalogOK(sameMap, nameUsed, _family)) {
                 return false;
             }
         }
         p.dialogDone(_family, _iconGroupsMap);
         return true;
     }

     /// CNTL A operations - So far no need to use these. - artifact of previous expansion plans
     /// for 4.21 CNTL A added to simplify conventional GUI 
    /**
     * Action item for restoring status set in makeAddIconButtonPanel.
     */
    private void addMissingStatus() {
        Set<String> set = _iconGroupsMap.keySet();
        HashMap<String, String> options = new HashMap<>();
        for (String status : ItemPanel.INDICATOR_TRACK) {
            if (!set.contains(status)) {
                options.put(ItemPalette.convertText(status), status);
            }
        }
        if (!options.isEmpty()) {
            Object[] selections = options.keySet().toArray();
            String key = (String) JOptionPane.showInputDialog(this,
                    Bundle.getMessage("PickStatus"), Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE, null,
                    selections, selections[0]);
            if (key != null) {
                addStatus(options.get(key));
            }
        } else {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("AllStatus"),
                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * NOT add a new family. Create a status family
     */
    private void addStatusSet() {
        String status = JOptionPane.showInputDialog(this, Bundle.getMessage("StatusName"),
                Bundle.getMessage("createNewFamily"), JOptionPane.QUESTION_MESSAGE);
        if (status != null) {
            addStatus(status);
        }
    }

    /**
     * Action item for delete status set in makeAddIconButtonPanel.
     */
    private void deleteStatusSet() {
        Set<String> set = _iconGroupsMap.keySet();
        HashMap<String, String> options = new HashMap<>();
        for (String status : set) {
            options.put(ItemPalette.convertText(status), status);
        }
        Object[] selections = options.keySet().toArray();
        String key = (String) JOptionPane.showInputDialog(this,
                Bundle.getMessage("PickDelete"), Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE, null,
                selections, selections[0]);
        if (key != null) {
            _iconGroupsMap.remove(options.get(key));
            IndicatorTOItemPanel p = (IndicatorTOItemPanel)_parent;
            if (_iconGroupsMap.isEmpty()) {
                p.deleteFamilySet();
                dispose();
            }
            p.addIcons2Panel(_iconGroupsMap, _iconEditPanel, true);
            _iconEditPanel.invalidate();
            pack();
        }
    }

    private void addStatus(String status) {
        HashMap<String, NamedIcon> iconMap = _parent.makeNewIconMap("Turnout");
        _iconGroupsMap.put(status, iconMap);
        IndicatorTOItemPanel p = (IndicatorTOItemPanel)_parent;
        p.addIcons2Panel(_iconGroupsMap, _iconEditPanel, true);
        _iconEditPanel.invalidate();
        pack();
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTOIconDialog.class);

}
