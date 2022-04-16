package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.StringActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for StringAction classes.
 */
@ServiceProvider(service = StringActionFactory.class)
public class StringFactory implements StringActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> stringActionClasses =
                Set.of(
                        new AbstractMap.SimpleEntry<>(Category.ITEM, StringActionMemory.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, StringActionStringIO.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, StringMany.class)
                );
        
        return stringActionClasses;
    }

}
