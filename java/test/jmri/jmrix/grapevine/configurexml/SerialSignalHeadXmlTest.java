package jmri.jmrix.grapevine.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 * Tests for the SerialSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialSignalHeadXmlTest {

    private GrapevineSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialSignalHeadXml constructor", new SerialSignalHeadXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
        jmri.InstanceManager.store(memo,GrapevineSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}

