package jmri.jmrix.qsi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the QsiSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class QsiSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {
    
    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Override 
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       new QsiTrafficControlScaffold();
       scm = new QsiSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
