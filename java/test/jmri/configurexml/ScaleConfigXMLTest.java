package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Scale XML tests
 * @author Dave Sand Copyright (C) 2018
 */
public class ScaleConfigXMLTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testLoad() {
        boolean loadResult = ScaleConfigXML.doLoad();
        Assert.assertTrue("load worked", loadResult);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClassMigrationManagerTest.class);

}
