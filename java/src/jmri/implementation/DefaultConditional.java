package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import javax.script.ScriptException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import jmri.Audio;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalManager;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.Logix;
import jmri.Memory;
import jmri.NamedBean;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.Sound;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.script.JmriScriptEngineManager;
import jmri.script.ScriptOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing the basic logic of the Conditional interface. This file is
 * part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 * @author Matthew Harris copyright (C) 2009
 * @author Egbert Broerse i18n 2016
 */
public class DefaultConditional extends AbstractNamedBean
        implements Conditional {

    static final java.util.ResourceBundle rbx = java.util.ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");  // NOI18N

    public DefaultConditional(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultConditional(String systemName) {
        super(systemName);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameConditional");  // NOI18N
    }

    // boolean expression of state variables
    private String _antecedent = "";
    private int _logicType = Conditional.ALL_AND;
    // variables (antecedent) parameters
    private ArrayList<ConditionalVariable> _variableList = new ArrayList<>();
    // actions (consequent) parameters
    protected ArrayList<ConditionalAction> _actionList = new ArrayList<>();

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
     * Get antecedent (boolean expression) of Conditional
     */
    @Override
    public String getAntecedentExpression() {
        return _antecedent;
    }

    /**
     * Get type of operators in the antecedent statement
     */
    @Override
    public int getLogicType() {
        return _logicType;
    }

    /**
     * set the logic type (all AND's all OR's or mixed AND's and OR's set the
     * antecedent expression - should be a well formed boolean statement with
     * parenthesis indicating the order of evaluation
     */
    @Override
    public void setLogicType(int type, String antecedent) {
        _logicType = type;
        _antecedent = antecedent;
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
     * <P>
     * This method assumes that all information has been validated.
     */
    @Override
    public void setStateVariables(ArrayList<ConditionalVariable> arrayList) {
        if (log.isDebugEnabled()) {
            log.debug("Conditional \"" + getUserName() + "\" (" + getSystemName()  // NOI18N
                    + ") updated ConditionalVariable list.");  // NOI18N
        }
        _variableList = arrayList;
    }

    /**
     * Make deep clone of variables
     */
    @Override
    public ArrayList<ConditionalVariable> getCopyOfStateVariables() {
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
    public ArrayList<ConditionalVariable> getStateVariableList() {
        return _variableList;
    }

    /**
     * Set list of actions
     */
    @Override
    public void setAction(ArrayList<ConditionalAction> arrayList) {
        _actionList = arrayList;
    }

    /**
     * Make deep clone of actions
     */
    @Override
    public ArrayList<ConditionalAction> getCopyOfActions() {
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
    public ArrayList<ConditionalAction> getActionList() {
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
        if (log.isTraceEnabled()) {
            log.trace("calculate starts for " + getSystemName());  // NOI18N
        }

        // check if  there are no state variables
        if (_variableList.isEmpty()) {
            // if there are no state variables, no state can be calculated
            setState(NamedBean.UNKNOWN);
            return _currentState;
        }
        boolean result = true;
        switch (_logicType) {
            case Conditional.ALL_AND:
                for (int i = 0; (i < _variableList.size()) && result; i++) {
                    result = _variableList.get(i).evaluate();
                }
                break;
            case Conditional.ALL_OR:
                result = false;
                for (int k = 0; (k < _variableList.size()) && !result; k++) {
                    result = _variableList.get(k).evaluate();
                }
                break;
            case Conditional.MIXED:
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
                } catch (NumberFormatException nfe) {
                    result = false;
                    log.error(getDisplayName() + " parseCalculation error antecedent= " + _antecedent + ", ex= " + nfe);  // NOI18N
                } catch (IndexOutOfBoundsException ioob) {
                    result = false;
                    log.error(getDisplayName() + " parseCalculation error antecedent= " + _antecedent + ", ex= " + ioob);  // NOI18N
                } catch (JmriException je) {
                    result = false;
                    log.error(getDisplayName() + " parseCalculation error antecedent= " + _antecedent + ", ex= " + je);  // NOI18N
                }
                break;
            default:
                log.warn("Conditional " + getSystemName() + " fell through switch in calculate");  // NOI18N
                break;
        }
        int newState = FALSE;
        if (log.isDebugEnabled()) {
            log.debug("Conditional \"" + getUserName() + "\" (" + getSystemName() + ") has calculated its state to be "  // NOI18N
                    + result + ". current state is " + _currentState + ".  enabled= " + enabled);  // NOI18N
        }
        if (result) {
            newState = TRUE;
        }

        if (log.isTraceEnabled()) {
            log.trace("   enabled starts at " + enabled);  // NOI18N
        }

        if (enabled) {
            if (evt != null) {
                // check if the current listener wants to (NOT) trigger actions
                enabled = wantsToTrigger(evt);
                if (log.isTraceEnabled()) {
                    log.trace("   wantsToTrigger sets enabled to " + enabled);  // NOI18N
                }
            }
        }
        if (_triggerActionsOnChange) {
            // pre 1/15/2011 on change only behavior
            if (newState == _currentState) {
                enabled = false;
                if (log.isTraceEnabled()) {
                    log.trace("   _triggerActionsOnChange sets enabled to false");  // NOI18N
                }
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
            log.error(getDisplayName() + " PropertyChangeEvent source of unexpected type: " + evt);  // NOI18N
        }
        return true;
    }

    static class DataPair {

        boolean result = false;
        int indexCount = 0;         // index reached when parsing completed
        BitSet argsUsed = null;     // error detection for missing arguments
    }

    /**
     * Check that an antecedent is well formed
     *
     */
    @Override
    public String validateAntecedent(String ant, ArrayList<ConditionalVariable> variableList) {
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
    DataPair parseCalculate(String s, ArrayList<ConditionalVariable> variableList)
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
            if (s.charAt(i) == 'R') { //NOI18N
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
            } else if (Bundle.getMessage("LogicNOT").equals(s.substring(i, i + (Bundle.getMessage("LogicNOT").length())))) { // compare the right length after i18n  // NOI18N
                i += Bundle.getMessage("LogicNOT").length(); // was: 3;  // NOI18N
                //not leftArg
                if (s.charAt(i) == '(') {
                    dp = parseCalculate(s.substring(++i), variableList);
                    leftArg = dp.result;
                    i += dp.indexCount;
                    argsUsed.or(dp.argsUsed);
                } else if (s.charAt(i) == 'R') { //NOI18N
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
                if (Bundle.getMessage("LogicAND").equals(s.substring(i, i + (Bundle.getMessage("LogicAND").length())))) { // compare the right length after i18n  // NOI18N
                    i += Bundle.getMessage("LogicAND").length(); // EN AND: 3;  // NOI18N
                    oper = OPERATOR_AND;
                } else if (Bundle.getMessage("LogicOR").equals(s.substring(i, i + (Bundle.getMessage("LogicOR").length())))) { // compare the right length after i18n  // NOI18N
                    i += Bundle.getMessage("LogicOR").length(); // EN OR: 2;  // NOI18N
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
                    if (s.charAt(i) == 'R') { //NOI18N
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
                    } else if ((i + 3) < s.length() && Bundle.getMessage("LogicNOT").equals(s.substring(i, i + (Bundle.getMessage("LogicNOT").length())))) { // compare the right length after i18n  // NOI18N
                        i += Bundle.getMessage("LogicNOT").length(); // EN NOT: 3;  // NOI18N
                        //not rightArg
                        if (s.charAt(i) == '(') {
                            dp = parseCalculate(s.substring(++i), variableList);
                            rightArg = dp.result;
                            i += dp.indexCount;
                            argsUsed.or(dp.argsUsed);
                        } else if (s.charAt(i) == 'R') { //NOI18N
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
     * <P>
     * Only get here if a change in state has occurred when calculating this
     * Conditional
     */
    @SuppressWarnings({"deprecation", "fallthrough"})  // NOI18N
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")  // NOI18N
    // it's unfortunate that this is such a huge method, because these annotation
    // have to apply to more than 500 lines of code - jake
    private void takeActionIfNeeded() {
        if (log.isTraceEnabled()) {
            log.trace("takeActionIfNeeded starts for " + getSystemName());  // NOI18N
        }
        int actionCount = 0;
        int actionNeeded = 0;
        int act = 0;
        int state = 0;
        ArrayList<String> errorList = new ArrayList<>();
        // Use a local copy of state to guarantee the entire list of actions will be fired off
        // before a state change occurs that may block their completion.
        int currentState = _currentState;
        for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            int neededAction = actionNeeded;
            int option = action.getOption();
            if (log.isTraceEnabled()) {
                log.trace(" takeActionIfNeeded considers action " + i + " with currentState: " + currentState + " and option: " + option);  // NOI18N
            }
            if (((currentState == TRUE) && (option == ACTION_OPTION_ON_CHANGE_TO_TRUE))
                    || ((currentState == FALSE) && (option == ACTION_OPTION_ON_CHANGE_TO_FALSE))
                    || (option == ACTION_OPTION_ON_CHANGE)) {
                // need to take this action
                actionNeeded++;
                SignalHead h = null;
                SignalMast f = null;
                Logix x = null;
                Light lgt = null;
                Warrant w = null;
                NamedBean nb = null;
                if (action.getNamedBean() != null) {
                    nb = action.getNamedBean().getBean();
                }
                int value = 0;
                Timer timer = null;
                int type = action.getType();
                String devName = getDeviceName(action);
                if (devName == null) {
                    errorList.add("invalid memory name in action - " + action);  // NOI18N
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("getDeviceName()=" + action.getDeviceName() + " devName= " + devName);  // NOI18N
                }
                switch (type) {
                    case Conditional.ACTION_NONE:
                        break;
                    case Conditional.ACTION_SET_TURNOUT:
                        Turnout t = (Turnout) nb;
                        if (t == null) {
                            errorList.add("invalid turnout name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                state = t.getKnownState();
                                if (state == Turnout.CLOSED) {
                                    act = Turnout.THROWN;
                                } else {
                                    act = Turnout.CLOSED;
                                }
                            }
                            t.setCommandedState(act);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                        action.stopTimer();
                    // fall through
                    case Conditional.ACTION_DELAYED_TURNOUT:
                        if (!action.isTimerActive()) {
                            // Create a timer if one does not exist
                            timer = action.getTimer();
                            if (timer == null) {
                                action.setListener(new TimeTurnout(i));
                                timer = new Timer(2000, action.getListener());
                                timer.setRepeats(true);
                            }
                            // Start the Timer to set the turnout
                            value = getMillisecondValue(action);
                            if (value < 0) {
                                break;
                            }
                            timer.setInitialDelay(value);
                            action.setTimer(timer);
                            action.startTimer();
                            actionCount++;
                        } else {
                            log.warn("timer already active on request to start delayed turnout action - "  // NOI18N
                                    + devName);
                        }
                        break;
                    case Conditional.ACTION_CANCEL_TURNOUT_TIMERS:
                        ConditionalManager cmg = jmri.InstanceManager.getDefault(jmri.ConditionalManager.class);
                        java.util.Iterator<String> iter = cmg.getSystemNameList().iterator();
                        while (iter.hasNext()) {
                            String sname = iter.next();
                            if (sname == null) {
                                errorList.add("Conditional system name null during cancel turnout timers for "  // NOI18N
                                        + action.getDeviceName());
                            }
                            Conditional c = cmg.getBySystemName(sname);
                            if (c == null) {
                                errorList.add("Conditional null during cancel turnout timers for "  // NOI18N
                                        + action.getDeviceName());
                            } else {
                                c.cancelTurnoutTimer(devName);
                                actionCount++;
                            }
                        }
                        break;
                    case Conditional.ACTION_LOCK_TURNOUT:
                        Turnout tl = (Turnout) nb;
                        if (tl == null) {
                            errorList.add("invalid turnout name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                if (tl.getLocked(Turnout.CABLOCKOUT)) {
                                    act = Turnout.UNLOCKED;
                                } else {
                                    act = Turnout.LOCKED;
                                }
                            }
                            if (act == Turnout.LOCKED) {
                                tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
                            } else if (act == Turnout.UNLOCKED) {
                                tl.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNAL_APPEARANCE:
                        h = (SignalHead) nb;
                        if (h == null) {
                            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            h.setAppearance(action.getActionData());
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNAL_HELD:
                        h = (SignalHead) nb;
                        if (h == null) {
                            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            h.setHeld(true);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_CLEAR_SIGNAL_HELD:
                        h = (SignalHead) nb;
                        if (h == null) {
                            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            h.setHeld(false);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNAL_DARK:
                        h = (SignalHead) nb;
                        if (h == null) {
                            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            h.setLit(false);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNAL_LIT:
                        h = (SignalHead) nb;
                        if (h == null) {
                            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            h.setLit(true);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_TRIGGER_ROUTE:
                        Route r = (Route) nb;
                        if (r == null) {
                            errorList.add("invalid Route name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            r.setRoute();
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SENSOR:
                        Sensor sn = (Sensor) nb;
                        if (sn == null) {
                            errorList.add("invalid Sensor name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                state = sn.getState();
                                if (state == Sensor.ACTIVE) {
                                    act = Sensor.INACTIVE;
                                } else {
                                    act = Sensor.ACTIVE;
                                }
                            }
                            try {
                                sn.setKnownState(act);
                                actionCount++;
                            } catch (JmriException e) {
                                log.warn("Exception setting Sensor " + devName + " in action");  // NOI18N
                            }
                        }
                        break;
                    case Conditional.ACTION_RESET_DELAYED_SENSOR:
                        action.stopTimer();
                    // fall through
                    case Conditional.ACTION_DELAYED_SENSOR:
                        if (!action.isTimerActive()) {
                            // Create a timer if one does not exist
                            timer = action.getTimer();
                            if (timer == null) {
                                action.setListener(new TimeSensor(i));
                                timer = new Timer(2000, action.getListener());
                                timer.setRepeats(true);
                            }
                            // Start the Timer to set the turnout
                            value = getMillisecondValue(action);
                            if (value < 0) {
                                break;
                            }
                            timer.setInitialDelay(value);
                            action.setTimer(timer);
                            action.startTimer();
                            actionCount++;
                        } else {
                            log.warn("timer already active on request to start delayed sensor action - "  // NOI18N
                                    + devName);
                        }
                        break;
                    case Conditional.ACTION_CANCEL_SENSOR_TIMERS:
                        ConditionalManager cm = jmri.InstanceManager.getDefault(jmri.ConditionalManager.class);
                        java.util.Iterator<String> itr = cm.getSystemNameList().iterator();
                        while (itr.hasNext()) {
                            String sname = itr.next();
                            if (sname == null) {
                                errorList.add("Conditional system name null during cancel sensor timers for "  // NOI18N
                                        + action.getDeviceName());
                            }
                            Conditional c = cm.getBySystemName(sname);
                            if (c == null) {
                                errorList.add("Conditional null during cancel sensor timers for "  // NOI18N
                                        + action.getDeviceName());
                            } else {
                                c.cancelSensorTimer(devName);
                                actionCount++;
                            }
                        }
                        break;
                    case Conditional.ACTION_SET_LIGHT:
                        lgt = (Light) nb;
                        if (lgt == null) {
                            errorList.add("invalid light name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            act = action.getActionData();
                            if (act == Route.TOGGLE) {
                                state = lgt.getState();
                                if (state == Light.ON) {
                                    act = Light.OFF;
                                } else {
                                    act = Light.ON;
                                }
                            }
                            lgt.setState(act);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_LIGHT_INTENSITY:
                        lgt = (Light) nb;
                        if (lgt == null) {
                            errorList.add("invalid light name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            try {
                                value = getIntegerValue(action);
                                if (value < 0) {
                                    break;
                                }
                                lgt.setTargetIntensity((value) / 100.0);
                                actionCount++;
                            } catch (IllegalArgumentException e) {
                                errorList.add("Exception in set light intensity action - " + action.getDeviceName());  // NOI18N
                            }
                        }
                        break;
                    case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                        lgt = (Light) nb;
                        if (lgt == null) {
                            errorList.add("invalid light name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            try {
                                value = getIntegerValue(action);
                                if (value < 0) {
                                    break;
                                }
                                lgt.setTransitionTime(value);
                                actionCount++;
                            } catch (IllegalArgumentException e) {
                                errorList.add("Exception in set light transition time action - " + action.getDeviceName());  // NOI18N
                            }
                        }
                        break;
                    case Conditional.ACTION_SET_MEMORY:
                        Memory m = (Memory) nb;
                        if (m == null) {
                            errorList.add("invalid memory name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            m.setValue(action.getActionString());
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_COPY_MEMORY:
                        Memory mFrom = (Memory) nb;
                        if (mFrom == null) {
                            errorList.add("invalid memory name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            Memory mTo = getMemory(action.getActionString());
                            if (mTo == null) {
                                errorList.add("invalid memory name in action - " + action.getActionString());  // NOI18N
                            } else {
                                mTo.setValue(mFrom.getValue());
                                actionCount++;
                            }
                        }
                        break;
                    case Conditional.ACTION_ENABLE_LOGIX:
                        x = InstanceManager.getDefault(jmri.LogixManager.class).getLogix(devName);
                        if (x == null) {
                            errorList.add("invalid logix name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            x.setEnabled(true);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_DISABLE_LOGIX:
                        x = InstanceManager.getDefault(jmri.LogixManager.class).getLogix(devName);
                        if (x == null) {
                            errorList.add("invalid logix name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            x.setEnabled(false);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_PLAY_SOUND:
                        String path = getActionString(action);
                        if (!path.equals("")) {
                            Sound sound = action.getSound();
                            if (sound == null) {
                                try {
                                    sound = new Sound(path);
                                } catch (NullPointerException ex) {
                                    errorList.add("invalid path to sound: " + path);  // NOI18N
                                }
                            }
                            if (sound != null) {
                                sound.play();
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_RUN_SCRIPT:
                        if (!(getActionString(action).equals(""))) {
                            JmriScriptEngineManager.getDefault().runScript(new File(jmri.util.FileUtil.getExternalFilename(getActionString(action))));
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_FAST_CLOCK_TIME:
                        Date date = InstanceManager.getDefault(jmri.Timebase.class).getTime();
                        date.setHours(action.getActionData() / 60);
                        date.setMinutes(action.getActionData() - ((action.getActionData() / 60) * 60));
                        date.setSeconds(0);
                        InstanceManager.getDefault(jmri.Timebase.class).userSetTime(date);
                        actionCount++;
                        break;
                    case Conditional.ACTION_START_FAST_CLOCK:
                        InstanceManager.getDefault(jmri.Timebase.class).setRun(true);
                        actionCount++;
                        break;
                    case Conditional.ACTION_STOP_FAST_CLOCK:
                        InstanceManager.getDefault(jmri.Timebase.class).setRun(false);
                        actionCount++;
                        break;
                    case Conditional.ACTION_CONTROL_AUDIO:
                        Audio audio = InstanceManager.getDefault(jmri.AudioManager.class).getAudio(devName);
                        if (audio == null) {
                            break;
                        }
                        if (audio.getSubType() == Audio.SOURCE) {
                            AudioSource audioSource = (AudioSource) audio;
                            switch (action.getActionData()) {
                                case Audio.CMD_PLAY:
                                    audioSource.play();
                                    break;
                                case Audio.CMD_STOP:
                                    audioSource.stop();
                                    break;
                                case Audio.CMD_PLAY_TOGGLE:
                                    audioSource.togglePlay();
                                    break;
                                case Audio.CMD_PAUSE:
                                    audioSource.pause();
                                    break;
                                case Audio.CMD_RESUME:
                                    audioSource.resume();
                                    break;
                                case Audio.CMD_PAUSE_TOGGLE:
                                    audioSource.togglePause();
                                    break;
                                case Audio.CMD_REWIND:
                                    audioSource.rewind();
                                    break;
                                case Audio.CMD_FADE_IN:
                                    audioSource.fadeIn();
                                    break;
                                case Audio.CMD_FADE_OUT:
                                    audioSource.fadeOut();
                                    break;
                                case Audio.CMD_RESET_POSITION:
                                    audioSource.resetCurrentPosition();
                                    break;
                                default:
                                    break;
                            }
                        } else if (audio.getSubType() == Audio.LISTENER) {
                            AudioListener audioListener = (AudioListener) audio;
                            switch (action.getActionData()) {
                                case Audio.CMD_RESET_POSITION:
                                    audioListener.resetCurrentPosition();
                                    break;
                                default:
                                    break; // nothing needed for others
                            }
                        }
                        break;
                    case Conditional.ACTION_JYTHON_COMMAND:
                        if (!(getActionString(action).isEmpty())) {
                            // add the text to the output frame
                            ScriptOutput.writeScript(getActionString(action));
                            // and execute
                            try {
                                JmriScriptEngineManager.getDefault().eval(getActionString(action), JmriScriptEngineManager.getDefault().getEngine(JmriScriptEngineManager.PYTHON));
                            } catch (ScriptException ex) {
                                log.error("Error executing script:", ex);  // NOI18N
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_ALLOCATE_WARRANT_ROUTE:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            String msg = w.allocateRoute(null);
                            if (msg != null) {
                                log.info("Warrant " + action.getDeviceName() + " - " + msg);  // NOI18N
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            w.deAllocate();
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_ROUTE_TURNOUTS:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            String msg = w.setRoute(0, null);
                            if (msg != null) {
                                log.info("Warrant " + action.getDeviceName() + " unable to Set Route - " + msg);  // NOI18N
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_THROTTLE_FACTOR:
                        log.info("Set warrant Throttle Factor deprecated - Use Warrrant Preferences");  // NOI18N
                        break;
                    case Conditional.ACTION_SET_TRAIN_ID:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            w.getSpeedUtil().setDccAddress(getActionString(action));
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_TRAIN_NAME:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            w.setTrainName(getActionString(action));
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_AUTO_RUN_WARRANT:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            jmri.jmrit.logix.WarrantTableFrame frame = jmri.jmrit.logix.WarrantTableFrame.getDefault();
                            String err = frame.runTrain(w, Warrant.MODE_RUN);
                            if (err != null) {
                                w.stopWarrant(true);
                            }
                            if (err != null) {
                                errorList.add("runAutoTrain error - " + err);  // NOI18N
                                w.stopWarrant(true);
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_MANUAL_RUN_WARRANT:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            String err = w.setRoute(0, null);
                            if (err == null) {
                                err = w.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
                            }
                            if (err != null) {
                                errorList.add("runManualTrain error - " + err);  // NOI18N
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_CONTROL_TRAIN:
                        w = (Warrant) nb;
                        if (w == null) {
                            errorList.add("invalid Warrant name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            if (!w.controlRunTrain(action.getActionData())) {
                                log.info("Train " + w.getSpeedUtil().getRosterId() + " not running  - " + devName);  // NOI18N
                            }
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNALMAST_ASPECT:
                        f = (SignalMast) nb;
                        if (f == null) {
                            errorList.add("invalid Signal Mast name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            f.setAspect(getActionString(action));
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNALMAST_HELD:
                        f = (SignalMast) nb;
                        if (f == null) {
                            errorList.add("invalid Signal Mast name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            f.setHeld(true);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_CLEAR_SIGNALMAST_HELD:
                        f = (SignalMast) nb;
                        if (f == null) {
                            errorList.add("invalid Signal Mast name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            f.setHeld(false);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNALMAST_DARK:
                        f = (SignalMast) nb;
                        if (f == null) {
                            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            f.setLit(false);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_SIGNALMAST_LIT:
                        f = (SignalMast) nb;
                        if (f == null) {
                            errorList.add("invalid Signal Head name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            f.setLit(true);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_BLOCK_VALUE:
                        OBlock b = (OBlock) nb;
                        if (b == null) {
                            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            b.setValue(getActionString(action));
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_SET_BLOCK_ERROR:
                        b = (OBlock) nb;
                        if (b == null) {
                            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            b.setError(true);
                            actionCount++;
                        }
                        break;
                    case Conditional.ACTION_CLEAR_BLOCK_ERROR:
                        b = (OBlock) nb;
                        if (b == null) {
                            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            b.setError(false);
                        }
                        break;
                    case ACTION_DEALLOCATE_BLOCK:
                        b = (OBlock) nb;
                        if (b == null) {
                            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            b.deAllocate(null);
                            actionCount++;
                        }
                        break;
                    case ACTION_SET_BLOCK_OUT_OF_SERVICE:
                        b = (OBlock) nb;
                        if (b == null) {
                            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            b.setOutOfService(true);
                            actionCount++;
                        }
                        break;
                    case ACTION_SET_BLOCK_IN_SERVICE:
                        b = (OBlock) nb;
                        if (b == null) {
                            errorList.add("invalid Block name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            b.setOutOfService(false);
                            actionCount++;
                        }
                        break;
                    case ACTION_SET_NXPAIR_ENABLED:
                        DestinationPoints dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(devName);
                        if (dp == null) {
                            errorList.add("Invalid NX Pair name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            dp.setEnabled(true);
                            actionCount++;
                        }
                        break;
                    case ACTION_SET_NXPAIR_DISABLED:
                        dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(devName);
                        if (dp == null) {
                            errorList.add("Invalid NX Pair name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            dp.setEnabled(false);
                            actionCount++;
                        }
                        break;
                    case ACTION_SET_NXPAIR_SEGMENT:
                        dp = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(devName);
                        if (dp == null) {
                            errorList.add("Invalid NX Pair name in action - " + action.getDeviceName());  // NOI18N
                        } else {
                            jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                                    setSingleSegmentRoute(devName);
                            actionCount++;
                        }
                        break;
                    default:
                        log.warn("takeActionIfNeeded drops through switch statement for action " + i + " of " + getSystemName());  // NOI18N
                        break;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Global state= " + _currentState + " Local state= " + currentState  // NOI18N
                        + " - Action " + (actionNeeded > neededAction ? "WAS" : "NOT")  // NOI18N
                        + " taken for action = " + action.getTypeString() + " " + action.getActionString()  // NOI18N
                        + " for device " + action.getDeviceName());  // NOI18N
            }
        }
        if (errorList.size() > 0) {
            for (int i = 0; i < errorList.size(); i++) {
                log.error(getDisplayName() + " - " + errorList.get(i));
            }
            if (!GraphicsEnvironment.isHeadless()) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                if (!_skipErrorDialog) {
                    new ErrorDialog(errorList, this);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Conditional \"" + getUserName() + "\" (" + getSystemName() + " has " + _actionList.size()  // NOI18N
                    + " actions and has executed " + actionCount  // NOI18N
                    + " actions of " + actionNeeded + " actions needed on state change to " + currentState);  // NOI18N
        }
    }   // takeActionIfNeeded

    static private boolean _skipErrorDialog = false;

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
            panel.add(new JLabel(getUserName() + " (" + getSystemName() + ")"));
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
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (rememberSession.isSelected()) {
                        _skipErrorDialog = true;
                    }
                    dispose();
                }
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
                log.error(getDisplayName() + " invalid memory name in action - " + devName);  // NOI18N
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
                log.error(getDisplayName() + " action \"" + action.getDeviceName()  // NOI18N
                        + "\" has invalid memory name in actionString - " + action.getActionString());  // NOI18N
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
        Memory m = InstanceManager.memoryManagerInstance().getMemory(name);
        if (m == null) {
            String sName = name.toUpperCase().trim();  // N11N
            m = InstanceManager.memoryManagerInstance().getMemory(sName);
        }
        return m;
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
                log.error("invalid memory name for action time variable - " + sNumber  // NOI18N
                        + ", for Action \"" + action.getTypeString()  // NOI18N
                        + "\", in Conditional \"" + getUserName() + "\" (" + getSystemName() + ")");  // NOI18N
                return -1;
            }
            try {
                time = Integer.parseInt((String) mem.getValue());
            } catch (NumberFormatException ex) {
                log.error("invalid action number variable from memory, \""  // NOI18N
                        + getUserName() + "\" (" + mem.getSystemName() + "), value = " + (String) mem.getValue()  // NOI18N
                        + ", for Action \"" + action.getTypeString()  // NOI18N
                        + "\", in Conditional \"" + getUserName() + "\" (" + getSystemName() + ")");  // NOI18N
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
                log.error("invalid memory name for action time variable - " + sNumber  // NOI18N
                        + ", for Action \"" + action.getTypeString()  // NOI18N
                        + "\", in Conditional \"" + getUserName() + "\" (" + getSystemName() + ")");  // NOI18N
                return -1;
            }
            try {
                time = Float.parseFloat((String) mem.getValue());
            } catch (NumberFormatException ex) {
                time = -1;
            }
            if (time <= 0) {
                log.error("invalid Millisecond value from memory, \""  // NOI18N
                        + getUserName() + "\" (" + mem.getSystemName() + "), value = " + (String) mem.getValue()  // NOI18N
                        + ", for Action \"" + action.getTypeString()  // NOI18N
                        + "\", in Conditional \"" + getUserName() + "\" (" + getSystemName() + ")");  // NOI18N
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
            if ((action.getType() == Conditional.ACTION_DELAYED_SENSOR)
                    || (action.getType() == Conditional.ACTION_RESET_DELAYED_SENSOR)) {
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
                            log.error(getDisplayName() + " Unknown sensor *" + action.getDeviceName() + " in cancelSensorTimer.");  // NOI18N
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
            if ((action.getType() == Conditional.ACTION_DELAYED_TURNOUT)
                    || (action.getType() == Conditional.ACTION_RESET_DELAYED_TURNOUT)) {
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
                            log.error(getDisplayName() + " Unknown turnout *" + action.getDeviceName() + " in cancelTurnoutTimer.");  // NOI18N
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
                log.error(getDisplayName() + " Invalid delayed sensor name - " + action.getDeviceName());  // NOI18N
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
                    log.warn("Exception setting delayed sensor " + action.getDeviceName() + " in action");  // NOI18N
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
                log.error(getDisplayName() + " Invalid delayed turnout name - " + action.getDeviceName());  // NOI18N
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

    private final static Logger log = LoggerFactory.getLogger(DefaultConditional.class);
}
