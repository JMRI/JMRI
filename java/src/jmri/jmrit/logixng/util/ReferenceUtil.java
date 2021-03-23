package jmri.jmrit.logixng.util;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.*;
import jmri.util.TypeConversionUtil;

/**
 * Utility methods to handle references
 */
public class ReferenceUtil {

    // The methods in this class are protected instead of private to let the
    // test class ReferenceUtilTest access the methods.
    
    /**
     * Checks if the parameter is a reference or not.
     * @param value the string to check
     * @return true if value has a reference. falsw otherwise
     */
    static public boolean isReference(String value) {
        if (value == null) return false;
        // A reference starts with { and ends with }
        return value.startsWith("{")
                && value.endsWith("}")
                && value.length() > 2;
    }
    
    static protected String unescapeString(String value, int startIndex, int endIndex) {
        boolean escaped = false;
        
        StringBuilder sb = new StringBuilder();
        for (int i=startIndex; i < endIndex; i++) {
            if (value.charAt(i) == '\\') escaped = !escaped;
            else escaped = false;
            
            if (! escaped) sb.append(value.charAt(i));
        }
        
        return sb.toString();
    }
    
    /**
     * Get the value.
     * The value ends either with end of string, or with any of the characters
     * comma, left square bracket, right square bracket or right curly bracket.
     * These characters may be escaped and should then be ignored.
     * @param reference the reference
     * @param startIndex where in the string the value starts, since the
     * reference string may contain several references.
     * @param endIndex index of the end of the value. This is an output parameter.
     * @return the value
     */
    static protected String getValue(String reference, int startIndex, IntRef endIndex) {
        boolean escapeFound = false;
        boolean escaped = false;
        int end = startIndex;
        while (end < reference.length()
                && (escaped ||
                    (reference.charAt(end) != ','
                    && reference.charAt(end) != '['
                    && reference.charAt(end) != ']'
                    && reference.charAt(end) != '{'
                    && reference.charAt(end) != '}'))) {
            if (reference.charAt(end) == '\\') {
                escaped = !escaped;
                escapeFound = true;
            } else {
                escaped = false;
            }
            end++;
        }
        endIndex.v = end;
        
        if (startIndex == end) throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        if (escapeFound) return unescapeString(reference, startIndex, end);
        else return reference.substring(startIndex, end);
    }
    
    /**
     * Get the reference or the value.The value ends either with end of string,
     * or with any of the characters comma, left square bracket, right square
     * bracket or right curly bracket.
     * These characters may be escaped and should then be ignored.
     * 
     * @param symbolTable the symbol table
     * @param reference the reference
     * @param startIndex where in the string the value starts, since the
     * reference string may contain several references.
     * @param endIndex index of the end of the value. This is an output parameter.
     * @return the value
     */
    static protected String getReferenceOrValue(SymbolTable symbolTable, String reference, int startIndex, IntRef endIndex) {
        
        // Do we have a new reference?
        if (reference.charAt(startIndex) == '{') {
            return getReference(symbolTable, reference, startIndex, endIndex);
        } else {
            return getValue(reference, startIndex, endIndex);
        }
    }
    
    /**
     * Get the value of a reference
     * @param symbolTable the symbol table
     * @param reference the reference
     * @param startIndex where in the string the reference starts, since the
     * reference string may contain several references.
     * @param endIndex index of the end of the reference. This is an output parameter.
     * @return the value of the reference
     */
    static protected String getReference(
            SymbolTable symbolTable, String reference, int startIndex, IntRef endIndex) {
        
        // A reference must start with the char {
        if (reference.charAt(startIndex) != '{') {
            throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        }
        
        String leftValue;
        String column;
        String row;
        
        startIndex++;
        
        leftValue = getReferenceOrValue(symbolTable, reference, startIndex, endIndex);
        
        if (endIndex.v == reference.length()) {
            throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        }
        
        if ((reference.charAt(endIndex.v) != '}') && (reference.charAt(endIndex.v) != '[')) {
            throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        }
        
        
        endIndex.v++;
        
//        if ((endIndex.v == reference.length()) || (reference.charAt(endIndex.v-1) != '[')) {
        if ((endIndex.v == reference.length()) || (reference.charAt(endIndex.v-1) == '}')) {
            
//            SymbolTable symbolTable =
//                    getCurrentConditionalNG().getSymbolTable();
            
            if ((symbolTable != null) && symbolTable.hasValue(leftValue)) {
                return TypeConversionUtil.convertToString(symbolTable.getValue(leftValue), false);
            }
            MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
            Memory m = memoryManager.getNamedBean(leftValue);
            if (m != null) {
                if (m.getValue() != null) return m.getValue().toString();
                else throw new IllegalArgumentException("Memory '"+leftValue+"' has no value");
            }
            else throw new IllegalArgumentException("Memory '"+leftValue+"' is not found");
        }
        
        // If we are here, we have a table reference. Find out column and row.
        if (reference.charAt(endIndex.v-1) != '[') {
            throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        }
        
        
        // If we are here, we have a table reference. Find out column and row.
        row = getReferenceOrValue(symbolTable, reference, endIndex.v, endIndex);
        
        endIndex.v++;
        
//        if ((endIndex.v+1 == reference.length()
//                && (reference.charAt(endIndex.v-1) == ']')
//                && (reference.charAt(endIndex.v) == '}'))) {
        if ((reference.charAt(endIndex.v-1) == ']')
                && (reference.charAt(endIndex.v) == '}')) {
            
            endIndex.v++;
            
            NamedTableManager tableManager =
                    InstanceManager.getDefault(NamedTableManager.class);
            
            NamedTable table = tableManager.getNamedBean(leftValue);
            if (table != null) {
                Object cell = table.getCell(row);
                return cell != null ? cell.toString() : null;
            } else {
                throw new IllegalArgumentException("Table '"+leftValue+"' is not found");
            }
        }
        
        if (endIndex.v == reference.length() || reference.charAt(endIndex.v-1) != ',') {
//            System.out.format("getReference(%s,%d): %s, %d, length: %d%n",
//                    reference,
//                    startIndex,
//                    reference.substring(startIndex, endIndex.v),
//                    endIndex.v,
//                    reference.length());
            throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        }
        
//        endIndex.v++;
        
        column = getReferenceOrValue(symbolTable, reference, endIndex.v, endIndex);
        if (endIndex.v == reference.length() || reference.charAt(endIndex.v) != ']') {
            throw new IllegalArgumentException("7Reference '"+reference+"' is not a valid reference");
        }
        
        if (((reference.charAt(endIndex.v) == ']')
                && (reference.charAt(endIndex.v+1) == '}'))) {
            
            endIndex.v++;
            
            NamedTableManager tableManager =
                    InstanceManager.getDefault(NamedTableManager.class);
            
            NamedTable table = tableManager.getNamedBean(leftValue);
            if (table != null) {
                Object cell = table.getCell(row,column);
                endIndex.v++;
                return cell != null ? cell.toString() : null;
            } else {
                throw new IllegalArgumentException("Table '"+leftValue+"' is not found");
            }
//            return "Testing 222....";
//            return leftValue[row];
        }
        
        throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        
//        throw new UnsupportedOperationException("Table is not yet supported");
    }
    
    @CheckReturnValue
    @Nonnull
    static public String getReference(SymbolTable symbolTable, String reference) {
        if (!isReference(reference)) {
            throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        }
        
        IntRef endIndex = new IntRef();
        String ref = getReference(symbolTable, reference, 0, endIndex);
        
        if (endIndex.v != reference.length()) {
            throw new IllegalArgumentException("Reference '"+reference+"' is not a valid reference");
        }
        
        return ref;
    }
    
    
    /**
     * Reference to an integer.
     * This class is cheaper to use than AtomicInteger.
     */
    protected static class IntRef {
        public int v;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReferenceUtil.class);
}
