package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.CanisbConnectionConfig;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * CanisbConnectionConfigXmlTest.java
 * 
 * Test for the CanisbConnectionConfigXml class
 *
 * @author  Andrew Crosland  Copyright (C) 2020
 */
public class CanisbConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @Test
    public void testCTor() {
        CanisbConnectionConfigXml c = new CanisbConnectionConfigXml();
        Assert.assertNotNull("exists",c);
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new CanisbConnectionConfigXml();
        cc = new CanisbConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
