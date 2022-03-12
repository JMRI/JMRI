package jmri.jmrit.roster.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GlobalRosterEntryComboBoxTest {

    @Test
    public void testCTor() {
        GlobalRosterEntryComboBox t = new GlobalRosterEntryComboBox();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(GlobalRosterEntryComboBoxTest.class);

}
