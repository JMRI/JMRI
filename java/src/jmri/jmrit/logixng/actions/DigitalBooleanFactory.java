package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
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
public class DigitalBooleanFactory implements DigitalBooleanActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> digitalBooleanActionClasses =
                Set.of(
                        new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalBooleanMany.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalBooleanOnChange.class)
                );
        
        return digitalBooleanActionClasses;
    }

}
