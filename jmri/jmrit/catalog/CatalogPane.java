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
 * Create a JPanel containing a tree of resources
 *
 * @author			Bob Jacobsen
 * @version			$Revision: 1.2 $
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
        insertResourceNodes("resources", resourceRoot, dRoot);
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
     * @param name Name of the resource to be scanned
     * @param parent Node for the parent of the resource to be scanned
     */
    void insertResourceNodes(String name, String path, DefaultMutableTreeNode parent) {
        // first, represent this one
        DefaultMutableTreeNode newElement = new DefaultMutableTreeNode(name);
        dModel.insertNodeInto(newElement, parent, parent.getChildCount());
        // then look for childrent and recurse
        // getSystemResource is a URL, getFile is the filename string
        File fp = new File(ClassLoader.getSystemResource(path).getFile());
        if (fp.isDirectory()) {
            // work on the kids
            String[] sp = fp.list();
 			for (int i=0; i<sp.length; i++) {
				if (log.isDebugEnabled()) log.debug("Descend into resource: "+sp[i]);
                insertResourceNodes(sp[i],path+"/"+sp[i],newElement);
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

    public ImageIcon getSelectedIcon() {
        if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
            // somebody has been selected
            log.debug("getSelectedIcon with "+dTree.getSelectionPath().toString());
            TreePath path = dTree.getSelectionPath();
            int level = path.getPathCount();
            if (level < 3) return null;
            if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("resources")) {
                // process a file
                String name = resourceRoot;
                for (int i=2; i<level; i++) {
                    name = name+"/"
                            +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load resource from "+name);
                return new ImageIcon(ClassLoader.getSystemResource(name));
            } else if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("files")) {
                // process a file
                String name = fileRoot;
                for (int i=2; i<level; i++) {
                    name = name+File.separator
                            +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load file from "+name);
                return new ImageIcon(name);
            } else log.error("unexpected first element on getSelectedIcon: "+path.getPathComponent(1));
        }
        return null;
    }

    public String getSelectedIconName() {
        if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
            // somebody has been selected
            log.debug("getSelectedIconName with "+dTree.getSelectionPath().toString());
            TreePath path = dTree.getSelectionPath();
            int level = path.getPathCount();
            if (level < 3) return null;
            if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("resources")) {
                // process a file
                String name = resourceRoot;
                for (int i=2; i<level; i++) {
                    name = name+"/"
                            +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load resource from "+name);
                return name;
            } else if (((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject().equals("files")) {
                // process a file
                String name = fileRoot;
                for (int i=2; i<level; i++) {
                    name = name+File.separator
                            +(String)((DefaultMutableTreeNode)path.getPathComponent(i)).getUserObject();
                }
                log.debug("attempt to load file from "+name);
                return name;
            } else log.error("unexpected first element on getSelectedIcon: "+path.getPathComponent(1));
        }
        return null;
    }

    public Icon getIconByName(String name) {
        return new ImageIcon(ClassLoader.getSystemResource(name));
    }

    JTree dTree;
    DefaultTreeModel dModel;
    DefaultMutableTreeNode dRoot;

    private final String resourceRoot = "resources";
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

