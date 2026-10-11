package jmri.util.swing;

import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
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
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        // panel.setLayout(new BorderLayout());
        checkBox = new TriStateJCheckBox();
        // panel.add(checkBox);
        panel.setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
        boolean expanded, boolean leaf, int row, boolean hasFocus) {

        panel.removeAll();
        panel.add(checkBox);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
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
            panel.add( withExtras);
        }
        return panel;
    }

    public JPanel getPanelExtras(DefaultMutableTreeNode value) {
        return null;
    }

}
