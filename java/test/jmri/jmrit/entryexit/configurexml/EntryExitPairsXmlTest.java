package jmri.jmrit.entryexit.configurexml;

import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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

    // The minimal setup for log4J
    @BeforeClass
    static public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterClass
    static public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EntryExitPairsXmlTest.class);

}
