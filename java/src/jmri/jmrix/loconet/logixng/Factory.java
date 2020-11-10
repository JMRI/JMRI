package jmri.jmrix.loconet.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalExpressionFactory;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for LogixNG LocoNet classes.
 */
@ServiceProvider(service = DigitalExpressionFactory.class)
public class Factory implements DigitalExpressionFactory {

    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    public static final LocoNet LOCONET = new LocoNet();
    
    
    /**
     * Do we have a LocoNet connection?
     * @return true if we have LocoNet, false otherwise
     */
    private static boolean hasLocoNet() {
        List<LocoNetSystemConnectionMemo> list = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        
        // We have at least one LocoNet connection if the list is not empty
        return !list.isEmpty();
    }
    
    @Override
    public void init() {
        // We don't want to add these classes if we don't have a LocoNet connection
        if (hasLocoNet()) {
            Category.registerCategory(LOCONET);
        }
    }
    
    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> getExpressionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> expressionClasses = new HashSet<>();
        
        // We don't want to add these classes if we don't have a LocoNet connection
        if (hasLocoNet()) {
            expressionClasses.add(new AbstractMap.SimpleEntry<>(LOCONET, ExpressionSlotUsage.class));
        }
        
        return expressionClasses;
    }
    
    
    
    public static final class LocoNet extends Category {
        
        public LocoNet() {
            super("LOCONET", Bundle.getMessage("MenuLocoNet"), 300);
        }
    }

}
