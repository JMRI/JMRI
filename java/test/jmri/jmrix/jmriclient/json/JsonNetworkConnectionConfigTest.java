package jmri.jmrix.jmriclient.json;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class JsonNetworkConnectionConfigTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("ConnectionConfig constructor",new JsonNetworkConnectionConfig());
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
