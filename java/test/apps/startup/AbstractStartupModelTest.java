package apps.startup;

import jmri.JmriException;
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
public class AbstractStartupModelTest {

    public AbstractStartupModelTest() {
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
     * Test of getName method, of class AbstractStartupModel.
     */
    @Test
    public void testGetName() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        Assert.assertNull("Name defaults to null", model.getName());
        model.setName("");
        Assert.assertNotNull("Name should be empty", model.getName());
        Assert.assertTrue("Name should be empty", model.getName().isEmpty());
        Assert.assertEquals("Name should be empty", "", model.getName());
        model.setName("name");
        Assert.assertNotNull("Name should not be empty", model.getName());
        Assert.assertFalse("Name should not be empty", model.getName().isEmpty());
        Assert.assertEquals("Name should not be empty", "name", model.getName());
    }

    /**
     * Test of setName method, of class AbstractStartupModel.
     */
    @Test
    public void testSetName() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        Assert.assertNull("Name defaults to null", model.getName());
        model.setName("");
        Assert.assertNotNull("Name should be empty", model.getName());
        Assert.assertTrue("Name should be empty", model.getName().isEmpty());
        Assert.assertEquals("Name should be empty", "", model.getName());
        model.setName("name");
        Assert.assertNotNull("Name should not be empty", model.getName());
        Assert.assertFalse("Name should not be empty", model.getName().isEmpty());
        Assert.assertEquals("Name should not be empty", "name", model.getName());
    }

    /**
     * Test of toString method, of class AbstractStartupModel.
     */
    @Test
    public void testToString() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        Assert.assertNotNull("toString defaults to nonnull", model.toString());
        model.setName("");
        Assert.assertNotNull("toString should be empty", model.toString());
        Assert.assertTrue("toString should be empty", model.toString().isEmpty());
        Assert.assertEquals("toString should be empty", "", model.toString());
        model.setName("name");
        Assert.assertNotNull("toString should not be empty", model.toString());
        Assert.assertFalse("toString should not be empty", model.toString().isEmpty());
        Assert.assertEquals("toString should not be empty", "name", model.toString());
    }

    /**
     * Test of isValid method, of class AbstractStartupModel.
     */
    @Test
    public void testIsValid() {
        AbstractStartupModel model = new AbstractStartupModelImpl();
        Assert.assertFalse("Model default state is invalid", model.isValid());
        model.setName("");
        Assert.assertFalse("Empty name is invalid", model.isValid());
        model.setName("name");
        Assert.assertTrue("Nonempty name is valid", model.isValid());
    }

    /**
     * Minimal implementation of AbstractStartupModel
     */
    public class AbstractStartupModelImpl extends AbstractStartupModel {

        @Override
        public void performAction() throws JmriException {
            // empty method not tested as abstract in class being tested
        }
    }

}
