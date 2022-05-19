package jmri.jmrit.logixng.actions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.Nonnull;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.script.ScriptEngineSelector;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * Executes a script.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionScript extends AbstractDigitalAction {

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private OperationType _operationType = OperationType.SingleLineCommand;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    private NamedBeanAddressing _scriptAddressing = NamedBeanAddressing.Direct;
    private String _script = "";
    private String _scriptReference = "";
    private String _scriptLocalVariable = "";
    private String _scriptFormula = "";
    private ExpressionNode _scriptExpressionNode;

    private final ScriptEngineSelector _scriptEngineSelector = new ScriptEngineSelector();


    public ActionScript(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionScript copy = new ActionScript(sysName, userName);
        copy.setComment(getComment());
        copy.setScript(_script);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationType(_operationType);
        copy.setOperationFormula(_operationFormula);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationReference(_operationReference);
        copy.setScriptAddressing(_scriptAddressing);
        copy.setScriptFormula(_scriptFormula);
        copy.setScriptLocalVariable(_scriptLocalVariable);
        copy.setScriptReference(_scriptReference);
        return manager.registerAction(copy);
    }

    public ScriptEngineSelector getScriptEngineSelector() {
        return _scriptEngineSelector;
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

    public void setScriptAddressing(NamedBeanAddressing addressing) throws ParserException {
        _scriptAddressing = addressing;
        parseScriptFormula();
    }

    public NamedBeanAddressing getScriptAddressing() {
        return _scriptAddressing;
    }

    public void setScript(String script) {
        if (script == null) _script = "";
        else _script = script;
    }

    public String getScript() {
        return _script;
    }

    public void setScriptReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _scriptReference = reference;
    }

    public String getScriptReference() {
        return _scriptReference;
    }

    public void setScriptLocalVariable(@Nonnull String localVariable) {
        _scriptLocalVariable = localVariable;
    }

    public String getScriptLocalVariable() {
        return _scriptLocalVariable;
    }

    public void setScriptFormula(@Nonnull String formula) throws ParserException {
        _scriptFormula = formula;
        parseScriptFormula();
    }

    public String getScriptFormula() {
        return _scriptFormula;
    }

    private void parseScriptFormula() throws ParserException {
        if (_scriptAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _scriptExpressionNode = parser.parseExpression(_scriptFormula);
        } else {
            _scriptExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getTheScript() throws JmriException {

        switch (_scriptAddressing) {
            case Direct:
                return _script;

            case Reference:
                return ReferenceUtil.getReference(getConditionalNG().getSymbolTable(), _scriptReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_scriptLocalVariable), false);

            case Formula:
                return _scriptExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _scriptExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : "";

            default:
                throw new IllegalArgumentException("invalid _scriptAddressing state: " + _scriptAddressing.name());
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
                    if (_scriptExpressionNode != null) {
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

        OperationType operation = getOperation();
        String script = getTheScript();

        Bindings bindings = new SimpleBindings();

        LogixNG_ScriptBindings.addScriptBindings(bindings);

        SymbolTable symbolTable = getConditionalNG().getSymbolTable();
        bindings.put("symbolTable", symbolTable);    // Give the script access to the local variable 'symbolTable'

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            ScriptEngineSelector.Engine engine =
                    _scriptEngineSelector.getSelectedEngine();

            if (engine == null) throw new JmriException("Script engine is null");

            switch (operation) {
                case RunScript:
                    try (InputStreamReader reader = new InputStreamReader(
                            new FileInputStream(jmri.util.FileUtil.getExternalFilename(script)),
                            StandardCharsets.UTF_8)) {
                        engine.getScriptEngine().eval(reader, bindings);
                    } catch (IOException | ScriptException e) {
                        log.warn("cannot execute script \"{}\"", script, e);
                    }
                    break;

                case SingleLineCommand:
                    try {
                        String theScript;
                        if (engine.isJython()) {
                            theScript = String.format("import jmri%n") + script;
                        } else {
                            theScript = script;
                        }
                        engine.getScriptEngine().eval(theScript, bindings);
                    } catch (ScriptException e) {
                        log.warn("cannot execute script", e);
                    }
                    break;

                default:
                    throw new IllegalArgumentException("invalid _stateAddressing state: " + _scriptAddressing.name());
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
        return Bundle.getMessage(locale, "ActionScript_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String operation;
        String script;

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

        switch (_scriptAddressing) {
            case Direct:
                script = Bundle.getMessage(locale, "AddressByDirect", _script);
                break;

            case Reference:
                script = Bundle.getMessage(locale, "AddressByReference", _scriptReference);
                break;

            case LocalVariable:
                script = Bundle.getMessage(locale, "AddressByLocalVariable", _scriptLocalVariable);
                break;

            case Formula:
                script = Bundle.getMessage(locale, "AddressByFormula", _scriptFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _scriptAddressing.name());
        }

        if (_operationAddressing == NamedBeanAddressing.Direct) {
            return Bundle.getMessage(locale, "ActionScript_Long", operation, script);
        } else {
            return Bundle.getMessage(locale, "ActionScript_LongUnknownOper", operation, script);
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
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void firePropertyChange(String p, Object old, Object n) {
        super.firePropertyChange(p, old, n);
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }


    public enum OperationType {
        RunScript(Bundle.getMessage("ActionScript_RunScript")),
        SingleLineCommand(Bundle.getMessage("ActionScript_SingleLineCommand"));

        private final String _text;

        private OperationType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionScript.class);

}
