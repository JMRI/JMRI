package jmri.jmrit.entryexit.configurexml;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EntryExitPairsXmlTest {

    @Test
    public void testCTor() {
        EntryExitPairsXml t = new EntryExitPairsXml();
        Assert.assertNotNull("exists",t);
    }

    @BeforeAll
    static public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterAll
    static public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EntryExitPairsXmlTest.class);

}
