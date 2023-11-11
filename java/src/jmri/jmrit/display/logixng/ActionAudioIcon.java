package jmri.jmrit.display.logixng;

import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.AudioIcon;
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
 * This action controls various things of a AudioIcon on a panel.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class ActionAudioIcon extends AbstractDigitalAction implements VetoableChangeListener {

    private String _editorName;
    private Editor _editor;
    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private String _positionableName;
    private AudioIcon _audioIcon;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private Operation _operation = Operation.Play;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    public ActionAudioIcon(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user, CategoryDisplay.DISPLAY);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionAudioIcon copy = new ActionAudioIcon(sysName, userName);
        copy.setComment(getComment());
        copy.setEditor(_editorName);
        copy.setAudioIcon(_positionableName);
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

    public void setEditor(@CheckForNull String editorName) {
        assertListenersAreNotRegistered(log, "setEditor");
        _editorName = editorName;
        if (editorName != null) {
            _editor = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager.class).getByName(editorName);
        } else {
            _editor = null;
        }
    }

    public String getEditorName() {
        return _editorName;
    }

    public void setAudioIcon(@CheckForNull String positionableName) {
        assertListenersAreNotRegistered(log, "setAudioIcon");
        _positionableName = positionableName;
        if ((positionableName != null) && (_editor != null)) {
            Positionable pos = _editor.getIdContents().get(_positionableName);
            if (pos instanceof AudioIcon) {
                _audioIcon = (AudioIcon)pos;
            } else {
                throw new IllegalArgumentException("positionableName is not an AudioIcon");
            }
        } else {
            _audioIcon = null;
        }
    }

    public String getAudioIconName() {
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

        switch (_addressing) {
            case Direct:
                positionable = this._audioIcon;
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

        if (positionable == null) {
            log.error("positionable is null");
            return;
        }

        AudioIcon audioIcon;
        if (positionable instanceof AudioIcon) {
            audioIcon = (AudioIcon)positionable;
        } else {
            throw new IllegalArgumentException("positionableName is not an AudioIcon");
        }


        String name = (_stateAddressing != NamedBeanAddressing.Direct)
                ? getNewState() : null;

        Operation operation;
        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            operation = _operation;
        } else {
            operation = Operation.valueOf(name);
        }

        ThreadingUtil.runOnLayout(() -> {
            switch (operation) {
                case Play:
                    audioIcon.play();
                    break;
                case Stop:
                    audioIcon.stop();
                    break;
                default:
                    throw new RuntimeException("operation has invalid value: "+operation.name());
            }
        });
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionAudioIcon_Short");
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

        return Bundle.getMessage(locale, "ActionAudioIcon_Long", editorName, positonableName, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        if ((_editorName != null) && (_editor == null)) {
            setEditor(_editorName);
        }
        if ((_positionableName != null) && (_audioIcon == null)) {
            setAudioIcon(_positionableName);
        }
    }

    public enum Operation {
        Play(Bundle.getMessage("ActionAudioIcon_Play")),
        Stop(Bundle.getMessage("ActionAudioIcon_Stop"));

        private final String _text;

        private Operation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudioIcon.class);

}
