package jmri.swing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.alexandriasoftware.swing.JInputValidatorPreferences;
import com.alexandriasoftware.swing.Validation;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

/**
 *
 * @author Randall Wood Copyright 2019
 */
public class SystemNameValidatorTest {
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getValidation method, of class SystemNameValidator.
     */
    @Test
    public void testGetValidation() {
        JTextComponent component = new JTextField();
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        SystemNameValidator instance = new SystemNameValidator(component, manager);
        JInputValidatorPreferences preferences = JInputValidatorPreferences.getPreferences();
        instance.setRequired(true);
        // test empty string with required == true
        component.setText("");
        assertEquals(Validation.Type.WARNING, instance.getValidation(component, preferences).getType());
        // test invalid name with required == true
        component.setText("invalid");
        assertEquals(Validation.Type.DANGER, instance.getValidation(component, preferences).getType());
        // test prefix with required == true
        component.setText(manager.getSystemNamePrefix());
        assertEquals(Validation.Type.WARNING, instance.getValidation(component, preferences).getType());
        // test valid name with required == true
        component.setText("IS123");
        assertEquals(Validation.Type.SUCCESS, instance.getValidation(component, preferences).getType());
        instance.setRequired(false);
        // test empty string with required == false
        component.setText("");
        assertEquals(Validation.Type.NONE, instance.getValidation(component, preferences).getType());
        // test invalid name with required == false
        component.setText("invalid");
        assertEquals(Validation.Type.DANGER, instance.getValidation(component, preferences).getType());
        // test prefix with required == false
        component.setText(manager.getSystemNamePrefix());
        assertEquals(Validation.Type.WARNING, instance.getValidation(component, preferences).getType());
        // test valid name with required == false
        component.setText("IS123");
        assertEquals(Validation.Type.SUCCESS, instance.getValidation(component, preferences).getType());
    }

    /**
     * Test of isRequired and setRequired methods, of class SystemNameValidator.
     */
    @Test
    public void testIsRequired() {
        JTextComponent component = new JTextField();
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        SystemNameValidator instance = new SystemNameValidator(component, manager);
        assertFalse(instance.isRequired());
        instance.setRequired(false);
        assertFalse(instance.isRequired());
        instance.setRequired(true);
        assertTrue(instance.isRequired());
        instance.setRequired(true);
        assertTrue(instance.isRequired());
        instance.setRequired(false);
        assertFalse(instance.isRequired());
    }
    
}
