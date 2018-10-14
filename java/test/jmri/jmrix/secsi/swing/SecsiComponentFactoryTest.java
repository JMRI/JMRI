package jmri.jmrix.secsi.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.secsi.SerialTrafficControlScaffold;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.jmrix.secsi.SerialTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SecsiComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SecsiComponentFactoryTest {


    // private SerialTrafficController tc = null;
    private SecsiSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SecsiComponentFactory action = new SecsiComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new SecsiSystemConnectionMemo();
        // tc = new SerialTrafficControlScaffold(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        // tc = null;
    }
}
