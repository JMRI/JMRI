package jmri.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a Manager that can serves as a proxy for multiple
 * system-specific implementations.
 * <p>
 * Automatically includes an Internal system, which need not be separately added
 * any more.
 * <p>
 * Encapsulates access to the "Primary" manager, used by default.
 * <p>
 * Internally, this is done by using a list of all non-Internal managers, plus a
 * separate reference to the internal manager.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2010
 */
abstract public class AbstractProxyManager<E extends NamedBean> implements Manager<E> {

    /**
     * Number of managers available through getManager(i) and getManagerList(),
     * including the Internal manager
     *
     * @return the number of managers.
     */
    protected int nMgrs() {
        // make sure internal present
        initInternal();

        return mgrs.size();
    }

    protected Manager<E> getMgr(int index) {
        // make sure internal present
        initInternal();

        if (index < mgrs.size()) {
            return mgrs.get(index);
        } else {
            throw new IllegalArgumentException("illegal index " + index);
        }
    }

    /**
     * Returns a list of all managers, including the internal manager. This is
     * not a live list.
     *
     * @return the list of managers
     */
    public List<Manager<E>> getManagerList() {
        // make sure internal present
        initInternal();

        return new ArrayList<>(mgrs);
    }

    public void addManager(Manager<E> m) {
        // check for already present
        if (mgrs.contains(m)) {
            // already present, complain and skip
            log.warn("Manager already present: {}", m);
            return;
        }
        mgrs.add(m);
        propertyVetoListenerList.stream().forEach((l) -> {
            m.addVetoableChangeListener(l);
        });
        propertyListenerList.stream().forEach((l) -> {
            m.addPropertyChangeListener(l);
        });
        if (log.isDebugEnabled()) {
            log.debug("added manager " + m.getClass());
        }
    }

    private Manager<E> initInternal() {
        if (internalManager == null) {
            log.debug("create internal manager when first requested");
            internalManager = makeInternalManager();
        }
        return internalManager;
    }

    private final java.util.ArrayList<Manager<E>> mgrs = new java.util.ArrayList<>();
    private Manager<E> internalManager = null;

    /**
     * Create specific internal manager as needed for concrete type.
     *
     * @return an internal manager
     */
    abstract protected Manager<E> makeInternalManager();

    /**
     * Locate via user name, then system name if needed. Subclasses use this to
     * provide more specific getters such as getSensor or getTurnout via casts.
     *
     * @param name the user or system name for the requested NamedBean
     * @return the requested NamedBean or null if nothing matches name
     */
    @Override
    public E getNamedBean(String name) {
        E t = getBeanByUserName(name);
        if (t != null) {
            return t;
        }
        return getBeanBySystemName(name);
    }

    /**
     * Enforces, and as a user convenience converts to, the standard form for a system name
     * for the NamedBeans handled by this manager and its submanagers.
     * <p>
     * Attempts to match by system prefix first.
     * <p> 
     *
     * @param inputName System name to be normalized
     * @throws NamedBean.BadSystemNameException If the inputName can't be converted to normalized form
     * @return A system name in standard normalized form 
     */
    @Override
    @CheckReturnValue
    public @Nonnull String normalizeSystemName(@Nonnull String inputName) throws NamedBean.BadSystemNameException {
        int index = matchTentative(inputName);
        if (index >= 0) {
            return getMgr(index).normalizeSystemName(inputName);
        }
        log.debug("normalizeSystemName did not find manager for name " + inputName + ", defer to default");
        return getMgr(0).normalizeSystemName(inputName);
    }

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new NamedBean: If the name is a valid system name, it will be used for
     * the new NamedBean. Otherwise, the makeSystemName method will attempt to
     * turn it into a valid system name. Subclasses use this to create provider methods such as 
     * getSensor or getTurnout via casts.
     *
     * @param name the user name or system name of the bean
     * @return an existing or new NamedBean
     */
    protected E provideNamedBean(String name) throws IllegalArgumentException {
        // make sure internal present
        initInternal();

        E t = getNamedBean(name);
        if (t != null) {
            return t;
        }
        // Doesn't exist. If the systemName was specified, find that system
        int index = matchTentative(name);
        if (index >= 0) {
            return makeBean(index, name, null);
        }
        log.debug("provideNamedBean did not find manager for name " + name + ", defer to default");
        return makeBean(0, getMgr(0).makeSystemName(name), null);
    }

    /**
     * Defer creation of the proper type to the subclass
     *
     * @param index      the manager to invoke
     * @param systemName the system name
     * @param userName   the user name
     * @return a bean
     */
    abstract protected E makeBean(int index, String systemName, String userName);

    @Override
    public E getBeanBySystemName(String systemName) {
        for (Manager<E> m : this.mgrs) {
            E b = m.getBeanBySystemName(systemName);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    @Override
    public E getBeanByUserName(String userName) {
        for (Manager<E> m : this.mgrs) {
            E b = m.getBeanByUserName(userName);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Sensor object representing a given physical turnout and
     * therefore only one with a specific system or user name.
     * <P>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <UL>
     * <LI>If a null reference is given for user name, no user name will be
     * associated with the NamedBean object created; a valid system name must be
     * provided
     * <LI>If a null reference is given for the system name, a system name will
     * _somehow_ be inferred from the user name. How this is done is system
     * specific. Note: a future extension of this interface will add an
     * exception to signal that this was not possible.
     * <LI>If both names are provided, the system name defines the hardware
     * access of the desired turnout, and the user address is associated with
     * it.
     * </UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * NamedBean when you should be looking them up.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return requested NamedBean object (never null)
     */
    public E newNamedBean(String systemName, String userName) {
        // make sure internal present
        initInternal();

        // if the systemName is specified, find that system
        int i = matchTentative(systemName);
        if (i >= 0) {
            return makeBean(i, systemName, userName);
        }

        // did not find a manager, allow it to default to the primary
        log.debug("Did not find manager for system name " + systemName + ", delegate to primary");
        return makeBean(0, systemName, userName);
    }

    @Override
    public void dispose() {
        for (int i = 0; i < mgrs.size(); i++) {
            mgrs.get(i).dispose();
        }
        mgrs.clear();
        if (internalManager != null) {
            internalManager.dispose(); // don't make if not made yet
        }
    }

    /**
     * Find the index of a matching manager. Returns -1 if there is no match,
     * which is not considered an error
     *
     * @param systemname the system name
     * @return the index of the matching manager
     */
    protected int matchTentative(String systemname) {
        for (int i = 0; i < nMgrs(); i++) {
            if (systemname.startsWith((getMgr(i)).getSystemPrefix() + (getMgr(i)).typeLetter())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find the index of a matching manager. Throws IllegalArgumentException if
     * there is no match, here considered to be an error that must be reported.
     *
     * @param systemname the system name
     * @return the index of the matching manager
     */
    protected int match(String systemname) {
        // make sure internal present
        initInternal();

        int index = matchTentative(systemname);
        if (index < 0) {
            throw new IllegalArgumentException("System name " + systemname + " failed to match");
        }
        return index;
    }

    @Override
    public void deleteBean(E s, String property) throws java.beans.PropertyVetoException {
        String systemName = s.getSystemName();
        try {
            getMgr(match(systemName)).deleteBean(s, property);
        } catch (java.beans.PropertyVetoException e) {
            throw e;
        }
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * Forwards the register request to the matching system
     *
     * @param s the bean
     */
    @Override
    public void register(E s) {
        String systemName = s.getSystemName();
        getMgr(match(systemName)).register(s);
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * Forwards the deregister request to the matching system
     *
     * @param s the name
     */
    @Override
    public void deregister(E s) {
        String systemName = s.getSystemName();
        getMgr(match(systemName)).deregister(s);
    }

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i < nMgrs(); i++) {
            getMgr(i).addPropertyChangeListener(l);
        }
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i < nMgrs(); i++) {
            getMgr(i).removePropertyChangeListener(l);
        }
    }

    @Override
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        if (!propertyVetoListenerList.contains(l)) {
            propertyVetoListenerList.add(l);
        }
        for (int i = 0; i < nMgrs(); i++) {
            getMgr(i).addVetoableChangeListener(l);
        }
    }

    @Override
    public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        if (propertyVetoListenerList.contains(l)) {
            propertyVetoListenerList.remove(l);
        }
        for (int i = 0; i < nMgrs(); i++) {
            getMgr(i).removeVetoableChangeListener(l);
        }
    }

    ArrayList<java.beans.PropertyChangeListener> propertyListenerList = new ArrayList<>(5);
    ArrayList<java.beans.VetoableChangeListener> propertyVetoListenerList = new ArrayList<>(5);

    /**
     * @return The system-specific prefix letter for the primary implementation
     */
    @Override
    public String getSystemPrefix() {
        try {
            return getMgr(0).getSystemPrefix();
        } catch (IndexOutOfBoundsException ie) {
            return "?";
        }
    }

    /**
     * @return The type letter for turnouts
     */
    @Override
    public char typeLetter() {
        return getMgr(0).typeLetter();
    }

    /**
     * @return A system name from a user input, typically a number, from the
     *         primary system.
     */
    @Override
    public String makeSystemName(String s) {
        return getMgr(0).makeSystemName(s);
    }

    /**
     * Get a list of all system names.
     * <p>
     * The list is ordered by system name
     *
     * @return a list, possibly empty, of system names
     */
    @Override
    @Nonnull
    public String[] getSystemNameArray() {
        List<E> list = getNamedBeanList();
        String[] retval = new String[list.size()];
        int i = 0;
        for (E e : list) retval[i++] = e.getSystemName();
        return retval;
    }

    /**
     * Get a list of all system names.
     * <p>
     * The list is ordered by system name
     *
     * @return a list, possibly empty, of system names
     */
    @Override
    @Nonnull
    public List<String> getSystemNameList() {
        List<E> list = getNamedBeanList();
        ArrayList<String> retval = new ArrayList<>(list.size());
        for (E e : list) retval.add(e.getSystemName());
        return retval;
    }

    /**
     * Get a list of all system names.
     * <p>
     * The list is ordered by system name
     *
     * @return a list, possibly empty, of system names
     */
    @Override
    @Nonnull
    public List<E> getNamedBeanList() {
        TreeSet<E> ts = new TreeSet<>(new NamedBeanComparator());
        mgrs.stream().forEach((m) -> {
            ts.addAll(m.getNamedBeanList());
        });
        return new ArrayList<>(ts);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractProxyManager.class);

}
