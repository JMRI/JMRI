package jmri.jmrit.operations.rollingstock.engines.tools;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({     
        DeleteEngineRosterActionTest.class,
        EngineAttributeEditFrameTest.class,
        ExportEngineRosterActionTest.class,
        ExportEnginesTest.class,
        ImportEngineActionTest.class,
        ImportEnginesTest.class,
        ImportRosterEngineActionTest.class,
        ImportRosterEnginesTest.class,
        NceConsistEngineActionTest.class,
        NceConsistEnginesTest.class,
        PrintEngineRosterActionTest.class,
        ResetEngineMovesActionTest.class
})

/**
 * Tests for the jmrit.operations.rollingstock package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
