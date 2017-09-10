package jmri.jmrit.conditional;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/*
* Tests for the ConditionalTreeEdit Class
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalTreeEditTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ConditionalTreeEdit Constructor Return", new ConditionalTreeEdit());  // NOI18N
    }

    @Test
    public void addConditionalTest() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConditionalTreeEdit cdlTreeEdit = new ConditionalTreeEdit("IX102");  // NOI18N

        JFrameOperator editFrame = new JFrameOperator(Bundle.getMessage("TitleEditLogix"));  // NOI18N
        Assert.assertNotNull(editFrame);

        JTreeOperator jto = new JTreeOperator(editFrame);
        Assert.assertNotNull(jto);

        // Create a new conditional, select the name field and give it a name
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAdd")).push();  // NOI18N
        JTextFieldOperator cdlName = new JTextFieldOperator(editFrame, 1);
        cdlName.clickMouse();
        cdlName.setText("IX102 Conditional 2");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Expand the Conditional, select the Variables row and select Add
        jto.expandRow(1);
        jto.selectRow(3);
        
        // Add a sensor Variable
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAdd")).push();  // NOI18N
        new JComboBoxOperator(editFrame, 0).selectItem(Bundle.getMessage("BeanNameSensor"));  // NOI18N
        new JTextFieldOperator(editFrame, 1).setText("Sensor 2");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        jto.selectRow(7);

        // Add a turnout Action
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAdd")).push();  // NOI18N
        new JComboBoxOperator(editFrame, 0).selectItem(Bundle.getMessage("BeanNameTurnout"));  // NOI18N
        new JTextFieldOperator(editFrame, 1).setText("Turnout 2");  // NOI18N
        new JComboBoxOperator(editFrame, 1).selectItem(Bundle.getMessage("ActionSetTurnout"));  // NOI18N
        new JComboBoxOperator(editFrame, Bundle.getMessage("TurnoutStateClosed")).selectItem(Bundle.getMessage("TurnoutStateThrown"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a sensor Action
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAdd")).push();  // NOI18N
        new JComboBoxOperator(editFrame, 0).selectItem(Bundle.getMessage("BeanNameSensor"));  // NOI18N
        new JTextFieldOperator(editFrame, 1).setText("Sensor 3");  // NOI18N
        new JComboBoxOperator(editFrame, 1).selectItem(Bundle.getMessage("ActionSetSensor"));  // NOI18N
        new JComboBoxOperator(editFrame, Bundle.getMessage("SensorStateActive")).selectItem(Bundle.getMessage("SensorStateInactive"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        new JButtonOperator(editFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrit.conditional.CreateTestObjects.createTestObjects();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
