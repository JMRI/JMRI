package jmri.jmrix.cmri.serial.sim.configurexml;

import jmri.jmrix.cmri.serial.sim.ConnectionConfig;
import jmri.jmrix.cmri.serial.sim.SimDriverAdapter;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * ConnectionConfigXmlTest.java
 *
 * Description: tests for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

    @Test
    @Ignore("hangs")
    public void testStore(){
      // tests that store produces an XML element from a new ConnectionConfig object.
      SimDriverAdapter p = new SimDriverAdapter();
      p.configure();
      ConnectionConfigXml x = new ConnectionConfigXml();
      x.getInstance();
      Assert.assertNotNull("ConnectionConfigXml store()",new ConnectionConfigXml().store(new ConnectionConfig(p)));
    }
}

