package jmri.jmrit.logixng.util.parser;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test ExpressionNodeString
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExpressionNodeStringTest {

    @Test
    public void testCtor() {
        ExpressionNodeString t = new ExpressionNodeString(null);
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
