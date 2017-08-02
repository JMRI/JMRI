package jmri.jmrix.loconet.pm4;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Paul Bender (C) 2016
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    PM4PanelTest.class
})
public class PackageTest {
}
