package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the TamsSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class TamsSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {
     
    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       new TamsInterfaceScaffold();
       scm = new TamsSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
