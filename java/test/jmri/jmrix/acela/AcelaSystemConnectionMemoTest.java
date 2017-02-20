package jmri.jmrix.acela;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the AcelaSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class AcelaSystemConnectionMemoTest {
     
    AcelaSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",memo);
    }

    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       AcelaTrafficController tc = new AcelaTrafficControlScaffold();
       memo = new AcelaSystemConnectionMemo();
    }

    @After
    public void tearDown(){
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
