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

/*
* Tests for the ConditionalListEdit Class.
*
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalListEditTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ConditionalListEdit Constructor Return", new ConditionalListEdit());  // NOI18N
    }

    @Test
    public void addConditionalTest() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new ConditionalListEdit("IX101");

        JFrameOperator editFrame = new JFrameOperator(Bundle.getMessage("TitleEditLogix"));  // NOI18N
        Assert.assertNotNull(editFrame);

        new JButtonOperator(editFrame, Bundle.getMessage("NewConditionalButton")).push();  // NOI18N
        JFrameOperator cdlFrame = new JFrameOperator(Bundle.getMessage("TitleEditConditional"));  // NOI18N
        Assert.assertNotNull(cdlFrame);
        new JTextFieldOperator(cdlFrame, 0).setText("IX101 Conditional 1");  // NOI18N

        // Add a state variable
        new JButtonOperator(cdlFrame, Bundle.getMessage("AddVariableButton")).push();  // NOI18N
        JFrameOperator varFrame = new JFrameOperator(Bundle.getMessage("TitleEditVariable"));  // NOI18N
        Assert.assertNotNull(varFrame);
        
        // Select Sensor, add name, set Inactive, Update
        new JComboBoxOperator(varFrame, 0).selectItem(Bundle.getMessage("BeanNameSensor"));  // NOI18N
        new JTextFieldOperator(varFrame, 0).setText("Sensor 1");  // NOI18N
        new JComboBoxOperator(varFrame, Bundle.getMessage("SensorStateActive")).selectItem(Bundle.getMessage("SensorStateInactive"));  // NOI18N
        new JButtonOperator(varFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        
        // Add an action
        new JButtonOperator(cdlFrame, Bundle.getMessage("addActionButton")).push();  // NOI18N
        JFrameOperator actFrame = new JFrameOperator(Bundle.getMessage("TitleEditAction"));  // NOI18N
        Assert.assertNotNull(actFrame);

        // Select Turnout, add name, set Turnout, set Thrown, Update
        new JComboBoxOperator(actFrame, 0).selectItem(Bundle.getMessage("BeanNameTurnout"));  // NOI18N
        new JTextFieldOperator(actFrame, 0).setText("Turnout 1");  // NOI18N
        new JComboBoxOperator(actFrame, 1).selectItem(Bundle.getMessage("ActionSetTurnout"));  // NOI18N
        new JComboBoxOperator(actFrame, Bundle.getMessage("TurnoutStateClosed")).selectItem(Bundle.getMessage("TurnoutStateThrown"));  // NOI18N
        new JButtonOperator(actFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        
        new JButtonOperator(cdlFrame, Bundle.getMessage("ButtonUpdate")).push();  // NOI18N
        new JButtonOperator(editFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrit.conditional.CreateTestObjects.createTestObjects();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
