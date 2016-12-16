package jmri.jmrix.dcc4pc;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the Dcc4PcSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class Dcc4PcSystemConnectionMemoTest {
     
    Dcc4PcSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",memo);
    }

    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
          @Override
          public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
          }
       };
       memo = new Dcc4PcSystemConnectionMemo(tc);
    }

    @After
    public void tearDown(){
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
