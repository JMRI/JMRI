package jmri.jmrix.jmriclient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JMRIClientSensorManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientSensorManager
 * class
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016
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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JMRIClientTrafficController tc = new JMRIClientTrafficController(){
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
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
