package apps.startup;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   apps.startup.configurexml.PackageTest.class,
   BundleTest.class,
   AbstractStartupModelTest.class,
   StartupPauseModelTest.class,
   AbstractActionModelTest.class,
   CreateButtonModelFactoryTest.class,
   PerformActionModelFactoryTest.class,
   PerformFileModelFactoryTest.class,
   PerformScriptModelFactoryTest.class,
   ScriptButtonModelFactoryTest.class,
   ScriptButtonModelTest.class,
   StartupActionModelUtilTest.class,
   StartupActionsPreferencesPanelTest.class,
   StartupModelCellEditorTest.class,
   StartupModelCellRendererTest.class,
   StartupPauseFactoryTest.class,
   TriggerRouteModelFactoryTest.class,
   TriggerRouteModelTest.class,
   ScriptButtonPanelTest.class
})
/**
 * Tests for the apps.startup.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
