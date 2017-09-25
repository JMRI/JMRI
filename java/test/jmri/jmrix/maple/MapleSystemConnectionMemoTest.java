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
public class MapleSystemConnectionMemoTest {
     
    MapleSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists", memo);
    }

    @Test
    public void systemPrefixTest() {
        // default values would be changed to K2 as there is already a connection with prefix [K] active
        MapleSystemConnectionMemo m = new MapleSystemConnectionMemo("K9", SerialConnectionTypeList.MAPLE);
        Assert.assertEquals("Special System Prefix", "K9", m.getSystemPrefix());
    }

    @Before
    public void setUp(){
       JUnitUtil.setUp();
       SerialTrafficController tc = new SerialTrafficController() {
          @Override
          public void sendSerialMessage(SerialMessage m, SerialListener reply) {
          }
       };
       memo = new MapleSystemConnectionMemo();
    }

    @After
    public void tearDown(){
        memo = null;
        JUnitUtil.tearDown();
    }

}
