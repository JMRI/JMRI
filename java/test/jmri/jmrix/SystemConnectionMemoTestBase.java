package jmri.jmrix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract base class for SystemConnectionMemo objects.
 *
 * @author Paul Bender Copyright (C) 2017	
 */
abstract public class SystemConnectionMemoTestBase {

    protected SystemConnectionMemo scm = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",scm);
    }

    @Test
    public void testProvidesConsistManager() {
       Assert.assertTrue("Memo Provides Consist Manager",scm.provides(jmri.ConsistManager.class));
    }


    // The minimal setup for log4J
    @Before
    abstract public void setUp();

    @After
    abstract public void tearDown();

}
