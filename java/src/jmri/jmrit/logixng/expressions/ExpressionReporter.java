package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a Reporter.
 *
 * @author Daniel Bergqvist Copyright 2018
 * @author Dave Sand Copyright 2021
 */
public class ExpressionReporter extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<Reporter> _reporterHandle;

    private ReporterValue _reporterValue = ReporterValue.CurrentReport;
    private ReporterOperation _reporterOperation = ReporterOperation.Equal;
    private CompareTo _compareTo = CompareTo.Value;

    private boolean _caseInsensitive = false;
    private String _constantValue = "";
    private NamedBeanHandle<Memory> _memoryHandle;
    private String _localVariable = "";
    private String _regEx = "";
    private boolean _listenToMemory = true;
//    private boolean _listenToMemory = false;

    public ExpressionReporter(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionReporter copy = new ExpressionReporter(sysName, userName);
        copy.setComment(getComment());
        if (_reporterHandle != null) copy.setReporter(_reporterHandle);
        copy.setReporterValue(_reporterValue);
        copy.setReporterOperation(_reporterOperation);
        copy.setCompareTo(_compareTo);
        copy.setCaseInsensitive(_caseInsensitive);
        copy.setConstantValue(_constantValue);
        if (_memoryHandle != null) copy.setMemory(_memoryHandle);
        copy.setListenToMemory(_listenToMemory);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }


    public void setReporter(@Nonnull String reporterName) {
        assertListenersAreNotRegistered(log, "setReporter");
        Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getReporter(reporterName);
        if (reporter != null) {
            setReporter(reporter);
        } else {
            removeReporter();
            log.warn("reporter \"{}\" is not found", reporterName);
        }
    }

    public void setReporter(@Nonnull NamedBeanHandle<Reporter> handle) {
        assertListenersAreNotRegistered(log, "setReporter");
        _reporterHandle = handle;
        InstanceManager.getDefault(ReporterManager.class).addVetoableChangeListener(this);
    }

    public void setReporter(@Nonnull Reporter reporter) {
        assertListenersAreNotRegistered(log, "setReporter");
        setReporter(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(reporter.getDisplayName(), reporter));
    }

    public void removeReporter() {
        assertListenersAreNotRegistered(log, "removeReporter");
        if (_reporterHandle != null) {
            InstanceManager.getDefault(ReporterManager.class).removeVetoableChangeListener(this);
            _reporterHandle = null;
        }
    }

    public NamedBeanHandle<Reporter> getReporter() {
        return _reporterHandle;
    }


    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setMemory");
        _memoryHandle = handle;
        InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
    }

    public void setMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");
        setMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeMemory() {
        assertListenersAreNotRegistered(log, "removeMemory");
        if (_memoryHandle != null) {
            _memoryHandle = null;
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
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


    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }


    public void setReporterValue(ReporterValue reporterValue) {
        _reporterValue = reporterValue;
    }

    public ReporterValue getReporterValue() {
        return _reporterValue;
    }


    public void setReporterOperation(ReporterOperation reporterOperation) {
        _reporterOperation = reporterOperation;
    }

    public ReporterOperation getReporterOperation() {
        return _reporterOperation;
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


    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Reporter) {
                if (evt.getOldValue().equals(getReporter().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Reporter_ReporterInUseVeto", getDisplayName()), e); // NOI18N
                }
            }

            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Reporter_MemoryInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
        }
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
     * instructions in {@link #_reporterOperation}.
     *
     * <strong>Note:</strong> {@link #_reporterOperation} must be one of
     * {@link #ExpressionReporter.ReporterOperation.LESS_THAN},
     * {@link #ExpressionReporter.ReporterOperation.LESS_THAN_OR_EQUAL},
     * {@link #ExpressionReporter.ReporterOperation.EQUAL},
     * {@link #ExpressionReporter.ReporterOperation.GREATER_THAN_OR_EQUAL},
     * or {@link #ExpressionReporter.ReporterOperation.GREATER_THAN}.
     *
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per _reporterOperation; false otherwise
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
                switch (_reporterOperation) // both are numbers
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
                        throw new IllegalArgumentException("_reporterOperation has unknown value: "+_reporterOperation.name());
                }
            } catch (NumberFormatException nfe) {
                return _reporterOperation == ReporterOperation.NotEqual;   // n1 is a number, n2 is not
            }
        } catch (NumberFormatException nfe) {
            try {
                Integer.parseInt(value2);
                return _reporterOperation == ReporterOperation.NotEqual;     // n1 is not a number, n2 is
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
        switch (_reporterOperation) {
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
                throw new IllegalArgumentException("_reporterOperation has unknown value: "+_reporterOperation.name());
        }
        return false;
    }

    private boolean matchRegex(String reporterValue, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(reporterValue);
        return m.matches();
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        if (_reporterHandle == null) return false;

        Object obj;
        switch (_reporterValue) {
            case CurrentReport:
                obj = _reporterHandle.getBean().getCurrentReport();
                break;

            case LastReport:
                obj = _reporterHandle.getBean().getLastReport();
                break;

            case State:
                obj = _reporterHandle.getBean().getState();
                break;

            default:
                obj = null;
        }
        String reporterValue = getString(obj);
        String otherValue = null;
        boolean result;

        switch (_compareTo) {
            case Value:
                otherValue = _constantValue;
                break;
            case Memory:
                otherValue = getString(_memoryHandle.getBean().getValue());
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

        switch (_reporterOperation) {
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
                result = compare(reporterValue, otherValue, _caseInsensitive);
                break;

            case IsNull:
                result = reporterValue == null;
                break;

            case IsNotNull:
                result = reporterValue != null;
                break;

            case MatchRegex:
                result = matchRegex(reporterValue, _regEx);
                break;

            case NotMatchRegex:
                result = !matchRegex(reporterValue, _regEx);
                break;

            default:
                throw new IllegalArgumentException("_reporterOperation has unknown value: "+_reporterOperation.name());
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
        return Bundle.getMessage(locale, "Reporter_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String reporterName;
        if (_reporterHandle != null) {
            reporterName = _reporterHandle.getName();
        } else {
            reporterName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        String message;
        String other;
        switch (_compareTo) {
            case Value:
                message = "Reporter_Long_CompareConstant";
                other = _constantValue;
                break;

            case Memory:
                message = "Reporter_Long_CompareMemory";
                other = memoryName;
                break;

            case LocalVariable:
                message = "Reporter_Long_CompareLocalVariable";
                other = _localVariable;
                break;

            case RegEx:
                message = "Reporter_Long_CompareRegEx";
                other = _regEx;
                break;

            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }

        switch (_reporterOperation) {
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
                return Bundle.getMessage(locale, message, reporterName, _reporterValue._text, _reporterOperation._text, other);

            case IsNull:
                // fall through
            case IsNotNull:
                return Bundle.getMessage(locale, "Reporter_Long_CompareNull", reporterName, _reporterValue._text, _reporterOperation._text);

            case MatchRegex:
                // fall through
            case NotMatchRegex:
                return Bundle.getMessage(locale, "Reporter_Long_CompareRegEx", reporterName, _reporterValue._text, _reporterOperation._text, other);

            default:
                throw new IllegalArgumentException("_reporterOperation has unknown value: "+_reporterOperation.name());
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
        if (!_listenersAreRegistered && (_reporterHandle != null)) {
            _reporterHandle.getBean().addPropertyChangeListener("value", this);
            if (_listenToMemory && (_memoryHandle != null)) {
                _memoryHandle.getBean().addPropertyChangeListener("value", this);
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _reporterHandle.getBean().removePropertyChangeListener("value", this);
            if (_listenToMemory && (_memoryHandle != null)) {
                _memoryHandle.getBean().removePropertyChangeListener("value", this);
            }
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

    public enum ReporterValue {
        CurrentReport(Bundle.getMessage("Reporter_Value_CurrentReport")),
        LastReport(Bundle.getMessage("Reporter_Value_LastReport")),
        State(Bundle.getMessage("Reporter_Value_State"));

        private final String _text;

        private ReporterValue(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }
    }


    public enum ReporterOperation {
        LessThan(Bundle.getMessage("ReporterOperation_LessThan"), true),
        LessThanOrEqual(Bundle.getMessage("ReporterOperation_LessThanOrEqual"), true),
        Equal(Bundle.getMessage("ReporterOperation_Equal"), true),
        GreaterThanOrEqual(Bundle.getMessage("ReporterOperation_GreaterThanOrEqual"), true),
        GreaterThan(Bundle.getMessage("ReporterOperation_GreaterThan"), true),
        NotEqual(Bundle.getMessage("ReporterOperation_NotEqual"), true),
        IsNull(Bundle.getMessage("ReporterOperation_IsNull"), false),
        IsNotNull(Bundle.getMessage("ReporterOperation_IsNotNull"), false),
        MatchRegex(Bundle.getMessage("ReporterOperation_MatchRegEx"), true),
        NotMatchRegex(Bundle.getMessage("ReporterOperation_NotMatchRegEx"), true);

        private final String _text;
        private final boolean _extraValue;

        private ReporterOperation(String text, boolean extraValue) {
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
        Value(Bundle.getMessage("Reporter_CompareTo_Value")),
        Memory(Bundle.getMessage("Reporter_CompareTo_Memory")),
        LocalVariable(Bundle.getMessage("Reporter_CompareTo_LocalVariable")),
        RegEx(Bundle.getMessage("Reporter_CompareTo_RegularExpression"));

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
        log.debug("getUsageReport :: ExpressionReporter: bean = {}, report = {}", cdl, report);
        if (getReporter() != null && bean.equals(getReporter().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
        if (getMemory() != null && bean.equals(getMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionReporter.class);

}
