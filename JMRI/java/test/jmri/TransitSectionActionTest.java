package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TransitSectionAction class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class TransitSectionActionTest {

   @Test
   public void ShortConstructorTest(){
      TransitSectionAction t;
      Assert.assertNotNull("Constructor", t = new TransitSectionAction(2,3));
      Assert.assertEquals(t.getWhenCode(), 2);
      Assert.assertEquals(t.getWhatCode(), 3);
      
      // check defaults
      Assert.assertEquals(t.getDataWhen(), -1);
      Assert.assertEquals(t.getDataWhat1(), -1);
      Assert.assertEquals(t.getDataWhat2(), -1);
      Assert.assertEquals(t.getStringWhen(), "");
      Assert.assertEquals(t.getStringWhat(), "");     
   }

   @Test
   public void LongConstructorTest(){
      TransitSectionAction t;
      Assert.assertNotNull("Constructor", t = new TransitSectionAction(4,5,6,7,8,"a","b"));
      Assert.assertEquals(t.getWhenCode(), 4);
      Assert.assertEquals(t.getWhatCode(), 5);

      Assert.assertEquals(t.getDataWhen(), 6);
      Assert.assertEquals(t.getDataWhat1(), 7);
      Assert.assertEquals(t.getDataWhat2(), 8);
      Assert.assertEquals(t.getStringWhen(), "a");
      Assert.assertEquals(t.getStringWhat(), "b");
   }

   @Test
   public void WhenCodeDataIndependentTest(){
      TransitSectionAction t = new TransitSectionAction(11,12,13,14,15,"A","B");

      Assert.assertEquals(t.getWhenCode(), 11);
      Assert.assertEquals(t.getDataWhen(), 13);
      
      t.setWhenCode(21);
      
      Assert.assertEquals(t.getWhenCode(), 21);
      Assert.assertEquals(t.getDataWhen(), 13);

      t.setDataWhen(32);
      
      Assert.assertEquals(t.getWhenCode(), 21);
      Assert.assertEquals(t.getDataWhen(), 32);
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
