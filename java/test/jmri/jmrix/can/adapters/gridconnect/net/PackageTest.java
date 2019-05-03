package jmri.jmrix.can.adapters.gridconnect.net;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.can.adapters.gridconnect.net.configurexml.PackageTest.class,
   MergConnectionConfigTest.class,
   MergNetworkDriverAdapterTest.class,
   NetworkDriverAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.net package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
