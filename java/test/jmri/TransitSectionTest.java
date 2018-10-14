package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TransitSection class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class TransitSectionTest {

   @Test
   public void SysNameConstructorTest(){
      Assert.assertNotNull("Delayed", new TransitSection("TS1",0,0,false));
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
