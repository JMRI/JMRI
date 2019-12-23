package jmri.jmrit.logixng.util.parser;

/**
 * A variable
 */
public interface Variable {

    public String getName();
    
    public Object getValue() throws Exception;
    
}
