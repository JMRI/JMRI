package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import jmri.jmrit.logixng.AnalogActionFactory;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.LogixNG_Category;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for AnalogAction classes.
 */
@ServiceProvider(service = AnalogActionFactory.class)
public class AnalogFactory implements AnalogActionFactory {

    @Override
    public Set<Map.Entry<LogixNG_Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<LogixNG_Category, Class<? extends Base>>> analogActionClasses =
                Set.of(new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, AnalogActionLightIntensity.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, AnalogActionMemory.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, AnalogMany.class)
                );
        
        return analogActionClasses;
    }

}
