package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JMRIClientReporterManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientReporterManager
 * class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "JR" + i;
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JMRIClientTrafficController tc = new JMRIClientTrafficController(){
           @Override
           public void sendJMRIClientMessage(JMRIClientMessage m,JMRIClientListener reply) {
           }
        };
        l = new JMRIClientReporterManager(new JMRIClientSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
