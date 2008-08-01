// CatalogPane.java

package jmri.jmrit.catalog;

import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

/**
 * Create a JPanel containing a tree of resources.
 * <P>
 * The tree has two top-level visible nodes.  One, "icons", represents
 * the contents of the icons directory in the resources tree in the .jar
 * file.  The other, "files", is all files found in the "resources"
 * filetree in the preferences directory.  Note that this means that
 * files in the distribution directory are _not_ included.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Bob Jacobsen  Copyright 2002
 * @version			$Revision: 1.14 $
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
        jmri.util.JTreeUtil.setExpandsSelectedPaths(dTree, true);

        dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        dTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
                        // somebody has been selected
                       preview.setIcon(getSelectedIcon());
                    }
                else preview.setIcon(null);
            }
        });

        // add a listener for debugging
        if (log.isDebugEnabled()) dTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
                        // somebody has been selected
                        log.debug("Selection event with "+dTree.getSelectionPath().toString());
                        log.debug("          icon: "+getSelectedIcon());
                    }
                }
            });
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel,BoxLayout.X_AXIS));
        previewPanel.add(new JLabel("File Preview:   "));
        JScrollPane js = new JScrollPane(preview);
        previewPanel.add(js);
        add(previewPanel);
    }

    public NamedIcon getSelectedIcon() {
        if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
            // somebody has been selected
            log.debug("getSelectedIcon with "+dTree.getSelectionPath().toString());
            TreePath path = dTree.getSelectionPath();
            int level = path.getPathCount();
            if (level < 3) return null;
            if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("resources")) {
                // process a .jar icon
                String name = CatalogTreeModel.resourceRoot;
                for (int i=2; i<level; i++) {
                    name = name+"/"
                        +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load resource from "+name);
                // return new NamedIcon(ClassLoader.getSystemResource(name), "resource:"+name);
                //return new NamedIcon(name, "resource:"+name);
                return getIconByName(name);
            } else if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("files")) {
                // process a file
                String name = "file:"+(String)((DefaultMutableTreeNode)path.getPathComponent(2)).getUserObject();
                for (int i=3; i<level; i++) {
                    name = name+File.separator
                        +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load file from "+name);
                //return new NamedIcon(name, "file:"+name);
                return getIconByName(name);
            } else log.error("unexpected first element on getSelectedIcon: "+path.getPathComponent(1));
        }
        return null;
    }

    /**
     * Find the icon corresponding to a name. There are three cases:
     * <UL>
     * <LI> Starts with "resource:", treat the rest as a resource pathname
     *                  in the .jar file
     * <LI> Starts with "file:", treat the rest as an absolute file pathname
     *                  or as a relative path below the resource directory in the preferences directory
     * <LI> Otherwise, treat the name as a resource pathname in the .jar file
     * </UL>
     * @param pName The name string, possibly starting with file: or resource:
     * @return the desired icon with this same pName as its name.
     */
    static public NamedIcon getIconByName(String pName) {
        if (pName.startsWith("resource:"))
            // return new NamedIcon(ClassLoader.getSystemResource(pName.substring(9)), pName);
            return new NamedIcon(pName.substring(9), pName);
        else if (pName.startsWith("file:")) {
            String fileName = pName.substring(5);
            
            // historically, absolute path names could be stored 
            // in the 'file' format.  Check for those, and
            // accept them if present
            if ((new File(fileName)).isAbsolute()) {
                log.debug("Load from absolute path: "+fileName);
                return new NamedIcon(fileName, pName);
            }
            // assume this is a relative path from the
            // preferences directory
            fileName = jmri.jmrit.XmlFile.userFileLocationDefault()+File.separator+"resources"+File.separator+fileName;
            log.debug("load from user preferences file: "+fileName);
            return new NamedIcon(fileName, pName);
        }
        // else return new NamedIcon(ClassLoader.getSystemResource(pName), pName);
        else return new NamedIcon(pName, pName);
    }

    JTree dTree;

    // Main entry point
    public static void main(String s[]) {

    	// initialize log4j - from logging control file (lcf) only
    	// if can find it!
    	String logFile = "default.lcf";
    	try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        log.info("CatalogPane starts");

    	// create the demo frame and menus
        CatalogPane pane = new CatalogPane();
        JFrame frame = new JFrame("CatalogPane");
        frame.getContentPane().add(pane);
        // pack and center this frame
      	frame.pack();

        frame.setVisible(true);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CatalogPane.class.getName());
}

