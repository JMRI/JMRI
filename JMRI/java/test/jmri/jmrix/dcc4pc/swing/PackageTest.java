package jmri.jmrix.dcc4pc.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.dcc4pc.swing.boardlists.PackageTest.class,
   jmri.jmrix.dcc4pc.swing.monitor.PackageTest.class,
   jmri.jmrix.dcc4pc.swing.packetgen.PackageTest.class,
   Dcc4PcMenuTest.class,
   Dcc4PcComponentFactoryTest.class,
   StatusPanelTest.class,
   Dcc4PcNamedPaneActionTest.class
})
/**
 * Tests for the jmri.jmrix.pi package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
