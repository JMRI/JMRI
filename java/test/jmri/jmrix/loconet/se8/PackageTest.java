package jmri.jmrix.loconet.se8;

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
    SE8PanelTest.class
})
public class PackageTest {
}
