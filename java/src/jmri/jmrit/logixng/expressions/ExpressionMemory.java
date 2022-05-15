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
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a Memory.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionMemory extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Memory> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    private final LogixNG_SelectNamedBean<Memory> _selectOtherMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    private MemoryOperation _memoryOperation = MemoryOperation.Equal;
    private CompareTo _compareTo = CompareTo.Value;
    private boolean _caseInsensitive = false;
    private String _constantValue = "";

    private String _localVariable = "";
    private String _regEx = "";
    private boolean _listenToOtherMemory = true;

    private final LogixNG_SelectTable _selectTable =
            new LogixNG_SelectTable(this, () -> {return _compareTo == CompareTo.Table;});


    public ExpressionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionMemory copy = new ExpressionMemory(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectOtherMemoryNamedBean.copy(copy._selectOtherMemoryNamedBean);
        _selectTable.copy(copy._selectTable);
        copy.setMemoryOperation(_memoryOperation);
        copy.setCompareTo(_compareTo);
        copy.setCaseInsensitive(_caseInsensitive);
        copy.setConstantValue(_constantValue);
        copy.setLocalVariable(_localVariable);
        copy.setRegEx(_regEx);
        copy.setListenToOtherMemory(_listenToOtherMemory);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<Memory> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<Memory> getSelectOtherMemoryNamedBean() {
        return _selectOtherMemoryNamedBean;
    }

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setLocalVariable");
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
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

    public void setListenToOtherMemory(boolean listenToOtherMemory) {
        this._listenToOtherMemory = listenToOtherMemory;
    }

    public boolean getListenToOtherMemory() {
        return _listenToOtherMemory;
    }

    public void setMemoryOperation(MemoryOperation memoryOperation) {
        _memoryOperation = memoryOperation;
    }

    public MemoryOperation getMemoryOperation() {
        return _memoryOperation;
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
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getString(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    /**
     * Compare two values using the comparator set using the comparison
     * instructions in {@link #_memoryOperation}.
     *
     * <strong>Note:</strong> {@link #_memoryOperation} must be one of
     * {@link #ExpressionMemory.MemoryOperation.LESS_THAN},
     * {@link #ExpressionMemory.MemoryOperation.LESS_THAN_OR_EQUAL},
     * {@link #ExpressionMemory.MemoryOperation.EQUAL},
     * {@link #ExpressionMemory.MemoryOperation.GREATER_THAN_OR_EQUAL},
     * or {@link #ExpressionMemory.MemoryOperation.GREATER_THAN}.
     *
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per _memoryOperation; false otherwise
     */
    private boolean compare(String value1, String value2, boolean caseInsensitive) {
        if (value1 == null) {
            return value2 == null;
        } else {
            if (value2 == null) {
                return false;
            }
            value1 = value1.trim();
            value2 = value2.trim();
        }
        try {
            int n1 = Integer.parseInt(value1);
            try {
                int n2 = Integer.parseInt(value2);
                log.debug("Compare numbers: n1= {} to n2= {}", n1, n2);
                switch (_memoryOperation) // both are numbers
                {
                    case LessThan:
                        return (n1 < n2);
                    case LessThanOrEqual:
                        return (n1 <= n2);
                    case Equal:
                        return (n1 == n2);
                    case NotEqual:
                        return (n1 != n2);
                    case GreaterThanOrEqual:
                        return (n1 >= n2);
                    case GreaterThan:
                        return (n1 > n2);
                    default:
                        throw new IllegalArgumentException("_memoryOperation has unknown value: "+_memoryOperation.name());
                }
            } catch (NumberFormatException nfe) {
                return _memoryOperation == MemoryOperation.NotEqual;   // n1 is a number, n2 is not
            }
        } catch (NumberFormatException nfe) {
            try {
                Integer.parseInt(value2);
                return _memoryOperation == MemoryOperation.NotEqual;     // n1 is not a number, n2 is
            } catch (NumberFormatException ex) { // OK neither a number
            }
        }
        log.debug("Compare Strings: value1= {} to value2= {}", value1, value2);
        int compare;
        if (caseInsensitive) {
            compare = value1.compareToIgnoreCase(value2);
        } else {
            compare = value1.compareTo(value2);
        }
        switch (_memoryOperation) {
            case LessThan:
                if (compare < 0) {
                    return true;
                }
                break;
            case LessThanOrEqual:
                if (compare <= 0) {
                    return true;
                }
                break;
            case Equal:
                if (compare == 0) {
                    return true;
                }
                break;
            case NotEqual:
                if (compare != 0) {
                    return true;
                }
                break;
            case GreaterThanOrEqual:
                if (compare >= 0) {
                    return true;
                }
                break;
            case GreaterThan:
                if (compare > 0) {
                    return true;
                }
                break;
            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_memoryOperation.name());
        }
        return false;
    }

    private boolean matchRegex(String memoryValue, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(memoryValue);
        return m.matches();
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        Memory memory = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (memory == null) return false;

        // ConditionalVariable, line 661:  boolean compare(String value1, String value2, boolean caseInsensitive) {
        String memoryValue = getString(memory.getValue());
        String otherValue = null;
        boolean result;

        switch (_compareTo) {
            case Value:
                otherValue = _constantValue;
                break;
            case Memory:
                Memory otherMemory = _selectOtherMemoryNamedBean.evaluateNamedBean(getConditionalNG());
                otherValue = getString(otherMemory.getValue());
                break;
            case Table:
                otherValue = getString(_selectTable.evaluateTableData(getConditionalNG()));
                break;
            case LocalVariable:
                otherValue = TypeConversionUtil.convertToString(getConditionalNG().getSymbolTable().getValue(_localVariable), false);
                break;
            case RegEx:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }

        switch (_memoryOperation) {
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
                result = compare(memoryValue, otherValue, _caseInsensitive);
                break;

            case IsNull:
                result = memoryValue == null;
                break;
            case IsNotNull:
                result = memoryValue != null;
                break;

            case MatchRegex:
                result = matchRegex(memoryValue, _regEx);
                break;

            case NotMatchRegex:
                result = !matchRegex(memoryValue, _regEx);
                break;

            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_memoryOperation.name());
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
        return Bundle.getMessage(locale, "Memory_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String memoryName = _selectNamedBean.getDescription(locale);

        String otherMemoryName = _selectOtherMemoryNamedBean.getDescription(locale);

        String message;
        String other1;
        String other2 = null;
        String other3 = null;

        switch (_compareTo) {
            case Value:
                message = "Memory_Long_CompareConstant";
                other1 = _constantValue;
                break;

            case Memory:
                message = "Memory_Long_CompareMemory";
                other1 = otherMemoryName;
                break;

            case Table:
                message = "Memory_Long_CompareTable";
                other1 = _selectTable.getTableNameDescription(locale);
                other2 = _selectTable.getTableRowDescription(locale);
                other3 = _selectTable.getTableColumnDescription(locale);
                break;

            case LocalVariable:
                message = "Memory_Long_CompareLocalVariable";
                other1 = _localVariable;
                break;

            case RegEx:
                message = "Memory_Long_CompareRegEx";
                other1 = _regEx;
                break;

            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }

        switch (_memoryOperation) {
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
                return Bundle.getMessage(locale, message, memoryName, _memoryOperation._text, other1, other2, other3);

            case IsNull:
                // fall through
            case IsNotNull:
                return Bundle.getMessage(locale, "Memory_Long_CompareNull", memoryName, _memoryOperation._text);

            case MatchRegex:
                // fall through
            case NotMatchRegex:
                return Bundle.getMessage(locale, "Memory_Long_CompareRegEx", memoryName, _memoryOperation._text, other1);

            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_memoryOperation.name());
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
        if (!_listenersAreRegistered) {
            _selectNamedBean.addPropertyChangeListener("value", this);
            if (_listenToOtherMemory) {
                _selectOtherMemoryNamedBean.addPropertyChangeListener("value", this);
            }
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener("value", this);
            if (_listenToOtherMemory) {
                _selectOtherMemoryNamedBean.removePropertyChangeListener("value", this);
            }
            _selectNamedBean.unregisterListeners();
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



    public enum MemoryOperation {
        LessThan(Bundle.getMessage("MemoryOperation_LessThan"), true),
        LessThanOrEqual(Bundle.getMessage("MemoryOperation_LessThanOrEqual"), true),
        Equal(Bundle.getMessage("MemoryOperation_Equal"), true),
        GreaterThanOrEqual(Bundle.getMessage("MemoryOperation_GreaterThanOrEqual"), true),
        GreaterThan(Bundle.getMessage("MemoryOperation_GreaterThan"), true),
        NotEqual(Bundle.getMessage("MemoryOperation_NotEqual"), true),
        IsNull(Bundle.getMessage("MemoryOperation_IsNull"), false),
        IsNotNull(Bundle.getMessage("MemoryOperation_IsNotNull"), false),
        MatchRegex(Bundle.getMessage("MemoryOperation_MatchRegEx"), true),
        NotMatchRegex(Bundle.getMessage("MemoryOperation_NotMatchRegEx"), true);

        private final String _text;
        private final boolean _extraValue;

        private MemoryOperation(String text, boolean extraValue) {
            this._text = text;
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
        Value(Bundle.getMessage("Memory_CompareTo_Value")),
        Memory(Bundle.getMessage("Memory_CompareTo_Memory")),
        LocalVariable(Bundle.getMessage("Memory_CompareTo_LocalVariable")),
        Table(Bundle.getMessage("Memory_CompareTo_Table")),
        RegEx(Bundle.getMessage("Memory_CompareTo_RegularExpression"));

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
        log.debug("getUsageReport :: ExpressionMemory: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
        _selectOtherMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionMemory.class);

}
