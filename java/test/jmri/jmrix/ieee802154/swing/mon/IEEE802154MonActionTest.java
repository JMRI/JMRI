package jmri.jmrix.ieee802154.swing.mon;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of IEEE802154MonAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class IEEE802154MonActionTest {

    private IEEE802154SystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IEEE802154MonAction action = new IEEE802154MonAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new IEEE802154SystemConnectionMemo();
        jmri.InstanceManager.setDefault(IEEE802154SystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();
    }
}
