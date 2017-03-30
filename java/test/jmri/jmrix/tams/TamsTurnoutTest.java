package jmri.jmrix.tams;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TamsTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    @Override
    public int numListeners() {
        return tnis.numListeners();
    }

    protected TamsInterfaceScaffold tnis;

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "xT 5,r,1",
                tnis.outbound.elementAt(tnis.outbound.size() - 1).toString());
        Assert.assertEquals("CLOSED state", jmri.Turnout.CLOSED, t.getCommandedState());
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "xT 5,g,1",
                tnis.outbound.elementAt(tnis.outbound.size() - 1).toString());
        Assert.assertEquals("THROWN state", jmri.Turnout.THROWN, t.getCommandedState());
    }


    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tnis = new TamsInterfaceScaffold();
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(tnis);  
        t = new TamsTurnout(5,memo.getSystemPrefix(),tnis);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(TamsTurnoutTest.class.getName());

}
