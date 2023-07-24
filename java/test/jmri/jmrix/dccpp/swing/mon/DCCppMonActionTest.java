package jmri.jmrix.dccpp.swing.mon;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of DCCppMonAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
public class DCCppMonActionTest {

    @Test
    public void testDccPpMemoCtor() {
        DCCppMonAction action = new DCCppMonAction();
        Assert.assertNotNull("exists", action);
    }

    private jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo(t);

        jmri.InstanceManager.store(memo, jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class);

    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
}
