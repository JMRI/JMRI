package jmri.jmrit.catalog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
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
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.palette.IconItemPanel;
import jmri.util.FileUtil;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JPanel containing trees of resources to replace default icons. The
 * panel also displays image files contained in a node of a tree. Drag and
 * Drop is implemented to drag a display of an icon to the display of an icon
 * that may be added to the panel.
 * <p>
 * This panel is used in the Icon Editors and also in the {@link ImageIndexEditor}.
 *
 * @author Pete Cressman Copyright 2009, 2018
 * @author Egbert Broerse Copyright 2017
 */
public class CatalogPanel extends JPanel {

    public static final double ICON_SCALE = 0.020;
    public static final int ICON_WIDTH = 100;
    public static final int ICON_HEIGHT = 100;

    private IconDisplayPanel _selectedImage;
    private IconItemPanel _parent;      // IconItemPanel could implement an interface if other classes use deselectIcon()
    private JSplitPane _splitPane;
    static Color _grayColor = new Color(235, 235, 235);
    static Color _darkGrayColor = new Color(150, 150, 150);
    protected Color[] colorChoice = new Color[] {Color.white, _grayColor, _darkGrayColor};
    /**
     * Array of BufferedImage backgrounds loaded as background image in Preview.
     */
    protected BufferedImage[] _backgrounds;

    JScrollPane _iconPane;
    JLabel _previewLabel = new JLabel(" ");
    protected ImagePanel _preview;
    private boolean _treeDnd;
    private boolean _dragIcons;

    private JScrollPane _treePane;
    private JTree _dTree;
    private DefaultTreeModel _model;
    private final ArrayList<CatalogTree> _branchModel = new ArrayList<>();

    /**
     * Constructor
     */
    public CatalogPanel() {
        _model = new DefaultTreeModel(new CatalogTreeNode("mainRoot"));
    }

    /**
     * Ctor for a named icon catalog split pane. Make sure both properties keys exist.
     *
     * @param label1 properties key to be used as the label for the icon tree
     * @param label2 properties key to be used as the instruction
     * @param addButtonPanel adds background select comboBox
     */
    public CatalogPanel(String label1, String label2, boolean addButtonPanel) {
        super(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        add(new JLabel(Bundle.getMessage(label2)), BorderLayout.NORTH);
        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                makeTreePanel(label1), makePreviewPanel()); // create left and right icon tree views
        _splitPane.setContinuousLayout(true);
        _splitPane.setOneTouchExpandable(true);
        add(_splitPane, BorderLayout.CENTER);
        if (addButtonPanel) {
            add(makeButtonPanel(), BorderLayout.SOUTH); // add the background chooser
        }
    }

    /**
     * Ctor for a named icon catalog split pane. Make sure both properties keys exist.
     *
     * @param label1 properties key to be used as the label for the icon tree
     * @param label2 properties key to be used as the instruction
     */
    public CatalogPanel(String label1, String label2) {
        this(label1, label2, true);
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

    /**
     * Customize CatalogPanel to be used either as editing/creating an ImageEditor
     * or as a panel to display or deliver icons to widgets
     * @param treeDnD true allows dropping into tree or panel
     * @param dragIcons true allows dragging icons from panel
     */
    protected void init(boolean treeDnD, boolean dragIcons) {
        _model = new DefaultTreeModel(new CatalogTreeNode("mainRoot"));
        if (treeDnD) { // index editor (right pane)
            _dTree = new DropJTree(_model);
            setTransferHandler(new DropOnPanelToNode());
        } else {       // Catalog (left pane index editor or all icon editors)
            _dTree = new JTree(_model);
        }
        _treeDnd = treeDnD;
        _dragIcons = dragIcons;
        log.debug("CatalogPanel.init _treeDnd= {}, _dragIcons= {}", _treeDnd, _dragIcons);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getClosedIcon());
        _dTree.setCellRenderer(renderer);
        _dTree.setRootVisible(false);
        _dTree.setShowsRootHandles(true);
        _dTree.setScrollsOnExpand(true);
        _dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        _dTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            updatePanel();
        });
        _dTree.setExpandsSelectedPaths(true);
        _treePane.setViewportView(_dTree);
    }
    
    public void setParent(IconItemPanel p) {
        _parent = p;
    }
    
    public void updatePanel() {
        log.debug("updatePanel: _dTree.isSelectionEmpty()= {} _dTree.getSelectionPath() is {}null",
                _dTree.isSelectionEmpty(), (_dTree.getSelectionPath() == null) ? "" : "not ");
        if (!_dTree.isSelectionEmpty() && _dTree.getSelectionPath() != null) {
            try {
                _previewLabel.setText(setIcons());
            } catch (OutOfMemoryError oome) {
                resetPanel();
                log.debug("setIcons threw OutOfMemoryError {}", oome);
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
     * For Index Editor to able to edit its tree
     * @return tree
     */
    protected JTree getTree() {
        return _dTree;
    }

    /**
     * Extend the Catalog by adding a tree to the root.
     *
     * @param tree the tree to add to the catalog
     */
    public void addTree(CatalogTree tree) {
        String name = tree.getSystemName();
        for (CatalogTree t : _branchModel) {
            if (name.equals(t.getSystemName())) {
                return;
            }
        }
        addTreeBranch(tree.getRoot());
        _branchModel.add(tree);
        _model.reload();
    }

    /**
     * Recursively add the branch nodes to the display tree.
     */
    private void addTreeBranch(CatalogTreeNode node) {
        if (log.isDebugEnabled()) {
            log.debug("addTreeBranch called for node= {}, has {} children.",
                    node.toString(), node.getChildCount());
        }
        CatalogTreeNode root = (CatalogTreeNode) _model.getRoot();
        Enumeration<TreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
            addNode(root, n);
        }
    }

    /**
     * Clone the node and adds to parent.
     */
    private void addNode(CatalogTreeNode parent, CatalogTreeNode n) {
        CatalogTreeNode node = new CatalogTreeNode((String) n.getUserObject());
        node.setLeaves(n.getLeaves());
        parent.add(node);
        Enumeration<TreeNode> e = n.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode nChild = (CatalogTreeNode)e.nextElement();
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
        for (CatalogTree t : _branchModel) {
            CatalogTreeNode cRoot = t.getRoot();
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
    private CatalogTreeNode match(CatalogTreeNode cRoot, TreeNode[] nodes, int idx) {
        if (idx == nodes.length) {
            return cRoot;
        }
        Enumeration<TreeNode> e = cRoot.children();
        CatalogTreeNode result = null;
        while (e.hasMoreElements()) {
            CatalogTreeNode cNode = (CatalogTreeNode)e.nextElement();
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
        for (CatalogTree t : _branchModel) {
            model = t;
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
    protected boolean insertNodeIntoModel(String name, CatalogTreeNode parent) {
        if (!nameOK(parent, name)) {
            return false;
        }
        int index = 0;
        Enumeration<TreeNode> e = parent.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
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
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
        return true;
    }

    /**
     * Delete a node from the displayed tree.
     *
     * @param node the node to delete
     */
    protected void removeNodeFromModel(CatalogTreeNode node) {
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(node);
        tree.removeNodeFromParent(getCorrespondingNode(node));
        _model.removeNodeFromParent(node);
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
    }

    /**
     * Make a change to a node in the displayed tree. Either its name or the
     * contents of its leaves (image references).
     *
     * @param node the node to change
     * @param name new name for the node
     * @return true if the change was successful
     */
    protected boolean renameNode(CatalogTreeNode node, String name) {
        if (!nameOK((CatalogTreeNode)node.getParent(), name)) {
            return false;
        }
        CatalogTreeNode cNode = getCorrespondingNode(node);
        cNode.setLeaves(node.getLeaves());
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(node);

        cNode.setUserObject(name);
        tree.nodeChanged(cNode);
        node.setUserObject(name);
        _model.nodeChanged(node);
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
        updatePanel();
        return true;
    }
    
    private void addLeaf(CatalogTreeNode node, NamedIcon icon) {
        node.addLeaf(icon.getName(), icon.getURL());

        CatalogTreeNode cNode = getCorrespondingNode(node);
        cNode.setLeaves(node.getLeaves());
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(node);

        cNode.setUserObject(node.toString());
        tree.nodeChanged(cNode);
        _model.nodeChanged(node);
        
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
        if (node.equals(getSelectedNode())) {
            updatePanel();            
        }
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

    private JPanel makeTreePanel(String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        _treePane = new JScrollPane(_dTree);
        panel.add(new JLabel(Bundle.getMessage(label)));
        // _treePane.setPreferredSize(new Dimension(100, 350));
        _treePane.setMinimumSize(new Dimension(30, 100));
        panel.add(_treePane);
        return panel;
    }
    /**
     * Set up a display pane for a tree that shows only directory nodes (no file
     * leaves). The leaves (icon images) will be displayed in this panel.
     */
    private JPanel makePreviewPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.add(_previewLabel);
        _preview = new ImagePanel();
        _preview.setOpaque(false);
        _iconPane =  new JScrollPane(_preview);
        previewPanel.add(_iconPane);
        // _preview.setMaximumSize(new Dimension(200,200));
        _iconPane.setMinimumSize(new Dimension(30, 100));
        _iconPane.setPreferredSize(new Dimension(2*ICON_WIDTH, 2*ICON_HEIGHT));
        return previewPanel;
    }

    /**
     * Create panel element containing a "View on:" drop down list.
     * Employs a normal JComboBox, no Panel Background option.
     *
     * @return the JPanel with label and drop down
     */
    private JPanel makeButtonPanel() {
        // create array of backgrounds
        if (_backgrounds == null) {
            _backgrounds = new BufferedImage[4];
            for (int i = 0; i <= 2; i++) {
                _backgrounds[i] = DrawSquares.getImage(300, 400, 10, colorChoice[i], colorChoice[i]);
            }
            _backgrounds[3] = DrawSquares.getImage(300, 400, 10, Color.white, _grayColor);
        }
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
     * Allows ItemPalette to set the preview panel background to match that of
     * the icon set being edited.
     *
     * @return Preview panel
     */
    public ImagePanel getPreviewPanel() {
        return _preview;
    }

    protected void resetPanel() {
        _selectedImage = null;
        if (_preview == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("_preview.removeAll done.");
        }
        _preview.removeAll();
        _preview.repaint();
    }

    // called by palette.IconItemPanel to get user's selection from catalog
    public NamedIcon getIcon() {
        if (_selectedImage != null) {
            return _selectedImage.getIcon();            
        }
        return null;
    }

    // called by palette.IconItemPanel when selection is made for its iconMap
    public void deselectIcon() {
        if (_selectedImage !=null) {
            _selectedImage.setBorder(null);
            _selectedImage = null;
        }
    }
    
    protected void setSelection(IconDisplayPanel panel) {
        if (_parent == null) {
            return;            
        }
        if (_selectedImage != null && !panel.equals(_selectedImage)) {
            deselectIcon();
        }
        if (panel != null) {
            panel.setBorder(BorderFactory.createLineBorder(Color.red, 2));
            _selectedImage = panel;
        } else {
            deselectIcon();
        }
        _parent.deselectIcon();            
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
                c.gridy = 0;
                newCol = true;
            } else {  // start new row
                c.gridy++;
                c.gridx = 0;
                newCol = false;
            }
            c.insets = new Insets(5, 5, 0, 0);

            JPanel p = new IconDisplayPanel(leaf.getName(), icon);
            gridbag.setConstraints(p, c);
            _preview.add(p);
            // log.debug("{} inserted at ({}, {})", leaf.getName(), c.gridx, c.gridy);
        }
        _preview.invalidate();

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        return Bundle.getMessage("numImagesInNode", node.getUserObject(), leaves.size());
    }

    class IconListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getSource() instanceof IconDisplayPanel) {
                IconDisplayPanel panel = (IconDisplayPanel)event.getSource();
                setSelection(panel);
            } else if(event.getSource() instanceof ImagePanel) {
                deselectIcon();
           }
        }
        @Override
        public void mousePressed(MouseEvent event) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("IconListener mouseReleased, _treeDnd= {}, popup= {}, source= {}",
                        _treeDnd, e.isPopupTrigger(), e.getSource().getClass().getName());
            }
           if (_treeDnd && e.isPopupTrigger()) {
               if (e.getSource() instanceof IconDisplayPanel) {
                   IconDisplayPanel panel = (IconDisplayPanel)e.getSource();
                   setSelection(panel);
                   NamedIcon icon = panel.getIcon();
                   showPopUp(e, icon);
               } else if (e.getSource() instanceof JLabel) {
                   JLabel label = (JLabel)e.getSource();
                   NamedIcon icon = (NamedIcon)label.getIcon();
                   if (icon !=null) {
                       showPopUp(e, icon);
                  }
               }
            }
        }
        @Override
        public void mouseEntered(MouseEvent event) {
        }
        @Override
        public void mouseExited(MouseEvent event) {
        }
    }

    public static CatalogPanel makeDefaultCatalog() {
        // log.debug("CatalogPanel catalog requested");
        return makeDefaultCatalog(true, false, true); // deactivate dragNdrop? (true, true, false)
    }

    public static CatalogPanel makeDefaultCatalog(boolean addButtonPanel, boolean treeDrop, boolean dragIcon) {
        CatalogPanel catalog = new CatalogPanel("catalogs", "selectNode", addButtonPanel);
        catalog.init(treeDrop, dragIcon);
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        for (CatalogTree tree : manager.getNamedBeanSet()) {
            String systemName = tree.getSystemName();
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
     * @return String       a formatted number
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
        sb.append('.');
        num /= 10;
        while (num > dx) {
            sb.append('0');
            num /= 10;
        }
        sb.append(dx);
        return sb.toString();
    }

    protected void setSelectedNode(CatalogTreeNode node) {
        _dTree.setExpandsSelectedPaths(true);
        if (log.isDebugEnabled()) {
            log.debug("setSelectedNode node: {}", node.toString());
        }
        if (node != null) {
            _dTree.setSelectionPath(new TreePath(node.getPath()));            
        } else {
            _dTree.setSelectionRow(0);
        }
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
            TreePath path = _dTree.getSelectionPath();
            if (log.isDebugEnabled()) {
                log.debug("getSelectedNode TreePath: {}, lastComponent= {}", path.toString(), path.getLastPathComponent().toString());
            }
            return (CatalogTreeNode) path.getLastPathComponent();
        }
        return null;
    }

    private void delete(NamedIcon icon) {
        CatalogTreeNode node = getSelectedNode();
        if (node == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("delete icon {} from node {}", icon.getName(), node.toString());
        }
        node.deleteLeaf(icon.getName(), icon.getURL());
        _model.nodeChanged(node);
        updatePanel();
        InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
    }

    private void rename(NamedIcon icon) {
        CatalogTreeNode node = getSelectedNode();
        if (node == null) {
            return;
        }
        String name = JOptionPane.showInputDialog(getParentFrame(this),
                Bundle.getMessage("newIconName"), icon.getName(),
                JOptionPane.QUESTION_MESSAGE);
        if (name != null && name.length() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("rename icon {} to {} from node {}", icon.getName(), name, node.toString());
            }
            CatalogTreeLeaf leaf = node.getLeaf(icon.getName(), icon.getURL());
            if (leaf != null) {
                leaf.setName(name);
            }
            /* Repaint of panel doesn't happen with these calls 
            _model.nodeChanged(node);
            updatePanel();*/
            TreePath path = _dTree.getSelectionPath();
            // deselect to refresh panel
            _dTree.setSelectionPath(null);
            _dTree.setSelectionPath(path);
            InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
        }
    }

    private void showPopUp(MouseEvent evt, NamedIcon icon) {
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
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    class DropOnPanelToNode extends TransferHandler {
        
        DataFlavor dataFlavor;
        
        DropOnPanelToNode() {
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.warn("DropOnPanelToNode Unable to create data flavor", cnfe);
            }
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDataFlavorSupported(dataFlavor)) {
                return false;
            }
            support.setDropAction(COPY);
            return true;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            CatalogTreeNode node = getSelectedNode();
            if (node == null) {
                return false;
            }
            try {
                Transferable t = support.getTransferable();
                NamedIcon icon = (NamedIcon) t.getTransferData(dataFlavor);
                addLeaf(node, icon);
                if (log.isDebugEnabled()) {
                    log.debug("DropOnPanelToNode.drop COMPLETED for {} into {}", icon.getURL(), node.toString());
                }
                return true;
            } catch (IOException | UnsupportedFlavorException ex) {
                log.warn("DropOnPanelToNode unable to drag and drop", ex);
            }
            return false;
        }
    }
    
    class DropJTree extends JTree implements DropTargetListener {

        DataFlavor dataFlavor;

        DropJTree(TreeModel model) {
            super(model);
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.warn("DropJTree Unable to create data flavor", cnfe);
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            // log.debug("DropJTree ctor");
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            // log.debug("DropJTree.dragExit");
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            // log.debug("DropJTree.dragEnter");
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // log.debug("DropJTree.dragOver");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // log.debug("DropJTree.dropActionChanged");
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon icon = (NamedIcon) tr.getTransferData(dataFlavor);
                    Point pt = e.getLocation();
                    TreePath path = _dTree.getPathForLocation(pt.x, pt.y);
                    if (path != null) {
                        CatalogTreeNode node = (CatalogTreeNode) path.getLastPathComponent();
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        addLeaf(node, icon);
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) {
                            log.debug("DropJTree.drop COMPLETED for {} into {}", icon.getURL(), node.toString());
                        }
                        return;
                    }
                }
            } catch (IOException | UnsupportedFlavorException ex) {
                log.warn("DropJTree unable to drag and drop", ex);
            }
            log.debug("DropJTree.drop REJECTED!");
            e.rejectDrop();
        }
    }

    public class IconDisplayPanel extends JPanel implements MouseListener{
        String _name;
        NamedIcon _icon;

        public IconDisplayPanel(String leafName, NamedIcon icon) {
            super();
            _name = leafName;
            _icon = icon;
            // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setLayout(new BorderLayout());
            setOpaque(false);
            if (_name != null) {
                setBorderAndIcon(icon);
            }
            addMouseListener(new IconListener());
        }
        
        NamedIcon getIcon() {
            return _icon;
        }

        void setBorderAndIcon(NamedIcon icon) {
            if (icon == null) {
                log.error("IconDisplayPanel: No icon for \"{}\"", _name);
                return;
            }
            try {
                JLabel image;
                if (_dragIcons) {
                    image = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime));
                } else {
                    image = new JLabel();
                }
                image.setOpaque(false);
                image.setName(_name);
                image.setToolTipText(icon.getName());
                double scale; 
                if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                    image.setText(Bundle.getMessage("invisibleIcon"));
                    image.setForeground(Color.lightGray);
                    scale = 0;
                } else {
                    scale = icon.reduceTo(ICON_WIDTH, ICON_HEIGHT, ICON_SCALE);
                }
                image.setIcon(icon);
                image.setHorizontalAlignment(JLabel.CENTER);
                image.addMouseListener(new IconListener());
                add(image, BorderLayout.NORTH);
                
                String scaleMessage = Bundle.getMessage("scale", CatalogPanel.printDbl(scale, 2));
                JLabel label = new JLabel(scaleMessage);
                label.setOpaque(false);
                label.setHorizontalAlignment(JLabel.CENTER);
                add(label, BorderLayout.CENTER);
                label = new JLabel(_name);
                label.setOpaque(false);
                label.setHorizontalAlignment(JLabel.CENTER);
//                label.addMouseListener(new IconListener());
                add(label, BorderLayout.SOUTH);
                setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            } catch (java.lang.ClassNotFoundException cnfe) {
                log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            }
        }

        public String getIconName() {
            return _name;
        }
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getSource() instanceof JLabel ) {
                setSelection(this);
            }
        }
        @Override
        public void mousePressed(MouseEvent event) {
        }
        @Override
        public void mouseReleased(MouseEvent event) {
        }
        @Override
        public void mouseEntered(MouseEvent event) {
        }
        @Override
        public void mouseExited(MouseEvent event) {
        }
    }
    

    private final static Logger log = LoggerFactory.getLogger(CatalogPanel.class);

}
