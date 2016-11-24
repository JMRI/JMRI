package jmri.jmrix.dcc4pc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.dcc4pc.serialdriver.PackageTest.class,
   jmri.jmrix.dcc4pc.configurexml.PackageTest.class,
   jmri.jmrix.dcc4pc.swing.PackageTest.class,
   Dcc4PcReporterManagerTest.class
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
