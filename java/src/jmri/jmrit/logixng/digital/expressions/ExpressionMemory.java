package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates the state of a Memory.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionMemory extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private ExpressionMemory _template;
    private NamedBeanHandle<Memory> _memoryHandle;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.IS;
    private MemoryOperation _memoryOperation = MemoryOperation.EQUAL;
    private boolean _listenersAreRegistered = false;

    public ExpressionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    private ExpressionMemory(ExpressionMemory template) {
        super(InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
        _template = template;
        if (_template == null) throw new NullPointerException();    // Temporary solution to make variable used.
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new ExpressionMemory(this);
    }
    
    public void setMemory(String memoryName) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
        } else {
            log.error("memory {} is not found", memoryName);
            _memoryHandle = null;
        }
    }
    
    public void setMemory(NamedBeanHandle<Memory> handle) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        _memoryHandle = handle;
    }
    
    public void setMemory(@CheckForNull Memory memory) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setMemory must not be called when listeners are registered");
            log.error("setMemory must not be called when listeners are registered", e);
            throw e;
        }
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
        } else {
            _memoryHandle = null;
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setMemoryState(MemoryOperation state) {
        _memoryOperation = state;
    }
    
    public MemoryOperation getMemoryState() {
        return _memoryOperation;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
                }
            }
        }
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
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        // Fix this later
        return false;
/*        
        MemoryOperation currentMemoryState = MemoryOperation.get(_memoryHandle.getBean().getCommandedState());
        if (_is_IsNot == Is_IsNot_Enum.IS) {
            return currentMemoryState == _memoryOperation;
        } else {
            return currentMemoryState != _memoryOperation;
        }
*/        
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
        return Bundle.getMessage(locale, "Memory_Long", memoryName, _is_IsNot.toString(), _memoryOperation._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_memoryHandle != null)) {
            _memoryHandle.getBean().addPropertyChangeListener("KnownState", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _memoryHandle.getBean().removePropertyChangeListener("KnownState", this);
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
        LESS_THAN(Bundle.getMessage("MemoryOperation_LessThan"), true),
        LESS_THAN_OR_EQUAL(Bundle.getMessage("MemoryOperation_LessThanOrEqual"), true),
        EQUAL(Bundle.getMessage("MemoryOperation_Equal"), true),
        GREATER_THAN_OR_EQUAL(Bundle.getMessage("MemoryOperation_GreaterThanOrEqual"), true),
        GREATER_THAN(Bundle.getMessage("MemoryOperation_GreaterThan"), true),
        NOT_EQUAL(Bundle.getMessage("MemoryOperation_NotEqual"), true),
        IS_NULL(Bundle.getMessage("MemoryOperation_IsNull"), false),
        IS_NOT_NULL(Bundle.getMessage("MemoryOperation_IsNotNull"), false),
        MATCH_REGEX(Bundle.getMessage("MemoryOperation_MatchRegEx"), false),
        NOT_MATCH_REGEX(Bundle.getMessage("MemoryOperation_NotMatchRegEx"), false);
        
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
    
    
    public enum Comparision {
        CASE_SENSITIVE_VALUE(Bundle.getMessage("MemoryOperation_LessThan")),
        CASE_INSENSITIVE_VALUE(Bundle.getMessage("MemoryOperation_LessThan")),
        NUMERIC_VALUE(Bundle.getMessage("MemoryOperation_LessThan")),
        CASE_SENSITIVE_MEMORY(Bundle.getMessage("MemoryOperation_LessThan")),
        CASE_INSENSITIVE_MEMORY(Bundle.getMessage("MemoryOperation_LessThan")),
        NUMERIC_MEMORY(Bundle.getMessage("MemoryOperation_LessThan"));
        
        private final String _text;
        
        private Comparision(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    // Should the memory be used as is, or should it be converted using toString()
    // or toReportString() methods? The toReportString() will return null if the
    // memory does not contains a report.
    public enum TypeOfValue {
        AS_IS(Bundle.getMessage("TypeOfValue_AsIs")),
        TO_STRING(Bundle.getMessage("TypeOfValue_ToString")),
        TO_REPORT(Bundle.getMessage("TypeOfValue_ToReport"));
        
        private final String _text;
        
        private TypeOfValue(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionMemory.class);
    
}
