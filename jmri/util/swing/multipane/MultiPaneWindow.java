// MultiPaneWindow.java

package jmri.util.swing.multipane;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import jmri.util.swing.*;

/**
 * Core JMRI multi-pane GUI window.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.1 $
 */

public class MultiPaneWindow extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     * @param typeName location within the xml/config directory
     *                 for the configuration files
     */
    public MultiPaneWindow(String name, String typeName) {
        super(name);
        configureFrame("config/"+typeName+"/Gui3LeftTree.xml");
        addMainMenuBar("config/"+typeName+"/Gui3Menus.xml");
        addMainToolBar("config/"+typeName+"/Gui3MainToolBar.xml");
        pack();
    }
    
    JSplitPane leftRightSplitPane;

    JPanel          left = new JPanel();
    
    JSplitPane      rightUpDownSplitPane;
    JPanel          rightTop = new JPanel();
    JPanel          rightBottom = new JPanel();

    public JComponent getLowerRight() {
        return rightBottom;
    }
    public JComponent getUpperRight() {
        return rightTop;
    }
    
    PanedInterface rightTopWI;
    
    protected void configureFrame(String treeFileName) {
        
        left.add(new JLabel("Left"));
                
        rightTop.setBorder(BorderFactory.createLineBorder(Color.black));
        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.Y_AXIS));
        rightTop.add(new JLabel("(Need some default content here)"));
        
        rightBottom.setLayout(new BoxLayout(rightBottom, BoxLayout.Y_AXIS));
        rightBottom.add(new JLabel("(Need some default content here)"));

        rightUpDownSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTop, rightBottom);
        rightUpDownSplitPane.setOneTouchExpandable(true);
        rightUpDownSplitPane.setResizeWeight(1.0);  // emphasize top part
        
        leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeLeftTree(treeFileName), rightUpDownSplitPane);
        leftRightSplitPane.setOneTouchExpandable(true);
        leftRightSplitPane.setResizeWeight(0.0);  // emphasize right part
        
        add(leftRightSplitPane, BorderLayout.CENTER);
    }
        
    protected JScrollPane makeLeftTree(String treeFileName) {
        final JTree tree;
        TreeNode topNode;
        
        rightTopWI = new PanedInterface(this);
        
        topNode = JTreeUtil.loadTree(treeFileName, rightTopWI);
        
        tree = new JTree(topNode);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);  // allow multiple roots
        
        // install listener
        tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                       tree.getLastSelectedPathComponent();
                if (node == null)
                    return; //Nothing is selected.	

                if (node.getUserObject() == null)
                    return; // Not an interesting node
                    
                if (node.getUserObject() instanceof AbstractAction) {
                    AbstractAction action = (AbstractAction)node.getUserObject();
                    action.actionPerformed(null);
                }
            }
        });
        // install in scroll area
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setMinimumSize(new Dimension(0,0));
        treeView.setPreferredSize(new Dimension(150,600));
        return treeView;
    }
    
    public void resetRightToPreferredSizes() { rightUpDownSplitPane.resetToPreferredSizes(); }
    
    protected void addMainMenuBar(String menuFileName) {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu[] menus = JMenuUtil.loadMenu(menuFileName, rightTopWI);
        for (JMenu j : menus) 
            menuBar.add(j);

        setJMenuBar(menuBar);
    }

    protected void addMainToolBar(String toolBarFileName) {
          
        JToolBar toolBar = JToolBarUtil.loadToolBar(toolBarFileName, rightTopWI);

        // this takes up space at the top until pulled to floating
        add(toolBar, BorderLayout.NORTH);
    }
    
    /**
     * Only close frame, etc, dispose() disposes of all 
     * cached panes
     */
    public void dispose() {
        rightTopWI.dispose();
        super.dispose();
    }
    
    /**
     * Set a toolbar to be initially floating.
     * This doesn't quite work right.
     */
    protected void setFloating(JToolBar toolBar) {
        //((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloatingLocation(100,100);
        ((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloating(true, new Point(500,500));
    }

}