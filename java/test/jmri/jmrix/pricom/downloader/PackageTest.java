package jmri.jmrix.pricom.downloader;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.pricom.downloader.PdiFileTest.class,
        jmri.jmrix.pricom.downloader.LoaderPaneTest.class,
        LoaderFrameTest.class,
        LoaderPanelActionTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.pricom.downloader package.
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class PackageTest  {
}
