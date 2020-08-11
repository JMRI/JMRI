package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JMRIClientLightTest {

    @Test
    public void testCTor() {
        JMRIClientTrafficController tc = new JMRIClientTrafficController() {
            @Override
            public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
                // do nothing to avoid null pointer when sending to non-existant
                // connection during test.
            }
        };
        JMRIClientSystemConnectionMemo memo = new JMRIClientSystemConnectionMemo(tc);
        JMRIClientLight t = new JMRIClientLight(3,memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JMRIClientLightTest.class);

}
