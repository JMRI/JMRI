// IconAdder.java
package jmri.jmrit.display;

import jmri.Manager;
import jmri.NamedBean;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
//import jmri.jmrit.XmlFile;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.catalog.PreviewDialog;
import jmri.jmrit.picker.PickListModel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.io.IOException;
import java.io.File;

import java.awt.Component;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

/**
 * Provides a simple editor for selecting N NamedIcons.  Class for
 * Icon Editors implements "Drag n Drop".  Allows
 * drops from icons dragged from a Catalog preview pane. 
 * <P>See {@link SensorIcon} for an item
 * that might want to have that type of information, and
 * {@link jmri.jmrit.display.panelEditor.PanelEditor} 
 * for an example of how to use this.
 *
 * @author Pete Cressman  Copyright (c) 2009, 2010
 */

public class IconAdder extends JPanel implements ListSelectionListener {

    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    private static int ROW_HEIGHT;

    HashMap <String, JToggleButton>   _iconMap;
    ArrayList <String>          _order;
    JScrollPane                 _pickTablePane;
    PickListModel               _pickListModel;
    CatalogTreeNode _defaultIcons;
    JPanel          _iconPanel;
    String          _type;
    boolean         _userDefaults;
    JTextField      _sysNametext;
    Manager         _manager;
    JTable          _table;
    JButton         _addButton;
    JButton         _addTableButton;
    JButton         _changeButton;
    JButton         _closeButton;
    CatalogPanel    _catalog;
    JFrame          _parent;

    public IconAdder() {
        _userDefaults = false;
        _iconMap = new HashMap <String, JToggleButton>(10);   // 10 is current max of signalHead icons
        _order = new ArrayList <String>();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void reset() {
        if (_table != null) {
            _table.getSelectedRow();
            _table.clearSelection();
        }
        closeCatalog();
        if (_defaultIcons != null) {
            _iconMap = new HashMap <String, JToggleButton>(10);
            _order = new ArrayList <String>();
            makeIcons(_defaultIcons);
            makeIconPanel();
        }
        this.validate();
    }

    public IconAdder(String type) {
        this();
        _type = type;
        initDefaultIcons();
    }

    @SuppressWarnings("unchecked")
    public void initDefaultIcons() {
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        CatalogTree tree = manager.getBySystemName("NXDI");
        if (tree != null) {
            CatalogTreeNode node = (CatalogTreeNode)tree.getRoot();
            Enumeration<CatalogTreeNode> e = node.children();
            while (e.hasMoreElements()) {
                CatalogTreeNode nChild = e.nextElement();
                if (_type.equals(nChild.toString())) {
                    _defaultIcons = nChild;
                    _userDefaults = true;
                    makeIcons(_defaultIcons);
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("IconAdder."+_type+", defaultIcons= "+_defaultIcons);
    }

    public CatalogTreeNode getDefaultIconNode() {
        _defaultIcons =new CatalogTreeNode(_type);
        for (int i=0; i<_order.size(); i++) {
            String key = _order.get(i);
            NamedIcon icon = (NamedIcon)_iconMap.get(key).getIcon();
            _defaultIcons.addLeaf(new CatalogTreeLeaf(key, icon.getURL(), i));
        }
        return _defaultIcons;
    }

    /**
    *  Build iconMap and orderArray from user's choice of defaults
    */
    private void makeIcons(CatalogTreeNode n) {
        if (log.isDebugEnabled()) log.debug("makeIcons from node= "+n.toString()+", numChildren= "+
                                            n.getChildCount()+", NumLeaves= "+n.getNumLeaves());
        ArrayList <CatalogTreeLeaf> list = n.getLeaves();
        for (int i=0; i<list.size(); i++) {
            CatalogTreeLeaf leaf = list.get(i);
            String name = leaf.getPath();
            setIcon(i, leaf.getName(), new NamedIcon(name, name));
        }
    }

    protected void setIcon(int order, String label, NamedIcon icon) {
        // make a button to change that icon
        if (log.isDebugEnabled()) log.debug("setNamedIcon: order= "+order+", key= "+label);
        if (log.isDebugEnabled()) log.debug("setIcon: icon width= "+icon.getIconWidth()+" height= "+icon.getIconHeight());
        icon.reduceTo(CatalogPanel.ICON_WIDTH, CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
        JToggleButton button = new IconButton(label, icon);
        button.setToolTipText(icon.getName());
        _iconMap.put(label, button);
        // calls may not be in ascending order, so pad array
        if (order > _order.size()) {
            for (int i=_order.size(); i<order+1; i++) {
                _order.add(i, label);
            }
        } else {
            if (order < _order.size()) {
                _order.remove(order);
            }
            _order.add(order, label);
        }
    }

    /**
    *  install the icons used to represent all the states of the entity being edited
    *   @param label - the state name to display, Must be unique from all other  
    *          calls to this method.
    *   @param name - the resource name of the icon image to displa
    *   @param order - (reverse) order of display, (0 last, to N first)
    */
    public void setIcon(int order, String label, String name) {
        if (!_userDefaults) {
            if (log.isDebugEnabled()) log.debug("setIcon: order= "+ order+", label= "+label+", name= "+name);
            setIcon(order, label, new NamedIcon(name, name));
        }
    }

    public void setParent(JFrame parent) {
        _parent = parent;
    }

    void pack() {
        _parent.pack();
    }

    public int getNumIcons() {
        return _iconMap.size();
    }    

   static int STRUT_SIZE = 5;
    /**
    * After all the calls to setIcon(...) are made, make the icon
    * display.   Two columns to save space for subsequent panels.
    */
    public void makeIconPanel() {
        if (_iconPanel != null) {
            this.remove(_iconPanel);
        }
        _iconPanel = new JPanel();
        _iconPanel.setLayout(new BoxLayout(_iconPanel, BoxLayout.Y_AXIS));
        JPanel panel = null;
        int cnt=0;
        for (int i=_order.size()-1; i>=0; i--) {
            if (panel == null) {
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            }
            String key = _order.get(i);
            JPanel p =new JPanel(); 
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(new JLabel(rbean.getString(key)));
            p.add(_iconMap.get(key));
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            if ((cnt&1)!=0) {
                _iconPanel.add(panel);
                _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
                panel = null;
            }
            cnt++;
        }
        if (panel != null) {
            _iconPanel.add(panel);
            _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        }
        this.add(_iconPanel,0);
        pack();
    }

    /**
    * After the calls to makeIconPanel(), optionally.
    * make a pick list table for managed elements.  (Not all
    * Icon Editors use pick lists)
    */
    public void setPickList(PickListModel tableModel) {
        tableModel.init();
        _pickListModel = tableModel;
        _table = new JTable(tableModel);

        _table.setRowSelectionAllowed(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _table.getSelectionModel().addListSelectionListener(this);
        ROW_HEIGHT = _table.getRowHeight();
        _table.setPreferredScrollableViewportSize(new java.awt.Dimension(200,7*ROW_HEIGHT));
        _table.setDragEnabled(true);
        TableColumnModel columnModel = _table.getColumnModel();

        TableColumn sNameColumnT = columnModel.getColumn(PickListModel.SNAME_COLUMN);
        sNameColumnT.setResizable(true);
        sNameColumnT.setMinWidth(50);
        sNameColumnT.setMaxWidth(200);

        TableColumn uNameColumnT = columnModel.getColumn(PickListModel.UNAME_COLUMN);
        uNameColumnT.setResizable(true);
        uNameColumnT.setMinWidth(100);
        uNameColumnT.setMaxWidth(300);

        _pickTablePane = new JScrollPane(_table);
        this.add(_pickTablePane);
        this.add(Box.createVerticalStrut(STRUT_SIZE));
        pack();
    }

    public void setSelection(NamedBean bean) {
        int row = _pickListModel.getIndexOf(bean);
        _table.addRowSelectionInterval(row, row);
        _pickTablePane.getVerticalScrollBar().setValue(row*ROW_HEIGHT);
    }

    /**
    *  When a Pick list is installed, table selection controls the Add button
    */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (row >= 0) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(rb.getString("ToolTipPickFromTable"));
            if (log.isDebugEnabled()) log.debug("_addButton.setEnabled(false): row= "+row);
        }
    }

    void checkIconSizes() {
        if (!_addButton.isEnabled()){
            return;
        }
        Iterator <JToggleButton> iter = _iconMap.values().iterator();
        int lastWidth = 0;
        int lastHeight = 0;
        while (iter.hasNext()) {
            JToggleButton but = iter.next();
            int nextWidth = but.getIcon().getIconWidth();
            int nextHeight = but.getIcon().getIconHeight();
            if ((lastWidth>0 && lastWidth != nextWidth) || (lastHeight>0 && lastHeight != nextHeight)) {
                JOptionPane.showMessageDialog(this, rb.getString("IconSizeDiff"), rb.getString("warnTitle"),
                                                     JOptionPane.WARNING_MESSAGE);
                return;
            }
            lastWidth = nextWidth;
            lastHeight = nextHeight;
        }
        if (log.isDebugEnabled()) log.debug("Size: width= "+lastWidth+", height= "+lastHeight); 
    }

    /**
    * Used by Panel Editor to make the final installation of the icon(s)
    * into the user's Panel.
    * <P>Note! the selection is cleared. When two successive calls are made, the
    * 2nd will always return null, regardless of the 1st return.
    */
    public NamedBean getTableSelection() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            NamedBean b = _pickListModel.getBeanAt(row);
            _table.clearSelection();
            _addButton.setEnabled(false);
            _addButton.setToolTipText(null);
            this.validate();
            if (log.isDebugEnabled()) log.debug("getTableSelection: row= "+row+", bean= "+b.getDisplayName());
            return b;
        } else if (log.isDebugEnabled()) log.debug("getTableSelection: row=0");
        return null;
    }

    /**
     * Returns a new NamedIcon object for your own use.
     * @param key Name of key (label)
     * @return Unique object
     */
    public NamedIcon getIcon(String key) {
        if (log.isDebugEnabled()) log.debug("getIcon for key= "+key); 
        return new NamedIcon((NamedIcon)_iconMap.get(key).getIcon());
    }

    /*
    * Supports selection of NamedBean from a pick list table
    * @param addIconAction - ActionListener that adds an icon to the panel -
    * representing either an entity a pick list selection, an
    * arbitrary inmage, or a value, such a
    * memory value.
    * @param changeIconAction - ActionListener that displays sources from
    * which to select an image file.  
    */
    public void complete(ActionListener addIconAction, ActionListener changeIconAction,
                         boolean addToTable, boolean update) {
        if (update) {
            _addButton = new JButton(rb.getString("ButtonUpdateIcon"));
        } else {
            _addButton = new JButton(rb.getString("ButtonAddIcon"));
        }
        _addButton.addActionListener(addIconAction);
        _addButton.setEnabled(true);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());       
        p.add(_addButton);
        if (changeIconAction != null) {
            _changeButton = new JButton(rb.getString("ButtonChangeIcon"));
            _changeButton.addActionListener(changeIconAction);
            p.add(_changeButton);
            _closeButton = new JButton(rb.getString("ButtonCloseCatalog"));
            _closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    closeCatalog();
                }
            });
            _closeButton.setVisible(false);
            p.add(_closeButton);
        }
        if (_table != null) {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(rb.getString("ToolTipPickFromTable"));
        }

        addAdditionalButtons(p);
        this.add(p);

        if (addToTable) {
            p = new JPanel();
            p.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
            _sysNametext = new JTextField();
            _sysNametext.setPreferredSize(
                new Dimension(150, _sysNametext.getPreferredSize().height+2));
            _addTableButton = new JButton(rb.getString("addToTable"));
            _addTableButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addToTable();
                    }
            });
            _addTableButton.setEnabled(false);
            _addTableButton.setToolTipText(rb.getString("ToolTipWillActivate"));
            p.add(_sysNametext);
            _sysNametext.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent a){
                        if (_sysNametext.getText().length() > 0) {
                            _addTableButton.setEnabled(true);
                            _addTableButton.setToolTipText(null);
                            _table.clearSelection();
                        }
                    }
                });

            p.add(_addTableButton);
            this.add(p);
        }

        this.add(Box.createVerticalStrut(STRUT_SIZE));
        this.add(new JSeparator());

        if (changeIconAction != null) {
            _catalog = CatalogPanel.makeDefaultCatalog();
            _catalog.setVisible(false);
            this.add(_catalog);
        }
        if (_type != null && _defaultIcons == null) {
            getDefaultIconNode();
        }
        valueChanged(null);
        pack();
    }

    protected void addAdditionalButtons(JPanel p) {}

    public boolean addIconIsEnabled() {
        return _addButton.isEnabled();
    }

    
    void addToTable() {
        String name = _sysNametext.getText();
        if (name != null && name.length() > 0) {
            NamedBean bean = _pickListModel.addBean(name);
            int setRow = _pickListModel.getIndexOf(bean);
            _table.setRowSelectionInterval(setRow, setRow);
            _pickTablePane.getVerticalScrollBar().setValue(setRow*ROW_HEIGHT);
        }
        _sysNametext.setText("");
        _addTableButton.setEnabled(false);
        _addTableButton.setToolTipText(rb.getString("ToolTipWillActivate"));
    }

    /*
    * Add panel to change icons 
    */
    public void addCatalog() {
        if (log.isDebugEnabled()) log.debug("addCatalog called:"); 
        // add the catalog, so icons can be selected
        if (_catalog == null)  {
            _catalog = CatalogPanel.makeDefaultCatalog();
        }
        _catalog.setVisible(true);
        /*
        this.add(new JSeparator());
        */
        if (_changeButton != null) {
            _changeButton.setVisible(false);
            _closeButton.setVisible(true);
        }
        //this.add(_catalog);
        if (_pickTablePane != null) {
            _pickTablePane.setVisible(false);
        }
        this.pack();
    }

    void closeCatalog() {
        if (_changeButton != null) {
            _catalog.setVisible(false);
            _changeButton.setVisible(true);
            _closeButton.setVisible(false);
        }
        if (_pickTablePane != null) {
            _pickTablePane.setVisible(true);
        }
        this.pack();
    }

    public void addDirectoryToCatalog(java.io.File dir) {
        if (_catalog == null) {
            _catalog = CatalogPanel.makeDefaultCatalog();
        }
        if (_changeButton != null) {
            _changeButton.setVisible(false);
            _closeButton.setVisible(true);
        }
        String name = dir.getName();
        _catalog.createNewBranch("IF"+name, name, dir.getAbsolutePath());
        this.add(_catalog);
        this.pack();
    }

    public void addTreeToCatalog(CatalogTree tree) {
        if (_catalog != null) {
            _catalog.addTree(tree);
        }
    }
    private class IconButton extends DropButton {
        String key;
        IconButton(String label, Icon icon) {  // init icon passed to avoid ref before ctor complete
            super(icon);
            key = label;
        }
    }
    
    /**
     * Clean up when its time to make it all go away
     */
    public void dispose() {
        // clean up GUI aspects
        this.removeAll();
        _iconMap = null;
        _order = null;
        _catalog = null;
    }

    class DropButton extends JToggleButton implements DropTargetListener {
        DataFlavor dataFlavor;
        DropButton (Icon icon) {
            super(icon);
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //if (log.isDebugEnabled()) log.debug("DropJLabel ctor");
        }
        public void dragExit(DropTargetEvent dte) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragExit ");
        }
        public void dragEnter(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragEnter ");
        }
        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragOver ");
        }
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dropActionChanged ");
        }
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if(e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon newIcon = (NamedIcon)tr.getTransferData(dataFlavor);
                    if (newIcon !=null) {
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        DropTarget target = (DropTarget)e.getSource();
                        IconButton iconButton = (IconButton)target.getComponent();
                        String key = iconButton.key;
                        JToggleButton button = _iconMap.get(key);
                        button.setIcon(newIcon);
                        _iconMap.put(key, button);
                        e.dropComplete(true);
                        checkIconSizes();
                        if (_type != null) {
                            ImageIndexEditor._indexChanged = true;
                        }
                        if (log.isDebugEnabled()) log.debug("DropJLabel.drop COMPLETED for "+key+
                                                             ", "+newIcon.getURL());
                    } else {
                        if (log.isDebugEnabled()) log.debug("DropJLabel.drop REJECTED!");
                        e.rejectDrop();
                    }
                }
            } catch(IOException ioe) {
                if (log.isDebugEnabled()) log.debug("DropPanel.drop REJECTED!");
                e.rejectDrop();
            } catch(UnsupportedFlavorException ufe) {
                if (log.isDebugEnabled()) log.debug("DropJLabel.drop REJECTED!");
                e.rejectDrop();
            }
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconAdder.class.getName());
}
