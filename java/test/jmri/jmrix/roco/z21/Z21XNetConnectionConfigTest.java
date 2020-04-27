package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Z21XNetConnectionConfigTest extends jmri.jmrix.AbstractStreamConnectionConfigTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        cc = new Z21XNetConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Z21XNetConnectionConfigTest.class);

}
