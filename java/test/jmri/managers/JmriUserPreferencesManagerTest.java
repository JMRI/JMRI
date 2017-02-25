package jmri.managers;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.managers.JmriUserPreferencesManagerTest class.
 *
 * @author Bob Jacobsen Copyright 2009
 * @author Randall Wood Copyright 2017
 */
public class JmriUserPreferencesManagerTest {

    private final static Logger log = LoggerFactory.getLogger(JmriUserPreferencesManagerTest.class);

    @Test
    public void testGetInstance() {
        Assert.assertNull(InstanceManager.getNullableDefault(UserPreferencesManager.class));
        Assert.assertNotNull(JmriUserPreferencesManager.getInstance());
        Assert.assertEquals(InstanceManager.getDefault(UserPreferencesManager.class), JmriUserPreferencesManager.getInstance());
        Assert.assertEquals(JmriUserPreferencesManager.getDefault(), JmriUserPreferencesManager.getInstance());
    }

    @Test
    public void testGetDefault() {
        Assert.assertNull(InstanceManager.getNullableDefault(UserPreferencesManager.class));
        Assert.assertNotNull(JmriUserPreferencesManager.getDefault());
        Assert.assertEquals(InstanceManager.getDefault(UserPreferencesManager.class), JmriUserPreferencesManager.getDefault());
    }

    @Test
    public void testAllowSave() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.disallowSave();
        Assert.assertFalse(m.isSaveAllowed());
        m.allowSave();
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testDisallowSave() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        Assert.assertTrue(m.isSaveAllowed());
        m.disallowSave();
        Assert.assertFalse(m.isSaveAllowed());
        m.allowSave();
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testSetSaveAllowed() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testIsSaveAllowed() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        Assert.assertTrue(m.isSaveAllowed());
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testGetScreen() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(Toolkit.getDefaultToolkit().getScreenSize(), (new TestJmriUserPreferencesManager()).getScreen());
    }

    @Test
    public void testSetSimplePreferenceState() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();

        Assert.assertTrue(!m.getSimplePreferenceState("one"));

        m.setSimplePreferenceState("one", true);
        Assert.assertTrue(m.getSimplePreferenceState("one"));
        Assert.assertTrue(!m.getSimplePreferenceState("two"));

        m.setSimplePreferenceState("one", false);
        Assert.assertTrue(!m.getSimplePreferenceState("one"));
        Assert.assertTrue(!m.getSimplePreferenceState("two"));
    }

    @Test
    public void testGetSimplePreferenceState() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();

        Assert.assertTrue(!m.getSimplePreferenceState("one"));

        m.setSimplePreferenceState("one", true);
        Assert.assertTrue(m.getSimplePreferenceState("one"));
        Assert.assertTrue(!m.getSimplePreferenceState("two"));

        m.setSimplePreferenceState("one", false);
        Assert.assertTrue(!m.getSimplePreferenceState("one"));
        Assert.assertTrue(!m.getSimplePreferenceState("two"));
    }

    @Test
    public void testGetSimplePreferenceStateList() {
    }

    @Test
    public void testSetPreferenceState() {
    }

    @Test
    public void testGetPreferenceState() {
    }

    @Test
    public void testPreferenceItemDetails() {
    }

    @Test
    public void testGetPreferenceList() {
    }

    @Test
    public void testGetPreferenceItemName() {
    }

    @Test
    public void testGetPreferenceItemDescription() {
    }

    @Test
    public void testSetSessionPreferenceState() {
    }

    @Test
    public void testGetSessionPreferenceState() {
    }

    @Test
    public void testShowInfoMessage_4args() {
    }

    @Test
    public void testShowErrorMessage() {
    }

    @Test
    public void testShowInfoMessage_6args() {
    }

    @Test
    public void testShowWarningMessage() {
    }

    @Test
    public void testShowMessage() {
    }

    @Test
    public void testAddComboBoxLastSelection() {
    }

    @Test
    public void testGetComboBoxLastSelection_String() {
    }

    @Test
    public void testSetComboBoxLastSelection() {
    }

    @Test
    public void testGetComboBoxSelectionSize() {
    }

    @Test
    public void testGetComboBoxName() {
    }

    @Test
    public void testGetComboBoxLastSelection_int() {
    }

    @Test
    public void testGetChangeMade() {
    }

    @Test
    public void testSetChangeMade() {
    }

    @Test
    public void testResetChangeMade() {
    }

    @Test
    public void testSetLoading() {
    }

    @Test
    public void testFinishLoading() {
    }

    @Test
    public void testDisplayRememberMsg() {
    }

    @Test
    public void testGetWindowLocation() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();

        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(TestUserPreferencesManager.class.toString(), windowLocation);
        Point savedWindowLocation = m.getWindowLocation(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testGetWindowSize() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();

        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(TestUserPreferencesManager.class.toString(), windowSize);
        Dimension savedWindowSize = m.getWindowSize(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowSize, savedWindowSize);
    }

    @Test
    public void testGetSaveWindowSize() {
    }

    @Test
    public void testGetSaveWindowLocation() {
    }

    @Test
    public void testSetSaveWindowSize() {
    }

    @Test
    public void testSetSaveWindowLocation() {
    }

    @Test
    public void testSetWindowLocation() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();

        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(TestUserPreferencesManager.class.toString(), windowLocation);
        Point savedWindowLocation = m.getWindowLocation(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testSetWindowSize() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();

        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(TestUserPreferencesManager.class.toString(), windowSize);
        Dimension savedWindowSize = m.getWindowSize(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowSize, savedWindowSize);
    }

    @Test
    public void testGetWindowList() {
    }

    @Test
    public void testSetProperty() {
    }

    @Test
    public void testGetProperty() {
    }

    @Test
    public void testGetPropertyKeys() {
    }

    @Test
    public void testIsWindowPositionSaved() {
    }

    @Test
    public void testGetClassDescription_String() {
    }

    @Test
    public void testGetPreferencesClasses() {
    }

    @Test
    public void testSetClassDescription() {
    }

    @Test
    public void testMessageItemDetails_6args() {
    }

    @Test
    public void testMessageItemDetails_5args() {
    }

    @Test
    public void testGetChoiceOptions() {
    }

    @Test
    public void testGetMultipleChoiceSize() {
    }

    @Test
    public void testGetMultipleChoiceList() {
    }

    @Test
    public void testGetChoiceName() {
    }

    @Test
    public void testGetChoiceDescription() {
    }

    @Test
    public void testGetMultipleChoiceOption() {
    }

    @Test
    public void testGetMultipleChoiceDefaultOption() {
    }

    @Test
    public void testSetMultipleChoiceOption_3args_1() {
    }

    @Test
    public void testSetMultipleChoiceOption_3args_2() {
    }

    @Test
    public void testSetTableColumnPreferences() {
    }

    @Test
    public void testGetTableColumnOrder() {
    }

    @Test
    public void testGetTableColumnWidth() {
    }

    @Test
    public void testGetTableColumnSort() {
    }

    @Test
    public void testGetTableColumnHidden() {
    }

    @Test
    public void testGetTableColumnAtNum() {
    }

    @Test
    public void testGetTablesList() {
    }

    @Test
    public void testGetTablesColumnList() {
    }

    @Test
    public void testGetClassDescription_0args() {
    }

    @Test
    public void testGetClassName() {
    }

    @Test
    public void testGetClassPreferences() {
    }

    @Test
    public void testGetPreferencesSize() {
    }

    @Test
    public void testReadUserPreferences() {
    }

    @Test
    public void testSaveElement() {
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        System.setProperty("org.jmri.Apps.configFilename", "jmriconfig2.xml");
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    private static class TestJmriUserPreferencesManager extends JmriUserPreferencesManager {

        @Override
        protected void showMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
            // Uncomment to force failure if wanting to verify that showMessage does not get called.
            //org.slf4j.LoggerFactory.getLogger(TestUserPreferencesManager.class).error("showMessage called.", new Exception());
        }
    }
}
