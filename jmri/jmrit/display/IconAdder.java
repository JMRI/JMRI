// IconAdder.java
package jmri.jmrit.display;

import jmri.Manager;
import jmri.NamedBean;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.catalog.PreviewDialog;
import jmri.managers.*;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
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

import javax.swing.filechooser.FileFilter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JDialog;

/**
 * Provides a simple editor for selecting N NamedIcons.  Class for
 * Icon Editors implements "Drag n Drop".  Allows
 * drops from icons dragged from a Catalog preview pane. 
 * <P>See {@link SensorIcon} for an item
 * that might want to have that type of information, and
 * {@link PanelEditor} for an example of how to use this.
 *
 * @author Pete Cressman  Copyright (c) 2009
 */

public class IconAdder extends JPanel implements ListSelectionListener {

    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");


    HashMap <String, JButton>   _iconMap;
    ArrayList <String>          _order;
    JScrollPane                 _pickTablePane;
    PickListModel               _pickListModel;
    String          _type;
    boolean         _userDefaults;
    JTextField      _sysNametext;
    Manager         _manager;
    JTable          _table;
    JButton         _addButton;
    JButton         _changeButton;
    JButton         _closeButton;
    CatalogPanel    _catalog;
    JFrame          _parent;

    public IconAdder() {
        _userDefaults = false;
        _iconMap = new HashMap <String, JButton>();
        _order = new ArrayList <String>();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public IconAdder(String type) {
        this();
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        CatalogTree defaultIcons = manager.getBySystemName("NXDI");
        if (defaultIcons != null) {
           CatalogTreeNode node = (CatalogTreeNode)defaultIcons.getRoot();
           makeIcons(type, node);
           _userDefaults = true;
        }
        if (log.isDebugEnabled()) log.debug("IconAdder."+type+", defaultIcons= "+defaultIcons);
        _type = type;
    }

    @SuppressWarnings("unchecked")
    public CatalogTreeNode getDefaultIconNode() {
        CatalogTreeNode node =new CatalogTreeNode(_type);
        for (int i=0; i<_order.size(); i++) {
            String key = _order.get(i);
            NamedIcon icon = (NamedIcon)_iconMap.get(key).getIcon();
            node.addLeaf(new CatalogTreeLeaf(key, icon.getURL(), i));
        }
        return node;
    }

    /**
    *  Build iconMap and orderArray from user's choice of defaults
    */
    @SuppressWarnings("unchecked")
    private void makeIcons(String type, CatalogTreeNode n) {
        if (log.isDebugEnabled()) log.debug("makeIcons node= "+n.toString());
        Enumeration<CatalogTreeNode> e = n.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode nChild = e.nextElement();
            if (type.equals(nChild.toString())) {
                ArrayList <CatalogTreeLeaf> list = nChild.getLeaves();
                for (int i=0; i<list.size(); i++) {
                    CatalogTreeLeaf leaf = list.get(i);
                    makeIcon(i, leaf.getName(), leaf.getPath());
                }
               return;
            }
        }
        e = n.children();
        while (e.hasMoreElements()) {
            makeIcons(type, e.nextElement());
        }
    }

    private void makeIcon(int order, String label, String name) {
        if (log.isDebugEnabled()) log.debug("makeIcon: order= "+ order+", label= "+label+", name= "+name);
        NamedIcon icon = new NamedIcon(name, name);
        // make a button to change that icon
        JButton button = new IconButton(label, icon);
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
            makeIcon(order, label, name);
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
                this.add(panel);
                this.add(Box.createVerticalStrut(STRUT_SIZE));
                panel = null;
            }
            cnt++;
        }
        if (panel != null) {
            this.add(panel);
            this.add(Box.createVerticalStrut(STRUT_SIZE));
        }
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
        _table.setPreferredScrollableViewportSize(new java.awt.Dimension(200,100));
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
            checkIconSizes();
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
        Iterator <JButton> iter = _iconMap.values().iterator();
        int lastWidth = 0;
        int lastHeight = 0;
        while (iter.hasNext()) {
            JButton but = iter.next();
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
    }

    /**
    * Used by Panel Editor to make the final installation of the icon(s)
    * into the user's Panel.
    */
    protected NamedBean getTableSelection() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            _table.clearSelection();
            _addButton.setEnabled(false);
            _addButton.setToolTipText(null);
            valueChanged(null);
            this.invalidate();
            return _pickListModel.getIndexOf(row);
        } else {
            String name = _sysNametext.getText();
            if (name != null && name .length() > 2) {
                _sysNametext.setText("");
                return _pickListModel.addBean(name);
            }
        }
        return null;
    }

    /**
     * Returns a new NamedIcon object for your own use.
     * @param key Name of key (label)
     * @return Unique object
     */
    public NamedIcon getIcon(String key) {
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
                         boolean addToTable) {
        _addButton = new JButton(rb.getString("ButtonAddIcon"));
        _addButton.addActionListener(addIconAction);
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
        valueChanged(null);     // set enabled, tool tips etc.
        this.add(p);

        if (addToTable) {
            p = new JPanel();
            p.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
            _sysNametext = new JTextField();
            _sysNametext.setPreferredSize(
                new Dimension(100, _sysNametext.getPreferredSize().height));
            p.add(_sysNametext);
            _sysNametext.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent a){
                        if (_sysNametext.getText().length() > 2) {
                            _addButton.setEnabled(true);
                            _addButton.setToolTipText(null);
                        }
                    }
                });

            p.add(new JLabel(rb.getString("addSysName")));
            this.add(p);
        }

        this.add(Box.createVerticalStrut(STRUT_SIZE));
        this.add(new JSeparator());

        _catalog = new CatalogPanel("catalog", "selectNode");
        _catalog.init(false);
        makeDefaultCatalog();
        _catalog.setVisible(false);
        this.add(_catalog);

        pack();
    }

    /*
    * Add panel to change icons 
    */
    public void addCatalog() {
        if (log.isDebugEnabled()) log.debug("addCatalog called: _catalog= "+_catalog); 
        // add the catalog, so icons can be selected
        if (_catalog != null)  {
            _catalog.setVisible(true);
        } else {
            _catalog = new CatalogPanel("catalog", "selectNode");
            _catalog.init(false);
            makeDefaultCatalog();
        }
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
        _catalog.setVisible(false);
        _changeButton.setVisible(true);
        _closeButton.setVisible(false);
        if (_pickTablePane != null) {
            _pickTablePane.setVisible(true);
        }
        this.pack();
    }

    void makeDefaultCatalog() {
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List <String> sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i=0; i<sysNames.size(); i++) {
                String systemName = sysNames.get(i);
                if (systemName.charAt(0) == 'I') {
                    _catalog.addTree( manager.getBySystemName(systemName));
                }
            }
        }
        _catalog.createNewBranch("IFJAR", "Program Directory", "resources");
        XmlFile.ensurePrefsPresent("resources");
        _catalog.createNewBranch("IFPREF", "Preferences Directory", XmlFile.prefsDir()+"resources");
    }

    public void addTreeToCatalog(CatalogTree tree) {
        if (_catalog != null) {
            _catalog.addTree(tree);
        }
    }

    // For choosing image directories
    static JFileChooser _directoryChooser = null;

    /*
    * Open file anywhere in the file system and let the user decide whether
    * to add it to the Catalog
    */
    public static File getDirectory(String msg, boolean dirsOnly) {    
        if (_directoryChooser == null) {
            _directoryChooser = new JFileChooser(System.getProperty("user.dir")+java.io.File.separator+"resources");
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            for (int i=0; i<CatalogTreeManager.IMAGE_FILTER.length; i++) {
                filt.addExtension(CatalogTreeManager.IMAGE_FILTER[i]);
            }
            _directoryChooser.setFileFilter(filt);
        }
        _directoryChooser.setDialogTitle(rb.getString(msg));
        _directoryChooser.rescanCurrentDirectory();
        if (dirsOnly) {
            _directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            _directoryChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JLabel label = new JLabel(rb.getString("loadDir1"));
            panel.add(label);
            label = new JLabel(rb.getString("loadDir2"));
            panel.add(label);
            label = new JLabel(rb.getString("loadDir3"));
            panel.add(label);
            label = new JLabel(rb.getString("loadDir4"));
            panel.add(label);
            _directoryChooser.setAccessory(panel);
        }
        int retVal = _directoryChooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) return null;  // give up if no file selected
 
        if (dirsOnly) {
            return _directoryChooser.getSelectedFile();
        } else {
            return _directoryChooser.getSelectedFile().getParentFile();
        }
    }

    private JTextField showWaitFrame(Frame parent, String msg) {
        _waitDialog = new JDialog(parent, rb.getString("waitTitle"), false);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(rb.getString("waitWarning")));
        JTextField waitText = new JTextField(msg);
        waitText.setEditable(false);
        waitText.setFont(new Font("Dialog", Font.BOLD, 12));
        waitText.setBackground(panel.getBackground());
        panel.add(waitText);
        _waitDialog.getContentPane().add(panel);
        _waitDialog.setLocation(30,50);
        _waitDialog.pack();
        java.awt.Rectangle r = _waitDialog.getContentPane().getBounds();
        if (log.isDebugEnabled()) log.debug("Bounds: r.x= "+r.x+", r.y= "+r.y+", r.width= "+
                                            r.width+", r.height= "+r.height);
        _waitDialog.repaint(0, r.x, r.y, r.width, r.height);
//        _waitDialog.invalidate();
//        _waitDialog.validate();
        _waitDialog.setVisible(false);
        return waitText;
    }

    private void closeWaitFrame() {
        if (_waitDialog != null) {
            _waitDialog.dispose();
            _waitDialog = null;
        }
    }

    /**
    *  Open one directory.
    * @param addDir - <pre>if true, allows directory to be added as a tree to the Catalog.
    *                      if false, allows preview panel to drag icons.
    */
    public void openDirectory(boolean addDir) {
        _waitText = showWaitFrame(_parent, rb.getString("prevMsg"));
        File dir = getDirectory("openDirMenu", false);
        if (dir != null) {
            _waitDialog.setVisible(true);
            _waitDialog.validate();
            if (addDir) {
                doPreviewDialog(dir, new AActionListener(dir), new MActionListener(dir, true),
                                null, new CActionListener(), 0);
            } else {
                doPreviewDialog(dir, null, new MActionListener(dir, true),
                                null, new CActionListener(), 0);
            }
        } else {
            closeWaitFrame();
        }
    }

    public void searchFS() {
        _waitText = showWaitFrame(_parent, rb.getString("prevMsg"));
        File dir = getDirectory("searchFSMenu", true);
        if (dir == null) {
            closeWaitFrame();
            return;
        }
        if (log.isDebugEnabled()) log.debug("searchFS at dir= "+dir.getPath());
        _waitDialog.setVisible(true);
        getImageDirectory(dir, CatalogTreeManager.IMAGE_FILTER);

        JOptionPane.showMessageDialog(this, rb.getString("DirNotFound"), rb.getString("searchFSMenu"),
                                             JOptionPane.INFORMATION_MESSAGE);
        closeWaitFrame();
    }

    class AActionListener implements ActionListener {
        File dir;
        public AActionListener(File d) {
            dir = d;
        }
        public void actionPerformed(ActionEvent a) {
            addDirectoryToCatalog(dir);
        }
    };
    class MActionListener implements ActionListener {
        File dir;
        boolean oneDir;
        public MActionListener(File d, boolean o) {
            dir = d;
            oneDir = o;
        }
        public void actionPerformed(ActionEvent a) {
            displayMore(dir, oneDir);
        }
    };
    class LActionListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            keepLooking();
        }
    };
    class CActionListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            cancelLooking();
        }
    };

    private void doPreviewDialog(File dir, ActionListener addAction, ActionListener moreAction,
                                 ActionListener lookAction, ActionListener cancelAction,
                                 int startNum) {
        _quitLooking = false;
        // if both addAction & lookAction not null dialog will be modeless - i.e dragable
        _previewDialog = new PreviewDialog(_parent, "previewDir", dir, CatalogTreeManager.IMAGE_FILTER,
                                            ((addAction != null)||(lookAction != null)) );
        _previewDialog.init(addAction, moreAction, lookAction, cancelAction, startNum);
        if (lookAction == null) {
            closeWaitFrame();
        }
    }

    private void getImageDirectory(File f, String[] filter) {
        File[] files = f.listFiles();
        if (files == null) {
            return;
        }
        for (int k=0; k<files.length; k++) {
            if (files[k].isDirectory()) {
                getImageDirectory(files[k], filter);
                if (_quitLooking){
                    return;
                }
                String text = _waitText.getText();
                if (text.length() > 400) {
                    text = text.substring(0, 15);
                }
                _waitText.setText(text+".");
                _waitText.setMinimumSize(_waitText.getPreferredSize());
                _waitDialog.pack();
            } else {
                for (int j=0; j<filter.length; j++) {
                    if (files[k].getName().endsWith(filter[j])) {
                        doPreviewDialog(f, new AActionListener(f), new MActionListener(f, false),
                                            new LActionListener(), new CActionListener(), 0);
                        return;
                    }
                }
            }
        }
    }

    PreviewDialog _previewDialog = null;
    JDialog _waitDialog;
    JTextField _waitText;
    boolean _quitLooking = false;
    
    void addDirectoryToCatalog(File dir) {
        if (_catalog == null) {
            _catalog = new CatalogPanel("catalog", "selectNode");
            _catalog.init(false);
            makeDefaultCatalog();
        }
        if (_changeButton != null) {
            _changeButton.setVisible(false);
            _closeButton.setVisible(true);
        }
        String name = dir.getName();
        _catalog.createNewBranch("IF"+name, name, dir.getAbsolutePath());
        this.add(_catalog);
        this.pack();
        cancelLooking();
    }

    void displayMore(File dir, boolean oneDir) {
        if (_previewDialog != null) {
            _quitLooking = false;
            int numFilesShown = _previewDialog.getNumFilesShown();
            _previewDialog.dispose();
            if (oneDir) {
                doPreviewDialog(dir, null, new MActionListener(dir, oneDir),
                                null, new CActionListener(), numFilesShown);
            } else {
                doPreviewDialog(dir, null, new MActionListener(dir,oneDir),
                                new LActionListener(), new CActionListener(), numFilesShown);
            }
        }
    }
    
    void keepLooking() {
        if (_previewDialog != null) {
            _quitLooking = false;
            _previewDialog.dispose();
            _previewDialog = null;
        }
    }

    void cancelLooking() {
        if (_previewDialog != null) {
            _quitLooking = true;
            _previewDialog.dispose();
            _previewDialog = null;
        }
    }

    private class IconButton extends DropButton {
        String key;
        IconButton(String label, Icon icon) {  // init icon passed to avoid ref before ctor complete
            super(icon);
            key = label;
            /*  Save this code in case there is a need to use an alternative
            icon changing method to DnD.
            addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        pickIcon(key);
                    }
                });
            */
        }
    }
/*  Save this code in case there is a need to use an alternative
            icon changing method to DnD.
    void pickIcon(String key) {
        if (_catalog != null && _catalog.isVisible()){
            NamedIcon newIcon = _catalog.getSelectedIcon();
            if (newIcon !=null) {
                JButton button = _iconMap.get(key);
                button.setIcon(newIcon);
                _iconMap.put(key, button);
            }
            this.invalidate();
        }
    }
*/
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

    class DropButton extends JButton implements DropTargetListener {
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
                        JButton button = _iconMap.get(key);
                        button.setIcon(newIcon);
                        _iconMap.put(key, button);
                        e.dropComplete(true);
                        checkIconSizes();
                        ImageIndexEditor._indexChanged = true;
                        if (log.isDebugEnabled()) log.debug("DropJLabel.drop COMPLETED for "+
                                                             newIcon.getURL());
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
    

    public static Frame getParentFrame(Component comp)
    {
        while (true)
        {
            if (comp instanceof Frame )
            {
                return (Frame)comp;
            }
            comp = comp.getParent();
        }
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconAdder.class.getName());
}
