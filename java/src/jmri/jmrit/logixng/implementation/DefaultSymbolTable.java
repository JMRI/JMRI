package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.LogixNG_Manager;
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
    
    
    public DefaultSymbolTable() {
        _prevSymbolTable = InstanceManager.getDefault(LogixNG_Manager.class).getSymbolTable();
        _stack = InstanceManager.getDefault(LogixNG_Manager.class).getStack();
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
    public Object getValue(String name) {
        return _stack.getValueAtIndex(_firstSymbolIndex + _symbols.get(name).getIndex());
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(String name, Object value) {
        _stack.setValueAtIndex(_firstSymbolIndex + _symbols.get(name).getIndex(), value);
    }
    
    /** {@inheritDoc} */
    @Override
    public void printSymbolTable(java.io.PrintStream stream) {
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

        SymbolTable symbolTable =
                InstanceManager.getDefault(LogixNG_Manager.class)
                        .getSymbolTable();

        for (SymbolTable.Symbol symbol : symbolTable.getSymbols().values()) {
            variables.put(symbol.getName(),
                    new LocalVariableExpressionVariable(symbol.getName()));
        }

        return new RecursiveDescentParser(variables);
    }
    
    /** {@inheritDoc} */
    @Override
    public void createSymbols(Collection<? extends SymbolTable.VariableData> symbolDefinitions) throws JmriException {
        for (SymbolTable.VariableData variable : symbolDefinitions) {
            Symbol symbol = new DefaultSymbol(variable.getName(), _stack.getCount() - _firstSymbolIndex);
            Object initialValue = null;
            
            if (_symbols.containsKey(symbol.getName())) {
                throw new IllegalArgumentException("Symbol table already contains the variable " + symbol.getName());
            }
            
            switch (variable.getInitalValueType()) {
                case None:
                    break;
                    
                case LocalVariable:
                    initialValue = _prevSymbolTable.getValue(variable.getInitialValueData());
                    break;
                    
                case Memory:
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(variable.getInitialValueData());
                    if (m != null) initialValue = m.getValue();
                    break;
                    
                case Reference:
                    if (ReferenceUtil.isReference(variable.getInitialValueData())) {
                        initialValue = ReferenceUtil.getReference(variable.getInitialValueData());
                    } else {
                        log.error("\"{}\" is not a reference", variable.getInitialValueData());
                    }
                    break;
                    
                case Formula:
                    RecursiveDescentParser parser = createParser();
                    ExpressionNode expressionNode = parser.parseExpression(variable.getInitialValueData());
                    initialValue = expressionNode.calculate();
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
        
        private final String _name;
        private final boolean _isInput;
        private final boolean _isOutput;
        
        public DefaultParameter(String name, boolean isInput, boolean isOutput) {
            _name = name;
            _isInput = isInput;
            _isOutput = isOutput;
        }
        
        /** {@inheritDoc} */
        @Override
        public String getName() {
            return _name;
        }
        
        /** {@inheritDoc} */
        @Override
        public boolean isInput() {
            return _isInput;
        }
        
        /** {@inheritDoc} */
        @Override
        public boolean isOutput() {
            return _isOutput;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSymbolTable.class);
    
}
