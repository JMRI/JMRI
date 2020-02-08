package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.CanSprogConnectionConfig;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * CanSprogConnectionConfigXmlTest.java
 * 
 * Description: tests for the CanSprogConnectionConfigXml class
 *
 * @author   Andrew Crosland  Copyright (C) 2020
 */
public class CanSprogConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new CanSprogConnectionConfigXml();
        cc = new CanSprogConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
