package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Mx1ThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private Mx1SystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
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
        tm = new Mx1ThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Mx1ThrottleManagerTest.class);

}
