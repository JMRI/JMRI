package jmri.jmrit.dualdecoder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        DualDecoderSelectFrameTest.class,
        DualDecoderSelectPaneTest.class,
        DualDecoderToolActionTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.dualdecoder tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
