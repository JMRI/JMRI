package jmri.jmrix.dccpp.swing;

import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConfigBaseStationActionTest {

    private DCCppSystemConnectionMemo _memo;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConfigBaseStationFrame action = new ConfigBaseStationFrame(_memo);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        _memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo(t);

        jmri.InstanceManager.store(_memo, jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConfigBaseStationActionTest.class);

}
