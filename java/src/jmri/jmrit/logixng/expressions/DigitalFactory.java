package jmri.jmrit.logixng.expressions;

import java.util.AbstractMap;
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
public class DigitalFactory implements DigitalExpressionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> getExpressionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> expressionClasses =
                Set.of(
                        new AbstractMap.SimpleEntry<>(Category.COMMON, And.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, Antecedent.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, DigitalCallModule.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionBlock.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionClock.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionConditional.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionDispatcher.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionEntryExit.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionLight.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionLocalVariable.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionMemory.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionOBlock.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionPower.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionReference.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionReporter.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionScript.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionSensor.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionSignalHead.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionSignalMast.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionTurnout.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionWarrant.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, False.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalFormula.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, Hold.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, LastResultOfDigitalExpression.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, LogData.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, Not.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, Or.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, TriggerOnce.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, True.class)
                );
        
        return expressionClasses;
    }

}
