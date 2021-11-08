package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.Module.Parameter;
import jmri.jmrit.logixng.Stack;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.LocalVariableExpressionVariable;

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
        for (int i=0; i < prevSymbolTable.getStack().getCount(); i++) {
            _stack.setValueAtIndex(i, prevSymbolTable.getStack().getValueAtIndex(i));
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
        if (symbol == null) {
            throw new SymbolNotFound(String.format("Symbol '%s' does not exist in symbol table", name));
        }
        return _stack.getValueAtIndex(_firstSymbolIndex + symbol.getIndex());
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean hasValue(String name) {
        return _symbols.containsKey(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(String name, Object value) {
        if (_symbols.get(name) == null) throw new IllegalArgumentException("The symbol "+name+" does not exist in the symbol table");
        _stack.setValueAtIndex(_firstSymbolIndex + _symbols.get(name).getIndex(), value);
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
    
    private RecursiveDescentParser createParser() throws ParserException {
        Map<String, Variable> variables = new HashMap<>();
        
        for (SymbolTable.Symbol symbol : getSymbols().values()) {
            variables.put(symbol.getName(),
                    new LocalVariableExpressionVariable(symbol.getName()));
        }
        
        return new RecursiveDescentParser(variables);
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
            Object initialValue = null;
            
            if (_symbols.containsKey(symbol.getName())) {
                throw new IllegalArgumentException("Symbol table already contains the variable " + symbol.getName());
            }
            
            switch (variable.getInitalValueType()) {
                case None:
                    break;
                    
                case Integer:
                    initialValue = Long.parseLong(variable.getInitialValueData());
                    break;
                    
                case FloatingNumber:
                    initialValue = Double.parseDouble(variable.getInitialValueData());
                    break;
                    
                case String:
                    initialValue = variable.getInitialValueData();
                    break;
                    
                case LocalVariable:
                    initialValue = symbolTable.getValue(variable.getInitialValueData());
//                    initialValue = _prevSymbolTable.getValue(variable.getInitialValueData());
                    break;
                    
                case Memory:
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(variable.getInitialValueData());
                    if (m != null) initialValue = m.getValue();
                    break;
                    
                case Reference:
                    if (ReferenceUtil.isReference(variable.getInitialValueData())) {
                        initialValue = ReferenceUtil.getReference(
                                symbolTable, variable.getInitialValueData());
                    } else {
                        log.error("\"{}\" is not a reference", variable.getInitialValueData());
                    }
                    break;
                    
                case Formula:
                    RecursiveDescentParser parser = createParser();
                    ExpressionNode expressionNode = parser.parseExpression(
                            variable.getInitialValueData());
                    initialValue = expressionNode.calculate(symbolTable);
                    break;
                    
                default:
                    log.error("definition._initalValueType has invalid value: {}", variable.getInitalValueType().name());
                    throw new IllegalArgumentException("definition._initalValueType has invalid value: " + variable.getInitalValueType().name());
            }
            
//            System.out.format("Add symbol: %s = %s%n", symbol.getName(), initialValue);
            
            _stack.push(initialValue);
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSymbolTable.class);
    
}
