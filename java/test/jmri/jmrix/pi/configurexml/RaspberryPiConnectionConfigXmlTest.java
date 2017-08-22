package jmri.jmrix.pi.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConnectionConfigXmlTest.java
 * <p>
 * Description: tests for the RaspberryPiConnectionConfigXml class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiConnectionConfigXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("RaspberryPiConnectionConfigXml constructor", new RaspberryPiConnectionConfigXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
