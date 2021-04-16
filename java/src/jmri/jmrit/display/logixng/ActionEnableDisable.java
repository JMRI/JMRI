package jmri.jmrit.display.logixng;

// import java.beans.PropertyChangeEvent;
// import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action enables/disables a Positionable on a panel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionEnableDisable extends AbstractDigitalAction implements VetoableChangeListener {

    private String _editorName;
    private Editor _editor;
    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
//    private NamedBeanHandle<Turnout> _turnoutHandle;
    private String _positionableName;
    private Positionable _positionable;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private IsControlling _isControlling = IsControlling.Enabled;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;
    
    public ActionEnableDisable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionEnableDisable copy = new ActionEnableDisable(sysName, userName);
        copy.setComment(getComment());
        copy.setEditor(_editorName);
        copy.setPositionable(_positionableName);
        copy.setIsControlling(_isControlling);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setStateAddressing(_stateAddressing);
        copy.setStateFormula(_stateFormula);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateReference(_stateReference);
        return manager.registerAction(copy);
    }
    
    public void setEditor(@CheckForNull String editorName) {
        assertListenersAreNotRegistered(log, "setEditor");
        _editorName = editorName;
        if (editorName != null) {
            _editor = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager.class).getByName(editorName);
        } else {
            _editor = null;
        }
//        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }
    
    public String getEditorName() {
        return _editorName;
    }
    
    public void setPositionable(@CheckForNull String positionableName) {
        assertListenersAreNotRegistered(log, "setPositionable");
        _positionableName = positionableName;
        if ((positionableName != null) && (_editor != null)) {
            _positionable = _editor.getIdContents().get(_positionableName);
        } else {
            _positionable = null;
        }
//        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }
    
    public String getPositionableName() {
        return _positionableName;
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
    
    public void setStateAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseStateFormula();
    }
    
    public NamedBeanAddressing getStateAddressing() {
        return _stateAddressing;
    }
    
    public void setIsControlling(IsControlling isControlling) {
        _isControlling = isControlling;
    }
    
    public IsControlling getIsControlling() {
        return _isControlling;
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
/*        
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Turnout_TurnoutInUseTurnoutExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    removeTurnout();
                }
            }
        }
*/        
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return CategoryDisplay.DISPLAY;
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
    public void execute() throws JmriException {
        Positionable positionable;
        
//        System.out.format("ActionEnableDisable.execute: %s%n", getLongDescription());
        
        switch (_addressing) {
            case Direct:
                positionable = this._positionable;
                break;
                
            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                positionable = _editor.getIdContents().get(ref);
                break;
                
            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                positionable = _editor.getIdContents().get(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;
                
            case Formula:
                positionable = _expressionNode != null ?
                        _editor.getIdContents().get(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        
//        System.out.format("ActionEnableDisable.execute: positionable: %s%n", positionable);
        
        if (positionable == null) {
            log.error("positionable is null");
            return;
        }
        
        String name = (_stateAddressing != NamedBeanAddressing.Direct)
                ? getNewState() : null;
        
        IsControlling state;
        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            state = _isControlling;
        } else {
            state = IsControlling.valueOf(name);
        }
        
        ThreadingUtil.runOnLayout(() -> {
            positionable.setControlling(state.getIsControlling());
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
        return Bundle.getMessage(locale, "ActionEnableDisable_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String editorName = _editorName != null ? _editorName : Bundle.getMessage(locale, "BeanNotSelected");
        String positonableName;
        String state;
        
        switch (_addressing) {
            case Direct:
                String positionableName;
                if (this._positionableName != null) {
                    positionableName = this._positionableName;
                } else {
                    positionableName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                positonableName = Bundle.getMessage(locale, "AddressByDirect", positionableName);
                break;
                
            case Reference:
                positonableName = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;
                
            case LocalVariable:
                positonableName = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;
                
            case Formula:
                positonableName = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;
                
            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        
        switch (_stateAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _isControlling._text);
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
        
        return Bundle.getMessage(locale, "ActionEnableDisable_Long", editorName, positonableName, state);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
/*        
        if ((_editorName != null) && (_editor == null)) {
            setEditor(_editorName);
        }
        if ((_positionableName != null) && (_positionable == null)) {
            setPositionable(_positionableName);
        }
*/        
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

    
    public enum IsControlling {
        Disabled(false, Bundle.getMessage("ActionEnableDisable_Disabled")),
        Enabled(true, Bundle.getMessage("ActionEnableDisable_Enabled"));
        
        private final boolean _isControlling;
        private final String _text;
        
        private IsControlling(boolean isControlling, String text) {
            this._isControlling = isControlling;
            this._text = text;
        }
        
        public boolean getIsControlling() {
            return _isControlling;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionEnableDisable.class);
    
}
