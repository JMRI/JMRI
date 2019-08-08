package jmri.jmrix.internal;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InternalSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        scm = new InternalSystemConnectionMemo();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(InternalSystemConnectionMemoTest.class);
}
