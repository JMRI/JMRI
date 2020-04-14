package jmri.jmrix.maple;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the MapleSystemConnectionMemo class
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class MapleSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {
     
    @Test
    public void systemPrefixTest() {
        // default values would be changed to K2 as there is already a connection with prefix [K] active
        MapleSystemConnectionMemo m = new MapleSystemConnectionMemo("K9", SerialConnectionTypeList.MAPLE);
        Assert.assertEquals("Special System Prefix", "K9", m.getSystemPrefix());
    }

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       MapleSystemConnectionMemo memo = new MapleSystemConnectionMemo();
       memo.setTrafficController(new SerialTrafficController() {
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
        scm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
