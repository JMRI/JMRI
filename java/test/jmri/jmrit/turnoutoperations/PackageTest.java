package jmri.jmrit.turnoutoperations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   CommonTurnoutOperationConfigTest.class,
   NoFeedbackTurnoutOperationConfigTest.class,
   RawTurnoutOperationConfigTest.class,
   SensorTurnoutOperationConfigTest.class,
   TurnoutOperationConfigTest.class,
   TurnoutOperationFrameTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.turnoutoperations tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 * @author      Paul Bender Copyright (C) 2017
 */
public class PackageTest {
}
