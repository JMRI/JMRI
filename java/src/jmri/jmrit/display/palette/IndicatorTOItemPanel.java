package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.NamedBean;
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
 */
public class IndicatorTOItemPanel extends TableItemPanel {

    final static String[] STATUS_KEYS = {"ClearTrack", "OccupiedTrack", "PositionTrack",
        "AllocatedTrack", "DontUseTrack", "ErrorTrack"};

    private DetectionPanel _detectPanel;
    private JPanel _tablePanel;
    protected HashMap<String, HashMap<String, NamedIcon>> _iconGroupsMap;

    public IndicatorTOItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<jmri.Turnout> model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    /**
     * Init for creation _bottom1Panel and _bottom2Panel alternate visibility in
     * bottomPanel depending on whether icon families exist. They are made first
     * because they are referenced in initIconFamiliesPanel().
     */
    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _bottom1Panel.remove(_editIconsButton);
            _detectPanel = new DetectionPanel(this);
            add(_detectPanel, 1);
            add(_iconFamilyPanel, 2);
            _buttonPosition = 2;
        }
    }

    /**
     * Init for conversion of plain track to indicator track.
     */
    @Override
    public void init(ActionListener doneAction) {
        super.init(doneAction);
        add(_iconFamilyPanel, 0);
        _buttonPosition = 0;
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
        checkCurrentMaps(iconMaps); // is map in families?, does user want to add it? etc.
        super.init(doneAction, null);
        _bottom1Panel.remove(_editIconsButton);
        _detectPanel = new DetectionPanel(this);
        add(_detectPanel, 1);
        add(_iconFamilyPanel, 2);
        _buttonPosition = 2;
    }

    /**
     * iconMap is existing map of the icon. Check whether map is one of the
     * families. If so, return. If not, does user want to add it to families? If
     * so, add. If not, save for return when updated.
     */
    private void checkCurrentMaps(HashMap<String, HashMap<String, NamedIcon>> iconMaps) {
        String family = null;
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> families
                = ItemPalette.getLevel4FamilyMaps(_itemType);
        Iterator<Entry<String, HashMap<String, HashMap<String, NamedIcon>>>> it = families.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashMap<String, HashMap<String, NamedIcon>>> entry = it.next();
            family = entry.getKey();
            log.debug("FamilyKey = {}", family);
            Iterator<Entry<String, HashMap<String, NamedIcon>>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, HashMap<String, NamedIcon>> ent = iter.next();
                HashMap<String, NamedIcon> subFamily = iconMaps.get(ent.getKey());
                if (!mapsAreEqual(ent.getValue(), subFamily)) {
                    family = null;
                    break;
                }
            }
            if (family != null) {
                _family = family;
                return;
            }
        }
        if (ItemPalette.getLevel4Family(_itemType, _family) != null) {
            JOptionPane.showMessageDialog(_paletteFrame,
                    Bundle.getMessage("DuplicateFamilyName", _family, _itemType),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            // make sure name does not duplicate a known name
            _family = null;
        }
        if (!_suppressNamePrompts) {
            if (_family == null || _family.trim().length() == 0) {
                _family = JOptionPane.showInputDialog(_paletteFrame, Bundle.getMessage("NoFamilyName"),
                        Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE);
                if (_family == null || _family.trim().length() == 0) {
                    // bail out
                    _family = null;
                    _suppressNamePrompts = true;
                    return;
                }
            }
            int result = JOptionPane.showConfirmDialog(_paletteFrame,
                    Bundle.getMessage("UnkownFamilyName", _family), Bundle.getMessage("QuestionTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                ItemPalette.addLevel4Family(_paletteFrame, _itemType, _family, iconMaps);
            } else if (result == JOptionPane.NO_OPTION) {
                _suppressNamePrompts = true;
            }
        }
    }

    /*
     * Get a handle in order to change visibility.
     */
    @Override
    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _tablePanel = super.initTablePanel(model, editor);
        return _tablePanel;
    }

    @Override
    public void dispose() {
        if (_detectPanel != null) {
            _detectPanel.dispose();
        }
    }

    /**
     * CENTER Panel
     */
    @Override
    protected void initIconFamiliesPanel() {
        if (_iconFamilyPanel == null) { // keep existing panels
            _iconFamilyPanel = new ImagePanel();
            _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        } else {
            _iconFamilyPanel.removeAll(); // TODO use a function in _iconFamilyPanel
        }
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> families
                = ItemPalette.getLevel4FamilyMaps(_itemType);
        if (families != null && families.size() > 0) {

            JPanel familyPanel = makeFamilyButtons(families.keySet().iterator(), (_iconGroupsMap == null));

            if (_iconGroupsMap == null) {
                _iconGroupsMap = families.get(_family);
            }
            // make _iconPanel + _dragIconPanel before calls to add icons
            addFamilyPanels(familyPanel);

            if (_iconGroupsMap == null) {
                log.error("_iconGroupsMap is null in initIconFamiliesPanel");
                _family = null;
            } else {
                addIcons2Panel(_iconGroupsMap); // need to have family iconMap identified before calling
                makeDndIconPanel(_iconGroupsMap.get("ClearTrack"), "TurnoutStateClosed");
            }
        } else {
            familiesMissing();
        }
        updateBackgrounds(); // create array of backgrounds
        log.debug("initIconFamiliesPanel done");
    }

    private void resetFamiliesPanel() {
        //_iconFamilyPanel.removeAll();
        _tablePanel.setVisible(true);
        initIconFamiliesPanel();
        int n = _iconFamilyPanel.getComponentCount();
        if (n > 2) {
            add(_iconFamilyPanel, 2);
        } else {
            add(_iconFamilyPanel, 0);
        }
        reset();
    }

    /**
     * Make matrix of icons - each row has a button to change icons.
     */
    private void addIcons2Panel(HashMap<String, HashMap<String, NamedIcon>> map) {
        GridBagLayout gridbag = new GridBagLayout();
        _iconPanel.setLayout(gridbag);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridy = -1;

        Iterator<Entry<String, HashMap<String, NamedIcon>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            c.gridx = 0;
            c.gridy++;

            Entry<String, HashMap<String, NamedIcon>> entry = it.next();
            String stateName = entry.getKey();
            JPanel panel = new JPanel();
            panel.add(new JLabel(ItemPalette.convertText(stateName)));
            panel.setOpaque(false);
            gridbag.setConstraints(panel, c);
            _iconPanel.add(panel);
            c.gridx++;
            HashMap<String, NamedIcon> iconMap = entry.getValue();
            ItemPanel.checkIconMap("Turnout", iconMap); // NOI18N
            Iterator<Entry<String, NamedIcon>> iter = iconMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, NamedIcon> ent = iter.next();
                String borderName = ItemPalette.convertText(ent.getKey());
                NamedIcon icon = new NamedIcon(ent.getValue());    // make copy for possible reduction
                icon.reduceTo(100, 100, 0.2);
                panel = new JPanel();
                panel.setOpaque(false);
                panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                        borderName));
                //if (log.isDebugEnabled()) log.debug("addIcons2Panel: "+borderName+" icon at ("
                //                                    +c.gridx+","+c.gridy+") width= "+icon.getIconWidth()+
                //                                    " height= "+icon.getIconHeight());
                JLabel image = new JLabel(icon);
                if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                    image.setText(Bundle.getMessage("invisibleIcon"));
                    image.setForeground(Color.lightGray);
                }
                image.setToolTipText(icon.getName());
                panel.add(image);
                int width = Math.max(85, panel.getPreferredSize().width);
                panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
                gridbag.setConstraints(panel, c);
                _iconPanel.add(panel);
                c.gridx++;
            }
            panel = new JPanel();
            panel.setOpaque(false);
            JButton button = new JButton(Bundle.getMessage("ButtonEditIcons"));
            button.addActionListener(new ActionListener() {
                String key;

                @Override
                public void actionPerformed(ActionEvent a) {
                    openStatusEditDialog(key);
                }

                ActionListener init(String k) {
                    key = k;
                    return this;
                }
            }.init(stateName));
            button.setToolTipText(Bundle.getMessage("ToolTipEditIcons"));
            panel.add(button);
            gridbag.setConstraints(panel, c);
            _iconPanel.add(panel);
            //if (log.isDebugEnabled()) log.debug("addIcons2Panel: row "+c.gridy+" has "+iconMap.size()+" icons");
        }
    }

    protected JPanel makeItemButtonPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        _showIconsButton = new JButton(Bundle.getMessage("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (_iconPanel.isVisible()) {
                    hideIcons();
                } else {
                    showIcons();
                }
            }
        });
        _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipShowIcons"));
        bottomPanel.add(_showIconsButton);

        JButton createIconsButton = new JButton(Bundle.getMessage("createNewFamily"));
        createIconsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                newFamilyDialog();
            }
        });
        createIconsButton.setToolTipText(Bundle.getMessage("ToolTipAddFamily"));
        bottomPanel.add(createIconsButton);

        if (!_update) {
            JButton deleteButton = new JButton(Bundle.getMessage("deleteFamily"));
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    deleteFamilySet();
                    dispose();
                }
            });
            deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteFamily"));
            bottomPanel.add(deleteButton);
        }
        return bottomPanel;
    }

    @Override
    protected boolean newFamilyDialog() {
        String family = JOptionPane.showInputDialog(_paletteFrame, Bundle.getMessage("EnterFamilyName"),
                Bundle.getMessage("createNewIconSet", _itemType), JOptionPane.QUESTION_MESSAGE);
        if (family == null || family.trim().length() == 0) {
            // bail out
            return false;
        }
        Iterator<String> iter = ItemPalette.getLevel4FamilyMaps(_itemType).keySet().iterator();
        while (iter.hasNext()) {
            if (family.equals(iter.next())) {
                JOptionPane.showMessageDialog(_paletteFrame,
                        Bundle.getMessage("DuplicateFamilyName", family, _itemType),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        _family = family;
        createNewFamily();
        showIcons();
        return true;
    }

    @Override
    protected void hideIcons() {
        if (_tablePanel != null) {
            _tablePanel.setVisible(true);
        }
        if (_detectPanel != null) {
            _detectPanel.setVisible(true);
        }
        super.hideIcons();
    }

    @Override
    protected void showIcons() {
        if (_detectPanel != null) {
            _detectPanel.setVisible(false);
        }
        if (_tablePanel != null) {
            _tablePanel.setVisible(false);
        }
        super.showIcons();
    }

    /**
     * Action item for delete family.
     */
    @Override
    protected void deleteFamilySet() {
        ItemPalette.removeLevel4IconMap(_itemType, _family, null);
        _family = null;
        _currentIconMap = null;
        updateFamiliesPanel();
    }

    private void createNewFamily() {
        _iconGroupsMap = new HashMap<String, HashMap<String, NamedIcon>>();
        for (int i = 0; i < STATUS_KEYS.length; i++) {
            _iconGroupsMap.put(STATUS_KEYS[i], makeNewIconMap("Turnout")); // NOI18N
        }
        ItemPalette.addLevel4Family(_editor, _itemType, _family, _iconGroupsMap);
        resetFamiliesPanel();
        setFamily(_family);
    }

    /**
     * _iconGroupsMap holds edit changes when done is pressed.
     */
    protected void updateIconGroupsMap(String key, HashMap<String, NamedIcon> iconMap) {
        _iconGroupsMap.put(key, iconMap);
    }

    @Override
    protected void setFamily(String family) {
        _family = family;
        log.debug("setFamily: for type \"{}\", family \"{}\"", _itemType, family);
        if (_iconPanel == null) {
            _iconPanel = new ImagePanel();
            _iconPanel.setOpaque(true);
            _iconPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
                    Bundle.getMessage("PreviewBorderTitle")));
            _iconFamilyPanel.add(_iconPanel, 0);
        } else {
            _iconPanel.removeAll();
        }
        if (!_suppressDragging) {
            if (_dragIconPanel == null) {
                makeDragIconPanel(1);
            } else {
                _dragIconPanel.removeAll();
            }
        }
        _iconGroupsMap = ItemPalette.getLevel4Family(_itemType, _family);
        addIcons2Panel(_iconGroupsMap);
        makeDndIconPanel(_iconGroupsMap.get("ClearTrack"), "TurnoutStateClosed");
        hideIcons();
    }

    @Override
    protected void updateFamiliesPanel() {
        if (log.isDebugEnabled()) {
            log.debug("updateFamiliesPanel for {}", _itemType);
        }
        if (_iconFamilyPanel != null) {
            removeIconFamiliesPanel();
        }
        initIconFamiliesPanel();
        add(_iconFamilyPanel, _buttonPosition);
        showIcons();
        _iconFamilyPanel.invalidate();
        invalidate();
        reset();
    }

    private void openStatusEditDialog(String key) {
        if (log.isDebugEnabled()) {
            log.debug("openStatusEditDialog for family \"{}\" and \"{}\"", _family, key);
        }
        _currentIconMap = _iconGroupsMap.get(key);
        _dialog = new IndicatorTOIconDialog(_itemType, _family, this, key, _currentIconMap);
    }

    /**
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
        if (_iconGroupsMap == null) {
            _iconGroupsMap = ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
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
            if (iconMap == null) {
                log.error("IconDragJLabel.getTransferData: iconMap is null!");
                return null;
            }

            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_editor);

                t.setOccBlock(_detectPanel.getOccBlock());
                t.setOccSensor(_detectPanel.getOccSensor());
                t.setShowTrain(_detectPanel.getShowTrainName());
                t.setTurnout(bean.getSystemName());
                t.setFamily(_family);

                Iterator<Entry<String, HashMap<String, NamedIcon>>> it = iconMap.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, HashMap<String, NamedIcon>> entry = it.next();
                    String status = entry.getKey();
                    Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                    while (iter.hasNext()) {
                        Entry<String, NamedIcon> ent = iter.next();
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
