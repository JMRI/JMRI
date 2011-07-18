package jmri.jmrit.tracker;

import jmri.Block;
import jmri.Memory;


/**
 * Tracks train into memory object
 *
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version	$Revision$
 */
public class MemoryTracker  {

    /**
     * Create a Tracker object, providing a list of blocks
     * to watch
     */
    public MemoryTracker(Block b, String namePrefix) {
        block = b;
        
        // make sure Memory objects exist & remember it
        m = jmri.InstanceManager.memoryManagerInstance()
            .provideMemory(namePrefix+block.getSystemName());
        // set listener in the block
        block.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                handleChange();
            }
        });
        
        // first update
        handleChange();
    }
    
    void handleChange() {
        if (log.isDebugEnabled() && (block.getValue()!=null)) log.debug("set value "+block.getValue()+" in block "+block.getSystemName());
        Object o = block.getValue();
        if (o!=null) o = o.toString();
        m.setValue(o);
    }
    
    Memory m;
    Block block;
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryTracker.class.getName());
}
