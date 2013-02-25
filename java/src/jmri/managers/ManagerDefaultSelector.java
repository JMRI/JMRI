// ManagerDefaultSelector.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import jmri.*;

/**
 * Records and executes a desired set of defaults
 * for the JMRI InstanceManager and ProxyManagers
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2010
 * @version			$Revision$
 * @since           2.9.4
 */
public class ManagerDefaultSelector {

    public static final ManagerDefaultSelector instance = new ManagerDefaultSelector();
    
    /*public static synchronized ManagerDefaultSelector instance() {
        if (instance == null) {
            if (log.isDebugEnabled()) log.debug("Manager Default Selector creating instance");
            // create and load
            instance = new ManagerDefaultSelector();
        }
        if (log.isDebugEnabled()) log.debug("ManagerDefaultSelector returns instance "+instance);
        return instance;
    }*/

    private ManagerDefaultSelector() {
        jmri.jmrix.SystemConnectionMemo.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                if(e.getPropertyName().equals("ConnectionNameChanged")){
                    String oldName = (String) e.getOldValue();
                    String newName = (String) e.getNewValue();
                    for (Class<?> c : defaults.keySet()) {
                        String connectionName = ManagerDefaultSelector.instance.defaults.get(c);
                        if(connectionName.equals(oldName))
                            ManagerDefaultSelector.instance.defaults.put(c, newName);
                    }
                } else if (e.getPropertyName().equals("ConnectionDisabled")){
                    Boolean newState = (Boolean) e.getNewValue();
                    if(newState){
                        jmri.jmrix.SystemConnectionMemo memo = (jmri.jmrix.SystemConnectionMemo)e.getSource();
                        String disabledName = memo.getUserName();
                        ArrayList<Class<?>> tmpArray = new ArrayList<Class<?>>();
                        for (Class<?> c : defaults.keySet()) {
                            String connectionName = ManagerDefaultSelector.instance.defaults.get(c);
                            if(connectionName.equals(disabledName)){
                                log.warn("Connection " + disabledName + " has been disabled, we shall remove it as the default for " + c);
                                tmpArray.add(c);
//                                ManagerDefaultSelector.instance.defaults.remove(c);
                            }
                        }
                        for(int i = 0; i<tmpArray.size(); i++){
                            ManagerDefaultSelector.instance.defaults.remove(tmpArray.get(i));
                        }
                    }
                } else if (e.getPropertyName().equals("ConnectionRemoved")){
                    String removedName = (String) e.getOldValue();
                    ArrayList<Class<?>> tmpArray = new ArrayList<Class<?>>();
                    for (Class<?> c : defaults.keySet()) {
                        String connectionName = ManagerDefaultSelector.instance.defaults.get(c);
                        if(connectionName.equals(removedName)){
                            log.warn("Connection " + removedName + " has been removed, we shall remove it as the default for " + c);
                            //ManagerDefaultSelector.instance.defaults.remove(c);
                            tmpArray.add(c);
                        }
                    }
                    for(int i = 0; i<tmpArray.size(); i++){
                        ManagerDefaultSelector.instance.defaults.remove(tmpArray.get(i));
                    }
                }
                notifyPropertyChangeListener("Updated", null, null);
            }
        });
    }
    
    /**
     * Return the userName of the system
     * that provides the default instance
     * for a specific class.
     * @param managerClass the specific type, e.g. TurnoutManager,
     *          for which a default system is desired
     * @return userName of the system, or null if none set
     */
    public String getDefault(Class<?> managerClass) {
        return defaults.get(managerClass);
    }

    /**
     * Record the userName of the system
     * that provides the default instance
     * for a specific class.
     * @param managerClass the specific type, e.g. TurnoutManager,
     *          for which a default system is desired
     * @param userName of the system, or null if none set
     */
    public void setDefault(Class<?> managerClass, String userName) {
        defaults.put(managerClass, userName);
    }

    /** 
     * load into InstanceManager
     */
    @SuppressWarnings("unchecked")
    public void configure() {
        List<Object> connList = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
        if (connList == null) return; // nothing to do 
        
        for (Class c : defaults.keySet()) {
            // 'c' is the class to load
            String connectionName = ManagerDefaultSelector.instance.defaults.get(c);
            // have to find object of that type from proper connection
            boolean found = false;
            for (int x = 0; x<connList.size(); x++) {
                jmri.jmrix.SystemConnectionMemo memo = (jmri.jmrix.SystemConnectionMemo)connList.get(x);
                String testName = memo.getUserName();
                if (testName.equals(connectionName)) {
                    found = true;
                    // match, store
                    InstanceManager.setDefault(c, memo.get(c));
                    break;
                }
            }
            /*
             * If the set connection can not be found then we shall set the manager default to use what
             * has currently been set.
             */
            if(!found){
                String currentName = null;
                if(c == ThrottleManager.class && InstanceManager.throttleManagerInstance()!=null){
                    currentName = InstanceManager.throttleManagerInstance().getUserName();
                } else if(c==PowerManager.class && InstanceManager.powerManagerInstance()!=null){
                    currentName = InstanceManager.powerManagerInstance().getUserName();
                } else if (c==ProgrammerManager.class && InstanceManager.programmerManagerInstance()!=null){
                    currentName = InstanceManager.programmerManagerInstance().getUserName();
                }
                if(currentName!=null){
                    log.warn("The configured " + connectionName + " for " + c + " can not be found so will use the default " + currentName);
                    ManagerDefaultSelector.instance.defaults.put(c, currentName);
                }
            }
        }
    }
    
    public Hashtable<Class<?>, String> defaults = new Hashtable<Class<?>, String>();
    
    final public Item[] knownManagers = new Item[] {
//                new Item("Clock", ClockControl.class, true),
//                new Item("Turnouts", TurnoutManager.class, true),
//                new Item("Lights", LightManager.class, true),
//                new Item("Sensors", SensorManager.class, true),
                new Item("Throttles", ThrottleManager.class, false),
                new Item("<html>Power<br>Control</html>", PowerManager.class, false),
                new Item("<html>Command<br>Station</html>", CommandStation.class, false),
                new Item("Programmer", ProgrammerManager.class, false)
    };
    
    public static class Item {
        public String typeName;
        public Class<?> managerClass;
        public boolean proxy;
        Item(String typeName, Class<?> managerClass, boolean proxy) {
            this.typeName = typeName;
            this.managerClass = managerClass;
            this.proxy = proxy;
        }
    }
    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }
    
    /**
     * Trigger the notification of all PropertyChangeListeners
     */
    @SuppressWarnings("unchecked")
	protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this)
            {
                v = (Vector<PropertyChangeListener>) listeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }
    
    // data members to hold contact with the property listeners
    final private static Vector<PropertyChangeListener> listeners = new Vector<PropertyChangeListener>();
    
    static Logger log = LoggerFactory.getLogger(ManagerDefaultSelector.class.getName());
}

/* @(#)ManagerDefaultSelector.java */
