package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Sprog3PlusConnectionConfig;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Sprog3PlusConnectionConfigXmlTest.java
 * 
 * Description: tests for the Sprog3PlusConnectionConfigXml class
 *
 * @author  Andrew Crosland  Copyright (C) 2020
 */
public class Sprog3PlusConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new Sprog3PlusConnectionConfigXml();
        cc = new Sprog3PlusConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
