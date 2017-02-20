package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the Mx1SystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class Mx1SystemConnectionMemoTest {
     
    Mx1SystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",memo);
    }

    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       Mx1TrafficController tc = new Mx1TrafficController(){
          @Override
          public boolean status(){
             return true;
          }
          @Override
          public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
          }
       };
       memo = new Mx1SystemConnectionMemo();
    }

    @After
    public void tearDown(){
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
