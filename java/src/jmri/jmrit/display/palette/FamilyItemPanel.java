package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
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
import jmri.jmrit.catalog.CatalogPanel;
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
 * @author Pete Cressman Copyright (c) 2010, 2011, 2018
 * @author Egbert Broerse 2017
 */
public abstract class FamilyItemPanel extends ItemPanel {

    protected String _family;
    protected JPanel _iconFamilyPanel;  // Holds preview of _iconPanel & _dragIconPanel. They alternate being empty
    protected ImagePanel _dragIconPanel; // a panel on _iconFamilyPanel - to drag to control panel, hidden upon [Show Icons]
    protected ImagePanel _iconPanel;     // a panel on _iconFamilyPanel - all icons in family, shown upon [Show Icons]
    protected JPanel _previewPanel;
    protected JPanel _familyButtonPanel;    // panel of radioButtons to select icon family

    protected int _buttonPosition = 0;      // position of _iconFamilyPanel (TableItemPanels use 1)
    JPanel _bottom1Panel; // typically displays the _showIconsButton and _editIconsButton
    JPanel _bottom2Panel; // createIconFamilyButton - when all families have been deleted
    JButton _showIconsButton;
    JButton _editIconsButton;
    JButton _updateButton;
    private HashMap<String, NamedIcon> _currentIconMap;
    private HashMap<String, NamedIcon> _unstoredMap;
    IconDialog _dialog;
    ButtonGroup _familyButtonGroup;

    protected static boolean _suppressNamePrompts = false;
    protected boolean _isUnstoredMap;

    /**
     * Constructor types with multiple families and multiple icon families.
     *
     * @param parentFrame   enclosing parentFrame
     * @param type          bean type
     * @param family        icon family
     * @param editor        panel editor
     */
    public FamilyItemPanel( DisplayFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, editor);
        _family = family;
    }

    /**
     * Create a FamilyIconPanel.
     * Also called by the enclosing TabbedPanel on change of displayed tab Pane to activate a different pane.
     */
    @Override
    public void init() {
        if (!_initialized) {
            _update = false;
            _suppressDragging = false;
            initIconFamiliesPanel();
            add(_iconFamilyPanel);
            makeBottomPanel(null);
            super.init();
            log.debug("init done for {}, family= {}", _itemType, _family);
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
        _update = true;
        _suppressDragging = true; // no dragging when updating
        if (iconMap != null) {
            checkCurrentMap(iconMap); // is map in families?, does user want to add it? etc.
        }
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
        makeBottomPanel(doneAction);
    }

    /**
     * Initialization for conversion of plain track to indicator track by CircuitBuilder.
     *
     * @param doneAction doneAction
     */
    @Override
    public void init(ActionListener doneAction) {
        _update = false;
        _suppressDragging = true; // no dragging in circuitBuilder
        _bottom1Panel = new JPanel();
        addShowButtonToBottom();
        addUpdateButtonToBottom(doneAction);
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
        add(_bottom1Panel);
        _initialized = true;
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
           }
        });
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteFamily"));
        _bottom1Panel.add(deleteButton);
    }

    /**
     * Check whether map is one of the families.
     * If so, return. If not, does user want to add it to families?
     * If so, add. If not, save for return when updated.
     * update ctor has entered a name for _family.
     *
     * @param iconMap existing map of the icon
     */
    private void checkCurrentMap(HashMap<String, NamedIcon> iconMap) {
        log.debug("checkCurrentMap: for type \"{}\", family \"{}\"", _itemType, _family);
        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        String family = findFamilyOfMap(iconMap, families);
        if (family != null) {  // icons same as a known family, maybe with another name
            if (family.equals(_family)) {
                return;
            }
            log.debug("Icon's family \"{}\" found but is called \"{}\".  Change to Catalog name.", _family, family);
            _family = family;
        } else {    // icon set not in catalog
            _unstoredMap = iconMap;
            if (_family == null || _family.trim().length() == 0) { 
                if (_suppressNamePrompts) {
                   _family = null;  // user doesn't want to be bothered
                   return;
               }
                _paletteFrame.setLocation(jmri.util.PlaceWindow.nextTo(_editor, null, _paletteFrame));
               _family = JOptionPane.showInputDialog(_paletteFrame, Bundle.getMessage("NoFamilyName"),
                        Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE);
            }
            if (_family != null && _family.trim().length() > 0) {
                // make sure name does not duplicate a known name
                Iterator<String> it = families.keySet().iterator();
                while (!ItemPalette.familyNameOK(_paletteFrame, _itemType, _family, it)) {
                    _family = JOptionPane.showInputDialog(_paletteFrame, Bundle.getMessage("EnterFamilyName"),
                            Bundle.getMessage("createNewIconSet", _itemType), JOptionPane.QUESTION_MESSAGE);
                    if (_family == null) {
                        return;  // user cancelled
                    }
                }
                log.debug("family name \"{}\"", _family);
                // name OK
                if (_suppressNamePrompts) {
                    return;     // user not interested in updating catalog
                }
                int result = JOptionPane.showConfirmDialog(_paletteFrame,
                        Bundle.getMessage("UnkownFamilyName", _family), Bundle.getMessage("QuestionTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    if (!ItemPalette.addFamily(_paletteFrame, _itemType, _family, iconMap)) {
                        JOptionPane.showMessageDialog(_paletteFrame,
                                Bundle.getMessage("badName", _family, _itemType),
                                Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
//                    } else {    // icon set added to catalog with name _family
//                        _unstoredMap = null;
                    }
                } else if (result == JOptionPane.NO_OPTION) {
                    _suppressNamePrompts = true;
                }
           }
        }
    }
    
    protected String getValidFamilyName(String family) {
        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        Iterator<String> it = families.keySet().iterator();
        while (!ItemPalette.familyNameOK(_paletteFrame, _itemType, family, it)) {
            family = JOptionPane.showInputDialog(_paletteFrame, Bundle.getMessage("EnterFamilyName"),
                    Bundle.getMessage("createNewIconSet", _itemType), JOptionPane.QUESTION_MESSAGE);
            if (family == null) {
                return null;  // user cancelled
            }
        }
        log.debug("getValidFamilyName = \"{}\"", family);
        return family;
    }

    protected boolean addFamily(String type, String family, HashMap<String, NamedIcon> iconMap) {
        if (!ItemPalette.addFamily(_paletteFrame, type, family, iconMap)) {
            JOptionPane.showMessageDialog(_paletteFrame,
                    Bundle.getMessage("badName", _family, _itemType),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else {
            setIconMap(iconMap);
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
            log.debug("FamilyKey = {}", entry.getKey());
            if (mapsAreEqual(entry.getValue(), iconMap)) {
                String family = entry.getKey();
                log.debug("Icon map found with different name \"{}\"", family);
                return family;
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
            _familyButtonPanel = makeFamilyButtons(families.keySet());
            if (_currentIconMap == null) {
                _currentIconMap = families.get(_family);
                if (_currentIconMap == null) {
                    _isUnstoredMap = true;
                    _currentIconMap = _unstoredMap;
                }
            }
            // make _iconPanel & _dragIconPanel before calls to add icons
            addFamilyPanels(_familyButtonPanel);
            if (_currentIconMap == null) {
                log.error("currentIconMap is null in initIconFamiliesPanel");
            } else {
                addIconsToPanel(_currentIconMap, _iconPanel, false); // need to have family iconMap identified before calling
                makeDndIconPanel(_currentIconMap, "BeanStateUnknown");
            }
        } else {
            familiesMissing();
        }
        log.debug("initIconFamiliesPanel update = {}, family = {}", _update, _family);
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
        reset();
    }

    /**
     * Create and set actions of radioButtons to change family on pane.
     * @param keySet of icon family names
     * @return family button panel
     */
    protected JPanel makeFamilyButtons(java.util.Set<String> keySet) {
        Iterator<String> iter = keySet.iterator();
        log.debug("makeFamilyButtons for {} family= {}", _itemType, _family);
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
        
        GridBagLayout gridbag = new GridBagLayout();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(gridbag);

        int numCol = 4;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        String family = "";
        JRadioButton button = null;
        while (iter.hasNext()) {
            family = iter.next();
            button = new JRadioButton(ItemPalette.convertText(family));
            addFamilyButtonListener(button, family);
            log.debug("\"{}\" ActionListener and button for family \"{}\" at gridx= {} gridy= {}", _itemType, family, c.gridx, c.gridy);
            if (family.equals(_family)) {
                button.setSelected(true);
            }
            gridbag.setConstraints(button, c);
            buttonPanel.add(button);
            c.gridx++;
            if (c.gridx >= numCol) { //start next row
                c.gridy++;
                c.gridx = 0;
            }
        }
        if (_currentIconMap == null) {
            if (_unstoredMap !=null) {
                if (_family == null) {
                    _family = Bundle.getMessage("unNamed");
                }
                _isUnstoredMap = true;
                _currentIconMap = _unstoredMap;
            } else if(_family == null || _family.trim().length()==0) {
                _family = family; // let last family be the selected one
                if (button != null) {
                    button.setSelected(true);
                }
            }
        }
        if (!keySet.contains(_family)) {
            button = new JRadioButton(_family);
            addFamilyButtonListener(button, _family);
            log.debug("\"{}\" ActionListener and button for family \"{}\" at gridx= {} gridy= {}", _itemType, _family, c.gridx, c.gridy);
            gridbag.setConstraints(button, c);
            buttonPanel.add(button);
            button.setSelected(true);
            if (_unstoredMap == null) {
                _unstoredMap = _currentIconMap;
                _isUnstoredMap = true;
            }
        }
        familyPanel.add(buttonPanel);
        return familyPanel;
    }
    
    private void addFamilyButtonListener (JRadioButton button, String family) {
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
     * Position secondary Preview component on _iconFamilyPanel (visible after [Show Icons]).
     * @param familyPanel panel of family buttons
     */
    protected void addFamilyPanels(JPanel familyPanel) {
        log.debug("addFamilyPanels for {}", _itemType);
        boolean makeBgBoxPanel = false;
        if (_iconPanel == null) { // don't overwrite existing _iconPanel
            _iconPanel = new ImagePanel();
            _iconPanel.setLayout(new FlowLayout());
            _iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            makeBgBoxPanel = true;
        }

        if (!_suppressDragging) {
            makeDragIconPanel(0);
        }
        if (makeBgBoxPanel) {
            if (!_update && !_suppressDragging) {
                _previewPanel = makePreviewPanel(_iconPanel, _dragIconPanel);
            } else {
                _previewPanel = makePreviewPanel(_iconPanel, null);
                _previewPanel.setVisible(false);
            }
            _iconFamilyPanel.add(_previewPanel);
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
        _iconPanel.setVisible(false);
        log.debug("addFamilyPanels for {} update={}", _family, _update);
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
            _dragIconPanel.setImage(_backgrounds[previewBgSet]); // pick up shared setting
            if (_iconPanel != null) {
                _iconPanel.setImage(_backgrounds[previewBgSet]); // pick up shared setting
            }
        } else {
            log.error("FamilyItemPanel - no value for previewBgSet");
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

    /**
     * Add family icons to Show Icons pane in Add Item tool.
     * @see #hideIcons()
     *
     * @param iconMap hashmap of icons currently active
     * @param iconPanel panel to fill with isons
     * @param dropIcon true for ability to drop new image on icon to change icon source
     */
    protected void addIconsToPanel(HashMap<String, NamedIcon> iconMap, ImagePanel iconPanel, boolean dropIcon) {
        if (iconMap == null) {
            log.debug("iconMap is null for type {} family {}", _itemType, _family);
            return;
        }
        if (iconPanel == null) { // bug for SignalMast icons (is of class ImagePanel)
            log.error("iconPanel is null for type {}", _itemType);
            return;
        }
        iconPanel.setOpaque(false);
        GridBagLayout gridbag = new GridBagLayout();
        iconPanel.setLayout(gridbag);
        FontMetrics fm = getFontMetrics(iconPanel.getFont());

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
        log.debug("adding {} icons to panel iconMap", cnt);
        for (Entry<String, NamedIcon> entry : iconMap.entrySet()) {
            NamedIcon icon = new NamedIcon(entry.getValue()); // make copy for possible reduction
            icon.reduceTo(100, 100, 0.2);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);
            // I18N use existing NamedBeanBundle keys
            String key = entry.getKey();
            if (log.isDebugEnabled()) {
                log.debug("add icon {} to Panel. key= {}", icon.getName(), key);
            }
            String borderName = getIconBorderName(key);
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), borderName));
            JLabel image;
            if (dropIcon) {
                image = new DropJLabel(icon, iconMap, _update);
            } else {
                image = new JLabel(icon);
            }
            double scale;
            if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                image.setText(Bundle.getMessage("invisibleIcon"));
                scale = 0;
            } else {
                scale = icon.reduceTo(CatalogPanel.ICON_WIDTH, CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
            }
            image.setOpaque(false);
            image.setToolTipText(icon.getName());
            image.setName(key);
            JPanel iPanel = new JPanel();
            iPanel.setOpaque(false);
            iPanel.add(image);
            panel.add(iPanel);

            String scaleText = java.text.MessageFormat.format(Bundle.getMessage("scale"),
                    new Object[]{CatalogPanel.printDbl(scale, 2)});
            JLabel label = new JLabel(scaleText);
            JPanel sPanel = new JPanel();
            sPanel.setOpaque(false);
            sPanel.add(label);
            panel.add(sPanel);
            int width = getFontMetrics(getFont()).stringWidth(borderName) + 10;
            width = Math.max(fm.stringWidth(scaleText), Math.max(width, CatalogPanel.ICON_WIDTH));
            int height = panel.getPreferredSize().height;
            panel.setPreferredSize(new Dimension(width, height));
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
//                    log.debug("addIconsToPanel: gridx = {} gridy = {}", c.gridx, c.gridy);
                    iconPanel.add(p);
                    c.gridx = 1;
                }
            }
            cnt--;
            gridbag.setConstraints(panel, c);
            iconPanel.add(panel);
        }
        log.debug("addIconsToPanel for type {} family \"{}\"", _itemType, _family);
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
                width = Math.max(CatalogPanel.ICON_WIDTH, Math.max(width, icon.getIconWidth())+10);
                panel.setPreferredSize(new Dimension(width, panel.getPreferredSize().height));
                _dragIconPanel.add(panel);
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
        log.debug("hideIcons for= {}, {}", _itemType, _family);
        boolean isPalette = (_paletteFrame instanceof ItemPalette); 
        Dimension totalDim;
        if (isPalette) {
            totalDim = ItemPalette._tabPane.getSize();
        } else {
            totalDim = _paletteFrame.getSize();            
        }
        Dimension oldDim = getSize();
        if (_update) {
            _previewPanel.setVisible(false);
            _previewPanel.invalidate(); // force redraw
        }
        _iconPanel.setVisible(false);
        _iconPanel.invalidate(); // force redraw
        if (!_suppressDragging) {
            _dragIconPanel.setVisible(true);
            _dragIconPanel.invalidate();
        } else {
            _previewPanel.setVisible(false);
            _previewPanel.invalidate(); // force redraw
        }
        reSizeDisplay(isPalette, oldDim, totalDim);
        _showIconsButton.setText(Bundle.getMessage("ShowIcons"));
        reset();
    }
    
    protected void showIcons() {
        boolean isPalette = (_paletteFrame instanceof ItemPalette); 
        Dimension totalDim;
        if (isPalette) {
            totalDim = ItemPalette._tabPane.getSize();
        } else {
            totalDim = _paletteFrame.getSize();            
        }
        Dimension oldDim = getSize();
        if (_update) {
            _previewPanel.setVisible(true);
            _previewPanel.invalidate(); // force redraw
        }
        _iconPanel.setVisible(true);
        _iconPanel.invalidate(); // force redraw
        if (!_suppressDragging) {
            _dragIconPanel.setVisible(false);
            _dragIconPanel.invalidate();
        } else {
            _previewPanel.setVisible(true);
            _previewPanel.invalidate(); // force redraw
        }
        if (log.isDebugEnabled()) {
            log.debug("showIcons for= {}, {}. oldDim= ({}, {}) totalDim= ({}, {})",
                    _itemType, _family, oldDim.width, oldDim.height, totalDim.width, totalDim.height);
        }
        reSizeDisplay(isPalette, oldDim, totalDim);
        _showIconsButton.setText(Bundle.getMessage("HideIcons"));
        reset();
    }

    /**
     * Action item for deletion of an icon family.
     */
    protected void deleteFamilySet() {
        if (JOptionPane.showConfirmDialog(_paletteFrame, Bundle.getMessage("confirmDelete", _family),
                Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION) {
            ItemPalette.removeIconMap(_itemType, _family);
            _family = null;
            _currentIconMap = null;
            updateFamiliesPanel();
        }
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
            ImagePanel iconPanel = _dialog.getIconEditPanel();
            if (iconPanel != null) {
                iconPanel.setImage(_backgrounds[index]);
            }
            iconPanel = _dialog.getCatalogPreviewPanel();
            if (iconPanel != null) {
                iconPanel.setImage(_backgrounds[index]);
            }
        }
        if (_iconPanel != null) {
            _iconPanel.setImage(_backgrounds[index]);      
        }
    }

    @Override
    protected void updateBackground0(BufferedImage im) {
        _backgrounds[0] = im;
    }

    protected void openDialog(String type, String family, HashMap<String, NamedIcon> iconMap) {
        closeDialogs();
        _dialog = new IconDialog(type, family, this, iconMap);
    }

    @Override
    public void closeDialogs() {
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
            log.error("setFamily called with _iconPanel == null type = {}", _itemType);
        } else {
            _iconPanel.removeAll(); // just clear contents
        }
        HashMap<String, NamedIcon> map = ItemPalette.getIconMap(_itemType, _family);
        if (map == null) {
            map = _unstoredMap;
            _isUnstoredMap = true;
        } else {
            _isUnstoredMap = false;
        }
        if (map != null) {
            _currentIconMap = map;
            log.debug("setFamily: {} family \"{}\" map has {} icons", _itemType, _family, map.size());
        } else {
            log.warn("Family \"{}\" for type \"{}\" for not found in Catalog.", _family, _itemType);                
        }
        if (!_suppressDragging) {
            makeDragIconPanel(0);
            makeDndIconPanel(_currentIconMap, "BeanStateUnknown");
        }
        addIconsToPanel(_currentIconMap, _iconPanel, false);
        _iconFamilyPanel.invalidate(); // force redraw
        hideIcons();
        setFamilyButton();
    }
    
    protected boolean isUnstoredMap() {
        return _isUnstoredMap;
    }
    
    protected void setFamilyButton() {
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
            if (visible) {
                _showIconsButton.setText(Bundle.getMessage("HideIcons"));
            } else {
                _showIconsButton.setText(Bundle.getMessage("ShowIcons"));
            }
        }
    }

    protected void setIconMap(HashMap<String, NamedIcon> map) {
        _currentIconMap = map;
        if (_isUnstoredMap) {
            _unstoredMap = map;
        }
        log.debug("setIconMap: for {} \"{}\" _isUnstoredMap={}", _itemType, _family, _isUnstoredMap);
        updateFamiliesPanel();
    }
    /**
     * Create icon set to panel icon display class.
     *
     * @return updated icon map
     */
    public HashMap<String, NamedIcon> getIconMap() {
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
