package apps.startup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class StartupPauseModelTest {
    
    public StartupPauseModelTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }
    
    @After
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
        Assert.assertNotNull(model.getName());
        Assert.assertEquals(Bundle.getMessage("StartupPauseModel.name", model.getDelay()), model.getName());
        model.setDelay(0);
        Assert.assertNotNull(model.getName());
        Assert.assertEquals(Bundle.getMessage("StartupPauseModel.name", model.getDelay()), model.getName());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        Assert.assertNotNull(model.getName());
        Assert.assertEquals(Bundle.getMessage("StartupPauseModel.name", model.getDelay()), model.getName());
        model.setDelay(-1); // invalid
        Assert.assertNotNull(model.getName());
    }

    /**
     * Test of isValid method, of class StartupPauseModel.
     */
    @Test
    public void testIsValid() {
        StartupPauseModel model = new StartupPauseModel();
        Assert.assertFalse(model.isValid());
        model.setDelay(0);
        Assert.assertTrue(model.isValid());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        Assert.assertTrue(model.isValid());
        model.setDelay(-1);
        Assert.assertFalse(model.isValid());
    }

    /**
     * Test of getDelay method, of class StartupPauseModel.
     */
    @Test
    public void testGetDelay() {
        StartupPauseModel model = new StartupPauseModel();
        Assert.assertEquals(-1, model.getDelay());
        model.setDelay(0);
        Assert.assertEquals(0, model.getDelay());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        Assert.assertEquals(10, model.getDelay());
        model.setDelay(-1);
        Assert.assertEquals(-1, model.getDelay());
    }

    /**
     * Test of setDelay method, of class StartupPauseModel.
     */
    @Test
    public void testSetDelay() {
        StartupPauseModel model = new StartupPauseModel();
        Assert.assertEquals(-1, model.getDelay());
        model.setDelay(0);
        Assert.assertEquals(0, model.getDelay());
        model.setDelay(StartupPauseModel.DEFAULT_DELAY);
        Assert.assertEquals(10, model.getDelay());
        model.setDelay(-1);
        Assert.assertEquals(-1, model.getDelay());
    }
    
}
