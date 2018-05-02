package jmri.jmrix.anyma.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AnymaDMX_ConnectionConfigXmlTest.java
 * <p>
 * Description: tests for the AnymaDMX_ConnectionConfigXml class
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_ConnectionConfigXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("AnymaDMX_ConnectionConfigXml constructor", new AnymaDMX_ConnectionConfigXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
