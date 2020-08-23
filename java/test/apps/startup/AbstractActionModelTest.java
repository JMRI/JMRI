package apps.startup;

import javax.swing.Action;

import jmri.JmriException;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Randall Wood
 */
public class AbstractActionModelTest {
    
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    /**
     * Test of getClassName method, of class AbstractActionModel.
     */
    @Test
    public void testGetClassName() {
        AbstractActionModel instance = new AbstractActionModelImpl();
        Assert.assertNotNull("Default empty String", instance.getClassName());
        Assert.assertEquals("Default empty String", "", instance.getClassName());
        instance.setClassName("oobleck");
        Assert.assertNotNull("Set to oobleck", instance.getClassName());
        Assert.assertEquals("Set to oobleck", "oobleck", instance.getClassName());
    }

    /**
     * Test of setClassName method, of class AbstractActionModel.
     */
    @Test
    public void testSetClassName() {
        AbstractActionModel instance = new AbstractActionModelImpl();
        Assert.assertNotNull("Default empty String", instance.getClassName());
        Assert.assertEquals("Default empty String", "", instance.getClassName());
        instance.setClassName("oobleck");
        Assert.assertNotNull("Set to oobleck", instance.getClassName());
        Assert.assertEquals("Set to oobleck", "oobleck", instance.getClassName());
    }

    /**
     * Test of isValid method, of class AbstractActionModel.
     */
    @Test
    public void testIsValid() {
        AbstractActionModel instance = new AbstractActionModelImpl();
        Assert.assertFalse("Default is invalid", instance.isValid());
        instance.setClassName("oobleck");
        Assert.assertFalse("Invalid class is invalid", instance.isValid());
        instance.setClassName(this.getClass().getName());
        Assert.assertTrue("Has class found in classpath", instance.isValid());
    }

    public class AbstractActionModelImpl extends AbstractActionModel {

        @Override
        protected void performAction(Action action) throws JmriException {
            // empty method not tested as abstract in class being tested
        }
    }
    
}
