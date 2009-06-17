// IconAdder.java
package jmri.jmrit.display;

import jmri.NamedBean;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.util.NamedBeanComparator;
import jmri.jmrit.XmlFile;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.catalog.PreviewDialog;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.io.IOException;
import java.io.File;

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
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
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

    HashMap <String, JButton>   _iconMap;
    ArrayList <String>          _order;
    ArrayList <NamedBean>       _pickList;
    JTable          _table;
    JButton         _addButton;
    JButton         _changeButton;
    JButton         _closeButton;
    CatalogPanel    _catalog;
    JFrame          _parent;

    public IconAdder() {
        _iconMap = new HashMap <String, JButton>();
        _order = new ArrayList <String>();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
    *  install the icons used to represent all the states of the entity being edited
    *   @param label - the state name to display, Must be unique from all other  
    *          calls to this method.
    *   @param name - the resource name of the icon image to displa
    *   @param order - (reverse) order of display, (0 last, to N first)
    */
    public void setIcon(int order, String label, String name) {
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
            p.add(new JLabel(key));
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
    public void setPickList(TreeSet ts) {
        _pickList = new ArrayList <NamedBean> (ts.size());
        Iterator iter = ts.iterator();
        while(iter.hasNext()) {
            NamedBean elt = (NamedBean)iter.next();
            _pickList.add(elt);
        }
        TableModel pickListModel = new PickListModel();
        _table = new JTable(pickListModel);

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

        JScrollPane js = new JScrollPane(_table);
        this.add(js);
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
        log.debug("Table valueChanged: row= "+row);
        if (row >= 0) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
            checkIconSizes();
        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(rb.getString("ToolTipPickFromTable"));
            log.debug("_addButton.setEnabled(false): row= "+row);
        }
    }

    void checkIconSizes() {
        if (!_addButton.isEnabled()){
            return;
        }
        Iterator iter = _iconMap.values().iterator();
        int lastWidth = 0;
        int lastHeight = 0;
        while (iter.hasNext()) {
            JButton but = (JButton)iter.next();
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
        log.debug("getTableSelection: row= "+row);
        if (row >= 0) {
            _table.clearSelection();
            _addButton.setEnabled(false);
            _addButton.setToolTipText(null);
            valueChanged(null);
            this.invalidate();
            return _pickList.get(row);
        }
        return null;
    }

    /**
     * Returns a new NamedIcon object for your own use.
     * @param name of key (label)
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
    public void complete(ActionListener addIconAction, ActionListener changeIconAction) {
        _addButton = new JButton(rb.getString("ButtonAddIcon"));
        _addButton.addActionListener(addIconAction);
        JPanel p = new JPanel();
        //p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
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
        valueChanged(null);     // set enabled, or not
        this.add(p);
        this.add(Box.createVerticalStrut(STRUT_SIZE));
        pack();
    }

    /*
    * Add panel to change icons 
    */
    public void addCatalog() {
        // add the catalog, so icons can be selected
        if (_catalog != null)  {
            _catalog.setVisible(true);
            return;
        } 
        this.add(new JSeparator());
        _catalog = new CatalogPanel("catalog", "selectNode");
        _catalog.init(false);
        makeDefaultCatalog();

        if (_changeButton != null) {
            _changeButton.setVisible(false);
            _closeButton.setVisible(true);
        }
        this.add(_catalog);
        this.pack();
    }

    void closeCatalog() {
        this.remove(_catalog);
        _catalog = null;
        _changeButton.setVisible(true);
        _closeButton.setVisible(false);
        this.pack();
    }

    void makeDefaultCatalog() {
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i=0; i<sysNames.size(); i++) {
                String systemName = (String)sysNames.get(i);
                if (systemName.charAt(0) == 'I') {
                    _catalog.addTree( manager.getBySystemName(systemName));
                }
            }
        }
        _catalog.createNewBranch("IFJAR", "resourceJar", "resources");
        XmlFile.ensurePrefsPresent("resources");
        _catalog.createNewBranch("IFPREF", "preferenceDir", XmlFile.prefsDir()+"resources");
    }

    // For choosing image directories
    static JFileChooser _directoryChooser = null;

    /*
    * Open file anywhere in the file system and let the user decide whether
    * to add it to the Catalog
    */
    public static File getDirectory() {    
        if (_directoryChooser == null) {
            _directoryChooser = new JFileChooser(System.getProperty("user.dir")+java.io.File.separator+"resources");
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            for (int i=0; i<CatalogTreeManager.IMAGE_FILTER.length; i++) {
                filt.addExtension(CatalogTreeManager.IMAGE_FILTER[i]);
            }
            _directoryChooser.setFileFilter(filt);
            _directoryChooser.setDialogTitle(rb.getString("openDirMenu"));
            //_directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        _directoryChooser.rescanCurrentDirectory();
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
        int retVal = _directoryChooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) return null;  // give up if no file selected
 
        return _directoryChooser.getSelectedFile().getParentFile();
        //File dir = _directoryChooser.getSelectedFile();
    }

    public void openDirectory() {
        File dir = getDirectory();
        if (dir != null) {
            doPreviewDialog(dir, new AActionListener(dir), null, new CActionListener());
        }
    }

    public void searchFS(boolean addToCatalogOption) {
        File[] roots = File.listRoots();
        String[] rootName = new String[roots.length];
        for (int i=0; i<roots.length; i++) {
            rootName[i] = roots[i].getAbsolutePath();
        }
        String root = (String)JOptionPane.showInputDialog(this, 
                                   rb.getString("selectDrive"), rb.getString("searchFSMenu"),  
                                   JOptionPane.QUESTION_MESSAGE, null, rootName, 
                                   rootName[roots.length/2]);
        if (root == null) {
            return;
        }
        _waitDialog = new JDialog(_parent, rb.getString("waitTitle"), false);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(rb.getString("waitWarning")));
        _waitText = new JTextField(java.text.MessageFormat.format(rb.getString("waitMsg"), 
                                                                  new Object[] {root}));
        _waitText.setEditable(false);
        _waitText.setBackground(panel.getBackground());
        panel.add(_waitText);
        _waitDialog.getContentPane().add(panel);
        _waitDialog.pack();
        _waitDialog.setLocation(100,50);
        _waitDialog.setVisible(true);

        log.debug("searchFS at root= "+root);
        _quitLooking = false;
        getImageDirectory(new File(root), CatalogTreeManager.IMAGE_FILTER, addToCatalogOption);

        JOptionPane.showMessageDialog(this, rb.getString("DirNotFound"), rb.getString("searchFSMenu"),
                                             JOptionPane.INFORMATION_MESSAGE);
        _waitDialog.dispose();
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

    private void doPreviewDialog(File dir, ActionListener addAction, ActionListener lookAction, 
                        ActionListener cancelAction) {
        _quitLooking = false;
        _previewDialog = new PreviewDialog(_parent, "previewDir", dir, 
                                          CatalogTreeManager.IMAGE_FILTER );
        _previewDialog.init(addAction, lookAction, cancelAction);
        _previewDialog.setVisible(true);
    }

    private void getImageDirectory(File f, String[] filter, boolean addToCatalogOption) {
        File[] files = f.listFiles();
        if (files == null) {
            return;
        }
        for (int k=0; k<files.length; k++) {
            if (files[k].isDirectory()) {
                getImageDirectory(files[k], filter, addToCatalogOption);
                if (_quitLooking){
                    return;
                }
                String text = _waitText.getText();
                if (text.length() > 400) {
                    text = text.substring(0, 20);
                }
                _waitText.setText(text+".");
                _waitText.setMinimumSize(_waitText.getPreferredSize());
                _waitDialog.pack();
            } else {
                for (int j=0; j<filter.length; j++) {
                    if (files[k].getName().endsWith(filter[j])) {
                        if (addToCatalogOption) {
                            doPreviewDialog(f, new AActionListener(f),
                                            new LActionListener(), new CActionListener());
                        } else {
                            doPreviewDialog(f, null,
                                            new LActionListener(), new CActionListener());
                        }
                        return;
                    }
                }
            }
        }
    }

    PreviewDialog _previewDialog;
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
    
    void keepLooking() {
        _quitLooking = false;
        _previewDialog.dispose();
        _previewDialog = null;
    }

    void cancelLooking() {
        _quitLooking = true;
        _previewDialog.dispose();
        _previewDialog = null;
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

    /**
     * Table model for pick list
     */
    class PickListModel  extends AbstractTableModel implements PropertyChangeListener {

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;

        public Class getColumnClass(int c) {
                return String.class;
        }

        public int getColumnCount () {
            return 2;
        }

        public String getColumnName(int c) {
            if (c == SNAME_COLUMN) {
                return rb.getString("SystemName");
            } else if (c == UNAME_COLUMN) {
                return rb.getString("UserName");
            }
            return "";
        }

        public boolean isCellEditable(int r,int c) {
            return ( false );
        }

        public int getRowCount () {
            return _pickList.size();
        }
        public Object getValueAt (int r,int c) {
            if (c == SNAME_COLUMN) {
                return _pickList.get(r).getSystemName();
            } else if (c == UNAME_COLUMN) {
                return _pickList.get(r).getUserName();
            }
            return null;
        }
        public void setValueAt(Object type,int r,int c) {
        }


        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        public void dispose() {
            // do this later
            //InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
        }
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
                        if (log.isDebugEnabled()) log.debug("DropJLabel.drop COMPLETED for "+
                                                             newIcon.getURL());
                    }
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();
            } catch(UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            }
            if (log.isDebugEnabled()) log.debug("DropJLabel.drop REJECTED!");
            e.rejectDrop();
        }
    }
    
/*
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
*/
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconAdder.class.getName());
}
