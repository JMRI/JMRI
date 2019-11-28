package jmri.jmrix.powerline.cm11;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialListener;

/**
 * Tests for SpecificSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

   @Override
   @Before
   public void setUp() {
       JUnitUtil.setUp();

       jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
       SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
       memo.setTrafficController(new SpecificTrafficController(memo){
          @Override
          public void sendSerialMessage(SerialMessage m,SerialListener reply) {
          }
          @Override
          public void transmitLoop(){
          }
          @Override
          public void receiveLoop(){
          }
       });
       memo.configureManagers();
       scm = memo;
   }

   @Override
   @After
   public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

   }

}
