package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a SignalHead.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionSignalHead extends AbstractDigitalAction
        implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<SignalHead> _signalHeadHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    
    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private OperationType _operationType = OperationType.Appearance;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;
    
    private NamedBeanAddressing _appearanceAddressing = NamedBeanAddressing.Direct;
    private int _signalHeadAppearance = SignalHead.DARK;
    private String _appearanceReference = "";
    private String _appearanceLocalVariable = "";
    private String _appearanceFormula = "";
    private ExpressionNode _appearanceExpressionNode;
    
    private NamedBeanHandle<SignalHead> _exampleSignalHeadHandle;
    
    
    public ActionSignalHead(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSignalHead copy = new ActionSignalHead(sysName, userName);
        copy.setComment(getComment());
        if (_signalHeadHandle != null) copy.setSignalHead(_signalHeadHandle);
        copy.setAppearance(_signalHeadAppearance);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationType(_operationType);
        copy.setOperationFormula(_operationFormula);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationReference(_operationReference);
        copy.setAppearanceAddressing(_appearanceAddressing);
        copy.setAppearanceFormula(_appearanceFormula);
        copy.setAppearanceLocalVariable(_appearanceLocalVariable);
        copy.setAppearanceReference(_appearanceReference);
        copy.setExampleSignalHead(_exampleSignalHeadHandle);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    public void setSignalHead(@Nonnull String signalHeadName) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalHeadName);
        if (signalHead != null) {
            setSignalHead(signalHead);
        } else {
            removeSignalHead();
            log.warn("signalHead \"{}\" is not found", signalHeadName);
        }
    }
    
    public void setSignalHead(@Nonnull NamedBeanHandle<SignalHead> handle) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        _signalHeadHandle = handle;
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
    }
    
    public void setSignalHead(@Nonnull SignalHead signalHead) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        setSignalHead(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead));
    }
    
    public void removeSignalHead() {
        assertListenersAreNotRegistered(log, "setSignalHead");
        if (_signalHeadHandle != null) {
            InstanceManager.getDefault(SignalHeadManager.class).removeVetoableChangeListener(this);
            _signalHeadHandle = null;
        }
    }
    
    public NamedBeanHandle<SignalHead> getSignalHead() {
        return _signalHeadHandle;
    }
    
    public void setExampleSignalHead(@Nonnull String signalHeadName) {
        assertListenersAreNotRegistered(log, "setExampleSignalHead");
        SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalHeadName);
        if (signalHead != null) {
            setExampleSignalHead(signalHead);
        } else {
            removeExampleSignalHead();
            log.warn("signalHead \"{}\" is not found", signalHeadName);
        }
    }
    
    public void setExampleSignalHead(@Nonnull NamedBeanHandle<SignalHead> handle) {
        assertListenersAreNotRegistered(log, "setExampleSignalHead");
        _exampleSignalHeadHandle = handle;
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
    }
    
    public void setExampleSignalHead(@Nonnull SignalHead signalHead) {
        assertListenersAreNotRegistered(log, "setExampleSignalHead");
        setExampleSignalHead(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead));
    }
    
    public void removeExampleSignalHead() {
        assertListenersAreNotRegistered(log, "removeExampleSignalHead");
        if (_exampleSignalHeadHandle != null) {
            InstanceManager.getDefault(SignalHeadManager.class).removeVetoableChangeListener(this);
            _exampleSignalHeadHandle = null;
        }
    }
    
    public NamedBeanHandle<SignalHead> getExampleSignalHead() {
        return _exampleSignalHeadHandle;
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
        parseOperationFormula();
    }
    
    public NamedBeanAddressing getOperationAddressing() {
        return _operationAddressing;
    }
    
    public void setOperationType(OperationType operationType) {
        _operationType = operationType;
    }
    
    public OperationType getOperationType() {
        return _operationType;
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
        parseOperationFormula();
    }
    
    public String getOperationFormula() {
        return _operationFormula;
    }
    
    private void parseOperationFormula() throws ParserException {
        if (_operationAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();
            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _operationExpressionNode = parser.parseExpression(_operationFormula);
        } else {
            _operationExpressionNode = null;
        }
    }
    
    public void setAppearanceAddressing(NamedBeanAddressing addressing) throws ParserException {
        _appearanceAddressing = addressing;
        parseAppearanceFormula();
    }
    
    public NamedBeanAddressing getAppearanceAddressing() {
        return _appearanceAddressing;
    }
    
    public void setAppearance(int appearance) {
        _signalHeadAppearance = appearance;
    }
    
    public int getAppearance() {
        return _signalHeadAppearance;
    }
    
    public void setAppearanceReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _appearanceReference = reference;
    }
    
    public String getAppearanceReference() {
        return _appearanceReference;
    }
    
    public void setAppearanceLocalVariable(@Nonnull String localVariable) {
        _appearanceLocalVariable = localVariable;
    }
    
    public String getAppearanceLocalVariable() {
        return _appearanceLocalVariable;
    }
    
    public void setAppearanceFormula(@Nonnull String formula) throws ParserException {
        _appearanceFormula = formula;
        parseAppearanceFormula();
    }
    
    public String getAppearanceFormula() {
        return _appearanceFormula;
    }
    
    private void parseAppearanceFormula() throws ParserException {
        if (_appearanceAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();
            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _appearanceExpressionNode = parser.parseExpression(_appearanceFormula);
        } else {
            _appearanceExpressionNode = null;
        }
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalHead) {
                if ((_signalHeadHandle != null)
                        && (evt.getOldValue().equals(_signalHeadHandle.getBean()))) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalHead_SignalHeadInUseSignalHeadActionVeto", getDisplayName()), e); // NOI18N
                }
                if ((_exampleSignalHeadHandle != null)
                        && (evt.getOldValue().equals(_exampleSignalHeadHandle.getBean()))) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalHead_SignalHeadInUseSignalHeadActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalHead) {
                if ((_signalHeadHandle != null)
                        && (evt.getOldValue().equals(_signalHeadHandle.getBean()))) {
                    removeSignalHead();
                }
                if ((_exampleSignalHeadHandle != null)
                        && (evt.getOldValue().equals(_exampleSignalHeadHandle.getBean()))) {
                    removeExampleSignalHead();
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
    
    private int getAppearanceFromName(String name) {
        if (_signalHeadHandle == null) throw new UnsupportedOperationException("_signalHeadHandle is null");
        
        SignalHead sh = _signalHeadHandle.getBean();
        String[] keys = sh.getValidStateKeys();
        for (int i=0; i < keys.length; i++) {
            if (name.equals(keys[i])) return sh.getValidStates()[i];
        }
        
        throw new IllegalArgumentException("Appearance "+name+" is not valid for signal head "+sh.getSystemName());
    }
    
    private int getNewAppearance() throws JmriException {
        
        switch (_appearanceAddressing) {
            case Direct:
                return _signalHeadAppearance;
                
            case Reference:
                return getAppearanceFromName(ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _appearanceReference));
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return getAppearanceFromName(TypeConversionUtil
                        .convertToString(symbolTable.getValue(_appearanceLocalVariable), false));
                
            case Formula:
                return _appearanceExpressionNode != null
                        ? getAppearanceFromName(TypeConversionUtil.convertToString(
                                _appearanceExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false))
                        : -1;
                
            default:
                throw new IllegalArgumentException("invalid _aspectAddressing state: " + _appearanceAddressing.name());
        }
    }
    
    private OperationType getOperation() throws JmriException {
        
        String oper = "";
        try {
            switch (_operationAddressing) {
                case Direct:
                    return _operationType;
                    
                case Reference:
                    oper = ReferenceUtil.getReference(
                            getConditionalNG().getSymbolTable(), _operationReference);
                    return OperationType.valueOf(oper);
                    
                case LocalVariable:
                    SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                    oper = TypeConversionUtil
                            .convertToString(symbolTable.getValue(_operationLocalVariable), false);
                    return OperationType.valueOf(oper);
                    
                case Formula:
                    if (_appearanceExpressionNode != null) {
                        oper = TypeConversionUtil.convertToString(
                                _operationExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false);
                        return OperationType.valueOf(oper);
                    } else {
                        return null;
                    }
                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _operationAddressing.name());
            }
        } catch (IllegalArgumentException e) {
            throw new JmriException("Unknown operation: "+oper, e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        SignalHead signalHead;
        
//        System.out.format("ActionSignalHead.execute: %s%n", getLongDescription());
        
        switch (_addressing) {
            case Direct:
                signalHead = _signalHeadHandle != null ? _signalHeadHandle.getBean() : null;
                break;
                
            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                signalHead = InstanceManager.getDefault(SignalHeadManager.class)
                        .getNamedBean(ref);
                break;
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                signalHead = InstanceManager.getDefault(SignalHeadManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;
                
            case Formula:
                signalHead = _expressionNode != null ?
                        InstanceManager.getDefault(SignalHeadManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        
//        System.out.format("ActionSignalHead.execute: sensor: %s%n", sensor);
        
        if (signalHead == null) {
//            log.warn("signalHead is null");
            return;
        }
        
        OperationType operation = getOperation();
        
        AtomicReference<JmriException> ref = new AtomicReference<>();
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            try {
                switch (operation) {
                    case Appearance:
                        int newAppearance = getNewAppearance();
                        if (newAppearance != -1) {
                            signalHead.setAppearance(newAppearance);
                        }
                        break;
                    case Lit:
                        signalHead.setLit(true);
                        break;
                    case NotLit:
                        signalHead.setLit(false);
                        break;
                    case Held:
                        signalHead.setHeld(true);
                        break;
                    case NotHeld:
                        signalHead.setHeld(false);
                        break;
        //            case PermissiveSmlDisabled:
        //                signalHead.setPermissiveSmlDisabled(true);
        //                break;
        //            case PermissiveSmlNotDisabled:
        //                signalHead.setPermissiveSmlDisabled(false);
        //                break;
                    default:
                        throw new JmriException("Unknown enum: "+_operationType.name());
                }
            } catch (JmriException e) {
                ref.set(e);
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
        return Bundle.getMessage(locale, "SignalHead_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String operation;
        String appearance;
        
        switch (_addressing) {
            case Direct:
                String sensorName;
                if (_signalHeadHandle != null) {
                    sensorName = _signalHeadHandle.getBean().getDisplayName();
                } else {
                    sensorName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", sensorName);
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
                operation = Bundle.getMessage(locale, "AddressByDirect", _operationType._text);
                break;

            case Reference:
                operation = Bundle.getMessage(locale, "AddressByReference", _operationReference);
                break;

            case LocalVariable:
                operation = Bundle.getMessage(locale, "AddressByLocalVariable", _operationLocalVariable);
                break;

            case Formula:
                operation = Bundle.getMessage(locale, "AddressByFormula", _operationFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _operationAddressing state: " + _operationAddressing.name());
        }
        
        switch (_appearanceAddressing) {
            case Direct:
                String a = "";
                if ((_signalHeadHandle != null) && (_signalHeadHandle.getBean() != null)) {
                    a = _signalHeadHandle.getBean().getAppearanceName(_signalHeadAppearance);
                }
                appearance = Bundle.getMessage(locale, "AddressByDirect", a);
                break;
                
            case Reference:
                appearance = Bundle.getMessage(locale, "AddressByReference", _appearanceReference);
                break;
                
            case LocalVariable:
                appearance = Bundle.getMessage(locale, "AddressByLocalVariable", _appearanceLocalVariable);
                break;
                
            case Formula:
                appearance = Bundle.getMessage(locale, "AddressByFormula", _appearanceFormula);
                break;
                
            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _appearanceAddressing.name());
        }
        
        if (_operationAddressing == NamedBeanAddressing.Direct) {
            if (_operationType == OperationType.Appearance) {
                return Bundle.getMessage(locale, "SignalHead_LongAppearance", namedBean, appearance);
            } else {
                return Bundle.getMessage(locale, "SignalHead_Long", namedBean, operation);
            }
        } else {
            return Bundle.getMessage(locale, "SignalHead_LongUnknownOper", namedBean, operation, appearance);
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
    
    
    
    public enum OperationType {
        Appearance(Bundle.getMessage("SignalHeadOperationType_Appearance")),
        Lit(Bundle.getMessage("SignalHeadOperationType_Lit")),
        NotLit(Bundle.getMessage("SignalHeadOperationType_NotLit")),
        Held(Bundle.getMessage("SignalHeadOperationType_Held")),
        NotHeld(Bundle.getMessage("SignalHeadOperationType_NotHeld"));
        
        private final String _text;
        
        private OperationType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHead.class);
    
}
