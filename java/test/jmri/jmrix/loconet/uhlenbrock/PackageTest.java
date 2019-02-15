package jmri.jmrix.loconet.uhlenbrock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.uhlenbrock.configurexml.PackageTest.class,
   UhlenbrockAdapterTest.class,
   UhlenbrockConnectionTypeListTest.class,
   UhlenbrockPacketizerTest.class,
   UhlenbrockSystemConnectionMemoTest.class,
   UhlenbrockProgrammerManagerTest.class,
   UhlenbrockLnThrottleManagerTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.uhlenbrock package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
