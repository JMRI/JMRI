package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.Logix;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers a entryExit.
 * <p>
 * This action has the Operation enum, similar to EnableLogix and other actions,
 * despite that's not needed since this action only has one option. But it's
 * here in case someone wants to add more options later.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionEntryExit extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<DestinationPoints> _destinationPointsHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private Operation _operationDirect = Operation.SetNXPairEnabled;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;
    
    public ActionEntryExit(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionEntryExit copy = new ActionEntryExit(sysName, userName);
        copy.setComment(getComment());
        if (_destinationPointsHandle != null) copy.setDestinationPoints(_destinationPointsHandle);
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
    
    public void setDestinationPoints(@Nonnull String entryExitName) {
        assertListenersAreNotRegistered(log, "setDestinationPoints");
        DestinationPoints entryExit = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).getNamedBean(entryExitName);
        if (entryExit != null) {
            ActionEntryExit.this.setDestinationPoints(entryExit);
        } else {
            removeDestinationPoints();
            log.error("DestinationPoints \"{}\" is not found", entryExitName);
        }
    }
    
    public void setDestinationPoints(@Nonnull NamedBeanHandle<DestinationPoints> handle) {
        assertListenersAreNotRegistered(log, "setDestinationPoints");
        _destinationPointsHandle = handle;
        InstanceManager.getDefault(LogixManager.class).addVetoableChangeListener(this);
    }
    
    public void setDestinationPoints(@Nonnull DestinationPoints entryExit) {
        assertListenersAreNotRegistered(log, "setDestinationPoints");
        ActionEntryExit.this.setDestinationPoints(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(entryExit.getDisplayName(), entryExit));
    }
    
    public void removeDestinationPoints() {
        assertListenersAreNotRegistered(log, "removeEntryExit");
        if (_destinationPointsHandle != null) {
            InstanceManager.getDefault(LogixManager.class).removeVetoableChangeListener(this);
            _destinationPointsHandle = null;
        }
    }
    
    public NamedBeanHandle<DestinationPoints> getDestinationPoints() {
        return _destinationPointsHandle;
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
            if (evt.getOldValue() instanceof DestinationPoints) {
                if (evt.getOldValue().equals(getDestinationPoints().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionEntryExit_DestinationPointsInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DestinationPoints) {
                if (evt.getOldValue().equals(getDestinationPoints().getBean())) {
                    removeDestinationPoints();
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
        DestinationPoints entryExit;
        
//        System.out.format("ActionEnableLogix.execute: %s%n", getLongDescription());
        
        switch (_addressing) {
            case Direct:
                entryExit = _destinationPointsHandle != null ? _destinationPointsHandle.getBean() : null;
                break;
                
            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                entryExit = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class)
                        .getNamedBean(ref);
                break;
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                entryExit = InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;
                
            case Formula:
                entryExit = _expressionNode != null ?
                        InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        
//        System.out.format("ActionEnableLogix.execute: entryExit: %s%n", entryExit);
        
        if (entryExit == null) {
//            log.error("entryExit is null");
            return;
        }
        
        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewLock() : null;
        
        Operation oper;
        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            oper = _operationDirect;
        } else {
            oper = Operation.valueOf(name);
        }

        // Variables used in lambda must be effectively final
        Operation theOper = oper;
        
        ThreadingUtil.runOnLayout(() -> {
            switch (theOper) {
                case SetNXPairEnabled:
                    entryExit.setEnabled(true);
                    break;
                case SetNXPairDisabled:
                    entryExit.setEnabled(false);
                    break;
                case SetNXPairSegment:
                    jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                            setSingleSegmentRoute(entryExit.getSystemName());
                    break;
                default:
                    throw new IllegalArgumentException("invalid oper state: " + theOper.name());
            }
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
        return Bundle.getMessage(locale, "ActionEntryExit_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state;
        
        switch (_addressing) {
            case Direct:
                String entryExitName;
                if (_destinationPointsHandle != null) {
                    entryExitName = _destinationPointsHandle.getBean().getDisplayName();
                } else {
                    entryExitName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", entryExitName);
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
        
        return Bundle.getMessage(locale, "ActionEntryExit_Long", namedBean, state);
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
        SetNXPairEnabled(Bundle.getMessage("ActionEntryExit_SetNXPairEnabled")),
        SetNXPairDisabled(Bundle.getMessage("ActionEntryExit_SetNXPairDisabled")),
        SetNXPairSegment(Bundle.getMessage("ActionEntryExit_SetNXPairSegment"));
        
        private final String _text;
        
        private Operation(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionEntryExit.class);
    
}
