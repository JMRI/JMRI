package jmri.jmrix.grapevine.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of GrapevineComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold(new GrapevineSystemConnectionMemo());
        m = new GrapevineSystemConnectionMemo();
        m.setSystemPrefix("ABC");
        m.setTrafficController(tc);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        tc = null;
    }

}
