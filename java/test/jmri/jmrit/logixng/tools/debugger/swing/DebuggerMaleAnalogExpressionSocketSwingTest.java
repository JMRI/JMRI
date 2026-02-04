package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DebuggerMaleAnalogExpressionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleAnalogExpressionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleAnalogExpressionSocketSwing t = new DebuggerMaleAnalogExpressionSocketSwing();
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
