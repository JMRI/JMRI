package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = DigitalActionFactory.class)
public class Factory implements DigitalActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses() {
        Set<Map.Entry<Category, Class<? extends Base>>> digitalActionClasses = new HashSet<>();
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLight.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, ActionListenOnBeans.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLocalVariable.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionMemory.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionScript.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSensor.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionThrottle.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, ActionTimer.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionTurnout.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, CallModule.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, DoAnalogAction.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, DoStringAction.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, IfThenElse.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, Logix.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, Many.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.EXRAVAGANZA, ShutdownComputer.class));
        return digitalActionClasses;
    }

}
