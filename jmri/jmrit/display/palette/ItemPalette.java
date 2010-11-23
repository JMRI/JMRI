package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Component;
//import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jdom.Element;
import jmri.util.JmriJFrame;

import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;

import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;


/**
 * Container for adding items to control panels
 *
 * @author Pete Cressman  Copyright (c) 2010
 */

public class ItemPalette extends JmriJFrame /* implements ListSelectionListener, ChangeListener */ {

    public static final ResourceBundle rbp = ResourceBundle.getBundle("jmri.jmrit.display.palette.PaletteBundle");
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    
    JTabbedPane _tabPane;
//    HashMap <String, ItemPanel> _itemPanelMap = new HashMap <String, ItemPanel>();

    static HashMap<String, Hashtable<String, Hashtable<String, NamedIcon>>> _iconMaps;
    // for now, special case 4 level maps since IndicatorTO is the only case.
    static HashMap<String, Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>>> _indicatorTOMaps;

    /**
    * Store palette icons in preferences file catalogTrees.xml 
    */
    public static void storeIcons() {
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        // unfiltered, xml-stored, item palate icon tree
        CatalogTree tree = manager.getBySystemName("NXPI");
        // discard old version
        if (tree != null) {
            manager.deregister(tree);
        }
        tree = manager.newCatalogTree("NXPI", "Item Palette");
        CatalogTreeNode root = (CatalogTreeNode)tree.getRoot();
        
        Iterator<Entry<String, Hashtable<String, Hashtable<String, NamedIcon>>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Hashtable<String, Hashtable<String, NamedIcon>>> entry = it.next();
            root.add(store3levelMap(entry.getKey(), entry.getValue()));
            if (log.isDebugEnabled()) log.debug("Add type node "+entry.getKey());
        }

        Iterator<Entry<String, Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>>>> its = _indicatorTOMaps.entrySet().iterator();
        while (its.hasNext()) {
            Entry<String, Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>>> entry = its.next();
            CatalogTreeNode typeNode = new CatalogTreeNode(entry.getKey());
            Iterator<Entry<String, Hashtable<String, Hashtable<String, NamedIcon>>>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, Hashtable<String, Hashtable<String, NamedIcon>>> ent = iter.next();
                typeNode.add(store3levelMap(ent.getKey(), ent.getValue()));
                if (log.isDebugEnabled()) log.debug("Add IndicatorTO node "+ent.getKey());
            }
            root.add(typeNode);
            if (log.isDebugEnabled()) log.debug("Add IndicatorTO node "+entry.getKey());
        }
    }

    static CatalogTreeNode store3levelMap(String type, Hashtable<String, Hashtable<String, NamedIcon>> familyMap) {
        CatalogTreeNode typeNode = new CatalogTreeNode(type);
        Iterator<Entry<String, Hashtable<String, NamedIcon>>> iter = familyMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Hashtable<String, NamedIcon>> ent = iter.next();
            String family = ent.getKey();
            CatalogTreeNode familyNode = new CatalogTreeNode(family);
            Hashtable <String, NamedIcon> iconMap = ent.getValue(); 
            Iterator<Entry<String, NamedIcon>> iterat = iconMap.entrySet().iterator();
            while (iterat.hasNext()) {
                Entry<String, NamedIcon> e = iterat.next();
                String state = e.getKey();
                String path = e.getValue().getURL();
                familyNode.addLeaf(state, path);
            }
            typeNode.add(familyNode);
            if (log.isDebugEnabled()) log.debug("Add familyNode "+familyNode);
        }
        return typeNode;
    }

    static void loadIcons() {
        if (_iconMaps==null) {
            _iconMaps = new HashMap <String, Hashtable<String, Hashtable<String, NamedIcon>>>();
            _indicatorTOMaps = 
                new HashMap<String, Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>>>();

            if (!loadSavedIcons()) {
                loadDefaultIcons();
            }
        }
    }

    static boolean loadSavedIcons() {
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        CatalogTree tree = manager.getBySystemName("NXPI");
        if (tree != null) {
            CatalogTreeNode root = (CatalogTreeNode)tree.getRoot();
            @SuppressWarnings("unchecked")
            Enumeration<CatalogTreeNode> e = root.children();
            while (e.hasMoreElements()) {
                CatalogTreeNode node = e.nextElement();
                String typeName = (String)node.getUserObject();
                // detect this is a 4 level map collection. 
                // not very elegant (i.e. extensible), but maybe all that's needed.
                if (typeName.equals("IndicatorTO")) {
                    Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> familyTOMap =
                                                loadIndicatorFamilyMap(node);
                    _indicatorTOMaps.put(typeName, familyTOMap); 
                    if (log.isDebugEnabled()) log.debug("Add "+familyTOMap.size()+
                                    " indicatorTO families to item type "+typeName+" to _indicatorTOMaps.");
                } else {
                    Hashtable<String, Hashtable<String, NamedIcon>> familyMap = 
                                                loadFamilyMap(node);
                    _iconMaps.put(typeName, familyMap); 
                    if (log.isDebugEnabled()) log.debug("Add item type "+typeName+" to _iconMaps.");
                }
            }
            if (log.isDebugEnabled()) log.debug("Icon Map has "+_iconMaps.size()+" members");
            return true;
        }
        return false;
    }

    static Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> 
                                        loadIndicatorFamilyMap(CatalogTreeNode node) {
        Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> familyMap =
                                new Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>>();
        @SuppressWarnings("unchecked")
        Enumeration<CatalogTreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = ee.nextElement();
            String name = (String)famNode.getUserObject();
            familyMap.put(name, loadFamilyMap(famNode));
        }
        return familyMap;
    }

    static Hashtable<String, Hashtable<String, NamedIcon>> loadFamilyMap(CatalogTreeNode node) {
        Hashtable <String, Hashtable<String, NamedIcon>> familyMap =
                 new Hashtable <String, Hashtable<String, NamedIcon>> ();
        @SuppressWarnings("unchecked")
        Enumeration<CatalogTreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = ee.nextElement();
            String familyName = (String)famNode.getUserObject();
            Hashtable <String, NamedIcon> iconMap = new Hashtable <String, NamedIcon> ();
            List <CatalogTreeLeaf> list = famNode.getLeaves();
            int w = 0;
            int h = 0;
            for (int i=0; i<list.size(); i++) {
                String iconName = list.get(i).getName();
                CatalogTreeLeaf leaf = list.get(i);
                String path = leaf.getPath();
                NamedIcon icon = new NamedIcon(path, path);
                w = Math.max(w, icon.getIconWidth());
                h = Math.max(h, icon.getIconHeight());
                iconMap.put(iconName, icon);
                if (log.isDebugEnabled()) log.debug("Add "+iconName+" icon to family "+familyName);
            }
            familyMap.put(familyName, iconMap); 
        }
        return familyMap;
    }

    static void loadDefaultIcons() {
        File file = new File("xml"+File.separator+"defaultPanelIcons.xml");
        if (!file.exists()) {
            log.error("defaultPanelIcons file doesn't exist: "+file.getPath());
            throw new IllegalArgumentException("defaultPanelIcons file doesn't exist: "+file.getPath());
        }
        try {
            jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile(){};
            Element root = xf.rootFromFile(file);
            @SuppressWarnings("unchecked")
            List<Element> typeList = root.getChild("ItemTypes").getChildren();
            for (int i = 0; i < typeList.size(); i++) {
                String typeName = typeList.get(i).getName();
                @SuppressWarnings("unchecked")
                List<Element>families = typeList.get(i).getChildren();
                // detect this is a 4 level map collection. 
                // not very elegant (i.e. extensible), but maybe all that's needed.
                if (typeName.equals("IndicatorTO")) {
                    Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> familyTOMap =
                                                loadDefaultIndicatorTOMap(families);
                    _indicatorTOMaps.put(typeName, familyTOMap); 
                    if (log.isDebugEnabled()) log.debug("Add "+familyTOMap.size()+
                                    " indicatorTO families to item type "+typeName+" to _indicatorTOMaps.");
                } else {
                    Hashtable<String, Hashtable<String, NamedIcon>> familyMap = loadDefaultFamilyMap(families);
                    _iconMaps.put(typeName, familyMap); 
                    if (log.isDebugEnabled()) log.debug("Add "+familyMap.size()+
                                                        " families to item type "+typeName+" to _iconMaps.");
                }
            }
        } catch (org.jdom.JDOMException e) {
            log.error("error reading file \""+file.getName()+"\" due to: "+e);
        } catch (java.io.IOException ioe) {
            log.error("error reading file \""+file.getName()+"\" due to: "+ioe);
        }
    }

    static Hashtable<String, Hashtable<String, NamedIcon>> loadDefaultFamilyMap(List<Element> families)
    {
        Hashtable<String, Hashtable<String, NamedIcon>> familyMap =
                new Hashtable<String, Hashtable<String, NamedIcon>> ();
        for (int k = 0; k < families.size(); k++) {
            String familyName = families.get(k).getName();
            Hashtable <String, NamedIcon> iconMap = 
                    new Hashtable <String, NamedIcon> ();     // Map of all icons of in family, familyName
            @SuppressWarnings("unchecked")
            List<Element>iconfiles = families.get(k).getChildren();
            for (int j = 0; j < iconfiles.size(); j++) {
                String iconName = iconfiles.get(j).getName();
                String fileName = iconfiles.get(j).getText().trim();
                if (fileName==null || fileName.length()==0) {
                    fileName = "resources/icons/misc/X-red.gif";
                    log.warn("loadDefaultIcons: iconName= "+iconName+" in family "+familyName+" has no image file.");
                }
                NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
                iconMap.put(iconName, icon);
            }
            familyMap.put(familyName, iconMap); 
            if (log.isDebugEnabled()) log.debug("Add "+iconMap.size()+" icons to family "+familyName);
        }
        return familyMap;
    }

    static Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> loadDefaultIndicatorTOMap(List<Element> typeList)
                    throws org.jdom.JDOMException
    {
        Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> familyTOMap =
                new Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> ();     // Map of all families of type, typeName
        for (int k = 0; k < typeList.size(); k++) {
            String familyName = typeList.get(k).getName();
            @SuppressWarnings("unchecked")
            List<Element> types = typeList.get(k).getChildren();
            Hashtable<String, Hashtable<String, NamedIcon>> familyMap = loadDefaultFamilyMap(types);
            familyTOMap.put(familyName, familyMap);
            if (log.isDebugEnabled()) log.debug("Add "+familyMap.size()+
                                " IndicatorTO sub-families to item type "+familyName+" to IndicatorTO families.");
        }
        return familyTOMap;
    }

    public ItemPalette() {
        loadIcons();
    }

    public ItemPalette(String title, Editor editor) {
        setTitle(title);
        loadIcons();
        addWindowListener(new java.awt.event.WindowAdapter() {
                Editor editor;
                public void windowClosing(java.awt.event.WindowEvent e) {
                    ImageIndexEditor.checkImageIndex(editor);
                    storeIcons();
                }
                java.awt.event.WindowAdapter init(Editor ed) {
                    editor = ed;
                    return this;
                }
        }.init(editor));
        
        makeMenus(editor);

        _tabPane = new JTabbedPane();
        ItemPanel itemPanel = new TableItemPanel(this, "Turnout",
                                       PickListModel.turnoutPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("Turnout", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Turnout"));
        
        itemPanel = new TableItemPanel(this, "Sensor",
                                       PickListModel.sensorPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("Sensor", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Sensor"));

        itemPanel = new SignalHeadItemPanel(this, "SignalHead",
                                       PickListModel.signalHeadPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("SignalHead", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("SignalHead"));

        itemPanel = new SignalMastItemPanel(this, "SignalMast",
                                            PickListModel.signalMastPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("SignalMast", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("SignalMast"));

        itemPanel = new MemoryItemPanel(this, "Memory",
                                        PickListModel.memoryPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("Memory", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Memory"));

        itemPanel = new ReporterItemPanel(this, "Reporter",
                                          PickListModel.reporterPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("Reporter", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Reporter"));

        itemPanel = new TableItemPanel(this, "Light",
                                       PickListModel.lightPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("Light", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Light"));

        itemPanel = new MultiSensorItemPanel(this, "MultiSensor",
                                             PickListModel.multiSensorPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("MultiSensor", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("MultiSensor"));
 
        ItemPanel iconPanel = new IconItemPanel(this, "Icon", editor);
        iconPanel.init();
//        _itemPanelMap.put("Icon", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("Icon"));
 
        iconPanel = new BackgroundItemPanel(this, "Background", editor);
        iconPanel.init();
//        _itemPanelMap.put("Background", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("Background")); 

        iconPanel = new TextItemPanel(this, "Text", editor);
        iconPanel.init();
//        _itemPanelMap.put("Text", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("Text"));     

        iconPanel = new RPSItemPanel(this, "RPSReporter", editor);
        iconPanel.init();
//        _itemPanelMap.put("RPSReporter", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("RPSReporter")); 

        iconPanel = new ClockItemPanel(this, "FastClock", editor);
        iconPanel.init();
//        _itemPanelMap.put("FastClock", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("FastClock")); 

/*
* Hold until documented.
***/
        itemPanel = new IndicatorItemPanel(this, "IndicatorTrack", editor);
        itemPanel.init();
//        _itemPanelMap.put("IndicatorTrack", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("IndicatorTrack"));

        itemPanel = new IndicatorTOItemPanel(this, "IndicatorTO",
                                       PickListModel.turnoutPickModelInstance(), editor);
        itemPanel.init();
//        _itemPanelMap.put("IndicatorTO", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("IndicatorTO"));
/***/
        setLayout(new BorderLayout(5,5));
        add(_tabPane, BorderLayout.CENTER);
        setLocation(10,10);               
//        setVisible(true);                   
        pack();
    }

    private void makeMenus(Editor editor) {
        java.util.ResourceBundle rbd = java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(rbd.getString("findIconMenu"));
        menuBar.add(findIcon);

        JMenuItem editItem = new JMenuItem(rbd.getString("editIndexMenu"));
        editItem.addActionListener(new ActionListener() {
                Editor editor;
                public void actionPerformed(ActionEvent e) {
                    ImageIndexEditor ii = ImageIndexEditor.instance(editor);
                    ii.pack();
                    ii.setVisible(true);
                }
                ActionListener init(Editor ed) {
                    editor = ed;
                    return this;
                }
            }.init(editor));
        findIcon.add(editItem);
        findIcon.addSeparator();

        JMenuItem searchItem = new JMenuItem(rbd.getString("searchFSMenu"));
        findIcon.add(searchItem);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.picker.PickTables", true);
    }

    /**
    * Creating new Families of icons. Superimposes a new modal JDialog with "default"
    * icons to be accepted (click done button and new family is added to type) -or
    * rejected (dismiss or clisk cancel button) 
    */
    static protected void createNewFamily(String type, ItemPanel parent) {
        if (type.equals("MultiSensor")) {
            new MultiSensorIconDialog(type, null, parent);
        } else if (type.equals("Icon") || type.equals("Background")) {
            new SingleIconDialog(type, null, parent);
        } else {
            new IconDialog(type, null, parent);
        }
    }

    /**
    * Adding a new Family of icons to the device type
    */
    static protected void addFamily(String type, String family, Hashtable<String, NamedIcon> iconMap) {
        getFamilyMaps(type).put(family, iconMap);
    }
    // Currently only needed for IndicatorTO type
    static protected void addLevel4Family(String type, String family, String key,
                                   Hashtable<String, NamedIcon> iconMap) {
        getLevel4FamilyMaps(type).get(family).put(key, iconMap);
    }
    static protected void addLevel4Family(String type, String family,
                                   Hashtable<String, Hashtable<String, NamedIcon>> iconMap) {
        getLevel4FamilyMaps(type).put(family, iconMap);
    }

    /**
    * Getting all the Families of icons for a given device type
    */
    static protected Hashtable<String, Hashtable<String, NamedIcon>> getFamilyMaps(String type) {
       return _iconMaps.get(type);
    }
    // Currently only needed for IndicatorTO type
    static protected Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> getLevel4FamilyMaps(String type) {
       return _indicatorTOMaps.get(type);
    }

    /**
    * Removing a Family of icons from the device type
    */
   static protected void removeIconMap(String type, String family) {
        if (log.isDebugEnabled()) log.debug("removeIconMap for family \""+family+" \" in type \""+type+"\"");
        _iconMaps.get(type).remove(family);
        if (log.isDebugEnabled()) {
            Hashtable <String, Hashtable<String, NamedIcon>> families = getFamilyMaps(type);
            if (families!=null && families.size()>0) {
                Iterator <String> it = families.keySet().iterator();
                while (it.hasNext()) {
                    log.debug("removeIconMap remaining Keys: family \""+it.next()+" \" in type \""+type+"\"");
                }
            }
        }
    }
    // Currently only needed for IndicatorTO type
    static protected void removeLevel4IconMap(String type, String family, String key) {
        if (log.isDebugEnabled()) log.debug("removelvl4IconMap for indicator family \""+family+" \" in type \""+type+
                                            "\" with key = \""+key+"\"");
        if (key!=null) {
            _indicatorTOMaps.get(type).get(family).remove(key);
        } else {
            _indicatorTOMaps.get(type).remove(family);
        }
    }

    /**
    * Getting a clone of the Family of icons for a given device type and family
    */
    static protected Hashtable<String, NamedIcon> getIconMap(String type, String family) {
        Hashtable <String, Hashtable<String, NamedIcon>> itemMap = _iconMaps.get(type);
        if (itemMap==null) {
            log.error("getIconMap failed. item type \""+type+"\" not found.");
            return null;
        }
        Hashtable<String, NamedIcon> iconMap = itemMap.get(family);
        if (iconMap==null) {
            log.error("getIconMap failed. family \""+family+"\" not found in item type \""+type+"\".");
            return null;
        }
        return cloneMap(iconMap);
    }

    static protected Hashtable<String, NamedIcon> cloneMap(Hashtable<String, NamedIcon> map) {
        Hashtable<String, NamedIcon> clone = new Hashtable<String, NamedIcon>();
        if (map!=null) {
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

    static protected String convertText(String name) {
        String cName = null;
        try {
            cName = ItemPalette.rbean.getString(name);
        } catch (java.util.MissingResourceException mre) {
            try {
                cName = ItemPalette.rbp.getString(name);
            } catch (java.util.MissingResourceException mre2) {
                cName = name;
            }
        }
        return cName;
    }

    static protected JPanel makeBannerPanel(String labelText, Component field) {
        JPanel panel = new JPanel(); 
//        panel.setLayout(new FlowLayout());
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        panel.add(new JLabel(ItemPalette.rbp.getString(labelText)), c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        panel.add(field, c);
        return panel;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ItemPalette.class.getName());
}
