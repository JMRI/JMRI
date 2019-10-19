package jmri.jmrit.display;

import java.awt.*;
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
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;

import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
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
 * <p>
 * See {@link SensorIcon} for an item that might want to have that type of
 * information, and {@link jmri.jmrit.display.panelEditor.PanelEditor} for an
 * example of how to use this.
 *
 * @author Pete Cressman Copyright (c) 2009, 2010
 */
public class IconAdder extends JPanel implements ListSelectionListener {

    private int ROW_HEIGHT;

    HashMap<String, JToggleButton> _iconMap;
    ArrayList<String> _iconOrderList;
    private JScrollPane _pickTablePane;
    private PickListModel _pickListModel;
    CatalogTreeNode _defaultIcons;      // current set of icons user has selected
    JPanel _iconPanel;
    private JPanel _buttonPanel;
    private String _type;
    private boolean _userDefaults;
    protected JTextField _sysNameText; // is set in IconAdderTest
    JTable _table;
    JButton _addButton;
    private JButton _addTableButton;
    private JButton _changeButton;
    private JButton _closeButton;
    private CatalogPanel _catalog;
    private JFrame _parent;
    private boolean _allowDeletes;
    boolean _update;    // updating existing icon from popup

    public IconAdder() {
        _userDefaults = false;
        _iconMap = new HashMap<>(10);
        _iconOrderList = new ArrayList<>();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public IconAdder(boolean allowDeletes) {
        this();
        _allowDeletes = allowDeletes;
    }

    public IconAdder(String type) {
        this();
        _type = type;
        initDefaultIcons();
    }

    public void reset() {
        if (_table != null) {
            _table.clearSelection();
        }
        closeCatalog();
        if (_defaultIcons != null) {
            makeIconPanel(true);
            log.debug("IconPanel ready");
        }
        this.revalidate();
    }

    public void initDefaultIcons() {
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        // unfiltered, xml-stored, default icon tree
        CatalogTree tree = manager.getBySystemName("NXDI");
        if (tree != null) {
            CatalogTreeNode node = tree.getRoot();

            Enumeration<TreeNode> e = node.children();

            while (e.hasMoreElements()) {
                CatalogTreeNode nChild = (CatalogTreeNode) e.nextElement();
                if (_type.equals(nChild.toString())) {
                    _defaultIcons = nChild; // consists of set of a NOI18N appearance name elements,
                    // each containing an icon URL path
                    _userDefaults = true;
                    break;
                }
            }
        }
        log.debug("initDefaultIcons: type= {}, defaultIcons= {}", _type, _defaultIcons);
    }

    /**
     * Replace the existing _defaultIcons TreeSet with a new set,
     * created from the current _iconMap set of icons. Note these might have I18N labels as their keys.
     * <p>
     * The new _defaultIcons might be a null Node.
     */
    private void createDefaultIconNodeFromMap() {
        log.debug("createDefaultIconNodeFromMap for node= {}, _iconOrderList.size()= {}", _type, _iconOrderList.size());
        _defaultIcons = new CatalogTreeNode(_type);
        for (Map.Entry<String, JToggleButton> entry : _iconMap.entrySet()) {
            NamedIcon icon = (NamedIcon) entry.getValue().getIcon();
            _defaultIcons.addLeaf(new CatalogTreeLeaf(entry.getKey(), icon.getURL(), _iconOrderList.indexOf(entry.getKey())));
        }
    }

    public CatalogTreeNode getDefaultIconNode() {
        log.debug("getDefaultIconNode for node= {}", _type);
        CatalogTreeNode defaultIcons = new CatalogTreeNode(_type);
        ArrayList<CatalogTreeLeaf> leafList = _defaultIcons.getLeaves();
        for (int i = 0; i < leafList.size(); i++) {
            CatalogTreeLeaf leaf = leafList.get(i);
            defaultIcons.addLeaf(new CatalogTreeLeaf(leaf.getName(), leaf.getPath(), i));
        }
        return defaultIcons;
    }

    /**
     * Build iconMap and orderArray from user's choice of defaults.
     *
     * @param n the root in a catalog from which icons are made
     */
    protected void makeIcons(CatalogTreeNode n) {
        if (log.isDebugEnabled()) {
            log.debug("makeIcons from node= {}, numChildren= {}, NumLeaves= {}",
                    n.toString(), n.getChildCount(), n.getNumLeaves());
        }
        _iconMap = new HashMap<>(10);
        _iconOrderList = new ArrayList<>();
        ArrayList<CatalogTreeLeaf> leafList = n.getLeaves();
        // adjust order of icons
        int k = leafList.size() - 1;
        for (int i = leafList.size() - 1; i >= 0; i--) {
            CatalogTreeLeaf leaf = leafList.get(i);
            String name = leaf.getName();
            String path = leaf.getPath();
            switch (name) {
                case "BeanStateInconsistent":
                    this.setIcon(0, name, new NamedIcon(path, path));
                    break;
                case "BeanStateUnknown":
                    this.setIcon(1, name, new NamedIcon(path, path));
                    break;
                default:
                    this.setIcon(k, name, new NamedIcon(path, path));
                    k--;
                    break;
            }
        }
    }

    /**
     * @param order the index to icon's name and the inverse order that icons
     *              are drawn in doIconPanel()
     * @param label the icon name displayed in the icon panel and the key
     *              to the icon button in _iconMap, supplied as I18N string
     * @param icon  the icon displayed in the icon button
     */
    protected void setIcon(int order, String label, NamedIcon icon) {
        // make a button to change that icon
        log.debug("setIcon at order= {}, key= {}", order, label);
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
        if (order > _iconOrderList.size()) {
            for (int i = _iconOrderList.size(); i < order; i++) {
                _iconOrderList.add(i, "placeHolder");
            }
        } else {
            if (order < _iconOrderList.size()) {
                _iconOrderList.remove(order);
            }
        }
        _iconOrderList.add(order, label);
    }

    /**
     * Install the icons used to represent all the states of the entity being
     * edited.
     *
     * @param order (reverse) order of display, (0 last, to N first)
     * @param label the state name to display. Must be unique from all other
     *              calls to this method
     * @param name  the resource name of the icon image to display
     */
    public void setIcon(int order, String label, String name) {
        log.debug("setIcon: order= {}, label= {}, name= {}", order, label, name);
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
     *
     * @param useDefaults true to use user-specified defaults; false otherwise
     */
    public void makeIconPanel(boolean useDefaults) {
        if (useDefaults && _userDefaults) {
            makeIcons(_defaultIcons);
        }
        log.debug("makeIconPanel updating");
        clearIconPanel();
        doIconPanel();
    }

    private void clearIconPanel() {
        if (_iconPanel != null) {
            this.remove(_iconPanel);
        }
        _iconPanel = new JPanel();
        _iconPanel.setLayout(new GridLayout(0,2));
    }

    protected void doIconPanel() {
        JPanel panel = null;
        for (int i = _iconOrderList.size() - 1; i >= 0; i--) {
            log.debug("adding icon #{}", i);
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            String key = _iconOrderList.get(i); // NOI18N
            // TODO BUG edit icon context usage in signal head; turnout etc work OK
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            String labelName = key;
            try {
                labelName = Bundle.getMessage(key); // I18N
            } catch (java.util.MissingResourceException mre) {
                log.warn("doIconPanel() property key {} missing", key);
            }
            JLabel name = new JLabel(labelName);
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(name);
            JToggleButton button = _iconMap.get(key);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(button);
            panel.add(p);
            // TODO align button centered in GridLayout EBR
            _iconPanel.add(panel);
        }
        this.add(_iconPanel, 0);
    }

    /**
     * After the calls to makeIconPanel(), optionally make a pick list table for
     * managed elements. (Not all Icon Editors use pick lists).
     *
     * @param tableModel the model from which the table is created
     */
    public void setPickList(PickListModel tableModel) {
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

    @SuppressWarnings("unchecked") // PickList is a parameterized class, but we don't use that here
    public void setSelection(NamedBean bean) {
        int row = _pickListModel.getIndexOf(bean);
        row = _table.convertRowIndexToView(row);
        _table.addRowSelectionInterval(row, row);
        _pickTablePane.getVerticalScrollBar().setValue(row * ROW_HEIGHT);
    }

    /**
     * When a Pick list is installed, table selection controls the Add button.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        log.debug("Table valueChanged: row= {}", row);
        if (row >= 0) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
            if (_type != null && _type.equals("SignalHead")) {
                // update Add Icon panel to match icons displayed to the selected signal head appearances
                makeIconMap(_pickListModel.getBeanAt(row)); // NOI18N
                clearIconPanel();
                doIconPanel();
            }
        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        }
        validate();
    }

    /**
     * Update/Recreate the iconMap for this bean, only called for SignalHeads.
     *
     * @param bean the object to create the map for
     */
    private void makeIconMap(NamedBean bean) {
        if (bean != null && _type != null && _type.equals("SignalHead")) {
            _iconMap = new HashMap<>(12);
            _iconOrderList = new ArrayList<>();
            ArrayList<CatalogTreeLeaf> leafList = _defaultIcons.getLeaves();
            int k = 0;
            String[] stateKeys = ((SignalHead) bean).getValidStateKeys(); // states contains non-localized appearances
            for (CatalogTreeLeaf leaf : leafList) {
                String name = leaf.getName(); // NOI18N
                log.debug("SignalHead Appearance leaf name= {}", name);
                for (String state : stateKeys) {
                    if (name.equals(state) || name.equals("SignalHeadStateDark")
                            || name.equals("SignalHeadStateHeld")) {
                        String path = leaf.getPath();
                        this.setIcon(k++, name, new NamedIcon(path, path));
                        break;
                    }
                }
            }
        } else { // no selection, revert to default signal head appearances
            makeIcons(_defaultIcons);
        }
        log.debug("makeIconMap: _iconMap.size()= {}", _iconMap.size());
    }

    private void checkIconSizes() {
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
                JOptionPane.showMessageDialog(this, Bundle.getMessage("IconSizeDiff"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            lastWidth = nextWidth;
            lastHeight = nextHeight;
        }
        log.debug("Size: width= {}, height= {}", lastWidth, lastHeight);
    }

    /**
     * Used by Panel Editor to make the final installation of the icon(s) into
     * the user's Panel.
     * <p>
     * Note! the selection is cleared. When two successive calls are made, the
     * 2nd will always return null, regardless of the 1st return.
     *
     * @return the selected item
     */
    public NamedBean getTableSelection() {
        if (InstanceManager.getDefault(CatalogTreeManager.class).isIndexChanged()) {
            checkIconSizes();
        }
        int row = _table.getSelectedRow();
        row = _table.convertRowIndexToModel(row);
        if (row >= 0) {
            NamedBean b = _pickListModel.getBeanAt(row);
            _table.clearSelection();
            _addButton.setEnabled(false);
            _addButton.setToolTipText(null);
            this.revalidate();
            if (b != null) {
                log.debug("getTableSelection: row = {}, bean = {}", row, b.getDisplayName());
            }
            return b;
        } else {
            log.debug("getTableSelection: row = 0");
        }
        return null;
    }

    /**
     * Get a new NamedIcon object for your own use.
     *
     * @param key Name of key (label)
     * @return Unique object
     */
    public NamedIcon getIcon(String key) {
        log.debug("getIcon for key= {}", key);
        return new NamedIcon((NamedIcon) _iconMap.get(key).getIcon());
    }

    /**
     * Get a new Hashtable of only the icons selected for display.
     *
     * @return a map of icons using the icon labels as keys
     */
    public Hashtable<String, NamedIcon> getIconMap() {
        log.debug("getIconMap: _allowDeletes= {}", _allowDeletes);
        Hashtable<String, NamedIcon> iconMap = new Hashtable<>();
        for (Map.Entry<String, JToggleButton> entry : _iconMap.entrySet()) {
            JToggleButton button = entry.getValue();
            log.debug("getIconMap: key= {}, button.isSelected()= {}", entry.getKey(), button.isSelected());
            if (!_allowDeletes || !button.isSelected()) {
                iconMap.put(entry.getKey(), new NamedIcon((NamedIcon) button.getIcon()));
            }
        }
        return iconMap;
    }

    /*
     * Support selection of NamedBean from a pick list table.
     *
     * @param addIconAction ActionListener that adds an icon to the panel -
     *          representing either an entity as pick list selection, an
     *          arbitrary image, or a value, such as a memory value
     * @param changeIconAction ActionListener that displays sources from
     *          which to select an image file
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
            _sysNameText = new JTextField();
            _sysNameText.setPreferredSize(
                    new Dimension(150, _sysNameText.getPreferredSize().height + 2));
            _addTableButton = new JButton(Bundle.getMessage("addToTable"));
            _addTableButton.addActionListener((ActionEvent a) -> {
                addToTable();
            });
            _addTableButton.setEnabled(false);
            _addTableButton.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
            p.add(_sysNameText);
            _sysNameText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent a) {
                    if (_sysNameText.getText().length() > 0) {
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
            _changeButton.addActionListener((ActionEvent a) -> {
                addCatalog();
            });
            p.add(_changeButton);
            _closeButton = new JButton(Bundle.getMessage("ButtonCloseCatalog"));
            _closeButton.addActionListener((ActionEvent a) -> {
                closeCatalog();
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
        if (_type != null) {
            createDefaultIconNodeFromMap();
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

    @SuppressWarnings("unchecked") // PickList is a parameterized class, but we don't use that here
    void addToTable() {
        String name = _sysNameText.getText();
        if (name != null && name.length() > 0) {
            NamedBean bean = _pickListModel.addBean(name);
            if (bean != null) {
                int setRow = _pickListModel.getIndexOf(bean);
                _table.setRowSelectionInterval(setRow, setRow);
                _pickTablePane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
            }
        }
        _sysNameText.setText("");
        _addTableButton.setEnabled(false);
        _addTableButton.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
    }

    /*
     * Add panel to change icons.
     */
    public void addCatalog() {
        log.debug("addCatalog() called");
        // add and display the catalog, so icons can be selected
        if (_catalog == null) {
            _catalog = CatalogPanel.makeDefaultCatalog();
            _catalog.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        }
        _catalog.setVisible(true); // display the tree view

        if (_changeButton != null) {
            _changeButton.setVisible(false);
            _closeButton.setVisible(true);
        }
        if (_pickTablePane != null) {
            _pickTablePane.setVisible(false); // hide the bean table during icon edit
        }
        pack();
    }

    void closeCatalog() {
        if (_changeButton != null) {
            _catalog.setVisible(false); // hide the tree view
            _changeButton.setVisible(true);
            _closeButton.setVisible(false);
        }
        if (_pickTablePane != null) {
            _pickTablePane.setVisible(true);
        }
        pack();
    }

    public void addDirectoryToCatalog() {
        if (_catalog == null) {
            _catalog = CatalogPanel.makeDefaultCatalog();
        }
        if (_changeButton != null) {
            _changeButton.setVisible(false);
            _closeButton.setVisible(true);
        }
        this.add(_catalog);
        this.pack();
    }

    /**
     * If icons are changed, update global tree.
     */
    private void updateCatalogTree() {
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        // unfiltered, xml-stored, default icon tree
        CatalogTree tree = manager.getBySystemName("NXDI");
        if (tree == null) { // build a new Default Icons tree
            tree = manager.newCatalogTree("NXDI", "Default Icons");
        }
        CatalogTreeNode root = tree.getRoot();

        Enumeration<TreeNode> e = root.children();

        String name = _defaultIcons.toString();
        while (e.hasMoreElements()) {
            CatalogTreeNode nChild = (CatalogTreeNode)e.nextElement();
            if (name.equals(nChild.toString())) {
                log.debug("Remove node {}", nChild);
                root.remove(nChild);
                break;
            }
        }
        root.add(_defaultIcons);
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
    }

    private class IconButton extends DropButton {

        String key; // NOI18N

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
        _iconOrderList = null;
        _catalog = null;
    }

    class DropButton extends JToggleButton implements DropTargetListener {

        DataFlavor dataFlavor;

        DropButton(Icon icon) {
            super(icon);
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.error("Unable to create drag and drop target.", cnfe);
            }
            // is the item created in this next line ever used?
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            // log.debug("DropJLabel ctor");
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            // log.debug("DropJLabel.dragExit ");
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            // log.debug("DropJLabel.dragEnter ");
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // log.debug("DropJLabel.dragOver ");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // log.debug("DropJLabel.dropActionChanged ");
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon newIcon = (NamedIcon) tr.getTransferData(dataFlavor);
                    if (newIcon != null) { // newIcon never null according to contract
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
                        log.debug("DropJLabel.drop COMPLETED for {}, {}", key, newIcon.getURL());
                    } else {
                        log.debug("DropJLabel.drop REJECTED!");
                        e.rejectDrop();
                    }
                }
            } catch (IOException ioe) {
                log.debug("DropPanel.drop REJECTED!");
                e.rejectDrop();
            } catch (UnsupportedFlavorException ufe) {
                log.debug("DropJLabel.drop REJECTED!");
                e.rejectDrop();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IconAdder.class);

}
