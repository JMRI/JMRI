package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple editor for selecting N NamedIcons. Class for Icon Editors
 * implements "Drag n Drop". Allows drops from icons dragged from a Catalog
 * preview pane.
 * <P>
 * See {@link SensorIcon} for an item that might want to have that type of
 * information, and {@link jmri.jmrit.display.panelEditor.PanelEditor} for an
 * example of how to use this.
 *
 * @author Pete Cressman Copyright (c) 2009, 2010
 */
public class IconAdder extends JPanel implements ListSelectionListener {

    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    int ROW_HEIGHT;

    HashMap<String, JToggleButton> _iconMap;
    ArrayList<String> _order;
    JScrollPane _pickTablePane;
    PickListModel _pickListModel;
    CatalogTreeNode _defaultIcons;      // current set of icons user has selected
    JPanel _iconPanel;
    JPanel _buttonPanel;
    String _type;
    boolean _userDefaults;
    JTextField _sysNametext;
    Manager _manager;
    JTable _table;
    JButton _addButton;
    JButton _addTableButton;
    JButton _changeButton;
    JButton _closeButton;
    CatalogPanel _catalog;
    JFrame _parent;
    boolean _allowDeletes;
    boolean _update;				// updating existing icon from popup

    public IconAdder() {
        _userDefaults = false;
        _iconMap = new HashMap<String, JToggleButton>(10);
        _order = new ArrayList<String>();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public IconAdder(boolean allowDeletes) {
        this();
        _allowDeletes = allowDeletes;
    }

    public void reset() {
        if (_table != null) {
            _table.clearSelection();
        }
        closeCatalog();
        if (_defaultIcons != null) {
            makeIconPanel(true);
        }
        this.revalidate();
    }

    public IconAdder(String type) {
        this();
        _type = type;
        initDefaultIcons();
    }

    public void initDefaultIcons() {
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        CatalogTree tree = manager.getBySystemName("NXDI");
        if (tree != null) {
            CatalogTreeNode node = tree.getRoot();
            
            Enumeration<CatalogTreeNode> e = node.children();
            
            while (e.hasMoreElements()) {
                CatalogTreeNode nChild = e.nextElement();
                if (_type.equals(nChild.toString())) {
                    _defaultIcons = nChild;
                    _userDefaults = true;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("initDefaultIcons: type= " + _type + ", defaultIcons= " + _defaultIcons);
        }
    }

    private CatalogTreeNode getDefaultIconNodeFromMap() {
        if (log.isDebugEnabled()) {
            log.debug("getDefaultIconNodeFromMap for node= " + _type
                    + ", _order.size()= " + _order.size());
        }
        _defaultIcons = new CatalogTreeNode(_type);
        Iterator<Entry<String, JToggleButton>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, JToggleButton> e = it.next();
            NamedIcon icon = (NamedIcon) e.getValue().getIcon();
            _defaultIcons.addLeaf(new CatalogTreeLeaf(e.getKey(), icon.getURL(), _order.indexOf(e.getKey())));
        }
        return _defaultIcons;
    }

    public CatalogTreeNode getDefaultIconNode() {
        if (log.isDebugEnabled()) {
            log.debug("getDefaultIconNode for node= " + _type);
        }
        CatalogTreeNode defaultIcons = new CatalogTreeNode(_type);
        ArrayList<CatalogTreeLeaf> list = _defaultIcons.getLeaves();
        for (int i = 0; i < list.size(); i++) {
            CatalogTreeLeaf leaf = list.get(i);
            defaultIcons.addLeaf(new CatalogTreeLeaf(leaf.getName(), leaf.getPath(), i));
        }
        return defaultIcons;
    }

    /**
     * Build iconMap and orderArray from user's choice of defaults
     */
    protected void makeIcons(CatalogTreeNode n) {
        if (log.isDebugEnabled()) {
            log.debug("makeIcons from node= " + n.toString() + ", numChildren= "
                    + n.getChildCount() + ", NumLeaves= " + n.getNumLeaves());
        }
        _iconMap = new HashMap<String, JToggleButton>(10);
        _order = new ArrayList<String>();
        ArrayList<CatalogTreeLeaf> list = n.getLeaves();
        // adjust order of icons
        int k = list.size() - 1;
        for (int i = list.size() - 1; i >= 0; i--) {
            CatalogTreeLeaf leaf = list.get(i);
            String name = leaf.getName();
            String path = leaf.getPath();
            if ("BeanStateInconsistent".equals(name)) {
                this.setIcon(0, name, new NamedIcon(path, path));
            } else if ("BeanStateUnknown".equals(name)) {
                this.setIcon(1, name, new NamedIcon(path, path));
            } else {
                this.setIcon(k, name, new NamedIcon(path, path));
                k--;
            }
        }
    }

    /**
     * @param order -the index to Sensor's name and the inverse order that icons
     *              are drawn in doIconPanel()
     * @param label -the Sensor's name displayed in the icon panel and the key
     *              to the icon button in _iconMap
     * @param icon  -the icon displayed in the icon button
     */
    protected void setIcon(int order, String label, NamedIcon icon) {
        // make a button to change that icon
        if (log.isDebugEnabled()) {
            log.debug("setIcon at order= " + order + ", key= " + label);
        }
        JToggleButton button = new IconButton(label, icon);
        if (icon == null || icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
            button.setText(Bundle.getMessage("invisibleIcon"));
            button.setForeground(Color.lightGray);
        } else {
            icon.reduceTo(CatalogPanel.ICON_WIDTH, CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
            button.setToolTipText(icon.getName());
        }

        if (_allowDeletes) {
            String fileName = "resources/icons/misc/X-red.gif";
            button.setSelectedIcon(new jmri.jmrit.catalog.NamedIcon(fileName, fileName));
        }
        if (icon != null) {
            icon.reduceTo(CatalogPanel.ICON_WIDTH, CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
        }

        _iconMap.put(label, button);
        // calls may not be in ascending order, so pad array
        if (order > _order.size()) {
            for (int i = _order.size(); i < order; i++) {
                _order.add(i, "placeHolder");
            }
        } else {
            if (order < _order.size()) {
                _order.remove(order);
            }
        }
        _order.add(order, label);
    }

    /**
     * install the icons used to represent all the states of the entity being
     * edited
     *
     * @param label - the state name to display, Must be unique from all other
     *              calls to this method.
     * @param name  - the resource name of the icon image to displa
     * @param order - (reverse) order of display, (0 last, to N first)
     */
    public void setIcon(int order, String label, String name) {
        if (log.isDebugEnabled()) {
            log.debug("setIcon: order= " + order + ", label= " + label + ", name= " + name);
        }
        this.setIcon(order, label, new NamedIcon(name, name));
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

    static int STRUT_SIZE = 3;

    /**
     * After all the calls to setIcon(...) are made, make the icon display. Two
     * columns to save space for subsequent panels.
     */
    public void makeIconPanel(boolean useDefaults) {
        if (useDefaults && _userDefaults) {
            makeIcons(_defaultIcons);
        }
        clearIconPanel();
        doIconPanel();
    }

    private void clearIconPanel() {
        if (_iconPanel != null) {
            this.remove(_iconPanel);
        }
        _iconPanel = new JPanel();
        _iconPanel.setLayout(new BoxLayout(_iconPanel, BoxLayout.Y_AXIS));
    }

    protected void doIconPanel() {
        JPanel panel = null;
        int cnt = 0;
        for (int i = _order.size() - 1; i >= 0; i--) {
            if (panel == null) {
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            }
            String key = _order.get(i);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            String labelName = key;
            try {
                labelName = rbean.getString(key);
            } catch (java.util.MissingResourceException mre) {
            }
            p.add(new JLabel(labelName));
            p.add(_iconMap.get(key));
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            if ((cnt & 1) != 0) {
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
        this.add(_iconPanel, 0);
    }

    /**
     * After the calls to makeIconPanel(), optionally. make a pick list table
     * for managed elements. (Not all Icon Editors use pick lists)
     */
    public void setPickList(PickListModel tableModel) {
        tableModel.init();
        _pickListModel = tableModel;
        _table = new JTable(tableModel);
        _pickListModel.makeSorter(_table);

        _table.setRowSelectionAllowed(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ROW_HEIGHT = _table.getRowHeight();
        _table.setPreferredScrollableViewportSize(new java.awt.Dimension(200, 7 * ROW_HEIGHT));
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
        _pickTablePane.getVerticalScrollBar().setValue(row * ROW_HEIGHT);
    }

    /**
     * When a Pick list is installed, table selection controls the Add button
     */
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        if (log.isDebugEnabled()) {
            log.debug("Table valueChanged: row= " + row);
        }
        if (row >= 0) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
            if (_type != null && _type.equals("SignalHead")) {
                makeIconMap(_pickListModel.getBeanAt(row));
                clearIconPanel();
                doIconPanel();
            }

        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        }
        validate();
    }

    void makeIconMap(NamedBean bean) {
        if (bean != null && _type != null && _type.equals("SignalHead")) {
            _order = new ArrayList<String>();
            _iconMap = new HashMap<String, JToggleButton>(12);
            int k = 0;
            ArrayList<CatalogTreeLeaf> list = _defaultIcons.getLeaves();
            String[] states = ((SignalHead) bean).getValidStateNames();
            for (int i = 0; i < list.size(); i++) {
                CatalogTreeLeaf leaf = list.get(i);
                String name = leaf.getName();
                try {
                    name = rbean.getString(leaf.getName());
                } catch (java.util.MissingResourceException mre) {
                }
                if (log.isDebugEnabled()) {
                    log.debug("makeIconMap: leafName= " + leaf.getName() + ", name= " + name);
                }
                for (int j = 0; j < states.length; j++) {
                    if (name.equals(states[j])
                            || leaf.getName().equals(rbean.getString("SignalHeadStateDark"))
                            || leaf.getName().equals(rbean.getString("SignalHeadStateHeld"))) {
                        String path = leaf.getPath();
                        this.setIcon(k++, leaf.getName(), new NamedIcon(path, path));
                        break;
                    }
                }
            }
        } else {
            makeIcons(_defaultIcons);
        }
        if (log.isDebugEnabled()) {
            log.debug("makeIconMap: _iconMap.size()= " + _iconMap.size());
        }
    }

    void checkIconSizes() {
        if (!_addButton.isEnabled()) {
            return;
        }
        Iterator<JToggleButton> iter = _iconMap.values().iterator();
        int lastWidth = 0;
        int lastHeight = 0;
        boolean first = true;
        while (iter.hasNext()) {
            JToggleButton but = iter.next();
            if (first) {
                lastWidth = but.getIcon().getIconWidth();
                lastHeight = but.getIcon().getIconHeight();
                first = false;
                continue;
            }
            int nextWidth = but.getIcon().getIconWidth();
            int nextHeight = but.getIcon().getIconHeight();
            if ((Math.abs(lastWidth - nextWidth) > 3 || Math.abs(lastHeight - nextHeight) > 3)) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("IconSizeDiff"), Bundle.getMessage("warnTitle"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            lastWidth = nextWidth;
            lastHeight = nextHeight;
        }
        if (log.isDebugEnabled()) {
            log.debug("Size: width= " + lastWidth + ", height= " + lastHeight);
        }
    }

    /**
     * Used by Panel Editor to make the final installation of the icon(s) into
     * the user's Panel.
     * <P>
     * Note! the selection is cleared. When two successive calls are made, the
     * 2nd will always return null, regardless of the 1st return.
     */
    public NamedBean getTableSelection() {
        if (ImageIndexEditor.isIndexChanged()) {
            checkIconSizes();
        }
        int row = _table.getSelectedRow();
        if (row >= 0) {
            NamedBean b = _pickListModel.getBeanAt(row);
            _table.clearSelection();
            _addButton.setEnabled(false);
            _addButton.setToolTipText(null);
            this.revalidate();
            if (log.isDebugEnabled()) {
                log.debug("getTableSelection: row= " + row + ", bean= " + b.getDisplayName());
            }
            return b;
        } else if (log.isDebugEnabled()) {
            log.debug("getTableSelection: row=0");
        }
        return null;
    }

    /**
     * Returns a new NamedIcon object for your own use.
     *
     * @param key Name of key (label)
     * @return Unique object
     */
    public NamedIcon getIcon(String key) {
        if (log.isDebugEnabled()) {
            log.debug("getIcon for key= " + key);
        }
        return new NamedIcon((NamedIcon) _iconMap.get(key).getIcon());
    }

    /**
     * Returns a new Hashtable of only the icons selected for display.
     */
    public Hashtable<String, NamedIcon> getIconMap() {
        if (log.isDebugEnabled()) {
            log.debug("getIconMap: _allowDeletes= " + _allowDeletes);
        }
        Hashtable<String, NamedIcon> iconMap = new Hashtable<String, NamedIcon>();
        Iterator<String> iter = _iconMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            JToggleButton button = _iconMap.get(key);
            if (log.isDebugEnabled()) {
                log.debug("getIconMap: key= " + key + ", button.isSelected()= " + button.isSelected());
            }
            if (!_allowDeletes || !button.isSelected()) {
                iconMap.put(key, new NamedIcon((NamedIcon) button.getIcon()));
            }
        }
        return iconMap;
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
    public void complete(ActionListener addIconAction, boolean changeIcon,
            boolean addToTable, boolean update) {
        _update = update;
        if (_buttonPanel != null) {
            this.remove(_buttonPanel);
        }
        _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        if (addToTable) {
            _sysNametext = new JTextField();
            _sysNametext.setPreferredSize(
                    new Dimension(150, _sysNametext.getPreferredSize().height + 2));
            _addTableButton = new JButton(Bundle.getMessage("addToTable"));
            _addTableButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addToTable();
                }
            });
            _addTableButton.setEnabled(false);
            _addTableButton.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
            p.add(_sysNametext);
            _sysNametext.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent a) {
                    if (_sysNametext.getText().length() > 0) {
                        _addTableButton.setEnabled(true);
                        _addTableButton.setToolTipText(null);
                        _table.clearSelection();
                    }
                }
            });

            p.add(_addTableButton);
            _buttonPanel.add(p);
            p = new JPanel();
            p.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
        }
        if (update) {
            _addButton = new JButton(Bundle.getMessage("ButtonUpdateIcon"));
        } else {
            _addButton = new JButton(Bundle.getMessage("ButtonAddIcon"));
        }
        _addButton.addActionListener(addIconAction);
        _addButton.setEnabled(true);
        if (changeIcon) {
            _changeButton = new JButton(Bundle.getMessage("ButtonChangeIcon"));
            _changeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addCatalog();
                }
            });
            p.add(_changeButton);
            _closeButton = new JButton(Bundle.getMessage("ButtonCloseCatalog"));
            _closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    closeCatalog();
                }
            });
            _closeButton.setVisible(false);
            p.add(_closeButton);
        }
        _buttonPanel.add(p);
        if (_table != null) {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        }
        addAdditionalButtons(_buttonPanel);
        p = new JPanel();
        p.add(_addButton);
        _buttonPanel.add(p);

        _buttonPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        _buttonPanel.add(new JSeparator());
        this.add(_buttonPanel);

        if (changeIcon) {
            _catalog = CatalogPanel.makeDefaultCatalog();
            _catalog.setVisible(false);
            _catalog.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
            this.add(_catalog);
        }
        if (_type != null /*&& _defaultIcons == null*/) {
            getDefaultIconNodeFromMap();
        }
        // Allow initial row to be set without getting callback to valueChanged
        if (_table != null) {
            _table.getSelectionModel().addListSelectionListener(this);
        }
        pack();
    }

    protected void addAdditionalButtons(JPanel p) {
    }

    public boolean addIconIsEnabled() {
        return _addButton.isEnabled();
    }

    void addToTable() {
        String name = _sysNametext.getText();
        if (name != null && name.length() > 0) {
            NamedBean bean = _pickListModel.addBean(name);
            int setRow = _pickListModel.getIndexOf(bean);
            _table.setRowSelectionInterval(setRow, setRow);
            _pickTablePane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        }
        _sysNametext.setText("");
        _addTableButton.setEnabled(false);
        _addTableButton.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
    }

    /*
     * Add panel to change icons 
     */
    public void addCatalog() {
        if (log.isDebugEnabled()) {
            log.debug("addCatalog called:");
        }
        // add the catalog, so icons can be selected
        if (_catalog == null) {
            _catalog = CatalogPanel.makeDefaultCatalog();
            _catalog.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
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
        pack();
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
        pack();
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
        _catalog.createNewBranch("IF" + name, name, dir.getAbsolutePath());
        this.add(_catalog);
        this.pack();
    }

    /**
     * If icons are changed, update global tree
     */
    private void updateCatalogTree() {
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        // unfiltered, xml-stored, default icon tree
        CatalogTree tree = manager.getBySystemName("NXDI");
        if (tree == null) {	// build a new Default Icons tree
            tree = manager.newCatalogTree("NXDI", "Default Icons");
        }
        CatalogTreeNode root = tree.getRoot();

        Enumeration<CatalogTreeNode> e = root.children();

        String name = _defaultIcons.toString();
        while (e.hasMoreElements()) {
            CatalogTreeNode nChild = e.nextElement();
            if (name.equals(nChild.toString())) {
                if (log.isDebugEnabled()) {
                    log.debug("Remove node " + nChild);
                }
                root.remove(nChild);
                break;
            }
        }
        root.add(_defaultIcons);
        ImageIndexEditor.indexChanged(true);
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

        DropButton(Icon icon) {
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
                if (e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon newIcon = (NamedIcon) tr.getTransferData(dataFlavor);
                    if (newIcon != null) {
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        DropTarget target = (DropTarget) e.getSource();
                        IconButton iconButton = (IconButton) target.getComponent();
                        String key = iconButton.key;
                        JToggleButton button = _iconMap.get(key);
                        NamedIcon oldIcon = (NamedIcon) button.getIcon();
                        button.setIcon(newIcon);
                        if (newIcon.getIconWidth() < 1 || newIcon.getIconHeight() < 1) {
                            button.setText(Bundle.getMessage("invisibleIcon"));
                            button.setForeground(Color.lightGray);
                        } else {
                            button.setText(null);
                        }
                        _iconMap.put(key, button);
                        if (!_update) {
                            _defaultIcons.deleteLeaf(key, oldIcon.getURL());
                            _defaultIcons.addLeaf(key, newIcon.getURL());
                            updateCatalogTree();
                        }
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) {
                            log.debug("DropJLabel.drop COMPLETED for " + key
                                    + ", " + newIcon.getURL());
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("DropJLabel.drop REJECTED!");
                        }
                        e.rejectDrop();
                    }
                }
            } catch (IOException ioe) {
                if (log.isDebugEnabled()) {
                    log.debug("DropPanel.drop REJECTED!");
                }
                e.rejectDrop();
            } catch (UnsupportedFlavorException ufe) {
                if (log.isDebugEnabled()) {
                    log.debug("DropJLabel.drop REJECTED!");
                }
                e.rejectDrop();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IconAdder.class.getName());
}
