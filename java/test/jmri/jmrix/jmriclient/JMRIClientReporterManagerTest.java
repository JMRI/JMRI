package jmri.jmrix.jmriclient;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JMRIClientTrafficController tc = new JMRIClientTrafficController(){
           @Override
           public void sendJMRIClientMessage(JMRIClientMessage m,JMRIClientListener reply) {
           }
        };
        l = new JMRIClientReporterManager(new JMRIClientSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
