package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JMRIClientSensorManagerTest.java
 * <p>
 * Test for the jmri.jmrix.jmriclient.JMRIClientSensorManager
 * class
 *
 * @author Bob Jacobsen
 * @author Paul Bender Copyright (C) 2016
 */
public class JMRIClientSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "JS" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull(l);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JMRIClientTrafficController tc = new JMRIClientTrafficController() {
            @Override
            public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
            }
        };
        JMRIClientSystemConnectionMemo m = new JMRIClientSystemConnectionMemo(tc);
        l = new JMRIClientSensorManager(m);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
