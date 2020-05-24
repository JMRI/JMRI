package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantTableActionTest {
    WarrantTableAction wta;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", wta);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        wta = WarrantTableAction.getDefault();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantTableActionTest.class);

}
