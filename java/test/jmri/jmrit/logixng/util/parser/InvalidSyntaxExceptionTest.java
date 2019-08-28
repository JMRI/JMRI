package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.util.parser.InvalidSyntaxException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ParsedExpression
 * 
 * @author Daniel Bergqvist 2019
 */
public class InvalidSyntaxExceptionTest {

    @Test
    public void testException() {
        InvalidSyntaxException t = new InvalidSyntaxException("Syntax error", 10);
        Assert.assertNotNull("not null", t);
        Assert.assertTrue("position is correct", 10 == t.getPosition());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
