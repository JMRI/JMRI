package jmri.jmrit.automat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   AbstractAutomatonTest.class,
   AutomatTest.class,
   jmri.jmrit.automat.monitor.PackageTest.class,
   SampleAutomaton2Test.class,
   SampleAutomaton3Test.class,
   SampleAutomatonTest.class,
   SigletTest.class,
   SampleAutomaton2ActionTest.class,
   SampleAutomaton3ActionTest.class,
   SampleAutomatonActionTest.class,
   JythonAutomatonTest.class,
   JythonAutomatonActionTest.class,
   JythonSigletTest.class,
   JythonSigletActionTest.class,
   AutomatSummaryTest.class
})
/**
 * Tests for the jmri.jmrit.automat package
 *
 * @author      Paul Bender Copyright (C) 2017
 */
public class PackageTest {


}
