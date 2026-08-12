package apps.startup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class StartupPauseModelTest {
    
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    /**
     * Test of getName method, of class StartupPauseModel.
     */
    @Test
    public void testGetName() {
        StartupPauseModel model = new StartupPauseModel();
        // even though model is invalid by default return name as if valid
        assertNotNull(model.getName());
        assertEquals(Bundle.getMessage("StartupPauseModel.name", model.getDelay()), model.getName());
        model.setDelay(0);
        assertNotNull(model.getName());
        assertEquals(Bundle.getMessage("StartupPauseModel.name", model.getDelay()), model.getName());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        assertNotNull(model.getName());
        assertEquals(Bundle.getMessage("StartupPauseModel.name", model.getDelay()), model.getName());
        model.setDelay(-1); // invalid
        assertNotNull(model.getName());
    }

    /**
     * Test of isValid method, of class StartupPauseModel.
     */
    @Test
    public void testIsValid() {
        StartupPauseModel model = new StartupPauseModel();
        assertFalse(model.isValid());
        model.setDelay(0);
        assertTrue(model.isValid());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        assertTrue(model.isValid());
        model.setDelay(-1);
        assertFalse(model.isValid());
    }

    /**
     * Test of getDelay method, of class StartupPauseModel.
     */
    @Test
    public void testGetDelay() {
        StartupPauseModel model = new StartupPauseModel();
        assertEquals(-1, model.getDelay());
        model.setDelay(0);
        assertEquals(0, model.getDelay());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        assertEquals(10, model.getDelay());
        model.setDelay(-1);
        assertEquals(-1, model.getDelay());
    }

    /**
     * Test of setDelay method, of class StartupPauseModel.
     */
    @Test
    public void testSetDelay() {
        StartupPauseModel model = new StartupPauseModel();
        assertEquals(-1, model.getDelay());
        model.setDelay(0);
        assertEquals(0, model.getDelay());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        assertEquals(10, model.getDelay());
        model.setDelay(-1);
        assertEquals(-1, model.getDelay());
    }
    
}
