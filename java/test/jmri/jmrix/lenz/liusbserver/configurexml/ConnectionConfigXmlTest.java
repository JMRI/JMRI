package jmri.jmrix.lenz.liusbserver.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.lenz.liusbserver.ConnectionConfig;
import javax.swing.JPanel;

/**
 * ConnectionConfigXmlTest.java
 *
 * Description: tests for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
        cc.loadDetails(new JPanel());
    }

    @After
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

    @Test
    @Ignore("generates error message when run")
    public void getInstanceTest() {
    }
}
