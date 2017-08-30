package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
@MockPolicy(Slf4jMockPolicy.class)

/**
 * <P>
 * Tests for XBeeNodeManager
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
@RunWith(PowerMockRunner.class)
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
        //apps.tests.Log4JFixture.setUp();
        tc = new XBeeInterfaceScaffold();
    }

    @After
    public void tearDown() {
        //apps.tests.Log4JFixture.tearDown();
        tc = null;
    }


}
