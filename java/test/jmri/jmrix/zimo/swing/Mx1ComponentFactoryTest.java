package jmri.jmrix.zimo.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import jmri.jmrix.zimo.Mx1TrafficController;
import jmri.jmrix.zimo.Mx1Message;
import jmri.jmrix.zimo.Mx1Listener;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Mx1ComponentFactoryTest {
        
    private Mx1SystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        Mx1ComponentFactory t = new Mx1ComponentFactory(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        Mx1TrafficController tc = new Mx1TrafficController(){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
           }
        };
        memo = new Mx1SystemConnectionMemo(tc);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1ComponentFactoryTest.class.getName());

}
