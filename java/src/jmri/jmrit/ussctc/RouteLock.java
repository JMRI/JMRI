package jmri.jmrit.ussctc;

import jmri.*;

import java.util.*;

/**
 * Lock if any of the SignalHeads controlling traffic over a turnout are not at stop.
 * <p>
 * This checks SignalHeads for RED; it locks against Restricting (FLASHRED) but you can
 * change that by overriding the checkSignalClear() method.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class RouteLock implements Lock {

    /**
     * @param list SignalHeads that cover this route
     */
    public RouteLock(List<NamedBeanHandle<SignalHead>> list) {
        this.list = list;
        this.beans = null;
    }
    
    /**
     * @param list SignalHeads that cover this route
     * @param beans Defines the specific route
     */
    public RouteLock(List<NamedBeanHandle<SignalHead>> list, List<BeanSetting> beans) {
        this.list = list;
        this.beans = beans;
    }
    
    /**
     * @param array User or system names of SignalHeads that cover this route
     */
    public RouteLock(String[] array) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        SignalHeadManager sm = InstanceManager.getDefault(SignalHeadManager.class);

        list = new ArrayList<>();
        for (String s : array) list.add(hm.getNamedBeanHandle(s, sm.getSignalHead(s)));
        
        this.beans = null;
    }

    /**
     * @param array User or system names of SignalHeads that cover this route
     * @param beans Defines the specific route
     */
    public RouteLock(String[] array, BeanSetting[] beans) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        SignalHeadManager sm = InstanceManager.getDefault(SignalHeadManager.class);

        list = new ArrayList<>();
        for (String s : array) list.add(hm.getNamedBeanHandle(s, sm.getSignalHead(s)));
        
        this.beans = new ArrayList<>();
        for (BeanSetting bean : beans) this.beans.add(bean);
        
    }

    /**
     * @param head User or system name of a SignalHead that covers this route
     */
    public RouteLock(String head) {
        this(new String[]{head});
    }

    List<NamedBeanHandle<SignalHead>> list; 
    List<BeanSetting> beans;
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    public boolean isLockClear() {
        // if this route isn't in effect, then permitted
        if (beans != null) {
            for (BeanSetting bean : beans) {
                if ( ! bean.check()) return true;
            }
        }
        
        for (NamedBeanHandle<SignalHead> handle : list) {
            if ( isSignalClear(handle) ) return false;
        }
        return true;
    }
    
    boolean isSignalClear(NamedBeanHandle<SignalHead> handle) {
        return handle.getBean().getState() != SignalHead.RED;
    }
}
