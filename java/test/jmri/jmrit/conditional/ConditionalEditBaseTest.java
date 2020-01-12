package jmri.jmrit.conditional;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JTextField;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import jmri.Conditional;

/*
* Tests for the ConditionalEditBase Class
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalEditBaseTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ConditionalEditBase Constructor Return", new ConditionalEditBase());  // NOI18N
    }

    @Test
    public void testStringCtor() {
        Assert.assertNotNull("ConditionalEditBase Constructor Return", new ConditionalEditBase("IX101"));  // NOI18N
    }

    @Test
    public void testNameBox() {
        ConditionalEditBase cdlBase = new ConditionalEditBase();

        Assert.assertNotNull("ConditionalEditBase createNameBox Return not null", cdlBase.createNameBox(Conditional.ItemType.SENSOR));  // NOI18N
        Assert.assertNull("ConditionalEditBase createNameBox Return null", cdlBase.createNameBox(Conditional.ItemType.CLOCK));  // NOI18N
    }

    @Test
    public void testPickListTables() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConditionalEditBase cdlBase = new ConditionalEditBase();

        cdlBase.openPickListTable();
        cdlBase.hidePickListTable();
        cdlBase.setPickListTab(Conditional.ItemType.SENSOR, true);

        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("TitlePickList"), false, false);  // NOI18N
        Assert.assertNotNull(frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testSinglePickList() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConditionalEditBase cdlBase = new ConditionalEditBase();

        JTextField _actionNameField = new JTextField("");

        cdlBase.createSinglePanelPickList(Conditional.ItemType.TURNOUT, cdlBase.new PickSingleListener(_actionNameField, Conditional.ItemType.TURNOUT), true);
        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("SinglePickFrame"), false, false);  // NOI18N
        Assert.assertNotNull(frame);

        JTableOperator tableOp = new JTableOperator(new JFrameOperator(frame));
        tableOp.clickOnCell(2, 1);
        Assert.assertEquals("Turnout 3", _actionNameField.getText());  // NOI18N

        JUnitUtil.dispose(frame);
    }

    @Test
    public void testValidators() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConditionalEditBase cdlBase = new ConditionalEditBase("LX101");  // NOI18N

        Assert.assertNotNull("ConditionalEditBase validateTurnoutReference Return not null", cdlBase.validateTurnoutReference("Turnout 1"));  // NOI18N
        Assert.assertNotNull("ConditionalEditBase validateSensorReference Return not null", cdlBase.validateSensorReference("Sensor 1"));  // NOI18N
        Assert.assertNotNull("ConditionalEditBase validateLogixReference Return not null", cdlBase.validateLogixReference("Logix 102"));  // NOI18N
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrit.conditional.CreateTestObjects.createTestObjects();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
