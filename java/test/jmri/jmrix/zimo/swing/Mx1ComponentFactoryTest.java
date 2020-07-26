package jmri.jmrix.zimo.swing;

import jmri.jmrix.zimo.Mx1Listener;
import jmri.jmrix.zimo.Mx1Message;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import jmri.jmrix.zimo.Mx1TrafficController;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
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
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Mx1ComponentFactoryTest.class);

}
