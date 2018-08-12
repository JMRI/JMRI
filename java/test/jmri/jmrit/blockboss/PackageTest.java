package jmri.jmrit.blockboss;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BlockBossLogicTest.class,
        BundleTest.class,
        jmri.jmrit.blockboss.configurexml.PackageTest.class,
        BlockBossActionTest.class,
        BlockBossFrameTest.class,
})

/**
 * Tests for the jmrit.blockboss package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest {
}
