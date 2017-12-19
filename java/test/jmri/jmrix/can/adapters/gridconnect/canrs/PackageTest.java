package jmri.jmrix.can.adapters.gridconnect.canrs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MergMessageTest.class,
        MergReplyTest.class,
        jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.PackageTest.class,
        MergTrafficControllerTest.class,
})

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.canrs package.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class PackageTest  {
}
