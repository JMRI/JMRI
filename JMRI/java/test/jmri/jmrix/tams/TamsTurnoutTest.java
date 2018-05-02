package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "xT 5,g,1",
                tnis.outbound.elementAt(tnis.outbound.size() - 1).toString());
    }


    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tnis = new TamsInterfaceScaffold();
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo(tnis);  
        t = new TamsTurnout(5,memo.getSystemPrefix(),tnis);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsTurnoutTest.class);

}
