package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the Mx1TrafficController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class Mx1TrafficControllerTest {

    private Mx1TrafficController tc = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",tc);
    }

    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       tc = new Mx1TrafficController(){
          @Override
          public boolean status() {return true;}
          @Override
          public void sendMx1Message(Mx1Message m, Mx1Listener reply){}
       };
    }

    @AfterEach
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
