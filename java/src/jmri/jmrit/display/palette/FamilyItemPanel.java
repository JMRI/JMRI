package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.annotation.Nonnull;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel general implementation for placement of CPE items having sets of
 * icons (families). The "family" is the set of icons that represent the various
 * states and/or status of the item.
 * 
 * @see ItemPanel palette class diagram
 * @author Pete Cressman Copyright (c) 2010, 2011, 2018
 * @author Egbert Broerse 2017
 */
public abstract class FamilyItemPanel extends ItemPanel {

    protected String _family;
    // _iconPanel (from ItemPanel) contains all the images of the icons for a
    // selected family.
    // _previewPanel (from ItemPanel) contains _iconPanel and optionally
    // _dragIconPanel.
    //protected JPanel _iconFamilyPanel; // Holds _previewPanel, _familyButtonPanel.
    protected ImagePanel _dragIconPanel; // panel to drag to the icons to the
                                         // control panel, hidden upon [Show Icons]
    protected JPanel _familyButtonPanel; // panel of radioButtons to select icon family
    protected JButton _showIconsButton;
    protected JButton _editButton;
    protected JButton _updateButton;
    protected HashMap<String, NamedIcon> _unstoredMap;
    protected IconDialog _dialog;
    protected ButtonGroup _familyButtonGroup;

    protected boolean _isUnstoredMap;

    /**
     * Constructor types with multiple families and multiple icon families.
     *
     * @param parentFrame enclosing parentFrame
     * @param type        bean type
     * @param family      icon family
     */
    public FamilyItemPanel(DisplayFrame parentFrame, String type, String family) {
        super(parentFrame, type);
        _family = family;
    }

    /**
     * Create a FamilyIconPanel. Also called by the enclosing TabbedPanel on
     * change of displayed tab Pane to activate a different pane.
     */
    @Override
    public void init() {
        if (!_initialized) {
            _update = false;
            _suppressDragging = false;
            JPanel bottomPanel = makeBottomPanel(null);
            initIconFamiliesPanel();
            // Must check since ReporterItemPanel overrides and returns null
            if (bottomPanel != null) {
                add(bottomPanel);
            }
            super.init();
            if (log.isDebugEnabled()) {
                log.debug("init done for {}, family= {}", _itemType, _family);
            }
        }
        hideIcons();
    }

    /**
     * Init for update of existing palette item type.
     *
     * @param doneAction doneAction
     * @param iconMap    iconMap
     */
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        _update = true;
        _suppressDragging = true; // no dragging when updating
        JPanel bottomPanel = makeBottomPanel(doneAction);
        if (iconMap != null) {
            checkCurrentMap(iconMap); // is map in catalog?
        }
        if (_family == null || _family.isEmpty()) {
            _family = Bundle.getMessage("unNamed");
        }
        initIconFamiliesPanel();
        add(bottomPanel);
        _initialized = true;
    }

    /**
     * Initialization for conversion of plain track to indicator track by
     * CircuitBuilder.
     *
     * @param bottomPanel button panel
     */
    public void init(JPanel bottomPanel) {
        _update = false;
        _suppressDragging = true; // no dragging in circuitBuilder
        initIconFamiliesPanel();
        bottomPanel.add(makeShowIconsButton(), 0);
        add(bottomPanel);
        _initialized = true;
    }

    @Override
    protected void makeDataFlavors() {}

    /**
     * Needed by CPE ConvertDialog.java
     * 
     * @return JPanel
     */
    public JPanel getBottomPanel() {
        return _bottom1Panel;
    }

    public JButton getUpdateButton() {
        return _updateButton;
    }

    /**
     * Add [Update] button to _bottom1Panel.
     * 
     * @param doneAction Action for button
     */
    @Override
    protected JButton makeUpdateButton(ActionListener doneAction) {
        _updateButton = new JButton(Bundle.getMessage("updateButton"));
        _updateButton.addActionListener(doneAction);
        _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        return _updateButton;
    }

    @Override
    protected void makeItemButtonPanel() {
        _bottom1Panel = new JPanel();
        _bottom1Panel.add(makeShowIconsButton());
        _bottom1Panel.add(makeEditButton());
        if (!_update) {
            addCreateDeleteFamilyButtons();
        }
    }

    private JButton makeShowIconsButton() {
        if (_showIconsButton == null) {
            _showIconsButton = new JButton(Bundle.getMessage("ShowIcons"));
            _showIconsButton.addActionListener(a -> {
                if (_iconPanel.isVisible()) {
                    hideIcons();
                } else {
                    showIcons();
                }
            });
            _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipShowIcons"));
        }
        return _showIconsButton;
    }

    private JButton makeEditButton() {
        if (_editButton == null) {
            _editButton = new JButton(Bundle.getMessage("ButtonEditIcons"));
            _editButton.addActionListener(a -> openDialog(_itemType, _family));
            _editButton.setToolTipText(Bundle.getMessage("ToolTipEditIcons"));
        }
        return _editButton;
    }

    /**
     * Replacement panel for _bottom1Panel when no icon families exist for
     * _itemType.
     */
    @Override
    protected void makeSpecialBottomPanel(boolean update) {
        _bottom2Panel = new JPanel();
        JButton button;
        if (update) {
            _bottom2Panel.add(makeEditButton());
        } else {
            button = new JButton(Bundle.getMessage("createNewFamily"));
            button.addActionListener(a -> newFamilyDialog());
            button.setToolTipText(Bundle.getMessage("ToolTipAddFamily"));
            _bottom2Panel.add(button);
        }
        button = new JButton(Bundle.getMessage("RestoreDefault"));
        button.addActionListener(a -> loadDefaultType());
        _bottom2Panel.add(button);
    }

    protected void addCreateDeleteFamilyButtons() {
        JButton createIconsButton = new JButton(Bundle.getMessage("createNewFamily"));
        createIconsButton.addActionListener(a -> newFamilyDialog());
        createIconsButton.setToolTipText(Bundle.getMessage("ToolTipAddFamily"));
        _bottom1Panel.add(createIconsButton);

        JButton deleteButton = new JButton(Bundle.getMessage("deleteFamily"));
        deleteButton.addActionListener(a -> deleteFamilySet());
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteFamily"));
        _bottom1Panel.add(deleteButton);
    }

    /**
     * Check whether map is one of the families. If so, return. If not, does
     * user want to add it to families? If so, add. If not, save for return when
     * updated. update ctor has entered a name for _family.
     *
     * @param iconMap existing map of the icon
     */
    private void checkCurrentMap(HashMap<String, NamedIcon> iconMap) {
        if (_itemType.equals("SignalMast")) {
            return;
        }
        String family = getValidFamilyName(_family, iconMap);

        if (_isUnstoredMap) {
            _unstoredMap = iconMap;
            int result = JOptionPane.showConfirmDialog(_frame.getEditor(), Bundle.getMessage("UnkownFamilyName", family),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                ItemPalette.addFamily(_itemType, family, iconMap);
            }
            _family = family;
        } else {
            if (family != null) { // icons same as a known family, maybe with
                                  // another name
                if (!family.equals(_family)) {
                    log.info(
                            "{} icon's family \"{}\" found but is called \"{}\" in the Catalog.  Name changed to Catalog name.",
                            _itemType, _family, family);
                    _family = family;
                }
                return;
            }
        }
    }

    /**
     * Check that family name proposed by user for an icon family 1. name is not
     * a duplicate key 2. icon family is already stored. (Sets "_isUnstoredMap"
     * flag.)
     * 
     * @param family  name for icon set
     * @param iconMap map the family name refers to.
     * @return valid family name or null if user declines to provide a valid
     *         name.
     */
    protected String getValidFamilyName(String family, HashMap<String, NamedIcon> iconMap) {
        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        String mapFamily;
        if (iconMap != null) {
            mapFamily = findFamilyOfMap(null, iconMap, families);
            if (mapFamily == null) {
                _isUnstoredMap = true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("getValidFamilyName: findFamilyOfMap {} found stored family \"{}\" for family \"{}\".",
                            _itemType, mapFamily, family);
                }
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
                if (mapFamily == null) { // user quit
                    return null;
                }
            }
            if (families.isEmpty()) {
                break;
            }
            Iterator<String> iter = families.keySet().iterator();
            while (iter.hasNext()) {
                String fam = iter.next();
                if (mapFamily.equals(fam)) {
                    if (_update) {
                        JOptionPane.showMessageDialog(_frame,
                                Bundle.getMessage("DuplicateFamilyName", _itemType,
                                        Bundle.getMessage("CannotUseName", Bundle.getMessage("unNamed"))),
                                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                        mapFamily = null;
                        nameOK = false;
                        break;
                    } else {
                        return mapFamily;
                    }
                }
                nameOK = true;
            }
            if (!nameOK && _update) {
                break;
            }
        }
        return mapFamily;
    }

    /**
     * Find the family name of the map in a families HashMap.
     *
     * @param exemptFamily exclude from matching
     * @param newMap       iconMap
     * @param families     families of itemType
     * @return null if map is not in the family
     */
    protected String findFamilyOfMap(String exemptFamily, HashMap<String, NamedIcon> newMap,
            HashMap<String, HashMap<String, NamedIcon>> families) {
        for (Entry<String, HashMap<String, NamedIcon>> entry : families.entrySet()) {
            String family = entry.getKey();
            // log.debug("FamilyKey = {}", entry.getKey());
            if (!family.equals(exemptFamily)) {
                if (mapsAreEqual(entry.getValue(), newMap)) {
                    // log.debug("findFamilyOfMap: Map found with name \"{}\"",
                    // entry.getKey());
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * See if the signal head map is supported by the family map. "Equals" in
     * this context means that each map is the same size, the keys are equal and
     * the urls for the icons are equal. Note that icons with different urls may
     * be or appear to be the same.
     * 
     * @param mapOne a HashMap<String, NamedIcon>
     * @param mapTwo another HashMap<String, NamedIcon>
     * @return true if all of signal head entries have matching entries in the
     *         family map.
     */
    protected boolean mapsAreEqual(HashMap<String, NamedIcon> mapOne, HashMap<String, NamedIcon> mapTwo) {
        if (mapOne.size() != mapTwo.size()) {
            return false;
        }
        for (Entry<String, NamedIcon> mapTwoEntry : mapTwo.entrySet()) {
            NamedIcon mapOneIcon = mapOne.get(mapTwoEntry.getKey());
            if (mapOneIcon == null) {
                return false;
            }
            String url = mapOneIcon.getURL();
            if (url == null || !url.equals(mapTwoEntry.getValue().getURL())) {
                return false;
            }
        }
        return true;
    }

    protected boolean NamesStoredMap(String family) {
        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families.keySet().contains(family)) {
            return true;
        }
        return false;
    }
    
    /*
     * Entry point when returning from result of familiesMissing() call
     */
    @Override
    protected void makeFamiliesPanel() {
        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families.values().isEmpty()) {
            if (!familiesMissing()) {   // still no families
                _currentIconMap = _unstoredMap;
                addIconsToPanel(_currentIconMap, _iconPanel, false);
                if (!_suppressDragging) {
                    makeDragIconPanel();
                    makeDndIcon(_currentIconMap);
                }
                addFamilyPanels(false);
            }
        } else {
            makeFamiliesPanel(families);
        }
    }

    private void makeFamiliesPanel(@Nonnull HashMap<String, HashMap<String, NamedIcon>> families) {

        makeFamilyButtons(families.keySet()); // makes _familyButtonPanel
        if (_currentIconMap == null) {
            _currentIconMap = families.get(_family);
            if (_currentIconMap == null) {
                _isUnstoredMap = true;
                _currentIconMap = _unstoredMap;
            }
        }
        addIconsToPanel(_currentIconMap, _iconPanel, false);

        if (!_suppressDragging) {
            makeDragIconPanel();
            makeDndIcon(_currentIconMap);
        }

        addFamilyPanels(!families.isEmpty());
    }

    @Override
    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(new JLabel(Bundle.getMessage("PickRowBean", _itemType)));
        blurb.add(new JLabel(Bundle.getMessage("DragBean")));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    protected void addFamilyPanels(boolean hasMaps) {
        makePreviewPanel(hasMaps, _dragIconPanel);

        if (_familyButtonPanel != null) {
            _iconFamilyPanel.add(_familyButtonPanel);
        }

        if (_bottom1Panel != null) {
            _bottom1Panel.setVisible(hasMaps);
        }
        if (_bottom2Panel != null) {
            _bottom2Panel.setVisible(!hasMaps);
        }
        _iconPanel.setVisible(false);
    }

    // the null checks are needed for several rare cases where nulls can occur
    protected void updateFamiliesPanel() {
        log.debug("updateFamiliesPanel for {}", _itemType);
        if (_iconPanel != null) {
            _iconPanel.removeAll();
        }
        if (_dragIconPanel != null) {
            _dragIconPanel.removeAll();
        }
        if (_previewPanel != null) {
            _previewPanel.setVisible(false);
        }
        if (_familyButtonPanel != null) {
            _familyButtonPanel.removeAll();
        }
        makeFamiliesPanel();
        _iconFamilyPanel.invalidate();
    }

    /**
     * Make the _familyButtonPanel panel of buttons to select a family. Create
     * and set actions of radioButtons to change family on pane.
     * 
     * @param keySet of icon family names
     */
    protected void makeFamilyButtons(java.util.Set<String> keySet) {
        if (_familyButtonPanel == null) {
            _familyButtonPanel = new JPanel(); // this is only a local object
            _familyButtonPanel.setLayout(new BoxLayout(_familyButtonPanel, BoxLayout.Y_AXIS));
        } else {
            _familyButtonPanel.removeAll();
        }
        log.debug("makeFamilyButtons for {} family= {}", _itemType, _family);
        String thisType = null;
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
            thisType = "RPSreporter"; // adapt for slightly different spelling
                                      // of Bundle key (2nd r lower case)
        } else {
            thisType = _itemType;
        }
        String txt = Bundle.getMessage("IconFamiliesLabel", Bundle.getMessage(thisType));
        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel(txt));
        p.setOpaque(false);
        _familyButtonPanel.add(p);
        _familyButtonGroup = new ButtonGroup();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = ItemPanel.itemGridBagConstraint();

        int numCol = 4;
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(gridbag);
        String family = "";
        int length = 0;
        JRadioButton button = null;

        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            family = iter.next();
            length += family.length();
            button = new JRadioButton(family);
            addFamilyButtonListener(button, family);
            if (family.equals(_family)) {
                button.setSelected(true);
            }
            log.debug("{} ActionListener and button for family \"{}\" at gridx= {} gridy= {}", _itemType, family,
                    c.gridx, c.gridy);
            gridbag.setConstraints(button, c);
            buttonPanel.add(button, c);
            if (c.gridx >= numCol || length > 50) { // start next row
                c.gridy++;
                c.gridx = 0;
                length = 0;
            }
            c.gridx++;
        }
        if (button != null && _family == null) {
            button.setSelected(true);
            _family = family;
        } else if (_family != null && !keySet.contains(_family)) {
            button = new JRadioButton(_family);
            addFamilyButtonListener(button, _family);
            log.debug("\"{}\" ActionListener and button for family \"{}\" at gridx= {} gridy= {}", _itemType, _family,
                    c.gridx, c.gridy);
            gridbag.setConstraints(button, c);
            buttonPanel.add(button, c);
            button.setSelected(true);
        }
        _familyButtonPanel.add(buttonPanel);
    }

    private void addFamilyButtonListener(JRadioButton button, String family) {
        button.addActionListener(new ActionListener() {
            String fam;

            @Override
            public void actionPerformed(ActionEvent e) {
                setFamily(fam);
            }

            ActionListener init(String f) {
                fam = f;
                return this;
            }
        }.init(family));
        _familyButtonGroup.add(button);
    }

    /**
     * Position initial Preview component on _iconFamilyPanel. If already
     * present, keep and clear it.
     */
    protected void makeDragIconPanel() {
        if (_dragIconPanel == null) {
            _dragIconPanel = new ImagePanel();
            _dragIconPanel.setOpaque(true); // to show background color/squares
            _dragIconPanel.setLayout(new FlowLayout());
            _dragIconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            _dragIconPanel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        } else {
            _dragIconPanel.removeAll();
        }
        _dragIconPanel.setImage(_frame.getPreviewBackground()); 
        if (_iconPanel != null) {
            _iconPanel.setImage(_frame.getPreviewBackground());
        }
        _dragIconPanel.setVisible(true);
    }

    @Override
    protected JPanel makeIconDisplayPanel(String key, HashMap<String, NamedIcon> iconMap, boolean dropIcon) {
        NamedIcon icon = iconMap.get(key);
        JPanel panel = new JPanel();
        JLabel image;
        if (dropIcon) {
            image = new DropJLabel(icon, iconMap);
        } else {
            image = new JLabel(icon);
        }
        wrapIconImage(icon, image, panel, key);
        return panel;
    }

    protected JLabel getDragger(DataFlavor flavor, HashMap<String, NamedIcon> map, NamedIcon icon) {
        return null;
    }

    protected void makeDndIcon(HashMap<String, NamedIcon> iconMap) {
        JLabel label = null;
        NamedIcon icon = null;
        if (iconMap != null) {
            String displayKey = getDisplayKey();
            if (iconMap.get(displayKey) == null) {
                displayKey = (String) iconMap.keySet().toArray()[0];
            }
            icon = iconMap.get(displayKey);
            if (log.isDebugEnabled()) {
                log.debug("makeDndIcon for {}, {}. displayKey \"{}\" has icon {}",
                        _itemType, _family, displayKey, (icon != null));
            }
            if (icon != null) {
                icon = new NamedIcon(icon);
            }
            try {
                label = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR), iconMap, icon);
            } catch (java.lang.ClassNotFoundException cnfe) {
                log.warn("no DndIconPanel for {}, {} created. {}", _itemType, displayKey, cnfe);
                label = new JLabel("NO ICON");
            }
        } else {
            label = new JLabel("- - - - - -");
        }
        JPanel panel = makeDragIconPanel(label);
        _dragIconPanel.add(panel);
    }

    /**
     * Get the key to display the icon to be used for dragging to the panel
     * 
     * @return key for desired icon
     */
    abstract protected String getDisplayKey();

    private JPanel makeDragIconPanel(JLabel label) {
        JPanel panel = new JPanel(new FlowLayout());
        String borderName = Bundle.getMessage("dragToPanel");
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                borderName));
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        panel.setOpaque(false);
        if (label != null) {
            label.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
            // label.setIcon(icon);
            label.setName(borderName);
            label.setOpaque(false);
            panel.add(label);
        }
        Dimension dim = panel.getPreferredSize();
        dim.width = Math.max(CatalogPanel.ICON_WIDTH, dim.width) + 10;
        panel.setPreferredSize(dim);
        return panel;
    }

    @Override
    protected void hideIcons() {
        boolean isPalette = (_frame instanceof ItemPalette);
        Dimension totalDim = _frame.getSize();
        Dimension oldDim = getSize();
        if (_iconPanel != null) {
            _iconPanel.setVisible(false);
            _iconPanel.invalidate(); // force redraw
            if (_update) {
                _previewPanel.setVisible(false);
            }
            if (!_suppressDragging) {
                _dragIconPanel.setVisible(true);
                _dragIconPanel.invalidate();
                _previewPanel.setVisible(true);
                _instructions.setVisible(true);
            } else {
                _previewPanel.setVisible(false);
            }
            _previewPanel.invalidate(); // force redraw
        }
        reSizeDisplay(isPalette, oldDim, totalDim);
        _showIconsButton.setText(Bundle.getMessage("ShowIcons"));
        closeDialogs();
    }

    protected void showIcons() {
        boolean isPalette = (_frame instanceof ItemPalette);
        Dimension totalDim = _frame.getSize();
        Dimension oldDim = getSize();
        if (_iconPanel != null) {
            _iconPanel.setVisible(true);
            _iconPanel.invalidate(); // force redraw
            if (_update) {
                _previewPanel.setVisible(true);
            }
            if (!_suppressDragging) {
                _dragIconPanel.setVisible(false);
                _dragIconPanel.invalidate();
                _instructions.setVisible(false);
            } else {
                _previewPanel.setVisible(true);
            }
            _previewPanel.invalidate(); // force redraw
        }
        reSizeDisplay(isPalette, oldDim, totalDim);
        _showIconsButton.setText(Bundle.getMessage("HideIcons"));
        closeDialogs();
    }

    /**
     * Action item for deletion of an icon family.
     */
    protected void deleteFamilySet() {
        if (JOptionPane.showConfirmDialog(_frame, Bundle.getMessage("confirmDelete", _family),
                Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            ItemPalette.removeIconMap(_itemType, _family);
            _family = null;
            _currentIconMap = null;
            updateFamiliesPanel();
            setFamily(_family);
        }
    }

    protected boolean newFamilyDialog() {
        String family = JOptionPane.showInputDialog(_frame, Bundle.getMessage("EnterFamilyName"),
                Bundle.getMessage("createNewFamily", _itemType), JOptionPane.QUESTION_MESSAGE);
        if (family == null) {
            return false;
        }
        _family = null; // don't delete current family when adding new
        openDialog(_itemType, family);
        return true;
    }

    protected void openDialog(String type, String family) {
        closeDialogs();
        hideIcons();
        _dialog = new IconDialog(type, family, this);
        if (_family == null) {
            _dialog.setMap(null);
        } else {
            _dialog.setMap(_currentIconMap);
        }
    }

    /**
     * IconDialog calls this method to make any changes 'permanent'. It is
     * responsible for testing that the changes are valid.
     * 
     * @param family  family name, possibly changed
     * @param iconMap family map, possibly changed
     */
    protected void dialogDoneAction(String family, HashMap<String, NamedIcon> iconMap) {
        if (!_update && !(family.equals(_family) && mapsAreEqual(iconMap, _currentIconMap))) {
            ItemPalette.removeIconMap(_itemType, _family);
            ItemPalette.addFamily(_itemType, family, iconMap);
        } else {
            _currentIconMap = iconMap;
            if (!NamesStoredMap(family)) {
                _isUnstoredMap = true;
            }
            if (_isUnstoredMap) {
                _unstoredMap = iconMap;
            }
        }
        _family = family;
        makeFamiliesPanel();
        setFamily(family);
        if (log.isDebugEnabled()) {
            log.debug("dialogDoneAction done for {} {} update={}. unStored={}",
                    _itemType, _family, _update, _isUnstoredMap);
        }
    }

    protected boolean isUpdate() {
        return _update;
    }

    @Override
    public void closeDialogs() {
        if (_dialog != null) {
            _dialog.closeDialogs();
            _dialog.dispose();
            _dialog = null;
        }
    }

    public void dispose() {
        closeDialogs();
    }

    /**
     * Recover from cancelled Add Family dialog
     */
    protected void setFamily() {
        if (_familyButtonGroup == null) {
            return;
        }
        Enumeration<AbstractButton> en = _familyButtonGroup.getElements();
        while (en.hasMoreElements()) {
            AbstractButton button = en.nextElement();
            if (button.isSelected()) {
                _family = button.getText();
                break;
            }
        }
    }
    /**
     * Action of family radio button. MultiSensorItemPanel and
     * IndicatorTOItemPanel must override.
     *
     * @param family icon family name
     */
    protected void setFamily(String family) {
        _family = family;
        setFamilyMaps();
        _iconFamilyPanel.invalidate(); // force redraw
        hideIcons();
        setFamilyButton();
    }

    protected void setFamilyMaps() {
        _currentIconMap = ItemPalette.getIconMap(_itemType, _family);
        if (_currentIconMap == null) {
            _isUnstoredMap = true;
            _currentIconMap = _unstoredMap;
        }
        if (!_suppressDragging) {
            makeDragIconPanel();
            makeDndIcon(_currentIconMap);
        }
        addIconsToPanel(_currentIconMap, _iconPanel, false);
    }

    protected void setFamilyButton() {
        if (_familyButtonGroup == null) {
            return;
        }
        Enumeration<AbstractButton> en = _familyButtonGroup.getElements();
        while (en.hasMoreElements()) {
            AbstractButton button =  en.nextElement();
            if (_family != null && _family.equals(button.getText())) {
                button.setSelected(true);
                break;
            }
        }
    }

    @Override
    protected void previewColorChange() {
        if (_dialog != null) {
            ImagePanel iconPanel = _dialog.getIconEditPanel();
            if (iconPanel != null) {
                iconPanel.setImage(_frame.getPreviewBackground());
            }
            iconPanel = _dialog.getCatalogPreviewPanel();
            if (iconPanel != null) {
                iconPanel.setImage(_frame.getPreviewBackground());
            }
        }
        super.previewColorChange();
    }

    /**
     * Create icon set to panel icon display class.
     *
     * @return updated icon map
     */
    public HashMap<String, NamedIcon> getIconMap() {
        if (_currentIconMap != null) {
            return _currentIconMap;
        }
        HashMap<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
        if (map == null) {
            map = _unstoredMap;
        }
        if (map == null) {
            log.warn("Family \"{}\" for type \"{}\" not found.", _family, _itemType);
            map = ItemPanel.makeNewIconMap(_itemType);
        }
        return map;
    }

    public String getFamilyName() {
        return _family;
    }

    private final static Logger log = LoggerFactory.getLogger(FamilyItemPanel.class);

}
