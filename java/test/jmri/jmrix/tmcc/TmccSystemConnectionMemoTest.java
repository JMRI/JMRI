package jmri.jmrix.tmcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the TMCCSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TmccSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       TmccSystemConnectionMemo memo = new TmccSystemConnectionMemo();
       new SerialTrafficController(memo) {
          @Override
          public void sendSerialMessage(SerialMessage m, SerialListener reply) {
          }
          @Override
          public void transmitLoop(){
          }
          @Override
          public void receiveLoop(){
          }
       };
       scm = memo;
    }

    @Override
    @After
    public void tearDown(){
        scm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
