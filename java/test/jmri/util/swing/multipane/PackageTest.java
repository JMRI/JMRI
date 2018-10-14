package jmri.util.swing.multipane;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultiJfcUnitTest.class,
        MultiPaneWindowTest.class,
        PanedInterfaceTest.class,
        ThreePaneTLRWindowTest.class,
})

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PackageTest  {
}
