package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DebuggerMaleDigitalBooleanActionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleDigitalBooleanActionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleDigitalBooleanActionSocketSwing t = new DebuggerMaleDigitalBooleanActionSocketSwing();
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
