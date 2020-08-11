package jmri.jmrix.anyma.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * UsbLightManagerXmlTest.java
 * <p>
 * Test for the UsbLightManagerXml class
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class UsbLightManagerXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("UsbLightManagerXml constructor", new UsbLightManagerXml());
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
