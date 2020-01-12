package jmri.util.prefs;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for InitializationException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class HasConnectionButUnableToConnectExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("HasConnectionButUnableToConnectException constructor",new HasConnectionButUnableToConnectException("test exception",null));
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
