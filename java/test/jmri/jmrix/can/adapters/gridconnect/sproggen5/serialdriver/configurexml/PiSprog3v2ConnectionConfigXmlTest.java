package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3v2ConnectionConfig;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Sprog3PlusConnectionConfigXmlTest.java
 * 
 * Test for the Sprog3PlusConnectionConfigXml class
 *
 * @author  Andrew Crosland  Copyright (C) 2021
 */
public class PiSprog3v2ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @Test
    public void testBothConstructors() {
        Assertions.assertNotNull(xmlAdapter, "xmlAdapter exists");
        Assertions.assertNotNull(cc, "cc exists");
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new PiSprog3v2ConnectionConfigXml();
        cc = new PiSprog3v2ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
