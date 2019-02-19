package jmri.jmrit.ctc.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrit.ctc.editor package
 *
 * @author Dave Sand Copyright (C) 2019
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    CtcEditorActionTest.class,
    CtcEditorStartupTest.class,
    jmri.jmrit.ctc.editor.code.PackageTest.class,
    jmri.jmrit.ctc.editor.gui.PackageTest.class
})
public class PackageTest{
}