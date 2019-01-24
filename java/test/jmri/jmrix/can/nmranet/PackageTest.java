package jmri.jmrix.can.nmranet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
     jmri.jmrix.can.nmranet.configurexml.PackageTest.class,
     jmri.jmrix.can.nmranet.swing.PackageTest.class,
     jmri.jmrix.can.nmranet.NmraConfigurationManagerTest.class,
     BundleTest.class
})
/**
 * Tests for the jmri.jmrix.can.nmranet.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
