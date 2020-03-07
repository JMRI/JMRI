package jmri.jmrix.rps;

import jmri.Manager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testValidSystemNameFormat() {
        RpsSystemConnectionMemo memo = (RpsSystemConnectionMemo) scm;
        Assert.assertTrue("valid format - RS(0,0,0);(1,0,0);(1,1,0)", Manager.NameValidity.VALID == memo.validSystemNameFormat("RS(0,0,0);(1,0,0);(1,1,0)", 'S'));

        Assert.assertTrue("invalid format - RS(0,0,0)", Manager.NameValidity.VALID != memo.validSystemNameFormat("RS(0,0,0)", 'S'));
        JUnitAppender.assertWarnMessage("need to have at least 3 points in RS(0,0,0)");

        memo = new RpsSystemConnectionMemo("R2", "RPS");
        Assert.assertTrue("invalid format - R2S(0,0,0);(1,0,0);1,1,0)", Manager.NameValidity.VALID != memo.validSystemNameFormat("R2S(0,0,0);(1,0,0);1,1,0)", 'S'));
        JUnitAppender.assertWarnMessage("missing brackets in point 2: \"1,1,0)\"");

        Assert.assertTrue("invalid format - R2S(0,0,0);(1,0,0);(1)", Manager.NameValidity.VALID != memo.validSystemNameFormat("R2S(0,0,0);(1,0,0);(1)", 'S'));
        JUnitAppender.assertWarnMessage("need to have three coordinates in point 2: \"(1)\"");
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new RpsSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsSystemConnectionMemoTest.class);

}
