package jmri.jmrix.maple;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the MapleSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class MapleSystemConnectionMemoTest {
     
    MapleSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",memo);
    }

    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       SerialTrafficController tc = new SerialTrafficController(){
          @Override
          public void sendSerialMessage(SerialMessage m,SerialListener reply) {
          }
       };
       memo = new MapleSystemConnectionMemo();
    }

    @After
    public void tearDown(){
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
