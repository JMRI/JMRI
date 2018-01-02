package jmri.jmris.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        JsonServerTest.class,
        BundleTest.class,
        JsonServerActionTest.class,
        JsonServerPreferencesPanelTest.class,
        JsonServerPreferencesTest.class,
        JsonProgrammerServerTest.class,
})

/**
 * Tests for the jmri.jmris.json package
 *
 * @author Paul Bender
 */
public class PackageTest {
}
