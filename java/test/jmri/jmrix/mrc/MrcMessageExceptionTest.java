package jmri.jmrix.mrc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MrcMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class MrcMessageExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("MrcMessageException constructor",new MrcMessageException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("MrcMessageException string constructor",new MrcMessageException("test exception"));
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
