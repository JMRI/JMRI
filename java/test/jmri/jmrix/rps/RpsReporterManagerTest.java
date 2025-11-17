package jmri.jmrix.rps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.beans.PropertyVetoException;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.NotApplicable;

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

    @Test
    @Override
    @NotApplicable("not possible on RPS so do nothing")
    public void testReporterProvideByNumber() {
    }

    @Test
    @Override
    public void testRegisterDuplicateSystemName()
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, PropertyVetoException {
        super.testRegisterDuplicateSystemName(l,
            l.makeSystemName(getNameToTest1()),
            l.makeSystemName(getNameToTest2()));
    }

    @Test
    public void testCTor() {
        assertNotNull( l, "exists");
    }

    @Override
    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName(getNameToTest1());
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return getNameToTest1();
    }

    @Test
    public void testGetSystemPrefix() {
        assertEquals("R", l.getSystemPrefix());
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
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsReporterManagerTest.class);

}
