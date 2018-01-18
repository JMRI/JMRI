package jmri.jmrix.qsi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.qsi.QsiTrafficControllerTest.class,
        jmri.jmrix.qsi.QsiMessageTest.class,
        jmri.jmrix.qsi.QsiReplyTest.class,
        jmri.jmrix.qsi.serialdriver.PackageTest.class,
        jmri.jmrix.qsi.qsimon.PackageTest.class,
        jmri.jmrix.qsi.packetgen.PackageTest.class,
        QsiSystemConnectionMemoTest.class,
        QsiPortControllerTest.class,
        jmri.jmrix.swing.PackageTest.class,
        QSIConnectionTypeListTest.class,
        QSIMenuTest.class,
        QsiProgrammerTest.class,
})

/**
 * Tests for the jmri.jmrix.qsi package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
