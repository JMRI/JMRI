package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.logixng.GlobalVariableManager;
import jmri.jmrit.logixng.Module.Parameter;
import jmri.jmrit.logixng.Stack;
import jmri.jmrit.logixng.Stack.ValueAndType;
import jmri.jmrit.logixng.SymbolTable;

/**
 * The default implementation of a NamedTable
 *
 * @author Daniel Bergqvist 2020
 */
public class DefaultSymbolTable implements SymbolTable {

    private final SymbolTable _prevSymbolTable;
    private final Stack _stack;
    private final int _firstSymbolIndex;
    private final Map<String, Symbol> _symbols = new HashMap<>();


    /**
     * Create a new instance of DefaultSymbolTable with no previous symbol table.
     */
    public DefaultSymbolTable() {
        _prevSymbolTable = null;
        _stack = new DefaultStack();
        _firstSymbolIndex = _stack.getCount();
    }

    /**
     * Create a new instance of DefaultSymbolTable with previous symbol table
     * and the stack from a ConditionalNG.
     * @param currentConditionalNG the ConditionalNG
     */
    public DefaultSymbolTable(ConditionalNG currentConditionalNG) {
        _prevSymbolTable = currentConditionalNG.getSymbolTable();
        _stack = currentConditionalNG.getStack();
        _firstSymbolIndex = _stack.getCount();
    }

    /**
     * Create a new instance of DefaultSymbolTable from a previous symbol table
     * and a stack.
     * @param prevSymbolTable the previous symbol table
     */
    public DefaultSymbolTable(SymbolTable prevSymbolTable) {
        _prevSymbolTable = null;
        _symbols.putAll(prevSymbolTable.getSymbols());
        _stack = new DefaultStack();
        for (Symbol symbol : _symbols.values()) {
            _stack.setValueAndTypeAtIndex(symbol.getIndex(),
                    prevSymbolTable.getValueAndType(symbol.getName()));

        }
        _firstSymbolIndex = _stack.getCount();
    }

    /**
     * Get the previous symbol table
     * @return the symbol table
     */
    public SymbolTable getPrevSymbolTable() {
        return _prevSymbolTable;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Symbol> getSymbols() {
        return Collections.unmodifiableMap(_symbols);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getSymbolValues() {
        Map<String, Object> symbolValues = new HashMap<>();
        for (Symbol symbol : _symbols.values()) {
            Object value = _stack.getValueAtIndex(_firstSymbolIndex + symbol.getIndex());
            symbolValues.put(symbol.getName(), value);
        }
        return Collections.unmodifiableMap(symbolValues);
    }

    /** {@inheritDoc} */
    @Override
    public Object getValue(String name) {
        Symbol symbol = _symbols.get(name);
        if (symbol != null) {
            return _stack.getValueAtIndex(_firstSymbolIndex + symbol.getIndex());
        }
        GlobalVariable globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).getByUserName(name);
        if (globalVariable != null) {
            return globalVariable.getValue();
        }
        throw new SymbolNotFound(String.format("Symbol '%s' does not exist in symbol table", name));
    }

    /** {@inheritDoc} */
    @Override
    public ValueAndType getValueAndType(String name) {
        Symbol symbol = _symbols.get(name);
        if (symbol != null) {
            return _stack.getValueAndTypeAtIndex(_firstSymbolIndex + symbol.getIndex());
        }
        throw new SymbolNotFound(String.format("Symbol '%s' does not exist in symbol table", name));
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasValue(String name) {
        if (_symbols.containsKey(name)) return true;
        GlobalVariable globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).getByUserName(name);
        return globalVariable != null;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(String name, Object value) {
        if (_symbols.get(name) != null) {
            _stack.setValueAtIndex(_firstSymbolIndex + _symbols.get(name).getIndex(), value);
        } else {
            GlobalVariable globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).getByUserName(name);
            if (globalVariable != null) {
                globalVariable.setValue(value);
            } else {
                throw new IllegalArgumentException(Bundle.getMessage("ExceptionSymbolNotInSymbolTable", name));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void printSymbolTable(java.io.PrintWriter stream) {
        stream.format("printSymbolTable:%n");
        for (Map.Entry<String, Symbol> entry : _symbols.entrySet()) {
            stream.format("Key: %s, Name: %s, Index: %d, Value: %s%n",
                    entry.getKey(),
                    entry.getValue().getName(),
                    entry.getValue().getIndex(),
                    _stack.getValueAtIndex(_firstSymbolIndex + entry.getValue().getIndex()));
        }
        stream.format("printSymbolTable done%n");
    }

    /** {@inheritDoc} */
    @Override
    public void createSymbols(Collection<? extends SymbolTable.VariableData> symbolDefinitions) throws JmriException {
        createSymbols(this, symbolDefinitions);
    }

    /** {@inheritDoc} */
    @Override
    public void createSymbols(SymbolTable symbolTable,
            Collection<? extends SymbolTable.VariableData> symbolDefinitions)
            throws JmriException {

        for (SymbolTable.VariableData variable : symbolDefinitions) {
            Symbol symbol = new DefaultSymbol(variable.getName(), _stack.getCount() - _firstSymbolIndex);

            if (_symbols.containsKey(symbol.getName())) {
                throw new IllegalArgumentException("Symbol table already contains the variable " + symbol.getName());
            }

            InitialValueType initialValueType = variable.getInitialValueType();
            Object initialValue = SymbolTable.getInitialValue(
                    SymbolTable.Type.Local,
                    symbol.getName(),
                    initialValueType,
                    variable.getInitialValueData(),
                    symbolTable,
                    _symbols);

//            System.out.format("Add symbol: %s = %s%n", symbol.getName(), initialValue);

            _stack.push(new ValueAndType(initialValueType, initialValue));
            _symbols.put(symbol.getName(), symbol);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeSymbols(Collection<? extends SymbolTable.VariableData> symbolDefinitions) throws JmriException {
        symbolDefinitions.forEach((parameter) -> {
            _symbols.remove(parameter.getName());
        });
    }

    /** {@inheritDoc} */
    @Override
    public Stack getStack() {
        return _stack;
    }


    public static class DefaultSymbol implements Symbol {

        private final String _name;
        private final int _index;

        public DefaultSymbol(String name, int index) {
            _name = name;
            _index = index;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return _name;
        }

        /** {@inheritDoc} */
        @Override
        public int getIndex() {
            return _index;
        }

    }


    public static class DefaultParameter implements Parameter {

        private String _name;
        private boolean _isInput;
        private boolean _isOutput;

        public DefaultParameter(String name, boolean isInput, boolean isOutput) {
            _name = name;
            _isInput = isInput;
            _isOutput = isOutput;
        }

        public DefaultParameter(Parameter parameter) {
            _name = parameter.getName();
            _isInput = parameter.isInput();
            _isOutput = parameter.isOutput();
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isInput() {
            return _isInput;
        }

        public void setIsInput(boolean value) {
            _isInput = value;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isOutput() {
            return _isOutput;
        }

        public void setIsOutput(boolean value) {
            _isOutput = value;
        }

    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSymbolTable.class);

}
