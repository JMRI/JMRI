package jmri.jmrix.lenz.lzv200.configurexml;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import jmri.jmrix.lenz.lzv200.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.lenz.configurexml.AbstractXNetSerialConnectionConfigXmlTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }
}
