package jmri.jmrit.display.logixng;

import java.beans.*;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.display.EditorManager;
import static jmri.jmrit.display.EditorManager.EDITORS;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action controls various things of a LayoutTurnout on a LayoutEditor panel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionLayoutTurnout extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private String _layoutEditorName;
    private LayoutEditor _layoutEditor;
    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private String _layoutTurnoutName;
    private LayoutTurnout _layoutTurnout;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private Operation _operation = Operation.Enable;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    public ActionLayoutTurnout(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionLayoutTurnout copy = new ActionLayoutTurnout(sysName, userName);
        copy.setComment(getComment());
        if (_layoutEditor != null) {
            copy.setLayoutEditor(_layoutEditor.getName());
        }
        copy.setLayoutTurnout(_layoutTurnout);
        copy.setOperation(_operation);
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

    public void setLayoutEditor(@CheckForNull String layoutEditorName) {
        assertListenersAreNotRegistered(log, "setEditor");

        InstanceManager.getDefault(EditorManager.class)
                .removePropertyChangeListener(EDITORS, this);

        _layoutEditorName = layoutEditorName;

        if (layoutEditorName != null) {
            _layoutEditor = InstanceManager.getDefault(EditorManager.class)
                    .get(LayoutEditor.class, layoutEditorName);
        } else {
            _layoutEditor = null;
        }
        if (_layoutEditor != null) {
            InstanceManager.getDefault(EditorManager.class)
                    .addPropertyChangeListener(EDITORS, this);
        } else {
            _layoutTurnout = null;
        }
//        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    public String getLayoutEditorName() {
        if (_layoutEditor != null) {
            return _layoutEditor.getName();
        } else {
            return null;
        }
    }

    public LayoutTurnout findLayoutTurnout(String name) {
        if (_layoutEditor != null) {
            for (LayoutTurnout lt : _layoutEditor.getLayoutTurnouts()) {
                String turnoutName = lt.getTurnoutName();
                if (!turnoutName.isBlank() && name.equals(turnoutName)) {
                    return lt;
                }
            }
        }
        return null;
    }

    public LayoutTurnout findLayoutTurnout(jmri.Turnout turnout) {
        if (_layoutEditor != null) {
            for (LayoutTurnout lt : _layoutEditor.getLayoutTurnouts()) {
                String turnoutName = lt.getTurnoutName();
                if (!turnoutName.isBlank()
                        && (turnoutName.equals(turnout.getSystemName())
                            || turnoutName.equals(turnout.getUserName()))) {
                    return lt;
                }
            }
        }
        return null;
    }

    public void setLayoutTurnout(@CheckForNull String layoutTurnoutName) {
        assertListenersAreNotRegistered(log, "setLayoutTurnout");
        _layoutTurnoutName = layoutTurnoutName;
        if ((layoutTurnoutName != null) && (_layoutEditor != null)) {
            this._layoutTurnout = findLayoutTurnout(layoutTurnoutName);
        } else {
            this._layoutTurnout = null;
        }
//        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    public void setLayoutTurnout(@CheckForNull LayoutTurnout layoutTurnout) {
        assertListenersAreNotRegistered(log, "setLayoutTurnout");
        if ((layoutTurnout != null) && (_layoutEditor != null)) {
            this._layoutTurnout = layoutTurnout;
        } else {
            this._layoutTurnout = null;
        }
//        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    public LayoutTurnout getLayoutTurnout() {
        return _layoutTurnout;
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

    public void setOperation(Operation isControlling) {
        _operation = isControlling;
    }

    public Operation getOperation() {
        return _operation;
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
    public LogixNG_Category getCategory() {
        return CategoryDisplay.DISPLAY;
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
        LayoutTurnout layoutTurnout;

//        System.out.format("ActionLayoutTurnout.execute: %s%n", getLongDescription());

        Object value;

        switch (_addressing) {
            case Direct:
                layoutTurnout = this._layoutTurnout;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                layoutTurnout = findLayoutTurnout(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                value = symbolTable.getValue(_localVariable);
                if (value instanceof jmri.Turnout) {
                    layoutTurnout = findLayoutTurnout((jmri.Turnout)value);
                } else {
                    layoutTurnout = findLayoutTurnout(TypeConversionUtil
                                    .convertToString(value, false));
                }
                break;

            case Formula:
                if (_expressionNode != null) {
                    value = _expressionNode.calculate(getConditionalNG().getSymbolTable());
                    if (value instanceof jmri.Turnout) {
                        layoutTurnout = findLayoutTurnout((jmri.Turnout)value);
                    } else {
                        layoutTurnout = findLayoutTurnout(TypeConversionUtil
                                        .convertToString(value, false));
                    }
                } else {
                    layoutTurnout = null;
                }
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("ActionLayoutTurnout.execute: layoutTurnout: %s%n", layoutTurnout);

        if (layoutTurnout == null) {
            log.debug("layoutTurnout is null");
            return;
        }

        String name = (_stateAddressing != NamedBeanAddressing.Direct)
                ? getNewState() : null;

        Operation operation;
        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            operation = _operation;
        } else {
            operation = Operation.valueOf(name);
        }

        ThreadingUtil.runOnGUI(() -> {
            switch (operation) {
                case Disable:
                    layoutTurnout.setDisabled(true);
                    break;
                case Enable:
                    layoutTurnout.setDisabled(false);
                    break;
/*
                case Hide:
                    layoutTurnout.setHidden(true);
                    break;
                case Show:
                    layoutTurnout.setHidden(false);
                    break;
*/
                default:
                    throw new RuntimeException("operation has invalid value: "+operation.name());
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
        return Bundle.getMessage(locale, "ActionLayoutTurnout_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String editorName = _layoutEditor != null
                ? _layoutEditor.getName() : Bundle.getMessage(locale, "BeanNotSelected");
        String positonableName;
        String state;

        switch (_addressing) {
            case Direct:
                String layoutTurnoutName;
                if (this._layoutTurnout != null) {
                    layoutTurnoutName = this._layoutTurnout.getTurnoutName();
                } else {
                    layoutTurnoutName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                positonableName = Bundle.getMessage(locale, "AddressByDirect", layoutTurnoutName);
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
                state = Bundle.getMessage(locale, "AddressByDirect", _operation._text);
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

        return Bundle.getMessage(locale, "ActionLayoutTurnout_Long", editorName, positonableName, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        if ((_layoutEditorName != null) && (_layoutEditor == null)) {
            setLayoutEditor(_layoutEditorName);
        }
        if ((_layoutTurnoutName != null) && (_layoutTurnout == null)) {
            setLayoutTurnout(_layoutTurnoutName);
        }
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
    public void propertyChange(PropertyChangeEvent evt) {
        if (EDITORS.equals(evt.getPropertyName())) {
            if (evt.getOldValue() == _layoutEditor) {
                _layoutEditor = null;
                _layoutTurnout = null;
                InstanceManager.getDefault(EditorManager.class)
                        .removePropertyChangeListener(EDITORS, this);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        InstanceManager.getDefault(EditorManager.class)
                .removePropertyChangeListener(EDITORS, this);
    }


    public enum Operation {
        Disable(Bundle.getMessage("ActionLayoutTurnout_Disable")),
        Enable(Bundle.getMessage("ActionLayoutTurnout_Enable"));
//        Hide(Bundle.getMessage("ActionLayoutTurnout_Hide")),
//        Show(Bundle.getMessage("ActionLayoutTurnout_Show"));

        private final String _text;

        private Operation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLayoutTurnout.class);

}
