package jmri.util.swing;

import java.awt.GraphicsEnvironment;
import java.util.*;

import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017	
 */
public class JmriBeanComboBoxTest {

    @Test
    public void testSensorSimpleCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Manager m = InstanceManager.getDefault(jmri.SensorManager.class);
        JmriBeanComboBox t = new JmriBeanComboBox(m);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSensorFullCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        m.provideSensor("IS1").setUserName("Sensor 1");
        Sensor s = m.provideSensor("IS2");
        s.setUserName("Sensor 2");
        m.provideSensor("IS3").setUserName("Sensor 3");
        
        JmriBeanComboBox t = new JmriBeanComboBox(m, s, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertNotNull("exists",t);
        Assert.assertEquals(s, t.getSelectedBean());
        Assert.assertEquals("Sensor 2", t.getSelectedUserName());
        Assert.assertEquals("IS2", t.getSelectedSystemName());
        Assert.assertEquals("Sensor 2", t.getSelectedItem());  // Display name is user name if present
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
        
        JmriBeanComboBox t = new JmriBeanComboBox(m, s2, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        Assert.assertNotNull("exists",t);

        // s2 checked in prior test, change selection without repeating
        t.setSelectedBean(s3);
        Assert.assertEquals(s3, t.getSelectedBean());
        Assert.assertEquals("Sensor 3", t.getSelectedUserName());
        Assert.assertEquals("IS3", t.getSelectedSystemName());
        Assert.assertEquals("Sensor 3", t.getSelectedItem());  // Display name is user name if present
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
        
        JmriBeanComboBox t = new JmriBeanComboBox(m, s2, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertEquals(4, t.getItemCount());
        Assert.assertEquals(s2, t.getSelectedBean());

        t.excludeItems(Arrays.asList(new NamedBean[]{s4}));
        Assert.assertNotNull(t.getExcludeItems());

        Assert.assertEquals(3, t.getItemCount());
        Assert.assertEquals(s2, t.getSelectedBean());

        t.excludeItems(Arrays.asList(new NamedBean[]{s2, s4}));

        Assert.assertEquals(2, t.getItemCount());
        Assert.assertTrue(! s2.equals(t.getSelectedBean())); // just has to change, don't care what to
    }

    @Test
    public void testSensorChangeDisplayMode() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        
        JmriBeanComboBox t = new JmriBeanComboBox(m, s1, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        Assert.assertNotNull("exists",t);
        Assert.assertEquals(JmriBeanComboBox.DisplayOptions.DISPLAYNAME, t.getDisplayOrder());

        Assert.assertEquals("Sensor 1", t.getSelectedItem());

        t.setDisplayOrder(JmriBeanComboBox.DisplayOptions.SYSTEMNAME);
        Assert.assertEquals(JmriBeanComboBox.DisplayOptions.SYSTEMNAME, t.getDisplayOrder());
        Assert.assertEquals("IS1", t.getSelectedItem());

        t.setDisplayOrder(JmriBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME);
        Assert.assertEquals(JmriBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME, t.getDisplayOrder());
        Assert.assertEquals("Sensor 1 - IS1", t.getSelectedItem());

        t.setDisplayOrder(JmriBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME);
        Assert.assertEquals(JmriBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME, t.getDisplayOrder());
        Assert.assertEquals("IS1 - Sensor 1", t.getSelectedItem());
    }

    @Test
    public void testSensorSetAndDefaultValidate() {
        Manager m = InstanceManager.getDefault(jmri.SensorManager.class);
        JmriBeanComboBox t = new JmriBeanComboBox(m);
        
        Assert.assertTrue(!t.isValidateMode());
        
        t.setValidateMode(true);
        Assert.assertTrue(t.isValidateMode());

        t.setValidateMode(false);
        Assert.assertTrue(!t.isValidateMode());
        
    }

    @Test
    public void testSensorSetBean() {
        // this test's structure is based on the code structure, which cares a lot about mode
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
        
        JmriBeanComboBox t = new JmriBeanComboBox(m, s1, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertEquals("Sensor 1", t.getSelectedItem());

        t.setSelectedBean(s2);
        Assert.assertEquals(s2, t.getSelectedBean());
        
        t.setDisplayOrder(JmriBeanComboBox.DisplayOptions.SYSTEMNAME);
        t.setSelectedBean(s3);
        Assert.assertEquals(s3, t.getSelectedBean());

        t.setDisplayOrder(JmriBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME);
        t.setSelectedBean(s4);
        Assert.assertEquals(s4, t.getSelectedBean());

        t.setDisplayOrder(JmriBeanComboBox.DisplayOptions.SYSTEMNAMEUSERNAME);
        t.setSelectedBean(s3);
        Assert.assertEquals(s3, t.getSelectedBean());

        t.setDisplayOrder(JmriBeanComboBox.DisplayOptions.USERNAME);
        t.setSelectedBean(s2);
        Assert.assertEquals(s2, t.getSelectedBean());
    }

    //@Test - not working yet
    public void testSensorNameChange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        
        JmriBeanComboBox t = new JmriBeanComboBox(m, s1, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

        Assert.assertEquals("IS1", t.getSelectedItem());

        s1.setUserName("Sensor 1");
        Assert.assertEquals("Sensor 1", t.getSelectedItem());

        s1.setUserName("new name");
        Assert.assertEquals("new name", t.getSelectedItem());
    }

    //@Test - not working yet
    public void testSensorAddTracking() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorManager m = InstanceManager.getDefault(jmri.SensorManager.class);
        Sensor s1 = m.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        
        JmriBeanComboBox t = new JmriBeanComboBox(m, s1, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        Assert.assertEquals(1, t.getItemCount());

        Sensor s2 = m.provideSensor("IS2");
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

    // private final static Logger log = LoggerFactory.getLogger(JmriBeanComboBoxTest.class);

}
