package jmri.jmrix.loconet.duplexgroup.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood (C) 2016
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    DuplexGroupInfoPanelTest.class,
    DuplexGroupScanPanelTest.class,
    DuplexGroupTabbedPanelTest.class,
    LnDplxGrpInfoImplTest.class,
    LnIPLImplementationTest.class
})
public class PackageTest {
}
