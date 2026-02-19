package jmri.time.implementation;

import java.time.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for InternalDateTime class.
 *
 * @author Bob Jacobsen (c) 2025
 **/
public class InternalDateTimeTest {

    InternalDateTime idt;
    LocalDateTime time;

    @Test
    public void testCheckStartValues(){
        assertEquals(1.0, idt.getRate().getRate(), "initial rate");
        assertFalse(idt.isRunning(), "initial running");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initTimeProviderManager();

        idt = new InternalDateTime("IU1");
        idt.init();
        idt.lockFromRunning();
        time = idt.getTime();
    }

    @AfterEach
    public void tearDown(){
        time = null;
        JUnitUtil.tearDown();
    }

}
