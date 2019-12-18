package jmri.jmrix.secsi.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.secsi.SerialTrafficController;
import jmri.jmrix.secsi.SerialTrafficControlScaffold;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of NodeConfigAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigActionTest {

    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction("secsi test Action", memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction(memo);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
        new SerialTrafficControlScaffold(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
