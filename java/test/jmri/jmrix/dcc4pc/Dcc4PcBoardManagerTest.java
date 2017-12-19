package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcc4PcBoardManagerTest {

    @Test
    public void testCTor() {
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
          @Override
          public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
          }
        };
        Dcc4PcSystemConnectionMemo memo = new Dcc4PcSystemConnectionMemo(tc);

        Dcc4PcSensorManager tm = new Dcc4PcSensorManager(tc,memo);
        Dcc4PcBoardManager t = new Dcc4PcBoardManager(tc,tm);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcBoardManagerTest.class);

}
