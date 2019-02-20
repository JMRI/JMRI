package jmri.jmrix.can.cbus.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CbusDummyCSSessionTest.class,
        CbusDummyCSTest.class,
        CbusDummyNodeTest.class,
        CbusEventResponderTest.class,
        CbusSimulatorTest.class,
        BundleTest.class
})

/**
 * Tests for the jmri.jmrix.can.cbus.simulator package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest  {
}
