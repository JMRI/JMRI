package jmri.jmrit.logixng.expressions;

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
public class DigitalFactory implements DigitalExpressionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> getExpressionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> expressionClasses = new HashSet<>();
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, And.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, Antecedent.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, DigitalCallModule.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionBlock.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionClock.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionConditional.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionEntryExit.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionLight.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionLocalVariable.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionMemory.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionOBlock.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionPower.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionReference.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionScript.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionSensor.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionSignalHead.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionSignalMast.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionTurnout.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ExpressionWarrant.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, False.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalFormula.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, Hold.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, LastResultOfDigitalExpression.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, Not.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, Or.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, TriggerOnce.class));
        expressionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, True.class));
        return expressionClasses;
    }

}
