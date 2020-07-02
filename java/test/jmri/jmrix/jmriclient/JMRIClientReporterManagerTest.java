package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JMRIClientReporterManagerTest.java
 *
 * Test for the jmri.jmrix.jmriclient.JMRIClientReporterManager
 * class
 *
 * @author Bob Jacobsen
 */
public class JMRIClientReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "JR" + i;
    }


    @BeforeEach
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

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
