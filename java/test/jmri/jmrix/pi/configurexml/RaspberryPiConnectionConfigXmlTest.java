package jmri.jmrix.pi.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * ConnectionConfigXmlTest.java
 * <p>
 * Description: tests for the RaspberryPiConnectionConfigXml class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new RaspberryPiConnectionConfigXml();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

    @Test
    @Ignore("needs mock pi setup")
    @ToDo("provide mock raspberry pi implementation so code can be tested using parent class test")
    public void getInstanceTest() {
        Assert.fail("test needs more setup");
    }

}
