package jmri.jmrit.speedometer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
      BundleTest.class,
      SpeedometerActionTest.class,
      SpeedometerFrameTest.class
})
/**
 * Invokes complete set of tests in the jmri.jmrit.speedometer tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
