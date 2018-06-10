package jmri.jmrix.loconet.loconetovertcp;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LnTcpServerAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LnTcpServerActionTest {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerAction action = new LnTcpServerAction("LocoNet test Action", memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerAction action = new LnTcpServerAction("LocoNet test Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerAction action = new LnTcpServerAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        memo = new LocoNetSystemConnectionMemo();
        lnis.setSystemConnectionMemo(memo);
        memo.setLnTrafficController(lnis);
    }

    @After
    public void tearDown() {
        memo.dispose();
        lnis = null;
        JUnitUtil.tearDown();
    }

}
