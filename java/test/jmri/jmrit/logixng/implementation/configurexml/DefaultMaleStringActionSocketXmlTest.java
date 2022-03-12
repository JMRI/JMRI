package jmri.jmrit.logixng.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultMaleStringActionSocketXml
 * 
 * @author Daniel Bergqvist 2021
 */
public class DefaultMaleStringActionSocketXmlTest {

    @Test
    public void testCtor() {
        DefaultMaleStringActionSocketXml t = new DefaultMaleStringActionSocketXml();
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
