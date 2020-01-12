package jmri.jmrix.cmri.serial.sim;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SimDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SimDriverAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SimDriverAdapter constructor", new SimDriverAdapter());
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
