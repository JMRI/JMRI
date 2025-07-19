package jmri.jmrit.logixng.implementation;

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
    @SuppressWarnings({"unchecked", "rawtypes"})    // Checked cast is not possible due to type erasure
    public void initialize() throws JmriException {
        SymbolTable symbolTable = new DefaultSymbolTable();

        Object value;

        switch (_initialValueType) {

            case Array:
                var newArray = SymbolTable.getInitialValue(
                        SymbolTable.Type.Global,
                        getUserName(),
                        _initialValueType,
                        _initialValueData,
                        symbolTable,
                        symbolTable.getSymbols());

                // Convert the array to a thread safe array
                // https://howtodoinjava.com/java/collections/arraylist/synchronize-arraylist/
                value = new CopyOnWriteArrayList<>((List)newArray);
                break;

            case Map:
                // https://crunchify.com/hashmap-vs-concurrenthashmap-vs-synchronizedmap-how-a-hashmap-can-be-synchronized-in-java/
                value = new ConcurrentHashMap<>();
                break;

            default:
                value = SymbolTable.getInitialValue(
                        SymbolTable.Type.Global,
                        getUserName(),
                        _initialValueType,
                        _initialValueData,
                        symbolTable,
                        symbolTable.getSymbols());
        }

        if (value != _value) setValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(Object value) {
        Object old = _value;
        LogixNGPreferences prefs = InstanceManager.getDefault(LogixNGPreferences.class);
        if (prefs.getStrictTypingLocalVariables()) {
            _value = SymbolTable.validateStrictTyping(_initialValueType, _value, value);
        } else {
            _value = value;
        }
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
    public LogixNG_Category getCategory() {
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
