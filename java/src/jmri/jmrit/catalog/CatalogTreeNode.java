// CatalogTreeNode.java
package jmri.jmrit.catalog;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node of a CatalogTree.
 *
 * Name for the node Path is info needed for leaves.
 *
 * @author	Pete Cressman Copyright 2009
 *
 */
public class CatalogTreeNode extends DefaultMutableTreeNode {

    /**
     *
     */
    private static final long serialVersionUID = -7982962823731804336L;
    // Sorted by height for ease of display in CatalogPanel
    private ArrayList<CatalogTreeLeaf> _leaves = new ArrayList<CatalogTreeLeaf>();

    public CatalogTreeNode(String name) {
        super(name);
    }

    /**
     * Append leaf to the end of the leaves list
     */
    public void addLeaf(CatalogTreeLeaf leaf) {
        _leaves.add(leaf);
    }

    /**
     * Insert leaf according to height.
     */
    public void addLeaf(String name, String path) {
        // check path
        NamedIcon icon = NamedIcon.getIconByName(path);
        if (icon == null) {
            log.warn("path \"" + path + "\" is not a NamedIcon.");
            return;
        }
        int h = icon.getIconHeight();
        for (int i = 0; i < _leaves.size(); i++) {
            CatalogTreeLeaf leaf = _leaves.get(i);
            if (h < leaf.getSize()) {
                _leaves.add(i + 1, new CatalogTreeLeaf(name, path, h));
                return;
            }
        }
        _leaves.add(new CatalogTreeLeaf(name, path, h));
    }

    /**
     * Leaves can be used for many-to-many relations
     */
    public void deleteLeaves(String name) {
        for (int i = 0; i < _leaves.size(); i++) {
            CatalogTreeLeaf leaf = _leaves.get(i);
            if (name.equals(leaf.getName())) {
                _leaves.remove(i);
            }
        }
    }

    public void deleteLeaf(String name, String path) {
        for (int i = 0; i < _leaves.size(); i++) {
            CatalogTreeLeaf leaf = _leaves.get(i);
            if (name.equals(leaf.getName()) && path.equals(leaf.getPath())) {
                _leaves.remove(i);
                return;
            }
        }
    }

    public CatalogTreeLeaf getLeaf(String name, String path) {
        for (int i = 0; i < _leaves.size(); i++) {
            CatalogTreeLeaf leaf = _leaves.get(i);
            if (name.equals(leaf.getName()) && path.equals(leaf.getPath())) {
                return leaf;
            }
        }
        return null;
    }

    /**
     * Leaves can be used for many-to-many relations
     */
    public ArrayList<CatalogTreeLeaf> getLeaves(String name) {
        ArrayList<CatalogTreeLeaf> leaves = new ArrayList<CatalogTreeLeaf>();
        for (int i = 0; i < _leaves.size(); i++) {
            CatalogTreeLeaf leaf = _leaves.get(i);
            if (name.equals(leaf.getName())) {
                leaves.add(leaf);
            }
        }
        return leaves;
    }

    public Enumeration<CatalogTreeNode> children() {
        return (Enumeration<CatalogTreeNode>) super.children();
    }
    
    public ArrayList<CatalogTreeLeaf> getLeaves() {
        return _leaves;
    }

    public int getNumLeaves() {
        return _leaves.size();
    }

    public void setLeaves(ArrayList<CatalogTreeLeaf> leaves) {
        _leaves = leaves;
    }

    private final static Logger log = LoggerFactory.getLogger(CatalogTreeNode.class.getName());
}
