package jmri.jmrix.dccpp.dccppovertcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Server class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class ServerTest {

   @Test
   public void getInstanceTest(){
      // Server is provided by InstanceManagerAutoInitialize
      Server s = jmri.InstanceManager.getDefault(Server.class);
      Assert.assertNotNull("Server getInstance", s);
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class, memo);
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
