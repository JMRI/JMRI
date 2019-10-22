package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * JMRIClientTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientTurnoutManager
 * class
 *
 * @author	Bob Jacobsen
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

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JMRIClientTrafficController tc = new JMRIClientTrafficController(){
           @Override
           public void sendJMRIClientMessage(JMRIClientMessage m,JMRIClientListener reply) {
           }
        };
        l= new JMRIClientTurnoutManager(new JMRIClientSystemConnectionMemo(tc));
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
