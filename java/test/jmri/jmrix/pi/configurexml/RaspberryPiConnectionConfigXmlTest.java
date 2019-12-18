package jmri.jmrix.pi.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * Tests for the RaspberryPiConnectionConfigXml class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new RaspberryPiConnectionConfigXml();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

    @Test
    @Ignore("needs mock pi setup")
    @ToDo("provide mock raspberry pi implementation so code can be tested using parent class test")
    @Override
    public void getInstanceTest() {
        Assert.fail("test needs more setup");
    }

}
