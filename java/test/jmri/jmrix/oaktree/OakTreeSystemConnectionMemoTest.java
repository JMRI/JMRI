package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the OakTreeSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class OakTreeSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {
     
    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       OakTreeSystemConnectionMemo memo = new OakTreeSystemConnectionMemo();
       memo.setTrafficController(new SerialTrafficController(memo){
          @Override
          public void sendSerialMessage(SerialMessage m, SerialListener reply) {
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
