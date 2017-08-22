package jmri.profile;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ProfileTest.class,
    ProfileUtilsTest.class,
    BundleTest.class,
    ProfileListCellRendererTest.class,
    ProfileFileFilterTest.class,
    ProfileFileViewTest.class,
    ProfileListModelTest.class,
    ProfileManagerTest.class,
    ProfilePreferencesPanelTest.class,
    ProfileTableModelTest.class,
    SearchPathTableModelTest.class,
    ProfileConfigurationTest.class,
    ProfilePropertiesTest.class,
    NullProfileTest.class,
    AddProfileDialogTest.class,
    ProfileManagerDialogTest.class
})
public class PackageTest {
}
