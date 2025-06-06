package jmri.jmrit.conditional;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.Assert;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/*
* Tests for the ConditionalTreeEdit Class.
*
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalTreeEditTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ConditionalTreeEdit Constructor Return", new ConditionalTreeEdit());  // NOI18N
    }

    @Test
    @DisabledIfHeadless
    public void addConditionalTest() {

        ConditionalTreeEdit t = new ConditionalTreeEdit("IX102");
        Assertions.assertNotNull(t);

        JFrameOperator editFrame = new JFrameOperator(Bundle.getMessage("TitleEditLogix"));  // NOI18N
        Assert.assertNotNull(editFrame);

        JTreeOperator jto = new JTreeOperator(editFrame);
        Assert.assertNotNull(jto);

        // Create a new conditional, select the name field and give it a name
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAddText")).push();  // NOI18N
        JTextFieldOperator cdlName = new JTextFieldOperator(editFrame, 1);
        cdlName.clickMouse();
        cdlName.setText("IX102 Conditional 2");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Expand the Conditional, select the Variables row and select Add
        jto.expandRow(1);
        jto.selectRow(3);

        // Add a sensor Variable
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAddText")).push();  // NOI18N
        new JComboBoxOperator(editFrame, 0).selectItem(Bundle.getMessage("BeanNameSensor"));  // NOI18N
        new JTextFieldOperator(editFrame, 1).setText("Sensor 2");  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        jto.selectRow(7);

        // Add a turnout Action
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAddText")).push();  // NOI18N
        new JComboBoxOperator(editFrame, 0).selectItem(Bundle.getMessage("BeanNameTurnout"));  // NOI18N
        new JTextFieldOperator(editFrame, 1).setText("Turnout 2");  // NOI18N
        new JComboBoxOperator(editFrame, 1).selectItem(Bundle.getMessage("ActionSetTurnout"));  // NOI18N
        new JComboBoxOperator(editFrame, Bundle.getMessage("TurnoutStateClosed")).selectItem(Bundle.getMessage("TurnoutStateThrown"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        // Add a sensor Action
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonAddText")).push();  // NOI18N
        new JComboBoxOperator(editFrame, 0).selectItem(Bundle.getMessage("BeanNameSensor"));  // NOI18N
        new JTextFieldOperator(editFrame, 1).setText("Sensor 3");  // NOI18N
        new JComboBoxOperator(editFrame, 1).selectItem(Bundle.getMessage("ActionSetSensor"));  // NOI18N
        new JComboBoxOperator(editFrame, Bundle.getMessage("SensorStateActive")).selectItem(Bundle.getMessage("SensorStateInactive"));  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N

        new JButtonOperator(editFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }

    @Test
    @DisabledIfHeadless
    public void singlePickTest() {
        // Edit a conditional using a single pick list

        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                setProperty("jmri.jmrit.beantable.LogixTableAction", "Selection Mode", "USESINGLE");  // NOI18N
        new ConditionalTreeEdit("IX102");

        JFrameOperator editFrame = new JFrameOperator(Bundle.getMessage("TitleEditLogix"));  // NOI18N
        Assert.assertNotNull(editFrame);

        JTreeOperator jto = new JTreeOperator(editFrame);
        Assert.assertNotNull(jto);

        // Expand the Conditional, expand the Variables row and select a variable
        jto.expandRow(0);
        jto.expandRow(2);
        jto.selectRow(3);

        // Click the name field, and select a row from the pick single table
        JTextFieldOperator varName = new JTextFieldOperator(editFrame, 1);
        varName.clickMouse();
        JFrameOperator pickFrame = new JFrameOperator(Bundle.getMessage("SinglePickFrame"));  // NOI18N
        Assert.assertNotNull(pickFrame);
        JTableOperator tableOp = new JTableOperator(pickFrame);
        Assert.assertNotNull(tableOp);
        tableOp.clickOnCell(2, 1);

        // Cancel and end the edit
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonCancel")).push();  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }

    @Test
    @DisabledIfHeadless
    public void comboBoxTest() {
        // Edit a conditional using the combo box option
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                setProperty("jmri.jmrit.beantable.LogixTableAction", "Selection Mode", "USECOMBO");  // NOI18N
        new ConditionalTreeEdit("IX102");

        JFrameOperator editFrame = new JFrameOperator(Bundle.getMessage("TitleEditLogix"));  // NOI18N
        Assert.assertNotNull(editFrame);

        JTreeOperator jto = new JTreeOperator(editFrame);
        Assert.assertNotNull(jto);

        // Expand the Conditional, expand the Variables row and select a variable
        jto.expandRow(0);
        jto.expandRow(2);
        jto.selectRow(4);
        Sensor sensor4 = InstanceManager.getDefault(SensorManager.class).getByUserName("Sensor 4"); // NOI18N
        new JComboBoxOperator(editFrame, 2).getModel().setSelectedItem(sensor4);

        // Cancel and end the edit
        JButtonOperator jbo = new JButtonOperator(editFrame, Bundle.getMessage("ButtonCancel"));  // NOI18N
        if (jbo.isEnabled()) {
            jbo.push();
        }
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        CreateTestObjects.createTestObjects();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionalTreeEditTest.class);
}
