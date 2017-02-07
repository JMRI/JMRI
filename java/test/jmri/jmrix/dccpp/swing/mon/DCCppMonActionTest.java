package jmri.jmrix.dccpp.swing.mon;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of DCCppMonAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DCCppMonActionTest {

    

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DCCppMonAction action = new DCCppMonAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo(t);

        jmri.InstanceManager.store(memo, jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class);

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
