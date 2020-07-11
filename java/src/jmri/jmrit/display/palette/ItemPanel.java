package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;

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
 * @author Egbert Broerse Copyright 2017
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
    protected JPanel _bottom1Panel; // typically displays the _showIconsButton and _editIconsButton
    protected JPanel _bottom2Panel; // createIconFamilyButton - when all families have been deleted
    protected JPanel _instructions;
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
    }

    /**
     * Initialize panel for selecting a new Control Panel item or for updating
     * an existing item. Adds table if item is a bean. i.e. customizes for the
     * item type.
     * Called by enclosing TabbedPanel on change of displayed tab Pane.
     */
    public void init() {
        if (!_initialized) {
            add(Box.createVerticalGlue());
            _initialized = true;
        }
    }

    protected void previewColorChange() {
        if (_previewPanel != null) {
            _previewPanel.setBackgroundSelection(_frame.getPreviewBg());
            _previewPanel.invalidate();
        }
    }

    public boolean oktoUpdate() {
        return true;
    }

    public void closeDialogs() {
    }

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
    // SIGNALMAST family is empty for now
    static final String[] RPSREPORTER = {"active", "error"};
    static final String[] ICON = {"Icon"};
    static final String[] BACKGROUND = {"Background"};
    final static String[] INDICATOR_TRACK = {"ClearTrack", "OccupiedTrack", "PositionTrack",
            "AllocatedTrack", "DontUseTrack", "ErrorTrack"};
    static final String[] PORTAL = {PortalIcon.HIDDEN, PortalIcon.VISIBLE, PortalIcon.PATH,
            PortalIcon.TO_ARROW, PortalIcon.FROM_ARROW};

    @Nonnull
    static private String[] getNames(String type) {
        if (type.equals("Turnout")) {
            return TURNOUT;
        } else if (type.equals("Sensor")) {
            return SENSOR;
        } else if (type.equals("SignalHead")) {
            return SIGNALHEAD;
        } else if (type.equals("Light")) {
            return LIGHT;
        } else if (type.equals("MultiSensor")) {
            return MULTISENSOR;
        } else if (type.equals("Icon")) {
            return ICON;
        } else if (type.equals("Background")) {
            return BACKGROUND;
        } else if (type.equals("RPSReporter")) {
            return RPSREPORTER;
        } else if (type.equals("IndicatorTrack")) {
            return INDICATOR_TRACK;
        } else if (type.equals("IndicatorTO")) {
            return INDICATOR_TRACK;
        } else if (type.equals("Portal")) {
            return PORTAL;
        } else {
            log.error("Item type \"{}\" cannot create icon sets!", type);
            return new String[]{};
        }
    }

    @Nonnull
    static protected HashMap<String, NamedIcon> makeNewIconMap(String type) {
        HashMap<String, NamedIcon> newMap = new HashMap<>();
        String[] names = getNames(type);
        for (String name : names) {
            NamedIcon icon = new NamedIcon(ItemPalette.RED_X, ItemPalette.RED_X);
            newMap.put(name, icon);
        }
        return newMap;
    }

    static protected void checkIconMap(String type, HashMap<String, NamedIcon> map) {
        String[] names = getNames(type);
        for (String name : names) {
            if (map.get(name) == null) {
                NamedIcon icon = new NamedIcon(ItemPalette.RED_X, ItemPalette.RED_X);
                // store RedX as default icon if icon not set
                map.put(name, icon);
            }
        }
    }

    protected DisplayFrame getParentFrame() {
        return _frame;
    }

    /**
     * Make a button panel that can populate an empty ItemPanel
     * @param update edit icons on a panel
     */
    abstract protected void makeSpecialBottomPanel(boolean update);

    /**
     * Make a button panel to populate editing an ItemPanel
     */
    abstract protected void makeItemButtonPanel();

    /**
     * Add [Update] button to _bottom1Panel.
     * @param doneAction Action for button
     * @return button with doneAction Action
     */
    abstract protected JButton makeUpdateButton(ActionListener doneAction);

    /**
     * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel
     * depending on whether icon families exist. _bottom1Panel typically
     * has button for further editing of a chosen icon family. _bottom2Panel
     * is for the exceptional case where there are no families at all.
     * <p>
     * Subclasses will insert other panels.
     *
     * @param doneAction the calling action
     * @return the panel
     */
    protected JPanel makeBottomPanel(ActionListener doneAction) {
        makeSpecialBottomPanel(_update); // special case for when no families exist for a given itemType
        makeItemButtonPanel();
        if (_bottom1Panel == null || _bottom2Panel == null) {
            log.error("Item panel for {} made null bottom panels!", _itemType);
        }
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        if (doneAction != null) {
            bottomPanel.add(makeUpdateButton(doneAction));
        }
        // If families are missing _bottom2Panel will be made visible.
        if (_iconPanel == null) {
            _bottom1Panel.setVisible(false);
            _bottom2Panel.setVisible(true);
        } else {
            _bottom1Panel.setVisible(true);
            _bottom2Panel.setVisible(false);
        }
        return bottomPanel;
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
            makeDataFlavors();
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
                _previewPanel.setVisible(hasMaps);
            } else {
                _previewPanel = new PreviewPanel(_frame, _iconPanel, null, false);
                _previewPanel.setVisible(false);
            }
            _iconFamilyPanel.add(_previewPanel);
        } else {
            _previewPanel.setVisible(true);
        }
    }

    abstract protected void makeDataFlavors();

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
            log.debug("addIconsToPanel adds {} icons (map size {})to iconPanel for {}", cnt, iconMap.size(), _itemType);
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
        String scaleText = java.text.MessageFormat.format(Bundle.getMessage("scale"),
                new Object[]{CatalogPanel.printDbl(scale, 2)});
        JLabel label = new JLabel(scaleText);
        JPanel sPanel = new JPanel();
        sPanel.setOpaque(false);
        sPanel.add(label);
        panel.add(sPanel);       

        FontMetrics fm = getFontMetrics(panel.getFont());
        int width = fm.stringWidth(borderName) + 5;
        width = Math.max(Math.max(width, CatalogPanel.ICON_WIDTH), icon.getIconWidth()+ 12);
        int height = panel.getPreferredSize().height;
        panel.setPreferredSize(new Dimension(width, height));
    }

    abstract protected JPanel instructions();

    /**
     * Part of the initialization and reseting of an ItemPanel.
     * Overrides allows divergence for different panel needs.
     */
    abstract protected void makeFamiliesPanel();

    abstract protected void hideIcons();

    protected boolean familiesMissing() {
        if (_bottom1Panel != null) {
            _bottom1Panel.setVisible(false);
        }
        boolean restore;
        if (_askOnce) {
            restore = false;
        } else {
            int result = JOptionPane.showConfirmDialog(_frame.getEditor(),
                    Bundle.getMessage("AllFamiliesDeleted", _itemType), Bundle.getMessage("QuestionTitle"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            restore = (result == JOptionPane.YES_OPTION);
            _askOnce = true;
        }
        if (restore) {
            loadDefaultType();
            return true;
        } else {
            if (_initialized && !_update) {
                _bottom2Panel.setVisible(true);
                _bottom2Panel.invalidate();
            }
            return false;
        }
    }
    
    protected void loadDefaultType() {
        ItemPalette.loadMissingItemType(_itemType);
        if (!_initialized) {
            makeFamiliesPanel();
       } else {
            initIconFamiliesPanel();
            hideIcons();
        }
    }

    // oldDim old panel size,
    // totalDim old frame size
    protected void reSizeDisplay(boolean isPalette, Dimension oldDim, Dimension totalDim) {
        Dimension newDim = getPreferredSize();
        Dimension frameDiffDim = new Dimension(totalDim.width - oldDim.width, totalDim.height - oldDim.height);
        if (log.isDebugEnabled()) {
            // Gather data for additional dimensions needed to display new panel in the total frame
            log.debug("resize {} for type {}. totalDim= ({}, {}) \"{}\" OldDim= ({}, {}) frameDiffDim= ({}, {})",
                    (isPalette?"tabPane":"update"), _itemType, totalDim.width, totalDim.height,
                    this._itemType, oldDim.width, oldDim.height, frameDiffDim.width, frameDiffDim.height);
        }
        Dimension deltaDim = shellDimension(this);
        if (isPalette && _initialized) {
            _frame.reSize(ItemPalette._tabPane, deltaDim, newDim);
        } else if (_update || _initialized) {
            _frame.reSize(_frame, deltaDim, newDim);                            
        }
    }

    public Dimension shellDimension(ItemPanel panel) {
        if (panel instanceof FamilyItemPanel) {
            if (panel._itemType.equals("SignalMast")/* || panel._itemType.equals("Reporter")*/) {
                return new Dimension(23, 136);
            }
            return new Dimension(23, 122);
        } else if (panel instanceof IconItemPanel) {
            return new Dimension(20, 70);
        }
        return new Dimension(8, 0);
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
