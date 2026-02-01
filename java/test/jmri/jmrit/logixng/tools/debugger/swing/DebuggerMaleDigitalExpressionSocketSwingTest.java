package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DebuggerMaleDigitalExpressionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleDigitalExpressionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleDigitalExpressionSocketSwing t = new DebuggerMaleDigitalExpressionSocketSwing();
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
