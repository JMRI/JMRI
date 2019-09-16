package jmri.jmrix.nce.macro;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    NceMacroEditPanelTest.class,
    NceMacroGenPanelTest.class,
    NceMacroBackupTest.class,
    NceMacroRestoreTest.class,
    BundleTest.class
})

/**
 * Tests for the jmri.jmrix.nce.macro package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
