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
   SigletTest.class
})
/**
 * Tests for the jmri.jmrit.automat.monitor package
 *
 * @author      Paul Bender Copyright (C) 2017
 */
public class PackageTest {


}
