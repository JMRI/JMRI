package jmri.jmrit.ctc.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrit.ctc.editor package
 *
 * @author Dave Sand Copyright (C) 2018
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    CtcEditorActionTest.class,
    CtcEditorStartupTest.class,
    CtcEditorTest.class
})
public class PackageTest{
}