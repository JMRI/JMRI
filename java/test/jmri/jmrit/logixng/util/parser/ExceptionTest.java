package jmri.jmrit.logixng.util.parser;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test exceptions
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExceptionTest {

    @Test
    public void testCalculateException() {
        CalculateException t = new CalculateException("Calculate exception");
        Assert.assertNotNull("not null", t);
    }
    
    @Test
    public void testInvalidSyntaxException() {
        InvalidSyntaxException t = new InvalidSyntaxException("Syntax error");
        Assert.assertNotNull("not null", t);
        Assert.assertTrue("position is correct", -1 == t.getPosition());
        
        t = new InvalidSyntaxException("Syntax error", 10);
        Assert.assertNotNull("not null", t);
        Assert.assertTrue("position is correct", 10 == t.getPosition());
    }
    
    @Test
    public void testFunctionNotExistsException() {
        FunctionNotExistsException t =
                new FunctionNotExistsException("Function does not exists", "MyFunc");
        Assert.assertNotNull("not null", t);
        Assert.assertTrue("strings matches", "MyFunc".equals(t.getFunctionName()));
    }
    
    @Test
    public void testIdentifierNotExistsException() {
        IdentifierNotExistsException t =
                new IdentifierNotExistsException("Identifier does not exists", "MyIdentifier");
        Assert.assertNotNull("not null", t);
        Assert.assertTrue("strings matches", "MyIdentifier".equals(t.getIdentifierName()));
    }
    
    @Test
    public void testParserException() {
        ParserException t = new ParserException();
        Assert.assertNotNull("not null", t);
        
        t = new ParserException("Parser exception");
        Assert.assertNotNull("not null", t);
    }
    
    @Test
    public void testWrongNumberOfParametersException() {
        WrongNumberOfParametersException t =
                new WrongNumberOfParametersException("Wrong number of parameters");
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
