package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test exceptions
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExceptionTest {

    @Test
    public void testCalculateException() {
        CalculateException t = new CalculateException("Calculate exception");
        assertNotNull( t, "not null");
    }
    
    @Test
    public void testInvalidSyntaxException() {
        InvalidSyntaxException t = new InvalidSyntaxException("Syntax error");
        assertNotNull( t, "not null");
        assertEquals( -1, t.getPosition(), "position is correct");
        
        t = new InvalidSyntaxException("Syntax error", 10);
        assertNotNull( t, "not null");
        assertEquals( 10, t.getPosition(), "position is correct");
    }
    
    @Test
    public void testFunctionNotExistsException() {
        FunctionNotExistsException t =
                new FunctionNotExistsException("Function does not exists", "MyFunc");
        assertNotNull( t, "not null");
        assertEquals( "MyFunc", t.getFunctionName(), "strings matches");
    }
    
    @Test
    public void testIdentifierNotExistsException() {
        IdentifierNotExistsException t =
                new IdentifierNotExistsException("Identifier does not exists", "MyIdentifier");
        assertNotNull( t, "not null");
        assertEquals( "MyIdentifier", t.getIdentifierName(), "strings matches");
    }
    
    @Test
    public void testParserException() {
        ParserException t = new ParserException();
        assertNotNull( t, "not null");
        
        t = new ParserException("Parser exception");
        assertNotNull( t, "not null");
    }
    
    @Test
    public void testWrongNumberOfParametersException() {
        WrongNumberOfParametersException t =
                new WrongNumberOfParametersException("Wrong number of parameters");
        assertNotNull( t, "not null");
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
