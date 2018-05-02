package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel general implementation for placement of CPE items having sets of icons (families).
 * @see ItemPanel palette class diagram
 * 
 * @author Pete Cressman Copyright (c) 2010, 2011
 * @author Egbert Broerse 2017
 */
public abstract class FamilyItemPanel extends ItemPanel {

    protected String _family;
    protected JPanel _iconFamilyPanel;  // Holds preview of _iconPanel & _dragIconPanel. They alternate being empty
    protected ImagePanel _dragIconPanel; // a panel on _iconFamilyPanel - to drag to control panel, hidden upon [Show Icons]
    protected ImagePanel _iconPanel;     // a panel on _iconFamilyPanel - all icons in family, shown upon [Show Icons]
    protected JPanel _familyButtonPanel;    // panel of radioButtons to select icon family

    protected boolean _suppressDragging;
    protected int _buttonPosition = 0;      // position of _iconFamilyPanel (TableItemPanels use 1)
    JPanel _bottom1Panel; // typically displays the _showIconsButton and _editIconsButton
    JPanel _bottom2Panel; // createIconFamilyButton - when all families have been deleted
    JButton _showIconsButton;
    JButton _editIconsButton;
    JButton _updateButton;
    protected HashMap<String, NamedIcon> _currentIconMap;
    IconDialog _dialog;
    ButtonGroup _familyButtonGroup;

    boolean _suppressNamePrompts;

    /**
     * Constructor types with multiple families and multiple icon families.
     *
     * @param parentFrame   enclosing parentFrame
     * @param type          bean type
     * @param family        icon family
     * @param editor        panel editor
     */
    public FamilyItemPanel(DisplayFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, editor);
        _family = family;
        _suppressNamePrompts = false;
    }

    /**
     * Create a FamilyIconPanel.
     * Also called by the enclosing TabbedPanel on change of displayed tab Pane to activate a different pane.
     */
    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _update = false;
            _suppressDragging = false;
            initIconFamiliesPanel();
            add(_iconFamilyPanel);
            makeBottomPanel(null);
        }
    }

    /**
     * Init for update of existing palette item type.
     * _bottom3Panel has an [Update Panel] button put onto _bottom1Panel.
     *
     * @param doneAction doneAction
     * @param iconMap iconMap
     */
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        _update = true;
        _suppressDragging = true; // no dragging when updating
        _currentIconMap = iconMap;
        if (iconMap != null) {
            checkCurrentMap(iconMap); // is map in families?, does user want to add it? etc.
        }
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
        makeBottomPanel(doneAction);
        // setSize(getPreferredSize());
    }

    /**
     * Init for conversion of plain track to IndicatorTrack.
     *
     * @param doneAction doneAction
     */
    public void init(ActionListener doneAction) {
        _update = false;
        _suppressDragging = true; // no dragging in circuitBuilder
        _bottom1Panel = new JPanel();
        addShowButtonToBottom();
        addUpdateButtonToBottom(doneAction);
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
        add(_bottom1Panel);
    }

    /**
     * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel
     * depending on whether icon families exist. They are made first because
     * they are referenced in initIconFamiliesPanel(). _bottom2Panel is for the
     * exceptional case where there are no families at all.
     * <p>
     * Subclasses will insert other panels.
     *
     * @param doneAction the calling action
     */
    protected void makeBottomPanel(ActionListener doneAction) {
        _bottom2Panel = makeCreateNewFamilyPanel(); // special case for when no families exist for a given itemType
        _bottom1Panel = makeItemButtonPanel();
        if (doneAction != null) {
            addUpdateButtonToBottom(doneAction);
        }
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        // If families are missing _bottom2Panel will be made visible.
        _bottom2Panel.setVisible(false);
        add(bottomPanel);
        log.debug("init done for family {}", _family);
    }

    /**
     * Add [Update] button to _bottom1Panel.
     * @param doneAction Action for button
     */
    protected void addUpdateButtonToBottom(ActionListener doneAction) {
        _updateButton = new JButton(Bundle.getMessage("updateButton")); // custom update label
        _updateButton.addActionListener(doneAction);
        _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        _bottom1Panel.add(_updateButton);
    }

    private void addShowButtonToBottom() {
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
        _bottom1Panel.add(_showIconsButton);
    }

    protected JPanel makeItemButtonPanel() {
        _bottom1Panel = new JPanel(new FlowLayout());
        addShowButtonToBottom();
        _editIconsButton = new JButton(Bundle.getMessage("ButtonEditIcons"));
        _editIconsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                openDialog(_itemType, _family, _currentIconMap);
            }
        });
        _editIconsButton.setToolTipText(Bundle.getMessage("ToolTipEditIcons"));
        _bottom1Panel.add(_editIconsButton);

        if (!_update) {
            addCreateDeleteFamilyButtons();
        }
        return _bottom1Panel;
    }
    
    protected void addCreateDeleteFamilyButtons() {
        JButton createIconsButton = new JButton(Bundle.getMessage("createNewFamily"));
        createIconsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                newFamilyDialog();
            }
        });
        createIconsButton.setToolTipText(Bundle.getMessage("ToolTipAddFamily"));
        _bottom1Panel.add(createIconsButton);

        JButton deleteButton = new JButton(Bundle.getMessage("deleteFamily"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                deleteFamilySet();
                dispose();
            }
        });
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteFamily"));
        _bottom1Panel.add(deleteButton);
    }

    /**
     * Check whether map is one of the families.
     * If so, return. If not, does user want to add it to families?
     * If so, add. If not, save for return when updated.
     *
     * @param iconMap existing map of the icon
     */
    private void checkCurrentMap(HashMap<String, NamedIcon> iconMap) {
        log.debug("checkCurrentMap: for type \"{}\", family \"{}\"", _itemType, _family);
        String family = findFamilyOfMap(iconMap, ItemPalette.getFamilyMaps(_itemType));
        if (family != null) {  // icons same as a known family, maybe with another name
            if (family.equals(_family)) {
                return;
            }
            log.debug("Icon's family \"{}\" found but is called \"{}\".  Change to Catalog name.", _family, family);
            _family = family;
            return;
        } else { // no match with Palette families
            if (ItemPalette.getIconMap(_itemType, _family) != null) {
                log.debug("Icon's family name \"{}\" keys a different icon set. Set name to null", _family);
            // JOptionPane.showMessageDialog(_paletteFrame,
            //      Bundle.getMessage("DuplicateFamilyName", _family, _itemType),
            //      Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            // make sure name does not duplicate a known name
                _family = null;
            } else {
                log.debug("Icon's family \"{}\" not found in Catalog.", _family);                
            }
        }
        if (!_suppressNamePrompts) {
            if (_family == null || _family.trim().length() == 0) {
                _family = JOptionPane.showInputDialog(_paletteFrame, Bundle.getMessage("NoFamilyName"),
                        Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE);
                if (_family == null || _family.trim().length() == 0) {
                    // bail out
                    _family = null;
                    // _suppressNamePrompts = true;
                    return;
                }
            }
            int result = JOptionPane.showConfirmDialog(_paletteFrame,
                    Bundle.getMessage("UnkownFamilyName", _family), Bundle.getMessage("QuestionTitle"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                ItemPalette.addFamily(_paletteFrame, _itemType, _family, iconMap);
            } else if (result == JOptionPane.NO_OPTION) {
                _suppressNamePrompts = true;
            }
        }
    }
    
    protected boolean addFamily(String type, String family, HashMap<String, NamedIcon> iconMap) {
        if (!ItemPalette.addFamily(_paletteFrame, type, family, iconMap)) {
            return false;
        } else {
            updateFamiliesPanel();
            setFamily(family);
            return true;
        }
    }

    /**
     * Find the family name of the map in a families HashMap.
     *
     * @return null if map is not in the family
     */
    private String findFamilyOfMap(HashMap<String, NamedIcon> iconMap, HashMap<String, HashMap<String, NamedIcon>> families) {
        Iterator<Entry<String, HashMap<String, NamedIcon>>> it = families.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashMap<String, NamedIcon>> entry = it.next();
            if (log.isDebugEnabled()) {
                log.debug("FamilyKey = {}", entry.getKey());
            }
            if (mapsAreEqual(entry.getValue(), iconMap)) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected boolean mapsAreEqual(HashMap<String, NamedIcon> map1, HashMap<String, NamedIcon> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }
        Iterator<Entry<String, NamedIcon>> iter = map1.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, NamedIcon> ent = iter.next();
            NamedIcon icon = map2.get(ent.getKey());
            if (icon == null) {
                if (log.isDebugEnabled()) {
                    log.debug("key = {}, family map url= {} item icon is null", ent.getKey(), ent.getValue().getURL());
                }
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("key = {}, family map url= {} item icon url= {}", ent.getKey(), ent.getValue().getURL(), icon.getURL());
                }
                String url = icon.getURL();
                if (url == null || !url.equals(ent.getValue().getURL())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Build lower (icon) element.
     * Overridden for SignalMastItemPanel.
     */
    protected void initIconFamiliesPanel() {
        log.debug("initIconFamiliesPanel for= {}, {}", _itemType, _family);
        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families != null && families.size() > 0) {
            if (_iconFamilyPanel == null) {
                _iconFamilyPanel = new JPanel();
                _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
            }
            _familyButtonPanel = makeFamilyButtons(families.keySet().iterator(), (_currentIconMap == null));
            if (_currentIconMap == null) {
                _currentIconMap = families.get(_family);
            }
            // make _iconPanel & _dragIconPanel before calls to add icons
            addFamilyPanels(_familyButtonPanel);
            if (_currentIconMap == null) {
                log.error("currentIconMap is null in initIconFamiliesPanel");
            } else {
                addIconsToPanel(_currentIconMap); // need to have family iconMap identified before calling
                makeDndIconPanel(_currentIconMap, "BeanStateUnknown");
            }
        } else {
            familiesMissing();
        }
        log.debug("initIconFamiliesPanel update={}, family={}", _update, _family);
    }

    protected void updateFamiliesPanel() {
        log.debug("updateFamiliesPanel for {}", _itemType);
        if (_iconFamilyPanel != null) {
            if (_iconPanel != null) {
                _iconPanel.removeAll();
            }
            if (_dragIconPanel != null) {
                _dragIconPanel.removeAll();
            }
            if (_familyButtonPanel != null) {
                _iconFamilyPanel.remove(_familyButtonPanel);
            }
        }
        initIconFamiliesPanel();
        add(_iconFamilyPanel, _buttonPosition);
        hideIcons();
        _iconFamilyPanel.invalidate();
        revalidate();
        reset();
    }

    /**
     * Create and set actions of radioButtons to change family on pane.
     * @param iter Iterates list of icon family names
     * @param setDefault choose button of the default family
     * @return family button panel
     */
    protected JPanel makeFamilyButtons(Iterator<String> iter, boolean setDefault) {
        log.debug("makeFamilyButtons for {}", _itemType);
        String thisType = null;
        JPanel familyPanel = new JPanel(); // this is only a local object
        familyPanel.setLayout(new BoxLayout(familyPanel, BoxLayout.Y_AXIS));
        // uses NamedBeanBundle property for basic beans like "Turnout" I18N
        if ("Sensor".equals(_itemType)) {
            thisType = "BeanNameSensor";
        } else if ("Turnout".equals(_itemType)) {
            thisType = "BeanNameTurnout";
        } else if ("SignalHead".equals(_itemType)) {
            thisType = "BeanNameSignalHead";
        } else if ("SignalMast".equals(_itemType)) {
            thisType = "BeanNameSignalMast";
        } else if ("Memory".equals(_itemType)) {
            thisType = "BeanNameMemory";
        } else if ("Reporter".equals(_itemType)) {
            thisType = "BeanNameReporter";
        } else if ("Light".equals(_itemType)) {
            thisType = "BeanNameLight";
        } else if ("Portal".equals(_itemType)) {
            thisType = "BeanNamePortal";
        } else if ("RPSReporter".equals(_itemType)) {
            thisType = "RPSreporter"; // adapt for slightly different spelling of Bundle key (2nd r lower case)
        } else {
            thisType = _itemType;
        }
        String txt = Bundle.getMessage("IconFamiliesLabel", Bundle.getMessage(thisType));
        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel(txt));
        p.setOpaque(false);
        familyPanel.add(p);
        _familyButtonGroup = new ButtonGroup();
        JPanel buttonPanel = new JPanel(new FlowLayout());
        String family = "";
        JRadioButton button = null;
        int count = 0;
        while (iter.hasNext()) {
            family = iter.next();
            count++;
            button = new JRadioButton(ItemPalette.convertText(family));
            button.addActionListener(new ActionListener() {
                String fam;

                @Override
                public void actionPerformed(ActionEvent e) {
                    setFamily(fam);
                }

                ActionListener init(String f) {
                    fam = f;
                    log.debug("ActionListener.init() for type \"{}\", family \"{}\"", _itemType, fam);
                    return this;
                }
            }.init(family));
            if (family.equals(_family)) {
                button.setSelected(true);
            }
            if (count > 4) { // put remaining radio buttons on a new line
                count = 0;
                familyPanel.add(buttonPanel);
                buttonPanel = new JPanel(new FlowLayout());
            }
            buttonPanel.add(button);
            _familyButtonGroup.add(button);
        }
        familyPanel.add(buttonPanel);
        if (setDefault && !family.equals(_family)) {
            _family = family; // let last family be the selected one
            if (button != null) {
                button.setSelected(true);
            } else {
                log.warn("null button after setting family");
            }
        }
        familyPanel.add(buttonPanel);
        return familyPanel;
    }

    /**
     * Position secondary Preview component on _iconFamilyPanel (visible after [Show Icons]).
     * @param familyPanel panel of family buttons
     */
    protected void addFamilyPanels(JPanel familyPanel) {
        log.debug("addFamilyPanels for {}", _itemType);
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        boolean makeBgBoxPanel = false;
        if (_iconPanel == null) { // don't overwrite existing _iconPanel
            _iconPanel = new ImagePanel();
            _iconPanel.setLayout(new FlowLayout());
            _iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            log.debug("_iconPanel created");
            makeBgBoxPanel = true;
        }
        _iconPanel.setVisible(false);

        if (!_suppressDragging) {
            makeDragIconPanel(0);
        }
        if (!_update) {
            if (makeBgBoxPanel) {
                _iconFamilyPanel.add(makePreviewPanel(_iconPanel, _dragIconPanel));
            }            
        } else {
            _iconPanel.setImage(_backgrounds[0]);
            _iconFamilyPanel.add(_iconPanel);
        }
        _iconFamilyPanel.add(familyPanel);
        if (_bottom1Panel != null) {
            _bottom1Panel.setVisible(true);
        }
        if (_bottom2Panel != null) {
            _bottom2Panel.setVisible(false);
        }
        log.debug("addFamilyPanels update={}", _update);
    }

    /**
     * Position initial Preview component on _iconFamilyPanel. If already present, keep and clear it.
     * @param position Positional order of DragIconPanel on IconFamilyPanel
     */
    protected void makeDragIconPanel(int position) {
        if (_dragIconPanel == null) {
            _dragIconPanel = new ImagePanel();
            _dragIconPanel.setOpaque(true); // to show background color/squares
            _dragIconPanel.setLayout(new FlowLayout());
            _dragIconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            _dragIconPanel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
            _iconFamilyPanel.add(_dragIconPanel, position); // place icons over background
        } else {
            _dragIconPanel.removeAll();
        }
        if (_backgrounds != null) {
            int previewBgSet = _paletteFrame.getPreviewBg();
            log.debug("set backgrounds to {}", previewBgSet);
            _dragIconPanel.setImage(_backgrounds[previewBgSet]); // pick up shared setting
            if (_iconPanel != null) {
                _iconPanel.setImage(_backgrounds[previewBgSet]); // pick up shared setting
            }
        } else {
            log.debug("FamilyItemPanel - no value for previewBgSet");
        }
        _dragIconPanel.setVisible(true);

    }

    protected void familiesMissing() {
        int result = JOptionPane.showConfirmDialog(_paletteFrame,
                Bundle.getMessage("AllFamiliesDeleted", _itemType), Bundle.getMessage("QuestionTitle"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            ItemPalette.loadMissingItemType(_itemType, _editor);
            initIconFamiliesPanel();
            _bottom1Panel.setVisible(true);
            _bottom2Panel.setVisible(false);
        } else {
            _bottom1Panel.setVisible(false);
            _bottom2Panel.setVisible(true);
        }
    }

    protected void addIconsToPanel(HashMap<String, NamedIcon> iconMap) {
        if (iconMap == null) {
            log.debug("iconMap is null for type {} family {}", _itemType, _family);
            return;
        }
        if (_iconPanel == null) { // bug for SignalMast icons (is of class ImagePanel)
            log.error("_iconPanel is null for type {}", _itemType);
            return;
        }
        _iconPanel.setOpaque(false);
        GridBagLayout gridbag = new GridBagLayout();
        _iconPanel.setLayout(gridbag);

        int numCol = 4;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = -1;
        c.gridy = 0;

        int cnt = iconMap.size();
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            NamedIcon icon = new NamedIcon(entry.getValue()); // make copy for possible reduction
            icon.reduceTo(100, 100, 0.2);
            JPanel panel = new JPanel(new FlowLayout());
            panel.setOpaque(false);
            // I18N use existing NamedBeanBundle keys
            String borderName = getIconBorderName(entry.getKey());
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                    borderName));
            JLabel image = new JLabel(icon);
            if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                image.setText(Bundle.getMessage("invisibleIcon"));
                image.setOpaque(false);
            }
            image.setToolTipText(icon.getName());
            panel.add(image);
            int width = getFontMetrics(getFont()).stringWidth(borderName);
            width = Math.max(100, Math.max(width, icon.getIconWidth())+10);
            panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
            c.gridx += 1;
            if (c.gridx >= numCol) { //start next row
                c.gridy++;
                c.gridx = 0;
                if (cnt < numCol - 1) { // last row
                    JPanel p = new JPanel(new FlowLayout());
                    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                    p.setOpaque(false);
                    p.add(Box.createHorizontalStrut(100));
                    gridbag.setConstraints(p, c);
                    log.debug("addIconsToPanel: gridx = {} gridy = {}", c.gridx, c.gridy);
                    _iconPanel.add(p);
                    c.gridx = 1;
                }
            }
            cnt--;
            gridbag.setConstraints(panel, c);
            _iconPanel.add(panel);
        }
    }

    protected String getIconBorderName(String key) {
        return ItemPalette.convertText(key);
    }

    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
        return null;
    }

    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        if (_suppressDragging) {
            return;
        }
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        if (iconMap != null) {
            if (iconMap.get(displayKey) == null) {
                displayKey = (String) iconMap.keySet().toArray()[0];
            }
            NamedIcon ic = iconMap.get(displayKey);
            if (ic != null) {
                NamedIcon icon = new NamedIcon(ic);
                JPanel panel = new JPanel(new FlowLayout());
                String borderName = ItemPalette.convertText("dragToPanel");
                panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                        borderName));
                panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
                panel.setOpaque(false);
                JLabel label;
                try {
                    label = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), iconMap, icon);
                    if (label != null) {
                        label.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
                        // label.setIcon(icon);
                        label.setName(borderName);
                        label.setOpaque(false);
                        panel.add(label);
                    }
                } catch (java.lang.ClassNotFoundException cnfe) {
                    log.warn("no DndIconPanel {} created", borderName, cnfe);
                }
                int width = getFontMetrics(getFont()).stringWidth(borderName);
                width = Math.max(100, Math.max(width, icon.getIconWidth())+10);
                panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
                _dragIconPanel.add(panel);
                return;
            }
        } else {
            log.error("No iconMap for makeDndIconPanel");
        }
    }

    protected void hideIcons() {
        if (_iconPanel == null) {
            log.debug("hideIcons() _iconPanel = null");
            return;
        }
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        _iconPanel.setVisible(false);
        if (!_suppressDragging) {
            _dragIconPanel.setVisible(true);
            _dragIconPanel.invalidate();
        }
        _showIconsButton.setText(Bundle.getMessage("ShowIcons"));
        reset();
    }

    protected void showIcons() {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        _iconPanel.setVisible(true);
        _iconPanel.invalidate(); // force redraw
        if (!_suppressDragging) {
            _dragIconPanel.setVisible(false);
        }
        _showIconsButton.setText(Bundle.getMessage("HideIcons"));
        reset();
    }

    /**
     * Action item for deletion of an icon family.
     */
    protected void deleteFamilySet() {
        ItemPalette.removeIconMap(_itemType, _family);
        _family = null;
        _currentIconMap = null;
        updateFamiliesPanel();
    }

    /**
     * Replacement panel for _bottom1Panel when no icon families exist for
     * _itemType.
     */
    private JPanel makeCreateNewFamilyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton newFamilyButton = new JButton(Bundle.getMessage("createNewFamily"));
        newFamilyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                newFamilyDialog();
            }
        });
        newFamilyButton.setToolTipText(Bundle.getMessage("ToolTipAddFamily"));
        panel.add(newFamilyButton);

        if(!_update) {
            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    updateFamiliesPanel();
                }
            });
            panel.add(cancelButton);
        }
        return panel;
    }

    protected boolean newFamilyDialog() {
        String family = JOptionPane.showInputDialog(_paletteFrame, Bundle.getMessage("EnterFamilyName"),
                Bundle.getMessage("createNewIconSet", _itemType), JOptionPane.QUESTION_MESSAGE);
        if (family == null || family.trim().length() == 0) {
            // bail out
            return false;
        }
        Iterator<String> iter = ItemPalette.getFamilyMaps(_itemType).keySet().iterator();
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        while (iter.hasNext()) {
            if (family.equals(iter.next())) {
                JOptionPane.showMessageDialog(_paletteFrame,
                        Bundle.getMessage("DuplicateFamilyName", family, _itemType),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        openDialog(_itemType, family, null);
        return true;
    }

    @Override
    protected void setPreviewBg(int index) {
        if (_dialog != null) {
            ImagePanel iconPanel = _dialog.getIconPanel();
            if (iconPanel != null) {
                iconPanel.setImage(_backgrounds[index]);
            }
        }
    }

    protected void openDialog(String type, String family, HashMap<String, NamedIcon> iconMap) {
        closeDialogs();
        _dialog = new IconDialog(type, family, this, iconMap);
        _dialog.sizeLocate();
    }

    @Override
    protected void closeDialogs() {
        if (_dialog != null) {
            _dialog.closeDialogs();
            _dialog.dispose();
        }
    }

    public void dispose() {
        closeDialogs();
    }

    /**
     * Action of family radio button.
     * MultiSensorItemPanel and IndicatorTOItemPanel must override.
     *
     * @param family icon family name
     */
    protected void setFamily(String family) {
        _family = family;
        log.debug("setFamily: for type \"{}\", family \"{}\"", _itemType, family);
        if (_iconPanel == null) {
            _iconPanel = new ImagePanel();
            _iconFamilyPanel.add(_iconPanel, 0);
            log.error("setFamily called with _iconPanel == null typs= {}", _itemType);
        } else {
            _iconPanel.removeAll(); // just clear contents
        }
        HashMap<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
        if (map != null) {
            _currentIconMap = map;
        } else {
            log.warn("Family \"{}\" for type \"{}\" for not found in Catalog.", _family, _itemType);                
        }
        if (!_suppressDragging) {
            makeDragIconPanel(0);
            makeDndIconPanel(_currentIconMap, "BeanStateUnknown");
        }
        addIconsToPanel(_currentIconMap);
        _iconFamilyPanel.revalidate(); // force redraw
        hideIcons();
        Enumeration<AbstractButton> en = _familyButtonGroup.getElements();
        while (en.hasMoreElements()) {
            JRadioButton but = (JRadioButton) en.nextElement();
            if (_family != null && _family.equals(but.getText())) {
                but.setSelected(true);
                break;
            }
        }
    }

    @Override
    protected void setEditor(Editor ed) {
        super.setEditor(ed);
        if (_initialized) {
            boolean visible = (_iconPanel != null && _iconPanel.isVisible()); // check for invalid _initialized state
            makeDragIconPanel(0);
            makeDndIconPanel(_currentIconMap, "BeanStateUnknown");
            if (_family != null) {
                setFamily(_family);
            }
            if (!visible) {
                hideIcons();
            } else {
                showIcons();
            }
        }
    }

    /**
     * Create icon set to panel icon display class.
     *
     * @return updated icon map
     */
    public HashMap<String, NamedIcon> getIconMap() {
        if (_currentIconMap == null) {
            _currentIconMap = ItemPalette.getIconMap(_itemType, _family);
        }
        if (_currentIconMap == null) {
            log.warn("Family \"{}\" for type \"{}\" for not found in Catalog.", _family, _itemType);                
        }
        return _currentIconMap;
    }

    public String getFamilyName() {
        return _family;
    }

    private final static Logger log = LoggerFactory.getLogger(FamilyItemPanel.class);

}
