package jmri.jmrit.operations.automation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AutomationManagerTest.class,
        AutomationItemTest.class,
        BundleTest.class,
        jmri.jmrit.operations.automation.actions.PackageTest.class,
        AutomationTableFrameTest.class,
        AutomationCopyFrameTest.class,
        AutomationCopyActionTest.class,
        AutomationTableModelTest.class,
        AutomationsTableFrameActionTest.class,
        AutomationsTableFrameTest.class,
        AutomationsTableFrameTest.class,
        AutomationsTableModelTest.class,
        XmlTest.class, 
        AutomationTest.class,
        AutomationResetActionTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.operations.automations tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
