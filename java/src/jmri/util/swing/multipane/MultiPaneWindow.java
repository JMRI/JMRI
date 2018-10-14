package jmri.util.swing.multipane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import jmri.util.swing.JMenuUtil;
import jmri.util.swing.JToolBarUtil;
import jmri.util.swing.JTreeUtil;

/**
 * Core JMRI multi-pane GUI window.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
public class MultiPaneWindow extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     *
     * @param name        the name and title of the window
     * @param treeFile path to the XML file for the navigation tree
     * @param menubarFile path to the XML file for the menubar
     * @param toolbarFile path to the XML file for the toolbar
     */
    public MultiPaneWindow(String name, String treeFile, String menubarFile, String toolbarFile) {
        super(name);
        buildGUI(treeFile, menubarFile, toolbarFile);
        pack();
    }

    JSplitPane leftRightSplitPane;

    JPanel left = new JPanel();

    JSplitPane rightUpDownSplitPane;
    JPanel rightTop = new JPanel();
    JPanel rightBottom = new JPanel();

    public JComponent getLowerRight() {
        return rightBottom;
    }

    public JComponent getUpperRight() {
        return rightTop;
    }

    PanedInterface rightTopWI;

    protected void buildGUI(String treeFile, String menubarFile, String toolbarFile) {
        configureFrame();
        configureNavTreePane(treeFile);
        addMainMenuBar(menubarFile);
        addMainToolBar(toolbarFile);
    }

    protected void configureFrame() {

        rightTop.setBorder(BorderFactory.createLineBorder(Color.black));
        rightTop.setLayout(new FlowLayout());            // new BoxLayout(rightTop, BoxLayout.Y_AXIS));

        rightBottom.setLayout(new FlowLayout());            // new BoxLayout(rightBottom, BoxLayout.Y_AXIS));

        rightUpDownSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTop, rightBottom);
        rightUpDownSplitPane.setOneTouchExpandable(true);
        rightUpDownSplitPane.setResizeWeight(1.0);  // emphasize top part

        leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JPanel(), // placeholder
                rightUpDownSplitPane);
        leftRightSplitPane.setOneTouchExpandable(true);
        leftRightSplitPane.setResizeWeight(0.0);  // emphasize right part

        add(leftRightSplitPane, BorderLayout.CENTER);
    }

    protected void configureNavTreePane(String treeFile) {
        leftRightSplitPane.setLeftComponent(makeNavTreePane(treeFile));
    }

    protected JScrollPane makeNavTreePane(String treeFile) {
        final JTree tree;
        TreeNode topNode;

        rightTopWI = new PanedInterface(this);

        topNode = makeNavTreeTopNode(treeFile, rightTopWI);

        tree = new JTree(topNode);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);  // allow multiple roots

        // install listener
        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) {
                    return; //Nothing is selected.	
                }
                if (node.getUserObject() == null) {
                    return; // Not an interesting node
                }
                if (node.getUserObject() instanceof AbstractAction) {
                    AbstractAction action = (AbstractAction) node.getUserObject();
                    action.actionPerformed(null);
                }
            }
        });
        // install in scroll area
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setMinimumSize(new Dimension(0, 0));
        treeView.setPreferredSize(new Dimension(150, 600));
        return treeView;
    }

    protected TreeNode makeNavTreeTopNode(String treeFile, PanedInterface rightTopWI) {
        return JTreeUtil.loadTree(treeFile, rightTopWI, null);  // no context
    }

    public void resetRightToPreferredSizes() {
        rightUpDownSplitPane.resetToPreferredSizes();
    }

    protected void addMainMenuBar(String menuFile) {
        JMenuBar menuBar = new JMenuBar();

        JMenu[] menus = JMenuUtil.loadMenu(menuFile, rightTopWI, null);
        for (JMenu j : menus) {
            menuBar.add(j);
        }

        setJMenuBar(menuBar);
    }

    protected void addMainToolBar(String toolBarFile) {

        JToolBar toolBar = JToolBarUtil.loadToolBar(toolBarFile, rightTopWI, null);

        // this takes up space at the top until pulled to floating
        add(toolBar, BorderLayout.NORTH);
    }

    /**
     * Only close frame, etc, dispose() disposes of all cached panes
     */
    @Override
    public void dispose() {
        rightTopWI.dispose();
        super.dispose();
    }

}
