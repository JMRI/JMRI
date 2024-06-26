package jmri.implementation;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;

/**
 * Class providing the basic logic of the Conditional interface.
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 * @author Matthew Harris copyright (C) 2009
 * @author Egbert Broerse i18n 2016
 */
public class DefaultConditional extends AbstractNamedBean
        implements Conditional {

    static final java.util.ResourceBundle rbx = java.util.ResourceBundle.getBundle("jmri.jmrit.conditional.ConditionalBundle");  // NOI18N

    private final DefaultConditionalExecute conditionalExecute;

    public DefaultConditional(String systemName, String userName) {
        super(systemName, userName);
        conditionalExecute = new DefaultConditionalExecute(this);
    }

    public DefaultConditional(String systemName) {
        super(systemName);
        conditionalExecute = new DefaultConditionalExecute(this);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameConditional");  // NOI18N
    }

    // boolean expression of state variables
    private String _antecedent = "";
    private Conditional.AntecedentOperator _logicType =
            Conditional.AntecedentOperator.ALL_AND;
    // variables (antecedent) parameters
    private List<ConditionalVariable> _variableList = new ArrayList<>();
    // actions (consequent) parameters
    protected List<ConditionalAction> _actionList = new ArrayList<>();

    private int _currentState = NamedBean.UNKNOWN;
    private boolean _triggerActionsOnChange = true;

    public static int getIndexInTable(int[] table, int entry) {
        for (int i = 0; i < table.length; i++) {
            if (entry == table[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get antecedent (boolean string expression) of Conditional.
     */
    @Override
    public String getAntecedentExpression() {
        return _antecedent;
    }

    /**
     * Get type of operators in the antecedent statement.
     */
    @Override
    public Conditional.AntecedentOperator getLogicType() {
        return _logicType;
    }

    /**
     * Set the logic type (all AND's all OR's or mixed AND's and OR's set the
     * antecedent expression - should be a well formed boolean statement with
     * parenthesis indicating the order of evaluation)
     *
     * @param type index of the logic type
     * @param antecedent non-localized (US-english) string description of antecedent
     */
    @Override
    public void setLogicType(Conditional.AntecedentOperator type, String antecedent) {
        _logicType = type;
        _antecedent = antecedent; // non-localised (universal) string description
        setState(NamedBean.UNKNOWN);
    }

    @Override
    public boolean getTriggerOnChange() {
        return _triggerActionsOnChange;
    }

    @Override
    public void setTriggerOnChange(boolean trigger) {
        _triggerActionsOnChange = trigger;
    }

    /**
     * Set State Variables for this Conditional. Each state variable will
     * evaluate either True or False when this Conditional is calculated.
     * <p>
     * This method assumes that all information has been validated.
     */
    @Override
    public void setStateVariables(List<ConditionalVariable> arrayList) {
        log.debug("Conditional \"{}\" ({}) updated ConditionalVariable list.",
                getUserName(), getSystemName());  // NOI18N
        _variableList = arrayList;
    }

    /**
     * Make deep clone of variables.
     */
    @Override
    @Nonnull
    public List<ConditionalVariable> getCopyOfStateVariables() {
        ArrayList<ConditionalVariable> variableList = new ArrayList<>();
        for (int i = 0; i < _variableList.size(); i++) {
            ConditionalVariable variable = _variableList.get(i);
            ConditionalVariable clone = new ConditionalVariable();
            clone.setNegation(variable.isNegated());
            clone.setOpern(variable.getOpern());
            clone.setType(variable.getType());
            clone.setName(variable.getName());
            clone.setDataString(variable.getDataString());
            clone.setNum1(variable.getNum1());
            clone.setNum2(variable.getNum2());
            clone.setTriggerActions(variable.doTriggerActions());
            clone.setState(variable.getState());
            clone.setGuiName(variable.getGuiName());
            variableList.add(clone);
        }
        return variableList;
    }

    /**
     * Get the list of state variables for this Conditional.
     *
     * @return the list of state variables
     */
    public List<ConditionalVariable> getStateVariableList() {
        return _variableList;
    }

    /**
     * Set list of actions.
     */
    @Override
    public void setAction(List<ConditionalAction> arrayList) {
        _actionList = arrayList;
    }

    /**
     * Make deep clone of actions.
     */
    @Override
    @Nonnull
    public List<ConditionalAction> getCopyOfActions() {
        ArrayList<ConditionalAction> actionList = new ArrayList<>();
        for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            ConditionalAction clone = new DefaultConditionalAction();
            clone.setType(action.getType());
            clone.setOption(action.getOption());
            clone.setDeviceName(action.getDeviceName());
            clone.setActionData(action.getActionData());
            clone.setActionString(action.getActionString());
            actionList.add(clone);
        }
        return actionList;
    }

    /**
     * Get the list of actions for this conditional.
     *
     * @return the list of actions
     */
    public List<ConditionalAction> getActionList() {
        return _actionList;
    }

    /**
     * Calculate this Conditional. When _enabled is false, Conditional.calculate
     * will compute the state of the conditional, but will not trigger its
     * actions. When _enabled is true, the state is computed and if the state
     * has changed, will trigger all its actions.
     */
    @Override
    public int calculate(boolean enabled, PropertyChangeEvent evt) {
        log.trace("calculate starts for {}", getSystemName());  // NOI18N

        // check if  there are no state variables
        if (_variableList.isEmpty()) {
            // if there are no state variables, no state can be calculated
            setState(NamedBean.UNKNOWN);
            return _currentState;
        }
        boolean result = true;
        switch (_logicType) {
            case ALL_AND:
                for (int i = 0; (i < _variableList.size()) && result; i++) {
                    result = _variableList.get(i).evaluate();
                }
                break;
            case ALL_OR:
                result = false;
                for (int k = 0; (k < _variableList.size()) && !result; k++) {
                    result = _variableList.get(k).evaluate();
                }
                break;
            case MIXED:
                char[] ch = _antecedent.toCharArray();
                int n = 0;
                for (int j = 0; j < ch.length; j++) {
                    if (ch[j] != ' ') {
                        if (ch[j] == '{' || ch[j] == '[') {
                            ch[j] = '(';
                        } else if (ch[j] == '}' || ch[j] == ']') {
                            ch[j] = ')';
                        }
                        ch[n++] = ch[j];
                    }
                }
                try {
                    DataPair dp = parseCalculate(new String(ch, 0, n), _variableList);
                    result = dp.result;
                } catch (NumberFormatException | IndexOutOfBoundsException | JmriException e) {
                    result = false;
                    log.error("{} parseCalculation error antecedent= {}, ex= {}", getDisplayName(), _antecedent, e,e);  // NOI18N
                }
                break;
            default:
                log.warn("Conditional {} fell through switch in calculate", getSystemName());  // NOI18N
                break;
        }
        int newState = FALSE;
        log.debug("Conditional \"{}\" ({}) has calculated its state to be {}. current state is {}. enabled= {}",
                getUserName(), getSystemName(), result, _currentState, enabled);  // NOI18N
        if (result) {
            newState = TRUE;
        }

        log.trace("   enabled starts at {}", enabled);  // NOI18N

        if (enabled) {
            if (evt != null) {
                // check if the current listener wants to (NOT) trigger actions
                enabled = wantsToTrigger(evt);
                log.trace("   wantsToTrigger sets enabled to {}", enabled);  // NOI18N
            }
        }
        if (_triggerActionsOnChange) {
            // pre 1/15/2011 on change only behavior
            if (newState == _currentState) {
                enabled = false;
                log.trace("   _triggerActionsOnChange sets enabled to false");  // NOI18N
            }
        }
        setState(newState);
        if (enabled) {
            takeActionIfNeeded();
        }
        return _currentState;
    }

    /**
     * Check if an event will trigger actions.
     *
     * @param evt the event that possibly triggers actions
     * @return true if event will trigger actions; false otherwise
     */
    boolean wantsToTrigger(PropertyChangeEvent evt) {
        try {
            String sysName = ((NamedBean) evt.getSource()).getSystemName();
            String userName = ((NamedBean) evt.getSource()).getUserName();
            for (int i = 0; i < _variableList.size(); i++) {
                if (sysName.equals(_variableList.get(i).getName())) {
                    return _variableList.get(i).doTriggerActions();
                }
            }
            if (userName != null) {
                for (int i = 0; i < _variableList.size(); i++) {
                    if (userName.equals(_variableList.get(i).getName())) {
                        return _variableList.get(i).doTriggerActions();
                    }
                }
            }
        } catch (ClassCastException e) {
            log.error("{} PropertyChangeEvent source of unexpected type: {}", getDisplayName(), evt);  // NOI18N
        }
        return true;
    }

    static class DataPair {
        boolean result = false;
        int indexCount = 0;         // index reached when parsing completed
        BitSet argsUsed = null;     // error detection for missing arguments
    }

    /**
     * Check that an antecedent is well formed.
     *
     * @param ant the antecedent string description
     * @param variableList arraylist of existing Conditional variables
     * @return error message string if not well formed
     */
    @Override
    public String validateAntecedent(String ant, List<ConditionalVariable> variableList) {
        char[] ch = ant.toCharArray();
        int n = 0;
        for (int j = 0; j < ch.length; j++) {
            if (ch[j] != ' ') {
                if (ch[j] == '{' || ch[j] == '[') {
                    ch[j] = '(';
                } else if (ch[j] == '}' || ch[j] == ']') {
                    ch[j] = ')';
                }
                ch[n++] = ch[j];
            }
        }
        int count = 0;
        for (int j = 0; j < n; j++) {
            if (ch[j] == '(') {
                count++;
            }
            if (ch[j] == ')') {
                count--;
            }
        }
        if (count > 0) {
            return java.text.MessageFormat.format(
                    rbx.getString("ParseError7"), new Object[]{')'});  // NOI18N
        }
        if (count < 0) {
            return java.text.MessageFormat.format(
                    rbx.getString("ParseError7"), new Object[]{'('});  // NOI18N
        }
        try {
            DataPair dp = parseCalculate(new String(ch, 0, n), variableList);
            if (n != dp.indexCount) {
                return java.text.MessageFormat.format(
                        rbx.getString("ParseError4"), new Object[]{ch[dp.indexCount - 1]});  // NOI18N
            }
            int index = dp.argsUsed.nextClearBit(0);
            if (index >= 0 && index < variableList.size()) {
                return java.text.MessageFormat.format(
                        rbx.getString("ParseError5"),  // NOI18N
                        new Object[]{variableList.size(), index + 1});
            }
        } catch (NumberFormatException | IndexOutOfBoundsException | JmriException nfe) {
            return rbx.getString("ParseError6") + nfe.getMessage();  // NOI18N
        }
        return null;
    }

    /**
     * Parses and computes one parenthesis level of a boolean statement.
     * <p>
     * Recursively calls inner parentheses levels. Note that all logic operators
     * are detected by the parsing, therefore the internal negation of a
     * variable is washed.
     *
     * @param s            The expression to be parsed
     * @param variableList ConditionalVariables for R1, R2, etc
     * @return a data pair consisting of the truth value of the level a count of
     *         the indices consumed to parse the level and a bitmap of the
     *         variable indices used.
     * @throws jmri.JmriException if unable to compute the logic
     */
    DataPair parseCalculate(String s, List<ConditionalVariable> variableList)
            throws JmriException {

        // for simplicity, we force the string to upper case before scanning
        s = s.toUpperCase();

        BitSet argsUsed = new BitSet(_variableList.size());
        DataPair dp = null;
        boolean leftArg = false;
        boolean rightArg = false;
        int oper = OPERATOR_NONE;
        int k = -1;
        int i = 0;      // index of String s
        //int numArgs = 0;
        if (s.charAt(i) == '(') {
            dp = parseCalculate(s.substring(++i), variableList);
            leftArg = dp.result;
            i += dp.indexCount;
            argsUsed.or(dp.argsUsed);
        } else // cannot be '('.  must be either leftArg or notleftArg
        {
            if (s.charAt(i) == 'R') {  // NOI18N
                try {
                    k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                    i += 2;
                } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                    k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                }
                leftArg = variableList.get(k - 1).evaluate();
                if (variableList.get(k - 1).isNegated()) {
                    leftArg = !leftArg;
                }
                i++;
                argsUsed.set(k - 1);
            } else if ("NOT".equals(s.substring(i, i + 3))) {  // NOI18N
                i += 3;

                // not leftArg
                if (s.charAt(i) == '(') {
                    dp = parseCalculate(s.substring(++i), variableList);
                    leftArg = dp.result;
                    i += dp.indexCount;
                    argsUsed.or(dp.argsUsed);
                } else if (s.charAt(i) == 'R') {  // NOI18N
                    try {
                        k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                        i += 2;
                    } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                        k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                    }
                    leftArg = variableList.get(k - 1).evaluate();
                    if (variableList.get(k - 1).isNegated()) {
                        leftArg = !leftArg;
                    }
                    i++;
                    argsUsed.set(k - 1);
                } else {
                    throw new JmriException(java.text.MessageFormat.format(
                            rbx.getString("ParseError1"), new Object[]{s.substring(i)}));  // NOI18N
                }
                leftArg = !leftArg;
            } else {
                throw new JmriException(java.text.MessageFormat.format(
                        rbx.getString("ParseError9"), new Object[]{s}));  // NOI18N
            }
        }
        // crank away to the right until a matching parent is reached
        while (i < s.length()) {
            if (s.charAt(i) != ')') {
                // must be either AND or OR
                if ("AND".equals(s.substring(i, i + 3))) {  // NOI18N
                    i += 3;
                    oper = OPERATOR_AND;
                } else if ("OR".equals(s.substring(i, i + 2))) {  // NOI18N
                    i += 2;
                    oper = OPERATOR_OR;
                } else {
                    throw new JmriException(java.text.MessageFormat.format(
                            rbx.getString("ParseError2"), new Object[]{s.substring(i)}));  // NOI18N
                }
                if (s.charAt(i) == '(') {
                    dp = parseCalculate(s.substring(++i), variableList);
                    rightArg = dp.result;
                    i += dp.indexCount;
                    argsUsed.or(dp.argsUsed);
                } else // cannot be '('.  must be either rightArg or notRightArg
                {
                    if (s.charAt(i) == 'R') {  // NOI18N
                        try {
                            k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                            i += 2;
                        } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                            k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                        }
                        rightArg = variableList.get(k - 1).evaluate();
                        if (variableList.get(k - 1).isNegated()) {
                            rightArg = !rightArg;
                        }
                        i++;
                        argsUsed.set(k - 1);
                    } else if ("NOT".equals(s.substring(i, i + 3))) {  // NOI18N
                        i += 3;
                        // not rightArg
                        if (s.charAt(i) == '(') {
                            dp = parseCalculate(s.substring(++i), variableList);
                            rightArg = dp.result;
                            i += dp.indexCount;
                            argsUsed.or(dp.argsUsed);
                        } else if (s.charAt(i) == 'R') {  // NOI18N
                            try {
                                k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                                i += 2;
                            } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                                k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                            }
                            rightArg = variableList.get(k - 1).evaluate();
                            if (variableList.get(k - 1).isNegated()) {
                                rightArg = !rightArg;
                            }
                            i++;
                            argsUsed.set(k - 1);
                        } else {
                            throw new JmriException(java.text.MessageFormat.format(
                                    rbx.getString("ParseError3"), new Object[]{s.substring(i)}));  // NOI18N
                        }
                        rightArg = !rightArg;
                    } else {
                        throw new JmriException(java.text.MessageFormat.format(
                                rbx.getString("ParseError9"), new Object[]{s.substring(i)}));  // NOI18N
                    }
                }
                if (oper == OPERATOR_AND) {
                    leftArg = (leftArg && rightArg);
                } else if (oper == OPERATOR_OR) {
                    leftArg = (leftArg || rightArg);
                }
            } else {  // This level done, pop recursion
                i++;
                break;
            }
        }
        dp = new DataPair();
        dp.result = leftArg;
        dp.indexCount = i;
        dp.argsUsed = argsUsed;
        return dp;
    }

    /**
     * Compares action options, and takes action if appropriate
     * <p>
     * Only get here if a change in state has occurred when calculating this
     * Conditional
     */
    private void takeActionIfNeeded() {
        if (log.isTraceEnabled()) {
            log.trace("takeActionIfNeeded starts for {}", getSystemName());  // NOI18N
        }
        Reference<Integer> actionCount = new Reference<>(0);
        int actionNeeded = 0;
        List<String> errorList = new ArrayList<>();
        // Use a local copy of state to guarantee the entire list of actions will be fired off
        // before a state change occurs that may block their completion.
        int currentState = _currentState;
        for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            int neededAction = actionNeeded;
            int option = action.getOption();
            if (log.isTraceEnabled()) {
                log.trace(" takeActionIfNeeded considers action {} with currentState: {} and option: {}", i, currentState, option);  // NOI18N
            }
            if (((currentState == TRUE) && (option == ACTION_OPTION_ON_CHANGE_TO_TRUE))
                    || ((currentState == FALSE) && (option == ACTION_OPTION_ON_CHANGE_TO_FALSE))
                    || (option == ACTION_OPTION_ON_CHANGE)) {
                // need to take this action
                actionNeeded++;
                NamedBean nb = null;
                if (action.getNamedBean() != null) {
                    nb = action.getNamedBean().getBean();
                }
                Conditional.Action type = action.getType();
                String devName = getDeviceName(action);
                if (devName == null) {
                    errorList.add("invalid memory name in action - " + action);  // NOI18N
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("getDeviceName()={} devName= {}", action.getDeviceName(), devName);  // NOI18N
                }
                switch (type) {
                    case NONE:
                        break;
                    case SET_TURNOUT:
                        conditionalExecute.setTurnout(action, (Turnout) nb, actionCount, errorList);
                        break;
                    case RESET_DELAYED_TURNOUT:
                        conditionalExecute.delayedTurnout(action, actionCount, new TimeTurnout(i), true, devName);
                        break;
                    case DELAYED_TURNOUT:
                        conditionalExecute.delayedTurnout(action, actionCount, new TimeTurnout(i), false, devName);
                        break;
                    case CANCEL_TURNOUT_TIMERS:
                        conditionalExecute.cancelTurnoutTimers(action, actionCount, errorList, devName);
                        break;
                    case LOCK_TURNOUT:
                        conditionalExecute.lockTurnout(action, (Turnout) nb, actionCount, errorList);
                        break;
                    case SET_SIGNAL_APPEARANCE:
                        conditionalExecute.setSignalAppearance(action, (SignalHead) nb, actionCount, errorList);
                        break;
                    case SET_SIGNAL_HELD:
                        conditionalExecute.setSignalHeld(action, (SignalHead) nb, actionCount, errorList);
                        break;
                    case CLEAR_SIGNAL_HELD:
                        conditionalExecute.clearSignalHeld(action, (SignalHead) nb, actionCount, errorList);
                        break;
                    case SET_SIGNAL_DARK:
                        conditionalExecute.setSignalDark(action, (SignalHead) nb, actionCount, errorList);
                        break;
                    case SET_SIGNAL_LIT:
                        conditionalExecute.setSignalLit(action, (SignalHead) nb, actionCount, errorList);
                        break;
                    case TRIGGER_ROUTE:
                        conditionalExecute.triggerRoute(action, (Route) nb, actionCount, errorList);
                        break;
                    case SET_SENSOR:
                        conditionalExecute.setSensor(action, (Sensor) nb, actionCount, errorList, devName);
                        break;
                    case RESET_DELAYED_SENSOR:
                        conditionalExecute.delayedSensor(action, actionCount, new TimeSensor(i), getMillisecondValue(action), true, devName);
                        break;
                    case DELAYED_SENSOR:
                        conditionalExecute.delayedSensor(action, actionCount, new TimeSensor(i), getMillisecondValue(action), false, devName);
                        break;
                    case CANCEL_SENSOR_TIMERS:
                        conditionalExecute.cancelSensorTimers(action, actionCount, errorList, devName);
                        break;
                    case SET_LIGHT:
                        conditionalExecute.setLight(action, (Light) nb, actionCount, errorList);
                        break;
                    case SET_LIGHT_INTENSITY:
                        conditionalExecute.setLightIntensity(action, (Light) nb, getIntegerValue(action), actionCount, errorList);
                        break;
                    case SET_LIGHT_TRANSITION_TIME:
                        conditionalExecute.setLightTransitionTime(action, (Light) nb, getIntegerValue(action), actionCount, errorList);
                        break;
                    case SET_MEMORY:
                        conditionalExecute.setMemory(action, (Memory) nb, actionCount, errorList);
                        break;
                    case COPY_MEMORY:
                        conditionalExecute.copyMemory(action, (Memory) nb, getMemory(action.getActionString()), getActionString(action), actionCount, errorList);
                        break;
                    case ENABLE_LOGIX:
                        conditionalExecute.enableLogix(action, actionCount, errorList, devName);
                        break;
                    case DISABLE_LOGIX:
                        conditionalExecute.disableLogix(action, actionCount, errorList, devName);
                        break;
                    case PLAY_SOUND:
                        conditionalExecute.playSound(action, getActionString(action), actionCount, errorList);
                        break;
                    case RUN_SCRIPT:
                        conditionalExecute.runScript(action, getActionString(action), actionCount);
                        break;
                    case SET_FAST_CLOCK_TIME:
                        conditionalExecute.setFastClockTime(action, actionCount);
                        break;
                    case START_FAST_CLOCK:
                        conditionalExecute.startFastClock(actionCount);
                        break;
                    case STOP_FAST_CLOCK:
                        conditionalExecute.stopFastClock(actionCount);
                        break;
                    case CONTROL_AUDIO:
                        conditionalExecute.controlAudio(action, devName);
                        break;
                    case JYTHON_COMMAND:
                        conditionalExecute.jythonCommand(action, getActionString(action), actionCount);
                        break;
                    case ALLOCATE_WARRANT_ROUTE:
                        conditionalExecute.allocateWarrantRoute(action, (Warrant) nb, actionCount, errorList);
                        break;
                    case DEALLOCATE_WARRANT_ROUTE:
                        conditionalExecute.deallocateWarrantRoute(action, (Warrant) nb, actionCount, errorList);
                        break;
                    case SET_ROUTE_TURNOUTS:
                        conditionalExecute.setRouteTurnouts(action, (Warrant) nb, actionCount, errorList);
                        break;
                    case GET_TRAIN_LOCATION:
                        conditionalExecute.getTrainLocation(action, (Warrant) nb, getMemory(action.getActionString()), getActionString(action), actionCount, errorList);
                        break;
                    case SET_TRAIN_ID:
                        conditionalExecute.setTrainId(action, (Warrant) nb, getActionString(action), actionCount, errorList);
                        break;
                    case SET_TRAIN_NAME:
                        conditionalExecute.setTrainName(action, (Warrant) nb, getActionString(action), actionCount, errorList);
                        break;
                    case AUTO_RUN_WARRANT:
                        conditionalExecute.autoRunWarrant(action, (Warrant) nb, actionCount, errorList);
                        break;
                    case MANUAL_RUN_WARRANT:
                        conditionalExecute.manualRunWarrant(action, (Warrant) nb, actionCount, errorList);
                        break;
                    case CONTROL_TRAIN:
                        conditionalExecute.controlTrain(action, (Warrant) nb, actionCount, errorList, devName);
                        break;
                    case SET_SIGNALMAST_ASPECT:
                        conditionalExecute.setSignalMastAspect(action, (SignalMast) nb, getActionString(action), actionCount, errorList);
                        break;
                    case SET_SIGNALMAST_HELD:
                        conditionalExecute.setSignalMastHeld(action, (SignalMast) nb, actionCount, errorList);
                        break;
                    case CLEAR_SIGNALMAST_HELD:
                        conditionalExecute.clearSignalMastHeld(action, (SignalMast) nb, actionCount, errorList);
                        break;
                    case SET_SIGNALMAST_DARK:
                        conditionalExecute.setSignalMastDark(action, (SignalMast) nb, actionCount, errorList);
                        break;
                    case SET_SIGNALMAST_LIT:
                        conditionalExecute.setSignalMastLit(action, (SignalMast) nb, actionCount, errorList);
                        break;
                    case SET_BLOCK_VALUE:
                        conditionalExecute.setBlockValue(action, (OBlock) nb, getActionString(action), actionCount, errorList);
                        break;
                    case SET_BLOCK_ERROR:
                        conditionalExecute.setBlockError(action, (OBlock) nb, actionCount, errorList);
                        break;
                    case CLEAR_BLOCK_ERROR:
                        conditionalExecute.clearBlockError(action, (OBlock) nb, errorList);
                        break;
                    case DEALLOCATE_BLOCK:
                        conditionalExecute.deallocateBlock(action, (OBlock) nb, actionCount, errorList);
                        break;
                    case SET_BLOCK_OUT_OF_SERVICE:
                        conditionalExecute.setBlockOutOfService(action, (OBlock) nb, actionCount, errorList);
                        break;
                    case SET_BLOCK_IN_SERVICE:
                        conditionalExecute.setBlockInService(action, (OBlock) nb, actionCount, errorList);
                        break;
                    case GET_BLOCK_TRAIN_NAME:
                        conditionalExecute.getBlockTrainName(action, (OBlock) nb, getMemory(action.getActionString()), getActionString(action), actionCount, errorList);
                        break;
                    case GET_BLOCK_WARRANT:
                        conditionalExecute.getBlockWarrant(action, (OBlock) nb, getMemory(action.getActionString()), getActionString(action), actionCount, errorList);
                        break;
                    case SET_NXPAIR_ENABLED:
                        conditionalExecute.setNXPairEnabled(action, actionCount, errorList, devName);
                        break;
                    case SET_NXPAIR_DISABLED:
                        conditionalExecute.setNXPairDisabled(action, actionCount, errorList, devName);
                        break;
                    case SET_NXPAIR_SEGMENT:
                        conditionalExecute.setNXPairSegment(action, actionCount, errorList, devName);
                        break;
                    default:
                        log.warn("takeActionIfNeeded drops through switch statement for action {} of {}", i, getSystemName());  // NOI18N
                        break;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Global state= {} Local state= {} - Action {} taken for action = {} {} for device {}", _currentState, currentState, actionNeeded > neededAction ? "WAS" : "NOT", action.getTypeString(), action.getActionString(), action.getDeviceName());  // NOI18N
            }
        }
        if (errorList.size() > 0) {
            for (int i = 0; i < errorList.size(); i++) {
                log.error(" error: {} - {}", getDisplayName(), errorList.get(i));
            }
            if (!GraphicsEnvironment.isHeadless()) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                if (!_skipErrorDialog) {
                    new ErrorDialog(errorList, this);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Conditional \"{}\" ({} has {} actions and has executed {} actions of {} actions needed on state change to {}", getUserName(), getSystemName(), _actionList.size(), actionCount, actionNeeded, currentState);  // NOI18N
        }
    }   // takeActionIfNeeded

    private static volatile boolean _skipErrorDialog = false;

    private static synchronized void setSkipErrorDialog( boolean skip ) {
        _skipErrorDialog = skip;
    }

    class ErrorDialog extends JDialog {

        JCheckBox rememberSession;

        ErrorDialog(List<String> list, Conditional cond) {
            super();
            setTitle("Logix Runtime Errors");  // NOI18N
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            JPanel panel = new JPanel();
            panel.add(new JLabel("Errors occurred executing Actions in Conditional:"));  // NOI18N
            contentPanel.add(panel);

            panel = new JPanel();
            panel.add(new JLabel(getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));
            contentPanel.add(panel);

            panel = new JPanel();
            panel.add(new JList<>(list.toArray(new String[0])));
            contentPanel.add(panel);

            panel = new JPanel();
            rememberSession = new JCheckBox("Skip error dialog for this session only?");  // NOI18N
            panel.add(rememberSession);
            contentPanel.add(panel);

            panel = new JPanel();
            JButton closeButton = new JButton("Close");  // NOI18N
            closeButton.addActionListener((ActionEvent a) -> {
                DefaultConditional.setSkipErrorDialog(rememberSession.isSelected());
                dispose();
            });
            panel.add(closeButton);
            contentPanel.add(panel);
            setContentPane(contentPanel);
            setLocation(250, 150);
            pack();
            setVisible(true);
        }
    }

    private String getDeviceName(ConditionalAction action) {
        String devName = action.getDeviceName();
        if (devName != null && devName.length() > 0 && devName.charAt(0) == '@') {
            String memName = devName.substring(1);
            Memory m = getMemory(memName);
            if (m == null) {
                log.error("{} invalid memory name in action - {}", getDisplayName(), devName);  // NOI18N
                return null;
            }
            devName = (String) m.getValue();
        }
        return devName;
    }

    private String getActionString(ConditionalAction action) {
        String devAction = action.getActionString();
        if (devAction != null && devAction.length() > 0 && devAction.charAt(0) == '@') {
            String memName = devAction.substring(1);
            Memory m = getMemory(memName);
            if (m == null) {
                log.error("{} action \"{}\" has invalid memory name in actionString - {}", getDisplayName(), action.getDeviceName(), action.getActionString());  // NOI18N
                return "";
            }
            devAction = (String) m.getValue();
        }
        return devAction;
    }

    /**
     * for backward compatibility with config files having system names in lower
     * case
     */
    static private Memory getMemory(String name) {
        return InstanceManager.memoryManagerInstance().getMemory(name);
    }

    /**
     * Get an integer from either a String literal or named memory reference.
     *
     * @param action an action containing either an integer or name of a Memory
     * @return the integral value of the action or -1 if the action references a
     *         Memory that does not contain an integral value
     */
    int getIntegerValue(ConditionalAction action) {
        String sNumber = action.getActionString();
        int time = 0;
        try {
            time = Integer.parseInt(sNumber);
        } catch (NumberFormatException e) {
            if (sNumber.charAt(0) == '@') {
                sNumber = sNumber.substring(1);
            }
            Memory mem = getMemory(sNumber);
            if (mem == null) {
                log.error("invalid memory name for action time variable - {}, for Action \"{}\", in Conditional \"{}\" ({})", sNumber, action.getTypeString(), getUserName(), getSystemName());  // NOI18N
                return -1;
            }
            try {
                time = Integer.parseInt((String) mem.getValue());
            } catch (NumberFormatException ex) {
                log.error("invalid action number variable from memory, \"{}\" ({}), value = {}, for Action \"{}\", in Conditional \"{}\" ({})", getUserName(), mem.getSystemName(), mem.getValue(), action.getTypeString(), getUserName(), getSystemName());  // NOI18N
                return -1;
            }
        }
        return time;
    }

    /**
     * Get the number of milliseconds from either a String literal or named
     * memory reference containing a value representing a number of seconds.
     *
     * @param action an action containing either a number of seconds or name of
     *               a Memory
     * @return the number of milliseconds represented by action of -1 if action
     *         references a Memory without a numeric value
     */
    int getMillisecondValue(ConditionalAction action) {
        String sNumber = action.getActionString();
        float time = 0;
        try {
            time = Float.parseFloat(sNumber);
        } catch (NumberFormatException e) {
            if (sNumber.charAt(0) == '@') {
                sNumber = sNumber.substring(1);
            }
            Memory mem = getMemory(sNumber);
            if (mem == null) {
                log.error("invalid memory name for action time variable - {}, for Action \"{}\", in Conditional \"{}\" ({})", sNumber, action.getTypeString(), getUserName(), getSystemName());  // NOI18N
                return -1;
            }
            try {
                time = Float.parseFloat((String) mem.getValue());
            } catch (NumberFormatException ex) {
                time = -1;
            }
            if (time <= 0) {
                log.error("invalid Millisecond value from memory, \"{}\" ({}), value = {}, for Action \"{}\", in Conditional \"{}\" ({})", getUserName(), mem.getSystemName(), mem.getValue(), action.getTypeString(), getUserName(), getSystemName());  // NOI18N
            }
        }
        return (int) (time * 1000);
    }

    /**
     * Stop a sensor timer if one is actively delaying setting of the specified
     * sensor
     */
    @Override
    public void cancelSensorTimer(String sname) {
        for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            if ((action.getType() == Conditional.Action.DELAYED_SENSOR)
                    || (action.getType() == Conditional.Action.RESET_DELAYED_SENSOR)) {
                if (action.isTimerActive()) {
                    String devName = getDeviceName(action);
                    // have active set sensor timer - is it for our sensor?
                    if (devName.equals(sname)) {
                        // yes, names match, cancel timer
                        action.stopTimer();
                    } else {
                        // check if same sensor by a different name
                        Sensor sn = InstanceManager.sensorManagerInstance().getSensor(devName);
                        if (sn == null) {
                            log.error("{} Unknown sensor *{} in cancelSensorTimer.", getDisplayName(), action.getDeviceName());  // NOI18N
                        } else if (sname.equals(sn.getSystemName())
                                || sname.equals(sn.getUserName())) {
                            // same sensor, cancel timer
                            action.stopTimer();
                        }
                    }
                }
            }
        }
    }

    /**
     * Stop a turnout timer if one is actively delaying setting of the specified
     * turnout
     */
    @Override
    public void cancelTurnoutTimer(String sname) {
        for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            if ((action.getType() == Conditional.Action.DELAYED_TURNOUT)
                    || (action.getType() == Conditional.Action.RESET_DELAYED_TURNOUT)) {
                if (action.isTimerActive()) {
                    // have active set turnout timer - is it for our turnout?
                    String devName = getDeviceName(action);
                    if (devName.equals(sname)) {
                        // yes, names match, cancel timer
                        action.stopTimer();
                    } else {
                        // check if same turnout by a different name
                        Turnout tn = InstanceManager.turnoutManagerInstance().getTurnout(devName);
                        if (tn == null) {
                            log.error("{} Unknown turnout *{} in cancelTurnoutTimer.", getDisplayName(), action.getDeviceName());  // NOI18N
                        } else if (sname.equals(tn.getSystemName())
                                || sname.equals(tn.getUserName())) {
                            // same turnout, cancel timer
                            action.stopTimer();
                        }
                    }
                }
            }
        }
    }

    /**
     * State of the Conditional is returned.
     *
     * @return state value
     */
    @Override
    public int getState() {
        return _currentState;
    }

    /**
     * State of Conditional is set. Not really public for Conditionals. The
     * state of a Conditional is only changed by its calculate method, so the
     * state is really a read-only bound property.
     *
     * @param state the new state
     */
    @Override
    public void setState(int state) {
        if (_currentState != state) {
            int oldState = _currentState;
            _currentState = state;
            firePropertyChange("KnownState", oldState, _currentState);  // NOI18N
        }
    }

    /**
     * Dispose this DefaultConditional.
     */
    @Override
    public void dispose() {
        super.dispose();
        for (int i = 0; i < _actionList.size(); i++) {
            _actionList.get(i).dispose();
        }
    }

    /**
     * Class for defining ActionListener for ACTION_DELAYED_SENSOR
     */
    class TimeSensor implements java.awt.event.ActionListener {

        public TimeSensor(int index) {
            mIndex = index;
        }

        private int mIndex = 0;

        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            // set sensor state
            ConditionalAction action = _actionList.get(mIndex);
            //String devName = getDeviceName(action);
            //Sensor sn = InstanceManager.sensorManagerInstance().getSensor(devName);
            if (action.getNamedBean() == null) {
                log.error("{} Invalid delayed sensor name - {}", getDisplayName(), action.getDeviceName());  // NOI18N
            } else {
                // set the sensor

                Sensor s = (Sensor) action.getNamedBean().getBean();
                try {
                    int act = action.getActionData();
                    if (act == Route.TOGGLE) {
                        // toggle from current state
                        int state = s.getKnownState();
                        if (state == Sensor.ACTIVE) {
                            act = Sensor.INACTIVE;
                        } else {
                            act = Sensor.ACTIVE;
                        }
                    }
                    s.setKnownState(act);
                } catch (JmriException e) {
                    log.warn("Exception setting delayed sensor {} in action", action.getDeviceName());  // NOI18N
                }
            }
            // Turn Timer OFF
            action.stopTimer();
        }
    }

    /**
     * Class for defining ActionListener for ACTION_DELAYED_TURNOUT
     */
    class TimeTurnout implements java.awt.event.ActionListener {

        public TimeTurnout(int index) {
            mIndex = index;
        }

        private int mIndex = 0;

        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            // set turnout state
            ConditionalAction action = _actionList.get(mIndex);
            if (action.getNamedBean() == null) {
                log.error("{} Invalid delayed turnout name - {}", getDisplayName(), action.getDeviceName());  // NOI18N
            } else {
                Turnout t = (Turnout) action.getNamedBean().getBean();
                int act = action.getActionData();
                if (act == Route.TOGGLE) {
                    // toggle from current state
                    int state = t.getKnownState();
                    if (state == Turnout.CLOSED) {
                        act = Turnout.THROWN;
                    } else {
                        act = Turnout.CLOSED;
                    }
                }
                // set the turnout
                t.setCommandedState(act);
            }
            // Turn Timer OFF
            action.stopTimer();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditional.class);
}
