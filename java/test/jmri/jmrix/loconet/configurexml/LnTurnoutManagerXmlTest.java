package jmri.jmrix.loconet.configurexml;

import jmri.jmrix.loconet.LnTurnout;
import jmri.jmrix.loconet.LnTurnoutManager;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the LnTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnTurnoutManagerXml constructor", new LnTurnoutManagerXml());
    }

    @Test
    public void testSaveAndRestoreWithProperties() {
        LnTurnoutManagerXml lntmXml = new LnTurnoutManagerXml();
        LnTurnout lnto = (LnTurnout) lmtm.newTurnout("LT61", "UNAMEt61");
        // check defaults UlenB binaryoutput true
        lnto.setBinaryOutput(true);
        Assert.assertFalse("Bypass is false",lnto.isByPassBushbyBit());
        Assert.assertFalse("SendOnOff is false",lnto.isSendOnAndOff());

        // check defaults Loconet binaryoutput false
        lnto.setBinaryOutput(false);
        Assert.assertFalse("Bypass is false",lnto.isByPassBushbyBit());
        Assert.assertTrue("SendOnOff is true",lnto.isSendOnAndOff());

        // update defaults
        lnto.setProperty(LnTurnoutManager.SENDONANDOFFKEY, false);
        lnto.setProperty(LnTurnoutManager.BYPASSBUSHBYBITKEY, true);
        Assert.assertTrue("Bypass is true",lnto.isByPassBushbyBit());
        Assert.assertFalse("SendOnOff is false",lnto.isSendOnAndOff());
        Element stored = lntmXml.store(lmtm);
        Assert.assertNotNull(stored);
        jmri.InstanceManager.getDefault().clearAll();
        jmri.InstanceManager.setTurnoutManager(lmtm);

        lntmXml.load(stored, null);
        LnTurnout t = (LnTurnout) lmtm.getBySystemName("LT61");
        Assert.assertNotNull(t);
        Assert.assertTrue("Bypass is true",t.isByPassBushbyBit());
        Assert.assertFalse("SendOnOff is false",t.isSendOnAndOff());
    }

    LocoNetInterfaceScaffold lnis;
    LocoNetSystemConnectionMemo memo;
    LnTurnoutManager lmtm;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface, register
        memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        lnis = new LocoNetInterfaceScaffold(memo);
        // create and register the manager object
        lmtm = new LnTurnoutManager(memo, lnis, false);
        jmri.InstanceManager.setTurnoutManager(lmtm);
    }

    @After
    public void tearDown() {
        memo.dispose();
        lnis = null;
        lmtm = null;
        JUnitUtil.tearDown();
    }

}

