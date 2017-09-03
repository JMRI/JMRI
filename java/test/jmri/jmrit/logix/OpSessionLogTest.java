package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OpSessionLogTest {

    @Test
    @Ignore("needs more thought")
    public void makeLogFileCheck() {
       // This is going to be a graphical check.
       // make sure the log file is correctly chosen and created.
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OpSessionLogTest.class);

}
