package jmri.jmrix.cmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * CMRISystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.cmri.CMRISystemConnectionMemo class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CMRISystemConnectionMemoTest {

    @Test public void constructorTest() {
        CMRISystemConnectionMemo m = new CMRISystemConnectionMemo();
        Assert.assertNotNull(m);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }
   
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
