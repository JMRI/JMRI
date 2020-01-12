package jmri.jmrix.ecos.utilities;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    RosterToEcosTest.class,
    RemoveObjectFromEcosTest.class,
    GetEcosObjectNumberTest.class,
    EcosLocoToRosterTest.class,
    BundleTest.class,
    AddRosterEntryToEcosTest.class
})
/**
 * Tests for the jmri.jmrix.ecos.utilities package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
