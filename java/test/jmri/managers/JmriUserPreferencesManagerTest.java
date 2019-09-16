package jmri.managers;

import apps.AppConfigBase;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.managers.JmriUserPreferencesManager class.
 *
 * @author Bob Jacobsen Copyright 2009
 * @author Randall Wood Copyright 2017
 */
public class JmriUserPreferencesManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final static Logger log = LoggerFactory.getLogger(JmriUserPreferencesManagerTest.class);
    private final String strClass = JmriUserPreferencesManagerTest.class.getName();

    @Test
    public void testGetInstance() {
        Assert.assertFalse(InstanceManager.containsDefault(UserPreferencesManager.class));
        Assert.assertNotNull(JmriUserPreferencesManager.getInstance());
        Assert.assertEquals(InstanceManager.getDefault(UserPreferencesManager.class), JmriUserPreferencesManager.getInstance());
        Assert.assertEquals(JmriUserPreferencesManager.getDefault(), JmriUserPreferencesManager.getInstance());
    }

    @Test
    public void testGetDefault() {
        Assert.assertFalse(InstanceManager.containsDefault(UserPreferencesManager.class));
        Assert.assertNotNull(JmriUserPreferencesManager.getDefault());
        Assert.assertEquals(InstanceManager.getDefault(UserPreferencesManager.class), JmriUserPreferencesManager.getDefault());
    }

    @Test
    public void testAllowSave() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        Assert.assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testDisallowSave() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        Assert.assertTrue(m.isSaveAllowed());
        m.setSaveAllowed(false);
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
        m.setSaveAllowed(false);

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
        m.setSaveAllowed(false);

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
        m.setSaveAllowed(false);
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
        m.setSaveAllowed(false);

        Assert.assertFalse(m.getPreferenceState(strClass, "test1"));

        m.setPreferenceState(strClass, "test1", true);
        Assert.assertTrue(m.getPreferenceState(strClass, "test1"));
        Assert.assertFalse(m.getPreferenceState(strClass, "test2"));

        m.setPreferenceState(strClass, "test1", false);
        Assert.assertFalse(m.getPreferenceState(strClass, "test1"));
        Assert.assertFalse(m.getPreferenceState(strClass, "test2"));

        // non-existant class should not cause an error
        m.setPreferenceState("non.existant.class", "test1", true);
        JUnitAppender.assertWarnMessage("class name \"non.existant.class\" cannot be found, perhaps an expected plugin is missing?");
    }

    @Test
    public void testGetPreferenceState() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);

        Assert.assertFalse(m.getPreferenceState(strClass, "test1"));

        m.setPreferenceState(strClass, "test1", true);
        Assert.assertTrue(m.getPreferenceState(strClass, "test1"));
        Assert.assertFalse(m.getPreferenceState(strClass, "test2"));

        m.setPreferenceState(strClass, "test1", false);
        Assert.assertFalse(m.getPreferenceState(strClass, "test1"));
        Assert.assertFalse(m.getPreferenceState(strClass, "test2"));
    }

    @Test
    public void testSetPreferenceItemDetails() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", "description1");
        Assert.assertEquals("description1", m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", null);
        Assert.assertNull(m.getPreferenceItemDescription(strClass, "test1"));
    }

    @Test
    public void testGetPreferenceList() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);

        Assert.assertTrue(m.getPreferenceList(strClass).isEmpty());

        m.setPreferenceState(strClass, "test1", true);
        Assert.assertTrue(m.getPreferenceState(strClass, "test1"));
        Assert.assertFalse(m.getPreferenceState(strClass, "test2"));

        m.setPreferenceState(strClass, "test1", false);
        Assert.assertFalse(m.getPreferenceState(strClass, "test1"));
        Assert.assertFalse(m.getPreferenceState(strClass, "test2"));
    }

    @Test
    public void testGetPreferenceItemName() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);

        Assert.assertTrue(m.getPreferenceList(strClass).isEmpty());

        Assert.assertNull(m.getPreferenceItemName(strClass, 0));

        m.setPreferenceState(strClass, "test1", true);
        Assert.assertEquals("test1", m.getPreferenceItemName(strClass, 0));
        Assert.assertNull("test2", m.getPreferenceItemName(strClass, 1));

        m.setPreferenceState(strClass, "test1", false);
        Assert.assertEquals("test1", m.getPreferenceItemName(strClass, 0));
        Assert.assertNull("test2", m.getPreferenceItemName(strClass, 1));
    }

    @Test
    public void testGetPreferenceItemDescription() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", "description1");
        Assert.assertEquals("description1", m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", null);
        Assert.assertNull(m.getPreferenceItemDescription(strClass, "test1"));
    }

    @Test
    public void testSetSessionPreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
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
        m.setSaveAllowed(false);
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
        m.setSaveAllowed(false);
        Assert.assertNull(m.title);
        Assert.assertNull(m.message);
        Assert.assertNull(m.strClass);
        Assert.assertNull(m.item);
        Assert.assertNull(m.alwaysRemember);
        Assert.assertNull(m.sessionOnly);
        Assert.assertEquals(-1, m.type);
        m.showInfoMessage("title1", "message1", strClass, "item1");
        Assert.assertEquals("title1", m.title);
        Assert.assertEquals("message1", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item1", m.item);
        Assert.assertTrue(m.alwaysRemember);
        Assert.assertFalse(m.sessionOnly);
        Assert.assertEquals(JOptionPane.INFORMATION_MESSAGE, m.type);
    }

    @Test
    public void testShowErrorMessage() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.title);
        Assert.assertNull(m.message);
        Assert.assertNull(m.strClass);
        Assert.assertNull(m.item);
        Assert.assertNull(m.alwaysRemember);
        Assert.assertNull(m.sessionOnly);
        Assert.assertEquals(-1, m.type);
        m.showErrorMessage("title1", "message1", strClass, "item1", true, true);
        Assert.assertEquals("title1", m.title);
        Assert.assertEquals("message1", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item1", m.item);
        Assert.assertTrue(m.alwaysRemember);
        Assert.assertTrue(m.sessionOnly);
        Assert.assertEquals(JOptionPane.ERROR_MESSAGE, m.type);
        m.showErrorMessage("title2", "message2", strClass, "item2", false, false);
        Assert.assertEquals("title2", m.title);
        Assert.assertEquals("message2", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item2", m.item);
        Assert.assertFalse(m.alwaysRemember);
        Assert.assertFalse(m.sessionOnly);
        Assert.assertEquals(JOptionPane.ERROR_MESSAGE, m.type);
    }

    @Test
    public void testShowInfoMessage_6args() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.title);
        Assert.assertNull(m.message);
        Assert.assertNull(m.strClass);
        Assert.assertNull(m.item);
        Assert.assertNull(m.alwaysRemember);
        Assert.assertNull(m.sessionOnly);
        Assert.assertEquals(-1, m.type);
        m.showInfoMessage("title1", "message1", strClass, "item1", true, true);
        Assert.assertEquals("title1", m.title);
        Assert.assertEquals("message1", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item1", m.item);
        Assert.assertTrue(m.alwaysRemember);
        Assert.assertTrue(m.sessionOnly);
        Assert.assertEquals(JOptionPane.INFORMATION_MESSAGE, m.type);
        m.showInfoMessage("title2", "message2", strClass, "item2", false, false);
        Assert.assertEquals("title2", m.title);
        Assert.assertEquals("message2", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item2", m.item);
        Assert.assertFalse(m.alwaysRemember);
        Assert.assertFalse(m.sessionOnly);
        Assert.assertEquals(JOptionPane.INFORMATION_MESSAGE, m.type);
    }

    @Test
    public void testShowWarningMessage() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.title);
        Assert.assertNull(m.message);
        Assert.assertNull(m.strClass);
        Assert.assertNull(m.item);
        Assert.assertNull(m.alwaysRemember);
        Assert.assertNull(m.sessionOnly);
        Assert.assertEquals(-1, m.type);
        m.showWarningMessage("title1", "message1", strClass, "item1", true, true);
        Assert.assertEquals("title1", m.title);
        Assert.assertEquals("message1", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item1", m.item);
        Assert.assertTrue(m.alwaysRemember);
        Assert.assertTrue(m.sessionOnly);
        Assert.assertEquals(JOptionPane.WARNING_MESSAGE, m.type);
        m.showWarningMessage("title2", "message2", strClass, "item2", false, false);
        Assert.assertEquals("title2", m.title);
        Assert.assertEquals("message2", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item2", m.item);
        Assert.assertFalse(m.alwaysRemember);
        Assert.assertFalse(m.sessionOnly);
        Assert.assertEquals(JOptionPane.WARNING_MESSAGE, m.type);
    }

    @Test
    public void testShowMessage() {
        // TODO: Use Jemmy to test showing real message
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.title);
        Assert.assertNull(m.message);
        Assert.assertNull(m.strClass);
        Assert.assertNull(m.item);
        Assert.assertNull(m.alwaysRemember);
        Assert.assertNull(m.sessionOnly);
        Assert.assertEquals(-1, m.type);
        m.showMessage("title1", "message1", strClass, "item1", true, true, JOptionPane.INFORMATION_MESSAGE);
        Assert.assertEquals("title1", m.title);
        Assert.assertEquals("message1", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item1", m.item);
        Assert.assertTrue(m.alwaysRemember);
        Assert.assertTrue(m.sessionOnly);
        m.showMessage("title2", "message2", strClass, "item2", false, false, JOptionPane.INFORMATION_MESSAGE);
        Assert.assertEquals("title2", m.title);
        Assert.assertEquals("message2", m.message);
        Assert.assertEquals(strClass, m.strClass);
        Assert.assertEquals("item2", m.item);
        Assert.assertFalse(m.alwaysRemember);
        Assert.assertFalse(m.sessionOnly);
    }

    @Test
    public void testAddComboBoxLastSelection() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value1");
        Assert.assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value2");
        Assert.assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test2", "value1");
        Assert.assertEquals(2, m.getComboBoxLastSelection().size());
    }

    @Test
    public void testGetComboBoxLastSelection_String() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value2");
        m.setComboBoxLastSelection("test2", "value1");
        Assert.assertEquals("value2", m.getComboBoxLastSelection("test1"));
        Assert.assertEquals("value1", m.getComboBoxLastSelection("test2"));
        Assert.assertNull(m.getComboBoxLastSelection("test3"));
    }

    @Test
    public void testSetComboBoxLastSelection() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value1");
        Assert.assertEquals("value1", m.getComboBoxLastSelection("test1"));
        m.setComboBoxLastSelection("test1", "value2");
        Assert.assertEquals("value2", m.getComboBoxLastSelection("test1"));
        m.setComboBoxLastSelection("test2", "value1");
        Assert.assertEquals("value1", m.getComboBoxLastSelection("test2"));
        Assert.assertNull(m.getComboBoxLastSelection("test3"));
    }

    @Test
    public void testGetComboBoxSelectionSize() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value1");
        Assert.assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value2");
        Assert.assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test2", "value1");
        Assert.assertEquals(2, m.getComboBoxLastSelection().size());
    }

    @Test
    public void testGetChangeMade() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.getChangeMade());
        m.setChangeMade(false);
        Assert.assertTrue(m.getChangeMade());
        m.resetChangeMade();
        Assert.assertFalse(m.getChangeMade());
    }

    @Test
    public void testSetChangeMade() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Listener l = new Listener();
        m.addPropertyChangeListener(l);
        Assert.assertFalse(m.getChangeMade());
        m.setChangeMade(false);
        Assert.assertTrue(m.getChangeMade());
        Assert.assertNull(l.event);
        m.setChangeMade(true);
        JUnitUtil.waitFor(() -> {
            return l.event != null && l.event.getPropertyName().equals(UserPreferencesManager.PREFERENCES_UPDATED);
        }, "event change notification fired");
    }

    @Test
    public void testResetChangeMade() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.getChangeMade());
        m.setChangeMade(false);
        Assert.assertTrue(m.getChangeMade());
        m.resetChangeMade();
        Assert.assertFalse(m.getChangeMade());
        m.resetChangeMade();
        Assert.assertFalse(m.getChangeMade());
    }

    @Test
    public void testIsLoading() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isLoading());
        m.setLoading();
        Assert.assertTrue(m.isLoading());
        m.finishLoading();
        Assert.assertFalse(m.isLoading());
    }

    @Test
    public void testSetLoading() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isLoading());
        m.setLoading();
        Assert.assertTrue(m.isLoading());
        m.finishLoading();
        Assert.assertFalse(m.isLoading());
    }

    @Test
    public void testFinishLoading() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.isLoading());
        m.finishLoading();
        Assert.assertFalse(m.isLoading());
        m.setLoading();
        Assert.assertTrue(m.isLoading());
        m.finishLoading();
        Assert.assertFalse(m.isLoading());
    }

    @Test
    public void testDisplayRememberMsg() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        m.setLoading();
        m.displayRememberMsg();
        Assert.assertNull(m.title);
        Assert.assertNull(m.message);
        Assert.assertNull(m.strClass);
        Assert.assertNull(m.item);
        Assert.assertNull(m.alwaysRemember);
        Assert.assertNull(m.sessionOnly);
        Assert.assertEquals(-1, m.type);
        m.finishLoading();
        //Bundle.getMessage("Reminder"), Bundle.getMessage("ReminderLine"), getClassName(), "reminder"
        m.displayRememberMsg();
        Assert.assertEquals(Bundle.getMessage("Reminder"), m.title);
        Assert.assertEquals(Bundle.getMessage("ReminderLine"), m.message);
        Assert.assertEquals(m.getClass().getName(), m.strClass);
        Assert.assertEquals("reminder", m.item);
        Assert.assertTrue(m.alwaysRemember);
        Assert.assertFalse(m.sessionOnly);
        Assert.assertEquals(JOptionPane.INFORMATION_MESSAGE, m.type);
    }

    @Test
    public void testGetWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(strClass, windowLocation);
        Point savedWindowLocation = m.getWindowLocation(strClass);
        Assert.assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testGetWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(strClass, windowSize);
        Dimension savedWindowSize = m.getWindowSize(strClass);
        Assert.assertEquals(windowSize, savedWindowSize);
    }

    @Test
    public void testGetSaveWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, true);
        Assert.assertTrue(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, false);
        Assert.assertFalse(m.getSaveWindowSize(strClass));
    }

    @Test
    public void testGetSaveWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, true);
        Assert.assertTrue(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, false);
        Assert.assertFalse(m.getSaveWindowLocation(strClass));
    }

    @Test
    public void testSetSaveWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, true);
        Assert.assertTrue(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, false);
        Assert.assertFalse(m.getSaveWindowSize(strClass));
    }

    @Test
    public void testSetSaveWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, true);
        Assert.assertTrue(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, false);
        Assert.assertFalse(m.getSaveWindowLocation(strClass));
    }

    @Test
    public void testSetWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(strClass, windowLocation);
        Point savedWindowLocation = m.getWindowLocation(strClass);
        Assert.assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testSetWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(strClass, windowSize);
        Dimension savedWindowSize = m.getWindowSize(strClass);
        Assert.assertEquals(windowSize, savedWindowSize);
    }

    @Test
    public void testGetWindowList() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertTrue(m.getWindowList().isEmpty());
        Point location = new Point(69, 96);
        m.setWindowLocation(strClass, location);
        Assert.assertEquals(1, m.getWindowList().size());
        Assert.assertEquals(strClass, m.getWindowList().get(0));
        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(strClass, windowSize);
        Assert.assertEquals(1, m.getWindowList().size());
        Assert.assertEquals(strClass, m.getWindowList().get(0));
    }

    @Test
    public void testSetProperty() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", log);
        Assert.assertEquals(log, m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", null);
        Assert.assertNull(m.getProperty(strClass, "test1"));
    }

    @Test
    public void testGetProperty() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", log);
        Assert.assertEquals(log, m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", null);
        Assert.assertNull(m.getProperty(strClass, "test1"));
    }

    @Test
    public void testGetPropertyKeys() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.getPropertyKeys(strClass));
        m.setProperty(strClass, "test1", log);
        m.setProperty(strClass, "test2", new Object());
        Assert.assertEquals(2, m.getPropertyKeys(strClass).size());
        Assert.assertTrue(m.getPropertyKeys(strClass).contains("test1"));
        Assert.assertTrue(m.getPropertyKeys(strClass).contains("test2"));
        Assert.assertFalse(m.getPropertyKeys(strClass).contains("test3"));
        m.setProperty(strClass, "test2", null);
        Assert.assertEquals(1, m.getPropertyKeys(strClass).size());
        Assert.assertTrue(m.getPropertyKeys(strClass).contains("test1"));
        Assert.assertFalse(m.getPropertyKeys(strClass).contains("test2"));
        Assert.assertFalse(m.getPropertyKeys(strClass).contains("test3"));
    }

    @Test
    public void testHasProperties() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertFalse(m.hasProperties(strClass));
        m.setProperty(strClass, "test1", log);
        m.setProperty(strClass, "test2", null);
        Assert.assertTrue(m.hasProperties(strClass));
    }

    @Test
    public void testGetClassDescription_String() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNotNull(m.getClassDescription(strClass));
        Assert.assertTrue(m.getClassDescription(strClass).isEmpty());
        m.setPreferenceState(strClass, "test1", true);
        Assert.assertNull(m.getClassDescription(strClass));
        m.setPreferenceState(AppConfigBase.class.getName(), "test1", true);
        String d = ResourceBundle.getBundle("apps.AppsConfigBundle").getString("Application");
        Assert.assertEquals(d, m.getClassDescription(AppConfigBase.class.getName()));
    }

    @Test
    public void testGetPreferencesClasses() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertEquals(1, m.getPreferencesClasses().size());
        Assert.assertEquals(m.getClass().getName(), m.getPreferencesClasses().get(0));
        m.setPreferenceState(strClass, "test1", true);
        Assert.assertEquals(2, m.getPreferencesClasses().size());
        Assert.assertEquals(strClass, m.getPreferencesClasses().get(1));
    }

    @Test
    public void testSetClassDescription() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNotNull(m.getClassDescription(strClass));
        Assert.assertTrue(m.getClassDescription(strClass).isEmpty());
        m.setClassDescription(strClass);
        Assert.assertNotNull(m.getClassDescription(strClass));
        Assert.assertTrue(m.getClassDescription(strClass).isEmpty());
        m.setClassDescription(AppConfigBase.class.getName());
        String d = ResourceBundle.getBundle("apps.AppsConfigBundle").getString("Application");
        Assert.assertEquals(d, m.getClassDescription(AppConfigBase.class.getName()));
    }

    @Test
    public void testSetMessageItemDetails() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertTrue(m.getChoiceOptions(strClass, "item1").isEmpty());
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(options, m.getChoiceOptions(strClass, "item1"));
    }

    @Test
    public void testGetChoiceOptions() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertTrue(m.getChoiceOptions(strClass, "item1").isEmpty());
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(options, m.getChoiceOptions(strClass, "item1"));
    }

    @Test
    public void testGetMultipleChoiceSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertEquals(0, m.getMultipleChoiceSize(strClass));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(1, m.getMultipleChoiceSize(strClass));
    }

    @Test
    public void testGetMultipleChoiceList() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertTrue(m.getMultipleChoiceList(strClass).isEmpty());
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(1, m.getMultipleChoiceList(strClass).size());
        Assert.assertEquals("item1", m.getMultipleChoiceList(strClass).get(0));
    }

    @Test
    public void testGetChoiceName() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertNull(m.getChoiceName(strClass, 0));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals("item1", m.getChoiceName(strClass, 0));
    }

    @Test
    public void testGetChoiceDescription() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertNull(m.getChoiceDescription(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals("description1", m.getChoiceDescription(strClass, "item1"));
    }

    @Test
    public void testGetMultipleChoiceOption() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
    }

    @Test
    public void testGetMultipleChoiceDefaultOption() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        Assert.assertEquals(0, m.getMultipleChoiceDefaultOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(0, m.getMultipleChoiceDefaultOption(strClass, "item1"));
    }

    @Test
    public void testSetMultipleChoiceOption_3args_String() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        m.setMultipleChoiceOption(strClass, "item1", "value1");
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        options.put(2, "test2");
        Assert.assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", "test1");
        Assert.assertEquals(1, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", "test2");
        Assert.assertEquals(2, m.getMultipleChoiceOption(strClass, "item1"));
    }

    @Test
    public void testSetMultipleChoiceOption_3args_int() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        m.setMultipleChoiceOption(strClass, "item1", "value1");
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        options.put(2, "test2");
        Assert.assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        Assert.assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", 1);
        Assert.assertEquals(1, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", 2);
        Assert.assertEquals(2, m.getMultipleChoiceOption(strClass, "item1"));
    }

    @Test
    public void testGetClassDescription_0args() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        Assert.assertEquals("Preference Manager", m.getClassDescription());
    }

    @Test
    public void testGetClassName() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        Assert.assertEquals(JmriUserPreferencesManager.class.getName(), m.getClassName());
    }

    @Test
    public void testGetClassPreferences() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.getClassPreferences(strClass));
        m.setPreferenceState(strClass, "test1", true);
        Assert.assertNotNull(m.getClassPreferences(strClass));
        Assert.assertEquals(1, m.getClassPreferences(strClass).getPreferenceListSize());
        Assert.assertEquals("test1", m.getClassPreferences(strClass).getPreferenceList().get(0).getItem());
    }

    @Test
    public void testGetPreferencesSize() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Assert.assertNull(m.getClassPreferences(strClass));
        Assert.assertEquals(0, m.getPreferencesSize(strClass));
        m.setPreferenceState(strClass, "test1", true);
        Assert.assertNotNull(m.getClassPreferences(strClass));
        Assert.assertEquals(1, m.getPreferencesSize(strClass));

    }

    @Test
    public void testReadUserPreferences() throws IOException {
        JUnitUtil.resetProfileManager(new NullProfile(folder.newFolder(Profile.PROFILE)));
        Point location = new Point(69, 96);
        Dimension windowSize = new Dimension(100, 200);
        UserPreferencesManager m1 = new TestJmriUserPreferencesManager();
        m1.setSaveAllowed(false);
        m1.setProperty(strClass, "test1", "value1");
        m1.setProperty(strClass, "intTest", 42);
        m1.setProperty(strClass, "doubleTest", Math.PI);
        m1.setProperty(strClass, "booleanTest", true);
        m1.setWindowLocation(strClass, location);
        m1.setWindowSize(strClass, windowSize);
        m1.setPreferenceState(strClass, "test2", true);
        m1.setPreferenceState(strClass, "test3", false);
        m1.setSimplePreferenceState(strClass, true);
        m1.setComboBoxLastSelection(strClass, "selection1");
        m1.setSaveAllowed(true);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        Assert.assertNotNull(profile); // test with profile
        File target = new File(new File(new File(profile.getPath(), "profile"), NodeIdentity.storageIdentity()), "user-interface.xml");
        Assert.assertTrue(target.exists());
        Assert.assertTrue(target.isFile());
        if (log.isDebugEnabled()) {
            Files.lines(target.toPath()).forEach((line) -> log.debug(line));
        }
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        JmriUserPreferencesManager m2 = new JmriUserPreferencesManager();
        m2.readUserPreferences();
        Assert.assertEquals("value1", m2.getProperty(strClass, "test1"));
        Assert.assertEquals(42, m2.getProperty(strClass, "intTest"));
        Assert.assertEquals(Math.PI, m2.getProperty(strClass, "doubleTest"));
        Assert.assertEquals(true, m2.getProperty(strClass, "booleanTest"));
        Assert.assertEquals(location, m2.getWindowLocation(strClass));
        Assert.assertEquals(windowSize, m2.getWindowSize(strClass));
        Assert.assertEquals(true, m2.getPreferenceState(strClass, "test2"));
        Assert.assertEquals(false, m2.getPreferenceState(strClass, "test3"));
        Assert.assertEquals(true, m2.getSimplePreferenceState(strClass));
        Assert.assertEquals("selection1", m2.getComboBoxLastSelection(strClass));
    }

    @Test
    public void testSaveElement() throws IOException {
        JUnitUtil.resetProfileManager(new NullProfile(folder.newFolder(Profile.PROFILE)));
        Point location = new Point(69, 96);
        Dimension windowSize = new Dimension(100, 200);
        UserPreferencesManager m1 = new TestJmriUserPreferencesManager();
        m1.setSaveAllowed(false);
        m1.setProperty(strClass, "test1", "value1");
        m1.setProperty(strClass, "intTest", 42);
        m1.setProperty(strClass, "doubleTest", Math.PI);
        m1.setProperty(strClass, "booleanTest", true);
        m1.setWindowLocation(strClass, location);
        m1.setWindowSize(strClass, windowSize);
        m1.setPreferenceState(strClass, "test2", true);
        m1.setPreferenceState(strClass, "test3", false);
        m1.setSimplePreferenceState(strClass, true);
        m1.setComboBoxLastSelection(strClass, "selection1");
        m1.setSaveAllowed(true);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        Assert.assertNotNull(profile); // test with profile
        File target = new File(new File(new File(profile.getPath(), "profile"), NodeIdentity.storageIdentity()), "user-interface.xml");
        Assert.assertTrue(target.exists());
        Assert.assertTrue(target.isFile());
        if (log.isDebugEnabled()) {
            Files.lines(target.toPath()).forEach((line) -> log.debug(line));
        }
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        JmriUserPreferencesManager m2 = new JmriUserPreferencesManager();
        m2.readUserPreferences();
        Assert.assertEquals("value1", m2.getProperty(strClass, "test1"));
        Assert.assertEquals(42, m2.getProperty(strClass, "intTest"));
        Assert.assertEquals(Math.PI, m2.getProperty(strClass, "doubleTest"));
        Assert.assertEquals(true, m2.getProperty(strClass, "booleanTest"));
        Assert.assertEquals(location, m2.getWindowLocation(strClass));
        Assert.assertEquals(windowSize, m2.getWindowSize(strClass));
        Assert.assertEquals(true, m2.getPreferenceState(strClass, "test2"));
        Assert.assertEquals(false, m2.getPreferenceState(strClass, "test3"));
        Assert.assertEquals(true, m2.getSimplePreferenceState(strClass));
        Assert.assertEquals("selection1", m2.getComboBoxLastSelection(strClass));
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        // ensure no existing UserPreferencesManager interferes with this test
        InstanceManager.reset(UserPreferencesManager.class);
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
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

        protected TestJmriUserPreferencesManager() {
            super();
        }

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
            //org.slf4j.LoggerFactory.getLogger(TestJmriUserPreferencesManager.class).error("showMessage called.", new Exception());
        }

        /**
         * Expose the HashMap of comboBox last selections for testing purposes.
         *
         * @return the map of combo box last selections
         */
        protected HashMap<String, String> getComboBoxLastSelection() {
            return this.comboBoxLastSelection;
        }

        /**
         * Expose the loading flag for testing purposes by changing the method
         * scope from protected to public.
         *
         * {@inheritDoc}
         */
        @Override
        protected boolean isLoading() {
            return super.isLoading();
        }
    }

    private static class Listener implements PropertyChangeListener {

        public PropertyChangeEvent event = null;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            this.event = evt;
        }
    }
}
