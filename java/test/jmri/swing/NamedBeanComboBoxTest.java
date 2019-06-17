package jmri.swing;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import com.alexandriasoftware.swing.JInputValidator;
import com.alexandriasoftware.swing.Validation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class NamedBeanComboBoxTest {

    @Test
    public void testSensorSimpleCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Manager<Sensor> m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testSensorFullCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        m.provideSensor("IS1").setUserName("Sensor 1");
        Sensor s = m.provideSensor("IS2");
        s.setUserName("Sensor 2");
        m.provideSensor("IS3").setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s, NamedBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertNotNull("exists", t);
        Assert.assertEquals(s, t.getSelectedItem());
        Assert.assertEquals("Sensor 2", t.getSelectedItemUserName());
        Assert.assertEquals("IS2", t.getSelectedItemSystemName());
        Assert.assertEquals("Sensor 2", t.getSelectedItemDisplayName()); // Display name is user name if present
    }

    @Test
    public void testSensorSelectEntry() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s2, NamedBeanComboBox.DisplayOptions.DISPLAYNAME);
        Assert.assertNotNull("exists", t);

        // s2 checked in prior test, change selection without repeating
        t.setSelectedItem(s3);
        Assert.assertEquals(s3, t.getSelectedItem());
        Assert.assertEquals("Sensor 3", t.getSelectedItemUserName());
        Assert.assertEquals("IS3", t.getSelectedItemSystemName());
        Assert.assertEquals("Sensor 3", t.getSelectedItemDisplayName()); // Display name is user name if present
    }

    @Test
    public void testSensorExcludeSome() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        Sensor s4 = m.provideSensor("IS4");
        s4.setUserName("Sensor 4");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s2, NamedBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertEquals(4, t.getItemCount());
        Assert.assertEquals(s2, t.getSelectedItem());

        t.setExcludedItems(new HashSet<>(Arrays.asList(new Sensor[]{s4})));
        Assert.assertNotNull(t.getExcludedItems());

        Assert.assertEquals(3, t.getItemCount());
        Assert.assertEquals(s2, t.getSelectedItem());

        t.setExcludedItems(new HashSet<>(Arrays.asList(new Sensor[]{s2, s4})));

        Assert.assertEquals(2, t.getItemCount());
        Assert.assertTrue(!s2.equals(t.getSelectedItem())); // just has to change, don't care what to
    }

    @Test
    public void testSensorChangeDisplayMode() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, NamedBeanComboBox.DisplayOptions.DISPLAYNAME);
        JList<Sensor> l = new JList<>(t.getModel());
        Assert.assertNotNull("exists", t);
        Assert.assertEquals(NamedBeanComboBox.DisplayOptions.DISPLAYNAME, t.getDisplayOrder());

        Assert.assertEquals("Sensor 1",
                ((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText());

        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.SYSTEMNAME);
        Assert.assertEquals(NamedBeanComboBox.DisplayOptions.SYSTEMNAME, t.getDisplayOrder());
        Assert.assertEquals("IS1",
                ((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText());

        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME);
        Assert.assertEquals(NamedBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME, t.getDisplayOrder());
        Assert.assertEquals("Sensor 1 (IS1)",
                ((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText());

        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME);
        Assert.assertEquals(NamedBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME, t.getDisplayOrder());
        Assert.assertEquals("IS1 (Sensor 1)",
                ((JLabel) t.getRenderer().getListCellRendererComponent(l, s1, 0, false, false)).getText());
    }

    @Test
    public void testSensorSetAndDefaultValidate() {
        Manager<Sensor> m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);

        Assert.assertTrue(!t.isValidatingInput());

        t.setValidatingInput(true);
        Assert.assertTrue(t.isValidatingInput());

        t.setValidatingInput(false);
        Assert.assertTrue(!t.isValidatingInput());

    }

    @Test
    public void testSensorEditText()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.SYSTEMNAME);
        t.setAllowNull(true);
        t.setEditable(true);
        JTextField c = ((JTextField) t.getEditor().getEditorComponent());

        Assert.assertEquals("", c.getText());

        c.setText("IS2");
        Assert.assertEquals("IS2", c.getText());
        // because setting the text *does not* trigger validation and selection
        // we need to manually force validation by calling a private method
        Method method = t.getClass().getDeclaredMethod("validateInput");
        method.setAccessible(true);
        method.invoke(t);
        Assert.assertEquals(s2, t.getSelectedItem());
    }

    @Test
    public void testSensorTestValidity()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m);
        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME);
        t.setAllowNull(true);
        t.setEditable(true);
        JTextField c = ((JTextField) t.getEditor().getEditorComponent());
        Method method = t.getClass().getDeclaredMethod("validateInput");
        method.setAccessible(true);

        // test with no matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);

        c.setText("");
        Assert.assertEquals("", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("IS1");
        Assert.assertEquals("IS1", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("K");
        Assert.assertEquals("K", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        // test with no matching bean and isValidatingInput() == true
        // should match NONE when empty and DANGER otherwise
        t.setValidatingInput(true);

        c.setText("");
        Assert.assertEquals("", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("IS1");
        Assert.assertEquals("IS1", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.DANGER, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("K");
        Assert.assertEquals("K", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.DANGER, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        // test with a matching bean and isValidatingInput() == false
        // should always match NONE
        t.setValidatingInput(false);
        m.provide("IS1");

        c.setText("");
        Assert.assertEquals("", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("IS1");
        Assert.assertEquals("IS1", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("K");
        Assert.assertEquals("K", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        // test with a matching bean and isValidatingInput() == true
        // should match DANGER with text "K" and NONE otherwise
        t.setValidatingInput(true);

        c.setText("");
        Assert.assertEquals("", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("IS1");
        Assert.assertEquals("IS1", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.NONE, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());

        c.setText("K");
        Assert.assertEquals("K", c.getText());
        method.invoke(t);
        Assert.assertEquals(Validation.Type.DANGER, ((JInputValidator) ((JComponent) t.getEditor().getEditorComponent()).getInputVerifier()).getValidation().getType());
    }

    @Test
    public void testSensorSetBean() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName("Sensor 2");
        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        Sensor s4 = m.provideSensor("IS4");
        s4.setUserName("Sensor 4");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, NamedBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertEquals("Sensor 1", t.getSelectedItemDisplayName());

        t.setSelectedItem(s2);
        Assert.assertEquals(s2, t.getSelectedItem());

        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.SYSTEMNAME);
        t.setSelectedItem(s3);
        Assert.assertEquals(s3, t.getSelectedItem());

        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME);
        t.setSelectedItem(s4);
        Assert.assertEquals(s4, t.getSelectedItem());

        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME);
        t.setSelectedItem(s3);
        Assert.assertEquals(s3, t.getSelectedItem());

        t.setDisplayOrder(NamedBeanComboBox.DisplayOptions.USERNAME);
        t.setSelectedItem(s2);
        Assert.assertEquals(s2, t.getSelectedItem());
    }

    @Test
    public void testSensorNameChange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, NamedBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertEquals("IS1", t.getSelectedItemDisplayName());

        s1.setUserName("Sensor 1");
        Assert.assertEquals("Sensor 1", t.getSelectedItemDisplayName());

        s1.setUserName("new name");
        Assert.assertEquals("new name", t.getSelectedItemDisplayName());
    }

    @Test
    public void testSensorAddTracking() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");

        NamedBeanComboBox<Sensor> t = new NamedBeanComboBox<>(m, s1, NamedBeanComboBox.DisplayOptions.DISPLAYNAME);
        Assert.assertEquals(1, t.getItemCount());

        Sensor s2 = m.provideSensor("IS2");
        s2.setUserName(null);
        Assert.assertEquals(2, t.getItemCount());

        Sensor s3 = m.provideSensor("IS3");
        s3.setUserName("Sensor 3");
        Assert.assertEquals(3, t.getItemCount());
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