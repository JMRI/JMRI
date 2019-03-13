package jmri.jmrix.loconet.pr2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.pr2.configurexml.PackageTest.class,
   LnPr2PacketizerTest.class,
   PR2AdapterTest.class,
   PR2SystemConnectionMemoTest.class,
   LnPr2PowerManagerTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.pr2 package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
