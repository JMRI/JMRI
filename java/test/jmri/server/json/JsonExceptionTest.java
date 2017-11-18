package jmri.server.json;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for JsonException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class JsonExceptionTest {

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("JsonException string constructor",new JsonException(500,"test exception"));
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
