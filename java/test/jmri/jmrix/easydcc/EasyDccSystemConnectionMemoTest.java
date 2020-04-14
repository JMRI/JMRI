package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the EasyDccSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EasyDccSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       scm = new EasyDccSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
