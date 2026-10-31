package jmri.time;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the MainTimeProviderHandler class.
 * @author Daniel Bergqvist (C) 2025
 */
public class MainTimeProviderHandlerTest {

    @Test
    public void testCheckStartValues(){
        assertNotNull(InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler());
//        assertEquals(1.0, idt.getRate().getRate(), "initial rate");
//        assertFalse(idt.isRunning(), "initial running");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initTimeProviderManager();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
