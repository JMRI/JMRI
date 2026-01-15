package jmri.jmrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.*;

import javax.swing.JPanel;

import jmri.JmriException;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.ToDo;

/**
 * Base tests for ConnectionConfig objects.
 *
 * @author Paul Bender Copyright (C) 2018   
 */
abstract public class AbstractConnectionConfigTestBase {

    protected ConnectionConfig cc = null;

    @Test 
    public void testCtor(){
        assertNotNull( cc, "Exists");
    }

    @Test
    public void testLoadDetails() throws JmriException{
        // verify no exceptions thrown
        ThreadingUtil.runOnGUIWithJmriException(
            () -> cc.loadDetails(new JPanel()));
    }

    @Test
    public void testName(){
        assertNotNull( cc.name(), "has name");
    }

    @Test
    public void testGetInfo(){
        assumeTrue( cc.getAdapter() != null, "No cc adapter set");
        assertNotNull("has info", cc.getInfo());
    }

    @Test
    public void testGetConnectionName(){
        assumeTrue( cc.getAdapter() != null, "No cc adapter set");
        assertNotNull( cc.getConnectionName(), "has connection name");
    }

    @Test
    public void testGetAndSetManufacturer(){
        assumeTrue( cc.getAdapter() != null, "No cc adapter set");
        assertNotNull( cc.getManufacturer(), "has manufacturer");
        cc.setManufacturer("foo");
        assertEquals( "foo", cc.getManufacturer(), "new manufacturer");
    }

    @Test
    @ToDo("Remove the assume to see which derived tests fail")
    public void testGetAdapter(){
        // many test classes derived from this class need to trigger
        // creation of the adapter.  Remove the assume to see which
        // ones still fail.  
        assumeTrue( cc.getAdapter() != null, "No cc adapter set");
        assertNotNull( cc.getAdapter(), "has adapter");
    }

    @Test
    public void testGetAndSetDisabled(){
        assumeTrue( cc.getAdapter() != null, "No cc adapter set");
        cc.setDisabled(true);
        assertTrue( cc.getDisabled(), "disabled");
        cc.setDisabled(false);
        assertFalse( cc.getDisabled(), "not disabled");
    }

    /**
     * Configure the ConnectionConfig object (cc) and any other
     * necessary objects.
     */
    abstract public void setUp();

    /**
     * Clean up the ConnectionConfig object (cc) and any other
     * necessary objects.
     */
    abstract public void tearDown();

}
