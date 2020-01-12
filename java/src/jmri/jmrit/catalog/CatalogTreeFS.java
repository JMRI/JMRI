package jmri.jmrit.catalog;

import java.io.File;
import java.util.HashMap;

/**
 * TreeModel used by CatalogPanel to create a tree of resources.
 * <p>
 * Source of the tree content is the file system. Only directories are included
 * in the tree. A filter can be set to extract particular file types.
 *
 * @author Pete Cressman Copyright 2009
 *
 */
public class CatalogTreeFS extends AbstractCatalogTree {

    String[] _filter;

    public CatalogTreeFS(String sysName, String userName) {
        super(sysName, userName);
    }

    public void setFilter(String[] filter) {
        _filter = new String[filter.length];
        for (int i = 0; i < filter.length; i++) {
            _filter[i] = filter[i];
        }
    }

    public String[] getFilter() {
        String[] filter = new String[_filter.length];
        for (int i = 0; i < _filter.length; i++) {
            filter[i] = _filter[i];
        }
        return filter;
    }

    boolean filter(String ext) {
        if (ext == null) {
            return false;
        }
        if (_filter == null || _filter.length == 0) {
            return true;
        }
        for (int i = 0; i < _filter.length; i++) {
            if (ext.equals(_filter[i])) {
                return true;
            }
        }
        return false;
    }
    
    int count = 0;
    int leafcount = 0;

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
        File fp = new File(pPath);
        if (!fp.exists()) {
            return;
        }

        // suppress overhead files
        String filename = fp.getName();
        if (filename.startsWith(".")) {
            return;
        }
        if (filename.equals("CVS")) {
            return;
        }

        if (fp.isDirectory()) {
            // first, represent this one
            CatalogTreeNode newElement = new CatalogTreeNode(pName);
            insertNodeInto(newElement, pParent, pParent.getChildCount());
            String[] sp = fp.list();
            for (int i = 0; i < sp.length; i++) {
                log.debug("Descend into resource: {} count {}",sp[i], count++);
                insertNodes(sp[i], pPath + File.separator + sp[i], newElement);
            }
        } else /* leaf */ {
            String ext = jmri.util.FileChooserFilter.getFileExtension(fp);
            if (!filter(ext)) {
                return;
            }
            int index = filename.indexOf('.');
            if (index > 0) {
                filename = filename.substring(0, index);
            }
            log.debug("add leaf: {} count {}", filename, leafcount++);
            pParent.addLeaf(filename, pPath);
        }
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
        if (parameters == null) {
            return;
        }
        parameters.remove(key);
    }

    HashMap<String, Object> parameters = null;

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CatalogTreeFS.class);
}
