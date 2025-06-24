package jmri.jmrit.conditional;

import javax.swing.JFrame;
import javax.swing.JTextField;

import jmri.Conditional;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/*
* Tests for the ConditionalEditBase Class
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalEditBaseTest {

    @Test
    public void testCtor() {
        Assertions.assertNotNull( new ConditionalEditBase(), "ConditionalEditBase Constructor Return");
    }

    @Test
    public void testStringCtor() {
        Assertions.assertNotNull( new ConditionalEditBase("IX101"), "ConditionalEditBase Constructor Return");
    }

    @Test
    public void testNameBox() {
        ConditionalEditBase cdlBase = new ConditionalEditBase();

        Assertions.assertNotNull( cdlBase.createNameBox(Conditional.ItemType.SENSOR),
            "ConditionalEditBase createNameBox Return not null");
        Assertions.assertNull( cdlBase.createNameBox(Conditional.ItemType.CLOCK),
            "ConditionalEditBase createNameBox Return null");
    }

    @Test
    @DisabledIfHeadless
    public void testPickListTables() {
        ConditionalEditBase cdlBase = new ConditionalEditBase();

        cdlBase.openPickListTable();
        cdlBase.hidePickListTable();
        cdlBase.setPickListTab(Conditional.ItemType.SENSOR, true);

        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("TitlePickList"), false, false);  // NOI18N
        Assertions.assertNotNull(frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    @DisabledIfHeadless
    public void testSinglePickList() {
        ConditionalEditBase cdlBase = new ConditionalEditBase();

        JTextField _actionNameField = new JTextField("");

        cdlBase.createSinglePanelPickList(Conditional.ItemType.TURNOUT, cdlBase.new PickSingleListener(_actionNameField, Conditional.ItemType.TURNOUT), true);
        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("SinglePickFrame"), false, false);  // NOI18N
        Assertions.assertNotNull(frame);

        JTableOperator tableOp = new JTableOperator(new JFrameOperator(frame));
        tableOp.clickOnCell(2, 1);
        Assertions.assertEquals("Turnout 3", _actionNameField.getText());

        JUnitUtil.dispose(frame);
    }

    @Test
    @DisabledIfHeadless
    public void testValidators() {
        ConditionalEditBase cdlBase = new ConditionalEditBase("LX101");  // NOI18N

        Assertions.assertNotNull( cdlBase.validateTurnoutReference("Turnout 1"),
            "ConditionalEditBase validateTurnoutReference Return not null");
        Assertions.assertNotNull( cdlBase.validateSensorReference("Sensor 1"),
            "ConditionalEditBase validateSensorReference Return not null");
        Assertions.assertNotNull( cdlBase.validateLogixReference("Logix 102"),
            "ConditionalEditBase validateLogixReference Return not null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        CreateTestObjects.createTestObjects();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
