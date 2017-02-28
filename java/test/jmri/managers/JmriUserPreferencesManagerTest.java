package jmri.managers;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.util.JUnitAppender;
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
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isSaveAllowed());
        m.allowSave();
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testDisallowSave() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        Assert.assertTrue(m.isSaveAllowed());
        m.disallowSave();
        Assert.assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testSetSaveAllowed() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testIsSaveAllowed() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        Assert.assertTrue(m.isSaveAllowed());
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testGetScreen() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(Toolkit.getDefaultToolkit().getScreenSize(), (new JmriUserPreferencesManager()).getScreen());
    }

    @Test
    public void testSetSimplePreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();

        Assert.assertFalse(m.getSimplePreferenceState("one"));

        m.setSimplePreferenceState("one", true);
        Assert.assertTrue(m.getSimplePreferenceState("one"));
        Assert.assertFalse(m.getSimplePreferenceState("two"));

        m.setSimplePreferenceState("one", false);
        Assert.assertFalse(m.getSimplePreferenceState("one"));
        Assert.assertFalse(m.getSimplePreferenceState("two"));
    }

    @Test
    public void testGetSimplePreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();

        Assert.assertFalse(m.getSimplePreferenceState("one"));

        m.setSimplePreferenceState("one", true);
        Assert.assertTrue(m.getSimplePreferenceState("one"));
        Assert.assertFalse(m.getSimplePreferenceState("two"));

        m.setSimplePreferenceState("one", false);
        Assert.assertFalse(m.getSimplePreferenceState("one"));
        Assert.assertFalse(m.getSimplePreferenceState("two"));
    }

    @Test
    public void testGetSimplePreferenceStateList() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        // defaults to empty
        Assert.assertEquals(0, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test1", true);
        // setting a preference to true adds it
        Assert.assertEquals(1, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test1", false);
        // setting a preference to false removes it
        Assert.assertEquals(0, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test1", true);
        m.setSimplePreferenceState("test2", true);
        // setting a preference to true adds it
        Assert.assertEquals(2, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test2", true);
        // setting a preference to true twice adds it once
        Assert.assertEquals(2, m.getSimplePreferenceStateList().size());
    }

    @Test
    public void testSetPreferenceState() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();

        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test1"));

        m.setPreferenceState(this.getClass().getName(), "test1", true);
        Assert.assertTrue(m.getPreferenceState(this.getClass().getName(), "test1"));
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test2"));

        m.setPreferenceState(this.getClass().getName(), "test1", false);
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test1"));
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test2"));

        // non-existant class should not cause an error
        m.setPreferenceState("non.existant.class", "test1", true);
        JUnitAppender.assertWarnMessage("class name \"non.existant.class\" cannot be found, perhaps an expected plugin is missing?");
    }

    @Test
    public void testGetPreferenceState() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();

        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test1"));

        m.setPreferenceState(this.getClass().getName(), "test1", true);
        Assert.assertTrue(m.getPreferenceState(this.getClass().getName(), "test1"));
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test2"));

        m.setPreferenceState(this.getClass().getName(), "test1", false);
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test1"));
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test2"));
    }

    @Test
    public void testSetPreferenceItemDetails() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        Assert.assertNull(m.getPreferenceItemDescription(this.getClass().getName(), "test1"));
        m.setPreferenceItemDetails(this.getClass().getName(), "test1", "description1");
        Assert.assertEquals("description1", m.getPreferenceItemDescription(this.getClass().getName(), "test1"));
        m.setPreferenceItemDetails(this.getClass().getName(), "test1", null);
        Assert.assertNull(m.getPreferenceItemDescription(this.getClass().getName(), "test1"));
    }

    @Test
    public void testGetPreferenceList() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();

        Assert.assertTrue(m.getPreferenceList(this.getClass().getName()).isEmpty());

        m.setPreferenceState(this.getClass().getName(), "test1", true);
        Assert.assertTrue(m.getPreferenceState(this.getClass().getName(), "test1"));
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test2"));

        m.setPreferenceState(this.getClass().getName(), "test1", false);
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test1"));
        Assert.assertFalse(m.getPreferenceState(this.getClass().getName(), "test2"));
    }

    @Test
    public void testGetPreferenceItemName() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();

        Assert.assertTrue(m.getPreferenceList(this.getClass().getName()).isEmpty());

        Assert.assertNull(m.getPreferenceItemName(this.getClass().getName(), 0));

        m.setPreferenceState(this.getClass().getName(), "test1", true);
        Assert.assertEquals("test1", m.getPreferenceItemName(this.getClass().getName(), 0));
        Assert.assertNull("test2", m.getPreferenceItemName(this.getClass().getName(), 1));

        m.setPreferenceState(this.getClass().getName(), "test1", false);
        Assert.assertEquals("test1", m.getPreferenceItemName(this.getClass().getName(), 0));
        Assert.assertNull("test2", m.getPreferenceItemName(this.getClass().getName(), 1));
    }

    @Test
    public void testGetPreferenceItemDescription() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        Assert.assertNull(m.getPreferenceItemDescription(this.getClass().getName(), "test1"));
        m.setPreferenceItemDetails(this.getClass().getName(), "test1", "description1");
        Assert.assertEquals("description1", m.getPreferenceItemDescription(this.getClass().getName(), "test1"));
        m.setPreferenceItemDetails(this.getClass().getName(), "test1", null);
        Assert.assertNull(m.getPreferenceItemDescription(this.getClass().getName(), "test1"));
    }

    @Test
    public void testSetSessionPreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        Assert.assertFalse(m.getSessionPreferenceState("test1"));
        Assert.assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", true);
        Assert.assertTrue(m.getSessionPreferenceState("test1"));
        Assert.assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", false);
        Assert.assertFalse(m.getSessionPreferenceState("test1"));
        Assert.assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test2", true);
        Assert.assertFalse(m.getSessionPreferenceState("test1"));
        Assert.assertTrue(m.getSessionPreferenceState("test2"));
    }

    @Test
    public void testGetSessionPreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        Assert.assertFalse(m.getSessionPreferenceState("test1"));
        Assert.assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", true);
        Assert.assertTrue(m.getSessionPreferenceState("test1"));
        Assert.assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", false);
        Assert.assertFalse(m.getSessionPreferenceState("test1"));
        Assert.assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test2", true);
        Assert.assertFalse(m.getSessionPreferenceState("test1"));
        Assert.assertTrue(m.getSessionPreferenceState("test2"));
    }

    @Test
    public void testShowInfoMessage_4args() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        Assert.assertNull(m.title);
        Assert.assertNull(m.message);
        Assert.assertNull(m.strClass);
        Assert.assertNull(m.item);
        Assert.assertNull(m.alwaysRemember);
        Assert.assertNull(m.sessionOnly);
        m.showInfoMessage("title1", "message1", this.getClass().getName(), "item1");
        Assert.assertEquals("title1", m.title);
        Assert.assertEquals("message1", m.message);
        Assert.assertEquals(this.getClass().getName(), m.strClass);
        Assert.assertEquals("item1", m.item);
        Assert.assertTrue(m.alwaysRemember);
        Assert.assertFalse(m.sessionOnly);
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
        UserPreferencesManager m = new JmriUserPreferencesManager();

        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(TestUserPreferencesManager.class.toString(), windowLocation);
        Point savedWindowLocation = m.getWindowLocation(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testGetWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();

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
        UserPreferencesManager m = new JmriUserPreferencesManager();

        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(TestUserPreferencesManager.class.toString(), windowLocation);
        Point savedWindowLocation = m.getWindowLocation(TestUserPreferencesManager.class.toString());
        Assert.assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testSetWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();

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

        public String title = null;
        public String message = null;
        public String strClass = null;
        public String item = null;
        // Boolean is nullable unlike boolean, null indicates showMessage not called
        public Boolean sessionOnly = null;
        public Boolean alwaysRemember = null;
        public int type = -1;

        @Override
        protected void showMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
            this.title = title;
            this.message = message;
            this.strClass = strClass;
            this.item = item;
            this.sessionOnly = sessionOnly;
            this.alwaysRemember = alwaysRemember;
            this.type = type;
            // Uncomment to force failure if wanting to verify that showMessage does not get called.
            //org.slf4j.LoggerFactory.getLogger(TestUserPreferencesManager.class).error("showMessage called.", new Exception());
        }
    }
}
