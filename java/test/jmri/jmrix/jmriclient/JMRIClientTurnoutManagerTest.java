package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;


/**
 * JMRIClientTurnoutManagerTest.java
 *
 * Test for the jmri.jmrix.jmriclient.JMRIClientTurnoutManager
 * class
 *
 * @author Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2012,2016
 */
public class JMRIClientTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i){
        return "JT" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull(l);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JMRIClientTrafficController tc = new JMRIClientTrafficController(){
           @Override
           public void sendJMRIClientMessage(JMRIClientMessage m,JMRIClientListener reply) {
           }
        };
        l= new JMRIClientTurnoutManager(new JMRIClientSystemConnectionMemo(tc));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
