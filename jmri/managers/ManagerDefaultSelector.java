// ManagerDefaultSelector.java

package jmri.managers;

import java.util.*;

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
 * @version			$Revision: 1.6 $
 * @since           2.9.4
 */
public class ManagerDefaultSelector {

    public static final ManagerDefaultSelector instance = new ManagerDefaultSelector();

    private ManagerDefaultSelector() {
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
            for (int x = 0; x<connList.size(); x++) {
                jmri.jmrix.SystemConnectionMemo memo = (jmri.jmrix.SystemConnectionMemo)connList.get(x);
                String testName = memo.getUserName();
                if (testName.equals(connectionName)) {
                    // match, store
                    InstanceManager.setDefault(c, memo.get(c));
                    break;
                }
            }
        }
    }
    
    public Hashtable<Class<?>, String> defaults = new Hashtable<Class<?>, String>();
    
    final public Item[] knownManagers = new Item[] {
                //new Item("Turnouts", TurnoutManager.class, true),
                //new Item("Sensors", SensorManager.class, true),
                //new Item("Throttles", ThrottleManager.class, false),
                //new Item("<html>Power<br>Control</html>", PowerManager.class, false),
                //new Item("Command Station", CommandStation.class, false)
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
}

/* @(#)ManagerDefaultSelector.java */
