package jmri.jmrix.loconet.ds64;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Paul Bender (C) 2016
 */
@RunWith(Suite.class)
@SuiteClasses({
    DS64BundleTest.class,
    Ds64TabbedPanelTest.class,
    SimpleTurnoutStateEntryTest.class,
    SimpleTurnoutTest.class,
    BundleTest.class,
})
public class PackageTest {
}
