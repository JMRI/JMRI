package jmri.jmrix.loconet.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.AnalogActionFactory;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for AnalogAction classes.
 */
@ServiceProvider(service = AnalogActionFactory.class)
public class AAFactory implements AnalogActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> analogActionClasses = new HashSet<>();
        
        // We don't want to add these classes if we don't have a LocoNet connection
        if (Common.hasLocoNet()) {
            analogActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, AnalogActionLocoNet_OPC_PEER.class));
        }
        
        return analogActionClasses;
    }

}
