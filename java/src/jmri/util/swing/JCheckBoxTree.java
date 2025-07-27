package jmri.util.swing;

import java.beans.PropertyChangeListener;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeSelectionModel;

import jmri.util.ThreadingUtil;

/**
 * A JCheckBox Tree adds checkboxes to each node of the tree.
 * Clicks on the checkboxes can toggle further checkboxes further down the node.
 * TriStateJCheckBox is used to render the partial state.
 * Inspired by https://stackoverflow.com/questions/21847411/java-swing-need-a-good-quality-developed-jtree-with-checkboxes
 * @author Steve Young Copyright (C) 2025
 */
public class JCheckBoxTree extends JTree {

    private transient HashMap<TreePath, CheckedNode> nodesCheckingState;
    private HashSet<TreePath> checkedPaths;
    public static final String PROPERTY_CHANGE_CHECKBOX_STATUS = "checkBoxesChanged";

    public JCheckBoxTree() {
        super();
        
        // Disabling toggling by double-click
        setToggleClickCount(0);

        setCellRenderer(new JCheckBoxTreeCellRenderer());

        // Overriding selection model by an empty one
        DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel() {      

            // Totally disabling the selection mechanism
            @Override
            public void setSelectionPath(TreePath path) {
            }           
            @Override
            public void addSelectionPath(TreePath path) {                       
            }           
            @Override
            public void removeSelectionPath(TreePath path) {
            }
            @Override
            public void setSelectionPaths(TreePath[] pPaths) {
            }
        };
        // Calling checking mechanism on mouse click
        this.addMouseListener(JmriMouseListener.adapt(new TreePathClickListener()));
        this.setSelectionModel(dtsm);
        ToolTipManager.sharedInstance().registerComponent(JCheckBoxTree.this);
    }

    // helper method to convert to JmriMouseEvent
    @Override
    public String getToolTipText( java.awt.event.MouseEvent ev) {
        JmriMouseEvent e = new JmriMouseEvent(ev);
        return getToolTipText(e);
    }

    // overriding classes use JmriMouseEvent
    public String getToolTipText( JmriMouseEvent ev) {
        return null;
    }

    /**
     * Click on a TreePath.
     * @param tp the TreePath clicked on, may be null.
     * @param updateListeners true to update listeners, else false.
     */
    public void treePathClicked( @CheckForNull TreePath tp, boolean updateListeners) {
        if (tp == null) {
            return;
        }
        boolean checkMode = ! nodesCheckingState.get(tp).isSelected();
        checkSubTree(tp, checkMode);
        updatePredecessorsWithCheckMode(tp, checkMode);

        if ( updateListeners ) {

            // Firing the check change event
            fireCheckChangeEvent();
            // Repainting tree after the data structures were updated
            ThreadingUtil.runOnGUI(this::repaint);
        }
    }

    private void fireCheckChangeEvent() {
        for ( PropertyChangeListener pcl : getPropertyChangeListeners() ) {
            pcl.propertyChange(new java.beans.PropertyChangeEvent(this, PROPERTY_CHANGE_CHECKBOX_STATUS, false, true));
        }
    }

    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        resetCheckingState();
    }

    /**
     * Get a List of Paths which are currently checked.
     * Does not include partially checked Paths.
     * @return array of paths.
     */
    @Nonnull
    public List<TreePath> getCheckedPaths() {
        List<TreePath> returnList = new ArrayList<>();
        if ( checkedPaths != null ) {
            for ( TreePath tp : checkedPaths ) {
                if ( ! isSelectedPartially(tp) ) {
                    returnList.add(tp);
                }

            }
        }
        return returnList;
    }

    /**
     * Check if a TreePath is Selected.
     * @param path the TreePath to check.
     * @return true if selected, else false.
     */
    public boolean isSelected(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return cn != null && cn.isSelected();
    }

    /**
     * Check if a Path is partially selected.
     * The node is selected, has children but not all of them are selected.
     * @param path the Path to check.
     * @return true if partially selected, else false.
     */
    public boolean isSelectedPartially(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return cn != null && cn.isSelected() && cn.hasChildren && !cn.allChildrenSelected;
    }

    /**
     * Check if a treePath has children.
     * @param path the TreePath to check for
     * @return true if the TreePath has children, else false.
     */
    public boolean hasChildren(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return cn != null && cn.hasChildren;
    }

    /**
     * Reset the Check-boxes.
     * Call if the model has a TreeNode added or removed.
     */
    public void resetCheckingState() {

        var currentPaths = this.getCheckedPaths();

        nodesCheckingState = new HashMap<>();
        checkedPaths = new HashSet<>();
        javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode)getModel().getRoot();
        if (node == null) {
            return;
        }
        addSubtreeToCheckingStateTracking(node);

        for ( TreePath selPath : currentPaths ) {
            if (!( hasChildren(selPath)) ) {
                this.treePathClicked(selPath, false);
            }
        }
        fireCheckChangeEvent();
        ThreadingUtil.runOnGUI(this::updateUI);
        
    }

    // Creating data structure of the current model for the checking mechanism
    private void addSubtreeToCheckingStateTracking(@Nonnull javax.swing.tree.DefaultMutableTreeNode node) {
        var path = node.getPath();
        TreePath tp = new TreePath(path);
        CheckedNode cn = new CheckedNode( false, node.getChildCount() > 0, false);
        nodesCheckingState.put(tp, cn);
        for (int i = 0 ; i < node.getChildCount() ; i++) {              
            addSubtreeToCheckingStateTracking((javax.swing.tree.DefaultMutableTreeNode)
                tp.pathByAddingChild(node.getChildAt(i)).getLastPathComponent());
        }
    }

    // When a node is checked/unchecked, updating the states of the predecessors
    private void updatePredecessorsWithCheckMode(@Nonnull TreePath tp, boolean check) {
        TreePath parentPath = tp.getParentPath();
        // If it is the root, stop the recursive calls and return
        if (parentPath == null) {
            return;
        }
        CheckedNode parentCheckedNode = nodesCheckingState.get(parentPath);
        javax.swing.tree.DefaultMutableTreeNode parentNode =
            (javax.swing.tree.DefaultMutableTreeNode) parentPath.getLastPathComponent();
        parentCheckedNode.allChildrenSelected = true;
        parentCheckedNode.setSelected(false);
        for (int i = 0 ; i < parentNode.getChildCount() ; i++) {                
            TreePath childPath = parentPath.pathByAddingChild(parentNode.getChildAt(i));
            CheckedNode childCheckedNode = nodesCheckingState.get(childPath);           
            // It is enough that even one subtree is not fully selected
            // to determine that the parent is not fully selected
            if (! childCheckedNode.allChildrenSelected) {
                parentCheckedNode.allChildrenSelected = false;      
            }
            // If at least one child is selected, selecting also the parent
            if (childCheckedNode.isSelected()) {
                parentCheckedNode.setSelected(true);
            }
        }
        if (parentCheckedNode.isSelected()) {
            checkedPaths.add(parentPath);
        } else {
            checkedPaths.remove(parentPath);
        }
        // Go to upper predecessor
        updatePredecessorsWithCheckMode(parentPath, check);
    }

    // Recursively checks/unchecks a subtree
    private void checkSubTree( @Nonnull TreePath tp, boolean check) {
        CheckedNode cn = nodesCheckingState.get(tp);
        cn.setSelected(check);
        javax.swing.tree.DefaultMutableTreeNode node =
            (javax.swing.tree.DefaultMutableTreeNode) tp.getLastPathComponent();
        for (int i = 0 ; i < node.getChildCount() ; i++) {              
            checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
        }
        cn.allChildrenSelected = check;
        if (check) {
            checkedPaths.add(tp);
        } else {
            checkedPaths.remove(tp);
        }
    }

    // Defining data structure that will enable to fast check-indicate the state of each node
    // It totally replaces the "selection" mechanism of the JTree
    private static class CheckedNode {
        private boolean isSelected;
        boolean hasChildren;
        boolean allChildrenSelected;

        CheckedNode(boolean isSelected, boolean hasChildren, boolean allChildrenSelected) {
            this.isSelected = isSelected;
            this.hasChildren = hasChildren;
            this.allChildrenSelected = allChildrenSelected;
        }

        boolean isSelected() {
            return isSelected;
        }

        void setSelected( boolean newVal) {
            isSelected = newVal;
        }

    }

    private class TreePathClickListener implements JmriMouseListener {

        @Override
        public void mouseClicked(JmriMouseEvent e) {
            treePathClicked(getPathForLocation(e.getX(), e.getY()), true);
        }

        @Override
        public void mousePressed(JmriMouseEvent e) {
        }

        @Override
        public void mouseReleased(JmriMouseEvent e) {
        }

        @Override
        public void mouseEntered(JmriMouseEvent e) {
        }

        @Override
        public void mouseExited(JmriMouseEvent e) {
        }

    }

}
