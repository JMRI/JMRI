package jmri.jmrix.cmri.serial.sim.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.jmrix.cmri.serial.sim.ConnectionConfig;
import jmri.jmrix.cmri.serial.sim.SimDriverAdapter;

/**
 * ConnectionConfigXmlTest.java
 *
 * Description: tests for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ConnectionConfigXml constructor",new ConnectionConfigXml());
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}

