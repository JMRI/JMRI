package jmri.jmrix.can.adapters.lawicell;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MessageTest.class,
        ReplyTest.class,
        jmri.jmrix.can.adapters.lawicell.canusb.PackageTest.class,
        SerialDriverAdapterTest.class,
        PortControllerTest.class,
        LawicellTrafficControllerTest.class,
})

/**
 * Tests for the jmri.jmrix.can.adapters.lawicell package.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class PackageTest  {
}
