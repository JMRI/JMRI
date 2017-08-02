package jmri.jmrix.openlcb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * OlcbSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbSystemConnectionMemo class
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016	
 */
public class OlcbSystemConnectionMemoTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull(new OlcbSystemConnectionMemo());
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
