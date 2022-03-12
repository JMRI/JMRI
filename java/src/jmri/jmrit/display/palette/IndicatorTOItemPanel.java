package jmri.jmrit.display.palette;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel for IndicatorTurnout items.
 *
 * @author Pete Cressman Copyright (c) 2010, 2020
 */
public class IndicatorTOItemPanel extends TableItemPanel<Turnout> {

    private JPanel _tablePanel;
    private HashMap<String, HashMap<String, NamedIcon>> _unstoredMaps;
    private DetectionPanel _detectPanel;
    protected HashMap<String, HashMap<String, NamedIcon>> _iconGroupsMap;

    public IndicatorTOItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<Turnout> model) {
        super(parentFrame, type, family, model);
    }

    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _detectPanel = new DetectionPanel(this);
            add(_detectPanel, 1);
        }
        hideIcons();
    }

    /**
     * CircuitBuilder init for conversion of plain track to indicator track.
     */
    @Override
    public void init(JPanel bottomPanel) {
        super.init(bottomPanel);
        add(_iconFamilyPanel, 0);
    }

    /**
     * Init for update of existing indicator turnout.
     * _bottom3Panel has "Update Panel" button put onto _bottom1Panel.
     *
     * @param doneAction doneAction
     * @param iconMaps iconMaps
     */
    public void initUpdate(ActionListener doneAction, HashMap<String, HashMap<String, NamedIcon>> iconMaps) {
        _iconGroupsMap = iconMaps;
        if (iconMaps != null) {
            checkCurrentMaps(iconMaps); // is map in families?, does user want to add it? etc.
        }
        if (_family == null || _family.isEmpty()) {
            _family = Bundle.getMessage("unNamed");
        }
        _detectPanel = new DetectionPanel(this);
        super.init(doneAction, null);
        add(_detectPanel, 1);
        add(_iconFamilyPanel, 2);
    }

    private void checkCurrentMaps(HashMap<String, HashMap<String, NamedIcon>> iconMaps) {
        String family = getValidFamily(_family, iconMaps);
        if (_isUnstoredMap) {
            _unstoredMaps = iconMaps;
            int result = JOptionPane.showConfirmDialog(_frame.getEditor(), Bundle.getMessage("UnkownFamilyName", family),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                ItemPalette.addLevel4Family(_itemType, family, iconMaps);
            }
            _family = family;
        } else {
            if (family != null) {  // icons same as a known family, maybe with another name
                if (!family.equals(_family)) {
                    log.info("{} icon's family \"{}\" found but is called \"{}\" in the Catalog.  Name changed to Catalog name.",
                            _itemType, _family, family);
                    _family = family;
                }
                return;
            }
        }

    }

    protected String getValidFamily(String family, HashMap<String, HashMap<String, NamedIcon>> iconMap) {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> families = ItemPalette.getLevel4FamilyMaps(_itemType);
        if (families == null || families.isEmpty()) {
            return null;
        }
        String mapFamily;
        if (iconMap != null) {
            mapFamily = findFamilyOfMaps(null, iconMap, families);
            if (log.isDebugEnabled()) {
                log.debug("getValidFamily: findFamilyOfMaps {} found stored family \"{}\" for family \"{}\".", _itemType, mapFamily, family);
            }
            if (mapFamily == null) {
                _isUnstoredMap = true;
            } else {
                _isUnstoredMap = false;
                if (family != null) {
                    return mapFamily;
                }
            }
        }
        mapFamily = family;
        // check that name is not duplicate.
        boolean nameOK = false;
        while (!nameOK) {
            if (mapFamily == null || mapFamily.isEmpty()) {
                Component fr;
                if (_dialog != null) fr = _dialog; else fr = this;
                mapFamily = JOptionPane.showInputDialog(fr, Bundle.getMessage("EnterFamilyName"),
                        Bundle.getMessage("createNewFamily"), JOptionPane.QUESTION_MESSAGE);
                if (mapFamily == null || mapFamily.isEmpty()) {   // user quit
                    return null;
                }
            }
            Iterator<String> iter = families.keySet().iterator();
            while (iter.hasNext()) {
                String fam = iter.next();
                log.debug("check names. fam={} family={} mapFamily={}", fam, family, mapFamily);
                if (mapFamily.equals(fam)) {   // family cannot be null
                    JOptionPane.showMessageDialog(_frame,
                            Bundle.getMessage("DuplicateFamilyName", mapFamily, _itemType, Bundle.getMessage("UseAnotherName")),
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                    mapFamily = null;
                    nameOK = false;
                    break;
                }
                nameOK = true;
            }
        }
        return mapFamily;
    }

    /**
     * Find the family name of the map in a families HashMap.
     *
     * @param exemptFamily exclude from matching
     * @param newMap iconMap
     * @param families families of itemType
     * @return null if map is not in the family
     */
    protected String findFamilyOfMaps(String exemptFamily,
                HashMap<String, HashMap<String, NamedIcon>> newMap,
                HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> families) {
        for (Entry<String, HashMap<String, HashMap<String, NamedIcon>>> entry : families.entrySet()) {
            String family = entry.getKey();
            if (!family.equals(exemptFamily)) {
                log.debug(" familyKey = {}", entry.getKey());
                HashMap<String, HashMap<String, NamedIcon>> statusMaps = entry.getValue();
                if (familiesAreEqual(newMap, statusMaps)) {
                    return family;
                }
            }
        }
        return null;
    }

    // Test if status families are equal
    protected boolean familiesAreEqual(
                HashMap<String,  HashMap<String, NamedIcon>> famOne,
                HashMap<String, HashMap<String, NamedIcon>> famTwo) {
        if (famOne.size() != famTwo.size()) {
            return false;
        }
        for (Entry<String, HashMap<String, NamedIcon>> ent : famOne.entrySet()) {
            String statusKey = ent.getKey();
            log.debug("  statusKey = {}", statusKey);
            HashMap<String, NamedIcon> map = famTwo.get(statusKey);
            if (map == null) {
                return false;
            }
            if (!mapsAreEqual(ent.getValue(), map)) {
                return false;
            }
            log.debug("  status {}'s are equal.", statusKey);
        }
        return true;
    }

    @Override
    protected boolean namesStoredMap(String family) {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> families =
                ItemPalette.getLevel4FamilyMaps(_itemType);
        if (families.keySet().contains(family)) {
            return true;
        }
        return false;
    }

    /*
     * Get a handle in order to change visibility.
     */
    @Override
    protected JPanel initTablePanel(PickListModel<Turnout> model) {
        _tablePanel = super.initTablePanel(model);
        return _tablePanel;
    }

    @Override
    public void dispose() {
        if (_detectPanel != null) {
            _detectPanel.dispose();
        }
        super.dispose();
    }

    @Override
    protected void makeFamiliesPanel() {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>
                    families = ItemPalette.getLevel4FamilyMaps(_itemType);
        boolean isEmpty = families.values().isEmpty();
        if (_bottomPanel == null) {
            makeBottomPanel(isEmpty);
        } else {
           if (isEmpty ^ _wasEmpty) {
               remove(_bottomPanel);
               makeBottomPanel(isEmpty);
           }
        }
        _wasEmpty = isEmpty;
        if (isEmpty) {
            _iconGroupsMap = _unstoredMaps;
            addIcons2Panel(_iconGroupsMap, _iconPanel, false);
            if (!_suppressDragging) {
                makeDragIconPanel();
                makeDndIcon();
            }
            addFamilyPanels(false);
         } else {
             makeFamiliesPanel(families);
         }
    }
    private void makeFamiliesPanel(@Nonnull HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> families) {

        makeFamilyButtons(families.keySet());  // makes _familyButtonPanel
        if (_iconGroupsMap == null) {
            _iconGroupsMap = families.get(_family);
            if (_iconGroupsMap == null) {
                _isUnstoredMap = true;
                _iconGroupsMap = _unstoredMaps;
            }
        }
        addIcons2Panel(_iconGroupsMap, _iconPanel, false); // need to have family maps identified before calling

        if (!_suppressDragging) {
            makeDragIconPanel();
            makeDndIcon();
        }
        addFamilyPanels(!families.isEmpty());
    }

    @Override
    protected String getDisplayKey() {
        return "TurnoutStateClosed";
    }

    /**
    * Add current family icons to Show Icons pane when _showIconsButton pressed
    * Also, dropIcon is true, call is from Icondialog and current family icons are
    * added for editing.
    * @see #hideIcons()
    *
    * @param iconMaps family maps
    * @param iconPanel panel to fill with icons
    * @param dropIcon true for ability to drop new image on icon to change icon source
    * */
    protected void addIcons2Panel(HashMap<String, HashMap<String, NamedIcon>> iconMaps, ImagePanel iconPanel, boolean dropIcon) {
        if (iconMaps == null) {
            return;
        }
        GridBagLayout gridbag = new GridBagLayout();
        iconPanel.setLayout(gridbag);
        iconPanel.removeAll();

        GridBagConstraints c = ItemPanel.itemGridBagConstraint();

        if (iconMaps.isEmpty()) {
            iconPanel.add(Box.createRigidArea(new Dimension(70,70)));
        }

        for (Entry<String, HashMap<String, NamedIcon>> stringHashMapEntry : iconMaps.entrySet()) {
            c.gridx = 0;
            c.gridy++;

            String statusName = stringHashMapEntry.getKey();
            JPanel panel = new JPanel();
            panel.add(new JLabel(ItemPalette.convertText(statusName)));
            panel.setOpaque(false);
            gridbag.setConstraints(panel, c);
            iconPanel.add(panel);
            c.gridx++;
            HashMap<String, NamedIcon> iconMap = stringHashMapEntry.getValue();
            ItemPanel.checkIconMap("Turnout", iconMap); // NOI18N
            for (Entry<String, NamedIcon> ent : iconMap.entrySet()) {
                String key = ent.getKey();
                panel = makeIconDisplayPanel(key, iconMap, dropIcon);

                gridbag.setConstraints(panel, c);
                iconPanel.add(panel);
                c.gridx++;
            }
        }
    }

    @Override
    protected void hideIcons() {
        if (_tablePanel != null) {
            _tablePanel.setVisible(true);
            _tablePanel.invalidate();
                    }
        if (_detectPanel != null) {
            _detectPanel.setVisible(true);
            _detectPanel.invalidate();
        }
        super.hideIcons();
    }

    @Override
    protected void showIcons() {
        if (_detectPanel != null) {
            _detectPanel.setVisible(false);
            _detectPanel.invalidate();
        }
        if (_tablePanel != null) {
            _tablePanel.setVisible(false);
            _tablePanel.invalidate(); // force redraw
        }
        super.showIcons();
    }

    /**
     * Action item for delete family.
     */
    @Override
    protected void deleteFamilySet() {
        if (JOptionPane.showConfirmDialog(_frame, Bundle.getMessage("confirmDelete", _family),
                Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION) {
            ItemPalette.removeLevel4IconMap(_itemType, _family, null);
            _family = null;
            _tablePanel.setVisible(true);
            updateFamiliesPanel();
            setFamily(_family);
        }
    }

    protected HashMap<String, HashMap<String, NamedIcon>> makeNewIconMap() {
        HashMap<String, HashMap<String, NamedIcon>> map = new HashMap<>();
        for (String statusKey : INDICATOR_TRACK) {
            map.put(statusKey, makeNewIconMap("Turnout")); // NOI18N
        }
        return map;
    }

    protected void makeDndIcon() {
        if (_iconGroupsMap != null) {
            makeDndIcon(_iconGroupsMap.get("ClearTrack"));
        } else {
            makeDndIcon(null);
        }
    }

    /**
     * Needed by setFamily() change _family display
     */
    @Override
    protected void setFamilyMaps() {
        _iconGroupsMap = ItemPalette.getLevel4Family(_itemType, _family);
        if (_iconGroupsMap == null) {
            _isUnstoredMap = true;
            _iconGroupsMap = _unstoredMaps;
        }
        if (!_suppressDragging) {
            makeDragIconPanel();
            makeDndIcon();
        }
        addIcons2Panel(_iconGroupsMap, _iconPanel, false);
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast follows specific Constructor")
    protected void openDialog(String type, String family) {
        closeDialogs();
        hideIcons();
        _dialog = new IndicatorTOIconDialog(type, family, this);
        IndicatorTOIconDialog d = (IndicatorTOIconDialog)_dialog;
        if (_family == null) {
            d.setMaps(null);
        } else {
            d.setMaps(_iconGroupsMap);
        }
        d.pack();
    }

    protected void dialogDone(String family, HashMap<String, HashMap<String, NamedIcon>> iconMap) {
        if (!_update && !(family.equals(_family) && familiesAreEqual(iconMap, _iconGroupsMap))) {
            ItemPalette.removeLevel4IconMap(_itemType, _family, null);
            ItemPalette.addLevel4Family(_itemType, family, iconMap);
        } else {
            _iconGroupsMap = iconMap;
            if (!namesStoredMap(family)) {
                _isUnstoredMap = true;
            }
            if (_isUnstoredMap) {
                _unstoredMaps = _iconGroupsMap;
            }
        }
        _family = family;
        makeFamiliesPanel();
        setFamily(family);
        _cntlDown = false;
        hideIcons();
        if (log.isDebugEnabled()) {
            log.debug("dialogDoneAction done for {} {}. {} unStored={}",
                    _itemType, _family, (_update?"update":""), _isUnstoredMap);
        }
    }

    /*
     * **************** pseudo inheritance ********************
     */
    public boolean getShowTrainName() {
        return _detectPanel.getShowTrainName();
    }

    public void setShowTrainName(boolean show) {
        _detectPanel.setShowTrainName(show);
    }

    public String getOccSensor() {
        return _detectPanel.getOccSensor();
    }

    public String getOccBlock() {
        return _detectPanel.getOccBlock();
    }

    public void setOccDetector(String name) {
        _detectPanel.setOccDetector(name);
    }

    public ArrayList<String> getPaths() {
        return _detectPanel.getPaths();
    }

    public void setPaths(ArrayList<String> paths) {
        _detectPanel.setPaths(paths);
    }

    public HashMap<String, HashMap<String, NamedIcon>> getIconMaps() {
        if (_iconGroupsMap != null) {
            return _iconGroupsMap;
        }
        _iconGroupsMap = ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
        if (_iconGroupsMap == null) {
            _iconGroupsMap = _unstoredMaps;
        }
        if (_iconGroupsMap == null) {
            log.warn("Family \"{}\" for type \"{}\" not found.", _family, _itemType);
            _iconGroupsMap = makeNewIconMap();
        }
        return _iconGroupsMap;
    }

    @Override
    protected JLabel getDragger(DataFlavor flavor,
            HashMap<String, NamedIcon> map, NamedIcon icon) {
        return new IconDragJLabel(flavor, icon);
    }

    protected class IconDragJLabel extends DragJLabel {

        public IconDragJLabel(DataFlavor flavor, NamedIcon icon) {
            super(flavor, icon);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return super.isDataFlavorSupported(flavor);
        }

        @Override
        protected boolean okToDrag() {
            NamedBean bean = getDeviceNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

       @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            NamedBean bean = getDeviceNamedBean();
            if (bean == null) {
                return null;
            }

            HashMap<String, HashMap<String, NamedIcon>> iconMap = getIconMaps();

            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_frame.getEditor());

                t.setOccBlock(_detectPanel.getOccBlock());
                t.setOccSensor(_detectPanel.getOccSensor());
                t.setShowTrain(_detectPanel.getShowTrainName());
                t.setTurnout(bean.getSystemName());
                t.setFamily(_family);

                for (Entry<String, HashMap<String, NamedIcon>> entry : iconMap.entrySet()) {
                    String status = entry.getKey();
                    for (Entry<String, NamedIcon> ent : entry.getValue().entrySet()) {
                        t.setIcon(status, ent.getKey(), new NamedIcon(ent.getValue()));
                    }
                }
                t.setLevel(Editor.TURNOUTS);
                return t;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icons for \"");
                sb.append(bean.getDisplayName());
                sb.append("\"");
                return  sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTOItemPanel.class);

}
