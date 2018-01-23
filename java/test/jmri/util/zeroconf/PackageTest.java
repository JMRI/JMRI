package jmri.util.zeroconf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ZeroConfServiceTest.class,
        ZeroConfClientTest.class,
        ZeroConfServiceEventTest.class,
})

/**
 * Invokes complete set of tests in the jmri.util.zeroconf tree
 *
 * @author	Bob Jacobsen Copyright 2003
 * @author Paul Bender Copyright 2014
 */
public class PackageTest  {
}
