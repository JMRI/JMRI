package jmri.jmrix.loconet.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.StringActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = StringActionFactory.class)
public class SAFactory implements StringActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> stringActionClasses = new HashSet<>();
        
        // We don't want to add these classes if we don't have a LocoNet connection
        if (Common.hasLocoNet()) {
            stringActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, StringActionLocoNet_OPC_PEER.class));
        }
        
        return stringActionClasses;
    }

}
