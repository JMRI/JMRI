package jmri.jmrix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for NetworkConfigException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class NetworkConfigExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("NetworkConfigException constructor",new NetworkConfigException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("NetworkConfigException string constructor",new NetworkConfigException("test exception"));
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
