package apps;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    apps.DecoderPro.PackageTest.class,
    apps.DispatcherPro.PackageTest.class,
    apps.PanelPro.PackageTest.class,
    apps.SoundPro.PackageTest.class,
    apps.TrainCrew.PackageTest.class,

    BundleTest.class,
    ConfigBundleTest.class,
    ValidateConfigFilesTest.class,
    apps.configurexml.PackageTest.class,
    apps.startup.PackageTest.class,
    apps.InstallTest.PackageTest.class,
    apps.gui3.PackageTest.class,
    JmriFacelessTest.class,
    apps.systemconsole.PackageTest.class,

	AboutActionTest.class,
	AppConfigBaseTest.class,
	AppsTest.class,
	CheckForUpdateActionTest.class,
	CreateButtonModelTest.class,
	FileLocationPaneTest.class,
	GuiLafConfigPaneTest.class,
	GuiLocalePreferencesPanelTest.class,
	LicenseActionTest.class,
	ManagerDefaultsConfigPaneTest.class,
	PerformActionModelTest.class,
	PerformFileModelTest.class,
	PerformScriptModelTest.class,
	ReportContextActionTest.class,
	RestartActionTest.class,
	RestartStartupActionFactoryTest.class,
	SplashWindowTest.class,
	StartupActionsManagerTest.class,
	SystemConsoleActionTest.class,
	SystemConsoleConfigPanelTest.class,
    apps.gui.PackageTest.class,
    SampleMinimalProgramTest.class,
    SystemConsoleTest.class,
    AppsLaunchFrameTest.class,
    RunCucumberTest.class,
})
/**
 * Invoke complete set of tests for the apps package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007, 2012
 * @author      Paul Bender, Copyright (C) 2016
 */
public class PackageTest {

}
