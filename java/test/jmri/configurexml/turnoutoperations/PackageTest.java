package jmri.configurexml.turnoutoperations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   NoFeedbackTurnoutOperationXmlTest.class,
   RawTurnoutOperationXmlTest.class,
   SensorTurnoutOperationXmlTest.class,
})
/**
 * Tests for the jmri.configurexml.turnoutoperations package
 *
 * @author      Paul Bender Copyright (C) 2017
 */
public class PackageTest {
}
