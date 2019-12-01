package jmri.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import com.alexandriasoftware.swing.JInputValidator;
import com.alexandriasoftware.swing.Validation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.NamedBean.DisplayOptions;
import jmri.util.JUnitUtil;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Randall Wood Copyright (C) 2019
 */
public class NamedBeanComboBoxTest {

    @Test
    public void testSensorSimpleCtor() {
        Manager<Sensor> m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        assertNotNull("exists", t);
    }

    @Test
    public void testSensorFullCtor() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        m.provideSensor("IS1").setUserName("Sensor 1");
        Sensor s = m.provideSensor("IS2");
        s.setUserName("Sensor 2");
        m.provideSensor("IS3").setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s, DisplayOptions.DISPLAYNAME);

        assertNotNull("exists", t);
        assertEquals(s, t.getSelectedItem());
        assertEquals("Sensor 2", t.getSelectedItemUserName());
        assertEquals("IS2", t.getSelectedItemSystemName());
        assertEquals("Sensor 2", t.getSelectedItemDisplayName()); // Display name is user name if present
        
        t.setSelectedItem(null);
        assertNull(t.getSelectedItemUserName());
        assertNull(t.getSelectedItemSystemName());
        assertNull(t.getSelectedItemDisplayName());
    }

    @Test
    public void testSensorSelectEntry() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s2, DisplayOptions.DISPLAYNAME);
        assertNotNull("exists", t);

        // s2 checked in prior test, change selection without repeating
        t.setSelectedItem(s3);
        assertEquals(s3, t.getSelectedItem());
        assertEquals("Sensor 3", t.getSelectedItemUserName());
        assertEquals("IS3", t.getSelectedItemSystemName());
        assertEquals("Sensor 3", t.getSelectedItemDisplayName()); // Display name is user name if present
    }

    @Test
    public void testSensorExcludeSome() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        Sensor s4 = m.provideSensor("IS4");
        s4.setUserName("Sensor 4");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s2, DisplayOptions.DISPLAYNAME);

        assertEquals(4, t.getItemCount());
        assertEquals(s2, t.getSelectedItem());

        t.setExcludedItems(new HashSet<>(Arrays.asList(new Sensor[]{s4})));
        assertNotNull(t.getExcludedItems());

        assertEquals(3, t.getItemCount());
        assertEquals(s2, t.getSelectedItem());

        t.setExcludedItems(new HashSet<>(Arrays.asList(new Sensor[]{s2, s4})));

        assertEquals(2, t.getItemCount());
        assertTrue(!s2.equals(t.getSelectedItem())); // just has to change, don't care what to
    }

    @Test
    public void testSensorChangeDisplayMode() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME);
        JList<Sensor> l = new JList<>(t.getModel());
        assertNotNull("exists", t);
        assertEquals(DisplayOptions.DISPLAYNAME, t.getDisplayOrder());

        assertEquals("Sensor 1",
                ((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText());

        t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
        assertEquals(DisplayOptions.SYSTEMNAME, t.getDisplayOrder());
        assertEquals("IS1",
                ((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText());

        t.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
        assertEquals(DisplayOptions.USERNAME_SYSTEMNAME, t.getDisplayOrder());
        assertEquals("Sensor 1 (IS1)",
                ((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText());
    }

    @Test
    public void testSensorSetAndDefaultValidate() {
        Manager<Sensor> m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);

        assertTrue(t.isValidatingInput());

        t.setValidatingInput(false);
        assertTrue(!t.isValidatingInput());

        t.setValidatingInput(true);
        assertTrue(t.isValidatingInput());

    }

    @Test
    public void testSensorAllowEdit() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        assertTrue(m.getNamedBeanSet().isEmpty());
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        assertFalse(t.isAllowNull());
        assertEquals(0, t.getModel().getSize());
        t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
        t.setAllowNull(true);
        assertTrue(t.isAllowNull());
        assertEquals(0, t.getModel().getSize());
        Sensor s1 = m.provideSensor("IS1");
        assertTrue(t.isAllowNull());
        assertEquals(2, t.getModel().getSize());
        assertNull(t.getItemAt(0));
        assertEquals(s1, t.getItemAt(1));
        t.setAllowNull(false);
        assertFalse(t.isAllowNull());
        assertEquals(1, t.getModel().getSize());
        assertEquals(s1, t.getItemAt(0));
        t.setAllowNull(true);
        assertTrue(t.isAllowNull());
        assertEquals(2, t.getModel().getSize());
        assertNull(t.getItemAt(0));
        assertEquals(s1, t.getItemAt(1));
    }

    @Test
    public void testSensorEditText()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
        t.setAllowNull(true);
        t.setEditable(true);
        JTextField c = ((JTextField) t.getEditor().getEditorComponent());

        assertEquals("", c.getText());

        c.setText("IS2");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS2", c.getText());
        assertEquals(s2, t.getSelectedItem());
    }

    @Test
    public void testSensorTestProvidingValidity()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        t.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
        t.setAllowNull(true);
        t.setEditable(true);
        t.setProviding(true);
        JTextField c = ((JTextField) t.getEditor().getEditorComponent());

        // test with no matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        Sensor s1 = t.getSelectedItem();
        assertEquals(s1, m.getBeanBySystemName("IS1"));

        c.setText("K ");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s1, t.getSelectedItem()); // selection did not change because of invalid input

        // clear manager
        m.deregister(s1);

        // test with no matching bean and isValidatingInput() == true
        // should match NONE when empty and DANGER otherwise
        t.setValidatingInput(true);

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.INFORMATION, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        s1 = t.getSelectedItem();
        assertEquals(s1, m.getBeanBySystemName("IS1"));

        c.setText("K ");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.DANGER, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s1, t.getSelectedItem()); // selection did not change because of invalid input

        // clear manager
        m.deregister(s1);

        // test with a matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        s1 = t.getSelectedItem();
        assertEquals(s1, m.getBeanBySystemName("IS1"));

        c.setText("K ");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s1, t.getSelectedItem()); // selection did not change because of invalid input

        // clear manager
        m.deregister(s1);

        // test with a matching bean and isValidatingInput() == true
        // should match DANGER with text "K " and NONE otherwise
        t.setValidatingInput(true);

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        t.setSelectedItem(null); // change selection to verify selection changes
        assertNull(t.getSelectedItem());
        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.INFORMATION, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        s1 = t.getSelectedItem();
        assertEquals(s1, m.getBeanBySystemName("IS1"));

        c.setText("K ");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.DANGER, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s1, t.getSelectedItem()); // selection did not change because of invalid input
    }

    @Test
    public void testSensorTestNonProvidingValidity()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        t.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
        t.setAllowNull(true);
        t.setEditable(true);
        t.setProviding(false);
        JTextField c = ((JTextField) t.getEditor().getEditorComponent());

        // test with no matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("K ");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        // test with no matching bean and isValidatingInput() == true
        // should match NONE when empty and WARNING otherwise
        t.setValidatingInput(true);

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.WARNING, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("K ");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.WARNING, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());        

        // test with a matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);
        Sensor s = m.provide("IS1");

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertNull(t.getSelectedItem());

        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s, t.getSelectedItem());

        c.setText("K ");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s, t.getSelectedItem()); // selection did not change because of invalid input

        // test with a matching bean and isValidatingInput() == true
        // should match WARNING with text "K " and NONE otherwise
        t.setValidatingInput(true);

        c.setText("");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s, t.getSelectedItem()); // selection did not change because of invalid input

        t.setSelectedItem(null); // change selection to verify selection changes
        assertNull(t.getSelectedItem());
        c.setText("IS1");
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("IS1", c.getText());
        assertEquals(Validation.Type.NONE, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s, t.getSelectedItem());

        c.setText("K ");
        JUnitUtil.waitFor(() -> "K ".equals(c.getText()));
        c.getInputVerifier().verify(c); // manually force validation because not on AWT thread
        assertEquals("K ", c.getText());
        assertEquals(Validation.Type.WARNING, ((JInputValidator) c.getInputVerifier()).getValidation().getType());
        assertEquals(s, t.getSelectedItem()); // selection did not change because of invalid input
    }

    @Test
    public void testSensorSetBean() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        Sensor s4 = m.provideSensor("IS4");
        s4.setUserName("Sensor 4");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME);

        assertEquals("Sensor 1", t.getSelectedItemDisplayName());

        t.setSelectedItem(s2);
        assertEquals(s2, t.getSelectedItem());

        t.setDisplayOrder(DisplayOptions.SYSTEMNAME);
        t.setSelectedItem(s3);
        assertEquals(s3, t.getSelectedItem());

        t.setDisplayOrder(DisplayOptions.USERNAME_SYSTEMNAME);
        t.setSelectedItem(s4);
        assertEquals(s4, t.getSelectedItem());

        t.setDisplayOrder(DisplayOptions.USERNAME);
        t.setSelectedItem(s2);
        assertEquals(s2, t.getSelectedItem());
    }

    @Test
    public void testSensorNameChange() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME);

        assertEquals("IS1", t.getSelectedItemDisplayName());

        s1.setUserName("Sensor 1");
        assertEquals("Sensor 1", t.getSelectedItemDisplayName());

        s1.setUserName("new name");
        assertEquals("new name", t.getSelectedItemDisplayName());
    }

    @Test
    public void testSensorAddTracking() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, DisplayOptions.DISPLAYNAME);
        assertEquals(1, t.getItemCount());

        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName(null);
        assertEquals(2, t.getItemCount());

        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        assertEquals(3, t.getItemCount());
    }

    @Test
    public void testIsProviding() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        assertTrue(t.isProviding());
        t.setProviding(false);
        assertFalse(t.isProviding());
        t.setProviding(true);
        assertTrue(t.isProviding());
    }

    @Test
    public void testGetManager() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        assertEquals("Manager is as expected", m, t.getManager());
    }

    @Test
    public void testDispose() {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        assertEquals("Manager has no listeners", 0, m.getPropertyChangeListeners().length);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        assertEquals("Manager has two listeners", 2, m.getPropertyChangeListeners().length);
        t.dispose();
        assertEquals("Manager has no listeners", 0, m.getPropertyChangeListeners().length);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NamedBeanComboBoxTest.class);

}