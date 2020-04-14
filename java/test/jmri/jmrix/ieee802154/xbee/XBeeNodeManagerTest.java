package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for XBeeNodeManager.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeNodeManagerTest {

   XBeeInterfaceScaffold tc = null; // set in setUp.

   @Test
   public void ConstructorTest(){
       XBeeNodeManager a = new XBeeNodeManager(tc){
          @Override
          public void startNodeDiscovery(){
             // no mock network, so don't try to discover.
          }
       };
       Assert.assertNotNull(a);
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

    }

}
