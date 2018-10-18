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
