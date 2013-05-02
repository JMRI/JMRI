// AbstractProxyManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Manager;
import jmri.NamedBean;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Iterator;

import jmri.util.SystemNameComparator;

/**
 * Implementation of a Manager that can serves as a proxy
 * for multiple system-specific implementations.  
 * <p>
 * Automatically includes an Internal system, which 
 * need not be separately added any more.
 * <p>
 * Encapsulates access to the "Primary" manager,
 * used by default.
 * <p>
 * Internally, this is done by using a list of all
 * non-Internal managers, plus a separate reference to the
 * internal manager.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2010
 * @version	$Revision$
 */
abstract public class AbstractProxyManager implements Manager {

    /**
     * Number of managers available through
     * getManager(i) and getManagerList(),
     * including the Internal manager
     */
    protected int nMgrs() {
        return mgrs.size()+1;
    }
    
    protected AbstractManager getMgr(int index) {
        if (index < mgrs.size()) return mgrs.get(index);
        else if (index == mgrs.size()) return getInternal();
        else throw new IllegalArgumentException("illegal index "+index);
    }

    /** 
     * Returns a list of all managers, including the
     * internal manager.  This is not a live list.
     */
    public List<Manager> getManagerList(){
        @SuppressWarnings("unchecked")
        List<Manager> retval = (List<Manager>)mgrs.clone();
        retval.add(getInternal());
        return retval;
    }

    public void addManager(Manager m) {
        mgrs.add((AbstractManager)m);
        if(log.isDebugEnabled()) log.debug("added manager " +m.getClass());
    }

    private AbstractManager getInternal() {
        if (internalManager == null) internalManager = makeInternalManager();
        return internalManager;
    }
    
    private java.util.ArrayList<AbstractManager> mgrs = new java.util.ArrayList<AbstractManager>();
    private AbstractManager internalManager = null;
    abstract protected AbstractManager makeInternalManager();
    
    /**
     * Locate via user name, then system name if needed.
     * Subclasses use this to provide e.g. getSensor, getTurnout, etc
     * via casts.
     *
     * @param name
     * @return Null if nothing by that name exists
     */
    public NamedBean getNamedBean(String name) {
        NamedBean t = getBeanByUserName(name);
        if (t != null) return t;
        return getBeanBySystemName(name);
    }

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new NamedBean: If the name
     * is a valid system name, it will be used for the new
     * NamedBean.  Otherwise, the makeSystemName method
     * will attempt to turn it into a valid system name.
     * Subclasses use this to provide e.g. getSensor, getTurnout, etc
     * via casts.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    protected NamedBean provideNamedBean(String name) {
        NamedBean t = getNamedBean(name);
        if (t!=null) return t;
        // Doesn't exist. If the systemName was specified, find that system
        int index = matchTentative(name);
        if (index >= 0) return makeBean(index, name, null);
        log.debug("Did not find manager for name "+name+", defer to default");
		return makeBean(0,getMgr(0).makeSystemName(name), null);
    }

    /**
     * Defer creation of the proper type to the subclass
     * @param index Which manager to invoke
     */
    abstract protected NamedBean makeBean(int index, String systemName, String userName);

    public NamedBean getBeanBySystemName(String systemName) {
        NamedBean t = null;
        for (int i=0; i<nMgrs(); i++) {
            t = (NamedBean)getMgr(i).getInstanceBySystemName(systemName);
            if (t!=null) return t;
        }
        return null;
    }

    public NamedBean getBeanByUserName(String userName) {
        NamedBean t = null;
        for (int i=0; i<nMgrs(); i++) {
            t = (NamedBean)getMgr(i).getInstanceByUserName(userName);
            if (t!=null) return t;
        }
        return null;
    }


    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Sensor object representing a given physical turnout
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference for a valid request;
     * a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the NamedBean object created; a valid system name must be provided
     *<LI>If a null reference is given for the system name, a system name
     *    will _somehow_ be inferred from the user name.  How this is done
     *    is system specific.  Note: a future extension of this interface
     *    will add an exception to signal that this was not possible.
     *<LI>If both names are provided, the system name defines the
     *    hardware access of the desired turnout, and the user address
     *    is associated with it.
     *</UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects.  This is a problem, and we don't have a
     * good solution except to issue warnings.
     * This will mostly happen if you're creating NamedBean when you should
     * be looking them up.
     * @return requested NamedBean object (never null)
     */
    public NamedBean newNamedBean(String systemName, String userName) {
        // if the systemName is specified, find that system
        int i = matchTentative(systemName);
        if (i >= 0) 
            return makeBean(i, systemName, userName);
            
        // did not find a manager, allow it to default to the primary
        log.debug("Did not find manager for system name "+systemName+", delegate to primary");
        return makeBean(0, systemName, userName);
    }

    public void dispose() {
        for (int i=0; i<mgrs.size(); i++)
            mgrs.get(i).dispose();
        mgrs.clear();
        if (internalManager != null) internalManager.dispose(); // don't make if not made yet
    }

    /**
     * Find the index of a matching manager.
     * Returns -1 if there is no match, which is not considered an 
     * error
     */
    protected int matchTentative(String systemname) {
        for (int i = 0; i<nMgrs(); i++)
             if ( systemname.startsWith((getMgr(i)).getSystemPrefix()+(getMgr(i)).typeLetter())) {
                return i;
            }
            return -1;
    }

    /**
     * Find the index of a matching manager.
     * Throws IllegalArgumentException if there is no match,
     * here considered to be an error that must be reported.
     */
    protected int match(String systemname) {
        int index = matchTentative(systemname);
        if (index < 0) throw new IllegalArgumentException("System name "+systemname+" failed to match");
        return index;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * Forwards the register request to the matching system
     */
    public void register(NamedBean s) {
        String systemName = s.getSystemName();
        getMgr(match(systemName)).register(s);
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * Forwards the deregister request to the matching system
     */
    public void deregister(NamedBean s) {
        String systemName = s.getSystemName();
        getMgr(match(systemName)).deregister(s);
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i<nMgrs(); i++)
            getMgr(i).addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i<nMgrs(); i++)
            getMgr(i).removePropertyChangeListener(l);
    }

    /**
     * @return The system-specific prefix letter for the primary implementation
     */
    public String getSystemPrefix() {
	try {
          return getMgr(0).getSystemPrefix();
        } catch(IndexOutOfBoundsException ie) {
          return "?";
        }
    }
    
    /**
     * Provide 1st char of systemPrefix for now
     * @deprecated
     */
    @Deprecated
    public char systemLetter() {
        return getSystemPrefix().charAt(0);
    }
    
    /**
     * @return The type letter for turnouts
     */
    public char typeLetter() {
        return getMgr(0).typeLetter();
    }

    /**
     * @return A system name from a user input, typically a number,
     * from the primary system.
     */
    public String makeSystemName(String s) {
        return getMgr(0).makeSystemName(s);
    }

    public String[] getSystemNameArray() {
        TreeSet<String> ts = new TreeSet<String>(new SystemNameComparator());
        for (int i = 0; i<nMgrs(); i++) {
            ts.addAll( getMgr(i).getSystemNameList() );
        }
        String[] arr = new String[ts.size()];
        Iterator<String> it = ts.iterator();
        int i=0;
        while(it.hasNext()) {
            arr[i++] = it.next();
        }
        return arr;
    }
    
    /**
     * Get a list of all system names.
     */
    public List<String> getSystemNameList() {
        TreeSet<String> ts = new TreeSet<String>(new SystemNameComparator());
        for (int i = 0; i<nMgrs(); i++) {
            ts.addAll(getMgr(i).getSystemNameList());
        }
        return new ArrayList<String>(ts);
    }
    
    public List<NamedBean> getNamedBeanList() {
        TreeSet<NamedBean> ts = new TreeSet<NamedBean>(new SystemNameComparator());
        for (AbstractManager m:mgrs) {
            ts.addAll(m.getNamedBeanList());
        }
        return new ArrayList<NamedBean>(ts);
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(AbstractProxyManager.class.getName());
}

/* @(#)AbstractProxyManager.java */
