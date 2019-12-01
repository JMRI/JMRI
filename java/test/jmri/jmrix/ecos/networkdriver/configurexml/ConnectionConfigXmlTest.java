package jmri.jmrix.ecos.networkdriver.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.easydcc.serialdriver.ConnectionConfig;

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
        /* somehow the adapter is still null after the loadDetails below.
           not creating cc causes the tests that use it in the abstract class
           to be skipped with an Assume */
        //cc = new ConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
