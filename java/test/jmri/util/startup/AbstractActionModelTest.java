package jmri.util.startup;

import javax.swing.Action;

import jmri.JmriException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood
 */
public class AbstractActionModelTest {

    /**
     * Test of getClassName method, of class AbstractActionModel.
     */
    @Test
    public void testGetClassName() {
        AbstractActionModel instance = new AbstractActionModelImpl();
        assertNotNull( instance.getClassName(), "Default empty String");
        assertEquals( "", instance.getClassName(), "Default empty String");
        instance.setClassName("oobleck");
        assertNotNull( instance.getClassName(), "Set to oobleck");
        assertEquals( "oobleck", instance.getClassName(), "Set to oobleck");
    }

    /**
     * Test of setClassName method, of class AbstractActionModel.
     */
    @Test
    public void testSetClassName() {
        AbstractActionModel instance = new AbstractActionModelImpl();
        assertNotNull( instance.getClassName(), "Default empty String");
        assertEquals( "", instance.getClassName(), "Default empty String");
        instance.setClassName("oobleck");
        assertNotNull( instance.getClassName(), "Set to oobleck");
        assertEquals( "oobleck", instance.getClassName(), "Set to oobleck");
    }

    /**
     * Test of isValid method, of class AbstractActionModel.
     */
    @Test
    public void testIsValid() {
        AbstractActionModel instance = new AbstractActionModelImpl();
        assertFalse( instance.isValid(), "Default is invalid");
        instance.setClassName("oobleck");
        assertFalse( instance.isValid(), "Invalid class is invalid");
        instance.setClassName(this.getClass().getName());
        assertTrue( instance.isValid(), "Has class found in classpath");
    }

    private static class AbstractActionModelImpl extends AbstractActionModel {

        @Override
        protected void performAction(Action action) throws JmriException {
            // empty method not tested as abstract in class being tested
        }
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
