package jmri.jmrit.logixng.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DefaultMaleAnalogActionSocketXml
 * 
 * @author Daniel Bergqvist 2021
 */
public class DefaultMaleAnalogActionSocketXmlTest {

    @Test
    public void testCtor() {
        DefaultMaleAnalogActionSocketXml t = new DefaultMaleAnalogActionSocketXml();
        Assertions.assertNotNull( t, "not null");
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
