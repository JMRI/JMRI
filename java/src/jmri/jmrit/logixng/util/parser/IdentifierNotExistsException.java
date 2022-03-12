package jmri.jmrit.logixng.util.parser;

/**
 * The identifier does not exist.
 * 
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class IdentifierNotExistsException extends ParserException {

    private final String _identifierName;
    
    /**
     * Constructs an instance of <code>IdentifierNotExistsException</code> with the specified detail message.
     * @param msg the detail message.
     * @param identifierName the name of the identifier
     */
    public IdentifierNotExistsException(String msg, String identifierName) {
        super(msg);
        _identifierName = identifierName;
    }
    
    public String getIdentifierName() {
        return _identifierName;
    }
    
}
