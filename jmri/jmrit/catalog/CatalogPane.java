// CatalogPane.java

package jmri.jmrit.catalog;

import java.awt.*;
import java.io.File;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * Create a JPanel containing a tree of resources.
 * <P>
 * The tree has two top-level visible nodes.  One, "icons", represents
 * the contents of the icons directory in the resources tree in the .jar
 * file.  The other, "files", is all files found in the "resources"
 * filetree in the preferences directory.  Note that this means that
 * files in the distribution directory are _not_ included.
 *
 * @author			Bob Jacobsen  Copyright 2002
 * @version			$Revision: 1.4 $
 */
public class CatalogPane extends JPanel {
	public CatalogPane() {

        super(true);

	    // create basic GUI
        dRoot = new DefaultMutableTreeNode("Root");
        dModel = new DefaultTreeModel(dRoot);
        dTree = new JTree(dModel);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // we manually create the first node, rather than use
        // the routine, so we can name it.
        insertResourceNodes("icons", resourceRoot, dRoot);
        insertFileNodes("files", fileRoot, dRoot);

        // build the tree GUI
        add(new JScrollPane(dTree));
        dTree.expandPath(new TreePath(dRoot));
        dTree.setRootVisible(false);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        try {   // following might not be present on Mac Classic, but
                //doesn't have a big effect
            dTree.setExpandsSelectedPaths(true);
        } catch (java.lang.NoSuchMethodError e) {}

        dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

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

	}

    /**
     * Recursively add a representation of the resources
     * below a particular resource
     * @param pName Name of the resource to be scanned; this
     *              is only used for the human-readable tree
     * @param pPath Path to this resource, including the pName part
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *              where in the tree to insert it.
     */
    void insertResourceNodes(String pName, String pPath, DefaultMutableTreeNode pParent) {
        // first, represent this one
        DefaultMutableTreeNode newElement = new DefaultMutableTreeNode(pName);
        dModel.insertNodeInto(newElement, pParent, pParent.getChildCount());
        // then look for childrent and recurse
        // getSystemResource is a URL, getFile is the filename string
        File fp = new File(ClassLoader.getSystemResource(pPath).getFile());
        if (fp.isDirectory()) {
            // work on the kids
            String[] sp = fp.list();
 			for (int i=0; i<sp.length; i++) {
				if (log.isDebugEnabled()) log.debug("Descend into resource: "+sp[i]);
                insertResourceNodes(sp[i],pPath+"/"+sp[i],newElement);
 			}
       }
    }

    /**
     * Recursively add a representation of the files
     * below a particular file
     * @param name Name of the file to be scanned
     * @param parent Node for the parent of the file to be scanned
     */
    void insertFileNodes(String name, String path, DefaultMutableTreeNode parent) {
        File fp = new File(path);
        if (!fp.exists()) return;
        // represent this one
        DefaultMutableTreeNode newElement = new DefaultMutableTreeNode(name);
        dModel.insertNodeInto(newElement, parent, parent.getChildCount());
        // then look for childrent and recurse
        // getSystemResource is a URL, getFile is the filename string
        if (fp.isDirectory()) {
            // work on the kids
            String[] sp = fp.list();
 			for (int i=0; i<sp.length; i++) {
				if (log.isDebugEnabled()) log.debug("Descend into file: "+sp[i]);
                insertFileNodes(sp[i],path+"/"+sp[i],newElement);
 			}
       }
    }

    public NamedIcon getSelectedIcon() {
        if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
            // somebody has been selected
            log.debug("getSelectedIcon with "+dTree.getSelectionPath().toString());
            TreePath path = dTree.getSelectionPath();
            int level = path.getPathCount();
            if (level < 3) return null;
            if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("icons")) {
                // process a .jar icon
                String name = resourceRoot;
                for (int i=2; i<level; i++) {
                    name = name+"/"
                            +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load resource from "+name);
                return new NamedIcon(ClassLoader.getSystemResource(name), "resource:"+name);
            } else if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("files")) {
                // process a file
                String name = fileRoot;
                for (int i=2; i<level; i++) {
                    name = name+File.separator
                            +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load file from "+name);
                return new NamedIcon(name, "file:"+name);
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
     * <LI> Otherwise, treat the name as a resource pathname in the .jar file
     * </UL>
     * @param pName The name string, possibly starting with file: or resource:
     * @return the desired icon with this same pName as its name.
     */
    public NamedIcon getIconByName(String pName) {
        if (pName.startsWith("resource:"))
            return new NamedIcon(ClassLoader.getSystemResource(pName.substring(9)), pName);
        else if (pName.startsWith("file:")) {
            String fileName = pName.substring(5);
            log.debug("load from file: "+fileName);
            return new NamedIcon(fileName, pName);
        }
        else return new NamedIcon(ClassLoader.getSystemResource(pName), pName);
    }

    JTree dTree;
    DefaultTreeModel dModel;
    DefaultMutableTreeNode dRoot;

    /**
     * Starting point in the .jar file for the "icons" part of the tree
     */
    private final String resourceRoot = "resources/icons";
    private final String fileRoot = jmri.jmrit.XmlFile.prefsDir()+"resources";

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

