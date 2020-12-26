package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test of LNCV Programming Pane tool.
 *
 * @author Egbert Broerse   Copyright 2020
 * @author Paul Bender Copyright (C) 2017
 */
public class LncvProgPaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();

        // prepare an interface, register
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        //jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);

        // pane for AbstractMonFrameTestBase, panel for JmriPanelTest
        panel = new LncvProgPane();
        helpTarget = "package.jmri.jmrix.loconet.swing.lncvprog.LncvProgPane";
        title = Bundle.getMessage("MenuItemLncvProg");
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = null;

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LncvProgPane.class);

}
