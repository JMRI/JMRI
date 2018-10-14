package jmri.jmrix.tmcc.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SerialDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SerialDriverAdapter constructor", new SerialDriverAdapter());
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
