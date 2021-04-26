package jmri.jmrix.mrc;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the MrcTrafficController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class MrcTrafficControllerTest {

    private MrcTrafficController tc = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",tc);
    }

    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       tc = new MrcTrafficController(){
          @Override
          public boolean status() { return true; }
          @Override
          public void sendMrcMessage(MrcMessage m) {}
          @Override
          public boolean isXmtBusy() { return false; }
       };
    }

    @AfterEach
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
