package jmri.jmrit.logixng.implementation;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.Stack;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.expressionnode.ExpressionNode;
import jmri.jmrit.logixng.util.parser.variables.LocalVariableExpressionVariable;

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
        System.out.format("DefaultSymbolTable: _firstSymbolIndex: %d%n", _firstSymbolIndex);
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
//        System.out.format("DefaultSymbolTable.getValue: stack: %s, name: %s%n", _stack, name);
//        System.out.format("DefaultSymbolTable.getValue: stack: %s, name: %s, _symbols.get(): %s%n", _stack, name, _symbols.get(name));
        return _stack.getValueAtIndex(_firstSymbolIndex + _symbols.get(name).getIndex());
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(String name, Object value) {
        _stack.setValueAtIndex(_firstSymbolIndex + _symbols.get(name).getIndex(), value);
    }
    
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
    
    /**
     * Create the symbols
     * @param symbolDefinitions list of symbols to create
     * @throws jmri.JmriException if an exception occurs
     */
    public void createSymbols(Collection<SymbolTable.ParameterData> symbolDefinitions) throws JmriException {
        for (SymbolTable.ParameterData parameter : symbolDefinitions) {
            Symbol symbol = new DefaultSymbol(parameter.getName(), _stack.getCount());
            Object initialValue = null;
            
//            System.out.format("createSymbols: Add symbol: %s, %s, %s%n", symbol.getName(), parameter.getInitalValueType().name(), parameter.getInitialValueData());
            
            switch (parameter.getInitalValueType()) {
                case None:
                    break;
                    
                case LocalVariable:
//                    System.out.format("AAAA createSymbols: %s, %s%n", parameter.getName(), parameter.getInitialValueData());
                    initialValue = _prevSymbolTable.getValue(parameter.getInitialValueData());
                    break;
                    
                case Memory:
                    Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(parameter.getInitialValueData());
                    if (m != null) initialValue = m.getValue();
                    break;
                    
                case Reference:
                    if (ReferenceUtil.isReference(parameter.getInitialValueData())) {
                        initialValue = ReferenceUtil.getReference(parameter.getInitialValueData());
                    } else {
                        log.error("\"{}\" is not a reference", parameter.getInitialValueData());
                    }
                    break;
                    
                case Formula:
                    RecursiveDescentParser parser = createParser();
                    ExpressionNode expressionNode = parser.parseExpression(parameter.getInitialValueData());
//                    System.out.format("Expression calculate%n");
                    initialValue = expressionNode.calculate();
//                    System.out.format("Expression calculate done%n");
                    break;
                    
                default:
                    System.out.format("definition._initalValueType has invalid value: %s%n", parameter.getInitalValueType().name());
                    throw new IllegalArgumentException("definition._initalValueType has invalid value: " + parameter.getInitalValueType().name());
            }
            
//            System.out.format("Add symbol: %s = %s%n", symbol.getName(), initialValue);
            
            _stack.push(initialValue);
            _symbols.put(symbol.getName(), symbol);
        }
    }
    
    
/*    
    public static class SymbolDefinition {
        
        public String _name;
        
        public InitialValueType _initalValueType;
        
        public String _initialValueData;
        
    }
*/    
    
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
    
    
    public static class DefaultParameter implements SymbolTable.Parameter {
        
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
    
    
    public static class DefaultParameterData implements SymbolTable.ParameterData {
        
        public String _name;
        public InitialValueType _initalValueType;
        public String _initialValueData;
        
        public DefaultParameterData(String name, InitialValueType initalValueType, String initialValueData) {
            _name = name;
            _initalValueType = initalValueType;
            _initialValueData = initialValueData;
        }
        
        /** {@inheritDoc} */
        @Override
        public String getName() {
            return _name;
        }
        
        /** {@inheritDoc} */
        @Override
        public InitialValueType getInitalValueType() {
            return _initalValueType;
        }
        
        /** {@inheritDoc} */
        @Override
        public String getInitialValueData() {
            return _initialValueData;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSymbolTable.class);
    
}
