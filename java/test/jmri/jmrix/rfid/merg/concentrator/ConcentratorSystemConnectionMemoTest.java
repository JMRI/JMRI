package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConcentratorSystemConnectionMemoTest.java
 *
 * Description:	tests for the ConcentratorSystemConnectionMemo class
 *
 * @author	Paul Bender Copyright(C) 2016
 */
public class ConcentratorSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        ConcentratorSystemConnectionMemo memo = new ConcentratorSystemConnectionMemo();
        ConcentratorTrafficController tc = new ConcentratorTrafficController(memo,"A-H"){
           @Override
           public void sendInitString(){
           }
           @Override
           public void transmitLoop(){
           }
           @Override
           public void receiveLoop(){
           }
        };
        memo.setRfidTrafficController(tc);
        memo.configureManagers(null,null);
        scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
