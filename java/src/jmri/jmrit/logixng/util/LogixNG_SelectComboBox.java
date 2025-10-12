package jmri.jmrit.logixng.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Select an combo box value for LogixNG actions and expressions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LogixNG_SelectComboBox implements VetoableChangeListener {

    /**
     * An item in the combo box.
     */
    public interface Item {

        /**
         * Return the key that's used to store the item in the tables and
         * panels file, as well as when indirect addessing is used.
         * @return the key
         */
        String getKey();

        /**
         * Return the value that's used to show the item in the ComboBox.
         * @return the value
         */
        @Override
        String toString();
    }

    private final AbstractBase _base;
    private final InUse _inUse;
    private Item[] _valuesArray;
    private final LogixNG_SelectTable _selectTable;
    private final PropertyChangeListener _listener;
    private boolean _listenToMemory;
    private boolean _listenersAreRegistered;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private Item _value;
    private String _reference = "";
    private NamedBeanHandle<Memory> _memoryHandle;
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;


    public LogixNG_SelectComboBox(AbstractBase base, Item[] valuesArray,
            Item initialValue, PropertyChangeListener listener) {
        _base = base;
        _inUse = () -> true;
        _valuesArray = valuesArray;
        _value = initialValue;
        _selectTable = new LogixNG_SelectTable(_base, _inUse);
        _listener = listener;
    }


    public void copy(LogixNG_SelectComboBox copy) throws ParserException {
        copy.setAddressing(_addressing);
        copy.setValue(_value);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setMemory(_memoryHandle);
        copy.setListenToMemory(_listenToMemory);
        copy.setFormula(_formula);
        _selectTable.copy(copy._selectTable);
    }

    public void setValues(Item[] valuesArray) {
        String key = _value != null ? _value.getKey() : null;
        _valuesArray = valuesArray;

        // Check if the selected value is in the array
        boolean found = false;
        for (Item value : _valuesArray) {
            if (value.getKey().equals(key)) {
                found = true;
            }
        }
        if (!found) {
            if (_valuesArray.length > 0) {
                _value = _valuesArray[0];
            } else {
                _value = null;
            }
        }
    }

    public Item[] getValues() {
        return _valuesArray;
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

    public void setValue(Item value) {
        _base.assertListenersAreNotRegistered(log, "setString");
        _value = value;
    }

    public void setValue(String key) {
        _base.assertListenersAreNotRegistered(log, "setString");
        if (key == null) {
            _value = null;
            return;
        }
        for (Item v : _valuesArray) {
            if (v.getKey().equals(key)) {
                _value = v;
                return;
            }
        }
        throw new IllegalArgumentException("Key " + key + " is not in list");
    }

    @CheckForNull
    public Item getValue() {
        return _value;
    }

    public Item getValueByKey(String key) {
        for (Item value : _valuesArray) {
            if (value.getKey().equals(key)) return value;
        }
        return null;
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

    public Item evaluateValue(ConditionalNG conditionalNG) throws JmriException {

        if (_addressing == NamedBeanAddressing.Direct) {
            return _value;
        } else {
            String key;

            switch (_addressing) {
                case Reference:
                    key = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _reference);
                    break;

                case Memory:
                    key = TypeConversionUtil
                            .convertToString(_memoryHandle.getBean().getValue(), false);
                    break;

                case LocalVariable:
                    SymbolTable symbolNamedBean = conditionalNG.getSymbolTable();
                    key = TypeConversionUtil
                            .convertToString(symbolNamedBean.getValue(_localVariable), false);
                    break;

                case Formula:
                    key = _expressionNode  != null
                            ? TypeConversionUtil.convertToString(
                                    _expressionNode.calculate(
                                            conditionalNG.getSymbolTable()), false)
                            : null;
                    break;

                case Table:
                    key = TypeConversionUtil.convertToString(
                            _selectTable.evaluateTableData(conditionalNG), false);
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
            }

            return LogixNG_SelectComboBox.this.getValueByKey(key);
        }
    }

    public String getDescription(Locale locale) {
        String description;

        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_addressing) {
            case Direct:
                description = Bundle.getMessage(locale, "AddressByDirect", _value);
                break;

            case Reference:
                description = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case Memory:
                description = Bundle.getMessage(locale, "AddressByMemory_Listen", memoryName, Base.getListenString(_listenToMemory));
                break;

            case LocalVariable:
                description = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                description = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            case Table:
                description = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _selectTable.getTableNameDescription(locale),
                        _selectTable.getTableRowDescription(locale),
                        _selectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing: " + _addressing.name());
        }
        return description;
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectComboBox.class);
}
