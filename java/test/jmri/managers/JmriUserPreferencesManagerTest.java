package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import apps.AppConfigBase;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.ResourceBundle;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.node.NodeIdentity;
import jmri.util.swing.JmriJOptionPane;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the jmri.managers.JmriUserPreferencesManager class.
 *
 * @author Bob Jacobsen Copyright 2009
 * @author Randall Wood Copyright 2017
 */
public class JmriUserPreferencesManagerTest {

    private final String strClass = JmriUserPreferencesManagerTest.class.getName();

    @Test
    public void testAllowSave() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testDisallowSave() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        assertTrue(m.isSaveAllowed());
        m.setSaveAllowed(false);
        assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testSetSaveAllowed() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        assertTrue(m.isSaveAllowed());
    }

    @Test
    public void testIsSaveAllowed() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        assertTrue(m.isSaveAllowed());
        m.setSaveAllowed(false);
        assertFalse(m.isSaveAllowed());
        m.setSaveAllowed(true);
        assertTrue(m.isSaveAllowed());
    }

    @Test
    @DisabledIfHeadless
    public void testGetScreen() {
        assertEquals(Toolkit.getDefaultToolkit().getScreenSize(), (new JmriUserPreferencesManager()).getScreen());
    }

    @Test
    public void testSetSimplePreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);

        assertFalse(m.getSimplePreferenceState("one"));

        m.setSimplePreferenceState("one", true);
        assertTrue(m.getSimplePreferenceState("one"));
        assertFalse(m.getSimplePreferenceState("two"));

        m.setSimplePreferenceState("one", false);
        assertFalse(m.getSimplePreferenceState("one"));
        assertFalse(m.getSimplePreferenceState("two"));
    }

    @Test
    public void testGetSimplePreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);

        assertFalse(m.getSimplePreferenceState("one"));

        m.setSimplePreferenceState("one", true);
        assertTrue(m.getSimplePreferenceState("one"));
        assertFalse(m.getSimplePreferenceState("two"));

        m.setSimplePreferenceState("one", false);
        assertFalse(m.getSimplePreferenceState("one"));
        assertFalse(m.getSimplePreferenceState("two"));
    }

    @Test
    public void testGetSimplePreferenceStateList() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        // defaults to empty
        assertEquals(0, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test1", true);
        // setting a preference to true adds it
        assertEquals(1, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test1", false);
        // setting a preference to false removes it
        assertEquals(0, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test1", true);
        m.setSimplePreferenceState("test2", true);
        // setting a preference to true adds it
        assertEquals(2, m.getSimplePreferenceStateList().size());
        m.setSimplePreferenceState("test2", true);
        // setting a preference to true twice adds it once
        assertEquals(2, m.getSimplePreferenceStateList().size());
    }

    @Test
    public void testSetPreferenceState() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);

        assertFalse(m.getPreferenceState(strClass, "test1"));

        m.setPreferenceState(strClass, "test1", true);
        assertTrue(m.getPreferenceState(strClass, "test1"));
        assertFalse(m.getPreferenceState(strClass, "test2"));

        m.setPreferenceState(strClass, "test1", false);
        assertFalse(m.getPreferenceState(strClass, "test1"));
        assertFalse(m.getPreferenceState(strClass, "test2"));

        // non-existant class should not cause an error
        m.setPreferenceState("non.existant.class", "test1", true);
        JUnitAppender.assertWarnMessage("class name \"non.existant.class\" cannot be found, perhaps an expected plugin is missing?");
    }

    @Test
    public void testGetPreferenceState() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);

        assertFalse(m.getPreferenceState(strClass, "test1"));

        m.setPreferenceState(strClass, "test1", true);
        assertTrue(m.getPreferenceState(strClass, "test1"));
        assertFalse(m.getPreferenceState(strClass, "test2"));

        m.setPreferenceState(strClass, "test1", false);
        assertFalse(m.getPreferenceState(strClass, "test1"));
        assertFalse(m.getPreferenceState(strClass, "test2"));
    }

    @Test
    public void testSetPreferenceItemDetails() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", "description1");
        assertEquals("description1", m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", null);
        assertNull(m.getPreferenceItemDescription(strClass, "test1"));
    }

    @Test
    public void testGetPreferenceList() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);

        assertTrue(m.getPreferenceList(strClass).isEmpty());

        m.setPreferenceState(strClass, "test1", true);
        assertTrue(m.getPreferenceState(strClass, "test1"));
        assertFalse(m.getPreferenceState(strClass, "test2"));

        m.setPreferenceState(strClass, "test1", false);
        assertFalse(m.getPreferenceState(strClass, "test1"));
        assertFalse(m.getPreferenceState(strClass, "test2"));
    }

    @Test
    public void testGetPreferenceItemName() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);

        assertTrue(m.getPreferenceList(strClass).isEmpty());

        assertNull(m.getPreferenceItemName(strClass, 0));

        m.setPreferenceState(strClass, "test1", true);
        assertEquals("test1", m.getPreferenceItemName(strClass, 0));
        assertNull( m.getPreferenceItemName(strClass, 1), "test2");

        m.setPreferenceState(strClass, "test1", false);
        assertEquals("test1", m.getPreferenceItemName(strClass, 0));
        assertNull( m.getPreferenceItemName(strClass, 1), "test2");
    }

    @Test
    public void testGetPreferenceItemDescription() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", "description1");
        assertEquals("description1", m.getPreferenceItemDescription(strClass, "test1"));
        m.setPreferenceItemDetails(strClass, "test1", null);
        assertNull(m.getPreferenceItemDescription(strClass, "test1"));
    }

    @Test
    public void testSetSessionPreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getSessionPreferenceState("test1"));
        assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", true);
        assertTrue(m.getSessionPreferenceState("test1"));
        assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", false);
        assertFalse(m.getSessionPreferenceState("test1"));
        assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test2", true);
        assertFalse(m.getSessionPreferenceState("test1"));
        assertTrue(m.getSessionPreferenceState("test2"));
    }

    @Test
    public void testGetSessionPreferenceState() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getSessionPreferenceState("test1"));
        assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", true);
        assertTrue(m.getSessionPreferenceState("test1"));
        assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test1", false);
        assertFalse(m.getSessionPreferenceState("test1"));
        assertFalse(m.getSessionPreferenceState("test2"));
        m.setSessionPreferenceState("test2", true);
        assertFalse(m.getSessionPreferenceState("test1"));
        assertTrue(m.getSessionPreferenceState("test2"));
    }

    @Test
    public void testShowInfoMessage_4args() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.title);
        assertNull(m.message);
        assertNull(m.strClass);
        assertNull(m.item);
        assertNull(m.alwaysRemember);
        assertNull(m.sessionOnly);
        assertEquals(-1, m.type);
        m.showInfoMessage("title1", "message1", strClass, "item1");
        assertEquals("title1", m.title);
        assertEquals("message1", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item1", m.item);
        assertTrue(m.alwaysRemember);
        assertFalse(m.sessionOnly);
        assertEquals(JmriJOptionPane.INFORMATION_MESSAGE, m.type);
    }

    @Test
    public void testShowErrorMessage() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.title);
        assertNull(m.message);
        assertNull(m.strClass);
        assertNull(m.item);
        assertNull(m.alwaysRemember);
        assertNull(m.sessionOnly);
        assertEquals(-1, m.type);
        m.showErrorMessage("title1", "message1", strClass, "item1", true, true);
        assertEquals("title1", m.title);
        assertEquals("message1", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item1", m.item);
        assertTrue(m.alwaysRemember);
        assertTrue(m.sessionOnly);
        assertEquals(JmriJOptionPane.ERROR_MESSAGE, m.type);
        m.showErrorMessage("title2", "message2", strClass, "item2", false, false);
        assertEquals("title2", m.title);
        assertEquals("message2", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item2", m.item);
        assertFalse(m.alwaysRemember);
        assertFalse(m.sessionOnly);
        assertEquals(JmriJOptionPane.ERROR_MESSAGE, m.type);
    }

    @Test
    public void testShowInfoMessage_6args() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.title);
        assertNull(m.message);
        assertNull(m.strClass);
        assertNull(m.item);
        assertNull(m.alwaysRemember);
        assertNull(m.sessionOnly);
        assertEquals(-1, m.type);
        m.showInfoMessage("title1", "message1", strClass, "item1", true, true);
        assertEquals("title1", m.title);
        assertEquals("message1", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item1", m.item);
        assertTrue(m.alwaysRemember);
        assertTrue(m.sessionOnly);
        assertEquals(JmriJOptionPane.INFORMATION_MESSAGE, m.type);
        m.showInfoMessage("title2", "message2", strClass, "item2", false, false);
        assertEquals("title2", m.title);
        assertEquals("message2", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item2", m.item);
        assertFalse(m.alwaysRemember);
        assertFalse(m.sessionOnly);
        assertEquals(JmriJOptionPane.INFORMATION_MESSAGE, m.type);
    }

    @Test
    public void testShowWarningMessage() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.title);
        assertNull(m.message);
        assertNull(m.strClass);
        assertNull(m.item);
        assertNull(m.alwaysRemember);
        assertNull(m.sessionOnly);
        assertEquals(-1, m.type);
        m.showWarningMessage("title1", "message1", strClass, "item1", true, true);
        assertEquals("title1", m.title);
        assertEquals("message1", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item1", m.item);
        assertTrue(m.alwaysRemember);
        assertTrue(m.sessionOnly);
        assertEquals(JmriJOptionPane.WARNING_MESSAGE, m.type);
        m.showWarningMessage("title2", "message2", strClass, "item2", false, false);
        assertEquals("title2", m.title);
        assertEquals("message2", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item2", m.item);
        assertFalse(m.alwaysRemember);
        assertFalse(m.sessionOnly);
        assertEquals(JmriJOptionPane.WARNING_MESSAGE, m.type);
    }

    @Test
    public void testShowMessage() {
        // TODO: Use Jemmy to test showing real message
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.title);
        assertNull(m.message);
        assertNull(m.strClass);
        assertNull(m.item);
        assertNull(m.alwaysRemember);
        assertNull(m.sessionOnly);
        assertEquals(-1, m.type);
        m.showMessage(null, "title1", "message1", strClass, "item1", true, true, JmriJOptionPane.INFORMATION_MESSAGE);
        assertEquals("title1", m.title);
        assertEquals("message1", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item1", m.item);
        assertTrue(m.alwaysRemember);
        assertTrue(m.sessionOnly);
        m.showMessage(null, "title2", "message2", strClass, "item2", false, false, JmriJOptionPane.INFORMATION_MESSAGE);
        assertEquals("title2", m.title);
        assertEquals("message2", m.message);
        assertEquals(strClass, m.strClass);
        assertEquals("item2", m.item);
        assertFalse(m.alwaysRemember);
        assertFalse(m.sessionOnly);
    }

    @Test
    public void testAddComboBoxLastSelection() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value1");
        assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value2");
        assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test2", "value1");
        assertEquals(2, m.getComboBoxLastSelection().size());
    }

    @Test
    public void testGetComboBoxLastSelection_String() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value2");
        m.setComboBoxLastSelection("test2", "value1");
        assertEquals("value2", m.getComboBoxLastSelection("test1"));
        assertEquals("value1", m.getComboBoxLastSelection("test2"));
        assertNull(m.getComboBoxLastSelection("test3"));
    }

    @Test
    public void testSetComboBoxLastSelection() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value1");
        assertEquals("value1", m.getComboBoxLastSelection("test1"));
        m.setComboBoxLastSelection("test1", "value2");
        assertEquals("value2", m.getComboBoxLastSelection("test1"));
        m.setComboBoxLastSelection("test2", "value1");
        assertEquals("value1", m.getComboBoxLastSelection("test2"));
        assertNull(m.getComboBoxLastSelection("test3"));
    }

    @Test
    public void testGetComboBoxSelectionSize() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertEquals(0, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value1");
        assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test1", "value2");
        assertEquals(1, m.getComboBoxLastSelection().size());
        m.setComboBoxLastSelection("test2", "value1");
        assertEquals(2, m.getComboBoxLastSelection().size());
    }

    @Test
    public void testGetChangeMade() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getChangeMade());
        m.setChangeMade(false);
        assertTrue(m.getChangeMade());
        m.resetChangeMade();
        assertFalse(m.getChangeMade());
    }

    @Test
    public void testSetChangeMade() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Listener l = new Listener();
        m.addPropertyChangeListener(l);
        assertFalse(m.getChangeMade());
        m.setChangeMade(false);
        assertTrue(m.getChangeMade());
        assertNull(l.event);
        m.setChangeMade(true);
        JUnitUtil.waitFor(() -> {
            return l.event != null && l.event.getPropertyName().equals(UserPreferencesManager.PREFERENCES_UPDATED);
        }, "event change notification fired");
    }

    @Test
    public void testResetChangeMade() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getChangeMade());
        m.setChangeMade(false);
        assertTrue(m.getChangeMade());
        m.resetChangeMade();
        assertFalse(m.getChangeMade());
        m.resetChangeMade();
        assertFalse(m.getChangeMade());
    }

    @Test
    public void testIsLoading() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.isLoading());
        m.setLoading();
        assertTrue(m.isLoading());
        m.finishLoading();
        assertFalse(m.isLoading());
    }

    @Test
    public void testSetLoading() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.isLoading());
        m.setLoading();
        assertTrue(m.isLoading());
        m.finishLoading();
        assertFalse(m.isLoading());
    }

    @Test
    public void testFinishLoading() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.isLoading());
        m.finishLoading();
        assertFalse(m.isLoading());
        m.setLoading();
        assertTrue(m.isLoading());
        m.finishLoading();
        assertFalse(m.isLoading());
    }

    @Test
    public void testDisplayRememberMsg() {
        TestJmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        m.setLoading();
        m.displayRememberMsg();
        assertNull(m.title);
        assertNull(m.message);
        assertNull(m.strClass);
        assertNull(m.item);
        assertNull(m.alwaysRemember);
        assertNull(m.sessionOnly);
        assertEquals(-1, m.type);
        m.finishLoading();
        //Bundle.getMessage("Reminder"), Bundle.getMessage("ReminderLine"), getClassName(), "reminder"
        m.displayRememberMsg();
        assertEquals(Bundle.getMessage("Reminder"), m.title);
        assertEquals(Bundle.getMessage("ReminderLine"), m.message);
        assertEquals(m.getClass().getName(), m.strClass);
        assertEquals("reminder", m.item);
        assertTrue(m.alwaysRemember);
        assertFalse(m.sessionOnly);
        assertEquals(JmriJOptionPane.INFORMATION_MESSAGE, m.type);
    }

    @Test
    public void testGetWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(strClass, windowLocation);
        Point savedWindowLocation = m.getWindowLocation(strClass);
        assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testGetWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(strClass, windowSize);
        Dimension savedWindowSize = m.getWindowSize(strClass);
        assertEquals(windowSize, savedWindowSize);
    }

    @Test
    public void testGetSaveWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, true);
        assertTrue(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, false);
        assertFalse(m.getSaveWindowSize(strClass));
    }

    @Test
    public void testGetSaveWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, true);
        assertTrue(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, false);
        assertFalse(m.getSaveWindowLocation(strClass));
    }

    @Test
    public void testSetSaveWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, true);
        assertTrue(m.getSaveWindowSize(strClass));
        m.setSaveWindowSize(strClass, false);
        assertFalse(m.getSaveWindowSize(strClass));
    }

    @Test
    public void testSetSaveWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, true);
        assertTrue(m.getSaveWindowLocation(strClass));
        m.setSaveWindowLocation(strClass, false);
        assertFalse(m.getSaveWindowLocation(strClass));
    }

    @Test
    public void testSetWindowLocation() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Point windowLocation = new Point(69, 96);
        m.setWindowLocation(strClass, windowLocation);
        Point savedWindowLocation = m.getWindowLocation(strClass);
        assertEquals(windowLocation, savedWindowLocation);
    }

    @Test
    public void testSetWindowSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(strClass, windowSize);
        Dimension savedWindowSize = m.getWindowSize(strClass);
        assertEquals(windowSize, savedWindowSize);
    }

    @Test
    public void testGetWindowList() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertTrue(m.getWindowList().isEmpty());
        Point location = new Point(69, 96);
        m.setWindowLocation(strClass, location);
        assertEquals(1, m.getWindowList().size());
        assertEquals(strClass, m.getWindowList().get(0));
        Dimension windowSize = new Dimension(666, 999);
        m.setWindowSize(strClass, windowSize);
        assertEquals(1, m.getWindowList().size());
        assertEquals(strClass, m.getWindowList().get(0));
    }

    @Test
    public void testSetProperty() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", log);
        assertEquals(log, m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", null);
        assertNull(m.getProperty(strClass, "test1"));
    }

    @Test
    public void testGetProperty() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", log);
        assertEquals(log, m.getProperty(strClass, "test1"));
        m.setProperty(strClass, "test1", null);
        assertNull(m.getProperty(strClass, "test1"));
    }

    @Test
    public void testGetPropertyKeys() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.getPropertyKeys(strClass));
        m.setProperty(strClass, "test1", log);
        m.setProperty(strClass, "test2", new Object());
        assertEquals(2, m.getPropertyKeys(strClass).size());
        assertTrue(m.getPropertyKeys(strClass).contains("test1"));
        assertTrue(m.getPropertyKeys(strClass).contains("test2"));
        assertFalse(m.getPropertyKeys(strClass).contains("test3"));
        m.setProperty(strClass, "test2", null);
        assertEquals(1, m.getPropertyKeys(strClass).size());
        assertTrue(m.getPropertyKeys(strClass).contains("test1"));
        assertFalse(m.getPropertyKeys(strClass).contains("test2"));
        assertFalse(m.getPropertyKeys(strClass).contains("test3"));
    }

    @Test
    public void testHasProperties() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertFalse(m.hasProperties(strClass));
        m.setProperty(strClass, "test1", log);
        m.setProperty(strClass, "test2", null);
        assertTrue(m.hasProperties(strClass));
    }

    @Test
    public void testGetClassDescription_String() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNotNull(m.getClassDescription(strClass));
        assertTrue(m.getClassDescription(strClass).isEmpty());
        m.setPreferenceState(strClass, "test1", true);
        assertNull(m.getClassDescription(strClass));
        m.setPreferenceState(AppConfigBase.class.getName(), "test1", true);
        String d = ResourceBundle.getBundle("apps.AppsConfigBundle").getString("Application");
        assertEquals(d, m.getClassDescription(AppConfigBase.class.getName()));
    }

    @Test
    public void testGetPreferencesClasses() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertEquals(1, m.getPreferencesClasses().size());
        assertEquals(m.getClass().getName(), m.getPreferencesClasses().get(0));
        m.setPreferenceState(strClass, "test1", true);
        assertEquals(2, m.getPreferencesClasses().size());
        assertEquals(strClass, m.getPreferencesClasses().get(1));
    }

    @Test
    public void testSetClassDescription() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNotNull(m.getClassDescription(strClass));
        assertTrue(m.getClassDescription(strClass).isEmpty());
        m.setClassDescription(strClass);
        assertNotNull(m.getClassDescription(strClass));
        assertTrue(m.getClassDescription(strClass).isEmpty());
        m.setClassDescription(AppConfigBase.class.getName());
        String d = ResourceBundle.getBundle("apps.AppsConfigBundle").getString("Application");
        assertEquals(d, m.getClassDescription(AppConfigBase.class.getName()));
    }

    @Test
    public void testSetMessageItemDetails() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertTrue(m.getChoiceOptions(strClass, "item1").isEmpty());
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(options, m.getChoiceOptions(strClass, "item1"));
    }

    @Test
    public void testGetChoiceOptions() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertTrue(m.getChoiceOptions(strClass, "item1").isEmpty());
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(options, m.getChoiceOptions(strClass, "item1"));
    }

    @Test
    public void testGetMultipleChoiceSize() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertEquals(0, m.getMultipleChoiceSize(strClass));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(1, m.getMultipleChoiceSize(strClass));
    }

    @Test
    public void testGetMultipleChoiceList() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertTrue(m.getMultipleChoiceList(strClass).isEmpty());
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(1, m.getMultipleChoiceList(strClass).size());
        assertEquals("item1", m.getMultipleChoiceList(strClass).get(0));
    }

    @Test
    public void testGetChoiceName() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertNull(m.getChoiceName(strClass, 0));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals("item1", m.getChoiceName(strClass, 0));
    }

    @Test
    public void testGetChoiceDescription() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertNull(m.getChoiceDescription(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals("description1", m.getChoiceDescription(strClass, "item1"));
    }

    @Test
    public void testGetMultipleChoiceOption() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
    }

    @Test
    public void testGetMultipleChoiceDefaultOption() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        assertEquals(0, m.getMultipleChoiceDefaultOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(0, m.getMultipleChoiceDefaultOption(strClass, "item1"));
    }

    @Test
    public void testSetMultipleChoiceOption_3args_String() {
        UserPreferencesManager m = new JmriUserPreferencesManager();
        m.setSaveAllowed(false);
        m.setMultipleChoiceOption(strClass, "item1", "value1");
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        options.put(2, "test2");
        assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", "test1");
        assertEquals(1, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", "test2");
        assertEquals(2, m.getMultipleChoiceOption(strClass, "item1"));
    }

    @Test
    public void testSetMultipleChoiceOption_3args_int() {
        UserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        m.setMultipleChoiceOption(strClass, "item1", "value1");
        HashMap<Integer, String> options = new HashMap<>();
        options.put(1, "test1");
        options.put(2, "test2");
        assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMessageItemDetails(strClass, "item1", "description1", options, 0);
        assertEquals(0, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", 1);
        assertEquals(1, m.getMultipleChoiceOption(strClass, "item1"));
        m.setMultipleChoiceOption(strClass, "item1", 2);
        assertEquals(2, m.getMultipleChoiceOption(strClass, "item1"));
    }

    @Test
    public void testGetClassDescription_0args() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        assertEquals("Preference Manager", m.getClassDescription());
    }

    @Test
    public void testGetClassName() {
        JmriUserPreferencesManager m = new JmriUserPreferencesManager();
        assertEquals(JmriUserPreferencesManager.class.getName(), m.getClassName());
    }

    @Test
    public void testGetClassPreferences() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.getClassPreferences(strClass));
        m.setPreferenceState(strClass, "test1", true);
        assertNotNull(m.getClassPreferences(strClass));
        assertEquals(1, m.getClassPreferences(strClass).getPreferenceListSize());
        assertEquals("test1", m.getClassPreferences(strClass).getPreferenceList().get(0).getItem());
    }

    @Test
    public void testGetPreferencesSize() {
        JmriUserPreferencesManager m = new TestJmriUserPreferencesManager();
        m.setSaveAllowed(false);
        assertNull(m.getClassPreferences(strClass));
        assertEquals(0, m.getPreferencesSize(strClass));
        m.setPreferenceState(strClass, "test1", true);
        assertNotNull(m.getClassPreferences(strClass));
        assertEquals(1, m.getPreferencesSize(strClass));

    }

    @Test
    public void testReadUserPreferences(@TempDir File folder) throws IOException {
        JUnitUtil.resetProfileManager(new NullProfile(folder));
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
        assertNotNull(profile); // test with profile
        File target = new File(new File(new File(profile.getPath(), "profile"), NodeIdentity.storageIdentity()), "user-interface.xml");
        assertTrue(target.exists());
        assertTrue(target.isFile());
        if (log.isDebugEnabled()) {
            Files.lines(target.toPath()).forEach((line) -> log.debug(line));
        }
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        JmriUserPreferencesManager m2 = new JmriUserPreferencesManager();
        m2.readUserPreferences();
        assertEquals("value1", m2.getProperty(strClass, "test1"));
        assertEquals(42, (int)m2.getProperty(strClass, "intTest"));
        assertEquals(Math.PI, (double)m2.getProperty(strClass, "doubleTest"), 0.001);
        assertEquals(true, m2.getProperty(strClass, "booleanTest"));
        assertEquals(location, m2.getWindowLocation(strClass));
        assertEquals(windowSize, m2.getWindowSize(strClass));
        assertEquals(true, m2.getPreferenceState(strClass, "test2"));
        assertEquals(false, m2.getPreferenceState(strClass, "test3"));
        assertEquals(true, m2.getSimplePreferenceState(strClass));
        assertEquals("selection1", m2.getComboBoxLastSelection(strClass));
    }

    @Test
    public void testSaveElement(@TempDir File folder) throws IOException {
        JUnitUtil.resetProfileManager(new NullProfile(folder));
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
        assertNotNull(profile); // test with profile
        File target = new File(new File(new File(profile.getPath(), "profile"), NodeIdentity.storageIdentity()), "user-interface.xml");
        assertTrue(target.exists());
        assertTrue(target.isFile());
        if (log.isDebugEnabled()) {
            Files.lines(target.toPath()).forEach((line) -> log.debug(line));
        }
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        JmriUserPreferencesManager m2 = new JmriUserPreferencesManager();
        m2.readUserPreferences();
        assertEquals("value1", m2.getProperty(strClass, "test1"));
        assertEquals(42, (int)m2.getProperty(strClass, "intTest"));
        assertEquals(Math.PI, (double)m2.getProperty(strClass, "doubleTest"), 0.001);
        assertEquals(true, m2.getProperty(strClass, "booleanTest"));
        assertEquals(location, m2.getWindowLocation(strClass));
        assertEquals(windowSize, m2.getWindowSize(strClass));
        assertEquals(true, m2.getPreferenceState(strClass, "test2"));
        assertEquals(false, m2.getPreferenceState(strClass, "test3"));
        assertEquals(true, m2.getSimplePreferenceState(strClass));
        assertEquals("selection1", m2.getComboBoxLastSelection(strClass));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetPreferencesProviders();
        // ensure no existing UserPreferencesManager interferes with this test
        InstanceManager.reset(UserPreferencesManager.class);
    }

    @AfterEach
    public void tearDown() {
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
        protected void showMessage(@javax.annotation.CheckForNull Component parent, String title,
            String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriUserPreferencesManagerTest.class);

}
