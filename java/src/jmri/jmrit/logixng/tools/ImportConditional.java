package jmri.jmrit.logixng.tools;

import java.util.List;

import javax.annotation.Nonnull;

import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.Memory;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.Turnout;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.actions.*;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;
import jmri.jmrit.logixng.digital.expressions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports Logixs to LogixNG
 * 
 * @author Daniel Bergqvist 2019
 */
public class ImportConditional {

//    private final Logix _logix;
    private final jmri.Conditional _conditional;
//    private final LogixNG _logixNG;
    private final ConditionalNG _conditionalNG;
    
    public ImportConditional(jmri.Logix logix, Conditional conditional,
            LogixNG logixNG, String conditionalNG_SysName) {
        
//        _logix = logix;
        _conditional = conditional;
//        _logixNG = logixNG;
        String userName = conditional.getSystemName();
        if (conditional.getUserName() != null) {
            userName += ": " + conditional.getUserName();
        }
//        _conditionalNG = new DefaultConditionalNG(conditionalNG_SysName, null);
        _conditionalNG = InstanceManager.getDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class)
                .createConditionalNG(conditionalNG_SysName, userName);
        
//        log.debug("Import Logix {} to LogixNG {}", _logix.getSystemName(), _logixNG.getSystemName());
//        log.error("AA: Import Conditional {} to ConditionalNG {}", _conditional.getSystemName(), _conditionalNG.getSystemName());
    }
    
    public ConditionalNG getConditionalNG() {
        return _conditionalNG;
    }
    
    public void doImport() throws SocketAlreadyConnectedException, JmriException {
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
        buildExpression(expression, conditionalVariables);
        
//        DigitalActionBean action = new Many(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        buildAction(logix, _conditional, conditionalActions);
        
        MaleSocket expressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        logix.getChild(0).connect(expressionSocket);
        
        MaleSocket logixAction = InstanceManager.getDefault(DigitalActionManager.class).registerAction(logix);
        _conditionalNG.getChild(0).connect(logixAction);
    }
    
    
    private void buildExpression(DigitalExpressionBean expression, List<ConditionalVariable> conditionalVariables)
            throws SocketAlreadyConnectedException, JmriException {
        for (int i=0; i < conditionalVariables.size(); i++) {
            jmri.ConditionalVariable cv = conditionalVariables.get(i);
            NamedBean nb = cv.getBean();
//            NamedBean nb = cv.getNamedBeanData();
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
                    newExpression = getSignalHeadExpression(cv, s);
                    break;
                case SIGNALMAST:
                    SignalMast sm = (SignalMast)nb;
                    newExpression = getSignalMastExpression(cv, sm);
                    break;
                case ENTRYEXIT:
//                    NamedBean nb = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getBySystemName(_name);
//                    newExpression = getSensorExpression(cv, sn);
                    newExpression = null;
                    break;
                case CONDITIONAL:
                    Conditional c = (Conditional)nb;
                    newExpression = getConditionalExpression(cv, c);
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
                    log.warn("Unexpected type in ImportConditional.doImport(): {} -> {}", cv.getType(), cv.getType().getItemType());
                    break;
            }
            
            if (newExpression != null) {
                MaleSocket newExpressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(newExpression);
                expression.getChild(i).connect(newExpressionSocket);
            }
        }
    }
    
    
    private void buildAction(Logix logix, Conditional conditional, List<ConditionalAction> conditionalActions)
            throws SocketAlreadyConnectedException, JmriException {
        
        for (int i=0; i < conditionalActions.size(); i++) {
            ConditionalAction ca = conditionalActions.get(i);
            
            boolean triggerType = conditional.getTriggerOnChange();
            
            OnChange.Trigger trigger;
            switch (ca.getOption()) {
                case Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE:
                    trigger = OnChange.Trigger.CHANGE_TO_TRUE;
                    break;
                    
                case Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE:
                    trigger = OnChange.Trigger.CHANGE_TO_FALSE;
                    break;
                    
                case Conditional.ACTION_OPTION_ON_CHANGE:
                    trigger = OnChange.Trigger.CHANGE;
                    break;
                    
                default:
                    throw new InvalidConditionalActionException(
                            Bundle.getMessage("ActionBadTrigger", ca.getOption()));
            }
            
            DigitalBooleanActionBean booleanAction =
                    new OnChange(InstanceManager.getDefault(DigitalBooleanActionManager.class).getAutoSystemName(), null, trigger);
            
            buildAction(booleanAction, ca);
            
            MaleSocket newBooleanActionSocket = InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(booleanAction);
            logix.getChild(i+1).connect(newBooleanActionSocket);
        }
    }
    
    private void buildAction(DigitalBooleanActionBean action, ConditionalAction conditionalAction)
            throws SocketAlreadyConnectedException, JmriException {
        
        NamedBean nb = conditionalAction.getBean();
        System.err.format("nb: %s%n", nb == null ? null : nb.getSystemName());
        DigitalActionBean newAction;
        switch (conditionalAction.getType().getItemType()) {
            case SENSOR:
                Sensor sn = (Sensor)nb;
                newAction = getSensorAction(conditionalAction, sn);
                break;
            case TURNOUT:
                Turnout tn = (Turnout)nb;
                newAction = getTurnoutAction(conditionalAction, tn);
                break;
            case MEMORY:
                Memory my = (Memory)nb;
                newAction = getMemoryAction(conditionalAction, my);
                break;
            case LIGHT:
                Light l = (Light)nb;
                newAction = getLightAction(conditionalAction, l);
                break;
            case SIGNALHEAD:
                SignalHead s = (SignalHead)nb;
                newAction = getSignalHeadAction(conditionalAction, s);
                break;
            case SIGNALMAST:
                SignalMast sm = (SignalMast)nb;
                newAction = getSignalMastAction(conditionalAction, sm);
                break;
            case ENTRYEXIT:
//                    NamedBean nb = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getBySystemName(_name);
//                    newAction = getSensorAction(cv, sn);
                newAction = null;
                break;
            case CONDITIONAL:
                Conditional c = (Conditional)nb;
                newAction = getConditionalAction(conditionalAction, c);
                break;
            case WARRANT:
                Warrant w = (Warrant)nb;
                newAction = getWarrantAction(conditionalAction, w);
                break;
            case OBLOCK:
                OBlock b = (OBlock)nb;
                newAction = getOBlockAction(conditionalAction, b);
                break;

            default:
                newAction = null;
                log.warn("Unexpected type in ImportConditional.doImport(): {} -> {}", conditionalAction.getType(), conditionalAction.getType().getItemType());
                break;
        }

        if (newAction != null) {
            MaleSocket newActionSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(newAction);
            action.getChild(0).connect(newActionSocket);
        }
    }
    
    
    private DigitalExpressionBean getSensorExpression(@Nonnull ConditionalVariable cv, Sensor sn) throws JmriException {
        ExpressionSensor expression =
                new ExpressionSensor(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getAutoSystemName(), null);
        
        System.err.format("Sensor: %s%n", sn == null ? null : sn.getSystemName());
        
        expression.setSensor(sn);
        
//        cv.getDataString();     // SignalMast, Memory, OBlock
//        cv.getNamedBeanData();  // Only for memory
//        cv.getNum1();   // Clock, Memory
//        cv.getNum2();   // Clock, Memory
        
        switch (cv.getType()) {
            case SENSOR_ACTIVE:
                expression.setSensorState(ExpressionSensor.SensorState.ACTIVE);
                break;
            case SENSOR_INACTIVE:
                expression.setSensorState(ExpressionSensor.SensorState.ACTIVE);
                break;
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ConditionalBadSensorType", cv.getType().toString()));
        }
        
        expression.setTriggerOnChange(cv.doTriggerActions());
        
        return expression;
    }
    
    
    private DigitalExpressionBean getTurnoutExpression(@Nonnull ConditionalVariable cv, Turnout tn) throws JmriException {
        return null;
    }
    
    
    private DigitalExpressionBean getMemoryExpression(@Nonnull ConditionalVariable cv, Memory my) throws JmriException {
        return null;
    }
    
    
    private DigitalExpressionBean getLightExpression(@Nonnull ConditionalVariable cv, Light l) throws JmriException {
        return null;
    }
    
    
    private DigitalExpressionBean getSignalHeadExpression(@Nonnull ConditionalVariable cv, SignalHead s) throws JmriException {
        return null;
    }
    
    
    private DigitalExpressionBean getSignalMastExpression(@Nonnull ConditionalVariable cv, SignalMast sm) throws JmriException {
        return null;
    }
    
    
    private DigitalExpressionBean getConditionalExpression(@Nonnull ConditionalVariable cv, Conditional c) throws JmriException {
        return null;
    }
    
    
    private DigitalExpressionBean getWarrantExpression(@Nonnull ConditionalVariable cv, Warrant w) throws JmriException {
        return null;
    }
    
    
    private DigitalExpressionBean getOBlockExpression(@Nonnull ConditionalVariable cv, OBlock b) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getSensorAction(@Nonnull ConditionalAction ca, Sensor sn) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getTurnoutAction(@Nonnull ConditionalAction ca, Turnout tn) throws JmriException {
        System.err.format("Turnout: %s%n", tn == null ? null : tn.getSystemName());
        
        ActionTurnout action;
        
//        cv.getDataString();     // SignalMast, Memory, OBlock
//        cv.getNamedBeanData();  // Only for memory
//        cv.getNum1();   // Clock, Memory
//        cv.getNum2();   // Clock, Memory
        
        switch (ca.getType()) {
            case SET_TURNOUT:
                action = new ActionTurnout(InstanceManager.getDefault(DigitalActionManager.class)
                                .getAutoSystemName(), null);
                
                action.setTurnout(tn);
                
                switch (ca.getActionData()) {
                    case jmri.Route.TOGGLE:
                        action.setTurnoutState(ActionTurnout.TurnoutState.CLOSED);
                        break;
                        
                    case Turnout.CLOSED:
                        action.setTurnoutState(ActionTurnout.TurnoutState.CLOSED);
                        break;
                        
                    case Turnout.THROWN:
                        action.setTurnoutState(ActionTurnout.TurnoutState.THROWN);
                        break;
                        
                    default:
                        throw new InvalidConditionalVariableException(
                                Bundle.getMessage("ActionBadTurnoutState", ca.getActionData()));
                }
                break;
            case RESET_DELAYED_TURNOUT:
            case DELAYED_TURNOUT:
            case CANCEL_TURNOUT_TIMERS:
            case LOCK_TURNOUT:
            default:
                throw new InvalidConditionalVariableException(
                        Bundle.getMessage("ActionBadTurnoutType", ca.getType().toString()));
        }
        
//        ca.getActionData();
//        action.setTriggerOnChange(ca.doTriggerActions());
        
        return action;
    }
    
    
    private DigitalActionBean getMemoryAction(@Nonnull ConditionalAction ca, Memory my) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getLightAction(@Nonnull ConditionalAction ca, Light l) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getSignalHeadAction(@Nonnull ConditionalAction ca, SignalHead s) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getSignalMastAction(@Nonnull ConditionalAction ca, SignalMast sm) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getConditionalAction(@Nonnull ConditionalAction ca, Conditional c) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getWarrantAction(@Nonnull ConditionalAction ca, Warrant w) throws JmriException {
        return null;
    }
    
    
    private DigitalActionBean getOBlockAction(@Nonnull ConditionalAction ca, OBlock b) throws JmriException {
        return null;
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ImportConditional.class);

}
