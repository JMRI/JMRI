package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Section class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class SectionTest {

   @Test
   public void SysNameConstructorTest(){
      Assert.assertNotNull("Constructor", new Section("TS1"));
   }

   @Test
   public void TwoNameStringConstructorTest(){
      Assert.assertNotNull("Constructor", new Section("TS1", "user name"));
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
