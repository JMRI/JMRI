package jmri.jmrix.anyma.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * AnymaDMX_ConnectionConfigXmlTest.java
 * <p>
 * Test for the AnymaDMX_ConnectionConfigXml class
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractUsbConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new AnymaDMX_ConnectionConfigXml();
        /* setting up the adapter through getInstance() makes calls to libusb
           which doesn't work right on CI servers.
           commenting out the creation of cc causes the tests that use it to not
           run in the parent class. */
        //cc = new AnymaDMX_ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }
}
