package jmri.jmrit.sensorgroup;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   SensorGroupActionTest.class,
   SensorGroupFrameTest.class,
   SensorGroupTest.class,
   SensorTableModelTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit.sensorgroup tree
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class PackageTest {
}
