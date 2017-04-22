package jmri.jmrix.rfid.merg.concentrator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConcentratorSystemConnectionMemoTest.java
 *
 * Description:	tests for the ConcentratorSystemConnectionMemo class
 *
 * @author	Paul Bender Copyright(C) 2016
 */
public class ConcentratorSystemConnectionMemoTest {

    @Test
    public void testCtor() {
        ConcentratorSystemConnectionMemo memo=new ConcentratorSystemConnectionMemo();
        Assert.assertNotNull("exists", memo);
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }

}
