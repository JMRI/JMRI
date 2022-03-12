package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for JmriConfigureXmlException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class JmriConfigureXmlExceptionTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("JmriConfigureXmlException constructor",new JmriConfigureXmlException());
   }

   @Test
   public void StringConstructorTest(){
      Assert.assertNotNull("JmriConfigureXmlException string constructor",new JmriConfigureXmlException("test exception"));
   }

   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
