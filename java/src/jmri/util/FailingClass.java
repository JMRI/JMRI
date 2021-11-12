package jmri.util;

/**
 * This class always fails.
 * @author Daniel Bergqvist (c) 2021
 */
public class FailingClass {
    
    public FailingClass() {
        throw new RuntimeException("This class always fails");
    }
}
