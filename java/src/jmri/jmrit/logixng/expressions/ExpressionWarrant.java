package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * This expression sets the state of a warrant.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionWarrant extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Warrant> _warrantHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private WarrantState _warrantState = WarrantState.RouteAllocated;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;
    
    public ExpressionWarrant(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionWarrant copy = new ExpressionWarrant(sysName, userName);
        copy.setComment(getComment());
        if (_warrantHandle != null) copy.setWarrant(_warrantHandle);
        copy.setBeanState(_warrantState);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.set_Is_IsNot(_is_IsNot);
        copy.setStateAddressing(_stateAddressing);
        copy.setStateFormula(_stateFormula);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateReference(_stateReference);
        return manager.registerExpression(copy);
    }
    
    public void setWarrant(@Nonnull String warrantName) {
        assertListenersAreNotRegistered(log, "setWarrant");
        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant(warrantName);
        if (warrant != null) {
            setWarrant(warrant);
        } else {
            removeWarrant();
            log.warn("warrant \"{}\" is not found", warrantName);
        }
    }
    
    public void setWarrant(@Nonnull NamedBeanHandle<Warrant> handle) {
        assertListenersAreNotRegistered(log, "setWarrant");
        _warrantHandle = handle;
        InstanceManager.getDefault(WarrantManager.class).addVetoableChangeListener(this);
    }
    
    public void setWarrant(@Nonnull Warrant warrant) {
        assertListenersAreNotRegistered(log, "setWarrant");
        setWarrant(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(warrant.getDisplayName(), warrant));
    }
    
    public void removeWarrant() {
        assertListenersAreNotRegistered(log, "setWarrant");
        if (_warrantHandle != null) {
            InstanceManager.getDefault(WarrantManager.class).removeVetoableChangeListener(this);
            _warrantHandle = null;
        }
    }
    
    public NamedBeanHandle<Warrant> getWarrant() {
        return _warrantHandle;
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
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setStateAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseStateFormula();
    }
    
    public NamedBeanAddressing getStateAddressing() {
        return _stateAddressing;
    }
    
    public void setBeanState(WarrantState state) {
        _warrantState = state;
    }
    
    public WarrantState getBeanState() {
        return _warrantState;
    }
    
    public void setStateReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _stateReference = reference;
    }
    
    public String getStateReference() {
        return _stateReference;
    }
    
    public void setStateLocalVariable(@Nonnull String localVariable) {
        _stateLocalVariable = localVariable;
    }
    
    public String getStateLocalVariable() {
        return _stateLocalVariable;
    }
    
    public void setStateFormula(@Nonnull String formula) throws ParserException {
        _stateFormula = formula;
        parseStateFormula();
    }
    
    public String getStateFormula() {
        return _stateFormula;
    }
    
    private void parseStateFormula() throws ParserException {
        if (_stateAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();
            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _stateExpressionNode = parser.parseExpression(_stateFormula);
        } else {
            _stateExpressionNode = null;
        }
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Warrant) {
                if (evt.getOldValue().equals(getWarrant().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Warrant_WarrantInUseWarrantExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Warrant) {
                if (evt.getOldValue().equals(getWarrant().getBean())) {
                    removeWarrant();
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
    
    private String getNewState() throws JmriException {
        
        switch (_stateAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference);
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_stateLocalVariable), false);
                
            case Formula:
                return _stateExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _stateExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _stateAddressing.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        Warrant warrant;
        
//        System.out.format("ExpressionWarrant.execute: %s%n", getLongDescription());
        
        switch (_addressing) {
            case Direct:
                warrant = _warrantHandle != null ? _warrantHandle.getBean() : null;
                break;
                
            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                warrant = InstanceManager.getDefault(WarrantManager.class)
                        .getNamedBean(ref);
                break;
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                warrant = InstanceManager.getDefault(WarrantManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;
                
            case Formula:
                warrant = _expressionNode != null ?
                        InstanceManager.getDefault(WarrantManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        
//        System.out.format("ExpressionWarrant.execute: warrant: %s%n", warrant);
        
        if (warrant == null) {
//            log.warn("warrant is null");
            return false;
        }
        
        WarrantState checkWarrantState;
        
        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            checkWarrantState = _warrantState;
        } else {
            checkWarrantState = WarrantState.valueOf(getNewState());
        }
        
        boolean result;
        
        switch (checkWarrantState) {
            case RouteFree:
                result = warrant.routeIsFree();
                break;
            case RouteOccupied:
                result = warrant.routeIsOccupied();
                break;
            case RouteAllocated:
                result = warrant.isAllocated();
                break;
            case RouteSet:
                result = warrant.hasRouteSet();
                break;
            case TrainRunning:
                result = ! (warrant.getRunMode() == Warrant.MODE_NONE);
                break;
            default:
                throw new UnsupportedOperationException("checkWarrantState has unknown value: " + checkWarrantState.name());
        }
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return result;
        } else {
            return !result;
        }
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
        return Bundle.getMessage(locale, "Warrant_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state;
        
        switch (_addressing) {
            case Direct:
                String warrantName;
                if (_warrantHandle != null) {
                    warrantName = _warrantHandle.getBean().getDisplayName();
                } else {
                    warrantName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", warrantName);
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
        
        switch (_stateAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _warrantState._text);
                break;
                
            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _stateReference);
                break;
                
            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _stateLocalVariable);
                break;
                
            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _stateFormula);
                break;
                
            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _stateAddressing.name());
        }
        
        return Bundle.getMessage(locale, "Warrant_Long", namedBean, _is_IsNot.toString(), state);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_warrantHandle != null)) {
            _warrantHandle.getBean().addPropertyChangeListener("KnownState", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _warrantHandle.getBean().removePropertyChangeListener("KnownState", this);
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
    
    
    public enum WarrantState {
        RouteFree(Bundle.getMessage("WarrantTypeRouteFree")),
        RouteOccupied(Bundle.getMessage("WarrantTypeOccupied")),
        RouteAllocated(Bundle.getMessage("WarrantTypeAllocated")),
        RouteSet(Bundle.getMessage("WarrantTypeRouteSet")),
        TrainRunning(Bundle.getMessage("WarrantTypeTrainRunning"));
        
        private final String _text;
        
        private WarrantState(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionWarrant.class);
    
}
