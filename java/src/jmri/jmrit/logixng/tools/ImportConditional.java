package jmri.jmrit.logixng.tools;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.DefaultConditionalAction;
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
                        .createConditionalNG(userName + name);
                counter++;
            }
            
            if (conditionalNG == null) throw new RuntimeException("Cannot create new ConditionalNG with name: \"" + userName + "\"");
            
            _conditionalNG = conditionalNG;
            
//            _conditionalNG = InstanceManager.getDefault(
//                    jmri.jmrit.logixng.ConditionalNG_Manager.class)
//                    .createConditionalNG(sysName, userName);
        } else {
            _conditionalNG = null;
        }
        
//        log.debug("Import Logix {} to LogixNG {}", _logix.getSystemName(), _logixNG.getSystemName());
//        log.error("AA: Import Conditional {} to ConditionalNG {}", _conditional.getSystemName(), _conditionalNG.getSystemName());
    }
    
    public ConditionalNG getConditionalNG() {
        return _conditionalNG;
    }
    
    public void doImport() throws SocketAlreadyConnectedException, JmriException {
/*        
        // This is only to remember to test everything Logix and Conditional supports
        String systemName = "";
        jmri.Logix l = null;
        
        java.beans.PropertyChangeEvent evt = null;
        jmri.Conditional c = null;
        
        l.getConditional(systemName);
        l.getConditionalByNumberOrder(0);
        l.getEnabled();
        
        c.cancelSensorTimer(systemName);
        c.cancelTurnoutTimer(systemName);
        c.getAntecedentExpression();        // Tested
        c.getCopyOfActions();
        c.getCopyOfStateVariables();
        c.getLogicType();                   // Tested
        c.getTriggerOnChange();             // Tested
*/        
        
        
        
        
        
        
        
//        boolean triggerOnChange = _conditional.getTriggerOnChange();
//        IfThenElse.Type type = triggerOnChange ? IfThenElse.Type.TRIGGER_ACTION : IfThenElse.Type.CONTINOUS_ACTION;
        
//        IfThenElse ifThen = new IfThenElse(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null, type);
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
                    newExpression = getSensorExpression(cv, sn);
                    break;
                case TURNOUT:
                    Turnout tn = (Turnout)nb;
                    newExpression = getTurnoutExpression(cv, tn);
                    break;
                case MEMORY:
                    Memory my = (Memory)nb;
                    newExpression = getMemoryExpression(cv, my);
                    break;
                case LIGHT:
                    Light l = (Light)nb;
                    newExpression = getLightExpression(cv, l);
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
                    newExpression = getEntryExitExpression(cv, dp);
                    break;
                case CONDITIONAL:
                    Conditional c = (Conditional)nb;
                    newExpression = getConditionalExpression(cv, c);
                    break;
                case CLOCK:
                    newExpression = getFastClockExpression(cv);
                    break;
                case WARRANT:
                    Warrant w = (Warrant)nb;
                    newExpression = getWarrantExpression(cv, w);
                    break;
                case OBLOCK:
                    OBlock b = (OBlock)nb;
                    newExpression = getOBlockExpression(cv, b);
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
                newAction = getAudioAction(conditionalAction);
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
    
    
    private DigitalExpressionBean getSensorExpression(@Nonnull ConditionalVariable cv, Sensor sn) throws JmriException {
        ExpressionSensor expression =
                new ExpressionSensor(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
//        System.err.format("Sensor: %s%n", sn == null ? null : sn.getSystemName());
        
        expression.setSensor(sn);
        
//        cv.getDataString();     // SignalMast, Memory, OBlock
//        cv.getNamedBeanData();  // Only for memory
//        cv.getNum1();   // Clock, Memory
//        cv.getNum2();   // Clock, Memory
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getTurnoutExpression(@Nonnull ConditionalVariable cv, Turnout tn) throws JmriException {
        ExpressionTurnout expression =
                new ExpressionTurnout(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setTurnout(tn);
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getMemoryExpression(@Nonnull ConditionalVariable cv, Memory my) throws JmriException {
        ExpressionMemory expression =
                new ExpressionMemory(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setMemory(my);
        
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
                expression.setOtherMemory(cv.getDataString());
                memory = InstanceManager.getDefault(MemoryManager.class).getMemory(cv.getDataString());
                if (memory == null) {   // Logix allows the memory name in cv.getDataString() to be a system name without system prefix
                    memory = InstanceManager.getDefault(MemoryManager.class).provide(cv.getDataString());
                    expression.setOtherMemory(memory.getSystemName());
                }
                break;
            case MEMORY_COMPARE_INSENSITIVE:
                expression.setCompareTo(ExpressionMemory.CompareTo.Memory);
                expression.setCaseInsensitive(true);
                expression.setOtherMemory(cv.getDataString());
                memory = InstanceManager.getDefault(MemoryManager.class).getMemory(cv.getDataString());
                if (memory == null) {   // Logix allows the memory name in cv.getDataString() to be a system name without system prefix
                    memory = InstanceManager.getDefault(MemoryManager.class).provide(cv.getDataString());
                    expression.setOtherMemory(memory.getSystemName());
                }
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadMemoryType", cv.getType().toString()));
        }
        
        expression.setListenToOtherMemory(false);
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getLightExpression(@Nonnull ConditionalVariable cv, Light ln) throws JmriException {
        ExpressionLight expression =
                new ExpressionLight(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setLight(ln);
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getSignalHeadExpression(
            @Nonnull ConditionalVariable cv, SignalHead s, AtomicBoolean isNegated)
            throws JmriException {
        
        ExpressionSignalHead expression =
                new ExpressionSignalHead(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setSignalHead(s);
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        isNegated.set(false);   // We have already handled this
        
        return expression;
    }
    
    
    private DigitalExpressionBean getSignalMastExpression(
            @Nonnull ConditionalVariable cv, SignalMast sm, AtomicBoolean isNegated)
            throws JmriException {
        
        ExpressionSignalMast expression =
                new ExpressionSignalMast(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setSignalMast(sm);
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        isNegated.set(false);   // We have already handled this
        
        return expression;
    }
    
    
    private DigitalExpressionBean getEntryExitExpression(@Nonnull ConditionalVariable cv, DestinationPoints dp) throws JmriException {
        ExpressionEntryExit expression =
                new ExpressionEntryExit(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setDestinationPoints(dp);
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getConditionalExpression(@Nonnull ConditionalVariable cv, Conditional cn) throws JmriException {
        ExpressionConditional expression =
                new ExpressionConditional(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setConditional(cn);
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getFastClockExpression(@Nonnull ConditionalVariable cv) throws JmriException {
        ExpressionClock expression =
                new ExpressionClock(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        if (cv.getType() != Conditional.Type.FAST_CLOCK_RANGE) {
            throw new InvalidConditionalVariableException(
                    Bundle.getMessage("ConditionalBadFastClockType", cv.getType().toString()));
        }
        
        expression.setType(ExpressionClock.Type.FastClock);
        expression.setRange(ConditionalVariable.fixMidnight(cv.getNum1()), ConditionalVariable.fixMidnight(cv.getNum2()));
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getWarrantExpression(@Nonnull ConditionalVariable cv, Warrant w) throws JmriException {
        ExpressionWarrant expression =
                new ExpressionWarrant(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        expression.setWarrant(w);
        
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
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getOBlockExpression(@Nonnull ConditionalVariable cv, OBlock b) throws JmriException {
        ExpressionOBlock expression =
                new ExpressionOBlock(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        OBlock.OBlockStatus oblockStatus = OBlock.OBlockStatus.getByName(cv.getDataString());
        
        if (oblockStatus == null) {
            throw new InvalidConditionalVariableException(
                    Bundle.getMessage("ConditionalBadOBlockDataString", cv.getDataString()));
        }
        
        expression.setOBlock(b);
        expression.setBeanState(oblockStatus);
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalActionBean getSensorAction(@Nonnull ConditionalAction ca, Sensor sn, String reference) throws JmriException {
        
        switch (ca.getType()) {
            case SET_SENSOR:
                ActionSensor action = 
                        new ActionSensor(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);
                
                if (reference != null) {
                    action.setAddressing(NamedBeanAddressing.Reference);
                    action.setReference(reference);
                } else {
                    action.setAddressing(NamedBeanAddressing.Direct);
                    action.setSensor(sn);
                }
                
                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action.setBeanState(ActionSensor.SensorState.Toggle);
                        break;
                        
                    case Sensor.INACTIVE:
                        action.setBeanState(ActionSensor.SensorState.Inactive);
                        break;
                        
                    case Sensor.ACTIVE:
                        action.setBeanState(ActionSensor.SensorState.Active);
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
//        System.err.format("Turnout: %s%n", tn == null ? null : tn.getSystemName());
        
        ActionTurnout action;
        
//        cv.getDataString();     // SignalMast, Memory, OBlock
//        cv.getNamedBeanData();  // Only for memory
//        cv.getNum1();   // Clock, Memory
//        cv.getNum2();   // Clock, Memory
        
        switch (ca.getType()) {
            case SET_TURNOUT:
                action = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);
                
                if (reference != null) {
                    action.setAddressing(NamedBeanAddressing.Reference);
                    action.setReference(reference);
                } else {
                    action.setAddressing(NamedBeanAddressing.Direct);
                    action.setTurnout(tn);
                }
                
                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action.setBeanState(ActionTurnout.TurnoutState.Toggle);
                        break;
                        
                    case Turnout.CLOSED:
                        action.setBeanState(ActionTurnout.TurnoutState.Closed);
                        break;
                        
                    case Turnout.THROWN:
                        action.setBeanState(ActionTurnout.TurnoutState.Thrown);
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
                    action2.setAddressing(NamedBeanAddressing.Reference);
                    action2.setReference(reference);
                } else {
                    action2.setAddressing(NamedBeanAddressing.Direct);
                    action2.setTurnout(tn);
                }
                
                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action2.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Toggle);
                        break;
                        
                    case Turnout.LOCKED:
                        action2.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Lock);
                        break;
                        
                    case Turnout.UNLOCKED:
                        action2.setTurnoutLock(ActionTurnoutLock.TurnoutLock.Unlock);
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
        
//        ca.getActionData();
//        action.setTriggerOnChange(ca.doTriggerActions());
        
        return action;
    }
    
    
    private DigitalActionBean getMemoryAction(@Nonnull ConditionalAction ca, Memory my, String reference) throws JmriException {
        
        ActionMemory action;
        
        action = new ActionMemory(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);
        
        if (reference != null) {
            action.setAddressing(NamedBeanAddressing.Reference);
            action.setReference(reference);
        } else {
            action.setAddressing(NamedBeanAddressing.Direct);
            action.setMemory(my);
        }
        
        switch (ca.getType()) {
            case SET_MEMORY:
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
                action.setOtherConstantValue(ca.getActionString());
                break;
                
            case COPY_MEMORY:
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
                action.setOtherMemory(ca.getActionString());
                break;
                
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadMemoryType", ca.getActionData()));
        }
        
        return action;
    }
    
    
    private DigitalActionBean getLightAction(@Nonnull ConditionalAction ca, Light l, String reference) throws JmriException {
        
        ActionLight action;
        
        switch (ca.getType()) {
            case SET_LIGHT:
                action = new ActionLight(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);
                
                if (reference != null) {
                    action.setAddressing(NamedBeanAddressing.Reference);
                    action.setReference(reference);
                } else {
                    action.setAddressing(NamedBeanAddressing.Direct);
                    action.setLight(l);
                }
                
                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action.setBeanState(ActionLight.LightState.Toggle);
                        break;
                        
                    case Light.OFF:
                        action.setBeanState(ActionLight.LightState.Off);
                        break;
                        
                    case Light.ON:
                        action.setBeanState(ActionLight.LightState.On);
                        break;
                        
                    default:
                        throw new InvalidConditionalVariableException(
                                Bundle.getMessage("ActionBadLightState", ca.getActionData()));
                }
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
            action.setAddressing(NamedBeanAddressing.Reference);
            action.setReference(reference);
        } else {
            action.setAddressing(NamedBeanAddressing.Direct);
            action.setSignalHead(sh);
        }
        action.setOperationAddressing(NamedBeanAddressing.Direct);
        action.setAppearanceAddressing(NamedBeanAddressing.Direct);
        
        action.setSignalHead(sh);
        
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
                        Bundle.getMessage("ConditionalBadSignalHeadType", ca.getType().toString()));
        }
        
        return action;
    }
    
    
    private DigitalActionBean getSignalMastAction(@Nonnull ConditionalAction ca, SignalMast sm, String reference) throws JmriException {
        ActionSignalMast action =
                new ActionSignalMast(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);
        
        if (reference != null) {
            action.setAddressing(NamedBeanAddressing.Reference);
            action.setReference(reference);
        } else {
            action.setAddressing(NamedBeanAddressing.Direct);
            action.setSignalMast(sm);
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
                        Bundle.getMessage("ConditionalBadSignalMastType", ca.getType().toString()));
        }
        
        return action;
    }
    
    
    private DigitalActionBean getEntryExitAction(@Nonnull ConditionalAction ca, DestinationPoints dp, String reference) throws JmriException {
        ActionEntryExit action =
                new ActionEntryExit(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);
        
        if (reference != null) {
            action.setAddressing(NamedBeanAddressing.Reference);
            action.setReference(reference);
        } else {
            action.setAddressing(NamedBeanAddressing.Direct);
            action.setDestinationPoints(dp);
        }
        action.setOperationAddressing(NamedBeanAddressing.Direct);
        
        switch (ca.getType()) {
            case SET_NXPAIR_ENABLED:
                action.setOperationDirect(ActionEntryExit.Operation.SetNXPairEnabled);
                break;
            case SET_NXPAIR_DISABLED:
                action.setOperationDirect(ActionEntryExit.Operation.SetNXPairDisabled);
                break;
            case SET_NXPAIR_SEGMENT:
                action.setOperationDirect(ActionEntryExit.Operation.SetNXPairSegment);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadEntryExitType", ca.getType().toString()));
        }
        
        return action;
    }
    
    
    private DigitalActionBean getWarrantAction(@Nonnull ConditionalAction ca, Warrant w, String reference) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getOBlockAction(@Nonnull ConditionalAction ca, OBlock b, String reference) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getEnableLogixAction(@Nonnull ConditionalAction ca) throws JmriException {
        EnableLogix action =
                new EnableLogix(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);
        
        action.setAddressing(NamedBeanAddressing.Direct);
        action.setOperationAddressing(NamedBeanAddressing.Direct);
        
        String devName = ca.getDeviceName();
        if (devName != null) {
            if (devName.length() > 0 && devName.charAt(0) == '@') {
                String memName = devName.substring(1);
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference("{" + memName + "}");
            } else {
                action.setLogix(devName);
            }
        }
        
        switch (ca.getType()) {
            case ENABLE_LOGIX:
                action.setOperationDirect(EnableLogix.Operation.Enable);
                break;
                
            case DISABLE_LOGIX:
                action.setOperationDirect(EnableLogix.Operation.Disable);
                break;
                
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadEntryExitType", ca.getType().toString()));
        }
        
        return action;
    }
    
    
    private DigitalActionBean getClockAction(@Nonnull ConditionalAction ca) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getAudioAction(@Nonnull ConditionalAction ca) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getScriptAction(@Nonnull ConditionalAction ca) throws JmriException {
        ActionSimpleScript action =
                new ActionSimpleScript(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);
        
        action.setOperationAddressing(NamedBeanAddressing.Direct);
        action.setScriptAddressing(NamedBeanAddressing.Direct);
        
        String script = ca.getActionString();
        if (script != null && script.length() > 0 && script.charAt(0) == '@') {
            action.setScriptAddressing(NamedBeanAddressing.Direct);
            action.setScriptReference(script.substring(1));
        } else {
            action.setScript(script);
        }
        
        switch (ca.getType()) {
            case RUN_SCRIPT:
                action.setOperationType(ActionSimpleScript.OperationType.RunScript);
                break;
                
            case JYTHON_COMMAND:
                action.setOperationType(ActionSimpleScript.OperationType.JythonCommand);
                break;
                
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadEntryExitType", ca.getType().toString()));
        }
        
        return action;
    }
    
    
    private DigitalActionBean getRouteAction(@Nonnull ConditionalAction ca, Route b, String reference) throws JmriException {
        TriggerRoute action =
                new TriggerRoute(InstanceManager.getDefault(DigitalActionManager.class)
                        .getAutoSystemName(), null);
        
        action.setAddressing(NamedBeanAddressing.Direct);
        action.setOperationAddressing(NamedBeanAddressing.Direct);
        
        String devName = ca.getDeviceName();
        if (devName != null) {
            if (devName.length() > 0 && devName.charAt(0) == '@') {
                String memName = devName.substring(1);
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference("{" + memName + "}");
            } else {
                action.setRoute(devName);
            }
        }
        
        switch (ca.getType()) {
            case TRIGGER_ROUTE:
                action.setOperationDirect(TriggerRoute.Operation.TriggerRoute);
                break;
                
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadEntryExitType", ca.getType().toString()));
        }
        
        return action;
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportConditional.class);

}
