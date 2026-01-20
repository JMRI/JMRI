package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.jmrit.logixng.Debugable.DebugConfig;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.Test;

/**
 * Base class for test classes that tests MaleSockets
 */
public abstract class MaleSocketTestBase {

    protected BaseManager<? extends NamedBean> manager;
    protected MaleSocket maleSocketA;
    protected MaleSocket maleSocketB;

    protected abstract String getNewSystemName();

    @Test
    public void testExists() throws JmriException {
        assertNotNull( maleSocketA, "exists");
        assertNotNull( maleSocketB, "exists");
        assertNotEquals( maleSocketA, maleSocketB, "not equals");
    }

    @Test
    public void testLock() {
        maleSocketA.setLocked(false);
        assertFalse(maleSocketA.isLocked());
        maleSocketA.setLocked(true);
        assertTrue(maleSocketA.isLocked());
        maleSocketA.setSystem(false);
        assertFalse(maleSocketA.isSystem());
        maleSocketA.setSystem(true);
        assertTrue(maleSocketA.isSystem());
    }

    @Test
    public void testCategory() throws JmriException {
        assertEquals( maleSocketA.getObject().getCategory(), maleSocketA.getCategory(),
            "category is correct");
        assertEquals( maleSocketB.getObject().getCategory(), maleSocketB.getCategory(),
            "category is correct 2");
        assertNotEquals( maleSocketA.getCategory(), maleSocketB.getCategory(),
            "categories are different");
    }

    @Test
    public void testShortDescription() throws JmriException {
        assertEquals( maleSocketA.getObject().getShortDescription(), maleSocketA.getShortDescription(),
            "getShortDescription() is correct");
        assertEquals( maleSocketB.getObject().getShortDescription(), maleSocketB.getShortDescription(),
            "getShortDescription() is correct");
        assertNotEquals( maleSocketA.getShortDescription(), maleSocketB.getShortDescription(),
            "getShortDescription() are different");
    }

    @Test
    public void testLongDescription() throws JmriException {
        assertEquals( maleSocketA.getObject().getLongDescription(), maleSocketA.getLongDescription(),
            "getLongDescription() is correct");
        assertEquals( maleSocketB.getObject().getLongDescription(), maleSocketB.getLongDescription(),
            "getLongDescription() is correct");
        assertNotEquals( maleSocketA.getLongDescription(), maleSocketB.getLongDescription(),
            "getLongDescription() are different");
    }

    @Test
    public void testGetSystemName() throws JmriException {
        assertNotNull(maleSocketA);
        assertNotNull(maleSocketA.getSystemName());
        assertNotNull(maleSocketA.getObject());
        assertNotNull(maleSocketA.getObject().getSystemName());


        assertEquals( maleSocketA.getObject().getSystemName(), maleSocketA.getSystemName(),
            "getSystemName() is correct");
        assertEquals( maleSocketB.getObject().getSystemName(), maleSocketB.getSystemName(),
            "getSystemName() is correct");
        assertNotEquals( maleSocketA.getSystemName(), maleSocketB.getSystemName(),
            "getSystemName() are different");
    }

    @Test
    public void testUserName() throws JmriException {
        maleSocketA.setUserName("Test username Abc");
        assertEquals( maleSocketA.getObject().getUserName(), maleSocketA.getUserName(),
            "getUserName() is correct");
        assertEquals( maleSocketB.getObject().getUserName(), maleSocketB.getUserName(),
            "getUserName() is correct");
        assertNotEquals( maleSocketA.getUserName(), maleSocketB.getUserName(),
            "getUserName() are different");

        maleSocketA.getObject().setUserName("Abc");
        assertEquals( "Abc", maleSocketA.getUserName(),
            "getUserName() is correct");

        maleSocketA.getObject().setUserName("Def");
        assertEquals( "Def", maleSocketA.getUserName(),
            "getUserName() is correct");
    }

    @Test
    public void testDisplayName() throws JmriException {
        maleSocketA.setUserName("Test username Abc");
        assertEquals( maleSocketA.getObject().getUserName(), maleSocketA.getUserName(),
            "getUserName() is correct");
        assertEquals( maleSocketB.getObject().getUserName(), maleSocketB.getUserName(),
            "getUserName() is correct");
        assertNotEquals( maleSocketA.getUserName(), maleSocketB.getUserName(),
            "getUserName() are different");

        maleSocketA.getObject().setUserName("Abc");
        assertEquals( "Abc", maleSocketA.getUserName(),
            "getUserName() is correct");

        maleSocketA.getObject().setUserName("Def");
        assertEquals( "Def", maleSocketA.getUserName(),
            "getUserName() is correct");

        assertEquals( ((NamedBean)maleSocketA.getObject()).getDisplayName(), ((NamedBean)maleSocketA).getDisplayName(),
            "getDisplayName() is correct");
        assertEquals( ((NamedBean)maleSocketB.getObject()).getDisplayName(), ((NamedBean)maleSocketB).getDisplayName(),
            "getDisplayName() is correct");
        assertNotEquals( ((NamedBean)maleSocketA).getDisplayName(), ((NamedBean)maleSocketB).getDisplayName(),
            "getDisplayName() are different");
    }

    @Test
    public void testState() throws JmriException {
        ((NamedBean)maleSocketA).setState(NamedBean.UNKNOWN);
        assertEquals( NamedBean.UNKNOWN, ((NamedBean)maleSocketA).getState(),
            "getState() is correct");
        JUnitAppender.assertWarnMessageStartingWith("Unexpected call to setState in ");
        JUnitAppender.assertWarnMessageStartingWith("Unexpected call to getState in ");
    }

    @Test
    public void testComment() throws JmriException {
        ((NamedBean)maleSocketA).setComment("Abc");
        assertEquals( "Abc", ((NamedBean)maleSocketA).getComment(),
            "getComment() is correct");

        ((NamedBean)maleSocketA).setComment("Def");
        assertEquals( "Def", ((NamedBean)maleSocketA).getComment(),
            "getComment() is correct");
    }

    @Test
    public void testProperty() {
        // Remove all properties to be sure we don't hit any problem later
        for (String property : ((NamedBean)maleSocketA).getPropertyKeys()) {
            ((NamedBean)maleSocketA).removeProperty(property);
        }

        // Test set property and read it
        ((NamedBean)maleSocketA).setProperty("Abc", "Something");
        assertEquals( "Something", ((NamedBean)maleSocketA).getProperty("Abc"),
            "getProperty() is correct");
        // Test set property to something else and read it
        ((NamedBean)maleSocketA).setProperty("Abc", "Something else");
        assertEquals( "Something else", ((NamedBean)maleSocketA).getProperty("Abc"),
            "getProperty() is correct");
        assertEquals( 1, ((NamedBean)maleSocketA).getPropertyKeys().size(),
            "num properties");
        // Test set property with another key and read it
        ((NamedBean)maleSocketA).setProperty("Def", "Something different");
        assertEquals( "Something different", ((NamedBean)maleSocketA).getProperty("Def"),
            "getProperty() is correct");
        // Test that the previous key hasn't been changed
        assertEquals( "Something else", ((NamedBean)maleSocketA).getProperty("Abc"),
            "getProperty() is correct");
        assertEquals( 2, ((NamedBean)maleSocketA).getPropertyKeys().size(),
            "num properties");
        // Test removing the property and read it
        ((NamedBean)maleSocketA).removeProperty("Abc");
        assertNull( ((NamedBean)maleSocketA).getProperty("Abc"),
            "getProperty() is null");
        assertEquals( 1, ((NamedBean)maleSocketA).getPropertyKeys().size(), "num properties");
    }

    @Test
    public void testDebugConfig() {
        DebugConfig debugConfig = maleSocketA.createDebugConfig();
        assertNotNull( debugConfig, "debugConfig is not null");
        maleSocketA.setDebugConfig(debugConfig);
        assertSame( debugConfig, maleSocketA.getDebugConfig(), "debugConfig correct");
        maleSocketA.setDebugConfig(null);
        assertNull( maleSocketA.getDebugConfig(), "debugConfig is null");
    }

    abstract public void setUp();

    abstract public void tearDown();

}
