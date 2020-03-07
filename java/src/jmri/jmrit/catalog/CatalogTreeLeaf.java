package jmri.jmrit.catalog;

/**
 * Leaf of a CatalogTree.
 * <p>
 * Name for the leaf Path to lead.
 *
 * @author Pete Cressman Copyright 2009
 *
 */
public class CatalogTreeLeaf {

    private String _name; // non-localized
    private String _path;
    private int _size;

    public CatalogTreeLeaf(String name, String path, int size) {
        _name = name;
        _path = path;
        _size = size;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getPath() {
        return _path;
    }

    public int getSize() {
        return _size;
    }

}
