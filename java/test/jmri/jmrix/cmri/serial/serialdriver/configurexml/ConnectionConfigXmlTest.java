package jmri.jmrix.cmri.serial.serialdriver.configurexml;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialTrafficControlScaffold;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig;
import jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter;
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
    @Ignore("causes errors")
    public void testStore(){
      // tests that store produces an XML element from a new ConnectionConfig object.
      ConnectionConfigXml c = new ConnectionConfigXml();
      SerialDriverAdapter p = new SerialDriverAdapter(){
              /**
               * set up all of the other objects to operate connected to this port
               */
              @Override
              public void configure() {
                // connect to the traffic controller
                SerialTrafficController tc = new SerialTrafficControlScaffold();
                tc.connectPort(this);
                ((CMRISystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);
                ((CMRISystemConnectionMemo)getSystemConnectionMemo()).configureManagers();
              }
      };
      ConnectionConfig cc = new ConnectionConfig(p);
      p.configure();
      c.getInstance(cc);
      Assert.assertNotNull("ConnectionConfigXml store()",c.store(cc));
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

