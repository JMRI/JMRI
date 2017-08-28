package jmri.jmrix.secsi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SecsiSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class SecsiSystemConnectionMemoTest {
     
    SecsiSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",memo);
    }

    @Before
    public void setUp(){
       JUnitUtil.setUp();
       SerialTrafficController tc = new SerialTrafficController(){
          @Override
          public void sendSerialMessage(SerialMessage m,SerialListener reply) {
          }
       };
       memo = new SecsiSystemConnectionMemo();
    }

    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
