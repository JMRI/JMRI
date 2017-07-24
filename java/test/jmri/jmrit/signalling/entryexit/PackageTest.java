package jmri.jmrit.signalling.entryexit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   StackNXPanelTest.class,
   PointDetailsTest.class,
   ManuallySetRouteTest.class,
   SourceTest.class,
   DestinationPointsTest.class,

})
/**
 * Invokes complete set of tests in the jmri.jmrit.signalling.entryexit tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
