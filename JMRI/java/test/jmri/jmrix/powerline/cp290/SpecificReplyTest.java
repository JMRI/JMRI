package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificReply class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificReplyTest {

   private SpecificTrafficController tc = null;

   @Test
   public void ConstructorTest(){
      Assert.assertNotNull("SpecificReply constructor",new SpecificReply(tc));
   }

   @Before
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tc = new SpecificTrafficController(new SpecificSystemConnectionMemo());

   }

   @After
   public void tearDown(){
        JUnitUtil.tearDown();
        tc = null;
   }

}
