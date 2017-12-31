package jmri.jmrit.jython;

import jmri.util.JUnitUtil;



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        JythonWindowsTest.class,
        SampleScriptTest.class,
        InputWindowActionTest.class,
        InputWindowTest.class,
        JynstrumentFactoryTest.class,
        JythonWindowTest.class,
        JynstrumentTest.class,
        JynstrumentPopupMenuTest.class,
        RunJythonScriptTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.jython tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
