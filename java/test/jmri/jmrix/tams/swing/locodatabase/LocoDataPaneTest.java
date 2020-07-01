package jmri.jmrix.tams.swing.locodatabase;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.TamsInterfaceScaffold;
import jmri.jmrix.tams.TamsTrafficController;

/**
 * Test simple functioning of LocoDataPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LocoDataPaneTest extends jmri.util.swing.JmriPanelTest {

    private TamsSystemConnectionMemo memo = null;

    @Test
    @Override
    public void testInitComponents() {
        // this test currently just makes sure we don't throw any exceptions
        // initializing the panel
        ((LocoDataPane)panel).initComponents(memo);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        memo = new TamsSystemConnectionMemo(tc);
        panel = new LocoDataPane();
        helpTarget="package.jmri.jmrix.tams.swing.locodatabase.LocoDataFrame";
        title="Tams Loco Database";
    }

    @Override
    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();    
    }
}
