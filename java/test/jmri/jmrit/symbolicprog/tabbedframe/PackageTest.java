package jmri.jmrit.symbolicprog.tabbedframe;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PaneProgPaneTest.class,
        PaneProgFrameTest.class,
        CheckProgrammerNamesTest.class,
        SchemaTest.class,
        QualifiedVarTest.class,
        PaneEditActionTest.class,
        PaneNewProgActionTest.class,
        PaneOpsProgActionTest.class,
        PaneProgActionTest.class,
        BundleTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit.symbolicprog.tabbedframe tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 * 
 */
public class PackageTest { 
}
