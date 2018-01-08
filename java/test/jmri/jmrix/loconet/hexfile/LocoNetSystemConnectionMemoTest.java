package jmri.jmrix.loconet.hexfile;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoNetSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new LocoNetSystemConnectionMemo();
    }
   
    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoNetSystemConnectionMemoTest.class);

}
