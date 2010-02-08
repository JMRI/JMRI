// MdiMainFrame.java

package jmri.util.swing.mdi;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.*;

import jmri.util.swing.*;

/**
 * Core JMRI JInternalPane GUI window.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.1 $
 */

public class MdiMainFrame extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     * @param typeName location within the xml/config directory
     *                 for the configuration files
     */
    public MdiMainFrame(String name, String typeName) {
        super(name);
        configureFrame("config/"+typeName+"/Gui3LeftTree.xml");
        addMainMenuBar("config/"+typeName+"/Gui3Menus.xml");
        addMainToolBar("config/"+typeName+"/Gui3MainToolBar.xml");
        pack();
    }
    
    JSplitPane leftRightSplitPane;

    JDesktopPane    desktop;

    JmriJInternalFrameInterface rightWI;
    
    protected void configureFrame(String treeFileName) {
        desktop = new JDesktopPane();                
        desktop.setBorder(BorderFactory.createLineBorder(Color.black));
        
        leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeLeftTree(treeFileName), desktop);
        leftRightSplitPane.setOneTouchExpandable(true);
        leftRightSplitPane.setResizeWeight(0.0);  // emphasize right part
        
        add(leftRightSplitPane, BorderLayout.CENTER);
    }
        
    protected JScrollPane makeLeftTree(String treeFileName) {
        final JTree tree;
        TreeNode topNode;
        
        rightWI = new JmriJInternalFrameInterface(this, desktop);
        
        topNode = JTreeUtil.loadTree(treeFileName, rightWI);
        
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
        
    protected void addMainMenuBar(String menuFileName) {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu[] menus = JMenuUtil.loadMenu(menuFileName, rightWI);
        for (JMenu j : menus) 
            menuBar.add(j);

        setJMenuBar(menuBar);
    }

    protected void addMainToolBar(String toolBarFileName) {
          
        JToolBar toolBar = JToolBarUtil.loadToolBar(toolBarFileName, rightWI);

        // this takes up space at the top until pulled to floating
        add(toolBar, BorderLayout.NORTH);
    }
    
    /**
     * Only close frame, etc, dispose() disposes of all 
     * cached panes
     */
    public void dispose() {
        rightWI.dispose();
        super.dispose();
    }
    
}