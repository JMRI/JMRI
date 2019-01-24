package jmri.jmrix;

import org.junit.*;
import javax.swing.JPanel;

/**
 * Base tests for ConnectionConfig objects.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractConnectionConfigTestBase {

    protected ConnectionConfig cc = null;

    @Test 
    public void testCtor(){
        Assert.assertNotNull("Exists",cc);
    }

    @Test
    public void testLoadDetails(){
        // verify no exceptions thrown
        cc.loadDetails(new JPanel());
    }

    @Test
    public void testName(){
        Assert.assertNotNull("has name",cc.name());
    }

    @Test
    public void testGetInfo(){
        Assert.assertNotNull("has info",cc.getInfo());
    }

    @Test
    public void testGetConnectionName(){
        Assert.assertNotNull("has connection name",cc.getConnectionName());
    }

    @Test
    public void testGetAndSetManufacturer(){
        Assert.assertNotNull("has manufacturer",cc.getManufacturer());
	cc.setManufacturer("foo");
        Assert.assertEquals("new manufacturer","foo",cc.getManufacturer());
    }

    @Test
    public void testGetAdapter(){
        Assert.assertNotNull("has adapter",cc.getAdapter());
    }

    @Test
    public void testGetAndSetDisabled(){
	cc.setDisabled(true);
        Assert.assertTrue("disabled",cc.getDisabled());
	cc.setDisabled(false);
        Assert.assertTrue("disabled",cc.getDisabled());
    }

    /**
     * configure the ConnectionConfig object (cc) and any other
     * neccesary objects.
     */
    @Before
    abstract public void setUp();

    /**
     * clean up the ConnectionConfig object (cc) and any other
     * neccesary objects.
     */
    @After
    abstract public void tearDown();

}
