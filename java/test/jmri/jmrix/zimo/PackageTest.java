package jmri.jmrix.zimo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.zimo.swing.PackageTest.class,
        jmri.jmrix.zimo.mx1.PackageTest.class,
        jmri.jmrix.zimo.mxulf.PackageTest.class,
        Mx1SystemConnectionMemoTest.class,
        Mx1PortControllerTest.class,
        Mx1TrafficControllerTest.class,
        Mx1ExceptionTest.class,
        Mx1MessageExceptionTest.class,
        Mx1CommandStationTest.class,
        Mx1ConnectionTypeListTest.class,
        Mx1MessageTest.class,
        Mx1PacketizerTest.class,
        Mx1PowerManagerTest.class,
        Mx1ProgrammerManagerTest.class,
        Mx1ProgrammerTest.class,
        Mx1ThrottleManagerTest.class,
        Mx1ThrottleTest.class,
        Mx1TurnoutManagerTest.class,
        Mx1TurnoutTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.zimo package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
