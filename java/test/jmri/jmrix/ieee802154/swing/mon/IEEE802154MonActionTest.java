package jmri.jmrix.ieee802154.swing.mon;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of IEEE802154MonAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IEEE802154MonActionTest {

    private IEEE802154SystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IEEE802154MonAction action = new IEEE802154MonAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new IEEE802154SystemConnectionMemo();
        jmri.InstanceManager.setDefault(IEEE802154SystemConnectionMemo.class,memo);
    }

    @After
    public void tearDown() {
	memo = null;
    	JUnitUtil.tearDown();    
    }
}
