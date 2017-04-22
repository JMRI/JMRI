package jmri.jmrix.mrc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   jmri.jmrix.mrc.swing.PackageTest.class,
   jmri.jmrix.mrc.simulator.PackageTest.class,
   jmri.jmrix.mrc.serialdriver.PackageTest.class,
   jmri.jmrix.mrc.configurexml.PackageTest.class,
   MrcSystemConnectionMemoTest.class,
   MrcPortControllerTest.class,
   MrcTrafficControllerTest.class,
   MrcExceptionTest.class,
   MrcMessageExceptionTest.class,
   MrcConnectionTypeListTest.class,
   MrcPacketizerTest.class,
   MrcPacketsTest.class
})

/**
 * Tests for the jmri.jmrix.mrc package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest {

}
