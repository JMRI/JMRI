package jmri.jmrix.powerline.cp290;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for SpecificTrafficController class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificTrafficControllerTest extends jmri.jmrix.powerline.SerialTrafficControllerTest {
        
   private SpecificSystemConnectionMemo memo = null;

   @Override
   @Before
   public void setUp(){
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new SpecificSystemConnectionMemo();
        tc = new SpecificTrafficController(memo);
   }

   @Override
   @After
   public void tearDown(){
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
        memo = null;
   }

}
