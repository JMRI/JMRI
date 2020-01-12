package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * StandaloneSystemConnectionMemoTest.java
 *
 * Description:	tests for the StandaloneSystemConnectionMemo class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class StandaloneSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        StandaloneSystemConnectionMemo memo =new StandaloneSystemConnectionMemo();
        StandaloneTrafficController tc = new StandaloneTrafficController(memo){
          @Override
          public void transmitLoop(){
          }
          @Override
          public void receiveLoop(){
          }
        };
        memo.setRfidTrafficController(tc);
        memo.configureManagers(
            new StandaloneSensorManager(memo),
            new StandaloneReporterManager(memo));
        scm=memo;
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
