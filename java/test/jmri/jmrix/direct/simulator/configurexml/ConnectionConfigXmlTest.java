package jmri.jmrix.direct.simulator.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the ConnectionConfigXml class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
    }
}
