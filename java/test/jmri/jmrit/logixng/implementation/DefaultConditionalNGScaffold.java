package jmri.jmrit.logixng.implementation;

/**
 * A ConditionalNG class that always has a symbol table.
 * 
 * @author Daniel Bergqvist 2020
 */
public class DefaultConditionalNGScaffold extends DefaultConditionalNG {
    
    public DefaultConditionalNGScaffold(String sys, String user) throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setSymbolTable(new DefaultSymbolTable(this));
    }
    
}
