package jmri.jmrix.loconet.loconetovertcp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LnOverTcpPacketizer class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class LnOverTcpPacketizerTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("LnOverTcpPacketizer constructor",new LnOverTcpPacketizer());
   }

   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
   }

}
