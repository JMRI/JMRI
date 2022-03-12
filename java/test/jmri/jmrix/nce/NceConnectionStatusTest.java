package jmri.jmrix.nce;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dave Heap Copyright (C) 2021
 */
public class NceConnectionStatusTest {

    private NceTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        NceConnectionStatus t = new NceConnectionStatus(tcis);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPowerProVer2007() {
        NceConnectionStatus t = new NceConnectionStatus(tcis);
        Assert.assertNotNull("exists", t);
        NceReply x = new NceReply(tcis);
        x.setBinary(true);
        x.setElement(0, 6);
        x.setElement(1, 2);
        x.setElement(2, 0);
        t.reply(x);
        Assert.assertFalse("Expected isPwrProVer060203orLater to be false", tcis.isPwrProVer060203orLater());
    }

    @Test
    public void testPowerProVer2007a() {
        NceConnectionStatus t = new NceConnectionStatus(tcis);
        Assert.assertNotNull("exists", t);
        NceReply x = new NceReply(tcis);
        x.setBinary(true);
        x.setElement(0, 6);
        x.setElement(1, 2);
        x.setElement(2, 1);
        t.reply(x);
        Assert.assertFalse("Expected isPwrProVer060203orLater to be false", tcis.isPwrProVer060203orLater());
    }

    @Test
    public void testPowerProVer2008to2011() {
        NceConnectionStatus t = new NceConnectionStatus(tcis);
        Assert.assertNotNull("exists", t);
        NceReply x = new NceReply(tcis);
        x.setBinary(true);
        x.setElement(0, 6);
        x.setElement(1, 2);
        x.setElement(2, 2);
        t.reply(x);
        Assert.assertFalse("Expected isPwrProVer060203orLater to be false", tcis.isPwrProVer060203orLater());
    }

    @Test
    public void testPowerProVer2021() {
        NceConnectionStatus t = new NceConnectionStatus(tcis);
        Assert.assertNotNull("exists", t);
        NceReply x = new NceReply(tcis);
        x.setBinary(true);
        x.setElement(0, 6);
        x.setElement(1, 2);
        x.setElement(2, 3);
        t.reply(x);
        Assert.assertTrue("Expected isPwrProVer060203orLater to be true", tcis.isPwrProVer060203orLater());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceConnectionStatusTest.class);
}
