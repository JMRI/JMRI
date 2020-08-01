package jmri.jmrix.grapevine.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.grapevine.SerialTrafficControlScaffold;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of GrapevineComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class GrapevineComponentFactoryTest {

    private SerialTrafficController tc = null;
    private GrapevineSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        GrapevineComponentFactory action = new GrapevineComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold(new GrapevineSystemConnectionMemo());
        m = new GrapevineSystemConnectionMemo();
        m.setSystemPrefix("ABC");
        m.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        tc = null;
    }

}
