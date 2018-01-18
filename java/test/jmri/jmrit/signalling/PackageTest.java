package jmri.jmrit.signalling;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        SignallingActionTest.class,
        SignallingFrameActionTest.class,
        SignallingFrameTest.class,
        SignallingSourceActionTest.class,
        SignallingSourceFrameTest.class,
        SignallingGuiToolsTest.class,
        SignallingSourcePanelTest.class,
        SignallingPanelTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.signalling tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest  {
}
