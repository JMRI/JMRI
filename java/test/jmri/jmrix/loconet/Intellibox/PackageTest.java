package jmri.jmrix.loconet.Intellibox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.Intellibox.configurexml.PackageTest.class,
   IBLnPacketizerTest.class,
   IbxConnectionTypeListTest.class,
   IntelliboxAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.Intellibox package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
