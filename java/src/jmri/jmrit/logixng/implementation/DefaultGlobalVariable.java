package jmri.jmrit.logixng.implementation;

import static jmri.NamedBean.UNKNOWN;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.*;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * The default implementation of GlobalVariable.
 *
 * @author Daniel Bergqvist Copyright 2018
 * @author Dave Sand        Copyright 2021
 */
public class DefaultGlobalVariable extends AbstractNamedBean
        implements GlobalVariable {

    private Object _value;
    private InitialValueType _initialValueType = InitialValueType.None;
    private String _initialValueData;


    public DefaultGlobalVariable(String sys, String user) throws BadUserNameException, BadSystemNameException  {
        super(sys, user);

        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(GlobalVariableManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setUserName(@CheckForNull String s) throws BadUserNameException {
        if ((s == null) || !SymbolTable.validateName(s)) {
            throw new BadUserNameException(
                    Bundle.getMessage(Locale.ENGLISH, "VariableNameIsNotValid", s),
                    Bundle.getMessage(Locale.getDefault(), "VariableNameIsNotValid", s));
        }
        super.setUserName(s);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable();

        Object value;

        switch (_initialValueType) {
            case None:
                value = null;
                break;

            case Integer:
                value = Long.parseLong(_initialValueData);
                break;

            case FloatingNumber:
                value = Double.parseDouble(_initialValueData);
                break;

            case String:
                value = _initialValueData;
                break;

            case Array:
                List<Object> array = new java.util.ArrayList<>();
                String initialValueData = _initialValueData;
                if (!initialValueData.isEmpty()) {
                    Object data = "";
                    String[] parts = initialValueData.split(":", 2);
                    if (parts.length > 1) {
                        initialValueData = parts[0];
                        if (Character.isDigit(parts[1].charAt(0))) {
                            try {
                                data = Long.parseLong(parts[1]);
                            } catch (NumberFormatException e) {
                                try {
                                    data = Double.parseDouble(parts[1]);
                                } catch (NumberFormatException e2) {
                                    throw new IllegalArgumentException("Data is not a number", e2);
                                }
                            }
                        } else if ((parts[1].charAt(0) == '"') && (parts[1].charAt(parts[1].length()-1) == '"')) {
                            data = parts[1].substring(1,parts[1].length()-1);
                        } else {
                            // Assume initial value is a local variable
                            data = symbolTable.getValue(parts[1]).toString();
                        }
                    }
                    try {
                        int count;
                        if (Character.isDigit(initialValueData.charAt(0))) {
                            count = Integer.parseInt(initialValueData);
                        } else {
                            // Assume size is a local variable
                            count = Integer.parseInt(symbolTable.getValue(initialValueData).toString());
                        }
                        for (int i=0; i < count; i++) array.add(data);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Initial capacity is not an integer", e);
                    }
                }
                // https://howtodoinjava.com/java/collections/arraylist/synchronize-arraylist/
                value = new CopyOnWriteArrayList<>(array);
                break;

            case Map:
                // https://crunchify.com/hashmap-vs-concurrenthashmap-vs-synchronizedmap-how-a-hashmap-can-be-synchronized-in-java/
                value = new ConcurrentHashMap<>();
                break;

            case LocalVariable:
                value = symbolTable.getValue(_initialValueData);
                break;

            case Memory:
                Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(_initialValueData);
                if (m != null) value = m.getValue();
                else return;
                break;

            case Reference:
                if (ReferenceUtil.isReference(_initialValueData)) {
                    value = ReferenceUtil.getReference(
                            symbolTable, _initialValueData);
                } else {
                    log.error("\"{}\" is not a reference", _initialValueData);
                    return;
                }
                break;

            case Formula:
                RecursiveDescentParser parser = createParser(symbolTable);
                ExpressionNode expressionNode = parser.parseExpression(_initialValueData);
                value = expressionNode.calculate(symbolTable);
                break;

            default:
                log.error("definition._initialValueType has invalid value: {}", _initialValueType.name());
                throw new IllegalArgumentException("definition._initialValueType has invalid value: " + _initialValueType.name());
        }

        setValue(value);
    }

    private RecursiveDescentParser createParser(SymbolTable symbolTable)
            throws ParserException {

        Map<String, Variable> variables = new HashMap<>();

        for (SymbolTable.Symbol symbol : symbolTable.getSymbols().values()) {
            variables.put(symbol.getName(),
                    new LocalVariableExpressionVariable(symbol.getName()));
        }

        return new RecursiveDescentParser(variables);
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(Object value) {
        Object old = _value;
        _value = value;
        // notify
        firePropertyChange("value", old, _value);
    }

    /** {@inheritDoc} */
    @Override
    public Object getValue() {
        return _value;
    }

    /** {@inheritDoc} */
    @Override
    public void setInitialValueType(InitialValueType type) {
        _initialValueType = type;
    }

    /** {@inheritDoc} */
    @Override
    public InitialValueType getInitialValueType() {
        return _initialValueType;
    }

    /** {@inheritDoc} */
    @Override
    public void setInitialValueData(String value) {
        _initialValueData = value;
    }

    /** {@inheritDoc} */
    @Override
    public String getInitialValueData() {
        return _initialValueData;
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameGlobalVariable");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in DefaultGlobalVariable.");  // NOI18N
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in DefaultGlobalVariable.");  // NOI18N
        return UNKNOWN;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return "GlobalVariable";
    }

    @Override
    public String getLongDescription(Locale locale) {
        return "GlobalVariable: "+getDisplayName();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        throw new UnsupportedOperationException("A GlobalVariable cannot have a parent");
    }

    /** {@inheritDoc} */
    @Override
    public boolean setParentForAllChildren(List<String> errors) {
        throw new UnsupportedOperationException("A GlobalVariable cannot have a parent");
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG getLogixNG() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void registerListeners() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListeners() {
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        printTree(settings, Locale.getDefault(), writer, indent, "", lineNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        printTree(settings, locale, writer, indent, "", lineNumber);
    }

    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            String currentIndent,
            MutableInt lineNumber) {

        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setup() {
        throw new UnsupportedOperationException("Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        throw new UnsupportedOperationException("Not supported");
    }

    /** {@inheritDoc} */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SLF4J_SIGN_ONLY_FORMAT",
                                                        justification="Specific log message format")
    public void getUsageTree(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("** {} :: {}", level, this.getClass().getName());

        throw new UnsupportedOperationException("Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
        throw new UnsupportedOperationException("Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public void getListenerRefsIncludingChildren(List<String> list) {
        list.addAll(getListenerRefs());
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultGlobalVariable.class);
}
