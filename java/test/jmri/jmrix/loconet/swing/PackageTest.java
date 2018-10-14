package jmri.jmrix.loconet.swing;

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
    jmri.jmrix.loconet.swing.throttlemsg.PackageTest.class,
    LnComponentFactoryTest.class,
    LocoNetMenuTest.class,
    LnNamedPaneActionTest.class
})
public class PackageTest {
}
