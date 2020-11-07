package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.*;

/**
 * Evaluates the state of a local variable.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionLocalVariable extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private String _variableName;
    private VariableOperation _variableOperation = VariableOperation.Equal;
    private CompareTo _compareTo = CompareTo.Value;
    private boolean _caseInsensitive = false;
    private String _constantValue = "";
    private NamedBeanHandle<Memory> _memoryHandle;
    private boolean _listenToMemory = true;
//    private boolean _listenToMemory = false;
    
    public ExpressionLocalVariable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setMemory");
        _variableName = variableName;
    }
    
    public String getVariableName() {
        return _variableName;
    }
    
    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.error("memory \"{}\" is not found", memoryName);
        }
    }
    
    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setMemory");
        _memoryHandle = handle;
        if (_memoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public void setMemory(@CheckForNull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            _memoryHandle = null;
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public void removeMemory() {
        assertListenersAreNotRegistered(log, "removeMemory");
        if (_memoryHandle != null) {
            InstanceManager.memoryManagerInstance().removeVetoableChangeListener(this);
            _memoryHandle = null;
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    public void setConstantValue(String constantValue) {
        _constantValue = constantValue;
    }
    
    public String getConstantValue() {
        return _constantValue;
    }
    
    public void setVariableOperation(VariableOperation variableOperation) {
        _variableOperation = variableOperation;
    }
    
    public VariableOperation getVariableOperation() {
        return _variableOperation;
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
/*        
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Memory_MemoryInUseMemoryExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    removeMemory();
                }
            }
        }
*/        
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    private String getString(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }
    
    /**
     * Compare two values using the comparator set using the comparison
     * instructions in {@link #_variableOperation}.
     *
     * <strong>Note:</strong> {@link #_variableOperation} must be one of
     * {@link #ExpressionLocalVariable.MemoryOperation.LESS_THAN},
     * {@link #ExpressionLocalVariable.MemoryOperation.LESS_THAN_OR_EQUAL},
     * {@link #ExpressionLocalVariable.MemoryOperation.EQUAL},
     * {@link #ExpressionLocalVariable.MemoryOperation.GREATER_THAN_OR_EQUAL},
     * or {@link #ExpressionLocalVariable.MemoryOperation.GREATER_THAN}.
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
                switch (_variableOperation) // both are numbers
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
                        throw new IllegalArgumentException("_memoryOperation has unknown value: "+_variableOperation.name());
                }
            } catch (NumberFormatException nfe) {
                return false;   // n1 is a number, n2 is not
            }
        } catch (NumberFormatException nfe) {
            try {
                Integer.parseInt(value2);
                return false;     // n1 is not a number, n2 is
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
        switch (_variableOperation) {
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
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_variableOperation.name());
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
    public boolean evaluate() {
        if (_variableName == null) return false;
        
        String variableValue = getString(
                InstanceManager.getDefault(LogixNG_Manager.class)
                        .getSymbolTable().getValue(_variableName));
        String otherValue = null;
        boolean result;
        
        switch (_compareTo) {
            case Value:
                otherValue = _constantValue;
                break;
            case Memory:
                otherValue = getString(_memoryHandle.getBean().getValue());
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
                result = compare(variableValue, otherValue, _caseInsensitive);
                break;
                
            case IsNull:
                result = variableValue == null;
                break;
            case IsNotNull:
                result = variableValue != null;
                break;
                
            case MatchRegex:
                result = matchRegex(variableValue, otherValue);
                break;
                
            case NotMatchRegex:
                result = !matchRegex(variableValue, otherValue);
                break;
                
            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_variableOperation.name());
        }
        
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing.
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
        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getBean().getDisplayName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        String message;
        String other;
        switch (_compareTo) {
            case Value:
                message = "Variable_Long_CompareConstant";
                other = _constantValue;
                break;
                
            case Memory:
                message = "Variable_Long_CompareMemory";
                other = memoryName;
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
                return Bundle.getMessage(locale, message, _variableName, _variableOperation._text, other);
                
            case IsNull:
                // fall through
            case IsNotNull:
                return Bundle.getMessage(locale, "Variable_Long_CompareNull", _variableName, _variableOperation._text);
                
            case MatchRegex:
                // fall through
            case NotMatchRegex:
                return Bundle.getMessage(locale, "Variable_Long_CompareRegex", _variableName, _variableOperation._text);
                
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
        if (!_listenersAreRegistered && (_variableName != null)) {
            if (_listenToMemory) _memoryHandle.getBean().addPropertyChangeListener("value", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_listenToMemory) _memoryHandle.getBean().addPropertyChangeListener("value", this);
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (getTriggerOnChange()) {
            getConditionalNG().execute();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum VariableOperation {
        LessThan(Bundle.getMessage("VariableOperation_LessThan"), true),
        LessThanOrEqual(Bundle.getMessage("VariableOperation_LessThanOrEqual"), true),
        Equal(Bundle.getMessage("VariableOperation_Equal"), true),
        GreaterThanOrEqual(Bundle.getMessage("VariableOperation_GreaterThanOrEqual"), true),
        GreaterThan(Bundle.getMessage("VariableOperation_GreaterThan"), true),
        NotEqual(Bundle.getMessage("VariableOperation_NotEqual"), true),
        IsNull(Bundle.getMessage("VariableOperation_IsNull"), false),
        IsNotNull(Bundle.getMessage("VariableOperation_IsNotNull"), false),
        MatchRegex(Bundle.getMessage("VariableOperation_MatchRegEx"), true),
        NotMatchRegex(Bundle.getMessage("VariableOperation_NotMatchRegEx"), true);
        
        private final String _text;
        private final boolean _extraValue;
        
        private VariableOperation(String text, boolean extraValue) {
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
        Value,
        Memory;
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLocalVariable.class);
    
}
