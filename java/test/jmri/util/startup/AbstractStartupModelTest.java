package jmri.util.startup;

import jmri.JmriException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class AbstractStartupModelTest {

    /**
     * Test of getName method, of class AbstractStartupModel.
     */
    @Test
    public void testGetName() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        assertNull( model.getName(), "Name defaults to null");
        model.setName("");
        assertNotNull( model.getName(), "Name should be empty");
        assertTrue( model.getName().isEmpty(), "Name should be empty");
        assertEquals( "", model.getName(), "Name should be empty");
        model.setName("name");
        assertNotNull( model.getName(), "Name should not be empty");
        assertFalse( model.getName().isEmpty(), "Name should not be empty");
        assertEquals( "name", model.getName(), "Name should not be empty");
    }

    /**
     * Test of setName method, of class AbstractStartupModel.
     */
    @Test
    public void testSetName() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        assertNull( model.getName(), "Name defaults to null");
        model.setName("");
        assertNotNull( model.getName(), "Name should be empty");
        assertTrue( model.getName().isEmpty(), "Name should be empty");
        assertEquals( "", model.getName(), "Name should be empty");
        model.setName("name");
        assertNotNull( model.getName(), "Name should not be empty");
        assertFalse( model.getName().isEmpty(), "Name should not be empty");
        assertEquals( "name", model.getName(), "Name should not be empty");
    }

    /**
     * Test of toString method, of class AbstractStartupModel.
     */
    @Test
    public void testToString() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        assertNotNull( model.toString(), "toString defaults to nonnull");
        model.setName("");
        assertNotNull( model.toString(), "toString should be empty");
        assertTrue( model.toString().isEmpty(), "toString should be empty");
        assertEquals( "", model.toString(), "toString should be empty");
        model.setName("name");
        assertNotNull( model.toString(), "toString should not be empty");
        assertFalse( model.toString().isEmpty(), "toString should not be empty");
        assertEquals( "name", model.toString(), "toString should not be empty");
    }

    /**
     * Test of isValid method, of class AbstractStartupModel.
     */
    @Test
    public void testIsValid() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        assertFalse( model.isValid(), "Model default state is invalid");
        model.setName("");
        assertFalse( model.isValid(), "Empty name is invalid");
        model.setName("name");
        assertTrue( model.isValid(), "Nonempty name is valid");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    /**
     * Minimal implementation of AbstractStartupModel
     */
    private static class AbstractStartupModelImpl extends AbstractStartupModel {

        @Override
        public void performAction() throws JmriException {
            // empty method not tested as abstract in class being tested
        }
    }

}
