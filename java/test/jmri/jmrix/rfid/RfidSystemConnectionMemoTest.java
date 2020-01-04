package jmri.jmrix.rfid;

import javax.annotation.Nonnull;
import jmri.Reporter;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.rfid.RfidSystemConnectionMemo class.
 *
 * @author	Paul Bender
 */
public class RfidSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp(){
        JUnitUtil.setUp();
        RfidSystemConnectionMemo memo = new RfidSystemConnectionMemo();
        RfidTrafficController tc = new RfidTrafficController(){
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
        RfidSensorManager s = new RfidSensorManager(memo){
            @Override
            protected Sensor createNewSensor(@Nonnull String systemName, String userName){
               return null;
            }
            @Override
            public void message(RfidMessage m){}

            @Override
            public void reply(RfidReply m){}

        };
        RfidReporterManager r = new RfidReporterManager(memo){
            @Override
            protected Reporter createNewReporter(@Nonnull String systemName, String userName){
               return null;
            }
            @Override
            public void message(RfidMessage m){}

            @Override
            public void reply(RfidReply m){}

        };
        memo.configureManagers(s,r);
        scm = memo;
    }

    @Override
    @After
    public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
