package jmri.jmrix.rps;

import jmri.Manager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RpsSystemConnectionMemoTest extends SystemConnectionMemoTestBase<RpsSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testValidSystemNameFormat() {
        Assert.assertTrue("valid format - RS(0,0,0);(1,0,0);(1,1,0)", Manager.NameValidity.VALID == scm.validSystemNameFormat("RS(0,0,0);(1,0,0);(1,1,0)", 'S'));

        Assert.assertTrue("invalid format - RS(0,0,0)", Manager.NameValidity.VALID != scm.validSystemNameFormat("RS(0,0,0)", 'S'));
        JUnitAppender.assertWarnMessage("need to have at least 3 points in RS(0,0,0)");

        scm = new RpsSystemConnectionMemo("R2", "RPS");
        Assert.assertTrue("invalid format - R2S(0,0,0);(1,0,0);1,1,0)", Manager.NameValidity.VALID != scm.validSystemNameFormat("R2S(0,0,0);(1,0,0);1,1,0)", 'S'));
        JUnitAppender.assertWarnMessage("missing brackets in point 2: \"1,1,0)\"");

        Assert.assertTrue("invalid format - R2S(0,0,0);(1,0,0);(1)", Manager.NameValidity.VALID != scm.validSystemNameFormat("R2S(0,0,0);(1,0,0);(1)", 'S'));
        JUnitAppender.assertWarnMessage("need to have three coordinates in point 2: \"(1)\"");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new RpsSystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsSystemConnectionMemoTest.class);
}
