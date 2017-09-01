package jmri.jmrix.dccpp.swing.mon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of DCCppMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DCCppMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", pane );
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo(t);

        jmri.InstanceManager.store(memo, jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class);
        pane = new DCCppMonPane();
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
