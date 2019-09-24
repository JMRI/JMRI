package jmri.jmrit.logixng.digital.actions_with_change;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionWithChangesFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = DigitalActionWithChangesFactory.class)
public class Factory implements DigitalActionWithChangesFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> digitalActionWithChangeClasses = new HashSet<>();
        digitalActionWithChangeClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, OnChangeAction.class));
        return digitalActionWithChangeClasses;
    }

}
