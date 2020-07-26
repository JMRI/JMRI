package jmri.jmrix.lenz.li100f.configurexml;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import jmri.jmrix.lenz.li100f.ConnectionConfig;

/**
 * Tests for the ConnectionConfigXml class
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
