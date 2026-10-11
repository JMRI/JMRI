package jmri.jmrit.logixng.expressions;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import jmri.jmrit.logixng.AnalogExpressionFactory;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.LogixNG_Category;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = AnalogExpressionFactory.class)
public class AnalogFactory implements AnalogExpressionFactory {

    @Override
    public Set<Map.Entry<LogixNG_Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<LogixNG_Category, Class<? extends Base>>> analogExpressionClasses =
                Set.of(new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, AnalogExpressionAnalogIO.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, AnalogExpressionConstant.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, AnalogExpressionLocalVariable.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, AnalogExpressionMemory.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, AnalogFormula.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, TimeSinceMidnight.class)
                );

        return analogExpressionClasses;
    }

}
