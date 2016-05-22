package jmri.jmrit.display.palette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.List;
import java.io.File;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jdom.Element;
import jmri.util.JmriJFrame;

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


/**
 * Container for adding items to control panels.
 * Singleton class loads and stores icons used in panels.
 *
 * @author Pete Cressman  Copyright (c) 2010
 */

public class ItemPalette extends JmriJFrame implements ChangeListener  {

    public static final int STRUT_SIZE = 10;
    
    static JTabbedPane _tabPane;
    static HashMap<String, ItemPanel> _tabIndex;
    static ItemPalette _instance;

    static HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> _iconMaps;
    // for now, special case 4 level maps since IndicatorTO is the only case.
    static HashMap<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>> _indicatorTOMaps;
    ItemPanel _currentItemPanel;
    
    /**
    * Store palette icons in preferences file catalogTrees.xml 
    */
    public static void storeIcons() {
        if (_iconMaps==null) {
            return;     // never loaded
        }
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        // unfiltered, xml-stored, item palate icon tree
        CatalogTree tree = manager.getBySystemName("NXPI");
        // discard old version
        if (tree != null) {
            manager.deregister(tree);
        }
        tree = manager.newCatalogTree("NXPI", "Item Palette");
        CatalogTreeNode root = (CatalogTreeNode)tree.getRoot();
        
        Iterator<Entry<String, HashMap<String, HashMap<String, NamedIcon>>>> it = _iconMaps.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashMap<String, HashMap<String, NamedIcon>>> entry = it.next();
            root.add(store3levelMap(entry.getKey(), entry.getValue()));
            if (log.isDebugEnabled()) log.debug("Add type node "+entry.getKey());
        }

        Iterator<Entry<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>>> its = _indicatorTOMaps.entrySet().iterator();
        while (its.hasNext()) {
            Entry<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>> entry = its.next();
            CatalogTreeNode typeNode = new CatalogTreeNode(entry.getKey());
            Iterator<Entry<String, HashMap<String, HashMap<String, NamedIcon>>>> iter = entry.getValue().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, HashMap<String, HashMap<String, NamedIcon>>> ent = iter.next();
                typeNode.add(store3levelMap(ent.getKey(), ent.getValue()));
                if (log.isDebugEnabled()) log.debug("Add IndicatorTO node "+ent.getKey());
            }
            root.add(typeNode);
            if (log.isDebugEnabled()) log.debug("Add IndicatorTO node "+entry.getKey());
        }
    }

    static CatalogTreeNode store3levelMap(String type, HashMap<String, HashMap<String, NamedIcon>> familyMap) {
        CatalogTreeNode typeNode = new CatalogTreeNode(type);
        Iterator<Entry<String, HashMap<String, NamedIcon>>> iter = familyMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, HashMap<String, NamedIcon>> ent = iter.next();
            String family = ent.getKey();
            CatalogTreeNode familyNode = new CatalogTreeNode(family);
            HashMap <String, NamedIcon> iconMap = ent.getValue(); 
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

    static public void loadIcons() {
        if (_iconMaps==null) {
//        	long t = System.currentTimeMillis();
            _iconMaps = new HashMap <String, HashMap<String, HashMap<String, NamedIcon>>>();
            _indicatorTOMaps = 
                new HashMap<String, HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>>();

            if (!loadSavedIcons()) {
                loadDefaultIcons();
            }
//            System.out.println("Palette icons loaded in "+ (System.currentTimeMillis()-t)+ " milliseconds.");
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
                    HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap =
                                                loadIndicatorFamilyMap(node);
                    if (log.isDebugEnabled()) log.debug("Add "+familyTOMap.size()+
                                    " indicatorTO families to item type "+typeName+" to _indicatorTOMaps.");
                    _indicatorTOMaps.put(typeName, familyTOMap); 
                } else {
                    HashMap<String, HashMap<String, NamedIcon>> familyMap = 
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

    static HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> 
                                        loadIndicatorFamilyMap(CatalogTreeNode node) {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyMap =
                                new HashMap<String, HashMap<String, HashMap<String, NamedIcon>>>();
        @SuppressWarnings("unchecked")
        Enumeration<CatalogTreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = ee.nextElement();
            String name = (String)famNode.getUserObject();
            familyMap.put(name, loadFamilyMap(famNode));
            Thread.yield();
        }
        return familyMap;
    }

    static HashMap<String, HashMap<String, NamedIcon>> loadFamilyMap(CatalogTreeNode node) {
        HashMap <String, HashMap<String, NamedIcon>> familyMap =
                 new HashMap <String, HashMap<String, NamedIcon>> ();
        @SuppressWarnings("unchecked")
        Enumeration<CatalogTreeNode> ee = node.children();
        while (ee.hasMoreElements()) {
            CatalogTreeNode famNode = ee.nextElement();
            String familyName = (String)famNode.getUserObject();
            HashMap <String, NamedIcon> iconMap = new HashMap <String, NamedIcon> ();
            List <CatalogTreeLeaf> list = famNode.getLeaves();
            int w = 0;
            int h = 0;
            for (int i=0; i<list.size(); i++) {
                String iconName = list.get(i).getName();
                CatalogTreeLeaf leaf = list.get(i);
                String path = leaf.getPath();
                try {
                    NamedIcon icon = new NamedIcon(path, path);                	
                } catch (Exception e) {
                	throw new NullPointerException("Can't find icon file: "+path);
                }
                NamedIcon icon = new NamedIcon(path, path);
                w = Math.max(w, icon.getIconWidth());
                h = Math.max(h, icon.getIconHeight());
                iconMap.put(iconName, icon);
                if (log.isDebugEnabled()) log.debug("Add "+iconName+" icon to family "+familyName);
                Thread.yield();
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
                    HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap =
                                                loadDefaultIndicatorTOMap(families);
                    _indicatorTOMaps.put(typeName, familyTOMap); 
                    if (log.isDebugEnabled()) log.debug("Add "+familyTOMap.size()+
                                    " indicatorTO families to item type "+typeName+" to _indicatorTOMaps.");
                } else {
                    HashMap<String, HashMap<String, NamedIcon>> familyMap = loadDefaultFamilyMap(families);
                    _iconMaps.put(typeName, familyMap); 
                    if (log.isDebugEnabled()) log.debug("Add "+familyMap.size()+
                                                        " families to item type "+typeName+" to _iconMaps.");
                }
                Thread.yield();
            }
        } catch (org.jdom.JDOMException e) {
            log.error("error reading file \""+file.getName()+"\" due to: "+e);
        } catch (java.io.IOException ioe) {
            log.error("error reading file \""+file.getName()+"\" due to: "+ioe);
        }
    }

    static HashMap<String, HashMap<String, NamedIcon>> loadDefaultFamilyMap(List<Element> families)
    {
        HashMap<String, HashMap<String, NamedIcon>> familyMap =
                new HashMap<String, HashMap<String, NamedIcon>> ();
        for (int k = 0; k < families.size(); k++) {
            String familyName = families.get(k).getName();
            HashMap <String, NamedIcon> iconMap = 
                    new HashMap <String, NamedIcon> ();     // Map of all icons of in family, familyName
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

    static HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> loadDefaultIndicatorTOMap(List<Element> typeList)
    {
        HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> familyTOMap =
                new HashMap<String, HashMap<String, HashMap<String, NamedIcon>>> ();     // Map of all families of type, typeName
        for (int k = 0; k < typeList.size(); k++) {
            String familyName = typeList.get(k).getName();
            @SuppressWarnings("unchecked")
            List<Element> types = typeList.get(k).getChildren();
            HashMap<String, HashMap<String, NamedIcon>> familyMap = loadDefaultFamilyMap(types);
            familyTOMap.put(familyName, familyMap);
            if (log.isDebugEnabled()) log.debug("Add "+familyMap.size()+
                                " IndicatorTO sub-families to item type "+familyName+" to IndicatorTO families.");
        }
        return familyTOMap;
    }

    static public ItemPalette getInstance(String title, Editor editor) {
    	if (_instance==null) {
    		_instance= new ItemPalette(title, editor);
    	}
    	return _instance;    	
    }

    private ItemPalette(String title, Editor editor) {
        super(title, true, true);
//        long t = System.currentTimeMillis();
        loadIcons();
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    closePanels(e);
                    ImageIndexEditor.checkImageIndex();
                }
        	});
        
        makeMenus(editor);
        buildTabPane(this, editor);

        setLayout(new BorderLayout(5,5));
        add(_tabPane, BorderLayout.CENTER);
        setLocation(10,10);
        JScrollPane sp = (JScrollPane)_tabPane.getSelectedComponent();
        _currentItemPanel = (ItemPanel)sp.getViewport().getView();
        pack();
//        System.out.println("Palette built in "+ (System.currentTimeMillis()-t)+ " milliseconds.");
    }
    
    static void buildTabPane(ItemPalette palette, Editor editor) {
        _tabPane = new JTabbedPane();
        _tabIndex = new HashMap<String, ItemPanel>();
        
        ItemPanel itemPanel = new TableItemPanel(palette, "Turnout", null,
                                       PickListModel.turnoutPickModelInstance(), editor);
        itemPanel.init();		// show panel on start
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("Turnout"));
        _tabIndex.put("Turnout", itemPanel);
        
        itemPanel = new TableItemPanel(palette, "Sensor", null,
                                       PickListModel.sensorPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("Sensor"));
        _tabIndex.put("Sensor", itemPanel);

        itemPanel = new SignalHeadItemPanel(palette, "SignalHead", null,
                                       PickListModel.signalHeadPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("SignalHead"));
        _tabIndex.put("SignalHead", itemPanel);

        itemPanel = new SignalMastItemPanel(palette, "SignalMast", null,
                                            PickListModel.signalMastPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("SignalMast"));
        _tabIndex.put("SignalMast", itemPanel);

        itemPanel = new MemoryItemPanel(palette, "Memory", null,
                                        PickListModel.memoryPickModelInstance(), editor);
         _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("Memory"));
        _tabIndex.put("Memory", itemPanel);

        itemPanel = new ReporterItemPanel(palette, "Reporter", null,
                                          PickListModel.reporterPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("Reporter"));
        _tabIndex.put("Reporter", itemPanel);

       itemPanel = new TableItemPanel(palette, "Light", null,
                                       PickListModel.lightPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("Light"));
        _tabIndex.put("Light", itemPanel);

        itemPanel = new MultiSensorItemPanel(palette, "MultiSensor", null,
                                             PickListModel.multiSensorPickModelInstance(), editor);
         _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("MultiSensor"));
        _tabIndex.put("MultiSensor", itemPanel);
 
        ItemPanel iconPanel = new IconItemPanel(palette, "Icon", null, editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("Icon"));
        _tabIndex.put("Icon", itemPanel);
 
        iconPanel = new BackgroundItemPanel(palette, "Background", null, editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("Background")); 
        _tabIndex.put("Background", itemPanel);

        iconPanel = new TextItemPanel(palette, "Text", null, editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("Text"));     
        _tabIndex.put("Text", itemPanel);

        iconPanel = new RPSItemPanel(palette, "RPSReporter", null, editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("RPSReporter")); 
        _tabIndex.put("RPSReporter", itemPanel);

        iconPanel = new ClockItemPanel(palette, "FastClock", null, editor);
        _tabPane.add(new JScrollPane(iconPanel), Bundle.getMessage("FastClock")); 
        _tabIndex.put("FastClock", itemPanel);

        itemPanel = new IndicatorItemPanel(palette, "IndicatorTrack", null, editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("IndicatorTrack"));
        _tabIndex.put("IndicatorTrack", itemPanel);

        itemPanel = new IndicatorTOItemPanel(palette, "IndicatorTO", null,
                                       PickListModel.turnoutPickModelInstance(), editor);
        _tabPane.add(new JScrollPane(itemPanel), Bundle.getMessage("IndicatorTO"));
        _tabIndex.put("IndicatorTO", itemPanel);
        
        _tabPane.addChangeListener(palette);
    	
    }

    public void stateChanged(ChangeEvent e) {
//        long t = System.currentTimeMillis();
        JTabbedPane tp = (JTabbedPane)e.getSource();
        JScrollPane sp = (JScrollPane)tp.getSelectedComponent();
        ItemPanel p = (ItemPanel)sp.getViewport().getView();
    	p.init();
    	if (_currentItemPanel!=null) {
        	_currentItemPanel.closeDialogs();    		
    	}
    	_currentItemPanel = p;
//        System.out.println("Panel "+p._itemType+" built in "+ (System.currentTimeMillis()-t)+ " milliseconds.");
    }

    private void makeMenus(Editor editor) {
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(Bundle.getMessage("findIconMenu"));
        menuBar.add(findIcon);

        JMenuItem editItem = new JMenuItem(Bundle.getMessage("editIndexMenu"));
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
        
        JMenuItem openItem = new JMenuItem(Bundle.getMessage("openDirMenu"));
        openItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    DirectorySearcher.instance().openDirectory(false);
                }
            });
        findIcon.add(openItem);
/*
        JMenuItem searchItem = new JMenuItem(Bundle.getMessage("searchFSMenu"));
        searchItem.addActionListener(new ActionListener() {
            IconAdder ea;
            public void actionPerformed(ActionEvent e) {
                File dir = jmri.jmrit.catalog.DirectorySearcher.instance().searchFS();
                if (dir != null) {
                    ea.addDirectoryToCatalog(dir);
                }
            }
            ActionListener init() {
//                ea = ed;
                return this;
            }
    	}.init());
        findIcon.add(searchItem);
*/        
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.display.ItemPalette", true);
    }

    public void closePanels(java.awt.event.WindowEvent e) {
        java.awt.Component[] comps = _tabPane.getComponents();
        if (log.isDebugEnabled()) log.debug("closePanels: tab count= "+_tabPane.getTabCount());
        for (int i=0; i<comps.length; i++) {
        	javax.swing.JViewport vp = (javax.swing.JViewport)((JScrollPane)comps[i]).getComponent(0);
            java.awt.Component ip = vp.getView();
        	if (ip instanceof ItemPanel) {
                ((ItemPanel)ip).dispose();                		
        	}
        }
        super.windowClosing(e);
    }

    /**
    * Look for duplicate name of family in the iterated set
    */
    static boolean familyNameOK(java.awt.Frame frame, String type, String family, Iterator <String> it) {
        if (family==null || family.length()==0) {
            JOptionPane.showMessageDialog(frame, 
                    Bundle.getMessage("EnterFamilyName"), 
                    Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        while (it.hasNext()) {
           if (family.equals(it.next())) {
               JOptionPane.showMessageDialog(frame,
                    java.text.MessageFormat.format(Bundle.getMessage("DuplicateFamilyName"), 
                    new Object[] { family, type }), 
                    Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
               return false;
           }
        }
        return true;
    }

    /**
    * Adding a new Family of icons to the device type
    */
    static protected boolean addFamily(java.awt.Frame frame, String type, String family, HashMap<String, NamedIcon> iconMap) {
        Iterator <String> iter = ItemPalette.getFamilyMaps(type).keySet().iterator();
        if (familyNameOK(frame, type, family, iter)) {
            getFamilyMaps(type).put(family, iconMap);
            ItemPanel itemPanel = _tabIndex.get(type);
            if (itemPanel instanceof FamilyItemPanel) {
            	((FamilyItemPanel)itemPanel).updateFamiliesPanel();
            }
            ImageIndexEditor.indexChanged(true);
            return true;
        }
        return false;
    }

    /**
    * Getting all the Families of icons for a given device type
    */
    static protected HashMap<String, HashMap<String, NamedIcon>> getFamilyMaps(String type) {
        return _iconMaps.get(type);
    }

    /**
    * Removing a Family of icons from the device type
    */
   static protected void removeIconMap(String type, String family) {
        if (log.isDebugEnabled()) log.debug("removeIconMap for family \""+family+" \" in type \""+type+"\"");
        _iconMaps.get(type).remove(family);
        ImageIndexEditor.indexChanged(true);
        if (log.isDebugEnabled()) {
            HashMap <String, HashMap<String, NamedIcon>> families = getFamilyMaps(type);
            if (families!=null && families.size()>0) {
                Iterator <String> it = families.keySet().iterator();
                while (it.hasNext()) {
                    log.debug("removeIconMap remaining Keys: family \""+it.next()+" \" in type \""+type+"\"");
                }
            }
        }
    }

    /**
    * Getting a clone of the Family of icons for a given device type and family
    */
    static protected HashMap<String, NamedIcon> getIconMap(String type, String family) {
        HashMap <String, HashMap<String, NamedIcon>> itemMap = _iconMaps.get(type);
        if (itemMap==null) {
            log.error("getIconMap failed. item type \""+type+"\" not found.");
            return null;
        }
        HashMap<String, NamedIcon> iconMap = itemMap.get(family);
        if (iconMap==null) {
            log.error("getIconMap failed. family \""+family+"\" not found in item type \""+type+"\".");
            return null;
        }
        return cloneMap(iconMap);
    }

    /************** Currently only needed for IndicatorTO type ***************/

    // add entire family
    static protected boolean addLevel4Family(java.awt.Frame frame, String type, String family,
                                   HashMap<String, HashMap<String, NamedIcon>> iconMap) {
        Iterator <String> iter = ItemPalette.getLevel4FamilyMaps(type).keySet().iterator();
        if (familyNameOK(frame, type, family, iter)) {
            getLevel4FamilyMaps(type).put(family, iconMap);
            ImageIndexEditor.indexChanged(true);
            return true;
        }
        return false;
    }

    // add entire family
    static protected void addLevel4FamilyMap(String type, String family,
                                   String key, HashMap<String, NamedIcon> iconMap) {
        HashMap<String, HashMap<String, NamedIcon>> familyMap = getLevel4Family(type, family);
        familyMap.put(key, iconMap);
        ImageIndexEditor.indexChanged(true);
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
        if (log.isDebugEnabled()) log.debug("removelvl4IconMap for indicator family \""+family+" \" in type \""+type+
                                            "\" with key = \""+key+"\"");
        if (key!=null) {
            _indicatorTOMaps.get(type).get(family).remove(key);
        } else {
            _indicatorTOMaps.get(type).remove(family);
        }
        ImageIndexEditor.indexChanged(true);
    }
    /**************************************************************************/


    static protected HashMap<String, NamedIcon> cloneMap(HashMap<String, NamedIcon> map) {
        HashMap<String, NamedIcon> clone = new HashMap<String, NamedIcon>();
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

    static public String convertText(String name) {
        String cName = null;
        try {
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
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        panel.add(new JLabel(Bundle.getMessage(labelText)), c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        panel.add(field, c);
        return panel;
    }

    static Logger log = LoggerFactory.getLogger(ItemPalette.class.getName());
}
