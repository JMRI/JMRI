package jmri.jmrix.openlcb.swing.downloader;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LoaderPaneTest extends jmri.util.swing.JmriPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        panel = new LoaderPane();
        title = "Firmware Downloader";
        helpTarget = "package.jmri.jmrix.openlcb.swing.downloader.LoaderFrame";
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LoaderPaneTest.class);

}
