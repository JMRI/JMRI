package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
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
        Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> digitalActionClasses =
                Set.of(
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionAudio.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionBlock.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionClock.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionClockRate.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ActionCreateBeansFromTable.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionDispatcher.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionEntryExit.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ActionFindTableRowOrColumn.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLight.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLightIntensity.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ActionListenOnBeans.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ActionListenOnBeansLocalVariable.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ActionListenOnBeansTable.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionLocalVariable.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionMemory.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionOBlock.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionPower.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionRequestUpdateAllSensors.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionReporter.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionRequestUpdateOfSensor.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionScript.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSensor.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSetReporter.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ActionShutDownTask.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSignalHead.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSignalMast.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionSound.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionTable.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionThrottle.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionThrottleFunction.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, ActionTimer.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionTurnout.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionTurnoutLock.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ActionWarrant.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, Break.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, Continue.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, DigitalCallModule.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalFormula.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, DoAnalogAction.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, DoStringAction.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, EnableLogix.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, EnableLogixNG.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, Error.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ExecuteAction.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, ExecuteDelayed.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, Exit.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, For.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, ForEach.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, IfThenElse.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, JsonDecode.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, Logix.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, LogData.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, LogLocalVariables.class),
                        new AbstractMap.SimpleEntry<>(Category.COMMON, DigitalMany.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, ProgramOnMain.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, Return.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, RunOnce.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, Sequence.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ShowDialog.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, ShutdownComputer.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, SimulateTurnoutFeedback.class),
                        new AbstractMap.SimpleEntry<>(Category.FLOW_CONTROL, TableForEach.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, Timeout.class),
                        new AbstractMap.SimpleEntry<>(Category.ITEM, TriggerRoute.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, WebBrowser.class),
                        new AbstractMap.SimpleEntry<>(Category.OTHER, WebRequest.class)
                );

        return digitalActionClasses;
    }

}
