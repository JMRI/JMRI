// AbstractCatalogTree.java

package jmri.jmrit.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.CatalogTree;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import javax.swing.tree.DefaultTreeModel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * TreeModel used by CatalogPanel to create a tree of resources.
 * <P>
 *
 * @author			Pete Cressman  Copyright 2009
 *
 */
public abstract class AbstractCatalogTree extends DefaultTreeModel implements CatalogTree {

    private String mUserName;
    private String mSystemName;

  // private AbstractCatalogTree() {
  //      super(new CatalogTreeNode("BAD Ctor!"));
  //      mSystemName = null;
  //      mUserName = null;
  //      log.warn("Unexpected use of null ctor");
  //      Exception e = new Exception();
  //      e.printStackTrace();
  //  }

    public AbstractCatalogTree(String sysname, String username) {
        super(new CatalogTreeNode(username));
        mUserName = username;
        mSystemName = sysname.toUpperCase();
    }

    public AbstractCatalogTree(String sysname) {
        this(sysname, "root");
    }

    /**
     * Recursively add nodes to the tree
     * @param pName Name of the resource to be scanned; this
     *              is only used for the human-readable tree
     * @param pPath Path to this resource, including the pName part
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *              where in the tree to insert it.
     */
    public abstract void insertNodes(String pName, String pPath, CatalogTreeNode pParent);

    /**
     * Starting point to recursively add nodes to the tree by scanning a file directory
     * @param pathToRoot Path to Directory to be scanned
     */
    public void insertNodes(String pathToRoot) {
        CatalogTreeNode root = (CatalogTreeNode)getRoot();
        if (log.isDebugEnabled()) log.debug("insertNodes: rootName= "+root.getUserObject()
                                            +", pathToRoot= "+pathToRoot);
        insertNodes((String)root.getUserObject(), pathToRoot, root);
    }


    /**** NamedBean implementation (Copied from AbstractNamedBean) **********/
    /**
     * Get associated comment text.  
     */
    public String getComment() { return this.comment; }
    
    /**
     * Set associated comment text.
     * <p>
     * Comments can be any valid text.
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
    
    public String getFullyFormattedDisplayName(){
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
     * Number of current listeners. May return -1 if the 
     * information is not available for some reason.
     */
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }
    
    Hashtable<java.beans.PropertyChangeListener, String> register = new Hashtable<java.beans.PropertyChangeListener, String>();
    Hashtable<java.beans.PropertyChangeListener, String> listenerRefs = new Hashtable<java.beans.PropertyChangeListener, String>();
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(l);
        if(beanRef!=null)
            register.put(l, beanRef);
        if(listenerRef!=null)
            listenerRefs.put(l, listenerRef);
    }
    
    public synchronized ArrayList<java.beans.PropertyChangeListener> getPropertyChangeListeners(String name) {
        ArrayList<java.beans.PropertyChangeListener> list = new ArrayList<java.beans.PropertyChangeListener>();
        Enumeration<java.beans.PropertyChangeListener> en = register.keys();
        while (en.hasMoreElements()) {
            java.beans.PropertyChangeListener l = en.nextElement();
            if(register.get(l).equals(name)){
                list.add(l);
            }
        }
        return list;
    }
    
    /* This allows a meaning full list of places where the bean is in use!*/
    public synchronized ArrayList<String> getListenerRefs() {
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<java.beans.PropertyChangeListener> en = listenerRefs.keys();
        while (en.hasMoreElements()) {
            java.beans.PropertyChangeListener l = en.nextElement();
            list.add(listenerRefs.get(l));
        }
        return list;
    }
    
    public synchronized void updateListenerRef(java.beans.PropertyChangeListener l, String newName){
        if(listenerRefs.contains(l)){
            listenerRefs.put(l, newName);
        }
    }
    
    public synchronized String getListenerRef(java.beans.PropertyChangeListener l) {
        return listenerRefs.get(l);
    }

    public String getSystemName() {return mSystemName;}

    public String getUserName() {return mUserName;}

    public void   setUserName(String s) {
        String old = mUserName;
        mUserName = s;
        firePropertyChange("UserName", old, s);
    }
    
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    public void dispose() { pcs = null; }

    public int getState(){ return 0; }

    public void setState(int s) throws jmri.JmriException{}
    
    public void addDeleteLock(jmri.NamedBean lock) { }
    
    public void removeDeleteLock(jmri.NamedBean lock) { }
    
    public boolean isDeleteAllowed() { return true; }

    static Logger log = LoggerFactory.getLogger(AbstractCatalogTree.class.getName());

}
