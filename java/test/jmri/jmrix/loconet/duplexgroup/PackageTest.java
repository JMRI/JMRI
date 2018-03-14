package jmri.jmrix.loconet.duplexgroup;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Paul Bender(C) 2016
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    jmri.jmrix.loconet.duplexgroup.swing.PackageTest.class,
    LnDplxGrpInfoImplConstantsTest.class,
    DuplexGroupMessageTypeTest.class
})
public class PackageTest {
}
