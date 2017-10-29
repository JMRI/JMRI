package jmri.implementation;

import jmri.DccLocoAddress;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
abstract public class AbstractConsistManagerTestBase {

    protected jmri.ConsistManager cm = null;

    // implementing classes should set cm to a valid value in setUp and 
    // cleanup in tearDown.
    @Before
    abstract public void setUp();
    @After
    abstract public void tearDown();


    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",cm);
    }

    @Test
    public void testGetConsist(){
        // getConsist with a valid address should always return
        // a consist.
        DccLocoAddress addr = new DccLocoAddress(5,false);
        Assert.assertNotNull("add consist",cm.getConsist(addr));
    }


    // private final static Logger log = LoggerFactory.getLogger(AbstractConsistManagerTestBase.class);

}
