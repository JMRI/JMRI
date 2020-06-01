package jmri.jmrix.easydcc;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the EasyDccSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EasyDccSystemConnectionMemoTest extends SystemConnectionMemoTestBase<EasyDccSystemConnectionMemo> {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new EasyDccSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
