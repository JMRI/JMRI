// LnSecurityElementManager.java

package jmri.jmrix.loconet;

import com.sun.java.util.collections.*;
import jmri.*;
import java.util.Enumeration;

/**
 * Manager for SecurityElement objects, currently local to LocoNet implementations.
 * Provides basic rendevous services. Also serves as the manager for AspectGenerator
 * objects (for now).
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version         $Revision: 1.1 $
 */
public class LnSecurityElementManager {

	public LnSecurityElementManager() {

        // register ourselves
        InstanceManager.configureManagerInstance().register(this);

	}

	// to free resources when no longer used
	public void dispose() throws JmriException {
        mHashSE.clear();
        mHashAG.clear();
	}

	protected Hashtable mHashSE = new Hashtable();   // stores known instances by system name/number
	protected Hashtable mHashAG = new Hashtable();   // stores known instances by system name/number

    /**
     * Get the list of known SecurityElement objects, unsorted
     */
    public List getSecurityElementList() {
        List out = new ArrayList();
        Enumeration en = mHashSE.elements();
        while (en.hasMoreElements()) {
            out.add((SecurityElement)en.nextElement());  // cast is just a sanity check
        }
         return out;
    }

    /**
     * Get the list of known AspectGenerator objects, unsorted
     */
    public List getAspectGeneratorList() {
        List out = new ArrayList();
        Enumeration en = mHashAG.elements();
        while (en.hasMoreElements()) {
            out.add((AspectGenerator)en.nextElement());  // cast is just a sanity check
        }
         return out;
    }
    /**
     * Create a new object as needed
     */
    public SecurityElement getSecurityElement(int i) {
        SecurityElement se = (SecurityElement)mHashSE.get(new Integer(i));
        if (se!=null) return se;
        else {
            se = new SecurityElement(i);
            mHashSE.put(new Integer(i), se);
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
        AspectGenerator ag = (AspectGenerator)mHashAG.get(new Integer(i));
        if (ag!=null) return ag;
        else {
            ag = new AspectGenerator(i);
            mHashAG.put(new Integer(i), ag);
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
	protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
		}


    static LnSecurityElementManager mInstance = null;

    public static LnSecurityElementManager instance() {
        if (mInstance == null) mInstance = new LnSecurityElementManager();
        return mInstance;
    }
}


/* @(#)LnSecurityElementManager.java */
