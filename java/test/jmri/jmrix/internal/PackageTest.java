package jmri.jmrix.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.internal.configurexml.PackageTest.class,
   InternalReporterManagerTest.class,
   InternalTurnoutManagerTest.class,
   InternalSensorManagerTest.class,
   InternalLightManagerTest.class,
   InternalAdapterTest.class,
   InternalConnectionTypeListTest.class,
   InternalSystemConnectionMemoTest.class,
   BundleTest.class,
   InternalConsistManagerTest.class,
   InternalConsistManagerOpsModeTest.class,
})
/**
 * Tests for the jmri.jmrix.internal package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
