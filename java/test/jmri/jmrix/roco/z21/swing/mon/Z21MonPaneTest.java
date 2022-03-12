package jmri.jmrix.roco.z21.swing.mon;

import jmri.util.JUnitUtil;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Z21MonPaneTest.java
 * <p>
 * Test for the jmri.jmrix.roco.z21.swing.mon.Z21MonPane class
 *
 * @author Paul Bender Copyright (C) 2014,2016
 */
public class Z21MonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null; 

    @Test
    public void testDefault() {
        jmri.util.swing.JmriNamedPaneAction f = new Z21MonPane.Default();
        Assert.assertNotNull(f);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        jmri.InstanceManager.store(memo, jmri.jmrix.roco.z21.Z21SystemConnectionMemo.class);
        // pane for AbstractMonPaneTestBase; panel for JmriPanelTest 
        panel = pane = new Z21MonPane();
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
        title = Bundle.getMessage("Z21TrafficTitle");
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo=null;
        tc.terminateThreads();
        tc=null;
        panel = pane = null;
        JUnitUtil.tearDown();
    }

}
