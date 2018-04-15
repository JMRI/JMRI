package jmri.jmrix.direct;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the DirectSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DirectSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    // Ctor etc are tested in MemoTestBase

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       scm = new DirectSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
