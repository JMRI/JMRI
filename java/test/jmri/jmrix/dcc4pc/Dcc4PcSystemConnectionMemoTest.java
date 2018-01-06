package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the Dcc4PcSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class Dcc4PcSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       // Dcc4Pc systems report being able to provide an addresed programmer, 
       // but they really just forward it to another connection. 
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Before
    @Override
    public void setUp(){
       JUnitUtil.setUp();
       Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
          @Override
          public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
          }
       };
       scm = new Dcc4PcSystemConnectionMemo(tc);
    }

    @After
    @Override
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
