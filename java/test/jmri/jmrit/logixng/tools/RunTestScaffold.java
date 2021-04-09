package jmri.jmrit.logixng.tools;

import jmri.JmriException;

/**
 * Interface used in the tests of this package.
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public interface RunTestScaffold {

    public void runTest(String message, boolean expectSuccess) throws JmriException;
    
}
