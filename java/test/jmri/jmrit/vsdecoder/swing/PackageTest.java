package jmri.jmrit.vsdecoder.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    DieselPaneTest.class,
    VSDecoderPreferencesPaneTest.class,
    ManageLocationsActionTest.class,
    VSDPreferencesActionTest.class,
    VSDControlTest.class,
    VSDManagerFrameTest.class,
    VSDOptionsDialogTest.class,
    VSDConfigDialogTest.class,
    ManageLocationsFrameTest.class
})


/**
 * Invokes complete set of tests in the jmri.jmrit.vsdecoder.swing tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
