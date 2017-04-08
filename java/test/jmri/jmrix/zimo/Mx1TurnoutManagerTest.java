package jmri.jmrix.zimo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Mx1TurnoutManagerTest {
        
    private Mx1TrafficController tc = null;

    @Test
    public void testCTor() {
        Mx1TurnoutManager t = new Mx1TurnoutManager(tc,"Z");
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new Mx1TrafficController(){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
           }
        };
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1TurnoutManagerTest.class.getName());

}
