package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcc4PcBoardManagerTest {

    Dcc4PcSensorManager tm = null;

    @Test
    public void testCTor() {
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
          @Override
          public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
          }
        };
        Dcc4PcSystemConnectionMemo memo = new Dcc4PcSystemConnectionMemo(tc);
        tm = new Dcc4PcSensorManager(tc,memo);
        Dcc4PcBoardManager t = new Dcc4PcBoardManager(tc,tm);
        Assert.assertNotNull("exists",t);
        tm.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        tm.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcBoardManagerTest.class);

}
