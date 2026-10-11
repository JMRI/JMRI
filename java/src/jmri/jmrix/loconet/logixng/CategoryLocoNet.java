package jmri.jmrix.loconet.logixng;

import java.util.List;

import jmri.jmrit.logixng.LogixNG_Category;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Defines the category LocoNet
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public final class CategoryLocoNet extends LogixNG_Category {
    
    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    public static final CategoryLocoNet LOCONET = new CategoryLocoNet();
    
    
    public CategoryLocoNet() {
        super("LOCONET", Bundle.getMessage("MenuLocoNet"), 300);
    }
    
    public static void registerCategory() {
        // We don't want to add these classes if we don't have a LocoNet connection
        if (hasLocoNet() && !LogixNG_Category.values().contains(LOCONET)) {
            LogixNG_Category.registerCategory(LOCONET);
        }
    }
    
    /**
     * Do we have a LocoNet connection?
     * @return true if we have LocoNet, false otherwise
     */
    public static boolean hasLocoNet() {
        List<LocoNetSystemConnectionMemo> list = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        
        // We have at least one LocoNet connection if the list is not empty
        return !list.isEmpty();
    }
    
}
