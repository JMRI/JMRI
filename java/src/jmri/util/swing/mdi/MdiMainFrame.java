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
 * @version $Revision$
 */

public class MdiMainFrame extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     */
    public MdiMainFrame(String name, String treeFile, String menubarFile, String toolbarFile) {
        super(name);
        configureFrame(treeFile);
        addMainMenuBar(menubarFile);
        addMainToolBar(toolbarFile);
        pack();
    }
    
    JSplitPane leftRightSplitPane;

    JDesktopPane    desktop;

    JmriJInternalFrameInterface rightWI;
    
    protected void configureFrame(String treeFile) {
        desktop = new JDesktopPane();                
        desktop.setBorder(BorderFactory.createLineBorder(Color.black));
        
        leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeLeftTree(treeFile), desktop);
        leftRightSplitPane.setOneTouchExpandable(true);
        leftRightSplitPane.setResizeWeight(0.0);  // emphasize right part
        
        add(leftRightSplitPane, BorderLayout.CENTER);
    }
        
    protected JScrollPane makeLeftTree(String treeFile) {
        final JTree tree;
        TreeNode topNode;
        
        rightWI = new JmriJInternalFrameInterface(this, desktop);
        
        topNode = JTreeUtil.loadTree(treeFile, rightWI, null);  // no context object
        
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
        
    protected void addMainMenuBar(String menuFile) {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu[] menus = JMenuUtil.loadMenu(menuFile, rightWI, null); // no central context
        for (JMenu j : menus) 
            menuBar.add(j);

        setJMenuBar(menuBar);
    }

    protected void addMainToolBar(String toolBarFile) {
          
        JToolBar toolBar = JToolBarUtil.loadToolBar(toolBarFile, rightWI, null);  // no context

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