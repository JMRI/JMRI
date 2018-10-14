package jmri.jmrix.pi.configurexml;

import jmri.util.JUnitUtil;
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
    }

    @Test
    @Ignore("needs mock pi setup")
    public void getInstanceTest() {
    }

}
