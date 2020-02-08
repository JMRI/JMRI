package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.CanisbConnectionConfig;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * CanisbConnectionConfigXmlTest.java
 * 
 * Description: tests for the CanisbConnectionConfigXml class
 *
 * @author  Andrew Crosland  Copyright (C) 2020
 */
public class CanisbConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new CanisbConnectionConfigXml();
        cc = new CanisbConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
