package jmri.jmrit.display.controlPanelEditor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrit.display.controlPanelEditor.shape.PackageTest.class,
    BundleTest.class,
    jmri.jmrit.display.controlPanelEditor.configurexml.PackageTest.class,
    ControlPanelEditorTest.class,
    ControlPanelEditorActionTest.class,
    PortalIconTest.class,
    CircuitBuilderTest.class
})

/**
 * Tests for the jmrit.display.controlPanelEditor package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010, 2015
 * @author	Paul Bender Copyright 2017
 */
public class PackageTest {

}
