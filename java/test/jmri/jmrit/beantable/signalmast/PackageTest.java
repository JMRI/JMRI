package jmri.jmrit.beantable.signalmast;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for classes in the jmri.jmrit.beantable.signalmast package
 *
 * @author	Bob Jacobsen Copyright 2014
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    AddSignalMastPanelTest.class,
    AddSignalMastJFrameTest.class,
    SignalMastRepeaterJFrameTest.class,
    SignalMastRepeaterPanelTest.class,
    SignalMastTableDataModelTest.class
})
public class PackageTest {
}
