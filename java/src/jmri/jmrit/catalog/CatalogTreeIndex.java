package jmri.jmrit.catalog;

import java.util.HashMap;
import jmri.CatalogTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeModel used by CatalogPanel to create a tree of resources. This model is
 * for trees that can be permanently stored to and reloaded from an XML file.
 * <p>
 * Source of the tree content is an XML file.
 *
 * @author Pete Cressman Copyright 2009
 *
 */
public class CatalogTreeIndex extends AbstractCatalogTree {

    public CatalogTreeIndex(String sysName, String userName) {

        super(sysName, userName);
    }

    /**
     * Recursively add nodes to the tree
     *
     * @param pName   Name of the resource to be scanned; this is only used for
     *                the human-readable tree
     * @param pPath   Path to this resource, including the pName part
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *                where in the tree to insert it.
     */
    @Override
    public void insertNodes(String pName, String pPath, CatalogTreeNode pParent) {
        CatalogTreeNode newNode;
        if (pPath == null) {
            newNode = new CatalogTreeNode("Image Index");
        } else {
            newNode = new CatalogTreeNode(pName);
        }
        if (log.isDebugEnabled()) {
            log.debug("insertNodeInto: newNode= {}, into parent= {}", newNode.getUserObject(), pParent.getUserObject());
        }
        insertNodeInto(newNode, pParent, pParent.getChildCount());
    }

    @Override
    public void setProperty(String key, Object value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(key, value);
    }

    @Override
    public Object getProperty(String key) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters.get(key);
    }

    @Override
    public java.util.Set<String> getPropertyKeys() {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters.keySet();
    }

    @Override
    public void removeProperty(String key) {
        if (parameters == null || key == null) {
            return;
        }
        parameters.remove(key);
    }

    HashMap<String, Object> parameters = null;

    private final static Logger log = LoggerFactory.getLogger(CatalogTreeIndex.class);
}
