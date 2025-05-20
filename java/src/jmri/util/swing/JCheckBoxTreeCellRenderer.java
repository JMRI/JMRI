package jmri.util.swing;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * JCheckBoxTreeCellRenderer implements a TreeCellRenderer for JCheckBoxTree
 * @author Steve Young Copyright (C) 2025
 */
public class JCheckBoxTreeCellRenderer implements TreeCellRenderer {

    private final TriStateJCheckBox checkBox;

    final JPanel panel;

    public JCheckBoxTreeCellRenderer() {
        super();
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        checkBox = new TriStateJCheckBox();
        panel.add(checkBox, BorderLayout.CENTER);
        panel.setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
        boolean expanded, boolean leaf, int row, boolean hasFocus) {

        panel.removeAll();
        panel.add(checkBox, BorderLayout.WEST);

        javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) value;
        if (!(tree instanceof JCheckBoxTree)) {
            return panel;
        }
        JCheckBoxTree jcbt = (JCheckBoxTree) tree;
        TreePath tp = new TreePath(node.getPath());
        if (jcbt.isSelectedPartially(tp)) {
            checkBox.setState(TriStateJCheckBox.State.PARTIAL);
        } else {
            checkBox.setSelected(jcbt.isSelected(tp));
        } 
        Object obj = node.getUserObject();
        checkBox.setText(obj == null ? null : obj.toString());
        JPanel withExtras = getPanelExtras(node);
        if ( withExtras != null ) {
            panel.add( withExtras, BorderLayout.EAST);
        }
        return panel;
    }

    public JPanel getPanelExtras(javax.swing.tree.DefaultMutableTreeNode value) {
        return null;
    }

}
