package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DebuggerMaleDigitalActionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleDigitalActionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleDigitalActionSocketSwing t = new DebuggerMaleDigitalActionSocketSwing();
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
