package jmri.jmrit.ctc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrit.ctc package
 *
 * @author Dave Sand Copyright (C) 2018
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    CtcRunActionTest.class,
    CtcRunStartupTest.class,
    CtcRunTest.class,
    jmri.jmrit.ctc.editor.PackageTest.class
})
public class PackageTest{
}