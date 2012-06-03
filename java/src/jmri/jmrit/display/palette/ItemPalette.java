package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Component;
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

    public static final int STRUT_SIZE = 10;
    
    static JTabbedPane _tabPane;
    static HashMap<String, ItemPanel> _tabIndex;

    static HashMap<String, Hashtable<String, Hashtable<String, NamedIcon>>> _iconMaps;
    // for now, special case 4 level maps since IndicatorTO is the only case.
    static HashMap<String, Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>>> _indicatorTOMaps;
    
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

    static public void loadIcons() {
        if (_iconMaps==null) {
//        	long t = System.currentTimeMillis();
            _iconMaps = new HashMap <String, Hashtable<String, Hashtable<String, NamedIcon>>>();
            _indicatorTOMaps = 
                new HashMap<String, Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>>>();

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
                    Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> familyTOMap =
                                                loadIndicatorFamilyMap(node);
                    if (log.isDebugEnabled()) log.debug("Add "+familyTOMap.size()+
                                    " indicatorTO families to item type "+typeName+" to _indicatorTOMaps.");
                    _indicatorTOMaps.put(typeName, familyTOMap); 
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
            Thread.yield();
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
                Thread.yield();
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
        super(false, false);
        loadIcons();
    }

    public ItemPalette(String title, Editor editor) {
        super(title, false, false);
//        long t = System.currentTimeMillis();
        loadIcons();
        addWindowListener(new java.awt.event.WindowAdapter() {
                Editor editor;
                public void windowClosing(java.awt.event.WindowEvent e) {
                    closePanels(e);
                    if (ImageIndexEditor.checkImageIndex(editor)) {
                        storeIcons();   // write maps to tree
                    }
                }
                java.awt.event.WindowAdapter init(Editor ed) {
                    editor = ed;
                    return this;
                }
        }.init(editor));
        
        makeMenus(editor);

        _tabPane = new JTabbedPane();
        _tabIndex = new HashMap<String, ItemPanel>();
        
        ItemPanel itemPanel = new TableItemPanel(this, "Turnout", null,
                                       PickListModel.turnoutPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("Turnout"));
        _tabIndex.put("Turnout", itemPanel);
        
        itemPanel = new TableItemPanel(this, "Sensor", null,
                                       PickListModel.sensorPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("Sensor"));
        _tabIndex.put("Sensor", itemPanel);

        itemPanel = new SignalHeadItemPanel(this, "SignalHead", null,
                                       PickListModel.signalHeadPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("SignalHead"));
        _tabIndex.put("SignalHead", itemPanel);

        itemPanel = new SignalMastItemPanel(this, "SignalMast", null,
                                            PickListModel.signalMastPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("SignalMast"));
        _tabIndex.put("SignalMast", itemPanel);

        itemPanel = new MemoryItemPanel(this, "Memory", null,
                                        PickListModel.memoryPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("Memory"));
        _tabIndex.put("Memory", itemPanel);

        itemPanel = new ReporterItemPanel(this, "Reporter", null,
                                          PickListModel.reporterPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("Reporter"));
        _tabIndex.put("Reporter", itemPanel);

        itemPanel = new TableItemPanel(this, "Light", null,
                                       PickListModel.lightPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(itemPanel, rbp.getString("Light"));
        _tabIndex.put("Light", itemPanel);

        itemPanel = new MultiSensorItemPanel(this, "MultiSensor", null,
                                             PickListModel.multiSensorPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("MultiSensor"));
        _tabIndex.put("MultiSensor", itemPanel);
 
        ItemPanel iconPanel = new IconItemPanel(this, "Icon", null, editor);
        iconPanel.init();
        _tabPane.add(new JScrollPane(iconPanel), rbp.getString("Icon"));
        _tabIndex.put("Icon", itemPanel);
 
        iconPanel = new BackgroundItemPanel(this, "Background", null, editor);
        iconPanel.init();
        _tabPane.add(new JScrollPane(iconPanel), rbp.getString("Background")); 
        _tabIndex.put("Background", itemPanel);

        iconPanel = new TextItemPanel(this, "Text", null, editor);
        iconPanel.init();
        _tabPane.add(new JScrollPane(iconPanel), rbp.getString("Text"));     
        _tabIndex.put("Text", itemPanel);

        iconPanel = new RPSItemPanel(this, "RPSReporter", null, editor);
        iconPanel.init();
        _tabPane.add(new JScrollPane(iconPanel), rbp.getString("RPSReporter")); 
        _tabIndex.put("RPSReporter", itemPanel);

        iconPanel = new ClockItemPanel(this, "FastClock", null, editor);
        iconPanel.init();
        _tabPane.add(new JScrollPane(iconPanel), rbp.getString("FastClock")); 
        _tabIndex.put("FastClock", itemPanel);

        itemPanel = new IndicatorItemPanel(this, "IndicatorTrack", null, editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("IndicatorTrack"));
        _tabIndex.put("IndicatorTrack", itemPanel);

        itemPanel = new IndicatorTOItemPanel(this, "IndicatorTO", null,
                                       PickListModel.turnoutPickModelInstance(), editor);
        itemPanel.init();
        _tabPane.add(new JScrollPane(itemPanel), rbp.getString("IndicatorTO"));
        _tabIndex.put("IndicatorTO", itemPanel);

        setLayout(new BorderLayout(5,5));
        add(_tabPane, BorderLayout.CENTER);
        setLocation(10,10);               
        pack();
//        System.out.println("Palette built in "+ (System.currentTimeMillis()-t)+ " milliseconds.");
   }

    private void makeMenus(Editor editor) {
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(rb.getString("findIconMenu"));
        menuBar.add(findIcon);

        JMenuItem editItem = new JMenuItem(rb.getString("editIndexMenu"));
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

        JMenuItem searchItem = new JMenuItem(rb.getString("searchFSMenu"));
        findIcon.add(searchItem);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.display.ItemPalette", true);
    }

    public void closePanels(java.awt.event.WindowEvent e) {
        java.awt.Component[] comps = _tabPane.getComponents();
        if (log.isDebugEnabled()) log.debug("closePanels: tab count= "+_tabPane.getTabCount());
        for (int i=0; i<comps.length; i++) {
            if (comps[i] instanceof ItemPanel) {
                //log.debug("windowClosing "+i+"th panel= "+comps[i].getClass().getName());
                ((ItemPanel)comps[i]).dispose();
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
                    ItemPalette.rbp.getString("EnterFamilyName"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        while (it.hasNext()) {
           if (family.equals(it.next())) {
               JOptionPane.showMessageDialog(frame,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateFamilyName"), 
                    new Object[] { family, type }), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
               return false;
           }
        }
        return true;
    }

    /**
    * Adding a new Family of icons to the device type
    */
    static protected boolean addFamily(java.awt.Frame frame, String type, String family, Hashtable<String, NamedIcon> iconMap) {
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
    static protected Hashtable<String, Hashtable<String, NamedIcon>> getFamilyMaps(String type) {
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
            Hashtable <String, Hashtable<String, NamedIcon>> families = getFamilyMaps(type);
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

    /************** Currently only needed for IndicatorTO type ***************/

    // add entire family
    static protected boolean addLevel4Family(java.awt.Frame frame, String type, String family,
                                   Hashtable<String, Hashtable<String, NamedIcon>> iconMap) {
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
                                   String key, Hashtable<String, NamedIcon> iconMap) {
        Hashtable<String, Hashtable<String, NamedIcon>> familyMap = getLevel4Family(type, family);
        familyMap.put(key, iconMap);
        ImageIndexEditor.indexChanged(true);
    }

    // Currently only needed for IndicatorTO type
    static protected Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> 
                                getLevel4FamilyMaps(String type) {
        return _indicatorTOMaps.get(type);
    }
    // Currently only needed for IndicatorTO type
    static protected Hashtable<String, Hashtable<String, NamedIcon>> 
                                getLevel4Family(String type, String family) {
        Hashtable<String, Hashtable<String, Hashtable<String, NamedIcon>>> map = _indicatorTOMaps.get(type);
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

    static public String convertText(String name) {
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
