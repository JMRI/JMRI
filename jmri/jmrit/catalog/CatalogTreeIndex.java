// CatalogTreeIndex.java

package jmri.jmrit.catalog;

import jmri.CatalogTree;
import jmri.jmrit.XmlFile;
import java.io.File;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

/**
 * TreeModel used by CatalogPanel to create a tree of resources.
 * This model is for trees that can be permanently stored to and
 * reloaded from an XML file.
 * <P>
 * Source of the tree content is an XML file. 
 *
 * @author			Pete Cressman  Copyright 2009
 *
 */
public class CatalogTreeIndex extends AbstractCatalogTree {

    public CatalogTreeIndex(String sysName, String userName) {

        super(sysName, userName);
    }

    /**
     * Recursively add nodes to the tree
     * @param pName Name of the resource to be scanned; this
     *              is only used for the human-readable tree
     * @param pPath Path to this resource, including the pName part
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *              where in the tree to insert it.
     */
    public void insertNodes(String pName, String pPath, CatalogTreeNode pParent) {
        CatalogTreeNode newNode = null;
        if (pPath == null) {
            newNode = new CatalogTreeNode("imageIndex");
        } else {
            newNode = new CatalogTreeNode(pName);
        }
        if (log.isDebugEnabled()) log.debug("insertNodeInto: newNode= "+newNode.getUserObject()+
                                            ", into parent= "+pParent.getUserObject());
        insertNodeInto(newNode, pParent, pParent.getChildCount());
    }
    /*
    public void insertNodes(String rootName, String pathToRoot) {
        CatalogTreeNode root = (CatalogTreeNode)getRoot();
        insertNodes(rootName, pathToRoot, root);
    }
   */
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CatalogTreeIndex.class.getName());
}

