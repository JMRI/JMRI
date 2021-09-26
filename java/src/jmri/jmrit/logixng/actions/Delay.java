package jmri.jmrit.logixng.actions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.jmrit.logixng.util.parser.Variable;
import jmri.util.TypeConversionUtil;


/**
 * Delay
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class Delay extends AbstractDigitalAction {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private int _time = 0;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private NamedBeanAddressing _timeUnitAddressing = NamedBeanAddressing.Direct;
    private TimeUnit _timeUnit = TimeUnit.Milliseconds;
    private String _timeUnitReference = "";
    private String _timeUnitLocalVariable = "";
    private String _timeUnitFormula = "";
    private ExpressionNode _timeUnitExpressionNode;

    public Delay(String sys, String user) {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Delay copy = new Delay(sysName, userName);
        copy.setComment(getComment());
        
        copy.setTime(_time);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);

        copy.setTimeUnitAddressing(_timeUnitAddressing);
        copy.setTimeUnit(_timeUnit);
        copy.setTimeUnitFormula(_timeUnitFormula);
        copy.setTimeUnitLocalVariable(_timeUnitLocalVariable);
        copy.setTimeUnitReference(_timeUnitReference);
        
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    private long getCurrentTime() throws JmriException {

        switch (_addressing) {
            case Reference:
                return Long.parseLong(ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference));

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToLong(symbolTable.getValue(_localVariable));

            case Formula:
                return _expressionNode != null
                        ? TypeConversionUtil.convertToLong(
                                _expressionNode.calculate(
                                        getConditionalNG().getSymbolTable()))
                        : 0;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _timeUnitAddressing.name());
        }
    }
    
    private TimeUnit getCurrentTimeUnit() throws JmriException {

        switch (_timeUnitAddressing) {
            case Direct:
                return _timeUnit;
                
            case Reference:
                return TimeUnit.valueOf(ReferenceUtil.getReference(getConditionalNG().getSymbolTable(), _timeUnitReference));
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TimeUnit.valueOf(TypeConversionUtil
                        .convertToString(symbolTable.getValue(_timeUnitLocalVariable), false));
                
            case Formula:
                return _timeUnitExpressionNode != null
                        ? TimeUnit.valueOf(TypeConversionUtil.convertToString(
                                _timeUnitExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false))
                        : TimeUnit.Milliseconds;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _timeUnitAddressing.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        double time = getCurrentTime() * getCurrentTimeUnit().getMultiply();
        
        try {
            Thread.sleep(Math.round(time));
        } catch (InterruptedException ex) {
            log.warn("Delay was aborted by InterruptedException", ex);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new IllegalArgumentException(String.format("index has invalid value: %d", index));
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Delay_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String timeStr;
        String state;

        switch (_addressing) {
            case Direct:
                timeStr = Integer.toString(_time);
                break;

            case Reference:
                timeStr = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                timeStr = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                timeStr = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        switch (_timeUnitAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _timeUnit._text);
                break;

            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _timeUnitReference);
                break;

            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _timeUnitLocalVariable);
                break;

            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _timeUnitFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _timeUnitAddressing state: " + _timeUnitAddressing.name());
        }

        return Bundle.getMessage(locale, "Delay_Long", timeStr, state);
    }

    public int getTime() {
        return _time;
    }

    public void setTime(int time) {
        _time = time;
    }

    public TimeUnit getTimeUnit() {
        return _timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        _timeUnit = timeUnit;
    }

    public void setAddressing(NamedBeanAddressing addressing) throws ParserException {
        _addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setFormula(@Nonnull String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }


    public void setTimeUnitAddressing(NamedBeanAddressing addressing) throws ParserException {
        _timeUnitAddressing = addressing;
        parseTimeUnitFormula();
    }

    public NamedBeanAddressing getTimeUnitAddressing() {
        return _timeUnitAddressing;
    }

    public void setTimeUnitReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _timeUnitReference = reference;
    }

    public String getTimeUnitReference() {
        return _timeUnitReference;
    }

    public void setTimeUnitLocalVariable(@Nonnull String localVariable) {
        _timeUnitLocalVariable = localVariable;
    }

    public String getTimeUnitLocalVariable() {
        return _timeUnitLocalVariable;
    }

    public void setTimeUnitFormula(@Nonnull String formula) throws ParserException {
        _timeUnitFormula = formula;
        parseTimeUnitFormula();
    }

    public String getTimeUnitFormula() {
        return _timeUnitFormula;
    }

    private void parseTimeUnitFormula() throws ParserException {
        if (_timeUnitAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _timeUnitExpressionNode = parser.parseExpression(_timeUnitFormula);
        } else {
            _timeUnitExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum TimeUnit {
        Milliseconds(1, Bundle.getMessage("TimeUnit_Milliseconds")),
        Seconds(1000, Bundle.getMessage("TimeUnit_Seconds")),
        Minutes(60*1000, Bundle.getMessage("TimeUnit_Minutes")),
        Hours(60*60*1000, Bundle.getMessage("TimeUnit_Hours"));

        private final String _text;
        private final long _multiply;

        private TimeUnit(long multiply, String text) {
            this._multiply = multiply;
            this._text = text;
        }

        public long getMultiply() {
            return _multiply;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Delay.class);
    
}
