package jmri.jmrix.loconet.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionFactory;
import jmri.jmrit.logixng.DigitalActionBean;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for LogixNG LocoNet classes.
 */
@ServiceProvider(service = DigitalActionFactory.class)
public class ActionFactory implements DigitalActionFactory {

    @Override
    public void init() {
        CategoryLocoNet.registerCategory();
    }
    
    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> getActionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> actionClasses = new HashSet<>();
        
        // We don't want to add these classes if we don't have a LocoNet connection
        if (CategoryLocoNet.hasLocoNet()) {
            actionClasses.add(new AbstractMap.SimpleEntry<>(CategoryLocoNet.LOCONET, ActionClearSlots.class));
            actionClasses.add(new AbstractMap.SimpleEntry<>(CategoryLocoNet.LOCONET, ActionUpdateSlots.class));
        }
        
        return actionClasses;
    }
    
}
