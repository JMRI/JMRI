package jmri.jmrix.grapevine;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the GrapevineSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class GrapevineSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }
     
    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       new SerialTrafficControlScaffold();
       scm = new GrapevineSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
