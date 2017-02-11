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
   Dcc4PcTrafficControllerTest.class
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
