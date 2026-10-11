package jmri.jmrix.marklin.simulation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
 
/**
 * Tests for the MarklinSimConnectionConfigXml class.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new MarklinSimConnectionConfigXml();
        cc = new jmri.jmrix.marklin.simulation.MarklinSimConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

}
