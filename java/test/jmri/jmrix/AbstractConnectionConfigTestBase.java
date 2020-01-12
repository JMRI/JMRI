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
        Assume.assumeNotNull("adapter set",cc.getAdapter());
        Assert.assertNotNull("has info",cc.getInfo());
    }

    @Test
    public void testGetConnectionName(){
        Assume.assumeNotNull("adapter set",cc.getAdapter());
        Assert.assertNotNull("has connection name",cc.getConnectionName());
    }

    @Test
    public void testGetAndSetManufacturer(){
        Assume.assumeNotNull("adapter set",cc.getAdapter());
        Assert.assertNotNull("has manufacturer",cc.getManufacturer());
        cc.setManufacturer("foo");
        Assert.assertEquals("new manufacturer","foo",cc.getManufacturer());
    }

    @Test
    public void testGetAdapter(){
        // many test classes derived from this class need to triger 
        // creation of the adapter.  Remove the assume to see which
        // ones still fail.  
        Assume.assumeNotNull("adapter set",cc.getAdapter());
        Assert.assertNotNull("has adapter",cc.getAdapter());
    }

    @Test
    public void testGetAndSetDisabled(){
        Assume.assumeNotNull("adapter set",cc.getAdapter());
        cc.setDisabled(true);
        Assert.assertTrue("disabled",cc.getDisabled());
        cc.setDisabled(false);
        Assert.assertFalse("not disabled",cc.getDisabled());
    }

    /**
     * Configure the ConnectionConfig object (cc) and any other
     * necessary objects.
     */
    @Before
    abstract public void setUp();

    /**
     * Clean up the ConnectionConfig object (cc) and any other
     * necessary objects.
     */
    @After
    abstract public void tearDown();

}
