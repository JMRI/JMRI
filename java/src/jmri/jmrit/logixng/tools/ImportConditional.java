package jmri.jmrit.logixng.tools;

import java.util.List;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Logix;
import jmri.Memory;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.actions.Many;
import jmri.jmrit.logixng.digital.expressions.And;
import jmri.jmrit.logixng.digital.expressions.Antecedent;
import jmri.jmrit.logixng.digital.expressions.Or;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalActionBean;

/**
 * Imports Logixs to LogixNG
 * 
 * @author Daniel Bergqvist 2019
 */
public class ImportConditional {

//    private final Logix _logix;
    private final Conditional _conditional;
//    private final LogixNG _logixNG;
    private final ConditionalNG _conditionalNG;
    
    public ImportConditional(Logix logix, Conditional conditional, LogixNG logixNG, String conditionalNG_SysName) {
        
//        _logix = logix;
        _conditional = conditional;
//        _logixNG = logixNG;
//        _conditionalNG = new DefaultConditionalNG(conditionalNG_SysName, null);
        _conditionalNG = InstanceManager.getDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class)
                .createConditionalNG(conditionalNG_SysName, null);
        
//        log.debug("Import Logix {} to LogixNG {}", _logix.getSystemName(), _logixNG.getSystemName());
        log.error("AA: Import Conditional {} to ConditionalNG {}", _conditional.getSystemName(), _conditionalNG.getSystemName());
    }
    
    public void doImport() throws SocketAlreadyConnectedException, Exception {
        boolean triggerOnChange = _conditional.getTriggerOnChange();
        IfThenElse.Type type = triggerOnChange ? IfThenElse.Type.TRIGGER_ACTION : IfThenElse.Type.CONTINOUS_ACTION;
        
        // This will fail, but fix it later.
        IfThenElse ifThen = new IfThenElse("", null, type);
        
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
                // This will fail, but fix it later.
                expression = new Antecedent("", null);
                ((Antecedent)expression).setAntecedent(antecedentExpression);
                break;
            default:
                return;
        }
        buildExpression(expression, conditionalVariables);
        
        DigitalActionBean action = new Many(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        buildAction(action, conditionalActions);
        
        MaleSocket expressionSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        ifThen.getChild(0).connect(expressionSocket);
        
        MaleSocket ifThenAction = InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThen);
        _conditionalNG.getChild(0).connect(ifThenAction);
    }
    
    
    private void buildExpression(DigitalExpressionBean expression, List<ConditionalVariable> conditionalVariables) throws SocketAlreadyConnectedException {
        for (int i=0; i < conditionalVariables.size(); i++) {
            ConditionalVariable cv = conditionalVariables.get(i);
            NamedBean nb = cv.getNamedBeanData();
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
    
    
    private void buildAction(DigitalActionBean action, List<ConditionalAction> conditionalActions) throws SocketAlreadyConnectedException {
        for (int i=0; i < conditionalActions.size(); i++) {
            ConditionalAction ca = conditionalActions.get(i);
            NamedBean nb = ca.getBean();
            DigitalActionBean newAction;
            switch (ca.getType().getItemType()) {
                case SENSOR:
                    Sensor sn = (Sensor)nb;
                    newAction = getSensorAction(ca, sn);
                    break;
                case TURNOUT:
                    Turnout tn = (Turnout)nb;
                    newAction = getTurnoutAction(ca, tn);
                    break;
                case MEMORY:
                    Memory my = (Memory)nb;
                    newAction = getMemoryAction(ca, my);
                    break;
                case LIGHT:
                    Light l = (Light)nb;
                    newAction = getLightAction(ca, l);
                    break;
                case SIGNALHEAD:
                    SignalHead s = (SignalHead)nb;
                    newAction = getSignalHeadAction(ca, s);
                    break;
                case SIGNALMAST:
                    SignalMast sm = (SignalMast)nb;
                    newAction = getSignalMastAction(ca, sm);
                    break;
                case ENTRYEXIT:
//                    NamedBean nb = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getBySystemName(_name);
//                    newAction = getSensorAction(cv, sn);
                    newAction = null;
                    break;
                case CONDITIONAL:
                    Conditional c = (Conditional)nb;
                    newAction = getConditionalAction(ca, c);
                    break;
                case WARRANT:
                    Warrant w = (Warrant)nb;
                    newAction = getWarrantAction(ca, w);
                    break;
                case OBLOCK:
                    OBlock b = (OBlock)nb;
                    newAction = getOBlockAction(ca, b);
                    break;

                default:
                    newAction = null;
                    log.warn("Unexpected type in ImportConditional.doImport(): {} -> {}", ca.getType(), ca.getType().getItemType());
                    break;
            }
            
            if (newAction != null) {
                MaleSocket newActionSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(newAction);
                action.getChild(i).connect(newActionSocket);
            }
        }
    }
    
    
    private DigitalExpressionBean getSensorExpression(ConditionalVariable cv, Sensor sn) {
        return null;
    }
    
    
    private DigitalExpressionBean getTurnoutExpression(ConditionalVariable cv, Turnout tn) {
        return null;
    }
    
    
    private DigitalExpressionBean getMemoryExpression(ConditionalVariable cv, Memory my) {
        return null;
    }
    
    
    private DigitalExpressionBean getLightExpression(ConditionalVariable cv, Light l) {
        return null;
    }
    
    
    private DigitalExpressionBean getSignalHeadExpression(ConditionalVariable cv, SignalHead s) {
        return null;
    }
    
    
    private DigitalExpressionBean getSignalMastExpression(ConditionalVariable cv, SignalMast sm) {
        return null;
    }
    
    
    private DigitalExpressionBean getConditionalExpression(ConditionalVariable cv, Conditional c) {
        return null;
    }
    
    
    private DigitalExpressionBean getWarrantExpression(ConditionalVariable cv, Warrant w) {
        return null;
    }
    
    
    private DigitalExpressionBean getOBlockExpression(ConditionalVariable cv, OBlock b) {
        return null;
    }
    
    
    private DigitalActionBean getSensorAction(ConditionalAction ca, Sensor sn) {
        return null;
    }
    
    
    private DigitalActionBean getTurnoutAction(ConditionalAction ca, Turnout tn) {
        return null;
    }
    
    
    private DigitalActionBean getMemoryAction(ConditionalAction ca, Memory my) {
        return null;
    }
    
    
    private DigitalActionBean getLightAction(ConditionalAction ca, Light l) {
        return null;
    }
    
    
    private DigitalActionBean getSignalHeadAction(ConditionalAction ca, SignalHead s) {
        return null;
    }
    
    
    private DigitalActionBean getSignalMastAction(ConditionalAction ca, SignalMast sm) {
        return null;
    }
    
    
    private DigitalActionBean getConditionalAction(ConditionalAction ca, Conditional c) {
        return null;
    }
    
    
    private DigitalActionBean getWarrantAction(ConditionalAction ca, Warrant w) {
        return null;
    }
    
    
    private DigitalActionBean getOBlockAction(ConditionalAction ca, OBlock b) {
        return null;
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ImportConditional.class);

}
