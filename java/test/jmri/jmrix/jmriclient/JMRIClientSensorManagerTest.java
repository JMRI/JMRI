package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JMRIClientSensorManagerTest.java
 * <p>
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientSensorManager
 * class
 *
 * @author	Bob Jacobsen
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

    // The minimal setup for log4J
    @Override
    @Before
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

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
