// CatalogPane.java
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
 * @author	Bob Jacobsen Copyright 2002
 * @version	$Revision$
 */
public class CatalogPane extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -1101700212671088828L;

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
        previewPanel.add(new JLabel("File Preview:   "));
        JScrollPane js = new JScrollPane(preview);
        previewPanel.add(js);
        add(previewPanel);
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public NamedIcon getSelectedIcon() {
        if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null) {
            // somebody has been selected
            log.debug("getSelectedIcon with " + dTree.getSelectionPath().toString());
            TreePath path = dTree.getSelectionPath();
            int level = path.getPathCount();
            if (level < 3) {
                return null;
            }
            if (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject().equals("resources")) {
                // process a .jar icon
                StringBuffer buf = new StringBuffer(CatalogTreeModel.resourceRoot);
                for (int i = 2; i < level; i++) {
                    buf.append("/");
                    buf.append((String) ((DefaultMutableTreeNode) path.getPathComponent(i)).getUserObject());
                }
                String name = buf.toString();
                log.debug("attempt to load resource from " + name);
                return NamedIcon.getIconByName(name);
            } else if (((DefaultMutableTreeNode) path.getPathComponent(1)).getUserObject().equals("files")) {
                // process a file
                String name = "file:" + (String) ((DefaultMutableTreeNode) path.getPathComponent(2)).getUserObject();
                for (int i = 3; i < level; i++) {
                    name = name + File.separator
                            + (String) ((DefaultMutableTreeNode) path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load file from " + name);
                //return new NamedIcon(name, "file:"+name);
                return NamedIcon.getIconByName(name);
            } else {
                log.error("unexpected first element on getSelectedIcon: " + path.getPathComponent(1));
            }
        }
        return null;
    }

    JTree dTree;

    private final static Logger log = LoggerFactory.getLogger(CatalogPane.class.getName());
}
