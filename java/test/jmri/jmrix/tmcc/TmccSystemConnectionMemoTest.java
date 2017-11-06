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
public class TmccSystemConnectionMemoTest {
     
    TmccSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists", memo);
    }

    @Before
    public void setUp(){
       JUnitUtil.setUp();
       memo = new TmccSystemConnectionMemo();
       SerialTrafficController tc = new SerialTrafficController(memo) {
          @Override
          public void sendSerialMessage(SerialMessage m, SerialListener reply) {
          }
       };
    }

    @After
    public void tearDown(){
        memo = null;
        JUnitUtil.tearDown();
    }

}
