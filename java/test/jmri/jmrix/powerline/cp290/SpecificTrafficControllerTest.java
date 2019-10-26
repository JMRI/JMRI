package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;
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
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new SpecificSystemConnectionMemo();
        tc = new SpecificTrafficController(memo);
   }

   @Override
   @After
   public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        memo = null;
   }

}
