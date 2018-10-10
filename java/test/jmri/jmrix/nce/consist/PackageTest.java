package jmri.jmrix.nce.consist;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    NceConsistEditPanelTest.class,
    NceConsistRosterTest.class,
    NceConsistRosterEntryTest.class,
    NceConsistBackupTest.class,
    NceConsistRestoreTest.class,
    NceConsistRosterMenuTest.class,
    PrintNceConsistRosterActionTest.class,
    BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.nce.consist package
 *
 * @author      Paul Bender Copyright (C) 2017
 */
public class PackageTest {


}
