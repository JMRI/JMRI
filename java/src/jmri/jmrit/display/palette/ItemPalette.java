package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Component;
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
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.DirectorySearcher;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for adding items to Control Panels. Loads and stores icons used in
 * Control Panel Editor panels. For background colors to work on a particular editor
 * instance, select the 'Item Palette' item under 'Add Items' menu to configure 
 * ItemPalette for that editor. Otherwise any item can be dragged and
 * dropped to any editor. The icons are displayed on the background
 * of the last editor to call the ItemPalette instance. The user can set it
 * to another color or a white/gray squares pattern using the "View on:" combo.
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class ItemPalette extends JmriJFrame implements ChangeListener {

    public static final int STRUT_SIZE = 10;

    static JTabbedPane _tabPane;
    static HashMap<String, ItemPanel> _tabIndex;

    static HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> _iconMaps;
    // for now, special case 4 level maps since IndicatorTO is the only case.
    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>> _indicatorTOMaps;
    private ItemPanel _currentItemPanel;

    /**
     * Store palette icons in preferences file catalogTrees.xml
     */
    public static void storeIcons() {
        if (_iconMaps == null) {
            return;     // never loaded
        }
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        // unfiltered, xml-stored, item palate icon tree
        CatalogTree tree = manager.getBySystemName("NXPI");
        // discard old version
        if (tree != null) {
            manager.deregister(tree);
        }
        tree = manager.newCatalogTree("NXPI", "Item Palette");
        CatalogTreeNode root = tree.getRoot();

        Iterator<Entry<String, HashMap<String, HashMap<String, NamedIcon>>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashMap<String, HashMap<String, NamedIcon>>> entry = it.next();
            root.add(store3levelMap(entry.getKey(), entry.getValue()));
            if (log.isDebugEnabled()) {
                log.debug("Add type node " + entry.getKey());
            }
        }

        Iterator<Entry<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>>> its = _indicatorTOMaps.entrySet().iterator();
        while (its.hasNext()) {
            Entry<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>> entry = its.next();
            CatalogTreeNode typeNode = new CatalogTreeNode(entry.getKey());
            Iterator<Entry<String, HashMap<String, HashMap<String, NamedIcon>>>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, HashMap<String, HashMap<String, NamedIcon>>> ent = iter.next();
                typeNode.add(store3levelMap(ent.getKey(), ent.getValue()));
                if (log.isDebugEnabled()) {
                    log.debug("Add IndicatorTO node " + ent.getKey());
                }
            }
            root.add(typeNode);
            if (log.isDebugEnabled()) {
                log.debug("Add IndicatorTO node " + entry.getKey());
            }
        }
    }

    static CatalogTreeNode store3levelMap(String type, HashMap<String, HashMap<String, NamedIcon>> familyMap) {
        CatalogTreeNode typeNode = new CatalogTreeNode(type);
        Iterator<Entry<String, HashMap<String, NamedIcon>>> iter = familyMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, HashMap<String, NamedIcon>> ent = iter.next();
            String family = ent.getKey();
            CatalogTreeNode familyNode = new CatalogTreeNode(family);
            HashMap<String, NamedIcon> iconMap = ent.getValue();
            Iterator<Entry<String, NamedIcon>> iterat = iconMap.entrySet().iterator();
            while (iterat.hasNext()) {
                Entry<String, NamedIcon> e = iterat.next();
                String state = e.getKey();
                String path = e.getValue().getURL();
                familyNode.addLeaf(state, path);
            }
            typeNode.add(familyNode);
            if (log.isDebugEnabled()) {
                log.debug("Add familyNode " + familyNode);
            }
        }
        return typeNode;
    }

    static public void loadIcons(Editor ed) {
        if (_iconMaps == null) {
            // long t = System.currentTimeMillis();
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
            _iconMaps = new HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>();
            _indicatorTOMaps
                    = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>>();

            if (!loadSavedIcons(ed)) {
                loadDefaultIcons(ed);
            }
            // System.out.println("Palette icons loaded in " + (System.currentTimeMillis()-t) + " milliseconds.");
        }
    }

    static boolean loadSavedIcons(Editor ed) {
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        CatalogTree tree = manager.getBySystemName("NXPI");
        if (tree != null) {
            CatalogTreeNode root = tree.getRoot();
            @SuppressWarnings("unchecked") // root.children() is still unchecked in JDOM2
            Enumeration<CatalogTreeNode> e = root.children();
            while (e.hasMoreElements()) {
                CatalogTreeNode node = e.nextElement();
                String typeName = (String) node.getUserObject();
                // detect this is a 4 level map collection.
                // not very elegant (i.e. extensible), but maybe all that's needed.
                if (typeName.equals("IndicatorTO")) {
                    HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap
                            = loadIndicatorFamilyMap(node, ed);
                    if (log.isDebugEnabled()) {
                        log.debug("Add {} indicatorTO families to item type {} for _indicatorTOMaps.",
                                familyTOMap.size(), typeName );
                    }
                    _indicatorTOMaps.put(typeName, familyTOMap);
                } else {
                    HashMap<String, HashMap<String, NamedIcon>> familyMap
                            = loadFamilyMap(node, ed);
                    _iconMaps.put(typeName, familyMap);
                    log.debug("Add item type {} to _iconMaps.", typeName);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Icon Map has {} members", _iconMaps.size());
            }
            return true;
        }
        return false;
    }

    static HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>
            loadIndicatorFamilyMap(CatalogTreeNode node, Editor ed) {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyMap
                = new HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>();
        @SuppressWarnings("unchecked") // node.children() is still unchecked in JDOM2
        Enumeration<CatalogTreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = ee.nextElement();
            String name = (String) famNode.getUserObject();
            familyMap.put(name, loadFamilyMap(famNode, ed));
            Thread.yield();
        }
        return familyMap;
    }

    static HashMap<String, HashMap<String, NamedIcon>> loadFamilyMap(CatalogTreeNode node, Editor ed) {
        HashMap<String, HashMap<String, NamedIcon>> familyMap
                = new HashMap<String, HashMap<String, NamedIcon>>();
        @SuppressWarnings("unchecked") // node.children() is still unchecked in JDOM2
        Enumeration<CatalogTreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = ee.nextElement();
            String familyName = (String) famNode.getUserObject();
            HashMap<String, NamedIcon> iconMap = new HashMap<String, NamedIcon>();
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
                        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
                    }
                }
                if (icon != null) {
                    iconMap.put(iconName, icon);
                    if (log.isDebugEnabled()) {
                        log.debug("Add {} icon to family \"{}\"", iconName, familyName);
                    }
                }
                Thread.yield();
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
        List<Element> typeList = root.getChild("ItemTypes").getChildren();
        return typeList;
    }

    static void loadDefaultIcons(Editor ed) {
        try {
            List<Element> typeList = getDefaultIconItemTypes();
            for (int i = 0; i < typeList.size(); i++) {
                String typeName = typeList.get(i).getName();
                List<Element> families = typeList.get(i).getChildren();
                loadFamilies(typeName, families, ed);
                Thread.yield();
            }
        } catch (org.jdom2.JDOMException e) {
            log.error("error reading file \"defaultPanelIcons.xml\" due to: " + e);
        } catch (java.io.IOException ioe) {
            log.error("error reading file \"defaultPanelIcons.xml\" due to: " + ioe);
        }
    }

    static void loadFamilies(String typeName, List<Element> families, Editor ed) {
        // detect this is a 4 level map collection.
        // not very elegant (i.e. extensible), but maybe all that's needed.
        if (typeName.equals("IndicatorTO")) {
            HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap
                    = loadDefaultIndicatorTOMap(families, ed);
            _indicatorTOMaps.put(typeName, familyTOMap);
            if (log.isDebugEnabled()) {
                log.debug("Add {} indicatorTO families to item type {} to _indicatorTOMaps.",
                        familyTOMap.size(), typeName);
            }
        } else {
            HashMap<String, HashMap<String, NamedIcon>> familyMap = loadDefaultFamilyMap(families, ed);
            _iconMaps.put(typeName, familyMap);
            if (log.isDebugEnabled()) {
                log.debug("Add {} families to item type \"{}\" to _iconMaps.",
                        familyMap.size(), typeName);
            }
        }
    }

    static void loadMissingItemType(String itemType, Editor ed) {
        try {
            List<Element> typeList = getDefaultIconItemTypes();
            for (int i = 0; i < typeList.size(); i++) {
                String typeName = typeList.get(i).getName();
                if (!typeName.equals(itemType)) {
                    continue;
                }
                List<Element> families = typeList.get(i).getChildren();
                loadFamilies(itemType, families, ed);
                InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
            }
        } catch (org.jdom2.JDOMException e) {
            log.error("error reading file \"defaultPanelIcons.xml\" due to: " + e);
        } catch (java.io.IOException ioe) {
            log.error("error reading file \"defaultPanelIcons.xml\" due to: " + ioe);
        }
    }

    static HashMap<String, HashMap<String, NamedIcon>> loadDefaultFamilyMap(List<Element> families, Editor ed) {
        HashMap<String, HashMap<String, NamedIcon>> familyMap
                = new HashMap<String, HashMap<String, NamedIcon>>();
        for (int k = 0; k < families.size(); k++) {
            String familyName = families.get(k).getName();
            HashMap<String, NamedIcon> iconMap
                    = new HashMap<String, NamedIcon>();     // Map of all icons of in family, familyName
            List<Element> iconfiles = families.get(k).getChildren();
            for (int j = 0; j < iconfiles.size(); j++) {
                String iconName = iconfiles.get(j).getName();
                String fileName = iconfiles.get(j).getText().trim();
                if (fileName.length() == 0) {
                    fileName = "resources/icons/misc/X-red.gif";
                    log.warn("loadDefaultFamilyMap: iconName = {} in family {} has no image file.", iconName, familyName);
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
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap
                = new HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>();     // Map of all families of type, typeName
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
        Iterator<Entry<String, ItemPanel>> iter = _tabIndex.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, ItemPanel> entry = iter.next();
            ItemPanel tab = entry.getValue();
            tab.setEditor(ed);            
        }
        String name = ed.getName();
        if (name == null || name.equals("")) {
            name = Bundle.getMessage("untitled");
        }
        instance.setTitle(Bundle.getMessage("MenuItemItemPalette") + " - " + name);
        // Either of these positioning calls puts the instance on the primary monitor. ???
        java.awt.Point pt = ed.getLocation();
        instance.setLocation(pt.x, pt.y);
        // instance.setLocationRelativeTo(ed);
        instance.pack();
        instance.setVisible(true);
        return instance;
    }
    
    private ItemPalette(String title, Editor ed) {
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
        setLocation(10, 10);
        JScrollPane sp = (JScrollPane) _tabPane.getSelectedComponent();
        _currentItemPanel = (ItemPanel) sp.getViewport().getView();
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        pack();
    }

    /**
     * Add the tabs on the the Control Panel Editor.
     */
    static void buildTabPane(ItemPalette palette, Editor editor) {
        _tabPane = new JTabbedPane();
        _tabIndex = new HashMap<String, ItemPanel>();

        ItemPanel itemPanel = new TableItemPanel(palette, "Turnout", null,
                PickListModel.turnoutPickModelInstance(), editor);
        itemPanel.init();  // show panel on start
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNameTurnout"));
        _tabIndex.put("Turnout", itemPanel);

        itemPanel = new TableItemPanel(palette, "Sensor", null,
                PickListModel.sensorPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNameSensor"));
        _tabIndex.put("Sensor", itemPanel);

        itemPanel = new SignalHeadItemPanel(palette, "SignalHead", null,
                PickListModel.signalHeadPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNameSignalHead"));
        _tabIndex.put("SignalHead", itemPanel);

        itemPanel = new SignalMastItemPanel(palette, "SignalMast", null,
                PickListModel.signalMastPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNameSignalMast"));
        _tabIndex.put("SignalMast", itemPanel);

        itemPanel = new MemoryItemPanel(palette, "Memory", null,
                PickListModel.memoryPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNameMemory"));
        _tabIndex.put("Memory", itemPanel);

        itemPanel = new ReporterItemPanel(palette, "Reporter", null,
                PickListModel.reporterPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNameReporter"));
        _tabIndex.put("Reporter", itemPanel);

        itemPanel = new TableItemPanel(palette, "Light", null,
                PickListModel.lightPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNameLight"));
        _tabIndex.put("Light", itemPanel);

        itemPanel = new MultiSensorItemPanel(palette, "MultiSensor", null,
                PickListModel.multiSensorPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("MultiSensor"));
        _tabIndex.put("MultiSensor", itemPanel);

        ItemPanel iconPanel = new IconItemPanel(palette, "Icon", editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("Icon"));
        _tabIndex.put("Icon", iconPanel); // changed from "itemPanel"

        iconPanel = new BackgroundItemPanel(palette, "Background", editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("Background"));
        _tabIndex.put("Background", iconPanel);

        iconPanel = new TextItemPanel(palette, "Text", editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("Text"));
        _tabIndex.put("Text", iconPanel);

        iconPanel = new RPSItemPanel(palette, "RPSReporter", null, editor);
        // itemPanel.init();  // show panel on start
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("RPSreporter")); // stored in jmri.jmrit.display.DisplayBundle
        _tabIndex.put("RPSReporter", iconPanel);

        iconPanel = new ClockItemPanel(palette, "FastClock", editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("FastClock"));
        _tabIndex.put("FastClock", iconPanel);

        itemPanel = new IndicatorItemPanel(palette, "IndicatorTrack", null, editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("IndicatorTrack"));
        _tabIndex.put("IndicatorTrack", itemPanel);

        itemPanel = new IndicatorTOItemPanel(palette, "IndicatorTO", null,
                PickListModel.turnoutPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("IndicatorTO"));
        _tabIndex.put("IndicatorTO", itemPanel);

        itemPanel = new PortalItemPanel(palette, "Portal", null, editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("BeanNamePortal"));
        _tabIndex.put("Portal", itemPanel);

        _tabPane.addChangeListener(palette);
        // _tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        // long t = System.currentTimeMillis();
        JTabbedPane tp = (JTabbedPane) e.getSource();
        JScrollPane sp = (JScrollPane) tp.getSelectedComponent();
        ItemPanel p = (ItemPanel) sp.getViewport().getView();
        p.init();
        log.debug("different tab displayed");
        if (_currentItemPanel != null) {
            _currentItemPanel.closeDialogs();
        }
        _currentItemPanel = p;
        pack();
    }

    private void makeMenus(Editor editor) {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
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
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
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

    /**
     * Look for duplicate name of family in the iterated set.
     */
    private static boolean familyNameOK(java.awt.Frame frame, String type, String family, Iterator<String> it) {
        if (family == null || family.length() == 0) {
            JOptionPane.showMessageDialog(frame,
                    Bundle.getMessage("EnterFamilyName"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        while (it.hasNext()) {
            if (family.equals(it.next())) {
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
            HashMap<String, HashMap<String, NamedIcon>> typeMap = new HashMap<String, HashMap<String, NamedIcon>>();
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
            InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
            return true;
        }
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
        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
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
            InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
            return true;
        }
        return false;
    }

    // add entire family
    static protected void addLevel4FamilyMap(String type, String family,
            String key, HashMap<String, NamedIcon> iconMap) {
        HashMap<String, HashMap<String, NamedIcon>> familyMap = getLevel4Family(type, family);
        familyMap.put(key, iconMap);
        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
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
            log.debug("removelvl4IconMap for indicator family \"{}\" in type \"{}\" with key \"{}\"",
                    family, type, key);
        }
        if (key != null) {
            _indicatorTOMaps.get(type).get(family).remove(key);
        } else {
            _indicatorTOMaps.get(type).remove(family);
        }
        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
    }

    ///////////////////////////////////////////////////////////////////////////////
    
    static protected HashMap<String, NamedIcon> cloneMap(HashMap<String, NamedIcon> map) {
        HashMap<String, NamedIcon> clone = new HashMap<String, NamedIcon>();
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
