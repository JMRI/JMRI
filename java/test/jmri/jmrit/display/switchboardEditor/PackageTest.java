package jmri.jmrit.display.switchboardEditor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmrit.display.switchboardEditor package
 *
 * @author	Egbert Broerse Copyright 2017
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrit.display.switchboardEditor.configurexml.PackageTest.class,
        SwitchboardEditorTest.class,
        SwitchboardEditorActionTest.class,
        SchemaTest.class,
        BeanSwitchTest.class
})

public class PackageTest {
}

