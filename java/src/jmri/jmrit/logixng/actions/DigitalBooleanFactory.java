package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import jmri.Category;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.LogixNG_Category;

import org.openide.util.lookup.ServiceProvider;

import jmri.jmrit.logixng.DigitalBooleanActionFactory;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = DigitalBooleanActionFactory.class)
public class DigitalBooleanFactory implements DigitalBooleanActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> digitalBooleanActionClasses =
                Set.of(new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, DigitalBooleanMany.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, DigitalBooleanLogixAction.class)
                );

        return digitalBooleanActionClasses;
    }

}
