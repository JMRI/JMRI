package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DebuggerMaleAnalogActionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleAnalogActionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleAnalogActionSocketSwing t = new DebuggerMaleAnalogActionSocketSwing();
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
