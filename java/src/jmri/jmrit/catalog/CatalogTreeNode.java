package jmri.jmrit.catalog;

import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node of a CatalogTree.
 *
 * Name for the node Path is info needed for leafs.
 *
 * @author Pete Cressman Copyright 2009
 *
 */
public class CatalogTreeNode extends DefaultMutableTreeNode {

    // Sorted by height for ease of display in CatalogPanel
    private ArrayList<CatalogTreeLeaf> _leafs = new ArrayList<>();

    public CatalogTreeNode(String name) {
        super(name);
    }

    /**
     * Append leaf to the end of the leafs list.
     *
     * @param leaf the leaf to add
     */
    public void addLeaf(CatalogTreeLeaf leaf) {
        _leafs.add(leaf);
    }

    /**
     * Insert leaf according to height.
     *
     * @param name name of the new leaf
     * @param path path to the new leaf
     */
    public void addLeaf(String name, String path) {
        // check path
        NamedIcon icon = NamedIcon.getIconByName(path);
        if (icon == null) {
            log.warn("path \"" + path + "\" is not a NamedIcon.");
            return;
        }
        int h = icon.getIconHeight();
        for (int i = 0; i < _leafs.size(); i++) {
            CatalogTreeLeaf leaf = _leafs.get(i);
            if (h < leaf.getSize()) {
                _leafs.add(i + 1, new CatalogTreeLeaf(name, path, h));
                return;
            }
        }
        _leafs.add(new CatalogTreeLeaf(name, path, h));
    }

    /**
     * Leafs can be used for many-to-many relations.
     *
     * @param name the leafs to remove
     */
    public void deleteLeaves(String name) {
        for (int i = 0; i < _leafs.size(); i++) {
            CatalogTreeLeaf leaf = _leafs.get(i);
            if (name.equals(leaf.getName())) {
                _leafs.remove(i);
            }
        }
    }

    public void deleteLeaf(String name, String path) {
        for (int i = 0; i < _leafs.size(); i++) {
            CatalogTreeLeaf leaf = _leafs.get(i);
            if (name.equals(leaf.getName()) && path.equals(leaf.getPath())) {
                _leafs.remove(i);
                return;
            }
        }
    }

    public CatalogTreeLeaf getLeaf(String name, String path) {
        for (int i = 0; i < _leafs.size(); i++) {
            CatalogTreeLeaf leaf = _leafs.get(i);
            if (name.equals(leaf.getName()) && path.equals(leaf.getPath())) {
                return leaf;
            }
        }
        return null;
    }

    /**
     * Leafs can be used for many-to-many relations.
     *
     * @param name name of the leafs to get
     * @return a list of matching leafs; an empty list if there are no matching
     *         leafs
     */
    public ArrayList<CatalogTreeLeaf> getLeaves(String name) {
        ArrayList<CatalogTreeLeaf> leafs = new ArrayList<>();
        for (int i = 0; i < _leafs.size(); i++) {
            CatalogTreeLeaf leaf = _leafs.get(i);
            if (name.equals(leaf.getName())) {
                leafs.add(leaf);
            }
        }
        return leafs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<CatalogTreeNode> children() {
        return super.children();
    }

    public ArrayList<CatalogTreeLeaf> getLeaves() {
        return _leafs;
    }

    public int getNumLeaves() {
        return _leafs.size();
    }

    public void setLeaves(ArrayList<CatalogTreeLeaf> leafs) {
        _leafs = leafs;
    }

    private final static Logger log = LoggerFactory.getLogger(CatalogTreeNode.class);
}
