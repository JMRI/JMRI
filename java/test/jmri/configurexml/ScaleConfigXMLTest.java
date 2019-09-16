package jmri.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Scale XML tests
 * @author Dave Sand Copyright (C) 2018
 */
public class ScaleConfigXMLTest {

    @Test
    public void ctorTest() {
        ScaleConfigXML sxml = new ScaleConfigXML();
        Assert.assertNotNull(sxml);
    }

    @Test
    public void testLoad() {
        boolean loadResult = ScaleConfigXML.doLoad();
        Assert.assertTrue("load worked", loadResult);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClassMigrationManagerTest.class);

}
