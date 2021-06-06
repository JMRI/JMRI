package jmri.jmrit.logixng;

/**
 * A class used when a test class needs a fake parent.
 * 
 * @author Daniel Bergqvist 2021
 */
public class FakeParent extends jmri.jmrit.logixng.implementation.DefaultLogixNG {
    
    public FakeParent() {
        super("IQ1", null);
    }

    @Override
    public final int getChildCount() {
        return 0;
    }
    
}
