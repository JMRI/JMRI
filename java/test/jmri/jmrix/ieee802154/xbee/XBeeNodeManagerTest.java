package jmri.jmrix.ieee802154.xbee;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for XBeeNodeManager.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class XBeeNodeManagerTest {

   XBeeInterfaceScaffold tc = null; // set in setUp.

   @Test
   public void testCtor(){
       XBeeNodeManager a = new XBeeNodeManager(tc){
          @Override
          public void startNodeDiscovery(){
             // no mock network, so don't try to discover.
          }
       };
       Assert.assertNotNull(a);
   }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
    }

    @AfterEach
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
