package jmri.jmrix.cmri.serial.sim.configurexml;

import jmri.jmrix.cmri.serial.sim.ConnectionConfig;
import jmri.jmrix.cmri.serial.sim.SimDriverAdapter;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

