package jmri.jmrit.catalog;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.*;

import jmri.CatalogTreeManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JFrame for creating and editing an Image Index.
 *
 * @author Pete Cressman Copyright 2009
 */
public final class ImageIndexEditor extends JmriJFrame {

    private CatalogPanel _catalog;
    private CatalogPanel _index;
    private JTree _indexTree;

    public static final String IconDataFlavorMime = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=jmri.jmrit.catalog.NamedIcon";

    /**
     * Ctor for an unnamed ImageIndexEditor.
     */
    private ImageIndexEditor() {
        super();
    }

    /**
     * Ctor for a named ImageIndexEditor.
     *
     * @param name title to display on the editor frame
     */
    private ImageIndexEditor(String name) {
        super(name);
    }

    private void init() {
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(findIcon);
        JMenuItem storeItem = new JMenuItem(Bundle.getMessage("MIStoreImageIndex"));
        findIcon.add(storeItem);
        storeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                InstanceManager.getDefault(CatalogTreeManager.class).storeImageIndex();
            }
        });

        findIcon.addSeparator();
        JMenuItem openItem = new JMenuItem(Bundle.getMessage("openDirMenu"));
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(DirectorySearcher.class).openDirectory();
            }
        });
        findIcon.add(openItem);

        JMenuItem searchItem = new JMenuItem(Bundle.getMessage("searchFSMenu"));
        searchItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(DirectorySearcher.class).searchFS();
            }
        });
        findIcon.add(searchItem);

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        menuBar.add(editMenu);
        addMenuItems(editMenu, null);
        setJMenuBar(menuBar);

        addHelpMenu("package.jmri.jmrit.catalog.ImageIndex", true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel(Bundle.getMessage("dragIconsInstr"), SwingConstants.LEFT));
        mainPanel.add(labelPanel);
        JPanel catalogsPanel = new JPanel();
        catalogsPanel.setLayout(new BoxLayout(catalogsPanel, BoxLayout.X_AXIS));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                makeCatalogPanel(), makeIndexPanel());
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        catalogsPanel.add(splitPane);
        mainPanel.add(catalogsPanel);
        getContentPane().add(mainPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                InstanceManager.getDefault(DirectorySearcher.class).close();
            }
        });
        setLocation(10, 200);
        pack();
        setVisible(true);
    }

    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations, generics
    private JPanel makeCatalogPanel() {
        _catalog = new CatalogPanel("defaultCatalog", "selectNode", true); // make sure both these properties keys exist
        // log.debug("init the new CatalogPanel for ImageIndexEditor.makeCatalogPanel()");
        _catalog.init(false, true);
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        List<String> sysNames = manager.getSystemNameList();

        for (String systemName : sysNames) {
            if (systemName.startsWith("IF")) {
                _catalog.addTree(manager.getBySystemName(systemName));
                // log.debug("added item to tree");
            }
        }

        _catalog.createNewBranch("IFJAR", "Program Directory", "resources");
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "resources");
        _catalog.createNewBranch("IFPREF", "Preferences Directory", FileUtil.getUserFilesPath() + "resources");
        return _catalog;
    }

    /*
     * Provide node editing.
     *
     * @param evt mouse event providing x and y position in frame
     */
    private void showTreePopUp(MouseEvent evt) {
        int row = _indexTree.getRowForLocation(evt.getX(), evt.getY());
        if (row <= 0) {
            return;
        }
        TreePath path = _indexTree.getPathForLocation(evt.getX(), evt.getY());
        String nodeName = path.getLastPathComponent().toString();
        log.debug("showTreePopUp node= {}", nodeName);
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(nodeName));
        popup.add(new javax.swing.JPopupMenu.Separator());
        
        addMenuItems(popup, (CatalogTreeNode) path.getLastPathComponent());
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void addMenuItems(javax.swing.JComponent editMenu, CatalogTreeNode node) {
        JMenuItem addItem = new JMenuItem(Bundle.getMessage("addNode"));
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNode(node);
            }
        });
        editMenu.add(addItem);
        JMenuItem renameItem = new JMenuItem(Bundle.getMessage("renameNode"));
        renameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renameNode(node);
            }
        });
        editMenu.add(renameItem);
        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("deleteNode"));
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteNode(node);
            }
        });
        editMenu.add(deleteItem);
    }

    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations, generics
    private JPanel makeIndexPanel() {
        _index = new CatalogPanel("ImageIndex", "selectIndexNode", true); // make sure both these properties keys exist
        // log.debug("init the new CatalogPanel for ImageIndexEditor.makeIndexPanel()");
        // activate dragNdrop init(true,true) allows drop into same panel as the drag - i.e. duplicates icon in node
        _index.init(true, false);

        boolean found = false;
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        List<String> sysNames = manager.getSystemNameList();

        for (String systemName : sysNames) {
            if (systemName.startsWith("IX")) {
                _index.addTree(manager.getBySystemName(systemName));
                found = true;
            }
        }

        if (!found) {
            _index.createNewBranch("IXII", Bundle.getMessage("ImageIndexRoot"), "ImageIndexRoot");
        }
        _indexTree = _index.getTree();
        _indexTree.setSelectionRow(0);   // begin with ImageIndexRoot selected
        _indexTree.addMouseListener(new java.awt.event.MouseAdapter () {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || e.isMetaDown() || e.isAltDown()) {
                    showTreePopUp(e);
                }
            }
        });
        return _index;
    }

    void addNode(CatalogTreeNode selectedNode) {
        if (selectedNode == null) {
            selectedNode = _index.getSelectedNode();            
        }
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectAddNode"),
                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, Bundle.getMessage("nameAddNode"),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                if (!_index.insertNodeIntoModel(name, selectedNode)) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("duplicateNodeName", name),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        invalidate();
    }

    private void renameNode(CatalogTreeNode selectedNode) {
        if (selectedNode == null) {
            selectedNode = _index.getSelectedNode();            
        }
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectRenameNode"),
                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, Bundle.getMessage("newNameNode"),
                    selectedNode.getUserObject());
            if (name != null) {
                if (!_index.renameNode(selectedNode, name)) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("duplicateNodeName", name),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }

            }
        }
        invalidate();
    }

    private void deleteNode(CatalogTreeNode selectedNode) {
        if (selectedNode == null) {
            selectedNode = _index.getSelectedNode();            
        }
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectDeleteNode"),
                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("delete node \"{}\" level= {}.", selectedNode, selectedNode.getLevel());
        }
        if (selectedNode.getLevel() <= 1) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("deleteRootNode"),
                    Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
            return;
        } else {
            int numNodes = countSubNodes(selectedNode);
            int numIcons = countIcons(selectedNode);
            int response = JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("confirmDeleteNode", selectedNode.getUserObject(), numNodes, numIcons),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                CatalogTreeNode parent = (CatalogTreeNode)selectedNode.getParent();
                _index.removeNodeFromModel(selectedNode);
                _index.setSelectedNode(parent);
            }
        }
    }

    int countSubNodes(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration<TreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
            cnt += countSubNodes(n) + 1;
        }
        return cnt;
    }

    private int countIcons(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration<TreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n =(CatalogTreeNode)e.nextElement();
            cnt += countIcons(n);
        }
        cnt += node.getNumLeaves();
        return cnt;
    }

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(ImageIndexEditor.class)) {
                ImageIndexEditor instance = new ImageIndexEditor(Bundle.getMessage("editIndexFrame"));
                instance.init();
                return instance;
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(ImageIndexEditor.class);
            return set;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ImageIndexEditor.class);

}
