package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3ConnectionConfig;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PiSprog3ConnectionConfigXmlTest.java
 * 
 * Test for the PiSprog3ConnectionConfigXml class
 *
 * @author  Andrew Crosland  Copyright (C) 2020
 */
public class PiSprog3ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @Test
    public void testCTor() {
        PiSprog3ConnectionConfigXml c = new PiSprog3ConnectionConfigXml();
        Assert.assertNotNull("exists",c);
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new PiSprog3ConnectionConfigXml();
        cc = new PiSprog3ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
