package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertNotNull( section1, "section is not null");
        section1.setState(Section.FREE);

        expression2 = new ExpressionSection("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Section \"''\" is Free", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionSection("IQDE321", "My Section");
        assertNotNull( expression2, "object exists");
        assertEquals( "My Section", expression2.getUserName(), "Username matches");
        assertEquals( "Section \"''\" is Free", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionSection("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(section1);
        assertSame( section1, expression2.getSelectNamedBean().getNamedBean().getBean(), "section is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Section \"section1\" is Free", expression2.getLongDescription(), "String matches");

        Section t = InstanceManager.getDefault(SectionManager.class).createNewSection("section2");
        expression2 = new ExpressionSection("IQDE321", "My section");
        expression2.getSelectNamedBean().setNamedBean(t);
        assertSame( t, expression2.getSelectNamedBean().getNamedBean().getBean(), "section is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My section", expression2.getUserName(), "Username matches");
        assertEquals( "Section \"section2\" is Free", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var es = new ExpressionSection("IQE55:12:XY11", null);
            fail("Not thrown, created " + es);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var es = new ExpressionSection("IQE55:12:XY11", "A name");
            fail("Not thrown, created " + es);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionSection.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionSection.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testSectionState() {
        assertEquals( "Free", ExpressionSection.SectionState.Free.toString(), "String matches");
        assertEquals( "Forward", ExpressionSection.SectionState.Forward.toString(), "String matches");

        assertEquals( Section.FREE, ExpressionSection.SectionState.Free.getID(), "ID matches");
        assertEquals( Section.FORWARD, ExpressionSection.SectionState.Forward.getID(), "ID matches");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionSection.getSelectNamedBean().removeNamedBean();
        assertEquals("Section", expressionSection.getShortDescription());
        assertEquals("Section \"''\" is Free", expressionSection.getLongDescription());
        expressionSection.getSelectNamedBean().setNamedBean(section1);
        expressionSection.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSection.getSelectEnum().setEnum(ExpressionSection.SectionState.Free);
        assertEquals("Section \"section1\" is Free", expressionSection.getLongDescription());
        expressionSection.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals("Section \"section1\" is not Free", expressionSection.getLongDescription());
        expressionSection.getSelectEnum().setEnum(ExpressionSection.SectionState.Forward);
        assertEquals("Section \"section1\" is not Forward", expressionSection.getLongDescription());
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
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Change the section twice to trigger the "then" state
        section1.setState(Section.FORWARD);
        section1.setState(Section.FREE);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");

        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Change the section to trigger the "else" state.
        section1.setState(Section.FORWARD);
        // The action should now be executed so the atomic boolean should still be false since the action is the else.
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionSection.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Create two events to trigger on change to the "then" state.
        section1.setState(Section.FREE);
        section1.setState(Section.FORWARD);
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    @Test
    public void testSetSection() {
        expressionSection.unregisterListeners();

        Section otherSection = InstanceManager.getDefault(SectionManager.class).createNewSection("sectionX");
        assertNotEquals( otherSection, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Sections are different");
        expressionSection.getSelectNamedBean().setNamedBean(otherSection);
        assertEquals( otherSection, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Sections are equal");

        NamedBeanHandle<Section> otherSectionHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherSection.getDisplayName(), otherSection);
        expressionSection.getSelectNamedBean().removeNamedBean();
        assertNull( expressionSection.getSelectNamedBean().getNamedBean(), "Section is null");
        expressionSection.getSelectNamedBean().setNamedBean(otherSectionHandle);
        assertEquals( otherSection, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Sections are equal");
        assertEquals( otherSectionHandle, expressionSection.getSelectNamedBean().getNamedBean(), "SectionHandles are equal");
    }

    @Test
    public void testSetSectionException() {
        assertNotNull( section1, "Section is not null");
        assertNotNull( expressionSection.getSelectNamedBean().getNamedBean(), "Section is not null");
        expressionSection.registerListeners();
        RuntimeException exc = assertThrows( RuntimeException.class, () ->
            expressionSection.getSelectNamedBean().setNamedBean("A section"),
                "Expected exception thrown");
        assertNotNull(exc);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        exc = assertThrows( RuntimeException.class, () -> {
            Section section99 = InstanceManager.getDefault(SectionManager.class).createNewSection("section99");
            NamedBeanHandle<Section> sectionHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(section99.getDisplayName(), section99);
            expressionSection.getSelectNamedBean().setNamedBean(sectionHandle99);
        }, "Expected exception thrown");
        assertNotNull(exc);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        exc = assertThrows( RuntimeException.class, () ->
            expressionSection.getSelectNamedBean().removeNamedBean(),
                "Expected exception thrown");
        assertNotNull(exc);
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
        assertNotNull( section1, "Section is not null");
        expressionSection.getSelectNamedBean().setNamedBean(section1);

        // Get some other section for later use
        Section otherSection = InstanceManager.getDefault(SectionManager.class).createNewSection("sectionQ");
        assertNotNull( otherSection, "Section is not null");
        assertNotEquals( section1, otherSection, "Section is not equal");

        // Test vetoableChange() for some other propery
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( section1, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Section matches");

        // Test vetoableChange() for a string
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( section1, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Section matches");
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( section1, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Section matches");

        // Test vetoableChange() for another section
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSection, null));
        assertEquals( section1, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Section matches");
        expressionSection.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSection, null));
        assertEquals( section1, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Section matches");

        // Test vetoableChange() for its own section
        PropertyVetoException exc = assertThrows( PropertyVetoException.class, () ->
            expressionSection.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", section1, null)),
                "Expected exception thrown");
        assertNotNull(exc);

        assertEquals( section1, expressionSection.getSelectNamedBean().getNamedBean().getBean(), "Section matches");
        expressionSection.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", section1, null));
        assertNull( expressionSection.getSelectNamedBean().getNamedBean(), "Section is null");
    }

    @Before
    @BeforeEach
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

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSectionTest.class);

}
