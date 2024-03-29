package jmri.jmrix.lenz.liusbserver.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.jdom2.Element;

import jmri.jmrix.lenz.liusbserver.ConnectionConfig;

/**
 * Tests for the jmri.jmrix.lenz.liusbserver.configurexml.ConnectionConfigXml class.
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

    @Test
    @Override
    public void getInstanceTest() {
       super.getInstanceTest();
       JUnitAppender.assertErrorMessageStartsWith("Error opening network connection to");
       JUnitAppender.assertErrorMessageStartsWith("init (pipe)");
       JUnitAppender.assertErrorMessageStartsWith("Error connecting or configuring port.");
    }

    /**
     * { @inheritdoc }
     */
    @Override
    protected void validateConnectionDetails(jmri.jmrix.ConnectionConfig cc,Element e){
        // LIUSBServer only stores standard connection details.
    }

}
