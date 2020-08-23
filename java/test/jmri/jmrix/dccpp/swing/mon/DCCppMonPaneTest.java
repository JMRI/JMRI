package jmri.jmrix.dccpp.swing.mon;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of DCCppMonPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DCCppMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo(t);

        jmri.InstanceManager.store(memo, jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class);
        // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new DCCppMonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = "DCC++ Traffic Monitor";
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
