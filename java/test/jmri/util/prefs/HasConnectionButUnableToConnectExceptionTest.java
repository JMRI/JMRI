package jmri.util.prefs;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
