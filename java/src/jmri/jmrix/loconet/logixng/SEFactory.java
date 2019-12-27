package jmri.jmrix.loconet.logixng;

import jmri.jmrit.logixng.string.expressions.*;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.StringExpressionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = StringExpressionFactory.class)
public class SEFactory implements StringExpressionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> stringExpressionClasses = new HashSet<>();
        stringExpressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, StringExpressionLocoNet_OPC_PEER.class));
        return stringExpressionClasses;
    }

}
