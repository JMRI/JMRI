// CatalogTreeModel.java
package jmri.jmrit.catalog;

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeModel used by CatalogPane to create a tree of resources.
 * <P>
 * Accessed via the instance() member, as we expect to have only one of these
 * models.
 * <P>
 * The tree has two top-level visible nodes. One, "icons", represents the
 * contents of the icons directory in the resources tree in the .jar file. The
 * other, "files", is all files found in the "resources" filetree in the
 * preferences directory. Note that this means that files in the distribution
 * directory are _not_ included.
 * <P>
 * As a special case "simplification", the catalog tree will not contain CVS
 * directories, or files whose name starts with a "."
 *
 * @author	Bob Jacobsen Copyright 2002
 * @version	$Revision$
 */
public class CatalogTreeModel extends DefaultTreeModel {

    /**
     *
     */
    private static final long serialVersionUID = 2743570810771604153L;

    public CatalogTreeModel() {

        super(new DefaultMutableTreeNode("Root"));
        dRoot = (DefaultMutableTreeNode) getRoot();  // this is used because we can't store the DMTN we just made during the super() call

        // we manually create the first node, rather than use
        // the routine, so we can name it.
        insertResourceNodes("resources", resourceRoot, dRoot);
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "resources");
        insertFileNodes("files", fileRoot, dRoot);

    }

    /**
     * Recursively add a representation of the resources below a particular
     * resource
     *
     * @param pName   Name of the resource to be scanned; this is only used for
     *                the human-readable tree
     * @param pPath   Path to this resource, including the pName part
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *                where in the tree to insert it.
     */
    void insertResourceNodes(String pName, String pPath, DefaultMutableTreeNode pParent) {
        // the following (commented) line only worked in JBuilder (July 27 2002)
        // so we switched to storing this info in the resource/ filetree in
        // the application directory, using the 2nd two lines (uncommented)
        // File fp = new File(ClassLoader.getSystemResource(pPath).getFile());
        File fp = new File(pPath);
        if (!fp.exists()) {
            return;
        }

        // suppress overhead files
        if (fp.getName().startsWith(".")) {
            return;
        }
        if (fp.getName().equals("CVS")) {
            return;
        }

        // first, represent this one
        DefaultMutableTreeNode newElement = new DefaultMutableTreeNode(pName);
        insertNodeInto(newElement, pParent, pParent.getChildCount());
        // then look for children and recurse
        if (fp.isDirectory()) {
            // work on the kids
            String[] sp = fp.list();
            for (int i = 0; i < sp.length; i++) {
                //if (log.isDebugEnabled()) log.debug("Descend into resource: "+sp[i]);
                insertResourceNodes(sp[i], pPath + "/" + sp[i], newElement);
            }
        }
    }

    /**
     * Recursively add a representation of the files below a particular file
     *
     * @param name   Name of the file to be scanned
     * @param parent Node for the parent of the file to be scanned
     */
    void insertFileNodes(String name, String path, DefaultMutableTreeNode parent) {
        File fp = new File(path);
        if (!fp.exists()) {
            return;
        }

        // suppress overhead files
        if (fp.getName().startsWith(".")) {
            return;
        }
        if (fp.getName().equals("CVS")) {
            return;
        }

        // represent this one
        DefaultMutableTreeNode newElement = new DefaultMutableTreeNode(name);
        insertNodeInto(newElement, parent, parent.getChildCount());
        // then look for childrent and recurse
        // getSystemResource is a URL, getFile is the filename string
        if (fp.isDirectory()) {
            // work on the kids
            String[] sp = fp.list();
            for (int i = 0; i < sp.length; i++) {
                //if (log.isDebugEnabled()) log.debug("Descend into file: "+sp[i]);
                insertFileNodes(sp[i], path + "/" + sp[i], newElement);
            }
        }
    }

    DefaultMutableTreeNode dRoot;

    static public CatalogTreeModel instance() {
        if (instanceValue == null) {
            instanceValue = new CatalogTreeModel();
        }
        return instanceValue;
    }

    static private CatalogTreeModel instanceValue = null;

    /**
     * Starting point in the .jar file for the "icons" part of the tree
     */
    static final String resourceRoot = "resources";
    static final String fileRoot = FileUtil.getUserFilesPath() + "resources";

    private final static Logger log = LoggerFactory.getLogger(CatalogTreeModel.class.getName());
}
