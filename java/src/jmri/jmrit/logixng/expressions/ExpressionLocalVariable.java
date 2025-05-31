package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.util.CompareUtil;
import jmri.util.CompareUtil.CompareType;
import jmri.util.CompareUtil.CompareOperation;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a local variable.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionLocalVariable extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private String _localVariable;
    private VariableOperation _variableOperation = VariableOperation.Equal;
    private CompareType _compareType = CompareType.NumberOrString;
    private CompareTo _compareTo = CompareTo.Value;
    private boolean _caseInsensitive = false;
    private String _constantValue = "";
    private final LogixNG_SelectNamedBean<Memory> _selectMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);
    private String _otherLocalVariable = "";
    private String _regEx = "";
    private boolean _listenToMemory = true;

    private final LogixNG_SelectTable _selectTable =
            new LogixNG_SelectTable(this, () -> {return _compareTo == CompareTo.Table;});


    public ExpressionLocalVariable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionLocalVariable copy = new ExpressionLocalVariable(sysName, userName);
        copy.setComment(getComment());
        copy.setLocalVariable(_localVariable);
        copy.setVariableOperation(_variableOperation);
        copy.setCompareType(_compareType);
        copy.setCompareTo(_compareTo);
        copy.setCaseInsensitive(_caseInsensitive);
        copy.setConstantValue(_constantValue);
        _selectMemoryNamedBean.copy(copy._selectMemoryNamedBean);
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setRegEx(_regEx);
        _selectTable.copy(copy._selectTable);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<Memory> getSelectMemoryNamedBean() {
        return _selectMemoryNamedBean;
    }

    public void setLocalVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setLocalVariable");
        _localVariable = variableName;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setOtherLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setOtherLocalVariable");
        _otherLocalVariable = localVariable;
    }

    public String getOtherLocalVariable() {
        return _otherLocalVariable;
    }

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

    public void setConstantValue(String constantValue) {
        _constantValue = constantValue;
    }

    public String getConstantValue() {
        return _constantValue;
    }

    public void setRegEx(String regEx) {
        _regEx = regEx;
    }

    public String getRegEx() {
        return _regEx;
    }

    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }

    public void setVariableOperation(VariableOperation variableOperation) {
        _variableOperation = variableOperation;
    }

    public VariableOperation getVariableOperation() {
        return _variableOperation;
    }

    public void setCompareType(CompareType compareType) {
        _compareType = compareType;
    }

    public CompareType getCompareType() {
        return _compareType;
    }

    public void setCompareTo(CompareTo compareTo) {
        _compareTo = compareTo;
    }

    public CompareTo getCompareTo() {
        return _compareTo;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        _caseInsensitive = caseInsensitive;
    }

    public boolean getCaseInsensitive() {
        return _caseInsensitive;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    private String getString(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    private boolean matchRegex(String memoryValue, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(memoryValue);
        return m.matches();
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        if (_localVariable == null) return false;

        String variableValue = getString(getConditionalNG()
                        .getSymbolTable().getValue(_localVariable));
        String otherValue = null;
        boolean result;

        switch (_compareTo) {
            case Value:
                otherValue = _constantValue;
                break;
            case Memory:
                Memory memory = _selectMemoryNamedBean.evaluateNamedBean(getConditionalNG());
                otherValue = getString(memory.getValue());
                break;
            case Table:
                otherValue = getString(_selectTable.evaluateTableData(getConditionalNG()));
                break;
            case LocalVariable:
                otherValue = TypeConversionUtil.convertToString(getConditionalNG().getSymbolTable().getValue(_otherLocalVariable), false);
                break;
            case RegEx:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }

        switch (_variableOperation) {
            case LessThan:
                // fall through
            case LessThanOrEqual:
                // fall through
            case Equal:
                // fall through
            case NotEqual:
                // fall through
            case GreaterThanOrEqual:
                // fall through
            case GreaterThan:
                result = CompareUtil.compare(_compareType, _variableOperation._oper, variableValue, otherValue, _caseInsensitive);
                break;

            case IsNull:
                result = variableValue == null;
                break;
            case IsNotNull:
                result = variableValue != null;
                break;

            case MatchRegex:
                result = matchRegex(variableValue, _regEx);
                break;

            case NotMatchRegex:
                result = !matchRegex(variableValue, _regEx);
                break;

            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_variableOperation.name());
        }

        return result;
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "LocalVariable_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String variableName;
        if ((_localVariable == null) || _localVariable.isEmpty()) {
            variableName = Bundle.getMessage(locale, "BeanNotSelected");
        } else {
            variableName = _localVariable;
        }

        String memoryName = _selectMemoryNamedBean.getDescription(locale);

        String message;
        String other1;
        String other2 = null;
        String other3 = null;

        switch (_compareTo) {
            case Value:
                message = "LocalVariable_Long_CompareConstant";
                other1 = _constantValue;
                break;

            case Memory:
                message = "LocalVariable_Long_CompareMemory";
                other1 = memoryName;
                break;

            case LocalVariable:
                message = "LocalVariable_Long_CompareLocalVariable";
                other1 = _otherLocalVariable;
                break;

            case Table:
                message = "LocalVariable_Long_CompareTable";
                other1 = _selectTable.getTableNameDescription(locale);
                other2 = _selectTable.getTableRowDescription(locale);
                other3 = _selectTable.getTableColumnDescription(locale);
                break;

            case RegEx:
                message = "LocalVariable_Long_CompareRegEx";
                other1 = _regEx;
                break;

            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }

        switch (_variableOperation) {
            case LessThan:
                // fall through
            case LessThanOrEqual:
                // fall through
            case Equal:
                // fall through
            case NotEqual:
                // fall through
            case GreaterThanOrEqual:
                // fall through
            case GreaterThan:
                return Bundle.getMessage(locale, message, variableName, _variableOperation._text, other1, other2, other3);

            case IsNull:
                // fall through
            case IsNotNull:
                return Bundle.getMessage(locale, "LocalVariable_Long_CompareNull", variableName, _variableOperation._text);

            case MatchRegex:
                // fall through
            case NotMatchRegex:
                return Bundle.getMessage(locale, "LocalVariable_Long_CompareRegEx", variableName, _variableOperation._text, other1);

            default:
                throw new IllegalArgumentException("_variableOperation has unknown value: "+_variableOperation.name());
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
        if (!_listenersAreRegistered && _listenToMemory) {
            _selectMemoryNamedBean.addPropertyChangeListener("value", this);
            _selectMemoryNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered && _listenToMemory) {
            _selectMemoryNamedBean.removePropertyChangeListener("value", this);
            _selectMemoryNamedBean.unregisterListeners();
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public enum VariableOperation {
        LessThan(CompareOperation.LessThan, null, true),
        LessThanOrEqual(CompareOperation.LessThanOrEqual, null, true),
        Equal(CompareOperation.Equal, null, true),
        GreaterThanOrEqual(CompareOperation.GreaterThanOrEqual, null, true),
        GreaterThan(CompareOperation.GreaterThan, null, true),
        NotEqual(CompareOperation.NotEqual, null, true),
        IsNull(null, Bundle.getMessage("LocalVariableOperation_IsNull"), false),
        IsNotNull(null, Bundle.getMessage("LocalVariableOperation_IsNotNull"), false),
        MatchRegex(null, Bundle.getMessage("LocalVariableOperation_MatchRegEx"), true),
        NotMatchRegex(null, Bundle.getMessage("LocalVariableOperation_NotMatchRegEx"), true);

        private final CompareOperation _oper;
        private final String _text;
        private final boolean _extraValue;

        private VariableOperation(CompareOperation oper, String text, boolean extraValue) {
            this._oper = oper;
            this._text = oper != null ? oper.toString() : text;
            this._extraValue = extraValue;
        }

        @Override
        public String toString() {
            return _text;
        }

        public boolean hasExtraValue() {
            return _extraValue;
        }

    }


    public enum CompareTo {
        Value(Bundle.getMessage("LocalVariable_CompareTo_Value")),
        Memory(Bundle.getMessage("LocalVariable_CompareTo_Memory")),
        LocalVariable(Bundle.getMessage("LocalVariable_CompareTo_LocalVariable")),
        Table(Bundle.getMessage("LocalVariable_CompareTo_Table")),
        RegEx(Bundle.getMessage("LocalVariable_CompareTo_RegularExpression"));

        private final String _text;

        private CompareTo(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ExpressionLocalVariable: bean = {}, report = {}", cdl, report);
        _selectMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLocalVariable.class);

}
