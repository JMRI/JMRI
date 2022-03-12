package jmri.jmrit.logixng.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultMaleAnalogActionSocketXml
 * 
 * @author Daniel Bergqvist 2021
 */
public class DefaultMaleAnalogActionSocketXmlTest {

    @Test
    public void testCtor() {
        DefaultMaleAnalogActionSocketXml t = new DefaultMaleAnalogActionSocketXml();
        Assert.assertNotNull("not null", t);
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
