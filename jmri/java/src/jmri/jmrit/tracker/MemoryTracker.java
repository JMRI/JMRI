package jmri.jmrit.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Block;
import jmri.Memory;
import jmri.NamedBeanHandle;


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
        Memory m = jmri.InstanceManager.memoryManagerInstance()
            .provideMemory(namePrefix+block.getSystemName());
        namedMemory = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(namePrefix+block.getSystemName(), m);
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
        namedMemory.getBean().setValue(o);
    }
    
    NamedBeanHandle<Memory> namedMemory;
    //Memory m;
    Block block;
    
    static Logger log = LoggerFactory.getLogger(MemoryTracker.class.getName());
}
