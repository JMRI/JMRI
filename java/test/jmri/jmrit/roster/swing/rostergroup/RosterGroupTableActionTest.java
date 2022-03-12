package jmri.jmrit.roster.swing.rostergroup;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RosterGroupTableActionTest {

    @Test
    public void testCTor() {
        RosterGroupTableAction t = new RosterGroupTableAction();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RosterGroupTableActionTest.class);

}
