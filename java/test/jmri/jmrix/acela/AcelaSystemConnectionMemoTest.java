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
     
    private AcelaSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",memo);
    }

    @Test
    public void testDefaultCtor(){
       Assert.assertNotNull("exists",new AcelaSystemConnectionMemo());
    }

    @Before
    public void setUp(){
       JUnitUtil.setUp();
       AcelaTrafficController tc = new AcelaTrafficControlScaffold();
       memo = new AcelaSystemConnectionMemo(tc);
    }

    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
