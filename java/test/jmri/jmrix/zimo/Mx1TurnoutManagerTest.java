package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Mx1TurnoutManagerTest {
        
    private Mx1TrafficController tc = null;

    @Test
    public void testCTor() {
        Mx1TurnoutManager t = new Mx1TurnoutManager(tc.getAdapterMemo());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new Mx1TrafficController(){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
           }
        };
        tc.setAdapterMemo(new Mx1SystemConnectionMemo());
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Mx1TurnoutManagerTest.class);

}
