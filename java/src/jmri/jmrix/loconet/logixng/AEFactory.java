package jmri.jmrix.loconet.logixng;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.AnalogExpressionFactory;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = AnalogExpressionFactory.class)
public class AEFactory implements AnalogExpressionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> analogExpressionClasses = new HashSet<>();
        analogExpressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, AnalogExpressionLocoNet_OPC_PEER.class));
        return analogExpressionClasses;
    }

}
