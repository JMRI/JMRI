package jmri.jmrit.catalog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JPanel containing trees of resources to replace default icons. The
 * panel also displays image files files contained in a node of a tree. Drag and
 * Drop is implemented to drag a display of an icon to the display of an icon
 * that may be added to the panel.
 * <P>
 * This panel is used in the Icon Editors and also in the ImageIndex Editor.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Pete Cressman Copyright 2009
 *
 */
public class CatalogPanel extends JPanel implements MouseListener {

    public static final double ICON_SCALE = 0.15;
    public static final int ICON_WIDTH = 100;
    public static final int ICON_HEIGHT = 100;

    JPanel _selectedImage;
    static Color _grayColor = new Color(235, 235, 235);
    protected Color _currentBackground = _grayColor;

    JLabel _previewLabel = new JLabel(" ");
    protected JPanel _preview;
    boolean _noDrag;

    JScrollPane _treePane;
    JTree _dTree;
    DefaultTreeModel _model;
    ArrayList<CatalogTree> _branchModel = new ArrayList<CatalogTree>();

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
//        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, p1, makePreviewPanel());
//        sp.setResizeWeight(0.2);
//        add(sp);
        add(p);
        add(makeButtonPanel());
    }

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
        if (treeDnD) {   // index editor (right pane)
            _dTree = new DropJTree(_model);
            _noDrag = true;
        } else {    // Catalog (left pane index editor or all icon editors)
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

        _dTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                updatePanel();
            }
        });
        _dTree.setExpandsSelectedPaths(true);
        _treePane.setViewportView(_dTree);
//        setupPanel();
    }

    public void updatePanel() {
        if (log.isDebugEnabled()) {
            log.debug("updatePanel: _dTree.isSelectionEmpty()= " + _dTree.isSelectionEmpty()
                    + ", _dTree.getSelectionPath() is null " + (_dTree.getSelectionPath() == null));
        }
        if (!_dTree.isSelectionEmpty() && _dTree.getSelectionPath() != null) {
            try {
                _previewLabel.setText(setIcons());
            } catch (OutOfMemoryError oome) {
                resetPanel();
                if (log.isDebugEnabled()) {
                    log.debug("setIcons threw OutOfMemoryError " + oome);
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
     */
    public void createNewBranch(String systemName, String userName, String path) {

        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        CatalogTree tree = manager.getBySystemName(systemName);
        if (tree != null) {
            jmri.util.ThreadingUtil.runOnGUI(()->{
                addTree(tree);
            });
        } else {
            final CatalogTree t = manager.newCatalogTree(systemName, userName);
            jmri.util.ThreadingUtil.runOnGUI(()->{
                t.insertNodes(path);
                addTree(t);
            });
        }
    }

    /**
     * Extend the Catalog by adding a tree to the root.
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
     * Recursively add the branch nodes to display tree
     */
    @SuppressWarnings("unchecked")
    private void addTreeBranch(CatalogTreeNode node) {
        if (log.isDebugEnabled()) {
            log.debug("addTreeBranch called for node= " + node.toString()
                    + ", has " + node.getChildCount() + " children");
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
     * Find the corresponding CatalogTreeManager tree to the displayed branch
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
        ImageIndexEditor.indexChanged(true);
        return true;
    }

    /**
     * Delete a node from the displayed tree.
     */
    public void removeNodeFromModel(CatalogTreeNode node) {
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(node);
        tree.removeNodeFromParent(getCorrespondingNode(node));
        _model.removeNodeFromParent(node);
        ImageIndexEditor.indexChanged(true);
    }

    /**
     * Make a change to a node in the displayed tree. Either its name or the
     * contents of its leaves (image references)
     */
    public boolean nodeChange(CatalogTreeNode node, String name) {
        CatalogTreeNode cNode = getCorrespondingNode(node);
        cNode.setLeaves(node.getLeaves());
        AbstractCatalogTree tree = (AbstractCatalogTree) getCorespondingModel(node);

        cNode.setUserObject(name);
        node.setUserObject(name);
        tree.nodeChanged(cNode);
        _model.nodeChanged(node);
        updatePanel();
        ImageIndexEditor.indexChanged(true);
        updatePanel();
        return true;
    }

    /**
     * Node names in the path to the root must be unique
     */
    private boolean nameOK(CatalogTreeNode node, String name) {
        TreeNode[] nodes = node.getPath();
        for (int i = 0; i < nodes.length; i++) {
            if (name.equals(nodes[i].toString())) {
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
     * Setup a display pane for a tree that shows only directory nodes (no file
     * leaves) The leaves (icon images) will be displayed in this panel.
     */
    private JPanel makePreviewPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.add(_previewLabel);
        _preview = new JPanel();
        JScrollPane js = new JScrollPane(_preview);
        previewPanel.add(js);
        return previewPanel;
    }

    private JPanel makeButtonPanel() {
        JRadioButton whiteButton = new JRadioButton(Bundle.getMessage("white"), false);
        JRadioButton grayButton = new JRadioButton(Bundle.getMessage("lightGray"), true);
        JRadioButton darkButton = new JRadioButton(Bundle.getMessage("darkGray"), false);
        whiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _currentBackground = Color.white;
                setBackground(_preview);
            }
        });
        grayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _currentBackground = _grayColor;
                setBackground(_preview);
            }
        });
        darkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _currentBackground = new Color(150, 150, 150);
                setBackground(_preview);
            }
        });
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        JPanel pp = new JPanel();
        pp.add(new JLabel(Bundle.getMessage("setBackground")));
        backgroundPanel.add(pp);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        ButtonGroup selGroup = new ButtonGroup();
        selGroup.add(whiteButton);
        selGroup.add(grayButton);
        selGroup.add(darkButton);
        buttonPanel.add(whiteButton);
        buttonPanel.add(grayButton);
        buttonPanel.add(darkButton);
        backgroundPanel.add(buttonPanel);
        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
        return backgroundPanel;
    }

    public void setBackground(Container container) {
        container.setBackground(_currentBackground);
        Component[] comp = container.getComponents();
        for (int i = 0; i < comp.length; i++) {
            comp[i].setBackground(_currentBackground);
            if (comp[i] instanceof java.awt.Container) {
                setBackground((Container) comp[i]);
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
        for (int i = 0; i < comp.length; i++) {
            comp[i].removeMouseListener(this);
        }
        _preview.removeAll();
        setBackground(_preview);
        _preview.repaint();
    }

    public class MemoryExceptionHandler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            _noMemory = true;
            log.error("Exception from setIcons: " + e, e);
        }
    }

    protected boolean _noMemory = false;

    /**
     * Display the icons in the preview panel
     */
    protected String setIcons() throws OutOfMemoryError {
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
            } else if (c.gridy < numRow) { //start next row
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

//            JLabel image = null;
            JLabel nameLabel = null;
            if (_noDrag) {
//                image = new JLabel();
                nameLabel = new JLabel(leaf.getName());
            } else {
                try {
//                    image = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime));
                    nameLabel = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime));
                } catch (java.lang.ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                    continue;
                }
            }
//            image.setOpaque(true);
//            image.setName(leaf.getName());
//            image.setBackground(_currentBackground);
//            image.setIcon(icon);
            nameLabel.setText(leaf.getName());
            nameLabel.setName(leaf.getName());
            nameLabel.setBackground(_currentBackground);
            nameLabel.setIcon(icon);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//            JPanel iPanel = new JPanel();
//            iPanel.add(image);
//            p.add(iPanel);
            p.add(nameLabel);
            JLabel label = new JLabel(java.text.MessageFormat.format(Bundle.getMessage("scale"),
                    new Object[]{printDbl(scale, 2)}));
            p.add(label);
            if (_noDrag) {
                p.addMouseListener(this);
            }
            gridbag.setConstraints(p, c);
            if (_noMemory) {
                continue;
            }
            _preview.add(p);
            if (log.isDebugEnabled()) {
                log.debug(leaf.getName() + " inserted at (" + c.gridx + ", " + c.gridy
                        + ") w= " + icon.getIconWidth() + ", h= " + icon.getIconHeight());
            }
        }
        c.gridy++;
        c.gridx++;
        JLabel bottom = new JLabel();
        gridbag.setConstraints(bottom, c);
        _preview.add(bottom);

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        return java.text.MessageFormat.format(Bundle.getMessage("numImagesInNode"),
                new Object[]{node.getUserObject(), Integer.valueOf(leaves.size())});
    }

    public static CatalogPanel makeDefaultCatalog() {
        CatalogPanel catalog = new CatalogPanel("catalogs", "selectNode");
        catalog.init(false);
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        List<String> sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i = 0; i < sysNames.size(); i++) {
                String systemName = sysNames.get(i);
                if (systemName.charAt(0) == 'I') {
                    catalog.addTree(manager.getBySystemName(systemName));
                }
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
     * Utility
     */
    public static String printDbl(double z, int decimalPlaces) {
        if (Double.isNaN(z) || decimalPlaces > 8) {
            return Double.toString(z);
        } else if (decimalPlaces <= 0) {
            return Integer.toString((int) Math.rint(z));
        }
        StringBuffer sb = new StringBuffer();
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

    /**
     */
    public void setSelectedNode(String[] names) {
        _dTree.setExpandsSelectedPaths(true);
        CatalogTreeNode[] path = new CatalogTreeNode[names.length];
        for (int i = 0; i < names.length; i++) {
            path[i] = new CatalogTreeNode(names[i]);
        }
        _dTree.setSelectionPath(new TreePath(path));
    }

    /**
     */
    public void scrollPathToVisible(String[] names) {
        _dTree.setExpandsSelectedPaths(true);
        CatalogTreeNode[] path = new CatalogTreeNode[names.length];
        for (int i = 0; i < names.length; i++) {
            path[i] = new CatalogTreeNode(names[i]);
        }
        _dTree.scrollPathToVisible(new TreePath(path));
    }

    /**
     * Return the node the user has selected.
     */
    public CatalogTreeNode getSelectedNode() {
        if (!_dTree.isSelectionEmpty() && _dTree.getSelectionPath() != null) {
            // somebody has been selected
            if (log.isDebugEnabled()) {
                log.debug("getSelectedNode with "
                        + _dTree.getSelectionPath().toString());
            }
            TreePath path = _dTree.getSelectionPath();
            return (CatalogTreeNode) path.getLastPathComponent();
        }
        return null;
    }

    void delete(NamedIcon icon) {
        CatalogTreeNode node = getSelectedNode();
        node.deleteLeaf(icon.getName(), icon.getURL());
        updatePanel();
    }

    void rename(NamedIcon icon) {
        String name = JOptionPane.showInputDialog(getParentFrame(this),
                Bundle.getMessage("newIconName"), icon.getName(),
                JOptionPane.QUESTION_MESSAGE);
        if (name != null && name.length() > 0) {
            CatalogTreeNode node = getSelectedNode();
            CatalogTreeLeaf leaf = node.getLeaf(icon.getName(), icon.getURL());
            if (leaf != null) {
                leaf.setName(name);
            }
            getParentFrame(this).invalidate();
            updatePanel();
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
            log.debug("showPopUp " + icon.toString());
        }
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(icon.getName()));
        popup.add(new JMenuItem(icon.getURL()));
        popup.add(new javax.swing.JPopupMenu.Separator());

        popup.add(new AbstractAction(Bundle.getMessage("RenameIcon")) {
            /**
             *
             */
            private static final long serialVersionUID = 1598669233012552574L;
            NamedIcon icon;

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
            /**
             *
             */
            private static final long serialVersionUID = -2412374302278407718L;
            NamedIcon icon;

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

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Container con = (Container) e.getSource();
            JLabel label = (JLabel) con.getComponent(0);
            NamedIcon icon = (NamedIcon) label.getIcon();
            showPopUp(e, icon);
        }
    }

    class DropJTree extends JTree implements DropTargetListener {

        /**
         *
         */
        private static final long serialVersionUID = -6754039215423513840L;
        DataFlavor dataFlavor;

        DropJTree(TreeModel model) {
            super(model);
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //if (log.isDebugEnabled()) log.debug("DropJTree ctor");
        }

        public void dragExit(DropTargetEvent dte) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragExit ");
        }

        public void dragEnter(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragEnter ");
        }

        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dragOver ");
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJTree.dropActionChanged ");
        }

        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon icon = (NamedIcon) tr.getTransferData(dataFlavor);
                    Point pt = e.getLocation();
                    if (log.isDebugEnabled()) {
                        log.debug("DropJTree.drop: Point= (" + pt.x + ", " + pt.y + ")");
                    }
                    TreePath path = _dTree.getPathForLocation(pt.x, pt.y);
                    if (path != null) {
                        CatalogTreeNode node = (CatalogTreeNode) path.getLastPathComponent();
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        node.addLeaf(icon.getName(), icon.getURL());
                        nodeChange(node, node.toString());
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) {
                            log.debug("DropJTree.drop COMPLETED for " + icon.getURL());
                        }
                        return;
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            }
            if (log.isDebugEnabled()) {
                log.debug("DropJTree.drop REJECTED!");
            }
            e.rejectDrop();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CatalogPanel.class.getName());
}
