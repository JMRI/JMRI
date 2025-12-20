package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionSection
 *
 * @author Dave Sand 2023
 */
public class ExpressionSectionTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionSection expressionSection;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private Section section1;


    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Section \"section1\" is Free ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Section \"section1\" is Free ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionBlock(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        ExpressionSection expression2;
        Assert.assertNotNull("section is not null", section1);
        section1.setState(Section.FREE);

        expression2 = new ExpressionSection("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Section \"''\" is Free", expression2.getLongDescription());

        expression2 = new ExpressionSection("IQDE321", "My Section");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My Section", expression2.getUserName());
        Assert.assertEquals("String matches", "Section \"''\" is Free", expression2.getLongDescription());

        expression2 = new ExpressionSection("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(section1);
        Assert.assertTrue("section is correct", section1 == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Section \"section1\" is Free", expression2.getLongDescription());

        Section t = InstanceManager.getDefault(SectionManager.class).createNewSection("section2");
        expression2 = new ExpressionSection("IQDE321", "My section");
        expression2.getSelectNamedBean().setNamedBean(t);
        Assert.assertTrue("section is correct", t == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My section", expression2.getUserName());
        Assert.assertEquals("String matches", "Section \"section2\" is Free", expression2.getLongDescription());

        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionSection("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new ExpressionSection("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionSection.getChildCount());

        boolean hasThrown = false;
        try {
            expressionSection.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testSectionState() {
        Assert.assertEquals("String matches", "Free", ExpressionSection.SectionState.Free.toString());
        Assert.assertEquals("String matches", "Forward", ExpressionSection.SectionState.Forward.toString());

        Assert.assertEquals("ID matches", Section.FREE, ExpressionSection.SectionState.Free.getID());
        Assert.assertEquals("ID matches", Section.FORWARD, ExpressionSection.SectionState.Forward.getID());
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", LogixNG_Category.ITEM == _base.getCategory());
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionSection.getSelectNamedBean().removeNamedBean();
        Assert.assertEquals("Section", expressionSection.getShortDescription());
        Assert.assertEquals("Section \"''\" is Free", expressionSection.getLongDescription());
        expressionSection.getSelectNamedBean().setNamedBean(section1);
        expressionSection.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSection.getSelectEnum().setEnum(ExpressionSection.SectionState.Free);
        Assert.assertEquals("Section \"section1\" is Free", expressionSection.getLongDescription());
        expressionSection.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        Assert.assertEquals("Section \"section1\" is not Free", expressionSection.getLongDescription());
        expressionSection.getSelectEnum().setEnum(ExpressionSection.SectionState.Forward);
        Assert.assertEquals("Section \"section1\" is not Forward", expressionSection.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Set initial states: Section and expression states are "is Free"
        atomicBoolean.set(false);
        section1.setState(Section.FREE);
        expressionSection.getSelectNamedBean().setNamedBean(section1);
        expressionSection.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSection.getSelectEnum().setEnum(ExpressionSection.SectionState.Free);

        // Toggle the section twice since Free is 'then' action. This should not execute the conditional.
        section1.setState(Section.FORWARD);
        section1.setState(Section.FREE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Change the section twice to trigger the "then" state
        section1.setState(Section.FORWARD);
        section1.setState(Section.FREE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());

        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Change the section to trigger the "else" state.
        section1.setState(Section.FORWARD);
        // The action should now be executed so the atomic boolean should still be false since the action is the else.
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Test IS_NOT
        expressionSection.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Create two events to trigger on change to the "then" state.
        section1.setState(Section.FREE);
        section1.setState(Section.FORWARD);
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }

    @Test
    public void testSetSection() {
        expressionSection.unregisterListeners();

        Section otherSection = InstanceManager.getDefault(SectionManager.class).createNewSection("sectionX");
        Assert.assertNotEquals("Sections are different", otherSection, expressionSection.getSelectNamedBean().getNamedBean().getBean());
        expressionSection.getSelectNamedBean().setNamedBean(otherSection);
        Assert.assertEquals("Sections are equal", otherSection, expressionSection.getSelectNamedBean().getNamedBean().getBean());

        NamedBeanHandle<Section> otherSectionHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherSection.getDisplayName(), otherSection);
        expressionSection.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Section is null", expressionSection.getSelectNamedBean().getNamedBean());
        expressionSection.getSelectNamedBean().setNamedBean(otherSectionHandle);
        Assert.assertEquals("Sections are equal", otherSection, expressionSection.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertEquals("SectionHandles are equal", otherSectionHandle, expressionSection.getSelectNamedBean().getNamedBean());
    }

    @Test
    public void testSetSectionException() {
        Assert.assertNotNull("Section is not null", section1);
        Assert.assertNotNull("Section is not null", expressionSection.getSelectNamedBean().getNamedBean());
        expressionSection.registerListeners();
        boolean thrown = false;
        try {
            expressionSection.getSelectNamedBean().setNamedBean("A section");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            Section section99 = InstanceManager.getDefault(SectionManager.class).createNewSection("section99");
            NamedBeanHandle<Section> sectionHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(section99.getDisplayName(), section99);
            expressionSection.getSelectNamedBean().setNamedBean(sectionHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            expressionSection.getSelectNamedBean().removeNamedBean();
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionBlock has no block
        conditionalNG.setEnabled(false);
        expressionSection.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expressionSection and set the section
        Assert.assertNotNull("Section is not null", section1);
        expressionSection.getSelectNamedBean().setNamedBean(section1);

        // Get some other section for later use
        Section otherSection = InstanceManager.getDefault(SectionManager.class).createNewSection("sectionQ");
        Assert.assertNotNull("Section is not null", otherSection);
        Assert.assertNotEquals("Section is not equal", section1, otherSection);

        // Test vetoableChange() for some other propery
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Section matches", section1, expressionSection.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for a string
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Section matches", section1, expressionSection.getSelectNamedBean().getNamedBean().getBean());
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Section matches", section1, expressionSection.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for another section
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSection, null));
        Assert.assertEquals("Section matches", section1, expressionSection.getSelectNamedBean().getNamedBean().getBean());
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSection, null));
        Assert.assertEquals("Section matches", section1, expressionSection.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for its own section
        boolean thrown = false;
        try {
            expressionSection.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", section1, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals("Section matches", section1, expressionSection.getSelectNamedBean().getNamedBean().getBean());
        expressionSection.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", section1, null));
        Assert.assertNull("Section is null", expressionSection.getSelectNamedBean().getNamedBean());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionSection = new ExpressionSection("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSection);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionSection;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        section1 = InstanceManager.getDefault(SectionManager.class).createNewSection("section1");
        expressionSection.getSelectNamedBean().setNamedBean(section1);

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSectionTest.class);

}
