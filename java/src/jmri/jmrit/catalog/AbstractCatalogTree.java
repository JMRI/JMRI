package jmri.jmrit.catalog;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.tree.DefaultTreeModel;
import jmri.CatalogTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeModel used by CatalogPanel to create a tree of resources.
 * <P>
 *
 * @author	Pete Cressman Copyright 2009
 *
 */
public abstract class AbstractCatalogTree extends DefaultTreeModel implements CatalogTree {

    private String mUserName;
    private String mSystemName;

    public AbstractCatalogTree(String sysname, String username) {
        super(new CatalogTreeNode(username));
        mUserName = username;
        mSystemName = sysname.toUpperCase();
    }

    public AbstractCatalogTree(String sysname) {
        this(sysname, "root");
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameCatalog");
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
    public abstract void insertNodes(String pName, String pPath, CatalogTreeNode pParent);

    /**
     * Starting point to recursively add nodes to the tree by scanning a file
     * directory
     *
     * @param pathToRoot Path to Directory to be scanned
     */
    public void insertNodes(String pathToRoot) {
        CatalogTreeNode root = getRoot();
        if (log.isDebugEnabled()) {
            log.debug("insertNodes: rootName= " + root.getUserObject()
                    + ", pathToRoot= " + pathToRoot);
        }
        insertNodes((String) root.getUserObject(), pathToRoot, root);
    }

    /**
     * Get the root element of the tree as a jmri.CatalogTreeNode object.
     * (Instead of Object, as parent swing.TreeModel provides)
     */
    public CatalogTreeNode getRoot() {
        return (CatalogTreeNode) super.getRoot();
    }

    /**
     * ** NamedBean implementation (Copied from AbstractNamedBean) *********
     */
    /**
     * Get associated comment text.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Set associated comment text.
     * <p>
     * Comments can be any valid text.
     *
     * @param comment Null means no comment associated.
     */
    public void setComment(String comment) {
        String old = this.comment;
        this.comment = comment;
        firePropertyChange("Comment", old, comment);
    }
    private String comment;

    public String getDisplayName() {
        String name = getUserName();
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return getSystemName();
        }
    }

    public String getFullyFormattedDisplayName() {
        String name = getUserName();
        if (name != null && name.length() > 0) {
            name = name + "(" + getSystemName() + ")";
        } else {
            name = getSystemName();
        }
        return name;
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       	Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    java.beans.PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Number of current listeners. May return -1 if the information is not
     * available for some reason.
     */
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    Hashtable<java.beans.PropertyChangeListener, String> register = new Hashtable<>();
    Hashtable<java.beans.PropertyChangeListener, String> listenerRefs = new Hashtable<>();

    public synchronized void addPropertyChangeListener(PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(l);
        if (beanRef != null) {
            register.put(l, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(l, listenerRef);
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        ArrayList<PropertyChangeListener> list = new ArrayList<>();
        Enumeration<PropertyChangeListener> en = register.keys();
        while (en.hasMoreElements()) {
            PropertyChangeListener l = en.nextElement();
            if (register.get(l).equals(name)) {
                list.add(l);
            }
        }
        return list.toArray(new PropertyChangeListener[list.size()]);
    }

    /* This allows a meaning full list of places where the bean is in use!*/
    public synchronized ArrayList<String> getListenerRefs() {
        ArrayList<String> list = new ArrayList<>();
        Enumeration<PropertyChangeListener> en = listenerRefs.keys();
        while (en.hasMoreElements()) {
            PropertyChangeListener l = en.nextElement();
            list.add(listenerRefs.get(l));
        }
        return list;
    }

    public synchronized void updateListenerRef(PropertyChangeListener l, String newName) {
        if (listenerRefs.containsKey(l)) {
            listenerRefs.put(l, newName);
        }
    }

    public synchronized String getListenerRef(java.beans.PropertyChangeListener l) {
        return listenerRefs.get(l);
    }

    public String getSystemName() {
        return mSystemName;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String s) {
        String old = mUserName;
        mUserName = s;
        firePropertyChange("UserName", old, s);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public void dispose() {
        pcs = null;
    }

    public int getState() {
        return 0;
    }

    public void setState(int s) throws jmri.JmriException {
    }

    public void addDeleteLock(jmri.NamedBean lock) {
    }

    public void removeDeleteLock(jmri.NamedBean lock) {
    }

    public boolean isDeleteAllowed() {
        return true;
    }

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractCatalogTree.class.getName());

}
