package jmri.jmrit.display.panelEditor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmrit.display.panelEditor package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010, 2015
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrit.display.panelEditor.configurexml.PackageTest.class,
        PanelEditorActionTest.class,
        PanelEditorTest.class,
})
public class PackageTest {
}
