package jmri.jmrix.dcc4pc.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.dcc4pc.swing.boardlists.PackageTest.class,
   jmri.jmrix.dcc4pc.swing.monitor.PackageTest.class,
   jmri.jmrix.dcc4pc.swing.packetgen.PackageTest.class,
   Dcc4PcMenuTest.class,
   Dcc4PcComponentFactoryTest.class
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
