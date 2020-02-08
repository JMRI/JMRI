package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CanisbSerialDriverAdapter class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class CanisbSerialDriverAdapterTest {

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SerialDriverAdapter constructor",new CanisbSerialDriverAdapter());
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
