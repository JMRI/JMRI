package jmri.jmrit.ussctc;

import java.util.*;
import jmri.*;

/**
 * Lock if any of the SignalHeadSections controlling traffic are running time.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class TimeLock implements Lock {

    /**
     * @param list SignalHeadSections that cover this route
     */
    public TimeLock(List<SignalHeadSection> list) {
        this.list = list;
        this.beans = null;
    }
    
    /**
     * @param list SignalHeadSections that cover this route
     * @param beans Defines the specific route
     */
    public TimeLock(List<SignalHeadSection> list, List<BeanSetting> beans) {
        this.list = list;
        this.beans = beans;
    }
    
    /**
     * @param array SignalHeadSections that cover this route
     * @param beans Defines the specific route
     */
    public TimeLock(SignalHeadSection[] array, BeanSetting[] beans) {
        list = new ArrayList<>();
        for (SignalHeadSection s : array) list.add(s);
        
        this.beans = new ArrayList<>();
        for (BeanSetting bean : beans) this.beans.add(bean);
        
    }

    /**
     * @param array SignalHeadSections that cover this route
     */
    public TimeLock(SignalHeadSection[] array) {
        list = new ArrayList<>();
        for (SignalHeadSection s : array) list.add(s);
        
        this.beans = null;   
    }

    /**
     * @param head SignalHeadSection that covers this route
     */
    public TimeLock(SignalHeadSection head) {
        this(new SignalHeadSection[]{head});
    }

    List<SignalHeadSection> list; 
    List<BeanSetting> beans;
    
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
        
        for (SignalHeadSection section : list) {
            if (section.isRunningTime()) {
                lockLogger.setStatus(this, "Locked: "+section.getStation()+" running time");
                return false;
            }
        }
        lockLogger.setStatus(this, "");
        return true;
    }
    
}
