package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.DirectorySearcher;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for adding items to Control Panels. Starting point for palette package.
 * <p>
 * Loads and stores icons used in Control Panel Editor panels.
 * For background colors to work on a particular editor instance, select the
 * 'Item Palette' item under 'Add Items' menu and configure the 'Backgrounds' tab
 * ItemPalette for that editor. Otherwise any item can be dragged and
 * dropped to any editor.
 * <p>
 * The icons are displayed on the background of the last editor to call the
 * ItemPalette instance. In session the user can set it to another color or a white/gray
 * squares pattern using the "View on:" combo. This choice is shared across tabs
 * as a field on the {@link jmri.jmrit.display.DisplayFrame} parent frame.
 * <p>
 * <a href="doc-files/ItemPalette-ClassDiagram.png"><img src="doc-files/ItemPalette-ClassDiagram.png" alt="UML Class diagram" height="50%" width="50%"></a>
 *
 * @author Pete Cressman Copyright (c) 2010, 2018
 * @author Egbert Broerse Copyright (c) 2017
 */
/*
@startuml jmri/jmrit/display/palette/doc-files/ItemPalette-ClassDiagram.png

abstract class JPanel
package "jmri.util.swing.ImagePanel" {
   class ImagePanel {
-BufferedImage back
+setImage()
+paintComponent()
}
}
package "jmri.util.swing.DrawSquares" {
   class "DrawSquares" {
+DrawSquares()
}
}
abstract class ItemPanel {
-String type
#int previewBgSet
#BufferedImage[] _backgrounds
#MakeBgCombo()
}
JPanel --|> ItemPanel
abstract class FamilyItemPanel
class TableItemPanel
class IndicatorItemPanel
IndicatorItemPanel : type = "Indicator"
object viewOnCombo
viewOnCombo : -int choice
viewOnCombo : +EventListener InitListener
object preview
preview : -image = 1
preview : +EventListener comboListener
object TurnoutItemPanel
TurnoutItemPanel : type = "Turnout"
TableItemPanel -- TurnoutItemPanel
object SensorItemPanel
SensorItemPanel : type = "Sensor"
TableItemPanel -- SensorItemPanel
class SignalMastItemPanel
SignalMastItemPanel : type = "SignalMast"
TableItemPanel --|> SignalMastItemPanel
class MultiSensorItemPanel
MultiSensorItemPanel : type = "MultiSensor"
TableItemPanel --|> MultiSensorItemPanel
class IconItemPanel
class BackgroundItemPanel
BackgroundItemPanel : type = "Background"
IconItemPanel --|> BackgroundItemPanel
class ClockItemPanel
ClockItemPanel : type = "Clock"
IconItemPanel --|> ClockItemPanel
class DecoratorPanel
DecoratorPanel : #int previewBgSet
DecoratorPanel : #BufferedImage[] _backgrounds
JPanel --|> DecoratorPanel
abstract class DragJComponent
JPanel --|> DragJComponent
class TextItemPanel
TextItemPanel : type = "Text"

ItemPanel --|> FamilyItemPanel
FamilyItemPanel --|> TableItemPanel
FamilyItemPanel --|> IndicatorItemPanel
DecoratorPanel *-- viewOnCombo
FamilyItemPanel *-- viewOnCombo : if != SignalMast
FamilyItemPanel *-- preview
IconItemPanel *-- viewOnCombo : if != Background
SignalMastItemPanel *-- viewOnCombo
viewOnCombo ..> preview: setImage[n]
viewOnCombo -- DrawSquares
ItemPanel --|> IconItemPanel
ItemPanel --|> TextItemPanel
DecoratorPanel -- TextItemPanel
ImagePanel -- preview
DragJComponent --|> ReporterItemPanel
ReporterItemPanel *-- preview
' MemoryItemPanel not shown

@enduml
*/

public class ItemPalette extends DisplayFrame implements ChangeListener {

    public static final int STRUT_SIZE = 10;

    protected static JTabbedPane _tabPane;
    private static HashMap<String, ItemPanel> _tabIndex;

    private static volatile HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> _iconMaps;
    // for now, special case 4 level maps since IndicatorTO is the only case.
    private static volatile HashMap<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>> _indicatorTOMaps;
    private ItemPanel _currentItemPanel;

    /**
     * Store palette icons in preferences file catalogTrees.xml
     */
    public static void storeIcons() {
        if (_iconMaps == null) {
            return;     // never loaded
        }
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        // unfiltered, xml-stored, item palate icon tree
        CatalogTree tree = manager.getBySystemName("NXPI");
        // discard old version
        if (tree != null) {
            manager.deregister(tree);
        }
        tree = manager.newCatalogTree("NXPI", "Item Palette");
        CatalogTreeNode root = tree.getRoot();

        for (Entry<String, HashMap<String, HashMap<String, NamedIcon>>> entry : _iconMaps.entrySet()) {
            root.add(store3levelMap(entry.getKey(), entry.getValue()));
            if (log.isDebugEnabled()) {
                log.debug("Add type node " + entry.getKey());
            }
        }

        for (Entry<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>> entry : _indicatorTOMaps.entrySet()) {
            CatalogTreeNode typeNode = new CatalogTreeNode(entry.getKey());
            for (Entry<String, HashMap<String, HashMap<String, NamedIcon>>> ent : entry.getValue().entrySet()) {
                typeNode.add(store3levelMap(ent.getKey(), ent.getValue()));
                log.debug("Add IndicatorTO node {}", ent.getKey());
            }
            root.add(typeNode);
            log.debug("Add IndicatorTO node {}", entry.getKey());
        }
    }

    static CatalogTreeNode store3levelMap(String type, HashMap<String, HashMap<String, NamedIcon>> familyMap) {
        CatalogTreeNode typeNode = new CatalogTreeNode(type);
        for (Entry<String, HashMap<String, NamedIcon>> mapEntry : familyMap.entrySet()) {
            String family = mapEntry.getKey();
            CatalogTreeNode familyNode = new CatalogTreeNode(family);
            HashMap<String, NamedIcon> iconMap = mapEntry.getValue();
            for (Entry<String, NamedIcon> iconEntry : iconMap.entrySet()) {
                String state = iconEntry.getKey();
                String path = iconEntry.getValue().getURL();
                familyNode.addLeaf(state, path);
            }
            typeNode.add(familyNode);
            log.debug("Add familyNode {}", familyNode);
        }
        return typeNode;
    }

    public static void loadIcons(Editor ed) {
        if (_iconMaps == null) {
            // long t = System.currentTimeMillis();
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
            _iconMaps = new HashMap<>();
            _indicatorTOMaps = new HashMap<>();

            if (!loadSavedIcons(ed)) {
                loadDefaultIcons(ed);
            }
        }
    }

    static boolean loadSavedIcons(Editor ed) {
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        CatalogTree tree = manager.getBySystemName("NXPI");
        if (tree != null) {
            CatalogTreeNode root = tree.getRoot();
            Enumeration<TreeNode> e = root.children();
            while (e.hasMoreElements()) {
                CatalogTreeNode node = (CatalogTreeNode)e.nextElement();
                String typeName = (String) node.getUserObject();
                // detect this is a 4 level map collection.
                // not very elegant (i.e. extensible), but maybe all that's needed.
                if (typeName.equals("IndicatorTO")) {
                    HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap
                            = loadIndicatorFamilyMap(node, ed);
                    log.debug("Add {} indicatorTO families to item type {} for _indicatorTOMaps.",
                            familyTOMap.size(), typeName );
                    _indicatorTOMaps.put(typeName, familyTOMap);
                } else {
                    HashMap<String, HashMap<String, NamedIcon>> familyMap
                            = loadFamilyMap(node, ed);
                    _iconMaps.put(typeName, familyMap);
                    log.debug("Add item type {} to _iconMaps.", typeName);
                }
            }
            log.debug("Icon Map has {} members", _iconMaps.size());
            return true;
        }
        return false;
    }

    static HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>
            loadIndicatorFamilyMap(CatalogTreeNode node, Editor ed) {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyMap
                = new HashMap<>();
        Enumeration<TreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = (CatalogTreeNode)ee.nextElement();
            String name = (String) famNode.getUserObject();
            familyMap.put(name, loadFamilyMap(famNode, ed));
        }
        return familyMap;
    }

    static HashMap<String, HashMap<String, NamedIcon>> loadFamilyMap(CatalogTreeNode node, Editor ed) {
        HashMap<String, HashMap<String, NamedIcon>> familyMap = new HashMap<>();
        Enumeration<TreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = (CatalogTreeNode)ee.nextElement();
            String familyName = (String) famNode.getUserObject();
            HashMap<String, NamedIcon> iconMap = new HashMap<>();
            List<CatalogTreeLeaf> list = famNode.getLeaves();
            for (int i = 0; i < list.size(); i++) {
                String iconName = list.get(i).getName();
                CatalogTreeLeaf leaf = list.get(i);
                String path = leaf.getPath();
                NamedIcon icon = NamedIcon.getIconByName(path);
                if (icon == null) {
                    icon = ed.loadFailed(iconName, path);
                    if (icon == null) {
                        log.info("{} removed for url = {}", iconName, path);
                    } else {
                        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
                    }
                }
                if (icon != null) {
                    iconMap.put(iconName, icon);
                    log.debug("Add {} icon to family \"{}\"", iconName, familyName);
                }
            }
            familyMap.put(familyName, iconMap);
        }
        return familyMap;
    }

    static List<Element> getDefaultIconItemTypes() throws org.jdom2.JDOMException, java.io.IOException {
        URL file = FileUtil.findURL("xml/defaultPanelIcons.xml");
        if (file == null) {
            log.error("defaultPanelIcons file (xml/defaultPanelIcons.xml) doesn't exist.");
            throw new IllegalArgumentException("defaultPanelIcons file (xml/defaultPanelIcons.xml) doesn't exist.");
        }
        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile() {
        };
        Element root = xf.rootFromURL(file);
        return (root.getChild("ItemTypes").getChildren());
    }

    static void loadDefaultIcons(Editor ed) {
        try {
            List<Element> typeList = getDefaultIconItemTypes();
            for (Element type : typeList) {
                String typeName = type.getName();
                List<Element> families = type.getChildren();
                loadFamilies(typeName, families, ed);
            }
        } catch (org.jdom2.JDOMException | java.io.IOException e) {
            log.error("error reading file \"defaultPanelIcons.xml\" due to: ", e);
        }
    }

    static void loadFamilies(String typeName, List<Element> families, Editor ed) {
        // detect this is a 4 level map collection.
        // not very elegant (i.e. extensible), but maybe all that's needed.
        if (typeName.equals("IndicatorTO")) {
            HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap
                    = loadDefaultIndicatorTOMap(families, ed);
            _indicatorTOMaps.put(typeName, familyTOMap);
            log.debug("Add {} indicatorTO families to item type {} to _indicatorTOMaps.",
                    familyTOMap.size(), typeName);
        } else {
            HashMap<String, HashMap<String, NamedIcon>> familyMap = loadDefaultFamilyMap(families, ed);
            _iconMaps.put(typeName, familyMap);
            log.debug("Add {} families to item type \"{}\" to _iconMaps.",
                    familyMap.size(), typeName);
        }
    }

    static void loadMissingItemType(String itemType, Editor ed) {
        try {
            List<Element> typeList = getDefaultIconItemTypes();
            for (Element type : typeList) {
                String typeName = type.getName();
                if (!typeName.equals(itemType)) {
                    continue;
                }
                List<Element> families = type.getChildren();
                loadFamilies(itemType, families, ed);
                InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
            }
        } catch (org.jdom2.JDOMException | java.io.IOException ex) {
            log.error("error reading file \"defaultPanelIcons.xml\" due to: ", ex);
        }
    }

    static HashMap<String, HashMap<String, NamedIcon>> loadDefaultFamilyMap(List<Element> families, Editor ed) {
        HashMap<String, HashMap<String, NamedIcon>> familyMap = new HashMap<>();
        for (int k = 0; k < families.size(); k++) {
            String familyName = families.get(k).getName();
            HashMap<String, NamedIcon> iconMap = new HashMap<>();
            // Map of all icons of in family, familyName
            List<Element> iconfiles = families.get(k).getChildren();
            for (int j = 0; j < iconfiles.size(); j++) {
                String iconName = iconfiles.get(j).getName();
                String fileName = iconfiles.get(j).getText().trim();
                if (fileName.length() == 0) {
                    fileName = "resources/icons/misc/X-red.gif";
                    log.warn("loadDefaultFamilyMap: iconName = {} in family {} has no image file.",
                            iconName, familyName);
                }
                NamedIcon icon = NamedIcon.getIconByName(fileName);
                if (icon == null) {
                    icon = ed.loadFailed(iconName, fileName);
                    if (icon == null) {
                        log.info("{} removed for url= {}", iconName, fileName);
                    }
                }
                if (icon != null) {
                    iconMap.put(iconName, icon);
                }
            }
            familyMap.put(familyName, iconMap);
            if (log.isDebugEnabled()) {
                log.debug("Add {}  icons to family \"{}\"", iconMap.size(), familyName);
            }
        }
        return familyMap;
    }

    static HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>
            loadDefaultIndicatorTOMap(List<Element> typeList, Editor ed) {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap = new HashMap<>();
        // Map of all families of type, typeName
        for (int k = 0; k < typeList.size(); k++) {
            String familyName = typeList.get(k).getName();
            List<Element> types = typeList.get(k).getChildren();
            HashMap<String, HashMap<String, NamedIcon>> familyMap = loadDefaultFamilyMap(types, ed);
            familyTOMap.put(familyName, familyMap);
            if (log.isDebugEnabled()) {
                log.debug("Add {}  IndicatorTO sub-families to item type {}  to IndicatorTO families.",
                        familyMap.size(), familyName);
            }
        }
        return familyTOMap;
    }

    static public ItemPalette getDefault(String title, @Nonnull Editor ed) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        ItemPalette instance = InstanceManager.getOptionalDefault(ItemPalette.class).orElseGet(() -> {
            return InstanceManager.setDefault(ItemPalette.class, new ItemPalette(title, ed));
        });
        Iterator<ItemPanel> iter = _tabIndex.values().iterator();
        while (iter.hasNext()) {
            iter.next().setEditor(ed);
        }
        String name = ed.getName();
        if (name == null || name.equals("")) {
            name = Bundle.getMessage("untitled");
        }
        instance.setTitle(Bundle.getMessage("MenuItemItemPalette") + " - " + name);
        // pack before setLocation
        instance.pack();
        instance.setLocation(jmri.util.PlaceWindow.nextTo(ed, null, instance));
        instance.setVisible(true);
        return instance;
    }
    
    public ItemPalette(String title, Editor ed) {
        super(false, false);
        init(title, ed);
    }
    
    private void init(String title, Editor ed) {
        this.setTitle(title);
        loadIcons(ed);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closePanels(e);
            }
        });

        makeMenus(ed);
        buildTabPane(this, ed);

        setLayout(new BorderLayout(5, 5));
        add(_tabPane, BorderLayout.CENTER);
        JScrollPane sp = (JScrollPane) _tabPane.getSelectedComponent();
        _currentItemPanel = (ItemPanel) sp.getViewport().getView();
    }

    /*
     * Add the tabs on the Control Panel Editor.
     */
    static void buildTabPane(ItemPalette palette, Editor editor) {
        _tabPane = new JTabbedPane();
        _tabIndex = new HashMap<>();

        ItemPanel itemPanel = new TableItemPanel<>(palette, "Turnout", null,
                PickListModel.turnoutPickModelInstance(), editor);
        addItemTab(itemPanel, "Turnout", "BeanNameTurnout");
        itemPanel.init();  // show panel on start

        itemPanel = new TableItemPanel<>(palette, "Sensor", null,
                PickListModel.sensorPickModelInstance(), editor);
        addItemTab(itemPanel, "Sensor", "BeanNameSensor");

        itemPanel = new SignalHeadItemPanel(palette, "SignalHead", null,
                PickListModel.signalHeadPickModelInstance(), editor);
        addItemTab(itemPanel, "SignalHead", "BeanNameSignalHead");

        itemPanel = new SignalMastItemPanel(palette, "SignalMast", null,
                PickListModel.signalMastPickModelInstance(), editor);
        addItemTab(itemPanel, "SignalMast", "BeanNameSignalMast");

        itemPanel = new MemoryItemPanel(palette, "Memory", null,
                PickListModel.memoryPickModelInstance(), editor);
        addItemTab(itemPanel, "Memory", "BeanNameMemory");

        itemPanel = new ReporterItemPanel(palette, "Reporter", null,
                PickListModel.reporterPickModelInstance(), editor);
        addItemTab(itemPanel, "Reporter", "BeanNameReporter");

        itemPanel = new TableItemPanel<>(palette, "Light", null,
                PickListModel.lightPickModelInstance(), editor);
        addItemTab(itemPanel, "Light", "BeanNameLight");

        itemPanel = new MultiSensorItemPanel(palette, "MultiSensor", null,
                PickListModel.multiSensorPickModelInstance(), editor);
        addItemTab(itemPanel, "MultiSensor", "MultiSensor");

        itemPanel = new IconItemPanel(palette, "Icon", editor);
        addItemTab(itemPanel, "Icon", "Icon");

        itemPanel = new BackgroundItemPanel(palette, "Background", editor);
        addItemTab(itemPanel, "Background", "Background");

        itemPanel = new TextItemPanel(palette, "Text", editor);
        addItemTab(itemPanel, "Text", "Text");

        itemPanel = new RPSItemPanel(palette, "RPSReporter", null, editor);
        addItemTab(itemPanel, "RPSReporter", "RPSreporter");

        itemPanel = new ClockItemPanel(palette, "FastClock", editor);
        addItemTab(itemPanel, "FastClock", "FastClock");

        itemPanel = new IndicatorItemPanel(palette, "IndicatorTrack", null, editor);
        addItemTab(itemPanel, "IndicatorTrack", "IndicatorTrack");

        itemPanel = new IndicatorTOItemPanel(palette, "IndicatorTO", null,
                PickListModel.turnoutPickModelInstance(), editor);
        addItemTab(itemPanel, "IndicatorTO", "IndicatorTO");

        itemPanel = new PortalItemPanel(palette, "Portal", null, editor);
        addItemTab(itemPanel, "Portal", "BeanNamePortal");

        _tabPane.addChangeListener(palette);
    }
    
    static void addItemTab(ItemPanel itemPanel, String key, String tabTitle) {
        JScrollPane scrollPane = new JScrollPane(itemPanel);
        _tabPane.add(scrollPane, Bundle.getMessage(tabTitle));
        _tabIndex.put(key, itemPanel);
    }

    @Override
    public void updateBackground0(java.awt.image.BufferedImage im) {
        int bgIdx = getPreviewBg();
        Iterator<ItemPanel> iter = _tabIndex.values().iterator();
        while (iter.hasNext()) {
            ItemPanel panel = iter.next();
            panel.updateBackground0(im);
            panel.setPreviewBg(bgIdx);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // long t = System.currentTimeMillis();
        JTabbedPane tp = (JTabbedPane) e.getSource();
        JScrollPane sp = (JScrollPane) tp.getSelectedComponent();
        ItemPanel p = (ItemPanel) sp.getViewport().getView();
        Dimension oldTabDim = null;
        log.debug("different tab displayed for {} previewBgSet updated to {}", p._itemType, getPreviewBg());
        if (_currentItemPanel != null) {
            _currentItemPanel.closeDialogs();
            oldTabDim = _currentItemPanel.getSize();
        }
        Dimension totalDim = _tabPane.getSize();
        p.init(); // (re)initialize tab pane
        p.revalidate();
        Dimension newTabDim = p.getPreferredSize();
        if (oldTabDim == null) {
            oldTabDim = newTabDim;
        }
        Dimension deltaDim = new Dimension(totalDim.width - oldTabDim.width, totalDim.height - oldTabDim.height);
        if (log.isDebugEnabled()) 
            log.debug("_tabPane Dim= ({}, {}) Old Dim({})= ({}, {}) NewDim({})= ({}, {})", totalDim.width, totalDim.height, 
                _currentItemPanel._itemType, oldTabDim.width, oldTabDim.height,  p._itemType, newTabDim.width, newTabDim.height);
        // tabPane must be larger than the current panel it is displaying
        if (deltaDim.width < 8) {
            deltaDim.width = 8;
        }
        if (deltaDim.height < 50) { // at least 2 rows of tabs
            deltaDim.height = 50;
        }
        reSize(_tabPane, deltaDim, newTabDim, p._editor);
        if (p._bgColorBox != null) {
            p._bgColorBox.setSelectedIndex(getPreviewBg());
        }
        _currentItemPanel = p;
    }

    private void makeMenus(Editor editor) {
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(Bundle.getMessage("findIconMenu"));
        menuBar.add(findIcon);

        JMenuItem editItem = new JMenuItem(Bundle.getMessage("editIndexMenu"));
        editItem.addActionListener((ActionEvent e) -> {
                ImageIndexEditor ii = InstanceManager.getDefault(ImageIndexEditor.class);
                ii.pack();
                ii.setVisible(true);
        });
        findIcon.add(editItem);
        findIcon.addSeparator();

        JMenuItem openItem = new JMenuItem(Bundle.getMessage("openDirMenu"));
        openItem.addActionListener((ActionEvent e) -> {
            InstanceManager.getDefault(DirectorySearcher.class).openDirectory();
        });
        findIcon.add(openItem);

         JMenuItem searchItem = new JMenuItem(Bundle.getMessage("searchFSMenu"));
         searchItem.addActionListener((ActionEvent e) -> {
             jmri.jmrit.catalog.DirectorySearcher.instance().searchFS();
         });
         findIcon.add(searchItem);

        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.display.ItemPalette", true);
    }

    public void closePanels(java.awt.event.WindowEvent e) {
        java.awt.Component[] comps = _tabPane.getComponents();
        if (log.isDebugEnabled()) {
            log.debug("closePanels: tab count= " + _tabPane.getTabCount());
        }
        for (int i = 0; i < comps.length; i++) {
            javax.swing.JViewport vp = (javax.swing.JViewport) ((JScrollPane) comps[i]).getComponent(0);
            java.awt.Component ip = vp.getView();
            if (ip instanceof ItemPanel) {
                ((ItemPanel) ip).closeDialogs();
            }
        }
        super.windowClosing(e);
    }

    /*
     * Look for duplicate name of family in the iterated set.
     */
    protected static boolean familyNameOK(java.awt.Frame frame, String type, String family, Iterator<String> it) {
        if (family == null || family.length() == 0) {
            JOptionPane.showMessageDialog(frame,
                    Bundle.getMessage("EnterFamilyName"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        while (it.hasNext()) {
            String f = it.next();
            log.debug("familyNameOK compare {} {} to {}", type, family, f);
            if (family.equals(f)) {
                JOptionPane.showMessageDialog(frame,
                        java.text.MessageFormat.format(Bundle.getMessage("DuplicateFamilyName"),
                                new Object[]{family, type}),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Add a new Family of icons to the device type.
     *
     * @param frame frame
     * @param type type
     * @param family family
     * @param iconMap iconMap
     * @return result
     */
    static protected boolean addFamily(java.awt.Frame frame, String type, String family, HashMap<String, NamedIcon> iconMap) {
        if (ItemPalette.getFamilyMaps(type) == null) {
            HashMap<String, HashMap<String, NamedIcon>> typeMap = new HashMap<>();
            _iconMaps.put(type, typeMap);
            // typeMap.put(family, iconMap);
        }
        Iterator<String> iter = ItemPalette.getFamilyMaps(type).keySet().iterator();
        if (familyNameOK(frame, type, family, iter)) {
            getFamilyMaps(type).put(family, iconMap);
            /*            ItemPanel itemPanel = _tabIndex.get(type);
             if (itemPanel instanceof FamilyItemPanel) {
             ((FamilyItemPanel)itemPanel).updateFamiliesPanel();
             }*/
            InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
            return true;
        }
        log.warn("addFamily: family name \"{}\" for type {} NOT OK! map size= {}", family, type, iconMap.size());
        return false;
    }

    /**
     * Get all the Families of icons for a given device type.
     *
     * @param type type
     * @return map of families
     */
    static protected HashMap<String, HashMap<String, NamedIcon>> getFamilyMaps(String type) {
        return _iconMaps.get(type);
    }

    /**
     * Remove a Family of icons from the device type.
     *
     * @param type type
     * @param family family
     */
    static protected void removeIconMap(String type, String family) {
        if (log.isDebugEnabled()) {
            log.debug("removeIconMap for family \"{}\" in type \"{}\"", family, type);
        }
        _iconMaps.get(type).remove(family);
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
        if (log.isDebugEnabled()) {
            HashMap<String, HashMap<String, NamedIcon>> families = getFamilyMaps(type);
            if (families != null && families.size() > 0) {
                Iterator<String> it = families.keySet().iterator();
                while (it.hasNext()) {
                    log.debug("removeIconMap remaining Keys: family \"{}\" in type \"{}\"", it.next(), type);
                }
            }
        }
    }

    /*
     * Get a clone of the Family of icons for a given device type and family.
     */
    static protected HashMap<String, NamedIcon> getIconMap(String type, String family) {
        HashMap<String, HashMap<String, NamedIcon>> itemMap = _iconMaps.get(type);
        if (itemMap == null) {
            log.error("getIconMap failed. item type \"{}\" not found.", type);
            return null;
        }
        HashMap<String, NamedIcon> iconMap = itemMap.get(family);
        if (iconMap == null) {
            log.error("getIconMap failed. family \"{}\" not found in item type \"{}\"", family, type);
            return null;
        }
        return cloneMap(iconMap);
    }

    /**
     * ************ Currently only needed for IndicatorTO type **************
     * @param frame frame
     * @param type type
     * @param family family
     * @param iconMap iconMap
     * @return result
     */
    // add entire family
    static protected boolean addLevel4Family(java.awt.Frame frame, String type, String family,
            HashMap<String, HashMap<String, NamedIcon>> iconMap) {
        Iterator<String> iter = ItemPalette.getLevel4FamilyMaps(type).keySet().iterator();
        if (familyNameOK(frame, type, family, iter)) {
            getLevel4FamilyMaps(type).put(family, iconMap);
            InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
            return true;
        }
        return false;
    }

    // add entire family
    static protected void addLevel4FamilyMap(String type, String family,
            String key, HashMap<String, NamedIcon> iconMap) {
        HashMap<String, HashMap<String, NamedIcon>> familyMap = getLevel4Family(type, family);
        familyMap.put(key, iconMap);
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
    }

    // Currently only needed for IndicatorTO type
    static protected HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>
            getLevel4FamilyMaps(String type) {
        return _indicatorTOMaps.get(type);
    }

    // Currently only needed for IndicatorTO type
    static protected HashMap<String, HashMap<String, NamedIcon>>
            getLevel4Family(String type, String family) {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> map = _indicatorTOMaps.get(type);
        return map.get(family);
    }

    // Currently only needed for IndicatorTO type
    static protected void removeLevel4IconMap(String type, String family, String key) {
        if (log.isDebugEnabled()) {
            log.debug("removeLevel4IconMap for indicator family \"{}\" in type \"{}\" with key \"{}\"",
                    family, type, key);
        }
        if (key != null) {
            _indicatorTOMaps.get(type).get(family).remove(key);
        } else {
            _indicatorTOMaps.get(type).remove(family);
        }
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
    }

    ///////////////////////////////////////////////////////////////////////////////
    
    static protected HashMap<String, NamedIcon> cloneMap(HashMap<String, NamedIcon> map) {
        HashMap<String, NamedIcon> clone = new HashMap<>();
        if (map != null) {
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                String name = entry.getKey();
                NamedIcon icon = new NamedIcon(entry.getValue());
                clone.put(name, icon);
            }
        }
        return clone;
    }

    static public String convertText(String name) {
        String cName = null;
        try {
            // NOI18N
            cName = Bundle.getMessage(name);
        } catch (java.util.MissingResourceException mre) {
            try {
                cName = Bundle.getMessage(name);
            } catch (java.util.MissingResourceException mre2) {
                cName = name;
            }
        }
        return cName;
    }

    static protected JPanel makeBannerPanel(String labelText, Component field) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        panel.add(new JLabel(Bundle.getMessage(labelText)), c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL; // text field will expand
        panel.add(field, c);
        return panel;
    }

    private final static Logger log = LoggerFactory.getLogger(ItemPalette.class);

}
