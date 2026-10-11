package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DebuggerMaleStringActionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleStringActionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleStringActionSocketSwing t = new DebuggerMaleStringActionSocketSwing();
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
