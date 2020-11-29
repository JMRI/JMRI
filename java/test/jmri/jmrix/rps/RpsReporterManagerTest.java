package jmri.jmrix.rps;

import jmri.util.JUnitUtil;

import java.beans.PropertyVetoException;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    public void testReporterProvideByNumber() {
        // not possible on RPS so do nothing
    }

    @Override
    public void testRegisterDuplicateSystemName()
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, PropertyVetoException {
        super.testRegisterDuplicateSystemName(l,
            l.makeSystemName(getNameToTest1()),
            l.makeSystemName(getNameToTest2()));
    }

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName(getNameToTest1());
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return getNameToTest1();
    }

    @Test
    public void testGetSystemPrefix() {
        Assert.assertEquals("R", l.getSystemPrefix());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        l = new RpsReporterManager(new RpsSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsReporterManagerTest.class);

}
