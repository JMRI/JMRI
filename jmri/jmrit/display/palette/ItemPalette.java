package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
//import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
/*
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent; 
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
*/

import org.jdom.Element;
import jmri.util.JmriJFrame;

import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
//import jmri.NamedBean;

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
    HashMap <String, ItemPanel> _itemPanelMap = new HashMap <String, ItemPanel>();

    static HashMap <String, Hashtable<String, Hashtable<String, NamedIcon>>> _iconMaps;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="WMI_WRONG_MAP_ITERATOR", justification="iterator really short, efficiency not as important as clarity here")
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

        Iterator <String> it = _iconMaps.keySet().iterator();
        while (it.hasNext()) {
            String type = it.next();
            CatalogTreeNode typeNode = new CatalogTreeNode(type);
            Hashtable <String, Hashtable<String, NamedIcon>> familyMap = _iconMaps.get(type);
            Iterator <String> iter = familyMap.keySet().iterator();
            while (iter.hasNext()) {
                String family = iter.next();
                CatalogTreeNode familyNode = new CatalogTreeNode(family);
                Hashtable <String, NamedIcon> iconMap = familyMap.get(family); 
                Iterator <String> iterat = iconMap.keySet().iterator();
                while (iterat.hasNext()) {
                    String state = iterat.next();
                    String path = iconMap.get(state).getURL();
                    familyNode.addLeaf(state, path);
                }
                typeNode.add(familyNode);
                if (log.isDebugEnabled()) log.debug("Add familyNode "+familyNode);
            }
            root.add(typeNode);
            if (log.isDebugEnabled()) log.debug("Add typeNode "+typeNode);
        }
    }

    static void loadIcons() {
        _iconMaps = new HashMap <String, Hashtable<String, Hashtable<String, NamedIcon>>> ();

        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        CatalogTree tree = manager.getBySystemName("NXPI");
        if (tree != null) {
            CatalogTreeNode root = (CatalogTreeNode)tree.getRoot();
            @SuppressWarnings("unchecked")
            Enumeration<CatalogTreeNode> e = root.children();
            while (e.hasMoreElements()) {
                CatalogTreeNode node = e.nextElement();
                String typeName = (String)node.getUserObject();
                Hashtable <String, Hashtable<String, NamedIcon>> familyMap = new Hashtable <String, Hashtable<String, NamedIcon>> ();     // Map of all families of type, typeName
                @SuppressWarnings("unchecked")
                Enumeration<CatalogTreeNode> ee = node.children();
                while (ee.hasMoreElements()) {
                    CatalogTreeNode famNode = ee.nextElement();
                    String familyName = (String)famNode.getUserObject();
                    Hashtable <String, NamedIcon> iconMap = new Hashtable <String, NamedIcon> ();     // Map of all icons of in family, familyName
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
                _iconMaps.put(typeName, familyMap);
                if (log.isDebugEnabled()) log.debug("Add item type "+typeName+" to _iconMaps.");
            }
        } else {
            loadStandardIcons();
        }
        if (log.isDebugEnabled()) log.debug("Icon Map has "+_iconMaps.size()+" members");
    }

    static void loadStandardIcons() {
        File file = new File("xml"+File.separator
                                +"defaultPanelIcons.xml");
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
                Hashtable <String, Hashtable<String, NamedIcon>> familyMap = new Hashtable <String, Hashtable<String, NamedIcon>> ();     // Map of all families of type, typeName
                @SuppressWarnings("unchecked")
                List<Element>families = typeList.get(i).getChildren();
                for (int k = 0; k < families.size(); k++) {
                    String familyName = families.get(k).getName();
                    Hashtable <String, NamedIcon> iconMap = new Hashtable <String, NamedIcon> ();     // Map of all icons of in family, familyName
                    int w = 0;
                    int h = 0;
                    @SuppressWarnings("unchecked")
                    List<Element>iconfiles = families.get(k).getChildren();
                    for (int j = 0; j < iconfiles.size(); j++) {
                        String iconName = iconfiles.get(j).getName();
                        String fileName = iconfiles.get(j).getText().trim();
                        if (fileName==null || fileName.length()==0) {
                            fileName = "resources/icons/misc/X-red.gif";
                            log.warn("loadStandardIcons: iconName= "+iconName+" in family "+familyName+" has no image file.");
                        }
                        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
                        w = Math.max(w, icon.getIconWidth());
                        h = Math.max(h, icon.getIconHeight());
                        iconMap.put(iconName, icon);
                    }
                    familyMap.put(familyName, iconMap); 
                    if (log.isDebugEnabled()) log.debug("Add "+iconMap.size()+" icons to family "+familyName+" to item type "+typeName);
                }
                _iconMaps.put(typeName, familyMap); 
                if (log.isDebugEnabled()) log.debug("Add "+familyMap.size()+ " families to item type "+typeName+" to _iconMaps.");
            }
        } catch (org.jdom.JDOMException e) {
            log.error("error reading file \""+file.getName()+"\" due to: "+e);
        } catch (java.io.IOException ioe) {
            log.error("error reading file \""+file.getName()+"\" due to: "+ioe);
        }
    }

    public ItemPalette(String title, Editor editor) {
        setTitle(title);
        if (_iconMaps==null) {
            loadIcons();
        }
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
        _itemPanelMap.put("Turnout", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Turnout"));

        itemPanel = new TableItemPanel(this, "Sensor",
                                       PickListModel.sensorPickModelInstance(), editor);
        itemPanel.init();
        _itemPanelMap.put("Sensor", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Sensor"));

        itemPanel = new SignalHeadItemPanel(this, "SignalHead",
                                       PickListModel.signalHeadPickModelInstance(), editor);
        itemPanel.init();
        _itemPanelMap.put("SignalHead", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("SignalHead"));

        itemPanel = new SignalMastItemPanel(this, "SignalMast",
                                            PickListModel.signalMastPickModelInstance(), editor);
        itemPanel.init();
        _itemPanelMap.put("SignalMast", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("SignalMast"));

        itemPanel = new MemoryItemPanel(this, "Memory",
                                        PickListModel.memoryPickModelInstance(), editor);
        itemPanel.init();
        _itemPanelMap.put("Memory", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Memory"));

        itemPanel = new ReporterItemPanel(this, "Reporter",
                                          PickListModel.reporterPickModelInstance(), editor);
        itemPanel.init();
        _itemPanelMap.put("Reporter", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Reporter"));

        itemPanel = new TableItemPanel(this, "Light",
                                       PickListModel.lightPickModelInstance(), editor);
        itemPanel.init();
        _itemPanelMap.put("Light", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("Light"));

        itemPanel = new MultiSensorItemPanel(this, "MultiSensor",
                                             PickListModel.multiSensorPickModelInstance(), editor);
        itemPanel.init();
        _itemPanelMap.put("MultiSensor", itemPanel);
        _tabPane.add(itemPanel, rbp.getString("MultiSensor"));
 
        ItemPanel iconPanel = new IconItemPanel(this, "Icon", editor);
        iconPanel.init();
        _itemPanelMap.put("Icon", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("Icon"));
 
        iconPanel = new BackgroundItemPanel(this, "Background", editor);
        iconPanel.init();
        _itemPanelMap.put("Background", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("Background")); 

        iconPanel = new TextItemPanel(this, "Text", editor);
        iconPanel.init();
        _itemPanelMap.put("Text", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("Text"));     

        iconPanel = new RPSItemPanel(this, "RPSReporter", editor);
        iconPanel.init();
        _itemPanelMap.put("RPSReporter", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("RPSReporter")); 

        iconPanel = new ClockItemPanel(this, "FastClock", editor);
        iconPanel.init();
        _itemPanelMap.put("FastClock", iconPanel);
        _tabPane.add(iconPanel, rbp.getString("FastClock")); 

        setLayout(new BorderLayout(5,5));
        add(_tabPane, BorderLayout.CENTER);
        setLocation(0,100);               
        setVisible(true);                   
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

    public void reset() {
    }

    protected void addFamily(String type, String family, Hashtable<String, NamedIcon> iconMap) {
        getFamilyMaps(type).put(family, iconMap);
        _itemPanelMap.get(type).setFamily(family);
    }

    protected Hashtable<String, Hashtable<String, NamedIcon>> getFamilyMaps(String type) {
       return _iconMaps.get(type);
    }

    protected void removeIconMap(String type, String family) {
        if (log.isDebugEnabled()) log.debug("removeIconMap for family \""+family+" \" in type \""+type+"\"");
        _iconMaps.get(type).remove(family);
        Hashtable <String, Hashtable<String, NamedIcon>> families = getFamilyMaps(type);
        if (families!=null && families.size()>0) {
            Iterator <String> it = families.keySet().iterator();
            while (it.hasNext()) {
                if (log.isDebugEnabled()) log.debug("removeIconMap remaining Keys: family \""+it.next()+" \" in type \""+type+"\"");
            }
        }
    }

    protected void updateFamiliesPanel(String type) {
        if (log.isDebugEnabled()) log.debug("updateFamiliesPanel for "+type);
        ItemPanel itemPanel = _itemPanelMap.get(type);
        itemPanel.removeIconFamiliesPanel();
        itemPanel.initIconFamiliesPanel();
        itemPanel.validate();
        itemPanel.repaint();
        pack();
    }

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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="WMI_WRONG_MAP_ITERATOR", justification="iterator really short, efficiency not as important as clarity here")
    static protected Hashtable<String, NamedIcon> cloneMap(Hashtable<String, NamedIcon> map) {
        Hashtable<String, NamedIcon> clone = new Hashtable<String, NamedIcon>();
        if (map!=null) {
            Iterator <String> it = map.keySet().iterator();
            while (it.hasNext()) {
               String name = it.next();
               NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(map.get(name));
               clone.put(name, icon);
            }
        }
        return clone;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ItemPalette.class.getName());
}
