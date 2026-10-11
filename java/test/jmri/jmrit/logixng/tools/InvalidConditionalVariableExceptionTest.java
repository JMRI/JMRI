package jmri.jmrit.logixng.tools;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2020
 */
public class InvalidConditionalVariableExceptionTest {

    @Test
    public void testCtor() {
        Assertions.assertNotNull(new InvalidConditionalVariableException());
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
