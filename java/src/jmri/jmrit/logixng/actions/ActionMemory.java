package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.util.ThreadingUtil;

/**
 * This action sets the value of a memory.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionMemory extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<Memory> _memoryHandle;
    private NamedBeanHandle<Memory> _otherMemoryHandle;
    private MemoryOperation _memoryOperation = MemoryOperation.SetToString;
    private String _constantValue = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private boolean _listenToMemory = true;
//    private boolean _listenToMemory = false;
    
    public ActionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionMemory copy = new ActionMemory(sysName, userName);
        copy.setComment(getComment());
        if (_memoryHandle != null) copy.setMemory(_memoryHandle);
        if (_otherMemoryHandle != null) copy.setOtherMemory(_otherMemoryHandle);
        copy.setMemoryOperation(_memoryOperation);
        copy.setConstantValue(_constantValue);
        copy.setLocalVariable(_localVariable);
        copy.setFormula(_formula);
        copy.setListenToMemory(_listenToMemory);
        return manager.registerAction(copy);
    }
    
    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
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
        addRemoveVetoListener();
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
            addRemoveVetoListener();
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    public void setOtherMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setOtherMemory(memory);
        } else {
            removeOtherMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }
    
    public void setOtherMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        _otherMemoryHandle = handle;
        addRemoveVetoListener();
    }
    
    public void setOtherMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        setOtherMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }
    
    public void removeOtherMemory() {
        assertListenersAreNotRegistered(log, "removeOtherMemory");
        if (_otherMemoryHandle != null) {
            _otherMemoryHandle = null;
            addRemoveVetoListener();
        }
    }
    
    public NamedBeanHandle<Memory> getOtherMemory() {
        return _otherMemoryHandle;
    }
    
    public void setLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setOtherLocalVariable");
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
    
    public void setFormula(String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }
    
    public String getFormula() {
        return _formula;
    }
    
    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }
    
    public boolean getListenToMemory() {
        return _listenToMemory;
    }
    
    public void setMemoryOperation(MemoryOperation state) throws ParserException {
        _memoryOperation = state;
        parseFormula();
    }
    
    public MemoryOperation getMemoryOperation() {
        return _memoryOperation;
    }
    
    private void parseFormula() throws ParserException {
        if (_memoryOperation == MemoryOperation.CalculateFormula) {
            Map<String, Variable> variables = new HashMap<>();
/*            
            SymbolTable symbolTable =
                    InstanceManager.getDefault(LogixNG_Manager.class)
                            .getSymbolTable();
            
            if (symbolTable == null && 1==1) return;    // Why does this happens?
//            if (symbolTable == null && 1==1) return;    // Nothing we can do if we don't have a symbol table
            if (symbolTable == null) throw new RuntimeException("Daniel AA");
            if (symbolTable.getSymbols() == null) throw new RuntimeException("Daniel BB");
            if (symbolTable.getSymbols().values() == null) throw new RuntimeException("Daniel BB");
            
            for (SymbolTable.Symbol symbol : symbolTable.getSymbols().values()) {
                variables.put(symbol.getName(),
                        new LocalVariableExpressionVariable(symbol.getName()));
            }
*/            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }
    
    private void addRemoveVetoListener() {
        if ((_memoryHandle != null) || (_otherMemoryHandle != null)) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                boolean doVeto = false;
                if ((_memoryHandle != null) && evt.getOldValue().equals(_memoryHandle.getBean())) doVeto = true;
                if ((_otherMemoryHandle != null) && evt.getOldValue().equals(_otherMemoryHandle.getBean())) doVeto = true;
                if (doVeto) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionMemory_MemoryInUseMemoryExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if ((_memoryHandle != null) && evt.getOldValue().equals(_memoryHandle.getBean())) {
                    removeMemory();
                }
                if ((_otherMemoryHandle != null) && evt.getOldValue().equals(_otherMemoryHandle.getBean())) {
                    removeOtherMemory();
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
    public void execute() throws JmriException {
        if (_memoryHandle == null) return;
        
        final Memory memory = _memoryHandle.getBean();
        
        AtomicReference<JmriException> ref = new AtomicReference<>();
        
        ThreadingUtil.runOnLayout(() -> {
            
            switch (_memoryOperation) {
                case SetToNull:
                    memory.setValue(null);
                    break;
                    
                case SetToString:
                    memory.setValue(_constantValue);
                    break;
                    
                case CopyVariableToMemory:
                    Object variableValue = getConditionalNG()
                                    .getSymbolTable().getValue(_localVariable);
                    memory.setValue(variableValue);
                    break;
                    
                case CopyMemoryToMemory:
                    if (_otherMemoryHandle != null) {
                        memory.setValue(_otherMemoryHandle.getBean().getValue());
                    } else {
                        log.warn("setMemory should copy memory to memory but other memory is null");
                    }
                    break;
                    
                case CalculateFormula:
                    if (_formula.isEmpty()) {
                        memory.setValue(null);
                    } else {
                        try {
                            if (_expressionNode == null) {
                                return;
                            }
                            memory.setValue(_expressionNode.calculate(
                                    getConditionalNG().getSymbolTable()));
                        } catch (JmriException e) {
                            ref.set(e);
                        }
                    }
                    break;
                    
                default:
                    throw new IllegalArgumentException("_memoryOperation has invalid value: {}" + _memoryOperation.name());
            }
        });
        
        if (ref.get() != null) throw ref.get();
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
        return Bundle.getMessage(locale, "ActionMemory_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getBean().getDisplayName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        String copyToMemoryName;
        if (_otherMemoryHandle != null) {
            copyToMemoryName = _otherMemoryHandle.getBean().getDisplayName();
        } else {
            copyToMemoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        switch (_memoryOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionMemory_Long_Null", memoryName);
            case SetToString:
                return Bundle.getMessage(locale, "ActionMemory_Long_Value", memoryName, _constantValue);
            case CopyVariableToMemory:
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyVariableToMemory", memoryName, _localVariable);
            case CopyMemoryToMemory:
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyMemoryToMemory", memoryName, copyToMemoryName);
            case CalculateFormula:
                return Bundle.getMessage(locale, "ActionMemory_Long_Formula", memoryName, _formula);
            default:
                throw new IllegalArgumentException("_memoryOperation has invalid value: " + _memoryOperation.name());
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
        if (!_listenersAreRegistered && (_otherMemoryHandle != null)) {
            if (_listenToMemory) {
                _otherMemoryHandle.getBean().addPropertyChangeListener("value", this);
            }
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_listenToMemory && (_otherMemoryHandle != null)) {
                _otherMemoryHandle.getBean().addPropertyChangeListener("value", this);
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
    
    
    public enum MemoryOperation {
        SetToNull(Bundle.getMessage("ActionMemory_MemoryOperation_SetToNull")),
        SetToString(Bundle.getMessage("ActionMemory_MemoryOperation_SetToString")),
        CopyVariableToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyVariableToMemory")),
        CopyMemoryToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyMemoryToMemory")),
        CalculateFormula(Bundle.getMessage("ActionMemory_MemoryOperation_CalculateFormula"));
        
        private final String _text;
        
        private MemoryOperation(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemory.class);
    
}
