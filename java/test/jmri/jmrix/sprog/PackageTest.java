package jmri.jmrix.sprog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrix.sprog package
 *
 * @author	Bob Jacobsen
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   SprogSystemConnectionMemoTest.class,
   SprogTrafficControllerTest.class,
   SprogMessageTest.class,
   SprogCSStreamPortControllerTest.class
})
public class PackageTest {
}
