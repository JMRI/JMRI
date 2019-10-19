package jmri.jmrit.logixng.digital.boolean_actions;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import org.openide.util.lookup.ServiceProvider;
import jmri.jmrit.logixng.DigitalBooleanActionFactory;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = DigitalBooleanActionFactory.class)
public class Factory implements DigitalBooleanActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> digitalActionWithChangeClasses = new HashSet<>();
        digitalActionWithChangeClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, OnChange.class));
        return digitalActionWithChangeClasses;
    }

}
