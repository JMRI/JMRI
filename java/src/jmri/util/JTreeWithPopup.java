package jmri.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * JTree subclass that supports a popup menu.
 * <p>
 * From the
 * <A HREF="http://www.java-tips.org/java-se-tips/javax.swing/have-a-popup-attached-to-a-jtree.html">Java
 * Tips</a> web site.
 */
class JTreeWithPopup extends JTree implements ActionListener {

    JPopupMenu popup;
    JMenuItem mi;

    JTreeWithPopup(DefaultMutableTreeNode dmtn) {
        super(dmtn);
        // define the popup
        popup = new JPopupMenu();
        mi = new JMenuItem("Insert a children");
        mi.addActionListener(this);
        mi.setActionCommand("insert");
        popup.add(mi);
        mi = new JMenuItem("Remove this node");
        mi.addActionListener(this);
        mi.setActionCommand("remove");
        popup.add(mi);
        popup.setOpaque(true);
        popup.setLightWeightPopupEnabled(true);

        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                        }
                    }
                }
        );

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        DefaultMutableTreeNode dmtn, node;

        TreePath path = this.getSelectionPath();
        dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (ae.getActionCommand().equals("insert")) {
            node = new DefaultMutableTreeNode("children");
            dmtn.add(node);
            // thanks to Yong Zhang for the tip for refreshing the tree structure.
            ((DefaultTreeModel) this.getModel()).nodeStructureChanged(dmtn);
        }
        if (ae.getActionCommand().equals("remove")) {
            node = (DefaultMutableTreeNode) dmtn.getParent();
            // Bug fix by essam
            int nodeIndex = node.getIndex(dmtn); // declare an integer to hold the selected nodes index
            dmtn.removeAllChildren();          // remove any children of selected node
            node.remove(nodeIndex);            // remove the selected node, retain its siblings
            ((DefaultTreeModel) this.getModel()).nodeStructureChanged(dmtn);
        }
    }
}
