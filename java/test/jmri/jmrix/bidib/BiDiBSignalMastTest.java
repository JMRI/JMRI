package jmri.jmrix.bidib;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the BiDiBSignalMast class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSignalMastTest {
    
    BiDiBSystemConnectionMemo memo;
    BiDiBSignalMast t;
    
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("valid systemname", t.getAddr().isValid());
    }
    
    // TODO: more checks on systemname
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        t = new BiDiBSignalMast("BF$bsm:DB-HV-1969:block(Test1:13)");
    }
    
    @After
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();
    }

}
