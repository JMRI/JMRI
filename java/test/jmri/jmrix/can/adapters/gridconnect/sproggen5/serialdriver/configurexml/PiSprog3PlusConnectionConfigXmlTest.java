package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3PlusConnectionConfig;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Sprog3PlusConnectionConfigXmlTest.java
 * 
 * Test for the Sprog3PlusConnectionConfigXml class
 *
 * @author  Andrew Crosland  Copyright (C) 2021
 */
public class PiSprog3PlusConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @Test
    public void testBothConstructors() {
        Assertions.assertNotNull(xmlAdapter, "xmlAdapter exists");
        Assertions.assertNotNull(cc, "cc exists");
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new PiSprog3PlusConnectionConfigXml();
        cc = new PiSprog3PlusConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
