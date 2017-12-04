package jmri.jmrit.catalog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JPanel containing trees of resources to replace default icons. The
 * panel also displays image files files contained in a node of a tree. Drag and
 * Drop is implemented to drag a display of an icon to the display of an icon
 * that may be added to the panel.
 * <p>
 * This panel is used in the Icon Editors and also in the ImageIndex Editor.
 *
 * @author Pete Cressman Copyright 2009
 */
public class CatalogPanel extends JPanel implements MouseListener {

    public static final double ICON_SCALE = 0.010;
    public static final int ICON_WIDTH = 100;
    public static final int ICON_HEIGHT = 100;

    JPanel _selectedImage;
    static Color _grayColor = new Color(235, 235, 235);
    static Color _darkGrayColor = new Color(150, 150, 150);
    protected Color[] colorChoice = new Color[] {Color.white, _grayColor, _darkGrayColor};
    protected Color _currentBackground = _grayColor;
    protected BufferedImage[] _backgrounds; // array of Image backgrounds

    JLabel _previewLabel = new JLabel(" ");
    protected ImagePanel _preview;
    protected JScrollPane js;
    boolean _noDrag;

    JScrollPane _treePane;
    JTree _dTree;
    DefaultTreeModel _model;
    ArrayList<CatalogTree> _branchModel = new ArrayList<>();

    public CatalogPanel() {
        _model = new DefaultTreeModel(new CatalogTreeNode("mainRoot"));
    }

    public CatalogPanel(String label1, String label2) {
        super(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel(Bundle.getMessage(label2)));
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        _treePane = new JScrollPane(_dTree);
        _treePane.setMaximumSize(new Dimension(300, 10000));
        p1.add(new JLabel(Bundle.getMessage(label1)));
        p1.add(_treePane);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(p1);
        p.add(makePreviewPanel());
        add(p);
        add(makeButtonPanel());
    }

    @Override
    public void setToolTipText(String tip) {
        if (_dTree != null) {
            _dTree.setToolTipText(tip);
        }
        if (_treePane != null) {
            _treePane.setToolTipText(tip);
        }
        super.setToolTipText(tip);
    }

    protected void init(boolean treeDnD) {
        _model = new DefaultTreeModel(new CatalogTreeNode("mainRoot"));
        if (treeDnD) { // index editor (right pane)
            _dTree = new DropJTree(_model);
            _noDrag = true;
        } else {       // Catalog (left pane index editor or all icon editors)
            _dTree = new JTree(_model);
            _noDrag = false;
        }
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getClosedIcon());
        _dTree.setCellRenderer(renderer);
        _dTree.setRootVisible(false);
        _dTree.setShowsRootHandles(true);
        _dTree.setScrollsOnExpand(true);
        //_dTree.setDropMode(DropMode.ON);
        _dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        _dTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            updatePanel();
        });
        _dTree.setExpandsSelectedPaths(true);
        _treePane.setViewportView(_dTree);
    }

    public void updatePanel() {
        if (log.isDebugEnabled()) {
            log.debug("updatePanel: _dTree.isSelectionEmpty()= {} _dTree.getSelectionPath() is {}null",
                    _dTree.isSelectionEmpty(), (_dTree.getSelectionPath() == null) ? "" : "not ");
        }
        if (!_dTree.isSelectionEmpty() && _dTree.getSelectionPath() != null) {
            try {
                _previewLabel.setText(setIcons());
            } catch (OutOfMemoryError oome) {
                resetPanel();
                if (log.isDebugEnabled()) {
                    log.debug("setIcons threw OutOfMemoryError {}", oome);
                }
            }
        } else {
            _previewLabel.setText(" ");
        }
    }

    /**
     * Create a new model and add it to the main root.
     * <p>
     * Can be called from off the GUI thread.
     *
     * @param systemName the system name for the catalog
     * @param userName   the user name for the catalog
     * @param path       the path on the new branch
     */
    public void createNewBranch(String systemName, String userName, String path) {

        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        CatalogTree tree = manager.getBySystemName(systemName);
        if (tree != null) {
            jmri.util.ThreadingUtil.runOnGUI(() -> {
                addTree(tree);
            });
        } else {
            final CatalogTree t = manager.newCatalogTree(systemName, userName);
            jmri.util.ThreadingUtil.runOnGUI(() -> {
                t.insertNodes(path);
                addTree(t);
            });
        }
    }

    /**
     * Extend the Catalog by adding a tree to the root.
     *
     * @param tree the tree to add to the catalog
     */
    public void addTree(CatalogTree tree) {
        String name = tree.getSystemName();
        for (int i = 0; i < _branchModel.size(); i++) {
            if (name.equals(_branchModel.get(i).getSystemName())) {
                return;
            }
        }
        addTreeBranch(tree.getRoot());
        _branchModel.add(tree);
        _model.reload();
    }

    /**
     * Recursively add the branch nodes to display tree.
     */
    @SuppressWarnings("unchecked")
    private void addTreeBranch(CatalogTreeNode node) {
        if (log.isDebugEnabled()) {
            log.debug("addTreeBranch called for node= {}, has {} children.",
                    node.toString(), node.getChildCount());
        }
        //String name = node.toString();
        CatalogTreeNode root = (CatalogTreeNode) _model.getRoot();
        Enumeration<CatalogTreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            addNode(root, n);
        }
    }

    /**
     * Clones the node and adds to parent.
     */
    @SuppressWarnings("unchecked")
    private void addNode(CatalogTreeNode parent, CatalogTreeNode n) {
        CatalogTreeNode node = new CatalogTreeNode((String) n.getUserObject());
        node.setLeaves(n.getLeaves());
        parent.add(node);
        Enumeration<CatalogTreeNode> e = n.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode nChild = e.nextElement();
            addNode(node, nChild);
        }
    }

    /**
     * The tree held in the CatalogTreeManager must be kept in sync with the
     * tree displayed as the Image Index. Required in order to save the Index to
     * disc.
     */
    private CatalogTreeNode getCorrespondingNode(CatalogTreeNode node) {
        TreeNode[] nodes = node.getPath();
        CatalogTreeNode cNode = null;
        for (int i = 0; i < _branchModel.size(); i++) {
            CatalogTreeNode cRoot = _branchModel.get(i).getRoot();
            cNode = match(cRoot, nodes, 1);
            if (cNode != null) {
                break;
            }
        }
        return cNode;
    }

    /**
     * Find the corresponding node in a CatalogTreeManager tree with a displayed
     * node.
     */
    @SuppressWarnings("unchecked")
    private CatalogTreeNode match(CatalogTreeNode cRoot, TreeNode[] nodes, int idx) {
        if (idx == nodes.length) {
            return cRoot;
        }
        Enumeration<CatalogTreeNode> e = cRoot.children();
        CatalogTreeNode result = null;
        while (e.hasMoreElements()) {
            CatalogTreeNode cNode = e.nextElement();
            if (nodes[idx].toString().equals(cNode.toString())) {
                result = match(cNode, nodes, idx + 1);
                break;
            }
        }
        return result;
    }

    /**
     * Find the corresponding CatalogTreeManager tree to the displayed branch.
     */
    private CatalogTree getCorespondingModel(CatalogTreeNode node) {
        TreeNode[] nodes = node.getPath();
        CatalogTree model = null;
        for (int i = 0; i < _branchModel.size(); i++) {
            model = _branchModel.get(i);
            CatalogTreeNode cRoot = model.getRoot();
            if (match(cRoot, nodes, 1) != null) {
                break;
            }
        }
        return model;
    }

    /**
     * Insert a new node into the displayed tree.
     *
     * @param name   the name of the new node
     * @param parent the parent of name
     * @return true if the node was inserted
     */
    @SuppressWarnings("unchecked")
    public boolean insertNodeIntoModel(String name, CatalogTreeNode parent) {
        if (!nameOK(parent, name)) {
            return false;
        }
        int index = 0;
        Enumeration<CatalogTreeNode> e = parent.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            if (name.compareTo(n.toString()) < 0) {
                break;
            }
            index++;
        }
        CatalogTreeNode newChild = new CatalogTreeNode(name);
        _model.insertNodeInto(newChild, parent, index);
        CatalogTreeNode cParent = getCorrespondingNode(parent);
        CatalogTreeNode node = new CatalogTreeNode(name);
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(parent);
        tree.insertNodeInto(node, cParent, index);
        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
        return true;
    }

    /**
     * Delete a node from the displayed tree.
     *
     * @param node the node to delete
     */
    public void removeNodeFromModel(CatalogTreeNode node) {
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(node);
        tree.removeNodeFromParent(getCorrespondingNode(node));
        _model.removeNodeFromParent(node);
        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
    }

    /**
     * Make a change to a node in the displayed tree. Either its name or the
     * contents of its leaves (image references).
     *
     * @param node the node to change
     * @param name new name for the node
     * @return true if the change was successful
     */
    public boolean nodeChange(CatalogTreeNode node, String name) {
        CatalogTreeNode cNode = getCorrespondingNode(node);
        cNode.setLeaves(node.getLeaves());
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(node);

        cNode.setUserObject(name);
        node.setUserObject(name);
        tree.nodeChanged(cNode);
        _model.nodeChanged(node);
        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
        updatePanel();
        return true;
    }

    /**
     * Check that Node names in the path to the root are unique.
     */
    private boolean nameOK(CatalogTreeNode node, String name) {
        TreeNode[] nodes = node.getPath();
        for (TreeNode node1 : nodes) {
            if (name.equals(node1.toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Only call when log.isDebugEnabled() is true
     *
     * public void enumerateTree() { CatalogTreeNode root =
     * (CatalogTreeNode)_model.getRoot(); log.debug("enumerateTree called for
     * root= "+root.toString()+ ", has "+root.getChildCount()+" children");
     * Enumeration e =root.depthFirstEnumeration(); while (e.hasMoreElements())
     * { CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
     * log.debug("nodeName= "+n.getUserObject()+" has "+n.getLeaves().size()+"
     * leaves."); } }
     */

    /**
     * Set up a display pane for a tree that shows only directory nodes (no file
     * leaves). The leaves (icon images) will be displayed in this panel.
     */
    private JPanel makePreviewPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.add(_previewLabel);
        _preview = new ImagePanel();
        log.debug("Catalog ImagePanel created");
        _preview.setLayout(new BoxLayout(_preview, BoxLayout.Y_AXIS));
        _preview.setOpaque(false);
        js = new JScrollPane(_preview);
        previewPanel.add(js);

        // create array of backgrounds
        _backgrounds = new BufferedImage[4];
        for (int i = 0; i <= 2; i++) {
            _backgrounds[i] = DrawSquares.getImage(500, 500, 15, colorChoice[i], colorChoice[i]);
        }
        _backgrounds[3] = DrawSquares.getImage(500, 500, 15, Color.white, _grayColor);

        return previewPanel;
    }

    /**
     * Create panel element containing a "View on:" drop down list.
     * @see jmri.jmrit.catalog.PreviewDialog#setupPanel()
     *
     * @return the JPanel with label and drop down
     */
    private JPanel makeButtonPanel() {
        JComboBox<String> bgColorBox = new JComboBox<>();
        bgColorBox.addItem(Bundle.getMessage("White"));
        bgColorBox.addItem(Bundle.getMessage("LightGray"));
        bgColorBox.addItem(Bundle.getMessage("DarkGray"));
        bgColorBox.addItem(Bundle.getMessage("Checkers")); // checkers option
        bgColorBox.setSelectedIndex(0); // start as "White"
        bgColorBox.addActionListener((ActionEvent e) -> {
            // load background image
            _preview.setImage(_backgrounds[bgColorBox.getSelectedIndex()]);
            log.debug("Catalog setImage called");
            _preview.setOpaque(false);
            // _preview.repaint(); // force redraw
            _preview.invalidate();
        });

        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
        pp.add(new JLabel(Bundle.getMessage("setBackground")));
        pp.add(bgColorBox);
        backgroundPanel.add(pp);
        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
        return backgroundPanel;
    }

    /**
     * Reset the background to _currentBackground.
     * Visible on JPanels and JLabels, not on ImagePanels used as Preview background.
     *
     * @param container the parent to set plus all its children's background color
     * @deprecated since 4.9.6 use transparent icon panels on a graphic ImagePanel background
     * @see jmri.util.swing.ImagePanel#setImage(Image)
     */
    @Deprecated
    public void setBackground(Container container) {
        container.setBackground(_currentBackground);
        Component[] comp = container.getComponents();
        for (Component comp1 : comp) {
            if (comp1 instanceof java.awt.Container) {
                setBackground((Container) comp1);
            }
        }
        container.invalidate();
    }

    protected void resetPanel() {
        _selectedImage = null;
        if (_preview == null) {
            return;
        }
        Component[] comp = _preview.getComponents();
        for (Component comp1 : comp) {
            comp1.removeMouseListener(this);
        }
        _preview.removeAll();
        _preview.repaint();
    }

    public class MemoryExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            _noMemory = true;
            log.error("MemoryExceptionHandler: {}", e);
        }
    }

    private boolean _noMemory = false;

    /**
     * Display the icons in the preview panel.
     */
    private String setIcons() {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        resetPanel();
        CatalogTreeNode node = getSelectedNode();
        if (node == null) {
            return null;
        }
        List<CatalogTreeLeaf> leaves = node.getLeaves();
        if (leaves == null) {
            return null;
        }
        int numCol = 1;
        while (numCol * numCol < leaves.size()) {
            numCol++;
        }
        if (numCol > 1) {
            numCol--;
        }
        int numRow = leaves.size() / numCol;
        boolean newCol = false;
        _noMemory = false;
        // VM launches another thread to run ImageFetcher.
        // This handler will catch memory exceptions from that thread
        Thread.setDefaultUncaughtExceptionHandler(new MemoryExceptionHandler());
        GridBagLayout gridbag = new GridBagLayout();
        _preview.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy = 0;
        c.gridx = -1;
        for (int i = 0; i < leaves.size(); i++) {
            if (_noMemory) {
                continue;
            }
            CatalogTreeLeaf leaf = leaves.get(i);
            NamedIcon icon = new NamedIcon(leaf.getPath(), leaf.getName());
            double scale = icon.reduceTo(ICON_WIDTH, ICON_HEIGHT, ICON_SCALE);
            if (_noMemory) {
                continue;
            }
            if (c.gridx < numCol) {
                c.gridx++;
            } else if (c.gridy < numRow) { // start next row
                c.gridy++;
                if (!newCol) {
                    c.gridx = 0;
                }
            } else if (!newCol) { // start new column
                c.gridx++;
                numCol++;
                c.gridy = 0;
                newCol = true;
            } else {  // start new row
                c.gridy++;
                numRow++;
                c.gridx = 0;
                newCol = false;
            }
            c.insets = new Insets(5, 5, 0, 0);

            JLabel nameLabel;
            if (_noDrag) {
                nameLabel = new JLabel();
            } else {
                try {
                    nameLabel = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime));
                } catch (java.lang.ClassNotFoundException cnfe) {
                    log.warn("Unable to create drag label", cnfe);
                    continue;
                }
            }
            nameLabel.setName(leaf.getName());
            nameLabel.setOpaque(false);
            nameLabel.setIcon(icon);

            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(nameLabel);
            JLabel label = new JLabel(Bundle.getMessage("scale", CatalogPanel.printDbl(scale, 2)));
            p.add(label);
            label = new JLabel(leaf.getName());
            label.setOpaque(false);
            p.add(label);
            if (_noDrag) {
                p.addMouseListener(this);
            }
            gridbag.setConstraints(p, c);
            _preview.add(p);
            if (log.isDebugEnabled()) {
                log.debug("{} inserted at ({}, {})", leaf.getName(), c.gridx, c.gridy);
            }
        }
        c.gridy++;
        c.gridx++;
        JLabel bottom = new JLabel();
        gridbag.setConstraints(bottom, c);
        _preview.add(bottom);

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        return Bundle.getMessage("numImagesInNode", node.getUserObject(), leaves.size());
    }

    public static CatalogPanel makeDefaultCatalog() {
        CatalogPanel catalog = new CatalogPanel("catalogs", "selectNode");
        catalog.init(false);
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        List<String> sysNames = manager.getSystemNameList();
        for (int i = 0; i < sysNames.size(); i++) {
            String systemName = sysNames.get(i);
            if (systemName.charAt(0) == 'I') {
                catalog.addTree(manager.getBySystemName(systemName));
            }
        }
        catalog.createNewBranch("IFJAR", "Program Directory", "resources");
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "resources");
        catalog.createNewBranch("IFPREF", "Preferences Directory", FileUtil.getUserFilesPath() + "resources");
        return catalog;
    }

    public static Frame getParentFrame(Component comp) {
        while (true) {
            if (comp instanceof Frame) {
                return (Frame) comp;
            }
            if (comp == null) {
                return null;
            }
            comp = comp.getParent();
        }
    }

    public static void packParentFrame(Component comp) {
        Frame frame = getParentFrame(comp);
        if (frame != null) {
            frame.pack();
        }
    }

    /**
     * Utility returning a number as a string.
     *
     * @param z             double
     * @param decimalPlaces number of decimal places
     * @return String
     */
    public static String printDbl(double z, int decimalPlaces) {
        if (Double.isNaN(z) || decimalPlaces > 8) {
            return Double.toString(z);
        } else if (decimalPlaces <= 0) {
            return Integer.toString((int) Math.rint(z));
        }
        StringBuilder sb = new StringBuilder();
        if (z < 0) {
            sb.append('-');
        }
        z = Math.abs(z);
        int num = 1;
        int d = decimalPlaces;
        while (d-- > 0) {
            num *= 10;
        }
        int x = (int) Math.rint(z * num);
        int ix = x / num;                     // integer part
        int dx = x - ix * num;
        sb.append(ix);
        if (dx == 0) {
            return sb.toString();
        }
        if (decimalPlaces > 0) {
            sb.append('.');
            num /= 10;
            while (num > dx) {
                sb.append('0');
                num /= 10;
            }
            sb.append(dx);
        }
        return sb.toString();
    }

    protected void setSelectedNode(String[] names) {
        _dTree.setExpandsSelectedPaths(true);
        CatalogTreeNode[] path = new CatalogTreeNode[names.length];
        for (int i = 0; i < names.length; i++) {
            path[i] = new CatalogTreeNode(names[i]);
        }
        _dTree.setSelectionPath(new TreePath(path));
    }

    protected void scrollPathToVisible(String[] names) {
        _dTree.setExpandsSelectedPaths(true);
        CatalogTreeNode[] path = new CatalogTreeNode[names.length];
        for (int i = 0; i < names.length; i++) {
            path[i] = new CatalogTreeNode(names[i]);
        }
        _dTree.scrollPathToVisible(new TreePath(path));
    }

    /**
     * Return the node the user has selected.
     *
     * @return CatalogTreeNode
     */
    protected CatalogTreeNode getSelectedNode() {
        if (!_dTree.isSelectionEmpty() && _dTree.getSelectionPath() != null) {
            // somebody has been selected
            if (log.isDebugEnabled()) {
                log.debug("getSelectedNode with {}", _dTree.getSelectionPath().toString());
            }
            TreePath path = _dTree.getSelectionPath();
            return (CatalogTreeNode) path.getLastPathComponent();
        }
        return null;
    }

    private void delete(NamedIcon icon) {
        CatalogTreeNode node = getSelectedNode();
        if (log.isDebugEnabled()) {
            log.debug("delete icon {} from node {}", icon.getName(), node.toString());
        }
        node.deleteLeaf(icon.getName(), icon.getURL());
        updatePanel();
        InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
    }

    private void rename(NamedIcon icon) {
        String name = JOptionPane.showInputDialog(getParentFrame(this),
                Bundle.getMessage("newIconName"), icon.getName(),
                JOptionPane.QUESTION_MESSAGE);
        if (name != null && name.length() > 0) {
            CatalogTreeNode node = getSelectedNode();
            if (log.isDebugEnabled()) {
                log.debug("rename icon {} to {} from node {}", icon.getName(), name, node.toString());
            }
            CatalogTreeLeaf leaf = node.getLeaf(icon.getName(), icon.getURL());
            if (leaf != null) {
                leaf.setName(name);
            }
            TreePath path = _dTree.getSelectionPath();
            // deselect to refresh panel
            _dTree.setSelectionPath(null);
            _dTree.setSelectionPath(path);
            // updatePanel();
            InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
        }
    }

    /**
     * Return the icon selected in the preview panel Save this code in case
     * there is a need to use an alternative icon changing method rather than
     * DnD.
     *
     * public NamedIcon getSelectedIcon() { if (_selectedImage != null) { JLabel
     * l = (JLabel)_selectedImage.getComponent(0); // deselect
     * //setSelectionBackground(_currentBackground); Save for use as alternative
     * to DnD. _selectedImage = null; return (NamedIcon)l.getIcon(); } return
     * null; }
     */

    private void showPopUp(MouseEvent e, NamedIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("showPopUp {}", icon.toString());
        }
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(icon.getName()));
        popup.add(new JMenuItem(icon.getURL()));
        popup.add(new javax.swing.JPopupMenu.Separator());

        popup.add(new AbstractAction(Bundle.getMessage("RenameIcon")) {
            NamedIcon icon;

            @Override
            public void actionPerformed(ActionEvent e) {
                rename(icon);
            }

            AbstractAction init(NamedIcon i) {
                icon = i;
                return this;
            }
        }.init(icon));
        popup.add(new javax.swing.JPopupMenu.Separator());

        popup.add(new AbstractAction(Bundle.getMessage("DeleteIcon")) {
            NamedIcon icon;

            @Override
            public void actionPerformed(ActionEvent e) {
                delete(icon);
            }

            AbstractAction init(NamedIcon i) {
                icon = i;
                return this;
            }
        }.init(icon));
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Container con = (Container) e.getSource();
            JLabel label = (JLabel) con.getComponent(0);
            NamedIcon icon = (NamedIcon) label.getIcon();
            showPopUp(e, icon);
        }
    }

    class DropJTree extends JTree implements DropTargetListener {

        DataFlavor dataFlavor;

        DropJTree(TreeModel model) {
            super(model);
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.warn("Unable to create data flavor", cnfe);
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //if (log.isDebugEnabled()) log.debug("DropJTree ctor");
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragExit ");
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragEnter ");
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragOver ");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dropActionChanged ");
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon icon = (NamedIcon) tr.getTransferData(dataFlavor);
                    Point pt = e.getLocation();
                    if (log.isDebugEnabled()) {
                        log.debug("DropJTree.drop: Point= ({}, {})", pt.x, pt.y);
                    }
                    TreePath path = _dTree.getPathForLocation(pt.x, pt.y);
                    if (path != null) {
                        CatalogTreeNode node = (CatalogTreeNode) path.getLastPathComponent();
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        node.addLeaf(icon.getName(), icon.getURL());
                        nodeChange(node, node.toString());
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) {
                            log.debug("DropJTree.drop COMPLETED for {}", icon.getURL());
                        }
                        return;
                    }
                }
            } catch (IOException | UnsupportedFlavorException ex) {
                log.warn("unable to drag and drop", ex);
            }
            if (log.isDebugEnabled()) {
                log.debug("DropJTree.drop REJECTED!");
            }
            e.rejectDrop();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CatalogPanel.class);

}
