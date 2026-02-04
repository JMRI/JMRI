package jmri.jmrit.logixng.util.parser;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test ParsedExpression
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExpressionNodeNumberTest {

    @Test
    public void testCtor() {
        Token token = new Token(TokenType.NONE, "13.22", 0);
        ExpressionNodeFloatingNumber t = new ExpressionNodeFloatingNumber(token);
        Assertions.assertNotNull( t, "not null");
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
