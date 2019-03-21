package jmri.jmrit.ussctc;

import java.util.*;
import javax.annotation.Nonnull;
import jmri.*;

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
    public RouteLock(@Nonnull List<NamedBeanHandle<SignalHead>> list) {
        this.list = list;
        this.beans = null;
    }
    
    /**
     * @param list SignalHeads that cover this route
     * @param beans Defines the specific route
     */
    public RouteLock(@Nonnull List<NamedBeanHandle<SignalHead>> list, @Nonnull List<BeanSetting> beans) {
        this.list = list;
        this.beans = beans;
    }
    
    /**
     * @param array User or system names of SignalHeads that cover this route
     */
    public RouteLock(@Nonnull String[] array) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        SignalHeadManager sm = InstanceManager.getDefault(SignalHeadManager.class);

        ArrayDeque<NamedBeanHandle<SignalHead>> q = new ArrayDeque<>();
        for (String s : array) {
            SignalHead sig = sm.getSignalHead(s);
            if (sig != null) {
                q.add(hm.getNamedBeanHandle(s, sig));
            }
        }
        this.list = q;
        this.beans = null;
    }

    /**
     * @param array User or system names of SignalHeads that cover this route
     * @param beans Defines the specific route
     */
    public RouteLock(@Nonnull String[] array, @Nonnull BeanSetting[] beans) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        SignalHeadManager sm = InstanceManager.getDefault(SignalHeadManager.class);

        ArrayDeque<NamedBeanHandle<SignalHead>> q1 = new ArrayDeque<>();
        for (String s : array) {
            SignalHead sig = sm.getSignalHead(s);
            if (sig != null) {
                q1.add(hm.getNamedBeanHandle(s, sig));
            }
        }
        this.list = q1;
        
        ArrayDeque<BeanSetting> q2 = new ArrayDeque<>();
        for (BeanSetting bean : beans) {
            q2.add(bean);
        }
        this.beans = q2;
        
    }

    /**
     * @param head User or system name of a SignalHead that covers this route
     */
    public RouteLock(@Nonnull String head) {
        this(new String[]{head});
    }

    Iterable<NamedBeanHandle<SignalHead>> list; 
    Iterable<BeanSetting> beans;
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    @Override
    public boolean isLockClear() {
        // if this route isn't in effect, then permitted
        if (beans != null) {
            for (BeanSetting bean : beans) {
                if ( ! bean.check()) {
                    lockLogger.setStatus(this, "");
                    return true;
                }
            }
        }
        
        for (NamedBeanHandle<SignalHead> handle : list) {
            if ( isSignalClear(handle) ) {
                lockLogger.setStatus(this, "Locked due to route including signal "+handle.getBean().getDisplayName());
                return false;
            }
        }
        lockLogger.setStatus(this, "");
        return true;
    }
    
    boolean isSignalClear(@Nonnull NamedBeanHandle<SignalHead> handle) {
        return handle.getBean().getState() != SignalHead.RED;
    }
}
