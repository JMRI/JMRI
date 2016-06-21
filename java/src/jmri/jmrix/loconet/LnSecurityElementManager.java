package jmri.jmrix.loconet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import jmri.InstanceManager;

/**
 * Manager for SecurityElement objects, currently local to LocoNet
 * implementations. Provides basic rendevous services. Also serves as the
 * manager for AspectGenerator objects (for now).
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @deprecated 2.13.5, Does not work with the multi-connection correctly,
 * believe not to work correctly before hand and that the feature is not used.
 */
@Deprecated
public class LnSecurityElementManager {

    public LnSecurityElementManager() {

        // register ourselves
        InstanceManager.configureManagerInstance().registerConfig(this);

    }

    // to free resources when no longer used
    public void dispose() {
        mHashSE.clear();
        mHashAG.clear();
    }

    protected Hashtable<Integer, SecurityElement> mHashSE = new Hashtable<Integer, SecurityElement>();   // stores known instances by system name/number
    protected Hashtable<Integer, AspectGenerator> mHashAG = new Hashtable<Integer, AspectGenerator>();   // stores known instances by system name/number

    /**
     * Get the list of known SecurityElement objects, unsorted
     */
    public List<SecurityElement> getSecurityElementList() {
        List<SecurityElement> out = new ArrayList<SecurityElement>();
        Enumeration<SecurityElement> en = mHashSE.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());  // cast is just a sanity check
        }
        return out;
    }

    /**
     * Get the list of known AspectGenerator objects, unsorted
     */
    public List<AspectGenerator> getAspectGeneratorList() {
        List<AspectGenerator> out = new ArrayList<AspectGenerator>();
        Enumeration<AspectGenerator> en = mHashAG.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());  // cast is just a sanity check
        }
        return out;
    }

    /**
     * Create a new object as needed
     */
    public SecurityElement getSecurityElement(int i) {
        SecurityElement se = mHashSE.get(Integer.valueOf(i));
        if (se != null) {
            return se;
        } else {
            se = new SecurityElement(i);
            mHashSE.put(Integer.valueOf(i), se);
            return se;
        }
    }

    public SecurityElement getSecurityElement(String name) {
        int number = Integer.parseInt(name);
        return getSecurityElement(number);
    }

    /**
     * Create a new object as needed
     */
    public AspectGenerator getAspectGenerator(int i) {
        AspectGenerator ag = mHashAG.get(Integer.valueOf(i));
        if (ag != null) {
            return ag;
        } else {
            ag = new AspectGenerator(i);
            mHashAG.put(Integer.valueOf(i), ag);
            return ag;
        }
    }

    public AspectGenerator getAspectGenerator(String name) {
        int number = Integer.parseInt(name);
        return getAspectGenerator(number);
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    static volatile LnSecurityElementManager mInstance = null;

    public static LnSecurityElementManager instance() {
        if (mInstance == null) {
            mInstance = new LnSecurityElementManager();
        }
        return mInstance;
    }
}

