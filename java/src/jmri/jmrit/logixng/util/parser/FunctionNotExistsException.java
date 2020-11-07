package jmri.jmrit.logixng.util.parser;

/**
 *
 */
public class FunctionNotExistsException extends ParserException {

    private final String _functionName;
    
    /**
     * Constructs an instance of <code>FunctionNotExistsException</code> with the specified detail message.
     * @param msg the detail message.
     * @param functionName the name of the function
     */
    public FunctionNotExistsException(String msg, String functionName) {
        super(msg);
        _functionName = functionName;
    }
    
    public String getFunctionName() {
        return _functionName;
    }
    
}
