package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Mx1TurnoutManagerTest {
        
    private Mx1TrafficController tc = null;

    @Test
    public void testCTor() {
        Mx1TurnoutManager t = new Mx1TurnoutManager(tc.getAdapterMemo());
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        Mx1SystemConnectionMemo memo = new Mx1SystemConnectionMemo();
        tc = new Mx1TrafficController(){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public void sendMx1Message(Mx1Message m, Mx1Listener reply) {
           }
        };
        tc.setAdapterMemo(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Mx1TurnoutManagerTest.class);

}
