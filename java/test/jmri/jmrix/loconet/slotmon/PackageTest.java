package jmri.jmrix.loconet.slotmon;

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
    SlotMonPaneTest.class,
    SlotMonDataModelTest.class
})
public class PackageTest {
}
