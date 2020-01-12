package jmri.jmrit.logixng.digital.expressions;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.Category;
import org.openide.util.lookup.ServiceProvider;
import jmri.jmrit.logixng.DigitalExpressionFactory;
import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * The factory for DigitalExpressionBean classes.
 */
@ServiceProvider(service = DigitalExpressionFactory.class)
public class Factory implements DigitalExpressionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> getExpressionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> expressionClasses = new HashSet<>();
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, And.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, Antecedent.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionLight.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionSensor.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionTurnout.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, False.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, Hold.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, Or.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, ResetOnTrue.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, ExpressionTimer.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, TriggerOnce.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, True.class));
        return expressionClasses;
    }

}
