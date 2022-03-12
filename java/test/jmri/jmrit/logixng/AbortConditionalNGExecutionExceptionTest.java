package jmri.jmrit.logixng;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test AbortConditionalNGExecutionException
 * 
 * @author Daniel Bergqvist 2021
 */
public class AbortConditionalNGExecutionExceptionTest {

    @Test
    public void testCtor() {
        AbortConditionalNGExecutionException t = new AbortConditionalNGExecutionException();
        Assert.assertNotNull("not null", t);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
