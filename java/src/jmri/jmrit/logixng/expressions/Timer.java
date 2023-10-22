package jmri.jmrit.logixng.expressions;

import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TimerUtil;
import jmri.util.TypeConversionUtil;

/**
 * An expression that waits some time before returning True.
 *
 * This expression returns False until some time has elapsed. Then it returns
 * True once. After that, it returns False again until some time has elapsed.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class Timer extends AbstractDigitalExpression {

    private static class StateAndTimerTask{
        ProtectedTimerTask _timerTask;
        State _currentState = State.INIT;
    }

    private enum State { INIT, RUNNING, COMPLETED }

    private final Map<ConditionalNG, StateAndTimerTask> _stateAndTimerTask = new HashMap();
    private int _delay;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private TimerUnit _unit = TimerUnit.MilliSeconds;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;


    public Timer(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Timer copy = new Timer(sysName, userName);
        copy.setComment(getComment());
        copy.setDelayAddressing(_stateAddressing);
        copy.setDelay(_delay);
        copy.setDelayFormula(_stateFormula);
        copy.setDelayLocalVariable(_stateLocalVariable);
        copy.setDelayReference(_stateReference);
        copy.setUnit(_unit);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /**
     * Get a new timer task.
     * @param conditionalNG  the ConditionalNG
     * @param symbolTable    the symbol table
     * @param timerDelay     the time the timer should wait
     * @param timerStart     the time when the timer was started
     */
    private ProtectedTimerTask getNewTimerTask(ConditionalNG conditionalNG, SymbolTable symbolTable, long timerDelay, long timerStart) throws JmriException {

        return new ProtectedTimerTask() {
            @Override
            public void execute() {
                try {
                    synchronized(Timer.this) {
                        StateAndTimerTask stateAndTimerTask = _stateAndTimerTask.get(conditionalNG);
                        stateAndTimerTask._timerTask = null;

                        long currentTime = System.currentTimeMillis();
                        long currentTimerTime = currentTime - timerStart;
                        if (currentTimerTime < timerDelay) {
                            scheduleTimer(conditionalNG, symbolTable, timerDelay - currentTimerTime, currentTime);
                        } else {
                            stateAndTimerTask._currentState = State.COMPLETED;
                            conditionalNG.execute();
                        }
                    }
                } catch (RuntimeException | JmriException e) {
                    log.error("Exception thrown", e);
                }
            }
        };
    }

    private void scheduleTimer(ConditionalNG conditionalNG, SymbolTable symbolTable, long timerDelay, long timerStart) throws JmriException {
        synchronized(Timer.this) {
            StateAndTimerTask stateAndTimerTask = _stateAndTimerTask.get(conditionalNG);
            if (stateAndTimerTask._timerTask != null) {
                stateAndTimerTask._timerTask.stopTimer();
            }
            stateAndTimerTask._timerTask = getNewTimerTask(conditionalNG, symbolTable, timerDelay, timerStart);
            TimerUtil.schedule(stateAndTimerTask._timerTask, timerDelay);
        }
    }

    private long getNewDelay() throws JmriException {

        switch (_stateAddressing) {
            case Direct:
                return _delay;

            case Reference:
                return TypeConversionUtil.convertToLong(ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference));

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToLong(symbolTable.getValue(_stateLocalVariable));

            case Formula:
                return _stateExpressionNode != null
                        ? TypeConversionUtil.convertToLong(
                                _stateExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()))
                        : 0;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _stateAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        synchronized(this) {
            ConditionalNG conditionalNG = getConditionalNG();
            StateAndTimerTask stateAndTimerTask = _stateAndTimerTask
                    .computeIfAbsent(conditionalNG, o -> new StateAndTimerTask());
            if (stateAndTimerTask._currentState == State.RUNNING) {
                return false;
            }

            boolean completed = stateAndTimerTask._currentState == State.COMPLETED;
            stateAndTimerTask._currentState = State.RUNNING;

            if (stateAndTimerTask._timerTask != null) {
                stateAndTimerTask._timerTask.stopTimer();
            }
            long timerDelay = getNewDelay() * _unit.getMultiply();
            long timerStart = System.currentTimeMillis();
            scheduleTimer(conditionalNG, conditionalNG.getSymbolTable(), timerDelay, timerStart);

            return completed;
        }
    }

    public void setDelayAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseDelayFormula();
    }

    public NamedBeanAddressing getDelayAddressing() {
        return _stateAddressing;
    }

    /**
     * Get the delay.
     * @return the delay
     */
    public int getDelay() {
        return _delay;
    }

    /**
     * Set the delay.
     * @param delay the delay
     */
    public void setDelay(int delay) {
        _delay = delay;
    }

    public void setDelayReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _stateReference = reference;
    }

    public String getDelayReference() {
        return _stateReference;
    }

    public void setDelayLocalVariable(@Nonnull String localVariable) {
        _stateLocalVariable = localVariable;
    }

    public String getDelayLocalVariable() {
        return _stateLocalVariable;
    }

    public void setDelayFormula(@Nonnull String formula) throws ParserException {
        _stateFormula = formula;
        parseDelayFormula();
    }

    public String getDelayFormula() {
        return _stateFormula;
    }

    private void parseDelayFormula() throws ParserException {
        if (_stateAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _stateExpressionNode = parser.parseExpression(_stateFormula);
        } else {
            _stateExpressionNode = null;
        }
    }

    /**
     * Get the unit
     * @return the unit
     */
    public TimerUnit getUnit() {
        return _unit;
    }

    /**
     * Set the unit
     * @param unit the unit
     */
    public void setUnit(TimerUnit unit) {
        _unit = unit;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Timer_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String delay;

        switch (_stateAddressing) {
            case Direct:
                delay = Bundle.getMessage(locale, "Timer_DelayByDirect", _unit.getTimeWithUnit(_delay));
                break;

            case Reference:
                delay = Bundle.getMessage(locale, "Timer_DelayByReference", _stateReference, _unit.toString());
                break;

            case LocalVariable:
                delay = Bundle.getMessage(locale, "Timer_DelayByLocalVariable", _stateLocalVariable, _unit.toString());
                break;

            case Formula:
                delay = Bundle.getMessage(locale, "Timer_DelayByFormula", _stateFormula, _unit.toString());
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _stateAddressing.name());
        }

        return Bundle.getMessage(locale, "Timer_Long", delay);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Timer.class);

}
