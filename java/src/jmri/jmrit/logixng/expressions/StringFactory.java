package jmri.jmrit.logixng.expressions;

import java.util.AbstractMap;
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
public class StringFactory implements StringExpressionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> stringExpressionClasses =
                Set.of(
                        new AbstractMap.SimpleEntry<>(Category.ITEM, StringExpressionConstant.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, StringExpressionMemory.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, StringFormula.class)
                );
        
        return stringExpressionClasses;
    }

}
