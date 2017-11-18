package jmri.jmrix.jmriclient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JMRIClientTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientTurnout class
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2017
 */
public class JMRIClientTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase  {

    @Override
    public int numListeners() {
        return jcins.numListeners();
    }

    protected JMRIClientTrafficControlScaffold jcins;

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "TURNOUT "+ t.getSystemName()+ " CLOSED\n",
                jcins.outbound.elementAt(jcins.outbound.size() - 1).toString());
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "TURNOUT "+ t.getSystemName() + " THROWN\n",
                jcins.outbound.elementAt(jcins.outbound.size() - 1).toString());
    }

    @Override
    @Test
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController

        //is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }


    @Test
    public void testCtor() {
        Assert.assertNotNull(t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jcins = new JMRIClientTrafficControlScaffold();
        t = new JMRIClientTurnout(3, new JMRIClientSystemConnectionMemo(jcins));
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jcins = null;
    }

}
