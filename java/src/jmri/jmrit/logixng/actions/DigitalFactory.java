package jmri.jmrit.logixng.actions;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import jmri.Category;
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
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionAudio.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionBlock.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionClock.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionClockRate.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ActionCreateBeansFromTable.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionDispatcher.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionEntryExit.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ActionFindTableRowOrColumn.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionLight.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionLightIntensity.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ActionListenOnBeans.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ActionListenOnBeansLocalVariable.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ActionListenOnBeansTable.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionLocalVariable.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionMemory.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionOBlock.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionPower.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionRequestUpdateAllSensors.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionReporter.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionRequestUpdateOfSensor.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionRequestUpdateOfTurnout.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionScript.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionSensor.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionSetReporter.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ActionShutDownTask.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionSignalHead.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionSignalMast.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionSound.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionTable.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionThrottle.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionThrottleFunction.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, ActionTimer.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionTurnout.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionTurnoutLock.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ActionWarrant.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, Break.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, Continue.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, DigitalCallModule.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, DigitalFormula.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, DoAnalogAction.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, DoStringAction.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, EnableLogix.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, EnableLogixNG.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, Error.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ExecuteAction.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, ExecuteDelayed.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ExecuteProgram.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, Exit.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, For.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, ForEach.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, ForEachWithDelay.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, IfThenElse.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, JsonDecode.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, Logix.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, LogData.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, LogLocalVariables.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.COMMON, DigitalMany.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, ProgramOnMain.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, Return.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, RunOnce.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, Sequence.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ShowDialog.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, ShutdownComputer.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, SimulateTurnoutFeedback.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, TableForEach.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, Timeout.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.ITEM, TriggerRoute.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.FLOW_CONTROL, ValidationError.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, WebBrowser.class),
                        new AbstractMap.SimpleEntry<>(LogixNG_Category.OTHER, WebRequest.class)
                );

        return digitalActionClasses;
    }

}
