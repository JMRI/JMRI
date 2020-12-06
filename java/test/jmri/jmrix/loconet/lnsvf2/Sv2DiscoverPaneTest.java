package jmri.jmrix.loconet.lnsvf2;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test of Discover SV2 Devices Pane tool.
 *
 * @author Egbert Broerse   Copyright 2020
 * @author Paul Bender Copyright (C) 2017
 */
public class Sv2DiscoverPaneTest extends jmri.util.swing.JmriPanelTest {

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
        panel = new Sv2DiscoverPane();
        helpTarget = "package.jmri.jmrix.loconet.lnsvf2.Sv2DiscoverPane";
        title = Bundle.getMessage("MenuItemDiscoverSv2");
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = null;

        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Sv2DiscoverPane.class);

}
