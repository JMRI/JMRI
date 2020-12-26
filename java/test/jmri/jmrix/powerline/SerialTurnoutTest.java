package jmri.jmrix.powerline;

import jmri.implementation.AbstractTurnoutTestBase;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.powerline.SerialTurnout class
 *
 * @author Bob Jacobsen Copyright 2008 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private SerialSystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tc = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        memo = new SpecificSystemConnectionMemo();
        tc = new SerialTrafficControlScaffold();
        tc.setAdapterMemo(memo);
        memo.setTrafficController(tc);
        memo.setSerialAddress(new SerialAddress(memo));
        t = new SerialTurnout("PTA4", tc, "tA4");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    @Override
    public int numListeners() {
        return tc.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
//        tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
//        Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
//      tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//      Assert.assertTrue("message sent", tcis.outbound.size() > 0);
//      Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

}
