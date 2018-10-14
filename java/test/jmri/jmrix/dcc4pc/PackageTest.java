package jmri.jmrix.dcc4pc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.dcc4pc.serialdriver.PackageTest.class,
   jmri.jmrix.dcc4pc.configurexml.PackageTest.class,
   jmri.jmrix.dcc4pc.swing.PackageTest.class,
   Dcc4PcReporterManagerTest.class,
   Dcc4PcSystemConnectionMemoTest.class,
   Dcc4PcPortControllerTest.class,
   Dcc4PcTrafficControllerTest.class,
   Dcc4PcConnectionTypeListTest.class,
   Dcc4PcReplyTest.class,
   Dcc4PcMessageTest.class,
   Dcc4PcProgrammerManagerTest.class,
   Dcc4PcOpsModeProgrammerTest.class,
   Dcc4PcSensorTest.class,
   Dcc4PcSensorManagerTest.class,
   Dcc4PcReporterTest.class,
   Dcc4PcBoardManagerTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
