package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
