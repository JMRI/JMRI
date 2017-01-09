package jmri.jmrix.powerline;

import jmri.implementation.AbstractTurnoutTest;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.powerline.SerialTurnout class
 *
 * @author	Bob Jacobsen Copyright 2008 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
  */
public class SerialTurnoutTest extends AbstractTurnoutTest {

    private SerialSystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tc = null;

    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface
        memo = new SpecificSystemConnectionMemo();
        tc = new SerialTrafficControlScaffold();
        tc.setAdapterMemo(memo);
        memo.setTrafficController(tc);
        memo.setSerialAddress(new SerialAddress(memo));
        t = new SerialTurnout("PTA4", tc, "tA4");
    }

    @Override
    public int numListeners() {
        return tc.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {

//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
    }

}
