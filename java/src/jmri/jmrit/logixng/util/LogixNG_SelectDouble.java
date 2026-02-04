package jmri.jmrit.logixng.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Select a double for LogixNG actions and expressions.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectDouble implements VetoableChangeListener {

    private final AbstractBase _base;
    private final InUse _inUse;
    private final LogixNG_SelectTable _selectTable;
    private final int _numDecimals;
    private final PropertyChangeListener _listener;
    private boolean _listenToMemory;
    private boolean _listenersAreRegistered;
    private final FormatterParserValidator _formatterParserValidator;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private double _value;
    private String _reference = "";
    private String _localVariable = "";
    private NamedBeanHandle<Memory> _memoryHandle;
    private String _formula = "";
    private ExpressionNode _expressionNode;


    public LogixNG_SelectDouble(
            @Nonnull AbstractBase base,
            int numDecimals,
            @Nonnull PropertyChangeListener listener) {

        this(base, numDecimals, listener, new DefaultFormatterParserValidator());
    }

    public LogixNG_SelectDouble(
            @Nonnull AbstractBase base,
            int numDecimals,
            @Nonnull PropertyChangeListener listener,
            @Nonnull FormatterParserValidator formatterParserValidator) {
        _base = base;
        _inUse = () -> true;
        _selectTable = new LogixNG_SelectTable(_base, _inUse);
        _numDecimals = numDecimals;
        _listener = listener;
        _formatterParserValidator = formatterParserValidator;
        _value = _formatterParserValidator.getInitialValue();
    }

    public void copy(LogixNG_SelectDouble copy) throws ParserException {
        copy.setAddressing(_addressing);
        copy.setValue(_value);
        copy.setLocalVariable(_localVariable);
        copy.setMemory(_memoryHandle);
        copy.setListenToMemory(_listenToMemory);
        copy.setReference(_reference);
        copy.setFormula(_formula);
        _selectTable.copy(copy._selectTable);
    }

    @Nonnull
    public FormatterParserValidator getFormatterParserValidator() {
        return _formatterParserValidator;
    }

    public void setAddressing(@Nonnull NamedBeanAddressing addressing) throws ParserException {
        this._addressing = addressing;
        parseFormula();
    }

    public boolean isDirectAddressing() {
        return _addressing == NamedBeanAddressing.Direct;
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setValue(double value) {
        _base.assertListenersAreNotRegistered(log, "setEnum");
        _value = value;
    }

    public double getValue() {
        return _value;
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

    public void setMemory(@Nonnull String memoryName) {
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        _memoryHandle = handle;
        InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
        addRemoveVetoListener();
    }

    public void setMemory(@Nonnull Memory memory) {
        setMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeMemory() {
        if (_memoryHandle != null) {
            _memoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }

    public void setListenToMemory(boolean listenToMemory) {
        _listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
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

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

    private void addRemoveVetoListener() {
        if (_memoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    public double evaluateValue(ConditionalNG conditionalNG) throws JmriException {

        if (_addressing == NamedBeanAddressing.Direct) {
            return _value;
        } else {
            Object val;

            SymbolTable symbolNamedBean = conditionalNG.getSymbolTable();

            switch (_addressing) {
                case Reference:
                    val = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _reference);
                    break;

                case LocalVariable:
                    val = symbolNamedBean.getValue(_localVariable);
                    break;

                case Memory:
                    val = _memoryHandle.getBean().getValue();
                    break;

                case Formula:
                    val = _expressionNode != null
                            ? _expressionNode.calculate(conditionalNG.getSymbolTable())
                            : null;
                    break;

                case Table:
                    val = _selectTable.evaluateTableData(conditionalNG);
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
            }

            if (val instanceof String) {
                String validateResult = _formatterParserValidator.validate(val.toString());
                if (validateResult != null) throw new JmriException(validateResult);
                return _formatterParserValidator.parse(val.toString());
            }

            return TypeConversionUtil.convertToDouble(val, false, true, true);
        }
    }

    /**
     * Format the value
     * @param value The value
     * @return speed formatted as %1.3f
     */
    public String formatValue(double value) {
        return String.format(Locale.US, String.format("%%1.%df", _numDecimals), value);
    }

    public String getDescription(Locale locale) {
        String enumName;

        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_addressing) {
            case Direct:
                enumName = Bundle.getMessage(locale, "AddressByDirect", _value);
                break;

            case Reference:
                enumName = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case Memory:
                enumName = Bundle.getMessage(locale, "AddressByMemory_Listen", memoryName, Base.getListenString(_listenToMemory));
                break;

            case LocalVariable:
                enumName = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                enumName = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            case Table:
                enumName = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _selectTable.getTableNameDescription(locale),
                        _selectTable.getTableRowDescription(locale),
                        _selectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing: " + _addressing.name());
        }
        return enumName;
    }

    /**
     * Register listeners if this object needs that.
     */
    public void registerListeners() {
        if (!_listenersAreRegistered
                && (_addressing == NamedBeanAddressing.Memory)
                && (_memoryHandle != null)
                && _listenToMemory) {
            _memoryHandle.getBean().addPropertyChangeListener("value", _listener);
            _listenersAreRegistered = true;
        }
    }

    /**
     * Unregister listeners if this object needs that.
     */
    public void unregisterListeners() {
        if (_listenersAreRegistered
                && (_addressing == NamedBeanAddressing.Memory)
                && (_memoryHandle != null)
                && _listenToMemory) {
            _memoryHandle.getBean().removePropertyChangeListener("value", _listener);
            _listenersAreRegistered = false;
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName()) && _inUse.isInUse()) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                boolean doVeto = false;
                if ((_addressing == NamedBeanAddressing.Memory) && (_memoryHandle != null) && evt.getOldValue().equals(_memoryHandle.getBean())) {
                    doVeto = true;
                }
                if (doVeto) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("MemoryInUseMemoryExpressionVeto", _base.getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(_memoryHandle.getBean())) {
                    removeMemory();
                }
            }
        }
    }


    /**
     * Format, parse and validate.
     */
    public interface FormatterParserValidator {

        /**
         * Get the initial value
         * @return the initial value
         */
        public double getInitialValue();

        /**
         * Format the value
         * @param value the value
         * @return the formatted string
         */
        public String format(double value);

        /**
         * Parse the string
         * @param str the string
         * @return the parsed value
         */
        public double parse(String str);

        /**
         * Validates the string
         * @param str the string
         * @return null if valid. An error message if not valid
         */
        public String validate(String str);
    }


    public static class DefaultFormatterParserValidator
            implements FormatterParserValidator {

        @Override
        public double getInitialValue() {
            return 0;
        }

        @Override
        public String format(double value) {
            return Double.toString(value);
        }

        @Override
        public double parse(String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return getInitialValue();
            }
        }

        @Override
        public String validate(String str) {
            try {
                return null;
            } catch (NumberFormatException e) {
                return Bundle.getMessage("LogixNG_SelectDouble_MustBeValidInteger");
            }
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectDouble.class);
}
