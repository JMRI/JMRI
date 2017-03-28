package jmri.jmrit.catalog;

import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JPanel containing a tree of resources.
 * <P>
 * The tree has two top-level visible nodes. One, "icons", represents the
 * contents of the icons directory in the resources tree in the .jar file. The
 * other, "files", is all files found in the "resources" filetree in the
 * preferences directory. Note that this means that files in the distribution
 * directory are _not_ included.
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
 * @author Bob Jacobsen Copyright 2002
 */
public class CatalogPane extends JPanel {

    JLabel preview = new JLabel();

    public CatalogPane() {

        super(true);

        // create basic GUI
        dTree = new JTree(CatalogTreeModel.instance());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // build the tree GUI
        add(new JScrollPane(dTree));
        dTree.setRootVisible(false);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        dTree.setExpandsSelectedPaths(true);

        dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        dTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null) {
                    // somebody has been selected
                    preview.setIcon(getSelectedIcon());
                } else {
                    preview.setIcon(null);
                }
            }
        });

        // add a listener for debugging
        if (log.isDebugEnabled()) {
            dTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null) {
                        // somebody has been selected
                        log.debug("Selection event with " + dTree.getSelectionPath().toString());
                        log.debug("          icon: " + getSelectedIcon());
                    }
                }
            });
        }
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.X_AXIS));
        previewPanel.add(new JLabel(Bundle.getMessage("FilePreviewLabel")));
        JScrollPane js = new JScrollPane(preview);
        previewPanel.add(js);
        add(previewPanel);
    }

    public NamedIcon getSelectedIcon() {
        if (dTree.isSelectionEmpty() || dTree.getSelectionPath() == null) {
            return null;
        }
        // somebody has been selected
        if (log.isDebugEnabled()) log.debug("getSelectedIcon with " + dTree.getSelectionPath().toString());
        TreePath path = dTree.getSelectionPath();
        int level = path.getPathCount();
        if (level < 3) {
            return null;
        }
        StringBuffer buf;
        String name;
        if (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject().equals("resources")) {
            // process a .jar icon
            buf = new StringBuffer(CatalogTreeModel.resourceRoot);
            for (int i = 2; i < level; i++) {
                buf.append("/");
                buf.append((String) ((DefaultMutableTreeNode) path.getPathComponent(i)).getUserObject());
            }
        } else if (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject().equals("files")) {
            // process a file
            buf = new StringBuffer(CatalogTreeModel.fileRoot);
            buf.append((String) ((DefaultMutableTreeNode) path.getPathComponent(2)).getUserObject());
            for (int i = 3; i < level; i++) {
                buf.append(File.separator);
                buf.append((String) ((DefaultMutableTreeNode) path.getPathComponent(i)).getUserObject());
            }
        } else {
            log.error("unexpected first element on getSelectedIcon: " + path.getPathComponent(1));
            return null;
        }
        name = buf.toString();
        if (log.isDebugEnabled()) log.debug("attempt to load file from " + name);
        return NamedIcon.getIconByName(name);
    }

    JTree dTree;

    private final static Logger log = LoggerFactory.getLogger(CatalogPane.class.getName());
}
