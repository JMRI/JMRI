package jmri.jmrit.logixng.expressions;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.Category;
import jmri.jmrit.logixng.LogixNG_Category;

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
                new HashSet<>(      // Set.of() returns an immutable set
                        Set.of(new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, And.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, Antecedent.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, DigitalCallModule.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ConnectionName.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionAudio.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionBlock.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionClock.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionConditional.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionDispatcher.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionEntryExit.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionLight.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionLocalVariable.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionMemory.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionOBlock.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionPower.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionReference.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionReporter.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionScript.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionSection.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionSensor.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionSensorEdge.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionSignalHead.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionSignalMast.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionTransit.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionTurnout.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ExpressionWarrant.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, False.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, FileAsFlag.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, DigitalFormula.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, Hold.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, LastResultOfDigitalExpression.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, LogData.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, Not.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, Or.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, Timer.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, TriggerOnce.class),
                                new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, True.class)
                        ));

        if (jmri.util.SystemType.isLinux()) {
            expressionClasses.add(new AbstractMap.SimpleEntry<>(LogixNG_Category.LINUX, ExpressionLinuxLinePower.class));
        }

        return expressionClasses;
    }

}
