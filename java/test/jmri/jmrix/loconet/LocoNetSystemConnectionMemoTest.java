package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the LocoNetSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class LocoNetSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       scm = new LocoNetSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }
}
