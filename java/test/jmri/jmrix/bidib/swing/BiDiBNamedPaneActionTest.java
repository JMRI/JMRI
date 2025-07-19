package jmri.jmrix.bidib.swing;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for the BiDiBNamedPaneAction class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class BiDiBNamedPaneActionTest {

    BiDiBSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Nce Named Pane Test");
        BiDiBNamedPaneAction t = new BiDiBNamedPaneAction("Test Action", jf, "test", memo);
        Assertions.assertNotNull(t, "exists");
        jf.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
