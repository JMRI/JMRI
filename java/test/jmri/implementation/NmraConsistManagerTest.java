package jmri.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NmraConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        cm = new NmraConsistManager();
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NmraConsistManagerTest.class);
}
