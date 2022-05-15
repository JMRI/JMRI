package jmri.jmrit.logixng;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import jmri.*;
import jmri.JmriException;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.LocalVariableExpressionVariable;
import jmri.script.JmriScriptEngineManager;

import org.slf4j.Logger;

/**
 * A symbol table
 *
 * @author Daniel Bergqvist 2020
 */
public interface SymbolTable {

    /**
     * The list of symbols in the table
     * @return the symbols
     */
    public Map<String, Symbol> getSymbols();

    /**
     * The list of symbols and their values in the table
     * @return the name of the symbols and their values
     */
    public Map<String, Object> getSymbolValues();

    /**
     * Get the value of a symbol
     * @param name the name
     * @return the value
     */
    public Object getValue(String name);

    /**
     * Is the symbol in the symbol table?
     * @param name the name
     * @return true if the symbol exists, false otherwise
     */
    public boolean hasValue(String name);

    /**
     * Set the value of a symbol
     * @param name the name
     * @param value the value
     */
    public void setValue(String name, Object value);

    /**
     * Add new symbols to the symbol table
     * @param symbolDefinitions the definitions of the new symbols
     * @throws JmriException if an exception is thrown
     */
    public void createSymbols(Collection<? extends VariableData> symbolDefinitions)
            throws JmriException;

    /**
     * Add new symbols to the symbol table.
     * This method is used for parameters, when new symbols might be created
     * that uses symbols from a previous symbol table.
     *
     * @param symbolTable the symbol table to get existing symbols from
     * @param symbolDefinitions the definitions of the new symbols
     * @throws JmriException if an exception is thrown
     */
    public void createSymbols(
            SymbolTable symbolTable,
            Collection<? extends VariableData> symbolDefinitions)
            throws JmriException;

    /**
     * Removes symbols from the symbol table
     * @param symbolDefinitions the definitions of the symbols to be removed
     * @throws JmriException if an exception is thrown
     */
    public void removeSymbols(Collection<? extends VariableData> symbolDefinitions)
            throws JmriException;

    /**
     * Print the symbol table on a stream
     * @param stream the stream
     */
    public void printSymbolTable(java.io.PrintWriter stream);

    /**
     * Validates the name of a symbol
     * @param name the name
     * @return true if the name is valid, false otherwise
     */
    public static boolean validateName(String name) {
        if (name.isEmpty()) return false;
        if (!Character.isLetter(name.charAt(0))) return false;
        for (int i=0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i)) && (name.charAt(i) != '_')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the stack.
     * This method is only used internally by DefaultSymbolTable.
     *
     * @return the stack
     */
    public Stack getStack();


    /**
     * An enum that defines the types of initial value.
     */
    public enum InitialValueType {

        None(Bundle.getMessage("InitialValueType_None"), true),
        Integer(Bundle.getMessage("InitialValueType_Integer"), true),
        FloatingNumber(Bundle.getMessage("InitialValueType_FloatingNumber"), true),
        String(Bundle.getMessage("InitialValueType_String"), true),
        Array(Bundle.getMessage("InitialValueType_Array"), false),
        Map(Bundle.getMessage("InitialValueType_Map"), false),
        LocalVariable(Bundle.getMessage("InitialValueType_LocalVariable"), true),
        Memory(Bundle.getMessage("InitialValueType_Memory"), true),
        Reference(Bundle.getMessage("InitialValueType_Reference"), true),
        Formula(Bundle.getMessage("InitialValueType_Formula"), true),
        ScriptExpression(Bundle.getMessage("InitialValueType_ScriptExpression"), true),
        ScriptFile(Bundle.getMessage("InitialValueType_ScriptFile"), true),
        LogixNG_Table(Bundle.getMessage("InitialValueType_LogixNGTable"), true);

        private final String _descr;
        private final boolean _isValidAsParameter;

        private InitialValueType(String descr, boolean isValidAsParameter) {
            _descr = descr;
            _isValidAsParameter = isValidAsParameter;
        }

        @Override
        public String toString() {
            return _descr;
        }

        public boolean isValidAsParameter() {
            return _isValidAsParameter;
        }
    }


    /**
     * The definition of the symbol
     */
    public interface Symbol {

        /**
         * The name of the symbol
         * @return the name
         */
        public String getName();

        /**
         * The index on the stack for this symbol
         * @return the index
         */
        public int getIndex();

    }


    /**
     * Data for a variable.
     */
    public static class VariableData {

        public String _name;
        public InitialValueType _initialValueType = InitialValueType.None;
        public String _initialValueData;

        public VariableData(
                String name,
                InitialValueType initialValueType,
                String initialValueData) {

            _name = name;
            if (initialValueType != null) {
                _initialValueType = initialValueType;
            }
            _initialValueData = initialValueData;
        }

        public VariableData(VariableData variableData) {
            _name = variableData._name;
            _initialValueType = variableData._initialValueType;
            _initialValueData = variableData._initialValueData;
        }

        /**
         * The name of the variable
         * @return the name
         */
        public String getName() {
            return _name;
        }

        public InitialValueType getInitialValueType() {
            return _initialValueType;
        }

        public String getInitialValueData() {
            return _initialValueData;
        }

    }

    /**
     * Print a variable
     * @param log          the logger
     * @param pad          the padding
     * @param name         the name
     * @param value        the value
     * @param expandArraysAndMaps   true if arrays and maps should be expanded, false otherwise
     * @param headerName   header for the variable name
     * @param headerValue  header for the variable value
     */
    @SuppressWarnings("unchecked")  // Checked cast is not possible due to type erasure
    @SuppressFBWarnings(value="SLF4J_SIGN_ONLY_FORMAT", justification="The code prints a complex variable, like a map, to the log")
    public static void printVariable(
            Logger log,
            String pad,
            String name,
            Object value,
            boolean expandArraysAndMaps,
            String headerName,
            String headerValue) {

        if (expandArraysAndMaps && (value instanceof Map)) {
            log.warn("{}{}: {},", pad, headerName, name);
            var map = ((Map<? extends Object, ? extends Object>)value);
            for (var entry : map.entrySet()) {
                log.warn("{}{}{} -> {},", pad, pad, entry.getKey(), entry.getValue());
            }
        } else if (expandArraysAndMaps && (value instanceof List)) {
            log.warn("{}{}: {},", pad, headerName, name);
            var list = ((List<? extends Object>)value);
            for (int i=0; i < list.size(); i++) {
                log.warn("{}{}{}: {},", pad, pad, i, list.get(i));
            }
        } else  {
            log.warn("{}{}: {}, {}: {}", pad, headerName, name, headerValue, value);
        }
    }

    private static Object runScriptExpression(String initialData) {
        String script =
                "import jmri\n" +
                "variable.set(" + initialData + ")";

        JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

        Bindings bindings = new SimpleBindings();
        LogixNG_ScriptBindings.addScriptBindings(bindings);

        var variable = new Reference<Object>();
        bindings.put("variable", variable);

        try {
            String theScript = String.format("import jmri%n") + script;
            scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                    .eval(theScript, bindings);
        } catch (ScriptException e) {
            log.warn("cannot execute script", e);
            return null;
        }
        return variable.get();
    }

    private static Object runScriptFile(String initialData) {

        JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

        Bindings bindings = new SimpleBindings();
        LogixNG_ScriptBindings.addScriptBindings(bindings);

        var variable = new Reference<Object>();
        bindings.put("variable", variable);

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(jmri.util.FileUtil.getExternalFilename(initialData)),
                StandardCharsets.UTF_8)) {
            scriptEngineManager.getEngineByName(JmriScriptEngineManager.JYTHON)
                    .eval(reader, bindings);
        } catch (IOException | ScriptException e) {
            log.warn("cannot execute script \"{}\"", initialData, e);
            return null;
        }
        return variable.get();
    }

    private static Object copyLogixNG_Table(String initialData) {

        NamedTable myTable = InstanceManager.getDefault(NamedTableManager.class)
                .getNamedTable(initialData);

        var myMap = new java.util.concurrent.ConcurrentHashMap<Object, Map<Object, Object>>();

        for (int row=1; row <= myTable.numRows(); row++) {
            Object rowKey = myTable.getCell(row, 0);
            var rowMap = new java.util.concurrent.ConcurrentHashMap<Object, Object>();

            for (int col=1; col <= myTable.numColumns(); col++) {
                var columnKey = myTable.getCell(0, col);
                var cellValue = myTable.getCell(row, col);
                rowMap.put(columnKey, cellValue);
            }

            myMap.put(rowKey, rowMap);
        }

        return myMap;
    }

    public static Object getInitialValue(
            InitialValueType initialType,
            String initialData,
            SymbolTable symbolTable,
            Map<String, Symbol> symbols)
            throws ParserException, JmriException {

        switch (initialType) {
            case None:
                return null;

            case Integer:
                return Long.parseLong(initialData);

            case FloatingNumber:
                return Double.parseDouble(initialData);

            case String:
                return initialData;

            case Array:
                List<Object> array = new java.util.ArrayList<>();
                Object initialValue = array;
                String initialValueData = initialData;
                if ((initialValueData != null) && !initialValueData.isEmpty()) {
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
                return initialValue;

            case Map:
                return new java.util.HashMap<>();

            case LocalVariable:
                return symbolTable.getValue(initialData);

            case Memory:
                Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(initialData);
                if (m != null) return m.getValue();
                else return null;

            case Reference:
                if (ReferenceUtil.isReference(initialData)) {
                    return ReferenceUtil.getReference(
                            symbolTable, initialData);
                } else {
                    log.error("\"{}\" is not a reference", initialData);
                    return null;
                }

            case Formula:
                RecursiveDescentParser parser = createParser(symbols);
                ExpressionNode expressionNode = parser.parseExpression(
                        initialData);
                return expressionNode.calculate(symbolTable);

            case ScriptExpression:
                return runScriptExpression(initialData);

            case ScriptFile:
                return runScriptFile(initialData);

            case LogixNG_Table:
                return copyLogixNG_Table(initialData);

            default:
                log.error("definition._initialValueType has invalid value: {}", initialType.name());
                throw new IllegalArgumentException("definition._initialValueType has invalid value: " + initialType.name());
        }
    }

    private static RecursiveDescentParser createParser(Map<String, Symbol> symbols)
            throws ParserException {
        Map<String, Variable> variables = new HashMap<>();

        for (SymbolTable.Symbol symbol : Collections.unmodifiableMap(symbols).values()) {
            variables.put(symbol.getName(),
                    new LocalVariableExpressionVariable(symbol.getName()));
        }

        return new RecursiveDescentParser(variables);
    }



    public static class SymbolNotFound extends IllegalArgumentException {

        public SymbolNotFound(String message) {
            super(message);
        }
    }


    @SuppressFBWarnings(value="SLF4J_LOGGER_SHOULD_BE_PRIVATE", justification="Interfaces cannot have private fields")
    final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SymbolTable.class);

}
