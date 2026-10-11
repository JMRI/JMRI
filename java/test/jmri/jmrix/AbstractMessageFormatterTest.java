package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Abstract class to test MessageFormatter implementations.
 * @author Steve Young Copyright(C) 2025
 */
public abstract class AbstractMessageFormatterTest {

    protected MessageFormatter formatter;

    @Test
    public void testNotAMessage() {
        Assertions.assertNotNull(formatter);
        Assertions.assertFalse(formatter.handlesMessage(
            new jmri.jmrix.AbstractMessage("Not a Valid message?") {
        }));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        formatter = null;
        JUnitUtil.tearDown();
    }

}
