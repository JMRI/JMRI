package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * The factory for DigitalAction classes.
 */
@ServiceProvider(service = DigitalActionFactory.class)
public class DigitalFactory implements DigitalActionFactory {

    @Override
    public Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> getActionClasses() {
        Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> digitalActionClasses = new HashSet<>();
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionAudio.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionBlock.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionClock.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionDispatcher.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionEntryExit.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLight.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLightIntensity.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, ActionListenOnBeans.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, ActionListenOnBeansTable.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLocalVariable.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionMemory.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionOBlock.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionPower.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionReporter.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionScript.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSensor.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSignalHead.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSignalMast.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSound.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionThrottle.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, ActionTimer.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionTurnout.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionTurnoutLock.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, ActionWarrant.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, DigitalCallModule.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalFormula.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, DoAnalogAction.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, DoStringAction.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, EnableLogix.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, ExecuteDelayed.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, For.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, IfThenElse.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, Logix.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, LogData.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, LogLocalVariables.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalMany.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, Sequence.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, ShowDialog.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, ShutdownComputer.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.COMMON, TableForEach.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, Timeout.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.ITEM, TriggerRoute.class));
        digitalActionClasses.add(new AbstractMap.SimpleEntry<>(Category.OTHER, WebBrowser.class));
        return digitalActionClasses;
    }

}
