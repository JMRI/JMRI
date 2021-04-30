package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.Logix;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action enables/disables a Logix.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class EnableLogix extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Logix> _logixHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private Operation _operationDirect = Operation.Disable;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;
    
    public EnableLogix(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        EnableLogix copy = new EnableLogix(sysName, userName);
        copy.setComment(getComment());
        if (_logixHandle != null) copy.setLogix(_logixHandle);
        copy.setOperationDirect(_operationDirect);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationFormula(_operationFormula);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationReference(_operationReference);
        return manager.registerAction(copy);
    }
    
    public void setLogix(@Nonnull String logixName) {
        assertListenersAreNotRegistered(log, "setLogix");
        Logix logix = InstanceManager.getDefault(LogixManager.class).getLogix(logixName);
        if (logix != null) {
            EnableLogix.this.setLogix(logix);
        } else {
            removeLogix();
            log.error("logix \"{}\" is not found", logixName);
        }
    }
    
    public void setLogix(@Nonnull NamedBeanHandle<Logix> handle) {
        assertListenersAreNotRegistered(log, "setLogix");
        _logixHandle = handle;
        InstanceManager.getDefault(LogixManager.class).addVetoableChangeListener(this);
    }
    
    public void setLogix(@Nonnull Logix logix) {
        assertListenersAreNotRegistered(log, "setLogix");
        EnableLogix.this.setLogix(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(logix.getDisplayName(), logix));
    }
    
    public void removeLogix() {
        assertListenersAreNotRegistered(log, "removeLogix");
        if (_logixHandle != null) {
            InstanceManager.getDefault(LogixManager.class).removeVetoableChangeListener(this);
            _logixHandle = null;
        }
    }
    
    public NamedBeanHandle<Logix> getLogix() {
        return _logixHandle;
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
    
    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseLockFormula();
    }
    
    public NamedBeanAddressing getOperationAddressing() {
        return _operationAddressing;
    }
    
    public void setOperationDirect(Operation state) {
        _operationDirect = state;
    }
    
    public Operation getOperationDirect() {
        return _operationDirect;
    }
    
    public void setOperationReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _operationReference = reference;
    }
    
    public String getOperationReference() {
        return _operationReference;
    }
    
    public void setOperationLocalVariable(@Nonnull String localVariable) {
        _operationLocalVariable = localVariable;
    }
    
    public String getOperationLocalVariable() {
        return _operationLocalVariable;
    }
    
    public void setOperationFormula(@Nonnull String formula) throws ParserException {
        _operationFormula = formula;
        parseLockFormula();
    }
    
    public String getLockFormula() {
        return _operationFormula;
    }
    
    private void parseLockFormula() throws ParserException {
        if (_operationAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();
            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _operationExpressionNode = parser.parseExpression(_operationFormula);
        } else {
            _operationExpressionNode = null;
        }
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Logix) {
                if (evt.getOldValue().equals(getLogix().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("EnableLogix_LogixInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Logix) {
                if (evt.getOldValue().equals(getLogix().getBean())) {
                    removeLogix();
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
    
    private String getNewLock() throws JmriException {
        
        switch (_operationAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _operationReference);
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_operationLocalVariable), false);
                
            case Formula:
                return _operationExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _operationExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _operationAddressing.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Logix logix;
        
//        System.out.format("ActionEnableLogix.execute: %s%n", getLongDescription());
        
        switch (_addressing) {
            case Direct:
                logix = _logixHandle != null ? _logixHandle.getBean() : null;
                break;
                
            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                logix = InstanceManager.getDefault(LogixManager.class)
                        .getNamedBean(ref);
                break;
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                logix = InstanceManager.getDefault(LogixManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;
                
            case Formula:
                logix = _expressionNode != null ?
                        InstanceManager.getDefault(LogixManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        
//        System.out.format("ActionEnableLogix.execute: logix: %s%n", logix);
        
        if (logix == null) {
//            log.error("logix is null");
            return;
        }
        
        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewLock() : null;
        
        Operation lock;
        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            lock = _operationDirect;
        } else {
            lock = Operation.valueOf(name);
        }

        // Variables used in lambda must be effectively final
        Operation theLock = lock;
        
        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            logix.setEnabled(theLock == Operation.Enable);
        });
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
        return Bundle.getMessage(locale, "EnableLogix_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state;
        
        switch (_addressing) {
            case Direct:
                String logixName;
                if (_logixHandle != null) {
                    logixName = _logixHandle.getBean().getDisplayName();
                } else {
                    logixName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", logixName);
                break;
                
            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;
                
            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;
                
            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        
        switch (_operationAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _operationDirect._text);
                break;
                
            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _operationReference);
                break;
                
            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _operationLocalVariable);
                break;
                
            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _operationFormula);
                break;
                
            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _operationAddressing.name());
        }
        
        return Bundle.getMessage(locale, "EnableLogix_Long", namedBean, state);
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

    
    public enum Operation {
        Enable(Bundle.getMessage("EnableLogix_Enable")),
        Disable(Bundle.getMessage("EnableLogix_Disable"));
        
        private final String _text;
        
        private Operation(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnableLogix.class);
    
}
