package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.PreviewPanel;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.util.swing.ImagePanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanels for the various item types that can be added to a Panel - e.g. Sensors,
 * Turnouts, etc.
 *
 * Devices such as these have sets of icons to display their various states.
 * Such sets are called a "family" in the code. These devices then may have sets
 * of families to provide the user with a choice of the icon set to use for a
 * particular device.
 * These sets/families are defined in an xml file stored as xml/defaultPanelIcons.xml
 * including the icon file paths, to be loaded by an iterator.
 * The subclass FamilyItemPanel.java and its subclasses handles these devices.
 *
 * Other devices, e.g. Backgrounds or Memory, may use only one or no icon to
 * display. The subclass IconItemPanel.java and its subclasses handles these
 * devices.
 * @see jmri.jmrit.display.DisplayFrame for class diagram for the palette package.
 *
 * @author Pete Cressman Copyright (c) 2010, 2020
 * @author Egbert Broerse Copyright 2017, 2021
 */
public abstract class ItemPanel extends JPanel  {

    protected DisplayFrame _frame;
    protected String _itemType;
    protected boolean _initialized = false; // has init() been run
    protected boolean _update = false;      // editing existing icon, do not allow icon dragging. Set in init()
    protected boolean _suppressDragging;
    protected boolean _askOnce = false;
    protected JTextField _linkName = new JTextField(30);
    protected PreviewPanel _previewPanel; // contains _iconPanel and optionally _dragIconPanel when used to create a panel object
    protected HashMap<String, NamedIcon> _currentIconMap;
    protected ImagePanel _iconPanel;   // a panel on _iconFamilyPanel - all icons in family, shown upon [Show Icons]
    protected JPanel _iconFamilyPanel; // Holds _previewPanel, _familyButtonPanel.
    protected JPanel _bottomPanel; // contains function buttons for panel
    protected ActionListener _doneAction;   // update done action return
    protected boolean _wasEmpty;
    protected JPanel _instructions;

    /*
     * ****** Default family icon names *******
     *
     * NOTE: Names supplied must be available as properties keys and also match the
     * element names defined in xml/defaultPanelIcons.xml
     */
    static final String[] TURNOUT = {"TurnoutStateClosed", "TurnoutStateThrown",
            "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] SENSOR = {"SensorStateActive", "SensorStateInactive",
            "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] SIGNALHEAD = {"SignalHeadStateRed", "SignalHeadStateYellow",
            "SignalHeadStateGreen", "SignalHeadStateDark",
            "SignalHeadStateHeld", "SignalHeadStateLunar",
            "SignalHeadStateFlashingRed", "SignalHeadStateFlashingYellow",
            "SignalHeadStateFlashingGreen", "SignalHeadStateFlashingLunar"};
    static final String[] LIGHT = {"StateOff", "StateOn",
            "BeanStateInconsistent", "BeanStateUnknown"};
    static final String[] MULTISENSOR = {"SensorStateInactive", "BeanStateInconsistent",
            "BeanStateUnknown", "first", "second", "third"};
    // SIGNALMAST family is empty is signal system
    static final String[] RPSREPORTER = {"active", "error"};
    final static String[] INDICATOR_TRACK = {"ClearTrack", "OccupiedTrack", "PositionTrack",
            "AllocatedTrack", "DontUseTrack", "ErrorTrack"};
    static final String[] PORTAL = {PortalIcon.HIDDEN, PortalIcon.VISIBLE, PortalIcon.PATH,
            PortalIcon.TO_ARROW, PortalIcon.FROM_ARROW};

    protected static HashMap<String, String[]> STATE_MAP = new HashMap<>();
    static {
        STATE_MAP.put("Turnout", TURNOUT);
        STATE_MAP.put("Sensor", SENSOR);
        STATE_MAP.put("SignalHead", SIGNALHEAD);
        STATE_MAP.put("Light", LIGHT);
        STATE_MAP.put("MultiSensor", MULTISENSOR);
        STATE_MAP.put("RPSReporter", RPSREPORTER);
        STATE_MAP.put("IndicatorTrack", INDICATOR_TRACK);
        STATE_MAP.put("IndicatorTO", INDICATOR_TRACK);
        STATE_MAP.put("Portal", PORTAL);
    }

    protected static HashMap<String, String> NAME_MAP = new HashMap<>();
    static {
        NAME_MAP.put("Turnout", "BeanNameTurnout");
        NAME_MAP.put("Sensor", "BeanNameSensor");
        NAME_MAP.put("SignalHead", "BeanNameSignalHead");
        NAME_MAP.put("Light", "BeanNameLight");
        NAME_MAP.put("SignalMast", "BeanNameSignalMast");
        NAME_MAP.put("MultiSensor", "MultiSensor");
        NAME_MAP.put("Memory", "BeanNameMemory");
        NAME_MAP.put("Reporter", "BeanNameReporter");
        NAME_MAP.put("RPSReporter", "RPSreporter");
        NAME_MAP.put("IndicatorTrack", "IndicatorTrack");
        NAME_MAP.put("IndicatorTO", "IndicatorTO");
        NAME_MAP.put("Portal", "BeanNamePortal");
        NAME_MAP.put("Icon", "Icon");
        NAME_MAP.put("Background", "Background");
        NAME_MAP.put("Text", "Text");
        NAME_MAP.put("FastClock", "FastClock");
    }

    /**
     * Constructor for all item types.
     *
     * @param parentFrame ItemPalette instance
     * @param type        identifier of the ItemPanel type
     */
    public ItemPanel(DisplayFrame parentFrame, @Nonnull String type) {
        _frame = parentFrame;
        _itemType = type;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
    }

    /**
     * Initialize panel for selecting a new Control Panel item or for updating
     * an existing item. Adds table if item is a bean. i.e. customizes for the
     * item type.
     * Called by enclosing TabbedPanel on change of displayed tab Pane.
     */
    public void init() {
        if (!_initialized) {
            _update = false;
            _suppressDragging = false;
            initIconFamiliesPanel();
            _initialized = true;
        }
    }

    @Nonnull
    protected HashMap<String, NamedIcon> makeNewIconMap(String type) {
        HashMap<String, NamedIcon> newMap = new HashMap<>();
        for (String name : STATE_MAP.get(type)) {
            NamedIcon icon = new NamedIcon(ItemPalette.RED_X, ItemPalette.RED_X);
            newMap.put(name, icon);
        }
        return newMap;
    }

    static protected void checkIconMap(String type, HashMap<String, NamedIcon> map) {
        for (String name : STATE_MAP.get(type)) {
            if (map.get(name) == null) {
                NamedIcon icon = new NamedIcon(ItemPalette.RED_X, ItemPalette.RED_X);
                // store RedX as default icon if icon not set
                map.put(name, icon);
            }
        }
    }

    protected void previewColorChange() {
        if (_previewPanel != null) {
            _previewPanel.setBackgroundSelection(_frame.getPreviewBg());
            _previewPanel.invalidate();
        }
    }

    public void closeDialogs() {
    }

    /**
     * Make a button panel that can populate an empty ItemPanel
     * @param update edit icons on a panel
     * @return the panel
     */
    abstract protected JPanel makeSpecialBottomPanel(boolean update);

    /**
     * Make a button panel to populate editing an ItemPanel
     * @return the panel
     */
    abstract protected JPanel makeItemButtonPanel();

    /**
     * Add [Update] button to _bottom1Panel.
     * @param doneAction Action for button
     * @return button with doneAction Action
     */
    protected JButton makeUpdateButton(ActionListener doneAction) {
        JButton updateButton = new JButton(Bundle.getMessage("updateButton")); // custom update label
        updateButton.addActionListener(doneAction);
        updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        return updateButton;
    }


    protected void makeBottomPanel(boolean isEmpty) {
        if (isEmpty) {
            _bottomPanel = makeSpecialBottomPanel(_update);
        } else {
            _bottomPanel = makeItemButtonPanel();
        }
        if (_doneAction != null) {
            _bottomPanel.add(makeUpdateButton(_doneAction));
        }
        _bottomPanel.invalidate();
        add(_bottomPanel);
    }

    /**
     * Initialize or reset an ItemPanel.
     */
    protected void initIconFamiliesPanel() {
        if (_iconPanel == null) {
            _iconPanel = new ImagePanel();
            _iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            _iconPanel.setImage(_frame.getPreviewBackground());
            _iconPanel.setOpaque(false);
        }
        if (_iconFamilyPanel == null) {
            _iconFamilyPanel = new JPanel();
            _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
            add(_iconFamilyPanel);
        }
        makeFamiliesPanel();
        if (log.isDebugEnabled()) {
            log.debug("initIconFamiliesPanel done for {}, update= {}", _itemType, _update);
        }
    }

    protected void makePreviewPanel(boolean hasMaps, ImagePanel dragIconPanel) {
        if (_previewPanel == null) {
            if (!_update && !_suppressDragging) {
                _previewPanel = new PreviewPanel(_frame, _iconPanel, dragIconPanel, true);
                _instructions = instructions();
                _previewPanel.add(_instructions, 0);
            } else {
                _previewPanel = new PreviewPanel(_frame, _iconPanel, null, false);
                _previewPanel.setVisible(false);
            }
            _iconFamilyPanel.add(_previewPanel);
        }
        _previewPanel.setVisible(true);
        _previewPanel.invalidate();
    }

    /**
     * Add the current set of icons to a Show Icons pane. Used in several
     * ways by different ItemPanels. 
     * When dropIcon is true, call may be from an editing dialog and the
     * caller may allow the icon to dropped upon (replaced) or be the
     * source of dragging it - (e.g. IconItemPanel). When_showIconsButton 
     * pressed, dropIcon will be false.
     * 
     * @see #hideIcons()
     * @param iconMap   family maps
     * @param iconPanel panel to fill with icons
     * @param dropIcon  true for ability to drop new image on icon to change
     *                  icon source
     */
    protected void addIconsToPanel(HashMap<String, NamedIcon> iconMap, ImagePanel iconPanel, boolean dropIcon) {
        if (iconMap == null) {
            log.debug("_currentIconMap is null for type {}", _itemType);
            return;
        }
        iconPanel.removeAll();

        GridBagLayout gridbag = new GridBagLayout();
        iconPanel.setLayout(gridbag);

        int numCol = 4;
        GridBagConstraints c = ItemPanel.itemGridBagConstraint();

        if (iconMap.isEmpty()) {
            iconPanel.add(Box.createRigidArea(new Dimension(70,70)));
        }
        int cnt = 0;
        for (String key : iconMap.keySet()) {
            JPanel panel = makeIconDisplayPanel(key, iconMap, dropIcon);
            
            iconPanel.add(panel, c);
            if (c.gridx > numCol) { // start next row
                c.gridy++;
                c.gridx = 0;
            }
            c.gridx++;
            cnt++;
            gridbag.setConstraints(panel, c);
        }
        if (log.isDebugEnabled()) {
            log.debug("addIconsToPanel adds {} icons (map size {}) to iconPanel for {}", cnt, iconMap.size(), _itemType);
        }
        iconPanel.invalidate();
    }

    /**
     * Utility for above method. Implementation returns a JPanel extension
     * containing a bordered JLabel extension of icon and labels
     * 
     * @param key name of icon
     * @param iconMap containing icon for possible replacement
     * @param dropIcon JLabel extension may be replaceable or dragable.
     * @return the JPanel
     */
    abstract protected JPanel makeIconDisplayPanel(String key, HashMap<String, NamedIcon> iconMap, boolean dropIcon);

    /**
     * Utility used by implementations of above 'makeIconDisplayPanel' method to wrap its panel
     * @param icon icon held by a JLabel
     * @param image background image for panel
     * @param panel holds image and JLable
     * @param key key of icon in its set - name for the icon can be extracted from it
     */
    protected void wrapIconImage(NamedIcon icon, JLabel image, JPanel panel, String key) {
        String borderName = ItemPalette.convertText(key);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        // I18N use existing NamedBeanBundle keys
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), borderName));
        image.setOpaque(false);
        image.setToolTipText(icon.getName());
        image.setName(key);
        JPanel iPanel = new JPanel();
        iPanel.setOpaque(false);
        iPanel.add(image);
        panel.add(iPanel);

        double scale;
        if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
            image.setText(Bundle.getMessage("invisibleIcon"));
            scale = 0;
        } else {
            scale = icon.reduceTo(CatalogPanel.ICON_WIDTH, CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
        }
        String scaleText = java.text.MessageFormat.format(Bundle.getMessage("scale"), CatalogPanel.printDbl(scale, 2));
        JLabel label = new JLabel(scaleText);
        JPanel sPanel = new JPanel();
        sPanel.setOpaque(false);
        sPanel.add(label);
        panel.add(sPanel);       

        FontMetrics fm = getFontMetrics(panel.getFont());
        int width = fm.stringWidth(borderName) + 5;
        width = Math.max(Math.max(width, CatalogPanel.ICON_WIDTH), icon.getIconWidth() + 5);
        int height = panel.getPreferredSize().height;
        panel.setPreferredSize(new Dimension(width, height));
    }

    abstract protected JPanel instructions();

    /**
     * Part of the initialization and reseting of an ItemPanel.
     * Allows divergence for different panel needs.
     */
    abstract protected void makeFamiliesPanel();

    abstract protected void hideIcons();
    
    /**
     * See if the map is supported by the family map. "Equals" in
     * this context means that each map is the same size the keys are equal and
     * the urls for the icons are equal. Note that icons with different urls may
     * be or appear to be the same.
     * The item type "SignalHead" allows for unequal sizes but 'mapOne'
     * must contain 'mapTwo' elements.
     * 
     * @param mapOne an icon HashMap
     * @param mapTwo another icon HashMap
     * @return true if all of signal head entries have matching entries in the
     *         family map.
     */
    protected boolean mapsAreEqual(HashMap<String, NamedIcon> mapOne, HashMap<String, NamedIcon> mapTwo) {
        if (  !_itemType.equals("SignalHead") && mapOne.size() != mapTwo.size()) {
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

    protected void loadDefaultType() {
        ItemPalette.loadMissingItemType(_itemType);
        // Check for duplicate names or duplicate icon sets
        java.util.ArrayList<String> deletes = new java.util.ArrayList<>();
        if (!_itemType.equals("IndicatorTO")) {
            HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
            java.util.Set<String> keys = families.keySet();
            String[] key = new String[keys.size()];
            key = keys.toArray(key);
            for (int i=0; i<key.length; i++) {
                for (int j=i+1; j<key.length; j++) {
                    HashMap<String, NamedIcon> mapK = families.get(key[i]);
                    if (mapsAreEqual(mapK, families.get(key[j]))) {
                        deletes.add(queryWhichToDelete(key[i], key[j]));
                        break;
                    }
                }
            }
            for (String k : deletes) {
                ItemPalette.removeIconMap(_itemType, k);
            }
            if (this instanceof FamilyItemPanel) {
                ((FamilyItemPanel)this)._family = null;
            }
        } else {
            IndicatorTOItemPanel p = (IndicatorTOItemPanel)this;
            HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> 
                                families = ItemPalette.getLevel4FamilyMaps(_itemType);
            java.util.Set<String> keys = families.keySet();
            String[] key = new String[keys.size()];
            key = keys.toArray(key);
            for (int i=0; i<key.length; i++) {
                for (int j=i+1; j<key.length; j++) {
                    HashMap<String, HashMap<String, NamedIcon>> mapK = families.get(key[i]);
                    if (p.familiesAreEqual(mapK, families.get(key[j]))) {
                        deletes.add(queryWhichToDelete(key[i], key[j]));
                        break;
                    }
                }
            }
            for (String k : deletes) {
                ItemPalette.removeLevel4IconMap(_itemType, k, null);
            }
            p._family = null;
        }
        if (!_initialized) {
            makeFamiliesPanel();
        } else {
            initIconFamiliesPanel();
            hideIcons();
        }
    }

    /**
     * Ask user to choose from 2 different names for the same icon map.
     * @param key1 first name found for same map
     * @param key2 second name found, default to delete
     * @return the name and map to discard
     */
    private String queryWhichToDelete(String key1, String key2) {
        int result = JOptionPane.showOptionDialog(this, Bundle.getMessage("DuplicateMap", key1, key2),
                Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, null,
                new Object[] {key1, key2}, key1);
        if (result == JOptionPane.YES_OPTION) {
            return key2;
        } else if (result == JOptionPane.NO_OPTION) {
            return key1;
        }
        return key2;
    }

    /**
     * Resize frame to allow display/shrink after Icon map is dieplayed.
     * @param isPalette selector for what to resize, true to resize parent tabbed frame
     * @param oldDim old panel size
     * @param frameDim old frame size
     */
    protected void reSizeDisplay(boolean isPalette, Dimension oldDim, Dimension frameDim) {
        Dimension newDim = getPreferredSize();
        Dimension deltaDim = shellDimension(this);
        if (log.isDebugEnabled()) {
            // Gather data for additional dimensions needed to display new panel in the total frame
            Dimension frameDiffDim = new Dimension(frameDim.width - oldDim.width, frameDim.height - oldDim.height);
            log.debug("resize {} {}. frameDiffDim= ({}, {}) deltaDim= ({}, {}) prefDim= ({}, {}))",
                    (isPalette?"tabPane":"update"), _itemType,
                    frameDiffDim.width, frameDiffDim.height,
                    deltaDim.width, deltaDim.height, newDim.width, newDim.height);
        }
        if (isPalette && _initialized) {
            _frame.reSize(ItemPalette._tabPane, deltaDim, newDim);
        } else if (_update || _initialized) {
            _frame.reSize(_frame, deltaDim, newDim);                            
        }
    }

    public Dimension shellDimension(ItemPanel panel) {
        if (panel instanceof FamilyItemPanel) {
            return new Dimension(23, 122);
        } else if (panel instanceof IconItemPanel) {
            return new Dimension(23, 65);
        }
        return new Dimension(23, 48);
    }

    static public GridBagConstraints itemGridBagConstraint() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        return c;
    }

    private final static Logger log = LoggerFactory.getLogger(ItemPanel.class);

}
