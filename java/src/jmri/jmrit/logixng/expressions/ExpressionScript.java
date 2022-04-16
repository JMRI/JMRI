package jmri.jmrit.logixng.expressions;

import java.beans.*;
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
import jmri.script.JmriScriptEngineManager;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

import org.apache.commons.lang3.mutable.MutableBoolean;

/**
 * Executes a script.
 * The method evaluate() creates a MutableBoolean with the value "false" and
 * then sends that value as the variable "result" to the script. The script
 * then sets the value by the code: "result.setValue(value)"
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionScript extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private OperationType _operationType = OperationType.JythonCommand;
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

    private String _registerScript = "";
    private String _unregisterScript = "";


    public ExpressionScript(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionScript copy = new ExpressionScript(sysName, userName);
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
        copy.setRegisterListenerScript(_registerScript);
        copy.setUnregisterListenerScript(_unregisterScript);
        return manager.registerExpression(copy);
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

    public void setRegisterListenerScript(String script) {
        if (script == null) _registerScript = "";
        else _registerScript = script;
    }

    public String getRegisterListenerScript() {
        return _registerScript;
    }

    public void setUnregisterListenerScript(String script) {
        if (script == null) _unregisterScript = "";
        else _unregisterScript = script;
    }

    public String getUnregisterListenerScript() {
        return _unregisterScript;
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
    public boolean evaluate() throws JmriException {

        OperationType operation = getOperation();
        String script = getTheScript();

        JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

        Bindings bindings = new SimpleBindings();
        MutableBoolean result = new MutableBoolean(false);

        // this should agree with help/en/html/tools/scripting/Start.shtml - this link is wrong and should point to LogixNG documentation
        bindings.put("analogActions", InstanceManager.getNullableDefault(AnalogActionManager.class));
        bindings.put("analogExpressions", InstanceManager.getNullableDefault(AnalogExpressionManager.class));
        bindings.put("digitalActions", InstanceManager.getNullableDefault(DigitalActionManager.class));
        bindings.put("digitalBooleanActions", InstanceManager.getNullableDefault(DigitalBooleanActionManager.class));
        bindings.put("digitalExpressions", InstanceManager.getNullableDefault(DigitalExpressionManager.class));
        bindings.put("stringActions", InstanceManager.getNullableDefault(StringActionManager.class));
        bindings.put("stringExpressions", InstanceManager.getNullableDefault(StringExpressionManager.class));

        SymbolTable symbolTable = getConditionalNG().getSymbolTable();
        bindings.put("symbolTable", symbolTable);    // Give the script access to the local variable 'symbolTable'

        bindings.put("result", result);     // Give the script access to the local variable 'result'

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch (operation) {
                case RunScript:
                    try (InputStreamReader reader = new InputStreamReader(
                            new FileInputStream(jmri.util.FileUtil.getExternalFilename(script)),
                            StandardCharsets.UTF_8)) {
                        scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                                .eval(reader, bindings);
                    } catch (IOException | ScriptException e) {
                        log.warn("cannot execute script", e);
                    }
                    break;

                case JythonCommand:
                    try {
                        String theScript = String.format("import jmri%n") + script;
                        scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                                .eval(theScript, bindings);
                    } catch (ScriptException e) {
                        log.warn("cannot execute script", e);
                    }
                    break;

                default:
                    throw new IllegalArgumentException("invalid _stateAddressing state: " + _scriptAddressing.name());
            }
        });

        return result.booleanValue();
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
        return Bundle.getMessage(locale, "ExpressionScript_Short");
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
            return Bundle.getMessage(locale, "ExpressionScript_Long", operation, script);
        } else {
            return Bundle.getMessage(locale, "ExpressionScript_LongUnknownOper", operation, script);
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

            if (!_registerScript.trim().isEmpty()) {
                JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

                Bindings bindings = new SimpleBindings();
                MutableBoolean result = new MutableBoolean(false);

                // this should agree with help/en/html/tools/scripting/Start.shtml - this link is wrong and should point to LogixNG documentation
                bindings.put("analogActions", InstanceManager.getNullableDefault(AnalogActionManager.class));
                bindings.put("analogExpressions", InstanceManager.getNullableDefault(AnalogExpressionManager.class));
                bindings.put("digitalActions", InstanceManager.getNullableDefault(DigitalActionManager.class));
                bindings.put("digitalBooleanActions", InstanceManager.getNullableDefault(DigitalBooleanActionManager.class));
                bindings.put("digitalExpressions", InstanceManager.getNullableDefault(DigitalExpressionManager.class));
                bindings.put("stringActions", InstanceManager.getNullableDefault(StringActionManager.class));
                bindings.put("stringExpressions", InstanceManager.getNullableDefault(StringExpressionManager.class));

                bindings.put("result", result);     // Give the script access to the local variable 'result'

                bindings.put("self", this);         // Give the script access to myself with the local variable 'self'

                ThreadingUtil.runOnLayout(() -> {
                    try {
                        String theScript = String.format("import jmri%n") + _registerScript;
                        scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                                .eval(theScript, bindings);
                    } catch (RuntimeException | ScriptException e) {
                        log.warn("cannot execute script during registerListeners", e);
                    }
                });
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _listenersAreRegistered = false;

            if (!_unregisterScript.trim().isEmpty()) {
                JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

                Bindings bindings = new SimpleBindings();
                MutableBoolean result = new MutableBoolean(false);

                // this should agree with help/en/html/tools/scripting/Start.shtml - this link is wrong and should point to LogixNG documentation
                bindings.put("analogActions", InstanceManager.getNullableDefault(AnalogActionManager.class));
                bindings.put("analogExpressions", InstanceManager.getNullableDefault(AnalogExpressionManager.class));
                bindings.put("digitalActions", InstanceManager.getNullableDefault(DigitalActionManager.class));
                bindings.put("digitalBooleanActions", InstanceManager.getNullableDefault(DigitalBooleanActionManager.class));
                bindings.put("digitalExpressions", InstanceManager.getNullableDefault(DigitalExpressionManager.class));
                bindings.put("stringActions", InstanceManager.getNullableDefault(StringActionManager.class));
                bindings.put("stringExpressions", InstanceManager.getNullableDefault(StringExpressionManager.class));

                bindings.put("result", result);     // Give the script access to the local variable 'result'

                bindings.put("self", this);         // Give the script access to myself with the local variable 'self'

                ThreadingUtil.runOnLayout(() -> {
                    try {
                        String theScript = String.format("import jmri%n") + _unregisterScript;
                        scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                                .eval(theScript, bindings);
                    } catch (RuntimeException | ScriptException e) {
                        log.warn("cannot execute script during unregisterListeners", e);
                    }
                });
            }
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
        // Do nothing
    }


    public enum OperationType {
        RunScript(Bundle.getMessage("ExpressionScript_RunScript")),
        JythonCommand(Bundle.getMessage("ExpressionScript_JythonCommand"));

        private final String _text;

        private OperationType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionScript.class);

}
