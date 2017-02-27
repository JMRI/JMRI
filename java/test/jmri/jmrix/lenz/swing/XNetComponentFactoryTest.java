package jmri.jmrix.lenz.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of XNetComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XNetComponentFactoryTest {


    private XNetTrafficController tc = null;
    private XNetSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        XNetComponentFactory action = new XNetComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        m = new XNetSystemConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setSystemConnectionMemo(m);

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
        tc = null;
    }
}
