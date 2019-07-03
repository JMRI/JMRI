package jmri.jmrit.catalog;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.swing.tree.DefaultTreeModel;
import jmri.CatalogTree;
import jmri.NamedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeModel used by CatalogPanel to create a tree of resources.
 *
 * @author Pete Cressman Copyright 2009
 */
public abstract class AbstractCatalogTree extends DefaultTreeModel implements CatalogTree {

    // force changes through setUserName() to ensure rules are applied
    // as a side effect require reads through getUserName()
    private String mUserName;
    // final so does not need to be private to protect against changes
    protected final String mSystemName;

    public AbstractCatalogTree(String sysname, String username) {
        super(new CatalogTreeNode(username));
        mSystemName = sysname;
        // use this form to prevent subclass from overriding setUserName
        // during construction
        AbstractCatalogTree.this.setUserName(username);
    }

    public AbstractCatalogTree(String sysname) {
        this(sysname, "root");
    }

    @CheckReturnValue
    @Override
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
    @Override
    public abstract void insertNodes(String pName, String pPath, CatalogTreeNode pParent);

    /**
     * Starting point to recursively add nodes to the tree by scanning a file
     * directory
     *
     * @param pathToRoot Path to Directory to be scanned
     */
    @Override
    public void insertNodes(String pathToRoot) {
        // root is a field in the super class, so use r for root
        CatalogTreeNode r = getRoot();
        log.debug("insertNodes: rootName= {}, pathToRoot= {}", r.getUserObject(), pathToRoot);
        insertNodes((String) r.getUserObject(), pathToRoot, r);
    }

    /**
     * Get the root element of the tree as a jmri.CatalogTreeNode object
     * (Instead of Object, as parent swing.TreeModel provides).
     *
     * @return the root element
     */
    @CheckReturnValue
    @Override
    public CatalogTreeNode getRoot() {
        return (CatalogTreeNode) super.getRoot();
    }

    /*
     * NamedBean implementation (Copied from AbstractNamedBean) *********
     */
    /**
     * Get associated comment text.
     */
    @CheckReturnValue
    @Override
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
    @Override
    public void setComment(String comment) {
        String old = this.comment;
        this.comment = comment;
        firePropertyChange("Comment", old, comment);
    }
    private String comment;

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //  public void firePropertyChange(String propertyName,
    //             Object oldValue,
    //      Object newValue)
    // _once_ if anything has changed state
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    java.beans.PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * Number of current listeners. May return -1 if the information is not
     * available for some reason.
     */
    @CheckReturnValue
    @Override
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    HashMap<PropertyChangeListener, String> register = new HashMap<>();
    HashMap<PropertyChangeListener, String> listenerRefs = new HashMap<>();

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(l);
        if (beanRef != null) {
            register.put(l, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(l, listenerRef);
        }
    }

    @Override
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(propertyName, listener);
        if (beanRef != null) {
            register.put(listener, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(listener, listenerRef);
        }
    }

    @CheckReturnValue
    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        ArrayList<PropertyChangeListener> list = new ArrayList<>();
        register.keySet().stream().filter((l) -> (register.get(l).equals(name))).forEachOrdered((l) -> {
            list.add(l);
        });
        return list.toArray(new PropertyChangeListener[list.size()]);
    }

    /* This allows a meaning full list of places where the bean is in use!*/
    @CheckReturnValue
    @Override
    public synchronized ArrayList<String> getListenerRefs() {
        return new ArrayList<>(listenerRefs.values());
    }

    @Override
    public synchronized void updateListenerRef(PropertyChangeListener l, String newName) {
        if (listenerRefs.containsKey(l)) {
            listenerRefs.put(l, newName);
        }
    }

    @CheckReturnValue
    @Override
    public synchronized String getListenerRef(java.beans.PropertyChangeListener l) {
        return listenerRefs.get(l);
    }

    @CheckReturnValue
    @Override
    public String getSystemName() {
        return mSystemName;
    }

    @CheckReturnValue
    @Override
    public String getUserName() {
        return mUserName;
    }

    @Override
    public void setUserName(String s) {
        String old = mUserName;
        mUserName = NamedBean.normalizeUserName(s);
        firePropertyChange("UserName", old, mUserName);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    public void dispose() {
        pcs = null;
    }

    @Override
    @CheckReturnValue
    public int getState() {
        return 0;
    }

    @Override
    @CheckReturnValue
    public String describeState(int state) {
        switch (state) {
            case UNKNOWN:
                return Bundle.getMessage("BeanStateUnknowng");
            case INCONSISTENT:
                return Bundle.getMessage("BeanStateInconsistent");
            default:
                return Bundle.getMessage("BeanStateUnexpected", state);
        }
    }

    @Override
    public void setState(int s) throws jmri.JmriException {
    }

    public void addDeleteLock(jmri.NamedBean lock) {
    }

    public void removeDeleteLock(jmri.NamedBean lock) {
    }

    @CheckReturnValue
    public boolean isDeleteAllowed() {
        return true;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
    }

    /**
     * {@inheritDoc} 
     * 
     * By default, does an alphanumeric-by-chunks comparison
     */
    @CheckReturnValue
    @Override
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
        jmri.util.AlphanumComparator ac = new jmri.util.AlphanumComparator();
        return ac.compare(suffix1, suffix2);
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractCatalogTree.class);

}
