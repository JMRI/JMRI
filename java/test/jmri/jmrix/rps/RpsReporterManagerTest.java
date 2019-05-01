package jmri.jmrix.rps;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return l.getSystemPrefix() + "R" + i;
    }

    @Override
    protected String getNameToTest1() {
        return "(0,0,0);(1,0,0);(1,1,0);(0,1,0)";
    }

    @Override
    protected String getNameToTest2() {
        return "(0,1,0);(1,0,1);(0,1,0);(0,1,0)";
    }

    @Override
    public void testReporterProvideByNumber() {} // not possible on RPS

    @Override
    public void testRegisterDuplicateSystemName() {} // not possible in this form on RPS

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testGetSystemPrefix() {
        Assert.assertEquals("R", l.getSystemPrefix());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new RpsReporterManager(new RpsSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsReporterManagerTest.class);

}
