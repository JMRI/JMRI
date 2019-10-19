package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * ConcentratorReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.merg.concentrator.ConcentratorReply class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    ConcentratorTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new ConcentratorTrafficController(new ConcentratorSystemConnectionMemo(),"A-H"){
           @Override
           public void sendInitString(){
           }
        };
        tc.getAdapterMemo().setProtocol(new jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol());
            m = new ConcentratorReply(tc){
            @Override
            public String toMonitorString(){
               return "";
            }
        };
    }

    @After
    public void tearDown() {
        tc = null;
	m = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
