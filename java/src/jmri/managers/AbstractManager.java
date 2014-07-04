// AbstractManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import jmri.*;


/**
 * Abstract partial implementation for all Manager-type classes.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision$
 */
abstract public class AbstractManager
    implements Manager, java.beans.PropertyChangeListener {

    public AbstractManager(){
        registerSelf();
    }
    
    /*public AbstractManager(int order) {
        // register the result for later configuration
        xmlorder = order;
        registerSelf();
    }*/

    /**
     * By default, register this manager to store as configuration
     * information.  Override to change that.
     **/
    protected void registerSelf() {
         if (InstanceManager.configureManagerInstance()!=null) {
            InstanceManager.configureManagerInstance().registerConfig(this, getXMLOrder());
            log.debug("register for config");
        }
    }
    
    abstract public int getXMLOrder();

    
    public String makeSystemName(String s) {
        return getSystemPrefix()+typeLetter()+s;
    }

    /**
     * Provide access to deprecated method temporarilly
     * @deprecated 2.9.5 Use getSystemPrefix
     */
    @Deprecated
    public char systemLetter() {
        return getSystemPrefix().charAt(0);
    }
    
    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        if (InstanceManager.configureManagerInstance()!= null)
            InstanceManager.configureManagerInstance().deregister(this);
        _tsys.clear();
        _tuser.clear();
    }

    protected Hashtable<String, NamedBean> _tsys = new Hashtable<String, NamedBean>();   // stores known Turnout instances by system name
    protected Hashtable<String, NamedBean> _tuser = new Hashtable<String, NamedBean>();   // stores known Turnout instances by user name

    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.  This is intended to be used by
     * concrete classes to implement their getBySystemName method.
     * We can't call it that here because Java doesn't have polymorphic
     * return types.
     * @return requested Turnout object or null if none exists
     */
    protected Object getInstanceBySystemName(String systemName) {
        return _tsys.get(systemName);
    }

    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists. This is intended to be used by
     * concrete classes to implement their getBySystemName method.
     * We cant call it that here because Java doesn't have polymorphic
     * return types.
     * @return requested Turnout object or null if none exists
     */
    protected Object getInstanceByUserName(String userName) {
        return _tuser.get(userName);
    }
    
    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @param systemName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    public NamedBean getBeanBySystemName(String systemName){
        return _tsys.get(systemName);
    }
    
    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists.
     * @param userName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    public NamedBean getBeanByUserName(String userName){
        return _tuser.get(userName);
    }
    
    /**
     * Locate an instance based on a name.  Returns null if no
     * instance already exists.
     * @param name System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    public NamedBean getNamedBean(String name){
        NamedBean b = getBeanByUserName(name);
        if(b!=null) return b;
        return getBeanBySystemName(name);
    }
    
    /**
     * Method for a UI to delete a bean, the UI should first request a "CanDelete", then
     * if that comes back clear, or the user agrees with the actions, then a "DoDelete" can be called
     * which inform the listeners to delete the bean, then it will be deregistered and disposed of.
     * @param bean The NamedBean to be deleted
     * @param property The programmatic name of the property that is to be changed.
     *                 "CanDelete" will enquire with all listerners if the item can be deleted
     *                 "DoDelete" tells the listerner to delete the item
     * @throws PropertyVetoException - if the recipients wishes the delete to be aborted.
     */
    public void deleteBean(NamedBean bean, String property) throws java.beans.PropertyVetoException {
        try {
            fireVetoableChange(property, bean, null);
        } catch (java.beans.PropertyVetoException e) {
            throw e;
        }
        if(property.equals("DoDelete")){ //IN18N
            deregister(bean);
            bean.dispose();
        }
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific SignalHeadManagers
     * use this method extensively.
     */
    public void register(NamedBean s) {
        String systemName = s.getSystemName();
        _tsys.put(systemName, s);
        String userName = s.getUserName();
        if (userName != null) _tuser.put(userName, s);
        firePropertyChange("length", null, Integer.valueOf(_tsys.size()));
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this, "", "Manager");
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager
     * uses this method.
     */
    public void deregister(NamedBean s) {
        s.removePropertyChangeListener(this);
        String systemName = s.getSystemName();
        _tsys.remove(systemName);
        String userName = s.getUserName();
        if (userName != null) _tuser.remove(userName);
        firePropertyChange("length", null, Integer.valueOf(_tsys.size()));
        // listen for name and state changes to forward
    }

    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();  // OldValue is actually system name
            String now = (String) e.getNewValue();
            NamedBean t = (NamedBean)e.getSource();
            if (old!= null) _tuser.remove(old);
            if (now!= null) _tuser.put(now, t);
            
            //called DisplayListName, as DisplayName might get used at some point by a NamedBean
            firePropertyChange("DisplayListName", old, now); //IN18N
        }
    }

    public String[] getSystemNameArray() {
        String[] arr = new String[_tsys.size()];
        Enumeration<String> en = _tsys.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    }

    public List<String> getSystemNameList() {
        String[] arr = new String[_tsys.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _tsys.keys();
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    public List<NamedBean> getNamedBeanList() {
        return new ArrayList<NamedBean>(_tsys.values());
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
    
    java.beans.VetoableChangeSupport vcs = new java.beans.VetoableChangeSupport(this);
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vcs.addVetoableChangeListener(l);
    }
    public synchronized void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vcs.removeVetoableChangeListener(l);
    }
    
    /**
     * Method to inform all registered listerners of a vetoable change.
     * If the propertyName is "CanDelete" ALL listeners with an interest in the 
     * bean will throw an exception, which is recorded returned back to the invoking
     * method, so that it can be presented back to the user.  However if a listener 
     * decides that the bean can not be deleted then it should throw an exception with 
     * a property name of "DoNotDelete", this is thrown back up to the user and the 
     * delete process should be aborted.
     * @param p The programmatic name of the property that is to be changed.
     *                 "CanDelete" will enquire with all listerners if the item can be deleted.
     *                 "DoDelete" tells the listerner to delete the item.
     * @param old The old value of the property.
     * @param new The new value of the property.
     * @throws PropertyVetoException - if the recipients wishes the delete to be aborted.
     */
    protected void fireVetoableChange(String p, Object old, Object n) throws java.beans.PropertyVetoException {
        java.beans.PropertyChangeEvent evt = new java.beans.PropertyChangeEvent(this, p, old, n);
        if(p.equals("CanDelete")){ //IN18N
            StringBuilder message = new StringBuilder();
            for(java.beans.VetoableChangeListener vc : vcs.getVetoableChangeListeners()){
                log.info(""+vc);
                try {
                    vc.vetoableChange(evt);
                } catch (java.beans.PropertyVetoException e) {
                    if(e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")){ //IN18N
                        log.info(e.getMessage());
                        throw e;
                    }
                    message.append(e.getMessage());
                    message.append("<br><br>"); //IN18N
                }
            }
            throw new java.beans.PropertyVetoException(message.toString(), evt);
        } else {
            try {
                vcs.fireVetoableChange(evt);
            } catch (java.beans.PropertyVetoException e) {
                e.printStackTrace();
            }
        }
    }
    
    static Logger log = LoggerFactory.getLogger(AbstractManager.class.getName());

}

/* @(#)AbstractManager.java */
