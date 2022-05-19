package jmri.jmrit.logixng.tools;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Logix;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.expressions.*;
import jmri.jmrit.logixng.util.TimerUnit;

/**
 * Imports Logixs to LogixNG
 *
 * @author Daniel Bergqvist 2019
 */
public class ImportConditional {

    private final jmri.Conditional _conditional;
    private final ConditionalNG _conditionalNG;
    private final boolean _dryRun;


    /**
     * Create instance of ImportConditional
     * @param logix         the parent Logix of the conditional to import
     * @param conditional   the Conditional to import
     * @param logixNG       the parent LogixNG that the new ConditionalNG will be added to
     * @param sysName       the system name of the new ConditionalNG
     * @param dryRun        true if import without creating any new beans,
     *                      false if to create new beans
     */
    public ImportConditional(
            jmri.Logix logix,
            Conditional conditional,
            LogixNG logixNG,
            String sysName,
            boolean dryRun) {

        _dryRun = dryRun;
        _conditional = conditional;
        String userName = conditional.getSystemName();
        if (conditional.getUserName() != null) {
            userName += ": " + conditional.getUserName();
        }

        if (!_dryRun) {
            ConditionalNG conditionalNG = null;
            int counter = 0;
            while ((conditionalNG == null) && counter < 100) {
                String name = counter > 0 ? " - " + Integer.toString(counter) : "";
                conditionalNG = InstanceManager.getDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class)
                        .createConditionalNG(logixNG, sysName, userName + name);
                counter++;
            }

            if (conditionalNG == null) throw new RuntimeException("Cannot create new ConditionalNG with name: \"" + userName + "\"");

            _conditionalNG = conditionalNG;
        } else {
            _conditionalNG = null;
        }
    }

    public ConditionalNG getConditionalNG() {
        return _conditionalNG;
    }

    public void doImport() throws SocketAlreadyConnectedException, JmriException {

        Logix logix = new Logix(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);

        logix.setExecuteOnChange(_conditional.getTriggerOnChange());

        Conditional.AntecedentOperator ao = _conditional.getLogicType();
        String antecedentExpression = _conditional.getAntecedentExpression();
        List<ConditionalVariable> conditionalVariables = _conditional.getCopyOfStateVariables();
        List<ConditionalAction> conditionalActions = _conditional.getCopyOfActions();

        DigitalExpressionBean expression;
        switch (ao) {
            case ALL_AND:
                expression = new And(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
                break;
            case ALL_OR:
                expression = new Or(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
                break;
            case MIXED:
                expression = new Antecedent(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
                ((Antecedent)expression).setAntecedent(antecedentExpression);
                break;
            default:
                return;
        }

        // Is the Conditional a RTXINITIALIZER?
        if ((conditionalVariables.size() == 1) && (conditionalVariables.get(0).getType().getItemType() == Conditional.ItemType.NONE)) {
            expression =
                    new TriggerOnce(InstanceManager.getDefault(DigitalExpressionManager.class)
                            .getAutoSystemName(), null);

            True trueExpression =
                    new True(InstanceManager.getDefault(DigitalExpressionManager.class)
                            .getAutoSystemName(), null);
            if (!_dryRun) {
                MaleSocket socket = InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(trueExpression);
                expression.getChild(0).connect(socket);
            }
        } else {
            buildExpression(expression, conditionalVariables);
        }

        DigitalBooleanMany many =
                new DigitalBooleanMany(InstanceManager.getDefault(
                        DigitalBooleanActionManager.class).getAutoSystemName(), null);

        buildAction(many, conditionalActions);

        if (!_dryRun) {
            MaleSocket expressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
            logix.getExpressionSocket().connect(expressionSocket);

            MaleSocket manySocket = InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(many);
            logix.getActionSocket().connect(manySocket);

            MaleSocket logixAction = InstanceManager.getDefault(DigitalActionManager.class).registerAction(logix);
            _conditionalNG.getChild(0).connect(logixAction);
        }
    }


    private void buildExpression(DigitalExpressionBean expression, List<ConditionalVariable> conditionalVariables)
            throws SocketAlreadyConnectedException, JmriException {

        for (int i=0; i < conditionalVariables.size(); i++) {
            jmri.ConditionalVariable cv = conditionalVariables.get(i);
            NamedBean nb = cv.getBean();
            AtomicBoolean isNegated = new AtomicBoolean(cv.isNegated());
            DigitalExpressionBean newExpression;
            switch (cv.getType().getItemType()) {
                case SENSOR:
                    Sensor sn = (Sensor)nb;
                    newExpression = getSensorExpression(cv, sn, isNegated);
                    break;
                case TURNOUT:
                    Turnout tn = (Turnout)nb;
                    newExpression = getTurnoutExpression(cv, tn, isNegated);
                    break;
                case MEMORY:
                    Memory my = (Memory)nb;
                    newExpression = getMemoryExpression(cv, my);
                    break;
                case LIGHT:
                    Light l = (Light)nb;
                    newExpression = getLightExpression(cv, l, isNegated);
                    break;
                case SIGNALHEAD:
                    SignalHead s = (SignalHead)nb;
                    newExpression = getSignalHeadExpression(cv, s, isNegated);
                    break;
                case SIGNALMAST:
                    SignalMast sm = (SignalMast)nb;
                    newExpression = getSignalMastExpression(cv, sm, isNegated);
                    break;
                case ENTRYEXIT:
                    DestinationPoints dp = (DestinationPoints)nb;
                    newExpression = getEntryExitExpression(cv, dp, isNegated);
                    break;
                case CONDITIONAL:
                    Conditional c = (Conditional)nb;
                    newExpression = getConditionalExpression(cv, c, isNegated);
                    break;
                case CLOCK:
                    newExpression = getFastClockExpression(cv, isNegated);
                    break;
                case WARRANT:
                    Warrant w = (Warrant)nb;
                    newExpression = getWarrantExpression(cv, w, isNegated);
                    break;
                case OBLOCK:
                    OBlock b = (OBlock)nb;
                    newExpression = getOBlockExpression(cv, b, isNegated);
                    break;
                default:
                    newExpression = null;
                    log.error("Unexpected type in ImportConditional.doImport(): {} -> {}", cv.getType().name(), cv.getType().getItemType().name());
                    break;
            }

            if (newExpression != null) {

                boolean doTriggerActions = cv.doTriggerActions();

                if (isNegated.get()) {  // Some expressions have already handled Not
                    Not notExpression = new Not(InstanceManager.getDefault(DigitalExpressionManager.class)
                            .getAutoSystemName(), null);

                    if (!_dryRun) {
                        MaleSocket newExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(newExpression);
                        newExpressionSocket.setListen(doTriggerActions);
                        doTriggerActions = true;    // We don't want the Not expression to disable listen.
                        notExpression.getChild(0).connect(newExpressionSocket);
                    }
                    newExpression = notExpression;
                }

                if (!_dryRun) {
                    MaleSocket newExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(newExpression);
                    newExpressionSocket.setListen(doTriggerActions);
                    expression.getChild(i).connect(newExpressionSocket);
                }
            } else {
                log.error("ImportConditional.doImport() did not created an expression for type: {} -> {}", cv.getType().name(), cv.getType().getItemType().name());
            }
        }
    }


    private void buildAction(DigitalBooleanMany many, List<ConditionalAction> conditionalActions)
            throws SocketAlreadyConnectedException, JmriException {

        for (int i=0; i < conditionalActions.size(); i++) {
            ConditionalAction ca = conditionalActions.get(i);

            DigitalBooleanOnChange.Trigger trigger;
            switch (ca.getOption()) {
                case Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE:
                    trigger = DigitalBooleanOnChange.Trigger.CHANGE_TO_TRUE;
                    break;

                case Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE:
                    trigger = DigitalBooleanOnChange.Trigger.CHANGE_TO_FALSE;
                    break;

                case Conditional.ACTION_OPTION_ON_CHANGE:
                    trigger = DigitalBooleanOnChange.Trigger.CHANGE;
                    break;

                default:
                    throw new InvalidConditionalActionException(
                            Bundle.getMessage("ActionBadTrigger", ca.getOption()));
            }

            DigitalBooleanActionBean booleanAction =
                    new DigitalBooleanOnChange(InstanceManager.getDefault(DigitalBooleanActionManager.class).getAutoSystemName(), null, trigger);

            buildAction(booleanAction, ca);

            if (!_dryRun) {
                MaleSocket newBooleanActionSocket = InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(booleanAction);
                many.getChild(i).connect(newBooleanActionSocket);
            }
        }
    }

    private void buildAction(DigitalBooleanActionBean action, ConditionalAction conditionalAction)
            throws SocketAlreadyConnectedException, JmriException {


        String reference = null;
        String devName = conditionalAction.getDeviceName();
        if (devName != null && devName.length() > 0 && devName.charAt(0) == '@') {
            reference = "{"+devName.substring(1)+"}";
        }

        NamedBean nb = conditionalAction.getBean();
//        System.err.format("nb: %s%n", nb == null ? null : nb.getSystemName());
        DigitalActionBean newAction;
        switch (conditionalAction.getType().getItemType()) {
            case SENSOR:
                Sensor sn = (Sensor)nb;
                newAction = getSensorAction(conditionalAction, sn, reference);
                break;
            case TURNOUT:
                Turnout tn = (Turnout)nb;
                newAction = getTurnoutAction(conditionalAction, tn, reference);
                break;
            case MEMORY:
                Memory my = (Memory)nb;
                newAction = getMemoryAction(conditionalAction, my, reference);
                break;
            case LIGHT:
                Light l = (Light)nb;
                newAction = getLightAction(conditionalAction, l, reference);
                break;
            case SIGNALHEAD:
                SignalHead s = (SignalHead)nb;
                newAction = getSignalHeadAction(conditionalAction, s, reference);
                break;
            case SIGNALMAST:
                SignalMast sm = (SignalMast)nb;
                newAction = getSignalMastAction(conditionalAction, sm, reference);
                break;
            case ENTRYEXIT:
                DestinationPoints dp = (DestinationPoints)nb;
                newAction = getEntryExitAction(conditionalAction, dp, reference);
                break;
            case WARRANT:
                Warrant w = (Warrant)nb;
                newAction = getWarrantAction(conditionalAction, w, reference);
                break;
            case OBLOCK:
                OBlock b = (OBlock)nb;
                newAction = getOBlockAction(conditionalAction, b, reference);
                break;

            case LOGIX:
                newAction = getEnableLogixAction(conditionalAction);
                break;

            case CLOCK:
                newAction = getClockAction(conditionalAction);
                break;

            case AUDIO:
                newAction = getAudioOrSoundAction(conditionalAction);
                break;

            case SCRIPT:
                newAction = getScriptAction(conditionalAction);
                break;

            case OTHER:
                Route r = (Route) nb;
                newAction = getRouteAction(conditionalAction, r, reference);
                break;

            default:
                newAction = null;
                log.warn("Unexpected type in ImportConditional.doImport(): {} -> {}", conditionalAction.getType(), conditionalAction.getType().getItemType());
                break;
        }

        if (newAction != null) {
            if (!_dryRun) {
                MaleSocket newActionSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(newAction);
                action.getChild(0).connect(newActionSocket);
            }
        } else {
            log.error("ImportConditional.doImport() did not created an action for type: {} -> {}", conditionalAction.getType(), conditionalAction.getType().getItemType());
        }
    }


    private DigitalExpressionBean getSensorExpression(
            @Nonnull ConditionalVariable cv,
            Sensor sn,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionSensor expression =
                new ExpressionSensor(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

//        System.err.format("Sensor: %s%n", sn == null ? null : sn.getSystemName());

        expression.getSelectNamedBean().setNamedBean(sn);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        switch (cv.getType()) {
            case SENSOR_ACTIVE:
                expression.setBeanState(ExpressionSensor.SensorState.Active);
                break;
            case SENSOR_INACTIVE:
                expression.setBeanState(ExpressionSensor.SensorState.Inactive);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadSensorType", cv.getType().toString()));
        }

        return expression;
    }


    private DigitalExpressionBean getTurnoutExpression(
            @Nonnull ConditionalVariable cv,
            Turnout tn,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionTurnout expression =
                new ExpressionTurnout(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(tn);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        switch (cv.getType()) {
            case TURNOUT_CLOSED:
                expression.setBeanState(ExpressionTurnout.TurnoutState.Closed);
                break;
            case TURNOUT_THROWN:
                expression.setBeanState(ExpressionTurnout.TurnoutState.Thrown);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadTurnoutType", cv.getType().toString()));
        }

        return expression;
    }


    private DigitalExpressionBean getMemoryExpression(
            @Nonnull ConditionalVariable cv, Memory my)
            throws JmriException {

        ExpressionMemory expression =
                new ExpressionMemory(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(my);

        switch (cv.getNum1()) {
            case ConditionalVariable.EQUAL:
                expression.setMemoryOperation(ExpressionMemory.MemoryOperation.Equal);
                break;
            case ConditionalVariable.LESS_THAN:
                expression.setMemoryOperation(ExpressionMemory.MemoryOperation.LessThan);
                break;
            case ConditionalVariable.LESS_THAN_OR_EQUAL:
                expression.setMemoryOperation(ExpressionMemory.MemoryOperation.LessThanOrEqual);
                break;
            case ConditionalVariable.GREATER_THAN:
                expression.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThan);
                break;
            case ConditionalVariable.GREATER_THAN_OR_EQUAL:
                expression.setMemoryOperation(ExpressionMemory.MemoryOperation.GreaterThanOrEqual);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadMemoryNum1", cv.getType().toString()));
        }

        Memory memory;
        switch (cv.getType()) {
            case MEMORY_EQUALS:
                expression.setCompareTo(ExpressionMemory.CompareTo.Value);
                expression.setCaseInsensitive(false);
                expression.setConstantValue(cv.getDataString());
                break;
            case MEMORY_EQUALS_INSENSITIVE:
                expression.setCompareTo(ExpressionMemory.CompareTo.Value);
                expression.setCaseInsensitive(true);
                expression.setConstantValue(cv.getDataString());
                break;
            case MEMORY_COMPARE:
                expression.setCompareTo(ExpressionMemory.CompareTo.Memory);
                expression.setCaseInsensitive(false);
                expression.getSelectOtherMemoryNamedBean().setNamedBean(cv.getDataString());
                memory = InstanceManager.getDefault(MemoryManager.class).getMemory(cv.getDataString());
                if (memory == null) {   // Logix allows the memory name in cv.getDataString() to be a system name without system prefix
                    memory = InstanceManager.getDefault(MemoryManager.class).provide(cv.getDataString());
                    expression.getSelectOtherMemoryNamedBean().setNamedBean(memory.getSystemName());
                }
                break;
            case MEMORY_COMPARE_INSENSITIVE:
                expression.setCompareTo(ExpressionMemory.CompareTo.Memory);
                expression.setCaseInsensitive(true);
                expression.getSelectOtherMemoryNamedBean().setNamedBean(cv.getDataString());
                memory = InstanceManager.getDefault(MemoryManager.class).getMemory(cv.getDataString());
                if (memory == null) {   // Logix allows the memory name in cv.getDataString() to be a system name without system prefix
                    memory = InstanceManager.getDefault(MemoryManager.class).provide(cv.getDataString());
                    expression.getSelectOtherMemoryNamedBean().setNamedBean(memory.getSystemName());
                }
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadMemoryType", cv.getType().toString()));
        }

        expression.setListenToOtherMemory(false);

        return expression;
    }


    private DigitalExpressionBean getLightExpression(
            @Nonnull ConditionalVariable cv,
            Light ln,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionLight expression =
                new ExpressionLight(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(ln);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        switch (cv.getType()) {
            case LIGHT_ON:
                expression.setBeanState(ExpressionLight.LightState.On);
                break;
            case LIGHT_OFF:
                expression.setBeanState(ExpressionLight.LightState.Off);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadLightType", cv.getType().toString()));
        }

        return expression;
    }


    private DigitalExpressionBean getSignalHeadExpression(
            @Nonnull ConditionalVariable cv,
            SignalHead s,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionSignalHead expression =
                new ExpressionSignalHead(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(s);

        ExpressionSignalHead.QueryType appearence =
                isNegated.get() ? ExpressionSignalHead.QueryType.NotAppearance
                : ExpressionSignalHead.QueryType.Appearance;

        switch (cv.getType()) {
            case SIGNAL_HEAD_RED:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.RED);
                break;
            case SIGNAL_HEAD_YELLOW:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.YELLOW);
                break;
            case SIGNAL_HEAD_GREEN:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.GREEN);
                break;
            case SIGNAL_HEAD_DARK:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.DARK);
                break;
            case SIGNAL_HEAD_FLASHRED:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.FLASHRED);
                break;
            case SIGNAL_HEAD_FLASHYELLOW:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.FLASHYELLOW);
                break;
            case SIGNAL_HEAD_FLASHGREEN:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.FLASHGREEN);
                break;
            case SIGNAL_HEAD_LUNAR:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.LUNAR);
                break;
            case SIGNAL_HEAD_FLASHLUNAR:
                expression.setQueryType(appearence);
                expression.setAppearance(SignalHead.FLASHLUNAR);
                break;
            case SIGNAL_HEAD_LIT:
                expression.setQueryType(isNegated.get() ? ExpressionSignalHead.QueryType.NotLit : ExpressionSignalHead.QueryType.Lit);
                break;
            case SIGNAL_HEAD_HELD:
                expression.setQueryType(isNegated.get() ? ExpressionSignalHead.QueryType.NotHeld : ExpressionSignalHead.QueryType.Held);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadSignalHeadType", cv.getType().toString()));
        }

        isNegated.set(false);   // We have already handled this

        return expression;
    }


    private DigitalExpressionBean getSignalMastExpression(
            @Nonnull ConditionalVariable cv,
            SignalMast sm,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionSignalMast expression =
                new ExpressionSignalMast(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(sm);

        ExpressionSignalMast.QueryType aspect =
                isNegated.get() ? ExpressionSignalMast.QueryType.NotAspect
                : ExpressionSignalMast.QueryType.Aspect;

        switch (cv.getType()) {
            case SIGNAL_MAST_ASPECT_EQUALS:
                expression.setQueryType(aspect);
                expression.setAspect(cv.getDataString());
                break;
            case SIGNAL_MAST_LIT:
                expression.setQueryType(isNegated.get() ? ExpressionSignalMast.QueryType.NotLit : ExpressionSignalMast.QueryType.Lit);
                break;
            case SIGNAL_MAST_HELD:
                expression.setQueryType(isNegated.get() ? ExpressionSignalMast.QueryType.NotHeld : ExpressionSignalMast.QueryType.Held);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadSignalMastType", cv.getType().toString()));
        }

        isNegated.set(false);   // We have already handled this

        return expression;
    }


    private DigitalExpressionBean getEntryExitExpression(
            @Nonnull ConditionalVariable cv,
            DestinationPoints dp,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionEntryExit expression =
                new ExpressionEntryExit(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(dp);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        switch (cv.getType()) {
            case ENTRYEXIT_ACTIVE:
                expression.setBeanState(ExpressionEntryExit.EntryExitState.Active);
                break;
            case ENTRYEXIT_INACTIVE:
                expression.setBeanState(ExpressionEntryExit.EntryExitState.Inactive);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadEntryExitType", cv.getType().toString()));
        }

        return expression;
    }


    private DigitalExpressionBean getConditionalExpression(
            @Nonnull ConditionalVariable cv,
            Conditional cn,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionConditional expression =
                new ExpressionConditional(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(cn);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        switch (cv.getType()) {
            case CONDITIONAL_TRUE:
                expression.setConditionalState(ExpressionConditional.ConditionalState.True);
                break;
            case CONDITIONAL_FALSE:
                expression.setConditionalState(ExpressionConditional.ConditionalState.False);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadConditionalType", cv.getType().toString()));
        }

        return expression;
    }


    private DigitalExpressionBean getFastClockExpression(
            @Nonnull ConditionalVariable cv,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionClock expression =
                new ExpressionClock(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        if (cv.getType() != Conditional.Type.FAST_CLOCK_RANGE) {
            throw new InvalidConditionalVariableException(
                    Bundle.getMessage("ConditionalBadFastClockType", cv.getType().toString()));
        }
                log.info("Found a clock range");

        expression.setType(ExpressionClock.Type.FastClock);
        expression.setRange(ConditionalVariable.fixMidnight(cv.getNum1()), ConditionalVariable.fixMidnight(cv.getNum2()));

        return expression;
    }


    private DigitalExpressionBean getWarrantExpression(
            @Nonnull ConditionalVariable cv,
            Warrant w,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionWarrant expression =
                new ExpressionWarrant(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        expression.getSelectNamedBean().setNamedBean(w);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        switch (cv.getType()) {
            case ROUTE_FREE:
                expression.setBeanState(ExpressionWarrant.WarrantState.RouteFree);
                break;
            case ROUTE_OCCUPIED:
                expression.setBeanState(ExpressionWarrant.WarrantState.RouteOccupied);
                break;
            case ROUTE_ALLOCATED:
                expression.setBeanState(ExpressionWarrant.WarrantState.RouteAllocated);
                break;
            case ROUTE_SET:
                expression.setBeanState(ExpressionWarrant.WarrantState.RouteSet);
                break;
            case TRAIN_RUNNING:
                expression.setBeanState(ExpressionWarrant.WarrantState.TrainRunning);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadWarrantType", cv.getType().toString()));
        }

        return expression;
    }


    private DigitalExpressionBean getOBlockExpression(
            @Nonnull ConditionalVariable cv,
            OBlock b,
            AtomicBoolean isNegated)
            throws JmriException {

        ExpressionOBlock expression =
                new ExpressionOBlock(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);

        if (isNegated.get()) {
            expression.set_Is_IsNot(Is_IsNot_Enum.IsNot);
            isNegated.set(false);
        }

        OBlock.OBlockStatus oblockStatus = OBlock.OBlockStatus.getByName(cv.getDataString());

        if (oblockStatus == null) {
            throw new InvalidConditionalVariableException(
                    Bundle.getMessage("ConditionalBadOBlockDataString", cv.getDataString()));
        }

        expression.getSelectNamedBean().setNamedBean(b);
        expression.setBeanState(oblockStatus);

        return expression;
    }


    private DigitalActionBean getSensorAction(@Nonnull ConditionalAction ca, Sensor sn, String reference) throws JmriException {

        switch (ca.getType()) {
            case SET_SENSOR:
                ActionSensor action =
                        new ActionSensor(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);

                if (reference != null) {
                    action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
                    action.getSelectNamedBean().setReference(reference);
                } else {
                    action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
                    action.getSelectNamedBean().setNamedBean(sn);
                }

                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action.getSelectEnum().setEnum(ActionSensor.SensorState.Toggle);
                        break;

                    case Sensor.INACTIVE:
                        action.getSelectEnum().setEnum(ActionSensor.SensorState.Inactive);
                        break;

                    case Sensor.ACTIVE:
                        action.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
                        break;

                    default:
                        throw new InvalidConditionalVariableException(
                                Bundle.getMessage("ActionBadSensorState", ca.getActionData()));
                }
                return action;

            case RESET_DELAYED_SENSOR:
            case DELAYED_SENSOR:
                ConditionalAction caTemp = new DefaultConditionalAction();
                caTemp.setType(Conditional.Action.SET_SENSOR);
                caTemp.setActionData(ca.getActionData());
                DigitalActionBean subAction = getSensorAction(caTemp, sn, reference);
                ExecuteDelayed delayedAction =
                        new ExecuteDelayed(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);

                String sNumber = ca.getActionString();
                try {
                    int time = Integer.parseInt(sNumber);
                    delayedAction.setDelay(time);
                    delayedAction.setUnit(TimerUnit.Seconds);
                } catch (NumberFormatException e) {
                    try {
                        float time = Float.parseFloat(sNumber);
                        delayedAction.setDelay((int) (time * 1000));
                        delayedAction.setUnit(TimerUnit.MilliSeconds);
                    } catch (NumberFormatException e2) {
                        // If here, assume that sNumber has the name of a memory.
                        // Logix supports this memory to have a floating point value
                        // but LogixNG requires this memory to have an integer value.
                        if (sNumber.charAt(0) == '@') {
                            sNumber = sNumber.substring(1);
                        }
                        delayedAction.setDelayAddressing(NamedBeanAddressing.Reference);
                        delayedAction.setDelayReference("{" + sNumber + "}");
                        delayedAction.setUnit(TimerUnit.Seconds);
                    }
                }

                delayedAction.setResetIfAlreadyStarted(ca.getType() == Conditional.Action.RESET_DELAYED_SENSOR);
                if (!_dryRun) {
                    MaleSocket subActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(subAction);
                    delayedAction.getChild(0).connect(subActionSocket);
                }
                return delayedAction;

            case CANCEL_SENSOR_TIMERS:
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadSensorType", ca.getType().toString()));
        }
    }


    private DigitalActionBean getTurnoutAction(@Nonnull ConditionalAction ca, Turnout tn, String reference) throws JmriException {

        ActionTurnout action;

        switch (ca.getType()) {
            case SET_TURNOUT:
                action = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);

                if (reference != null) {
                    action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
                    action.getSelectNamedBean().setReference(reference);
                } else {
                    action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
                    action.getSelectNamedBean().setNamedBean(tn);
                }

                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Toggle);
                        break;

                    case Turnout.CLOSED:
                        action.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Closed);
                        break;

                    case Turnout.THROWN:
                        action.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
                        break;

                    default:
                        throw new InvalidConditionalVariableException(
                                Bundle.getMessage("ActionBadTurnoutState", ca.getActionData()));
                }
                break;

            case RESET_DELAYED_TURNOUT:
            case DELAYED_TURNOUT:
                ConditionalAction caTemp = new DefaultConditionalAction();
                caTemp.setType(Conditional.Action.SET_TURNOUT);
                caTemp.setActionData(ca.getActionData());
                DigitalActionBean subAction = getTurnoutAction(caTemp, tn, reference);
                ExecuteDelayed delayedAction =
                        new ExecuteDelayed(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);

                String sNumber = ca.getActionString();
                try {
                    int time = Integer.parseInt(sNumber);
                    delayedAction.setDelay(time);
                    delayedAction.setUnit(TimerUnit.Seconds);
                } catch (NumberFormatException e) {
                    try {
                        float time = Float.parseFloat(sNumber);
                        delayedAction.setDelay((int) (time * 1000));
                        delayedAction.setUnit(TimerUnit.MilliSeconds);
                    } catch (NumberFormatException e2) {
                        // If here, assume that sNumber has the name of a memory.
                        // Logix supports this memory to have a floating point value
                        // but LogixNG requires this memory to have an integer value.
                        if (sNumber.charAt(0) == '@') {
                            sNumber = sNumber.substring(1);
                        }
                        delayedAction.setDelayAddressing(NamedBeanAddressing.Reference);
                        delayedAction.setDelayReference("{" + sNumber + "}");
                        delayedAction.setUnit(TimerUnit.Seconds);
                    }
                }

                delayedAction.setResetIfAlreadyStarted(ca.getType() == Conditional.Action.RESET_DELAYED_TURNOUT);
                if (!_dryRun) {
                    MaleSocket subActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(subAction);
                    delayedAction.getChild(0).connect(subActionSocket);
                }
                return delayedAction;

            case LOCK_TURNOUT:
                ActionTurnoutLock action2 = new ActionTurnoutLock(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);

                if (reference != null) {
                    action2.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
                    action2.getSelectNamedBean().setReference(reference);
                } else {
                    action2.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
                    action2.getSelectNamedBean().setNamedBean(tn);
                }

                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action2.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Toggle);
                        break;

                    case Turnout.LOCKED:
                        action2.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Lock);
                        break;

                    case Turnout.UNLOCKED:
                        action2.getSelectEnum().setEnum(ActionTurnoutLock.TurnoutLock.Unlock);
                        break;

                    default:
                        throw new InvalidConditionalVariableException(
                                Bundle.getMessage("ActionBadTurnoutLock", ca.getActionData()));
                }
                return action2;

            case CANCEL_TURNOUT_TIMERS:
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadTurnoutType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getMemoryAction(@Nonnull ConditionalAction ca, Memory my, String reference) throws JmriException {

        ActionMemory action;

        action = new ActionMemory(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        if (reference != null) {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(reference);
        } else {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
            action.getSelectNamedBean().setNamedBean(my);
        }

        switch (ca.getType()) {
            case SET_MEMORY:
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
                action.setOtherConstantValue(ca.getActionString());
                break;

            case COPY_MEMORY:
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
                action.getSelectOtherMemoryNamedBean().setNamedBean(ca.getActionString());
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadMemoryType", ca.getActionData()));
        }

        return action;
    }


    private DigitalActionBean getLightAction(@Nonnull ConditionalAction ca, Light l, String reference) throws JmriException {

        ActionLight action = new ActionLight(InstanceManager.getDefault(DigitalActionManager.class)
                .getAutoSystemName(), null);

        if (reference != null) {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(reference);
        } else {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
            action.getSelectNamedBean().setNamedBean(l);
        }

        switch (ca.getType()) {
            case SET_LIGHT:
                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action.getSelectEnum().setEnum(ActionLight.LightState.Toggle);
                        break;

                    case Light.OFF:
                        action.getSelectEnum().setEnum(ActionLight.LightState.Off);
                        break;

                    case Light.ON:
                        action.getSelectEnum().setEnum(ActionLight.LightState.On);
                        break;

                    default:
                        throw new InvalidConditionalVariableException(
                                Bundle.getMessage("ActionBadLightState", ca.getActionData()));
                }
                break;

            case SET_LIGHT_INTENSITY:
                int intensity = 0;
                try {
                    intensity = Integer.parseInt(ca.getActionString());
                    if (intensity < 0 || intensity > 100) {
                        intensity = 0;
                    }
                } catch (NumberFormatException ex) {
                    intensity = 0;
                }
                action.setLightValue(intensity);
                action.getSelectEnum().setEnum(ActionLight.LightState.Intensity);
                break;

            case SET_LIGHT_TRANSITION_TIME:
                int interval = 0;
                try {
                    interval = Integer.parseInt(ca.getActionString());
                    if (interval < 0) {
                        interval = 0;
                    }
                } catch (NumberFormatException ex) {
                    interval = 0;
                }
                action.setLightValue(interval);
                action.getSelectEnum().setEnum(ActionLight.LightState.Interval);
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadLightType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getSignalHeadAction(@Nonnull ConditionalAction ca, SignalHead sh, String reference) throws JmriException {
        ActionSignalHead action =
                new ActionSignalHead(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        if (reference != null) {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(reference);
        } else {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
            action.getSelectNamedBean().setNamedBean(sh);
        }
        action.setOperationAddressing(NamedBeanAddressing.Direct);
        action.setAppearanceAddressing(NamedBeanAddressing.Direct);

        action.getSelectNamedBean().setNamedBean(sh);

        switch (ca.getType()) {
            case SET_SIGNAL_APPEARANCE:
                action.setOperationType(ActionSignalHead.OperationType.Appearance);
                action.setAppearance(ca.getActionData());
                break;

            case SET_SIGNAL_HELD:
                action.setOperationType(ActionSignalHead.OperationType.Held);
                break;

            case CLEAR_SIGNAL_HELD:
                action.setOperationType(ActionSignalHead.OperationType.NotHeld);
                break;

            case SET_SIGNAL_LIT:
                action.setOperationType(ActionSignalHead.OperationType.Lit);
                break;

            case SET_SIGNAL_DARK:
                action.setOperationType(ActionSignalHead.OperationType.NotLit);
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadSignalHeadType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getSignalMastAction(@Nonnull ConditionalAction ca, SignalMast sm, String reference) throws JmriException {
        ActionSignalMast action =
                new ActionSignalMast(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        if (reference != null) {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(reference);
        } else {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
            action.getSelectNamedBean().setNamedBean(sm);
        }
        action.setOperationAddressing(NamedBeanAddressing.Direct);
        action.setAspectAddressing(NamedBeanAddressing.Direct);

        switch (ca.getType()) {
            case SET_SIGNALMAST_ASPECT:
                action.setOperationType(ActionSignalMast.OperationType.Aspect);
                String aspect = ca.getActionString();
                if (aspect != null && aspect.length() > 0 && aspect.charAt(0) == '@') {
                    String memName = aspect.substring(1);
                    action.setAspectAddressing(NamedBeanAddressing.Reference);
                    action.setAspectReference("{" + memName + "}");
                } else {
                    action.setAspect(aspect);
                }
                break;

            case SET_SIGNALMAST_HELD:
                action.setOperationType(ActionSignalMast.OperationType.Held);
                break;

            case CLEAR_SIGNALMAST_HELD:
                action.setOperationType(ActionSignalMast.OperationType.NotHeld);
                break;

            case SET_SIGNALMAST_LIT:
                action.setOperationType(ActionSignalMast.OperationType.Lit);
                break;

            case SET_SIGNALMAST_DARK:
                action.setOperationType(ActionSignalMast.OperationType.NotLit);
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadSignalMastType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getEntryExitAction(@Nonnull ConditionalAction ca, DestinationPoints dp, String reference) throws JmriException {
        ActionEntryExit action =
                new ActionEntryExit(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        if (reference != null) {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(reference);
        } else {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
            action.getSelectNamedBean().setNamedBean(dp);
        }
        action.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);

        switch (ca.getType()) {
            case SET_NXPAIR_ENABLED:
                action.getSelectEnum().setEnum(ActionEntryExit.Operation.SetNXPairEnabled);
                break;
            case SET_NXPAIR_DISABLED:
                action.getSelectEnum().setEnum(ActionEntryExit.Operation.SetNXPairDisabled);
                break;
            case SET_NXPAIR_SEGMENT:
                action.getSelectEnum().setEnum(ActionEntryExit.Operation.SetNXPairSegment);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadEntryExitType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getWarrantAction(@Nonnull ConditionalAction ca, Warrant w, String reference) throws JmriException {
        ActionWarrant action =
                new ActionWarrant(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        if (reference != null) {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(reference);
        } else {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
            action.getSelectNamedBean().setNamedBean(w);
        }
        action.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);

        switch (ca.getType()) {
            case ALLOCATE_WARRANT_ROUTE:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.AllocateWarrantRoute);
                break;

            case DEALLOCATE_WARRANT_ROUTE:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.DeallocateWarrant);
                break;

            case SET_ROUTE_TURNOUTS:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.SetRouteTurnouts);
                break;

            case AUTO_RUN_WARRANT:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.AutoRunTrain);
                break;

            case MANUAL_RUN_WARRANT:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.ManuallyRunTrain);
                break;

            case CONTROL_TRAIN:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.ControlAutoTrain);
                action.setControlAutoTrain(ActionWarrant.ControlAutoTrain.values()[ca.getActionData() - 1]);
                break;

            case SET_TRAIN_ID:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.SetTrainId);
                String idData = ca.getActionString();
                if (idData == null || idData.isEmpty()) {
                    throw new InvalidConditionalActionException(
                            Bundle.getMessage("ActionBadWarrantValue", ca.getType().toString()));
                }
                if (idData.startsWith("@")) {
                    // indirect
                    String ref = "{" + idData.substring(1) + "}";
                    action.setDataAddressing(NamedBeanAddressing.Reference);
                    action.setDataReference(ref);
                } else {
                    action.setDataAddressing(NamedBeanAddressing.Direct);
                    action.setTrainIdName(idData);
                }
                break;

            case SET_TRAIN_NAME:
                action.getSelectEnum().setEnum(ActionWarrant.DirectOperation.SetTrainName);
                String nameData = ca.getActionString();
                if (nameData == null || nameData.isEmpty()) {
                    throw new InvalidConditionalActionException(
                            Bundle.getMessage("ActionBadWarrantValue", ca.getType().toString()));
                }
                if (nameData.startsWith("@")) {
                    // indirect
                    String ref = "{" + nameData.substring(1) + "}";
                    action.setDataAddressing(NamedBeanAddressing.Reference);
                    action.setDataReference(ref);
                } else {
                    action.setDataAddressing(NamedBeanAddressing.Direct);
                    action.setTrainIdName(nameData);
                }
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadwarrantType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getOBlockAction(@Nonnull ConditionalAction ca, OBlock b, String reference) throws JmriException {

        ActionOBlock action =
                new ActionOBlock(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        if (reference != null) {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(reference);
        } else {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
            action.getSelectNamedBean().setNamedBean(b);
        }
        action.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);

        switch (ca.getType()) {
            case DEALLOCATE_BLOCK:
                action.getSelectEnum().setEnum(ActionOBlock.DirectOperation.Deallocate);
                break;

            case SET_BLOCK_VALUE:
                action.getSelectEnum().setEnum(ActionOBlock.DirectOperation.SetValue);
                String oblockData = ca.getActionString();
                if (oblockData == null || oblockData.isEmpty()) {
                    throw new InvalidConditionalActionException(
                            Bundle.getMessage("ActionBadOBlockValue", ca.getType().toString()));
                }
                if (oblockData.startsWith("@")) {
                    // indirect
                    String ref = "{" + oblockData.substring(1) + "}";
                    action.setDataAddressing(NamedBeanAddressing.Reference);
                    action.setDataReference(ref);
                } else {
                    action.setDataAddressing(NamedBeanAddressing.Direct);
                    action.setOBlockValue(oblockData);
                }
                break;

            case SET_BLOCK_ERROR:
                action.getSelectEnum().setEnum(ActionOBlock.DirectOperation.SetError);
                break;

            case CLEAR_BLOCK_ERROR:
                action.getSelectEnum().setEnum(ActionOBlock.DirectOperation.ClearError);
                break;

            case SET_BLOCK_OUT_OF_SERVICE:
                action.getSelectEnum().setEnum(ActionOBlock.DirectOperation.SetOutOfService);
                break;

            case SET_BLOCK_IN_SERVICE:
                action.getSelectEnum().setEnum(ActionOBlock.DirectOperation.ClearOutOfService);
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadOBlockType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getEnableLogixAction(@Nonnull ConditionalAction ca) throws JmriException {
        EnableLogix action =
                new EnableLogix(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        action.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);

        String devName = ca.getDeviceName();
        if (devName != null) {
            if (devName.length() > 0 && devName.charAt(0) == '@') {
                String memName = devName.substring(1);
                action.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
                action.getSelectEnum().setReference("{" + memName + "}");
            } else {
                action.getSelectNamedBean().setNamedBean(devName);
            }
        }

        switch (ca.getType()) {
            case ENABLE_LOGIX:
                action.getSelectEnum().setEnum(EnableLogix.Operation.Enable);
                break;

            case DISABLE_LOGIX:
                action.getSelectEnum().setEnum(EnableLogix.Operation.Disable);
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadEnableLogixType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getClockAction(@Nonnull ConditionalAction ca) throws JmriException {
        ActionClock action =
                new ActionClock(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        switch (ca.getType()) {
            case START_FAST_CLOCK:
                action.getSelectEnum().setEnum(ActionClock.ClockState.StartClock);
                break;

            case STOP_FAST_CLOCK:
                action.getSelectEnum().setEnum(ActionClock.ClockState.StopClock);
                break;

            case SET_FAST_CLOCK_TIME:
                action.getSelectEnum().setEnum(ActionClock.ClockState.SetClock);
                action.getSelectTime().setValue(ca.getActionData());
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadSensorType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getAudioAction(@Nonnull ConditionalAction ca) throws JmriException {
        ActionAudio action =
                new ActionAudio(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        action.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);

        String sound = ca.getActionString();
        if (sound != null && sound.length() > 0 && sound.charAt(0) == '@') {
            action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
            action.getSelectNamedBean().setReference(sound.substring(1));
        } else {
            Audio audio = InstanceManager.getDefault(jmri.AudioManager.class).getAudio(ca.getDeviceName());
            if (audio != null) action.getSelectNamedBean().setNamedBean(audio);
        }

        switch (ca.getActionData()) {
            case Audio.CMD_PLAY:
                action.getSelectEnum().setEnum(ActionAudio.Operation.Play);
                break;
            case Audio.CMD_STOP:
                action.getSelectEnum().setEnum(ActionAudio.Operation.Stop);
                break;
            case Audio.CMD_PLAY_TOGGLE:
                action.getSelectEnum().setEnum(ActionAudio.Operation.PlayToggle);
                break;
            case Audio.CMD_PAUSE:
                action.getSelectEnum().setEnum(ActionAudio.Operation.Pause);
                break;
            case Audio.CMD_RESUME:
                action.getSelectEnum().setEnum(ActionAudio.Operation.Resume);
                break;
            case Audio.CMD_PAUSE_TOGGLE:
                action.getSelectEnum().setEnum(ActionAudio.Operation.PauseToggle);
                break;
            case Audio.CMD_REWIND:
                action.getSelectEnum().setEnum(ActionAudio.Operation.Rewind);
                break;
            case Audio.CMD_FADE_IN:
                action.getSelectEnum().setEnum(ActionAudio.Operation.FadeIn);
                break;
            case Audio.CMD_FADE_OUT:
                action.getSelectEnum().setEnum(ActionAudio.Operation.FadeOut);
                break;
            case Audio.CMD_RESET_POSITION:
                action.getSelectEnum().setEnum(ActionAudio.Operation.ResetPosition);
                break;
            default:
                break;
        }

        return action;
    }

    private DigitalActionBean getSoundAction(@Nonnull ConditionalAction ca) throws JmriException {
        ActionSound action =
                new ActionSound(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        action.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        action.setSoundAddressing(NamedBeanAddressing.Direct);

        String sound = ca.getActionString();
        if (sound != null && sound.length() > 0 && sound.charAt(0) == '@') {
            action.setSoundAddressing(NamedBeanAddressing.Reference);
            action.setSoundReference(sound.substring(1));
        } else {
            action.setSound(sound);
        }

        return action;
    }

    private DigitalActionBean getAudioOrSoundAction(@Nonnull ConditionalAction ca) throws JmriException {
        switch (ca.getType()) {
            case CONTROL_AUDIO:
                return getAudioAction(ca);

            case PLAY_SOUND:
                return getSoundAction(ca);

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadAudioOrSoundType", ca.getType().toString()));
        }
    }


    private DigitalActionBean getScriptAction(@Nonnull ConditionalAction ca) throws JmriException {
        ActionScript action =
                new ActionScript(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        action.setOperationAddressing(NamedBeanAddressing.Direct);
        action.setScriptAddressing(NamedBeanAddressing.Direct);

        String script = ca.getActionString();
        if (script != null && script.length() > 0 && script.charAt(0) == '@') {
            action.setScriptAddressing(NamedBeanAddressing.Reference);
            action.setScriptReference(script.substring(1));
        } else {
            action.setScript(script);
        }

        switch (ca.getType()) {
            case RUN_SCRIPT:
                action.setOperationType(ActionScript.OperationType.RunScript);
                break;

            case JYTHON_COMMAND:
                action.setOperationType(ActionScript.OperationType.SingleLineCommand);
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadScriptType", ca.getType().toString()));
        }

        return action;
    }


    private DigitalActionBean getRouteAction(@Nonnull ConditionalAction ca, Route b, String reference) throws JmriException {
        TriggerRoute action =
                new TriggerRoute(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);

        action.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        action.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);

        String devName = ca.getDeviceName();
        if (devName != null) {
            if (devName.length() > 0 && devName.charAt(0) == '@') {
                String memName = devName.substring(1);
                action.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
                action.getSelectEnum().setReference("{" + memName + "}");
            } else {
                action.getSelectNamedBean().setNamedBean(devName);
            }
        }

        switch (ca.getType()) {
            case TRIGGER_ROUTE:
                action.getSelectEnum().setEnum(TriggerRoute.Operation.TriggerRoute);
                break;

            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadRouteType", ca.getType().toString()));
        }

        return action;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportConditional.class);

}
