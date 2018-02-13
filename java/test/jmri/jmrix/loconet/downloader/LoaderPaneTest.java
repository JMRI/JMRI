package jmri.jmrix.loconet.downloader;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LoaderPaneTest extends jmri.util.swing.JmriPanelTest {

    private LnTrafficController lnis = null;
    private LocoNetSystemConnectionMemo memo = null;
    private SlotManager slotmanager = null;

    @Override
    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes ure there isn't an exception.
        ((LoaderPane) panel).initComponents(memo);
    }

    @Test
    public void testInitContext() throws Exception {
        // for now, just makes ure there isn't an exception.
        ((LoaderPane)panel).initContext(memo);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
        panel = new LoaderPane();
        helpTarget="package.jmri.jmrix.loconet.downloader.LoaderFrame";
        title="Firmware Downloader";
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LoaderPaneTest.class);

}
