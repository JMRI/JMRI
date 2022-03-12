package jmri.jmrix.lenz.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of XNetComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        m = new XNetSystemConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setSystemConnectionMemo(m);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
        tc = null;
    }
}
